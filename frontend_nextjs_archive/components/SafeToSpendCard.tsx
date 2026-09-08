"use client";

import React, { useState, useEffect } from "react";
import { aiApi, SafeToSpendResponse, DashboardSummary } from "@/lib/api";
import { toast } from "sonner";
import {
  Gauge,
  Sparkles,
  TrendingDown,
  TrendingUp,
  AlertTriangle,
  CheckCircle2,
  AlertOctagon,
  RefreshCw,
  Calendar,
  ShieldCheck,
  ChevronDown,
  ChevronUp,
} from "lucide-react";

interface SafeToSpendCardProps {
  summary?: DashboardSummary | null;
}

function computeFallbackSafeToSpend(summary?: DashboardSummary | null): SafeToSpendResponse {
  const now = new Date();
  const year = now.getFullYear();
  const month = now.getMonth();
  const today = now.getDate();
  const totalDaysInMonth = new Date(year, month + 1, 0).getDate();
  const daysPassed = Math.max(1, today);
  const daysRemaining = Math.max(1, totalDaysInMonth - today + 1);

  const totalSpent = summary ? parseFloat(summary.total_spent || "0") : 0;
  const totalIncome = summary ? parseFloat(summary.total_income || "0") : 0;
  const totalBudget = summary?.total_budget ? parseFloat(summary.total_budget) : null;

  const effectiveLimit = totalBudget && totalBudget > 0 ? totalBudget : (totalIncome > 0 ? totalIncome : 30000);
  const remainingBuffer = Math.max(0, effectiveLimit - totalSpent);
  const dailySafeSpend = (remainingBuffer / daysRemaining).toFixed(2);
  const currentBurnRate = (totalSpent / daysPassed).toFixed(2);

  const projectedAdditional = parseFloat(currentBurnRate) * Math.max(0, daysRemaining - 1);
  const projectedTotalSpend = totalSpent + projectedAdditional;
  const projectedMonthEndBalance = (totalIncome - projectedTotalSpend).toFixed(2);

  const burnPacePct = parseFloat(dailySafeSpend) > 0 
    ? Math.round((parseFloat(currentBurnRate) / parseFloat(dailySafeSpend)) * 100)
    : 250;

  const burnRateStatus: "optimal" | "warning" | "danger" = 
    remainingBuffer <= 0 || burnPacePct > 105 ? "danger" : burnPacePct > 85 ? "warning" : "optimal";

  let zeroCashDay: number | null = null;
  if (parseFloat(currentBurnRate) > 0 && projectedTotalSpend > effectiveLimit && remainingBuffer > 0) {
    const daysToDeplete = Math.floor(remainingBuffer / parseFloat(currentBurnRate));
    zeroCashDay = Math.min(totalDaysInMonth, daysPassed + daysToDeplete);
  } else if (remainingBuffer <= 0) {
    zeroCashDay = daysPassed;
  }

  let aiRec = "";
  let tips: string[] = [];
  if (burnRateStatus === "optimal") {
    aiRec = `Your spending velocity is optimal! You are currently burning ~₹${parseFloat(currentBurnRate).toLocaleString("en-IN")}/day against a safe allowance of ₹${parseFloat(dailySafeSpend).toLocaleString("en-IN")}/day.`;
    tips = [
      `You can safely spend up to ₹${parseFloat(dailySafeSpend).toLocaleString("en-IN")} today without impacting monthly targets.`,
      "Consider routing surplus cash into high-yield savings or emergency reserves.",
      "Maintain this steady pace through the weekend.",
    ];
  } else if (burnRateStatus === "warning") {
    aiRec = `Your spending pace is elevated at ₹${parseFloat(currentBurnRate).toLocaleString("en-IN")}/day (${burnPacePct}% of safe limit). You have ₹${remainingBuffer.toLocaleString("en-IN")} remaining for the last ${daysRemaining} days.`;
    tips = [
      `Cap daily discretionary purchases at ₹${parseFloat(dailySafeSpend).toLocaleString("en-IN")}/day for the rest of the month.`,
      "Postpone non-essential shopping until the next income cycle.",
      "Review recent transactions to identify potential savings opportunities.",
    ];
  } else {
    aiRec = `High burn rate alert! At your current burn rate, you risk exhausting your monthly buffer ${zeroCashDay ? `by Day ${zeroCashDay}` : "soon"}. Rebalancing is recommended.`;
    tips = [
      `Limit daily spending strictly to ₹${parseFloat(dailySafeSpend).toLocaleString("en-IN")}/day.`,
      "Enact a 48-hour pause on discretionary dining and shopping.",
      "Audit your top expense categories to identify immediate trimming opportunities.",
    ];
  }

  return {
    daily_safe_spend: dailySafeSpend,
    burn_rate_status: burnRateStatus,
    current_burn_rate_per_day: currentBurnRate,
    days_remaining_in_month: daysRemaining,
    days_passed: daysPassed,
    total_monthly_income: totalIncome.toFixed(2),
    total_spent_so_far: totalSpent.toFixed(2),
    remaining_buffer: remainingBuffer.toFixed(2),
    projected_month_end_balance: projectedMonthEndBalance,
    projected_zero_cash_day: zeroCashDay,
    burn_pace_percentage: burnPacePct,
    ai_recommendation: aiRec,
    actionable_tips: tips,
    provider_used: "client-resilient-engine",
  };
}

export function SafeToSpendCard({ summary }: SafeToSpendCardProps) {
  const [data, setData] = useState<SafeToSpendResponse | null>(() => computeFallbackSafeToSpend(summary));
  const [loading, setLoading] = useState(false);
  const [showTips, setShowTips] = useState(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  const fetchForecast = async (showToast = false) => {
    try {
      setLoading(true);
      setErrorMessage(null);
      const res = await aiApi.getSafeToSpend();
      setData(res);
      if (showToast) {
        toast.success("Safe-to-Spend real-time gauge updated!");
      }
    } catch (err: any) {
      const msg = err.message || "Unable to reach server";
      setErrorMessage(msg);
      // Fallback seamlessly to local computation without failing or showing a broken UI
      setData((prev) => prev || computeFallbackSafeToSpend(summary));
      if (showToast) {
        toast.info("Using live dashboard metrics for Safe-to-Spend gauge");
      }
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchForecast();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [summary?.total_spent, summary?.total_income, summary?.total_budget]);

  if (loading && !data) {
    return (
      <div className="glass-card p-5 sm:p-6 rounded-3xl border border-border/80 bg-card/80 animate-pulse flex items-center justify-between">
        <div className="space-y-2">
          <div className="h-4 w-36 bg-muted/60 rounded-md" />
          <div className="h-8 w-48 bg-muted/80 rounded-lg" />
        </div>
        <div className="h-10 w-28 bg-muted/60 rounded-xl" />
      </div>
    );
  }

  if (!data) {
    return (
      <div className="p-4 sm:p-5 rounded-3xl border border-border/60 bg-muted/20 flex flex-col sm:flex-row items-start sm:items-center justify-between gap-3">
        <div className="flex items-center gap-3">
          <div className="w-10 h-10 rounded-2xl bg-muted/60 flex items-center justify-center text-muted-foreground shrink-0">
            <Gauge className="w-5 h-5" />
          </div>
          <div>
            <p className="text-xs font-bold text-foreground">
              Safe-to-Spend Real-Time Gauge
            </p>
            <p className="text-[11px] text-muted-foreground mt-0.5">
              {errorMessage ? `Status: ${errorMessage}` : "Click to calculate your live daily burn limit."}
            </p>
          </div>
        </div>
        <button
          onClick={() => fetchForecast(true)}
          disabled={loading}
          className="self-end sm:self-center px-4 py-2 rounded-xl bg-primary text-white text-xs font-semibold hover:bg-primary/90 transition-all flex items-center gap-2 shadow-sm disabled:opacity-50"
        >
          <RefreshCw className={`w-3.5 h-3.5 ${loading ? "animate-spin" : ""}`} />
          <span>{loading ? "Calculating..." : "Load Live Gauge"}</span>
        </button>
      </div>
    );
  }

  const statusColor =
    data.burn_rate_status === "optimal"
      ? "text-emerald-500 bg-emerald-500/10 border-emerald-500/30"
      : data.burn_rate_status === "warning"
      ? "text-amber-500 bg-amber-500/10 border-amber-500/30"
      : "text-rose-500 bg-rose-500/10 border-rose-500/30";

  const statusGlow =
    data.burn_rate_status === "optimal"
      ? "from-emerald-500/15 to-teal-500/5 border-emerald-500/30"
      : data.burn_rate_status === "warning"
      ? "from-amber-500/15 to-orange-500/5 border-amber-500/30"
      : "from-rose-500/15 to-red-500/5 border-rose-500/30";

  return (
    <div
      className={`relative overflow-hidden rounded-3xl border bg-gradient-to-br ${statusGlow} p-5 sm:p-6 shadow-lg backdrop-blur-md transition-all duration-300`}
    >
      {/* Header Row */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3">
        <div className="flex items-center gap-3">
          <div
            className={`w-11 h-11 rounded-2xl flex items-center justify-center font-bold shadow-md ${
              data.burn_rate_status === "optimal"
                ? "bg-emerald-500 text-white shadow-emerald-500/20"
                : data.burn_rate_status === "warning"
                ? "bg-amber-500 text-slate-950 shadow-amber-500/20"
                : "bg-rose-500 text-white shadow-rose-500/20"
            }`}
          >
            <Gauge className="w-6 h-6" />
          </div>
          <div>
            <div className="flex items-center gap-2">
              <h3 className="font-extrabold text-sm sm:text-base text-foreground tracking-tight">
                Safe-to-Spend Real-Time Gauge
              </h3>
              <span
                className={`text-[10px] font-extrabold uppercase px-2 py-0.5 rounded-full border ${statusColor}`}
              >
                {data.burn_rate_status} pace
              </span>
            </div>
            <p className="text-xs text-muted-foreground mt-0.5">
              {data.days_remaining_in_month} days remaining in this billing cycle
            </p>
          </div>
        </div>

        {/* Refresh Button */}
        <button
          onClick={() => fetchForecast(true)}
          disabled={loading}
          className="self-end sm:self-center inline-flex items-center gap-1.5 px-3 py-1.5 rounded-xl bg-background/60 hover:bg-background border border-border/60 text-xs font-semibold text-muted-foreground hover:text-foreground transition-all shadow-sm disabled:opacity-50"
        >
          <RefreshCw className={`w-3.5 h-3.5 ${loading ? "animate-spin" : ""}`} />
          <span>Refresh</span>
        </button>
      </div>

      {/* Main Metric Spotlight */}
      <div className="mt-5 grid grid-cols-1 sm:grid-cols-3 gap-3.5">
        {/* Card 1: Today's Safe Daily Allowance */}
        <div className="p-4 rounded-2xl bg-card/80 border border-border/80 shadow-sm flex flex-col justify-between">
          <span className="text-xs font-semibold text-muted-foreground flex items-center gap-1">
            <Sparkles className="w-3.5 h-3.5 text-indigo-500" /> Today&apos;s Safe Burn Limit
          </span>
          <div className="my-2">
            <span className="text-2xl sm:text-3xl font-black font-mono text-foreground">
              ₹{Number(data.daily_safe_spend).toLocaleString("en-IN", { minimumFractionDigits: 2 })}
            </span>
            <span className="text-xs text-muted-foreground font-medium block mt-0.5">
              per day for remaining {data.days_remaining_in_month} days
            </span>
          </div>
        </div>

        {/* Card 2: Current Burn Rate Velocity */}
        <div className="p-4 rounded-2xl bg-card/80 border border-border/80 shadow-sm flex flex-col justify-between">
          <span className="text-xs font-semibold text-muted-foreground">
            Current Burn Velocity
          </span>
          <div className="my-2">
            <span className="text-2xl sm:text-3xl font-black font-mono text-foreground">
              ₹{Number(data.current_burn_rate_per_day).toLocaleString("en-IN", { minimumFractionDigits: 2 })}
              <span className="text-xs font-normal text-muted-foreground">/d</span>
            </span>
            <div className="flex items-center gap-1.5 mt-0.5">
              <span
                className={`text-xs font-bold ${
                  data.burn_pace_percentage <= 85
                    ? "text-emerald-500"
                    : data.burn_pace_percentage <= 105
                    ? "text-amber-500"
                    : "text-rose-500"
                }`}
              >
                {data.burn_pace_percentage}% of safe pace
              </span>
            </div>
          </div>
        </div>

        {/* Card 3: Projected Month-End Cash Flow */}
        <div className="p-4 rounded-2xl bg-card/80 border border-border/80 shadow-sm flex flex-col justify-between">
          <span className="text-xs font-semibold text-muted-foreground">
            Projected Month-End Savings
          </span>
          <div className="my-2">
            <span
              className={`text-2xl sm:text-3xl font-black font-mono ${
                Number(data.projected_month_end_balance) >= 0
                  ? "text-foreground"
                  : "text-rose-500"
              }`}
            >
              ₹{Number(data.projected_month_end_balance).toLocaleString("en-IN", { minimumFractionDigits: 2 })}
            </span>
            <span className="text-xs text-muted-foreground font-medium block mt-0.5">
              {Number(data.projected_month_end_balance) >= 0 ? "Surplus forecast" : "Deficit alert"}
            </span>
          </div>
        </div>
      </div>

      {/* Zero Cash Depletion Alert Banner (if danger) */}
      {data.projected_zero_cash_day && data.burn_rate_status === "danger" && (
        <div className="mt-3.5 p-3.5 rounded-2xl bg-rose-500/15 border border-rose-500/30 flex items-center gap-3 text-rose-800 dark:text-rose-200">
          <AlertOctagon className="w-5 h-5 text-rose-500 shrink-0 animate-pulse" />
          <p className="text-xs font-semibold leading-relaxed">
            <strong>Buffer Depletion Warning:</strong> At your current burn of ₹{Number(data.current_burn_rate_per_day).toLocaleString("en-IN")}/day, you risk exhausting your monthly budget by <strong>Day {data.projected_zero_cash_day}</strong>.
          </p>
        </div>
      )}

      {/* AI Narrative & Toggle Advice */}
      <div className="mt-3.5 pt-3 border-t border-border/60">
        <div className="flex items-center justify-between">
          <p className="text-xs text-foreground/90 leading-relaxed flex-1 mr-2">
            <span className="font-bold text-primary mr-1">AI Advisor:</span>
            {data.ai_recommendation}
          </p>
          <button
            type="button"
            onClick={() => setShowTips(!showTips)}
            className="inline-flex items-center gap-1 text-xs font-semibold text-primary hover:underline shrink-0"
          >
            {showTips ? (
              <>
                Hide Tips <ChevronUp className="w-3.5 h-3.5" />
              </>
            ) : (
              <>
                Action Tips <ChevronDown className="w-3.5 h-3.5" />
              </>
            )}
          </button>
        </div>

        {/* Collapsible Actionable Tips */}
        {showTips && data.actionable_tips && data.actionable_tips.length > 0 && (
          <div className="mt-3 p-3.5 rounded-2xl bg-card/90 border border-border/80 space-y-1.5 animate-in fade-in duration-200">
            {data.actionable_tips.map((tip, idx) => (
              <div key={idx} className="flex items-start gap-2 text-xs text-foreground/90">
                <ShieldCheck className="w-3.5 h-3.5 text-primary mt-0.5 shrink-0" />
                <span>{tip}</span>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}
