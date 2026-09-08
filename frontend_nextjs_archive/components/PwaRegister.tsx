"use client";

import { useEffect, useState } from "react";
import { toast } from "sonner";
import { Button } from "@/components/ui/button";
import { Download, WifiOff, X, Sparkles } from "lucide-react";

interface BeforeInstallPromptEvent extends Event {
  prompt: () => Promise<void>;
  userChoice: Promise<{ outcome: "accepted" | "dismissed"; platform: string }>;
}

export function PwaRegister() {
  const [deferredPrompt, setDeferredPrompt] = useState<BeforeInstallPromptEvent | null>(null);
  const [showInstallBanner, setShowInstallBanner] = useState(false);
  const [isInstalled, setIsInstalled] = useState(false);

  useEffect(() => {
    // 1. Register Service Worker
    if (typeof window !== "undefined" && "serviceWorker" in navigator) {
      window.addEventListener("load", () => {
        navigator.serviceWorker
          .register("/sw.js")
          .then((registration) => {
            // Check for updates
            registration.onupdatefound = () => {
              const installingWorker = registration.installing;
              if (installingWorker) {
                installingWorker.onstatechange = () => {
                  if (installingWorker.state === "installed") {
                    if (navigator.serviceWorker.controller) {
                      toast.info("New Spendora version available!", {
                        description: "Refresh to update to the latest experience.",
                        action: {
                          label: "Refresh",
                          onClick: () => window.location.reload(),
                        },
                      });
                    }
                  }
                };
              }
            };
          })
          .catch((err) => {
            console.warn("[Spendora PWA] Service Worker registration:", err);
          });
      });
    }

    // 2. Check if already installed
    if (window.matchMedia("(display-mode: standalone)").matches) {
      setIsInstalled(true);
    }

    // 3. Capture beforeinstallprompt for native-feel install banner
    const handleBeforeInstallPrompt = (e: Event) => {
      e.preventDefault();
      setDeferredPrompt(e as BeforeInstallPromptEvent);
      // Only show if user hasn't dismissed in the last 7 days
      const dismissedTime = localStorage.getItem("spendora_pwa_dismissed");
      if (!dismissedTime || Date.now() - parseInt(dismissedTime) > 7 * 24 * 60 * 60 * 1000) {
        setShowInstallBanner(true);
      }
    };

    const handleAppInstalled = () => {
      setIsInstalled(true);
      setShowInstallBanner(false);
      setDeferredPrompt(null);
      toast.success("Spendora installed successfully!", {
        description: "You can now launch Spendora directly from your home screen or desktop.",
      });
    };

    // 4. Online/Offline network listeners
    const handleOnline = () => {
      toast.success("Back Online", {
        description: "Live financial sync restored.",
      });
    };

    const handleOffline = () => {
      toast.warning("You are Offline", {
        description: "Spendora is running from local cache.",
        icon: <WifiOff className="w-4 h-4 text-amber-500" />,
        duration: 5000,
      });
    };

    window.addEventListener("beforeinstallprompt", handleBeforeInstallPrompt);
    window.addEventListener("appinstalled", handleAppInstalled);
    window.addEventListener("online", handleOnline);
    window.addEventListener("offline", handleOffline);

    return () => {
      window.removeEventListener("beforeinstallprompt", handleBeforeInstallPrompt);
      window.removeEventListener("appinstalled", handleAppInstalled);
      window.removeEventListener("online", handleOnline);
      window.removeEventListener("offline", handleOffline);
    };
  }, []);

  const handleInstallClick = async () => {
    if (!deferredPrompt) return;

    await deferredPrompt.prompt();
    const { outcome } = await deferredPrompt.userChoice;
    if (outcome === "accepted") {
      setShowInstallBanner(false);
      setDeferredPrompt(null);
    }
  };

  const handleDismiss = () => {
    setShowInstallBanner(false);
    localStorage.setItem("spendora_pwa_dismissed", Date.now().toString());
  };

  if (!showInstallBanner || isInstalled || !deferredPrompt) {
    return null;
  }

  return (
    <div className="fixed bottom-4 right-4 z-50 max-w-sm w-[calc(100%-2rem)] sm:w-auto animate-in fade-in slide-in-from-bottom-5 duration-300">
      <div className="flex items-center gap-3 p-3.5 bg-background/95 backdrop-blur-md border border-blue-500/30 rounded-2xl shadow-xl shadow-blue-500/10">
        <div className="w-10 h-10 rounded-xl bg-gradient-to-br from-blue-500 to-emerald-500 flex items-center justify-center text-white shrink-0 shadow-md">
          <Sparkles className="w-5 h-5" />
        </div>
        <div className="flex-1 min-w-0 pr-1">
          <h4 className="text-xs font-bold text-foreground truncate flex items-center gap-1.5">
            Install Spendora App
          </h4>
          <p className="text-[11px] text-muted-foreground line-clamp-1">
            Instant launch & offline access
          </p>
        </div>
        <div className="flex items-center gap-1.5 shrink-0">
          <Button
            size="sm"
            onClick={handleInstallClick}
            className="h-7 px-3 text-xs bg-blue-600 hover:bg-blue-500 text-white font-medium rounded-lg gap-1.5 shadow-sm"
          >
            <Download className="w-3.5 h-3.5" /> Install
          </Button>
          <Button
            variant="ghost"
            size="icon"
            onClick={handleDismiss}
            className="h-7 w-7 text-muted-foreground hover:text-foreground rounded-lg"
            title="Dismiss"
          >
            <X className="w-3.5 h-3.5" />
          </Button>
        </div>
      </div>
    </div>
  );
}
