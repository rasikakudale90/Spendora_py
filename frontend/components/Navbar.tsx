"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import { useState } from "react";
import {
  LayoutDashboard,
  ReceiptText,
  TrendingUp,
  Menu,
  X,
  Wallet,
  LogIn,
  UserPlus,
} from "lucide-react";
import { cn } from "@/lib/utils";
import ThemeToggle from "./ThemeToggle";
import { useAuth } from "@/context/AuthContext";
import { UserMenu } from "./UserMenu";

const NAV_ITEMS = [
  {
    name: "Dashboard",
    href: "/dashboard",
    icon: LayoutDashboard,
  },
  {
    name: "Expenses",
    href: "/expenses",
    icon: ReceiptText,
  },
  {
    name: "Income",
    href: "/income",
    icon: TrendingUp,
  },
];

export default function Navbar() {
  const pathname = usePathname();
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);
  const { user, isLoading } = useAuth();

  return (
    <header className="sticky top-0 z-50 w-full border-b border-border/60 glass-card">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex items-center justify-between h-16">
          {/* Logo & Brand */}
          <Link href={user ? "/dashboard" : "/login"} className="flex items-center gap-3 group">
            <div className="w-10 h-10 rounded-xl bg-gradient-to-tr from-emerald-500 to-teal-400 flex items-center justify-center shadow-lg shadow-emerald-500/20 group-hover:scale-105 transition-transform">
              <Wallet className="w-5 h-5 text-slate-950 font-bold" />
            </div>
            <div>
              <span className="text-xl font-bold bg-gradient-to-r from-emerald-500 via-teal-400 to-cyan-500 dark:from-emerald-400 dark:via-teal-300 dark:to-cyan-400 bg-clip-text text-transparent">
                Spendora
              </span>
              <span className="hidden sm:inline-block ml-2 text-xs text-emerald-600 dark:text-emerald-400 uppercase tracking-widest font-semibold px-2 py-0.5 rounded-full bg-emerald-500/10 border border-emerald-500/20">
                V1
              </span>
            </div>
          </Link>

          {/* Desktop Navigation Links (only if authenticated) */}
          {user && (
            <nav className="hidden md:flex items-center gap-1">
              {NAV_ITEMS.map((item) => {
                const Icon = item.icon;
                const isActive = pathname.startsWith(item.href);
                return (
                  <Link
                    key={item.href}
                    href={item.href}
                    className={cn(
                      "flex items-center gap-2 px-4 py-2 rounded-lg text-sm font-medium transition-all duration-200",
                      isActive
                        ? "bg-emerald-500/15 text-emerald-600 dark:text-emerald-400 border border-emerald-500/30 shadow-sm shadow-emerald-500/10"
                        : "text-muted-foreground hover:text-foreground hover:bg-slate-200/50 dark:hover:bg-white/5"
                    )}
                  >
                    <Icon className={cn("w-4 h-4", isActive ? "text-emerald-600 dark:text-emerald-400" : "text-muted-foreground")} />
                    {item.name}
                  </Link>
                );
              })}
            </nav>
          )}

          {/* Right actions: Currency, Theme Toggle & User Auth */}
          <div className="flex items-center gap-2.5">
            {/* Live Currency Indicator */}
            <div className="hidden sm:flex items-center gap-2 px-3 py-1.5 rounded-full bg-slate-100 dark:bg-slate-900/80 border border-border/80 text-xs text-emerald-600 dark:text-emerald-400 font-medium">
              <span className="w-2 h-2 rounded-full bg-emerald-500 animate-pulse"></span>
              <span>Currency: INR (₹)</span>
            </div>

            {/* Theme Toggle Button */}
            <ThemeToggle />

            {/* Auth section */}
            {!isLoading && (
              <>
                {user ? (
                  <UserMenu />
                ) : (
                  <div className="flex items-center gap-2">
                    <Link
                      href="/login"
                      className="px-3.5 py-1.5 rounded-xl text-xs font-semibold text-foreground hover:bg-muted/70 transition-colors flex items-center gap-1.5"
                    >
                      <LogIn className="w-3.5 h-3.5" />
                      Sign In
                    </Link>
                    <Link
                      href="/register"
                      className="px-3.5 py-1.5 rounded-xl text-xs font-semibold bg-primary text-white hover:bg-primary/90 transition-colors flex items-center gap-1.5 shadow-sm"
                    >
                      <UserPlus className="w-3.5 h-3.5" />
                      Register
                    </Link>
                  </div>
                )}
              </>
            )}

            {/* Hamburger menu button (mobile) */}
            {user && (
              <div className="flex md:hidden">
                <button
                  onClick={() => setMobileMenuOpen(!mobileMenuOpen)}
                  className="p-2 rounded-lg text-muted-foreground hover:text-foreground hover:bg-slate-200/60 dark:hover:bg-white/10 transition-colors"
                  aria-label="Toggle navigation menu"
                >
                  {mobileMenuOpen ? <X className="w-6 h-6" /> : <Menu className="w-6 h-6" />}
                </button>
              </div>
            )}
          </div>
        </div>
      </div>

      {/* Mobile Drawer (FR-1 Hamburger) */}
      {mobileMenuOpen && user && (
        <div className="md:hidden border-t border-border/60 bg-background/95 backdrop-blur-xl px-4 pt-3 pb-5 space-y-2 animate-in slide-in-from-top-2 duration-200">
          {NAV_ITEMS.map((item) => {
            const Icon = item.icon;
            const isActive = pathname.startsWith(item.href);
            return (
              <Link
                key={item.href}
                href={item.href}
                onClick={() => setMobileMenuOpen(false)}
                className={cn(
                  "flex items-center gap-3 px-4 py-3 rounded-xl text-base font-medium transition-colors",
                  isActive
                    ? "bg-emerald-500/15 text-emerald-600 dark:text-emerald-400 border border-emerald-500/30"
                    : "text-muted-foreground hover:text-foreground hover:bg-slate-200/50 dark:hover:bg-white/5"
                )}
              >
                <Icon className="w-5 h-5 text-emerald-500" />
                {item.name}
              </Link>
            );
          })}
          <div className="pt-2 flex items-center justify-between text-xs text-muted-foreground px-4 border-t border-border/40">
            <span>Currency: INR (₹)</span>
            <span className="px-2 py-0.5 rounded bg-emerald-500/10 text-emerald-600 dark:text-emerald-400 font-medium">
              {user.email}
            </span>
          </div>
        </div>
      )}
    </header>
  );
}
