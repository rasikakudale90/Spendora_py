"use client";

import React, { useState, useEffect } from "react";
import {
  ShieldCheck,
  Zap,
  TrendingUp,
  AlertTriangle,
  ArrowUpRight,
  Sparkles,
  RefreshCw,
  ChevronDown,
  ChevronUp,
} from "lucide-react";
import {
  RadarChart,
  PolarGrid,
  PolarAngleAxis,
  PolarRadiusAxis,
  Radar,
  ResponsiveContainer,
  Tooltip,
} from "recharts";
import { aiApi, FinancialHealthResponse } from "@/lib/api";
import { formatCurrency } from "@/lib/utils";

interface FinancialHealthCardProps {
  initialData?: FinancialHealthResponse | null;
  monthlyIncome?: number | string;
  monthlySpent?: number | string;
}

export const FinancialHealthCard: React.FC<FinancialHealthCardProps> = ({
  initialData,
  monthlyIncome = 0,
  monthlySpent = 0,
}) => {
  const [data, setData] = useState<FinancialHealthResponse | null>(initialData || null);
  const [loading, setLoading] = useState(!initialData);
  const [showAllBoosters, setShowAllBoosters] = useState(false);

  const fetchHealthScore = async () => {
    setLoading(true);
    try {
      const res = await aiApi.getFinancialHealthScore();
      setData(res);
    } catch (err) {
      console.warn("Falling back to client-side health calculation:", err);
      // Resilient client-side fallback calculation
      const income = Number(monthlyIncome) || 0;
      const spent = Number(monthlySpent) || 0;
      const netSavings = income - spent;
      const savingsRate = income > 0 ? (netSavings / income) * 100 : 0;

      let score = 70;
      if (savingsRate >= 20) score = 85;
      else if (savingsRate >= 10) score = 75;
      else if (savingsRate < 0) score = 48;

      setData({
        composite_score: score,
        tier: score >= 90 ? "elite" : score >= 75 ? "healthy" : score >= 60 ? "vulnerable" : "critical",
        tier_title: score >= 90 ? "Elite Wealth Builder" : score >= 75 ? "Financially Stable & Resilient" : score >= 60 ? "High Burn / Vulnerable" : "Cash Flow Deficit Alert",
        summary: `Your estimated financial health score is ${score}/100 based on your current ₹${spent.toLocaleString()}/mo burn rate.`,
        pillars: [
          { name: "Savings", score: Math.min(100, Math.max(20, Math.round(savingsRate * 4))), weight_pct: 25, benchmark_label: "≥ 20% savings", status: savingsRate >= 20 ? "optimal" : "fair", insight: `Current savings rate is ${savingsRate.toFixed(1)}%.` },
          { name: "Budgets", score: 75, weight_pct: 25, benchmark_label: "100% respected", status: "good", insight: "Category budgets are monitored." },
          { name: "Burn Rate", score: spent > income ? 45 : 85, weight_pct: 20, benchmark_label: "Safe burn velocity", status: spent > income ? "critical" : "optimal", insight: "Pacing relative to available funds." },
          { name: "Cash Cushion", score: netSavings > 0 ? 80 : 35, weight_pct: 15, benchmark_label: "Positive surplus", status: netSavings > 0 ? "good" : "critical", insight: "Surplus cash cushion." },
          { name: "Leak Control", score: 85, weight_pct: 15, benchmark_label: "≤ 5% leak ratio", status: "optimal", insight: "Low subscription overhead." },
        ],
        score_boosters: [
          { pillar: "Savings Discipline", impact_points: 8, action_text: "Target saving 20% of incoming income before discretionary spending.", category_hint: "Savings" },
          { pillar: "Burn Stability", impact_points: 6, action_text: "Maintain a steady daily burn pace under your monthly target.", category_hint: "Expenses" },
        ],
        monthly_income: income,
        monthly_spent: spent,
        monthly_net_savings: netSavings,
        savings_rate_pct: Math.round(savingsRate * 10) / 10,
        provider_used: "client-fallback-engine",
      });
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (!initialData) {
      fetchHealthScore();
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [initialData]);

  const getTierBadgeStyle = (tier: string) => {
    switch (tier) {
      case "elite":
        return "bg-emerald-500/10 text-emerald-400 border-emerald-500/30";
      case "healthy":
        return "bg-cyan-500/10 text-cyan-400 border-cyan-500/30";
      case "vulnerable":
        return "bg-amber-500/10 text-amber-400 border-amber-500/30";
      default:
        return "bg-rose-500/10 text-rose-400 border-rose-500/30";
    }
  };

  const getScoreColor = (score: number) => {
    if (score >= 90) return "text-emerald-400";
    if (score >= 75) return "text-teal-400";
    if (score >= 60) return "text-amber-400";
    return "text-rose-400";
  };

  const radarData = data?.pillars.map((p) => ({
    pillar: p.name.split(" ")[0], // Short name for clean radar rendering
    score: p.score,
    fullMark: 100,
  })) || [];

  return (
    <div className="relative overflow-hidden rounded-2xl border border-border/80 bg-card/60 backdrop-blur-xl p-5 sm:p-6 shadow-xl transition-all duration-300 hover:border-border">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3 pb-4 border-b border-border/50">
        <div className="flex items-center gap-3">
          <div className="w-10 h-10 rounded-xl bg-gradient-to-tr from-emerald-500/20 via-teal-500/10 to-cyan-500/20 border border-emerald-500/30 flex items-center justify-center shadow-inner">
            <ShieldCheck className="w-5 h-5 text-emerald-400" />
          </div>
          <div>
            <div className="flex items-center gap-2">
              <h3 className="text-base sm:text-lg font-bold text-foreground">
                Financial Health Score
              </h3>
              <span className="inline-flex items-center gap-1 text-[10px] font-semibold uppercase tracking-wider px-2 py-0.5 rounded-full bg-primary/10 text-primary border border-primary/20">
                <Sparkles className="w-3 h-3" /> AI Telemetry
              </span>
            </div>
            <p className="text-xs text-muted-foreground">
              Composite 5-pillar health audit & personalized score boosters
            </p>
          </div>
        </div>

        <button
          onClick={fetchHealthScore}
          disabled={loading}
          className="self-start sm:self-auto inline-flex items-center gap-1.5 text-xs text-muted-foreground hover:text-foreground transition-colors px-2.5 py-1.5 rounded-lg border border-border/60 hover:bg-muted/50"
        >
          <RefreshCw className={`w-3.5 h-3.5 ${loading ? "animate-spin text-primary" : ""}`} />
          <span>Refresh</span>
        </button>
      </div>

      {loading && !data ? (
        <div className="py-12 flex flex-col items-center justify-center gap-3 text-center">
          <RefreshCw className="w-8 h-8 text-primary animate-spin" />
          <p className="text-sm text-muted-foreground">Analyzing cash flow, budgets & spending habits...</p>
        </div>
      ) : data ? (
        <div className="mt-5 space-y-6">
          {/* Main Grid: Score Radial & Radar Chart */}
          <div className="grid grid-cols-1 lg:grid-cols-12 gap-6 items-center">
            {/* Left: Composite Score Circle & Summary (5 cols) */}
            <div className="lg:col-span-5 flex flex-col items-center sm:items-start text-center sm:text-left space-y-4">
              <div className="flex items-center gap-4">
                {/* Score Ring */}
                <div className="relative flex items-center justify-center w-24 h-24 sm:w-28 sm:h-28 rounded-full border-4 border-muted/40 shadow-inner bg-gradient-to-b from-card to-muted/20">
                  <div className="flex flex-col items-center justify-center">
                    <span className={`text-3xl sm:text-4xl font-black tracking-tight ${getScoreColor(data.composite_score)}`}>
                      {data.composite_score}
                    </span>
                    <span className="text-[10px] uppercase font-bold text-muted-foreground tracking-wider">
                      / 100
                    </span>
                  </div>
                </div>

                <div className="space-y-1.5">
                  <span className={`inline-block text-xs font-bold uppercase tracking-wider px-2.5 py-1 rounded-md border ${getTierBadgeStyle(data.tier)}`}>
                    {data.tier_title}
                  </span>
                  <div className="text-xs text-muted-foreground space-y-0.5">
                    <div>
                      Net Savings: <span className="font-semibold text-foreground">{formatCurrency(Number(data.monthly_net_savings))}</span>
                    </div>
                    <div>
                      Savings Rate: <span className={`font-semibold ${data.savings_rate_pct >= 20 ? "text-emerald-400" : "text-amber-400"}`}>{data.savings_rate_pct}%</span>
                    </div>
                  </div>
                </div>
              </div>

              <p className="text-xs sm:text-sm text-muted-foreground/90 leading-relaxed bg-muted/30 p-3 rounded-xl border border-border/40">
                &ldquo;{data.summary}&rdquo;
              </p>
            </div>

            {/* Right: 5-Pillar Spider Radar Chart (7 cols) */}
            <div className="lg:col-span-7 h-56 sm:h-64 w-full">
              <ResponsiveContainer width="100%" height="100%">
                <RadarChart cx="50%" cy="50%" outerRadius="75%" data={radarData}>
                  <PolarGrid stroke="currentColor" className="text-border/60" strokeDasharray="3 3" />
                  <PolarAngleAxis
                    dataKey="pillar"
                    tick={{ fill: "currentColor", fontSize: 11, fontWeight: 600 }}
                    className="text-muted-foreground"
                  />
                  <PolarRadiusAxis
                    angle={30}
                    domain={[0, 100]}
                    tick={{ fill: "currentColor", fontSize: 9 }}
                    className="text-muted-foreground/50"
                  />
                  <Radar
                    name="Score"
                    dataKey="score"
                    stroke="#10b981"
                    fill="#10b981"
                    fillOpacity={0.35}
                  />
                  <Tooltip
                    contentStyle={{
                      backgroundColor: "#0f172a",
                      border: "1px solid #334155",
                      borderRadius: "8px",
                      fontSize: "12px",
                    }}
                    itemStyle={{ color: "#38bdf8" }}
                  />
                </RadarChart>
              </ResponsiveContainer>
            </div>
          </div>

          {/* Pillars List (Horizontal Grid) */}
          <div className="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-5 gap-2.5">
            {data.pillars.map((pillar, idx) => (
              <div
                key={idx}
                className="p-2.5 rounded-xl border border-border/50 bg-muted/20 flex flex-col justify-between space-y-1.5"
              >
                <div className="flex items-center justify-between">
                  <span className="text-xs font-medium text-foreground truncate" title={pillar.name}>
                    {pillar.name}
                  </span>
                  <span className={`text-xs font-bold ${getScoreColor(pillar.score)}`}>
                    {pillar.score}
                  </span>
                </div>
                <div className="w-full bg-muted/60 rounded-full h-1.5 overflow-hidden">
                  <div
                    className={`h-1.5 rounded-full transition-all duration-500 ${
                      pillar.score >= 80 ? "bg-emerald-500" : pillar.score >= 60 ? "bg-amber-500" : "bg-rose-500"
                    }`}
                    style={{ width: `${pillar.score}%` }}
                  />
                </div>
                <span className="text-[10px] text-muted-foreground truncate" title={pillar.benchmark_label}>
                  {pillar.benchmark_label}
                </span>
              </div>
            ))}
          </div>

          {/* Score Booster Actions */}
          {data.score_boosters && data.score_boosters.length > 0 && (
            <div className="pt-2 border-t border-border/50">
              <div className="flex items-center justify-between mb-2.5">
                <div className="flex items-center gap-1.5 text-xs font-bold text-foreground">
                  <Zap className="w-3.5 h-3.5 text-amber-400" />
                  <span>AI Score Booster Actions</span>
                </div>
                {data.score_boosters.length > 2 && (
                  <button
                    onClick={() => setShowAllBoosters(!showAllBoosters)}
                    className="text-[11px] text-primary hover:underline flex items-center gap-0.5"
                  >
                    <span>{showAllBoosters ? "Show Less" : "View All"}</span>
                    {showAllBoosters ? <ChevronUp className="w-3 h-3" /> : <ChevronDown className="w-3 h-3" />}
                  </button>
                )}
              </div>

              <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-2.5">
                {(showAllBoosters ? data.score_boosters : data.score_boosters.slice(0, 3)).map((booster, idx) => (
                  <div
                    key={idx}
                    className="flex items-start gap-2.5 p-3 rounded-xl border border-amber-500/20 bg-amber-500/5 hover:border-amber-500/40 transition-colors"
                  >
                    <span className="shrink-0 px-2 py-0.5 text-[10px] font-black uppercase tracking-wider rounded-md bg-amber-500/20 text-amber-400 border border-amber-500/30">
                      +{booster.impact_points} PTS
                    </span>
                    <div className="space-y-0.5">
                      <span className="text-[10px] font-bold text-muted-foreground uppercase tracking-wider">
                        {booster.pillar}
                      </span>
                      <p className="text-xs text-foreground/90 leading-tight">
                        {booster.action_text}
                      </p>
                    </div>
                  </div>
                ))}
              </div>
            </div>
          )}
        </div>
      ) : null}
    </div>
  );
};
