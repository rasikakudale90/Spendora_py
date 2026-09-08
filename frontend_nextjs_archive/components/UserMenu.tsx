"use client";

import React, { useState, useRef, useEffect } from "react";
import { useAuth } from "@/context/AuthContext";
import { authApi } from "@/lib/api";
import { toast } from "sonner";
import {
  User,
  LogOut,
  KeyRound,
  ShieldAlert,
  ChevronDown,
  Lock,
  CheckCircle2,
  X,
} from "lucide-react";

export function UserMenu() {
  const { user, logout, logoutAll } = useAuth();
  const [isOpen, setIsOpen] = useState(false);
  const [isPasswordModalOpen, setIsPasswordModalOpen] = useState(false);
  const menuRef = useRef<HTMLDivElement>(null);

  // Form state for change password
  const [currentPassword, setCurrentPassword] = useState("");
  const [newPassword, setNewPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [errorMsg, setErrorMsg] = useState("");

  // Close dropdown on outside click
  useEffect(() => {
    function handleClickOutside(event: MouseEvent) {
      if (menuRef.current && !menuRef.current.contains(event.target as Node)) {
        setIsOpen(false);
      }
    }
    document.addEventListener("mousedown", handleClickOutside);
    return () => document.removeEventListener("mousedown", handleClickOutside);
  }, []);

  if (!user) return null;

  const initials = user.full_name
    ? user.full_name
        .split(" ")
        .map((n) => n[0])
        .join("")
        .toUpperCase()
        .slice(0, 2)
    : user.email[0].toUpperCase();

  const handleChangePassword = async (e: React.FormEvent) => {
    e.preventDefault();
    setErrorMsg("");

    if (newPassword !== confirmPassword) {
      setErrorMsg("New passwords do not match");
      return;
    }
    if (newPassword.length < 8) {
      setErrorMsg("New password must be at least 8 characters");
      return;
    }

    setIsSubmitting(true);
    try {
      await authApi.changePassword(currentPassword, newPassword);
      toast.success("Password changed successfully!");
      setIsPasswordModalOpen(false);
      setCurrentPassword("");
      setNewPassword("");
      setConfirmPassword("");
    } catch (err: any) {
      setErrorMsg(err.message || "Failed to change password");
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <>
      <div className="relative" ref={menuRef}>
        <button
          onClick={() => setIsOpen(!isOpen)}
          className="flex items-center gap-2.5 p-1.5 rounded-full hover:bg-muted/70 transition-all border border-border/40 focus:outline-none focus:ring-2 focus:ring-primary/40"
          aria-label="User profile menu"
          id="user-menu-button"
        >
          {user.avatar_url ? (
            // eslint-disable-next-line @next/next/no-img-element
            <img
              src={user.avatar_url}
              alt={user.full_name || user.email}
              className="w-8 h-8 rounded-full object-cover ring-2 ring-primary/20"
            />
          ) : (
            <div className="w-8 h-8 rounded-full bg-gradient-to-tr from-primary to-blue-400 text-white flex items-center justify-center font-bold text-xs shadow-sm">
              {initials}
            </div>
          )}
          <span className="text-xs font-semibold text-foreground hidden md:inline-block max-w-[120px] truncate">
            {user.full_name || user.email.split("@")[0]}
          </span>
          <ChevronDown className={`w-3.5 h-3.5 text-muted-foreground transition-transform duration-200 hidden md:inline-block ${isOpen ? "rotate-180" : ""}`} />
        </button>

        {isOpen && (
          <div className="absolute right-0 mt-2 w-64 rounded-2xl bg-card border border-border/70 shadow-2xl py-2 z-50 animate-in fade-in zoom-in-95 duration-150">
            {/* Header info */}
            <div className="px-4 py-3 border-b border-border/50">
              <p className="text-sm font-semibold text-foreground truncate">
                {user.full_name || "Spendora User"}
              </p>
              <p className="text-xs text-muted-foreground truncate">{user.email}</p>
              <span className="inline-block mt-1 px-2 py-0.5 text-[10px] font-medium bg-primary/10 text-primary rounded-full uppercase tracking-wider">
                {user.auth_provider === "google" ? "Google Connected" : "Email Account"}
              </span>
            </div>

            {/* Actions */}
            <div className="py-1">
              {user.auth_provider !== "google" && (
                <button
                  onClick={() => {
                    setIsOpen(false);
                    setIsPasswordModalOpen(true);
                  }}
                  className="w-full text-left px-4 py-2 text-xs font-medium text-foreground hover:bg-muted/70 flex items-center gap-2.5 transition-colors"
                >
                  <KeyRound className="w-4 h-4 text-muted-foreground" />
                  Change Password
                </button>
              )}

              <button
                onClick={() => {
                  setIsOpen(false);
                  logout();
                }}
                className="w-full text-left px-4 py-2 text-xs font-medium text-foreground hover:bg-muted/70 flex items-center gap-2.5 transition-colors"
              >
                <LogOut className="w-4 h-4 text-muted-foreground" />
                Sign Out
              </button>

              <button
                onClick={() => {
                  setIsOpen(false);
                  logoutAll();
                }}
                className="w-full text-left px-4 py-2 text-xs font-medium text-rose-500 hover:bg-rose-500/10 flex items-center gap-2.5 transition-colors"
              >
                <ShieldAlert className="w-4 h-4 text-rose-500" />
                Sign Out from All Devices
              </button>
            </div>
          </div>
        )}
      </div>

      {/* Change Password Dialog */}
      {isPasswordModalOpen && (
        <div className="fixed inset-0 z-50 bg-black/60 backdrop-blur-sm flex items-center justify-center p-4 animate-in fade-in duration-200">
          <div className="bg-card border border-border rounded-2xl p-6 max-w-md w-full shadow-2xl relative">
            <button
              onClick={() => setIsPasswordModalOpen(false)}
              className="absolute top-4 right-4 text-muted-foreground hover:text-foreground p-1 rounded-lg"
            >
              <X className="w-5 h-5" />
            </button>

            <div className="flex items-center gap-3 mb-5">
              <div className="w-10 h-10 rounded-xl bg-primary/10 text-primary flex items-center justify-center">
                <Lock className="w-5 h-5" />
              </div>
              <div>
                <h3 className="text-base font-bold text-foreground">Change Password</h3>
                <p className="text-xs text-muted-foreground">Keep your account safe with a strong password</p>
              </div>
            </div>

            {errorMsg && (
              <div className="mb-4 p-3 rounded-xl bg-rose-500/10 border border-rose-500/30 text-rose-600 dark:text-rose-400 text-xs font-medium">
                {errorMsg}
              </div>
            )}

            <form onSubmit={handleChangePassword} className="space-y-4">
              <div>
                <label className="block text-xs font-semibold text-foreground mb-1">
                  Current Password
                </label>
                <input
                  type="password"
                  required
                  value={currentPassword}
                  onChange={(e) => setCurrentPassword(e.target.value)}
                  className="w-full px-3 py-2 rounded-xl bg-muted/50 border border-border focus:outline-none focus:ring-2 focus:ring-primary/40 text-sm text-foreground"
                  placeholder="••••••••"
                />
              </div>

              <div>
                <label className="block text-xs font-semibold text-foreground mb-1">
                  New Password
                </label>
                <input
                  type="password"
                  required
                  value={newPassword}
                  onChange={(e) => setNewPassword(e.target.value)}
                  className="w-full px-3 py-2 rounded-xl bg-muted/50 border border-border focus:outline-none focus:ring-2 focus:ring-primary/40 text-sm text-foreground"
                  placeholder="Min. 8 chars, 1 upper, 1 lower, 1 number, 1 symbol"
                />
              </div>

              <div>
                <label className="block text-xs font-semibold text-foreground mb-1">
                  Confirm New Password
                </label>
                <input
                  type="password"
                  required
                  value={confirmPassword}
                  onChange={(e) => setConfirmPassword(e.target.value)}
                  className="w-full px-3 py-2 rounded-xl bg-muted/50 border border-border focus:outline-none focus:ring-2 focus:ring-primary/40 text-sm text-foreground"
                  placeholder="Re-enter new password"
                />
              </div>

              <div className="pt-2 flex gap-3">
                <button
                  type="button"
                  onClick={() => setIsPasswordModalOpen(false)}
                  className="flex-1 py-2.5 px-4 rounded-xl border border-border text-xs font-semibold text-muted-foreground hover:bg-muted/70 transition-colors"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  disabled={isSubmitting}
                  className="flex-1 py-2.5 px-4 rounded-xl bg-primary text-white text-xs font-semibold hover:bg-primary/90 transition-colors shadow-md disabled:opacity-50"
                >
                  {isSubmitting ? "Updating..." : "Update Password"}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </>
  );
}
