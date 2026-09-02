"use client";

import React, { createContext, useContext, useEffect, useState } from "react";
import { useRouter, usePathname } from "next/navigation";
import { User, authApi } from "@/lib/api";

interface AuthContextType {
  user: User | null;
  isLoading: boolean;
  login: (data: { email: string; password: string }) => Promise<void>;
  register: (data: { email: string; password: string; full_name?: string }) => Promise<void>;
  googleSignIn: (credential: string) => Promise<void>;
  logout: () => Promise<void>;
  logoutAll: () => Promise<void>;
  refreshUser: () => Promise<void>;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

const PUBLIC_PATHS = ["/login", "/register", "/forgot-password", "/reset-password"];

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [user, setUser] = useState<User | null>(null);
  const [isLoading, setIsLoading] = useState<boolean>(true);
  const router = useRouter();
  const pathname = usePathname();

  const refreshUser = async () => {
    try {
      const authRes = await authApi.refreshToken();
      setUser(authRes.user);
    } catch {
      setUser(null);
    }
  };

  useEffect(() => {
    let warmupTimer: NodeJS.Timeout | null = setTimeout(() => {
      // If server took > 2.5s, inform the user it's waking up
      try {
        const { toast } = require("sonner");
        toast.info("Connecting to server (waking up cloud instance)...", {
          id: "server-wakeup",
          duration: 45000,
        });
      } catch {}
    }, 2500);

    const initAuth = async () => {
      try {
        const authRes = await authApi.refreshToken();
        setUser(authRes.user);
      } catch {
        setUser(null);
      } finally {
        if (warmupTimer) {
          clearTimeout(warmupTimer);
          warmupTimer = null;
        }
        try {
          const { toast } = require("sonner");
          toast.dismiss("server-wakeup");
        } catch {}
        setIsLoading(false);
      }
    };
    initAuth();

    return () => {
      if (warmupTimer) clearTimeout(warmupTimer);
    };
  }, []);

  // Route protection
  useEffect(() => {
    if (isLoading) return;

    const isPublic = PUBLIC_PATHS.some((path) => pathname.startsWith(path));

    if (!user && !isPublic && pathname !== "/") {
      router.replace(`/login?redirect=${encodeURIComponent(pathname)}`);
    } else if (user && isPublic) {
      router.replace("/dashboard");
    }
  }, [user, isLoading, pathname, router]);

  const login = async (data: { email: string; password: string }) => {
    const res = await authApi.login(data);
    setUser(res.user);
  };

  const register = async (data: { email: string; password: string; full_name?: string }) => {
    await authApi.register(data);
    // User is created but not logged in. Must sign in on /login.
  };

  const googleSignIn = async (credential: string) => {
    const res = await authApi.googleSignIn(credential);
    setUser(res.user);
  };

  const logout = async () => {
    try {
      await authApi.logout();
    } finally {
      setUser(null);
      router.replace("/login");
    }
  };

  const logoutAll = async () => {
    try {
      await authApi.logoutAll();
    } finally {
      setUser(null);
      router.replace("/login");
    }
  };

  return (
    <AuthContext.Provider
      value={{
        user,
        isLoading,
        login,
        register,
        googleSignIn,
        logout,
        logoutAll,
        refreshUser,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = (): AuthContextType => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error("useAuth must be used within an AuthProvider");
  }
  return context;
};
