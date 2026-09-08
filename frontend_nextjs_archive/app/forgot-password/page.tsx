"use client";

import React, { useState, useEffect, useRef } from "react";
import Link from "next/link";
import { authApi } from "@/lib/api";
import { toast } from "sonner";
import {
  Wallet,
  Mail,
  ArrowLeft,
  KeyRound,
  CheckCircle2,
  Lock,
  Eye,
  EyeOff,
  RefreshCw,
  Sparkles,
  Check,
  X,
  ShieldCheck,
  Clock,
  AlertTriangle,
} from "lucide-react";

export default function ForgotPasswordPage() {
  // Multi-step State: 1 = Email Input, 2 = OTP & New Password, 3 = Reset Success
  const [step, setStep] = useState<1 | 2 | 3>(1);

  const [email, setEmail] = useState("");
  const [otpDigits, setOtpDigits] = useState<string[]>(["", "", "", ""]);
  const [devOtp, setDevOtp] = useState<string | null>(null);

  const [newPassword, setNewPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [showPassword, setShowPassword] = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);

  const [isLoading, setIsLoading] = useState(false);
  
  // 50-second OTP expiration countdown
  const [otpSecondsLeft, setOtpSecondsLeft] = useState(50);

  // References to the 4 OTP input elements
  const otpInputRefs = useRef<(HTMLInputElement | null)[]>([]);

  // 50-Second Countdown timer effect
  useEffect(() => {
    if (step !== 2 || otpSecondsLeft <= 0) return;
    const interval = setInterval(() => {
      setOtpSecondsLeft((prev) => (prev > 0 ? prev - 1 : 0));
    }, 1000);
    return () => clearInterval(interval);
  }, [step, otpSecondsLeft]);

  // Handle Step 1 & Resend: Send 50s OTP
  const handleSendOtp = async (e?: React.FormEvent) => {
    if (e) e.preventDefault();
    if (!email || !email.includes("@")) {
      toast.error("Please enter a valid email address");
      return;
    }

    setIsLoading(true);
    try {
      const res = await authApi.forgotPassword(email);
      setDevOtp(res.dev_otp || res.dev_reset_token || null);
      setOtpDigits(["", "", "", ""]);
      setOtpSecondsLeft(50); // Reset timer to 50 seconds
      setStep(2);
      toast.success("New 4-digit OTP sent (valid for 50 seconds)!");
      setTimeout(() => {
        otpInputRefs.current[0]?.focus();
      }, 150);
    } catch (err: any) {
      toast.error(err.message || "Failed to send reset code");
    } finally {
      setIsLoading(false);
    }
  };

  // Handle OTP digit changes with auto-advance and backspace handling
  const handleOtpChange = (index: number, val: string) => {
    if (otpSecondsLeft === 0) {
      toast.error("This OTP has expired. Please click 'Resend OTP Code' to generate a fresh code.");
      return;
    }

    if (val.length > 1) {
      const digits = val.replace(/\D/g, "").slice(0, 4).split("");
      const newDigits = [...otpDigits];
      digits.forEach((d, i) => {
        if (i < 4) newDigits[i] = d;
      });
      setOtpDigits(newDigits);
      const nextFocusIdx = Math.min(digits.length, 3);
      otpInputRefs.current[nextFocusIdx]?.focus();
      return;
    }

    const cleanChar = val.replace(/\D/g, "");
    const updated = [...otpDigits];
    updated[index] = cleanChar;
    setOtpDigits(updated);

    if (cleanChar && index < 3) {
      otpInputRefs.current[index + 1]?.focus();
    }
  };

  const handleOtpKeyDown = (index: number, e: React.KeyboardEvent<HTMLInputElement>) => {
    if (e.key === "Backspace" && !otpDigits[index] && index > 0) {
      otpInputRefs.current[index - 1]?.focus();
    }
  };

  const handleOtpPaste = (e: React.ClipboardEvent<HTMLInputElement>) => {
    e.preventDefault();
    if (otpSecondsLeft === 0) {
      toast.error("This OTP has expired. Please click 'Resend OTP Code' to generate a fresh code.");
      return;
    }

    const pasted = e.clipboardData.getData("text").replace(/\D/g, "").slice(0, 4);
    if (!pasted) return;

    const newDigits = ["", "", "", ""];
    pasted.split("").forEach((d, idx) => {
      if (idx < 4) newDigits[idx] = d;
    });
    setOtpDigits(newDigits);
    const nextIdx = Math.min(pasted.length, 3);
    otpInputRefs.current[nextIdx]?.focus();
  };

  const handleQuickFillDevOtp = () => {
    if (!devOtp) return;
    if (otpSecondsLeft === 0) {
      toast.error("This OTP has expired. Please click 'Resend OTP Code' to generate a fresh code.");
      return;
    }
    const digits = devOtp.slice(0, 4).split("");
    setOtpDigits(digits);
    toast.info("Auto-filled development OTP!");
  };

  // Password criteria checkers
  const passwordCriteria = [
    { label: "At least 8 characters", met: newPassword.length >= 8 },
    { label: "One uppercase letter", met: /[A-Z]/.test(newPassword) },
    { label: "One lowercase letter", met: /[a-z]/.test(newPassword) },
    { label: "One number", met: /\d/.test(newPassword) },
    { label: "One special character", met: /[!@#$%^&*(),.?":{}|<>]/.test(newPassword) },
  ];
  const allCriteriaMet = passwordCriteria.every((c) => c.met);

  // Handle Step 2: Reset Password with OTP
  const handleResetPassword = async (e: React.FormEvent) => {
    e.preventDefault();

    if (otpSecondsLeft === 0) {
      toast.error("This OTP has expired after 50 seconds. Please click 'Resend OTP Code' to generate a new code.");
      return;
    }

    const fullOtp = otpDigits.join("");
    if (fullOtp.length !== 4) {
      toast.error("Please enter the complete 4-digit OTP code");
      return;
    }

    if (!allCriteriaMet) {
      toast.error("Password does not meet all security requirements");
      return;
    }

    if (newPassword !== confirmPassword) {
      toast.error("Passwords do not match");
      return;
    }

    setIsLoading(true);
    try {
      await authApi.resetPassword(email, fullOtp, newPassword);
      setStep(3);
      toast.success("Password reset successfully!");
    } catch (err: any) {
      toast.error(err.message || "Failed to reset password. The OTP may be invalid or expired.");
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="min-h-[85vh] flex items-center justify-center py-10 px-4">
      <div className="w-full max-w-md">
        {/* Brand Header */}
        <div className="text-center mb-8">
          <div className="inline-flex items-center justify-center w-12 h-12 rounded-2xl bg-gradient-to-tr from-emerald-500 to-teal-400 text-slate-950 font-bold shadow-lg shadow-emerald-500/20 mb-3">
            <Wallet className="w-6 h-6" />
          </div>
          <h1 className="text-2xl font-black tracking-tight text-foreground">
            {step === 1 && "Forgot Password"}
            {step === 2 && "Enter Verification Code"}
            {step === 3 && "Password Updated"}
          </h1>
          <p className="text-xs sm:text-sm text-muted-foreground mt-1">
            {step === 1 && "Enter your registered email to receive a 4-digit OTP"}
            {step === 2 && `Sent 4-digit code to ${email} (valid for 50 seconds)`}
            {step === 3 && "Your account password has been updated securely"}
          </p>
        </div>

        {/* Card */}
        <div className="glass-card border border-border/70 rounded-3xl p-6 sm:p-8 shadow-2xl backdrop-blur-xl">
          {/* STEP 1: Enter Email */}
          {step === 1 && (
            <form onSubmit={handleSendOtp} className="space-y-4">
              <div>
                <label className="block text-xs font-semibold text-foreground mb-1.5">
                  Email Address
                </label>
                <div className="relative">
                  <Mail className="w-4 h-4 text-muted-foreground absolute left-3.5 top-1/2 -translate-y-1/2" />
                  <input
                    type="email"
                    required
                    value={email}
                    onChange={(e) => setEmail(e.target.value)}
                    className="w-full pl-10 pr-4 py-2.5 rounded-xl bg-muted/40 border border-border/80 focus:outline-none focus:ring-2 focus:ring-primary/40 text-sm text-foreground placeholder:text-muted-foreground/60 transition-all"
                    placeholder="you@example.com"
                  />
                </div>
              </div>

              <button
                type="submit"
                disabled={isLoading || !email}
                className="w-full py-3 px-4 rounded-xl bg-primary text-white text-sm font-semibold hover:bg-primary/90 transition-all duration-200 shadow-md shadow-primary/20 flex items-center justify-center gap-2 disabled:opacity-50 mt-2"
              >
                {isLoading ? (
                  <>
                    <RefreshCw className="w-4 h-4 animate-spin" />
                    Sending OTP...
                  </>
                ) : (
                  <>
                    <KeyRound className="w-4 h-4" />
                    Send 4-Digit Code
                  </>
                )}
              </button>

              <div className="mt-4 text-center">
                <Link
                  href="/login"
                  className="inline-flex items-center gap-1.5 text-xs text-muted-foreground hover:text-foreground font-medium transition-colors"
                >
                  <ArrowLeft className="w-3.5 h-3.5" />
                  Back to Sign In
                </Link>
              </div>
            </form>
          )}

          {/* STEP 2: Enter 4-Digit OTP & New Password */}
          {step === 2 && (
            <form onSubmit={handleResetPassword} className="space-y-5">
              {/* Email Badge with Change Option */}
              <div className="flex items-center justify-between p-2.5 rounded-xl bg-muted/40 border border-border/60 text-xs">
                <span className="text-muted-foreground truncate max-w-[200px]">{email}</span>
                <button
                  type="button"
                  onClick={() => setStep(1)}
                  className="text-primary hover:underline font-semibold text-[11px]"
                >
                  Change Email
                </button>
              </div>

              {/* 50-Second Countdown / Expiry Status Badge */}
              <div className="flex items-center justify-center">
                {otpSecondsLeft > 0 ? (
                  <div className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full bg-emerald-500/10 border border-emerald-500/30 text-emerald-600 dark:text-emerald-400 text-xs font-semibold animate-pulse">
                    <Clock className="w-3.5 h-3.5" />
                    <span>OTP expires in {otpSecondsLeft}s</span>
                  </div>
                ) : (
                  <div className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full bg-rose-500/10 border border-rose-500/30 text-rose-600 dark:text-rose-400 text-xs font-semibold">
                    <AlertTriangle className="w-3.5 h-3.5" />
                    <span>OTP expired! Request a new code below.</span>
                  </div>
                )}
              </div>

              {/* 4-Digit OTP Boxes */}
              <div>
                <label className="block text-xs font-semibold text-foreground mb-2 text-center">
                  Enter 4-Digit Verification Code
                </label>
                <div className="flex justify-center gap-2.5 sm:gap-3">
                  {otpDigits.map((digit, idx) => (
                    <input
                      key={idx}
                      ref={(el) => {
                        otpInputRefs.current[idx] = el;
                      }}
                      type="text"
                      inputMode="numeric"
                      pattern="[0-9]*"
                      maxLength={1}
                      disabled={otpSecondsLeft === 0}
                      value={digit}
                      onChange={(e) => handleOtpChange(idx, e.target.value)}
                      onKeyDown={(e) => handleOtpKeyDown(idx, e)}
                      onPaste={handleOtpPaste}
                      className={`w-12 h-14 sm:w-14 sm:h-16 text-center text-xl sm:text-2xl font-black font-mono rounded-2xl bg-muted/50 border-2 transition-all duration-150 shadow-inner ${
                        otpSecondsLeft === 0
                          ? "border-rose-500/40 opacity-60 cursor-not-allowed bg-rose-500/5 text-muted-foreground"
                          : "border-border/80 focus:border-primary focus:bg-primary/5 focus:outline-none focus:ring-4 focus:ring-primary/20 text-foreground"
                      }`}
                    />
                  ))}
                </div>

                {/* Dev Quick-Action */}
                {devOtp && otpSecondsLeft > 0 && (
                  <div className="mt-3 flex items-center justify-center">
                    <button
                      type="button"
                      onClick={handleQuickFillDevOtp}
                      className="inline-flex items-center gap-1 text-[11px] font-semibold text-blue-500 hover:text-blue-400 bg-blue-500/10 hover:bg-blue-500/20 px-2.5 py-1 rounded-lg transition-colors"
                    >
                      <Sparkles className="w-3 h-3" />
                      Quick Fill Dev OTP ({devOtp})
                    </button>
                  </div>
                )}
              </div>

              {/* New Password */}
              <div>
                <label className="block text-xs font-semibold text-foreground mb-1.5">
                  New Password
                </label>
                <div className="relative">
                  <Lock className="w-4 h-4 text-muted-foreground absolute left-3.5 top-1/2 -translate-y-1/2" />
                  <input
                    type={showPassword ? "text" : "password"}
                    required
                    value={newPassword}
                    onChange={(e) => setNewPassword(e.target.value)}
                    className="w-full pl-10 pr-10 py-2.5 rounded-xl bg-muted/40 border border-border/80 focus:outline-none focus:ring-2 focus:ring-primary/40 text-sm text-foreground placeholder:text-muted-foreground/60 transition-all"
                    placeholder="••••••••"
                  />
                  <button
                    type="button"
                    onClick={() => setShowPassword(!showPassword)}
                    className="absolute right-3 top-1/2 -translate-y-1/2 text-muted-foreground hover:text-foreground"
                  >
                    {showPassword ? <EyeOff className="w-4 h-4" /> : <Eye className="w-4 h-4" />}
                  </button>
                </div>
              </div>

              {/* Confirm Password */}
              <div>
                <label className="block text-xs font-semibold text-foreground mb-1.5">
                  Confirm New Password
                </label>
                <div className="relative">
                  <Lock className="w-4 h-4 text-muted-foreground absolute left-3.5 top-1/2 -translate-y-1/2" />
                  <input
                    type={showConfirmPassword ? "text" : "password"}
                    required
                    value={confirmPassword}
                    onChange={(e) => setConfirmPassword(e.target.value)}
                    className="w-full pl-10 pr-10 py-2.5 rounded-xl bg-muted/40 border border-border/80 focus:outline-none focus:ring-2 focus:ring-primary/40 text-sm text-foreground placeholder:text-muted-foreground/60 transition-all"
                    placeholder="••••••••"
                  />
                  <button
                    type="button"
                    onClick={() => setShowConfirmPassword(!showConfirmPassword)}
                    className="absolute right-3 top-1/2 -translate-y-1/2 text-muted-foreground hover:text-foreground"
                  >
                    {showConfirmPassword ? <EyeOff className="w-4 h-4" /> : <Eye className="w-4 h-4" />}
                  </button>
                </div>
              </div>

              {/* Password Requirements Checklist */}
              {newPassword.length > 0 && (
                <div className="p-3 rounded-xl bg-muted/30 border border-border/60 space-y-1 text-[11px]">
                  <p className="font-semibold text-muted-foreground mb-1.5">Password Requirements:</p>
                  <div className="grid grid-cols-1 sm:grid-cols-2 gap-1">
                    {passwordCriteria.map((c, i) => (
                      <div
                        key={i}
                        className={`flex items-center gap-1.5 ${
                          c.met ? "text-emerald-500 font-medium" : "text-muted-foreground"
                        }`}
                      >
                        {c.met ? <Check className="w-3 h-3" /> : <X className="w-3 h-3 opacity-60" />}
                        <span>{c.label}</span>
                      </div>
                    ))}
                  </div>
                </div>
              )}

              {/* Submit Button */}
              <button
                type="submit"
                disabled={
                  isLoading ||
                  otpSecondsLeft === 0 ||
                  otpDigits.some((d) => !d) ||
                  !allCriteriaMet ||
                  newPassword !== confirmPassword
                }
                className="w-full py-3 px-4 rounded-xl bg-primary text-white text-sm font-semibold hover:bg-primary/90 transition-all duration-200 shadow-md shadow-primary/20 flex items-center justify-center gap-2 disabled:opacity-50"
              >
                {isLoading ? (
                  <>
                    <RefreshCw className="w-4 h-4 animate-spin" />
                    Resetting Password...
                  </>
                ) : (
                  <>
                    <ShieldCheck className="w-4 h-4" />
                    Verify & Reset Password
                  </>
                )}
              </button>

              {/* Resend OTP & Back */}
              <div className="flex items-center justify-between pt-1 text-xs">
                <button
                  type="button"
                  onClick={() => handleSendOtp()}
                  disabled={isLoading}
                  className="text-primary hover:underline font-semibold flex items-center gap-1 disabled:text-muted-foreground"
                >
                  <RefreshCw className={`w-3 h-3 ${isLoading ? "animate-spin" : ""}`} />
                  {otpSecondsLeft === 0 ? "Resend New OTP Code" : `Resend Code (${otpSecondsLeft}s)`}
                </button>

                <Link
                  href="/login"
                  className="inline-flex items-center gap-1 text-muted-foreground hover:text-foreground font-medium"
                >
                  <ArrowLeft className="w-3.5 h-3.5" />
                  Sign In
                </Link>
              </div>
            </form>
          )}

          {/* STEP 3: Success Screen */}
          {step === 3 && (
            <div className="text-center space-y-4 py-2">
              <div className="w-14 h-14 rounded-full bg-emerald-500/15 text-emerald-500 flex items-center justify-center mx-auto shadow-lg shadow-emerald-500/10">
                <CheckCircle2 className="w-8 h-8" />
              </div>
              <h3 className="text-lg font-bold text-foreground">Password Reset Successfully!</h3>
              <p className="text-xs text-muted-foreground max-w-xs mx-auto">
                Your password has been changed securely. All existing sessions have been signed out.
              </p>

              <div className="pt-4">
                <Link
                  href={`/login?email=${encodeURIComponent(email)}`}
                  className="w-full py-3 px-4 rounded-xl bg-primary text-white text-sm font-semibold hover:bg-primary/90 transition-all duration-200 shadow-md shadow-primary/20 inline-flex items-center justify-center gap-2"
                >
                  Proceed to Sign In
                </Link>
              </div>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
