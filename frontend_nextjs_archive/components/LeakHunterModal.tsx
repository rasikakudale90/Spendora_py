"use client";

import React, { useState, useEffect } from "react";
import { aiApi, LeakAnalysisResponse } from "@/lib/api";
import { toast } from "sonner";
import {
  Search,
  RefreshCw,
  X,
  CreditCard,
  Coffee,
  AlertOctagon,
  ShieldCheck,
  TrendingDown,
  Calendar,
  Sparkles,
  Zap,
  DollarSign,
  CheckCircle2,
} from "lucide-react";

interface LeakHunterModalProps {
  isOpen: boolean;
  onClose: () => void;
}

export function LeakHunterModal({ isOpen, onClose }: LeakHunterModalProps) {
  const [loading, setLoading] = useState(true);
  const [data, setData] = useState<LeakAnalysisResponse | null>(null);
  const [activeTab, setActiveTab] = useState<"subscriptions" | "microleaks">("subscriptions");

  const fetchLeaks = async () => {
    try {
      setLoading(true);
      const res = await aiApi.getLeakAnalysis();
      setData(res);
    } catch (err: any) {
      toast.error(err.message || "Failed to audit financial leaks");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (isOpen) {
      fetchLeaks();
    }
  }, [isOpen]);

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-background/80 backdrop-blur-md animate-in fade-in duration-200">
      <div className="glass-card relative w-full max-w-2xl max-h-[90vh] overflow-y-auto rounded-3xl border border-border/80 p-6 sm:p-8 shadow-2xl bg-card/95">
        {/* Close Button */}
        <button
          onClick={onClose}
          className="absolute top-5 right-5 p-2 rounded-full text-muted-foreground hover:text-foreground hover:bg-muted/60 transition-colors"
          aria-label="Close modal"
        >
          <X className="w-5 h-5" />
        </button>

        {/* Header */}
        <div className="flex items-center gap-3 mb-6">
          <div className="w-12 h-12 rounded-2xl bg-gradient-to-tr from-amber-500 to-rose-500 text-white flex items-center justify-center font-bold shadow-lg shadow-amber-500/20">
            <Search className="w-6 h-6" />
          </div>
          <div>
            <h2 className="text-xl font-black text-foreground flex items-center gap-2">
              Autonomous Leak Hunter
              <span className="text-[10px] uppercase font-bold tracking-wider px-2 py-0.5 rounded-full bg-amber-500/10 text-amber-500 border border-amber-500/20">
                AI Audit
              </span>
            </h2>
            <p className="text-xs text-muted-foreground mt-0.5">
              Audits recurring subscriptions, micro-spending, and hidden money drains over 90 days.
            </p>
          </div>
        </div>

        {/* Loading State */}
        {loading ? (
          <div className="py-16 text-center space-y-3">
            <RefreshCw className="w-8 h-8 animate-spin text-primary mx-auto" />
            <p className="text-sm font-semibold text-foreground">
              Scanning 90 days of transactions for hidden leaks & subscriptions...
            </p>
            <p className="text-xs text-muted-foreground max-w-xs mx-auto">
              Extracting recurring intervals and micro-spending patterns with AI.
            </p>
          </div>
        ) : !data ? (
          <div className="py-12 text-center text-muted-foreground text-sm">
            Could not load leak audit data. Please try again.
          </div>
        ) : (
          <div className="space-y-5 animate-in fade-in duration-300">
            {/* Top Metric Cards */}
            <div className="grid grid-cols-2 sm:grid-cols-3 gap-3">
              {/* Monthly Drain */}
              <div className="p-3.5 rounded-2xl bg-rose-500/10 border border-rose-500/20">
                <span className="text-[11px] font-semibold text-rose-600 dark:text-rose-400 block">
                  Monthly Total Leak
                </span>
                <span className="text-lg sm:text-xl font-black text-rose-600 dark:text-rose-400 font-mono mt-1 block">
                  ₹{Number(data.total_monthly_leak).toLocaleString("en-IN", { minimumFractionDigits: 2 })}
                  <span className="text-xs font-normal">/mo</span>
                </span>
              </div>

              {/* Annualized Projection */}
              <div className="p-3.5 rounded-2xl bg-amber-500/10 border border-amber-500/20">
                <span className="text-[11px] font-semibold text-amber-600 dark:text-amber-400 block">
                  Annualized Drain
                </span>
                <span className="text-lg sm:text-xl font-black text-amber-600 dark:text-amber-400 font-mono mt-1 block">
                  ₹{Number(data.total_annual_projected_leak).toLocaleString("en-IN", { minimumFractionDigits: 0 })}
                  <span className="text-xs font-normal">/yr</span>
                </span>
              </div>

              {/* Counts */}
              <div className="p-3.5 rounded-2xl bg-muted/40 border border-border/60 col-span-2 sm:col-span-1 flex flex-col justify-center">
                <span className="text-[11px] font-semibold text-muted-foreground block">
                  Detected Drains
                </span>
                <div className="flex items-center gap-3 mt-1 text-xs font-bold text-foreground">
                  <span>{data.subscription_count} Subscriptions</span>
                  <span>•</span>
                  <span>{data.micro_leak_count} Micro-Leaks</span>
                </div>
              </div>
            </div>

            {/* AI Summary Banner */}
            <div className="p-4 rounded-2xl bg-gradient-to-r from-amber-500/10 via-rose-500/10 to-indigo-500/10 border border-amber-500/20 space-y-1.5">
              <div className="flex items-center justify-between">
                <span className="text-xs font-bold text-amber-600 dark:text-amber-400 flex items-center gap-1.5">
                  <Sparkles className="w-3.5 h-3.5" /> AI Audit Summary
                </span>
                <span className="text-[10px] text-muted-foreground font-mono">
                  {data.provider_used}
                </span>
              </div>
              <p className="text-xs text-foreground/90 leading-relaxed">
                {data.ai_summary}
              </p>
            </div>

            {/* Tabs */}
            <div className="flex items-center gap-2 p-1 rounded-xl bg-muted/50 border border-border/60">
              <button
                type="button"
                onClick={() => setActiveTab("subscriptions")}
                className={`flex-1 py-2 px-3 rounded-lg text-xs font-semibold transition-all flex items-center justify-center gap-1.5 ${
                  activeTab === "subscriptions"
                    ? "bg-card text-foreground shadow-sm border border-border/40"
                    : "text-muted-foreground hover:text-foreground"
                }`}
              >
                <CreditCard className="w-3.5 h-3.5" />
                Active Subscriptions ({data.detected_subscriptions.length})
              </button>
              <button
                type="button"
                onClick={() => setActiveTab("microleaks")}
                className={`flex-1 py-2 px-3 rounded-lg text-xs font-semibold transition-all flex items-center justify-center gap-1.5 ${
                  activeTab === "microleaks"
                    ? "bg-card text-foreground shadow-sm border border-border/40"
                    : "text-muted-foreground hover:text-foreground"
                }`}
              >
                <Coffee className="w-3.5 h-3.5" />
                Micro-Leaks & Fees ({data.micro_spending_leaks.length})
              </button>
            </div>

            {/* TAB 1: Subscriptions List */}
            {activeTab === "subscriptions" && (
              <div className="space-y-2.5">
                {data.detected_subscriptions.length === 0 ? (
                  <div className="p-8 text-center rounded-2xl bg-muted/20 border border-dashed border-border/60">
                    <CheckCircle2 className="w-8 h-8 text-emerald-500 mx-auto mb-2 opacity-80" />
                    <p className="text-xs font-semibold text-foreground">
                      No active subscriptions detected!
                    </p>
                    <p className="text-[11px] text-muted-foreground mt-0.5">
                      No repeating charges found in the past 90 days.
                    </p>
                  </div>
                ) : (
                  data.detected_subscriptions.map((sub, idx) => (
                    <div
                      key={idx}
                      className="p-3.5 rounded-2xl bg-muted/30 border border-border/60 flex items-center justify-between hover:bg-muted/50 transition-colors"
                    >
                      <div className="flex items-center gap-3">
                        <div className="w-9 h-9 rounded-xl bg-indigo-500/10 text-indigo-500 flex items-center justify-center shrink-0">
                          <CreditCard className="w-4 h-4" />
                        </div>
                        <div>
                          <p className="text-xs font-bold text-foreground">{sub.title}</p>
                          <p className="text-[11px] text-muted-foreground">
                            {sub.occurrence_count} charges recorded • Last: {sub.last_date}
                          </p>
                        </div>
                      </div>
                      <div className="text-right">
                        <p className="text-xs font-extrabold text-foreground font-mono">
                          ₹{Number(sub.estimated_monthly_cost).toLocaleString("en-IN")}/mo
                        </p>
                        <p className="text-[10px] text-muted-foreground font-mono">
                          ~₹{(Number(sub.estimated_monthly_cost) * 12).toLocaleString("en-IN")}/yr
                        </p>
                      </div>
                    </div>
                  ))
                )}
              </div>
            )}

            {/* TAB 2: Micro-Leaks List */}
            {activeTab === "microleaks" && (
              <div className="space-y-2.5">
                {data.micro_spending_leaks.length === 0 ? (
                  <div className="p-8 text-center rounded-2xl bg-muted/20 border border-dashed border-border/60">
                    <CheckCircle2 className="w-8 h-8 text-emerald-500 mx-auto mb-2 opacity-80" />
                    <p className="text-xs font-semibold text-foreground">
                      No micro-spending leaks detected!
                    </p>
                    <p className="text-[11px] text-muted-foreground mt-0.5">
                      You keep frequent small-ticket transactions under control.
                    </p>
                  </div>
                ) : (
                  data.micro_spending_leaks.map((leak, idx) => (
                    <div
                      key={idx}
                      className="p-3.5 rounded-2xl bg-muted/30 border border-border/60 space-y-2 hover:bg-muted/50 transition-colors"
                    >
                      <div className="flex items-center justify-between">
                        <div className="flex items-center gap-2">
                          <Coffee className="w-4 h-4 text-amber-500" />
                          <span className="text-xs font-bold text-foreground">
                            {leak.category_or_label}
                          </span>
                          <span className="text-[10px] px-2 py-0.5 rounded-full bg-muted font-medium text-muted-foreground">
                            {leak.transaction_count} items (≤₹150)
                          </span>
                        </div>
                        <div className="text-right font-mono">
                          <span className="text-xs font-extrabold text-rose-500 block">
                            ₹{Number(leak.monthly_total).toLocaleString("en-IN")}/mo
                          </span>
                        </div>
                      </div>

                      {/* Example items and annualized drain */}
                      <div className="flex flex-wrap items-center justify-between gap-2 text-[11px] text-muted-foreground pt-1 border-t border-border/40">
                        <div className="flex items-center gap-1.5 flex-wrap">
                          <span className="text-[10px] font-semibold text-muted-foreground">Examples:</span>
                          {leak.example_items.map((item, i) => (
                            <span
                              key={i}
                              className="text-[10px] px-2 py-0.5 rounded-md bg-muted/60 text-foreground font-medium"
                            >
                              {item}
                            </span>
                          ))}
                        </div>
                        <span className="text-[10px] font-bold text-amber-600 dark:text-amber-400">
                          Annual Drain: ~₹{Number(leak.annual_projected_drain).toLocaleString("en-IN")}/yr
                        </span>
                      </div>
                    </div>
                  ))
                )}
              </div>
            )}

            {/* Actionable Tips */}
            {data.actionable_savings_tips && data.actionable_savings_tips.length > 0 && (
              <div className="p-4 rounded-2xl bg-muted/30 border border-border/60 space-y-2">
                <span className="text-[11px] font-bold text-muted-foreground uppercase tracking-wider block">
                  Actionable Leak Reduction Steps:
                </span>
                {data.actionable_savings_tips.map((tip, i) => (
                  <div key={i} className="flex items-start gap-2 text-xs text-foreground/90">
                    <ShieldCheck className="w-3.5 h-3.5 text-amber-500 mt-0.5 shrink-0" />
                    <span>{tip}</span>
                  </div>
                ))}
              </div>
            )}

            {/* Footer Action */}
            <div className="flex items-center justify-between pt-2">
              <button
                type="button"
                onClick={fetchLeaks}
                className="inline-flex items-center gap-1.5 text-xs text-muted-foreground hover:text-foreground font-medium transition-colors"
              >
                <RefreshCw className="w-3.5 h-3.5" /> Re-scan History
              </button>
              <button
                type="button"
                onClick={onClose}
                className="py-2.5 px-6 rounded-xl bg-primary text-white text-xs font-semibold hover:bg-primary/90 transition-all shadow-sm"
              >
                Close Audit
              </button>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
