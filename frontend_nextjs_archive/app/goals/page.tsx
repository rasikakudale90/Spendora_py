"use client";

import React, { useState, useEffect } from "react";
import { useRouter } from "next/navigation";
import {
  Target,
  Plus,
  TrendingUp,
  Calendar,
  Sparkles,
  Zap,
  CheckCircle2,
  AlertCircle,
  MoreVertical,
  Edit2,
  Trash2,
  ArrowDownRight,
  ShieldAlert,
} from "lucide-react";
import { toast } from "sonner";
import { goalsApi, aiApi, Goal, GoalListResponse, GoalRunwayAnalysisResponse } from "@/lib/api";
import { formatCurrency } from "@/lib/utils";
import { useAuth } from "@/context/AuthContext";
import { GoalFormModal } from "@/components/goals/GoalFormModal";
import { GoalContributeModal } from "@/components/goals/GoalContributeModal";

export default function GoalsPage() {
  const router = useRouter();
  const { user, isLoading: authLoading } = useAuth();

  const [goalsData, setGoalsData] = useState<GoalListResponse | null>(null);
  const [runwayData, setRunwayData] = useState<GoalRunwayAnalysisResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const [statusFilter, setStatusFilter] = useState<string>("all");

  // Modals state
  const [formModalOpen, setFormModalOpen] = useState(false);
  const [goalToEdit, setGoalToEdit] = useState<Goal | null>(null);
  const [contributeModalOpen, setContributeModalOpen] = useState(false);
  const [goalToContribute, setGoalToContribute] = useState<Goal | null>(null);

  // Authentication check
  useEffect(() => {
    if (!authLoading && !user) {
      router.push("/login");
    }
  }, [user, authLoading, router]);

  const loadData = async () => {
    setLoading(true);
    try {
      const [gRes, rRes] = await Promise.all([
        goalsApi.getAll(statusFilter === "all" ? undefined : statusFilter),
        aiApi.getGoalsRunwayForecast().catch(() => null),
      ]);
      setGoalsData(gRes);
      setRunwayData(rRes);
    } catch (err: any) {
      toast.error(err.message || "Failed to load goals.");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (user) {
      loadData();
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [user, statusFilter]);

  const handleDeleteGoal = async (id: string, name: string) => {
    if (!confirm(`Are you sure you want to delete '${name}'? This cannot be undone.`)) return;
    try {
      await goalsApi.delete(id);
      toast.success(`Goal '${name}' deleted.`);
      loadData();
    } catch (err: any) {
      toast.error(err.message || "Failed to delete goal.");
    }
  };

  const getPacingBadge = (status?: string) => {
    switch (status) {
      case "ahead":
      case "on_track":
        return {
          bg: "bg-emerald-500/10 text-emerald-400 border-emerald-500/30",
          text: status === "ahead" ? "Ahead of Pace" : "On Track",
        };
      case "at_risk":
        return {
          bg: "bg-amber-500/10 text-amber-400 border-amber-500/30",
          text: "Pacing At Risk",
        };
      case "behind":
        return {
          bg: "bg-rose-500/10 text-rose-400 border-rose-500/30",
          text: "Behind Target",
        };
      case "completed":
        return {
          bg: "bg-teal-500/10 text-teal-400 border-teal-500/30",
          text: "Completed",
        };
      default:
        return {
          bg: "bg-muted text-muted-foreground border-border/60",
          text: "Ongoing Pace",
        };
    }
  };

  const getColorAccent = (color?: string) => {
    switch (color) {
      case "teal":
        return "border-teal-500/40 from-teal-500/10";
      case "cyan":
        return "border-cyan-500/40 from-cyan-500/10";
      case "violet":
        return "border-violet-500/40 from-violet-500/10";
      case "rose":
        return "border-rose-500/40 from-rose-500/10";
      case "amber":
        return "border-amber-500/40 from-amber-500/10";
      default:
        return "border-emerald-500/40 from-emerald-500/10";
    }
  };

  if (authLoading || !user) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-primary" />
      </div>
    );
  }

  const items = goalsData?.items || [];

  return (
    <div className="min-h-screen pb-16 pt-6 sm:pt-8 px-4 sm:px-6 lg:px-8 max-w-7xl mx-auto space-y-6">
      {/* Top Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <div className="flex items-center gap-2.5">
            <div className="w-10 h-10 rounded-xl bg-gradient-to-tr from-emerald-500 to-teal-400 flex items-center justify-center shadow-lg shadow-emerald-500/20">
              <Target className="w-5 h-5 text-slate-950 font-bold" />
            </div>
            <div>
              <h1 className="text-xl sm:text-2xl font-black text-foreground tracking-tight">
                Financial Goals & Savings Runway
              </h1>
              <p className="text-xs text-muted-foreground">
                Set target savings, track progress, and unlock AI runway acceleration
              </p>
            </div>
          </div>
        </div>

        <div className="flex items-center gap-3">
          <button
            onClick={() => {
              setGoalToEdit(null);
              setFormModalOpen(true);
            }}
            className="inline-flex items-center gap-2 px-4 py-2.5 rounded-xl bg-primary text-primary-foreground font-bold text-xs sm:text-sm shadow-lg shadow-primary/25 hover:opacity-90 transition-opacity"
          >
            <Plus className="w-4 h-4" />
            <span>New Goal</span>
          </button>
        </div>
      </div>

      {/* KPI Overview Cards */}
      {goalsData && (
        <div className="grid grid-cols-2 lg:grid-cols-4 gap-3 sm:gap-4">
          <div className="glass-card rounded-2xl p-4 border border-border/80">
            <span className="text-[11px] font-semibold text-muted-foreground uppercase tracking-wider">
              Total Target
            </span>
            <div className="text-lg sm:text-2xl font-black text-foreground mt-1">
              {formatCurrency(Number(goalsData.total_target_amount))}
            </div>
            <span className="text-[10px] text-muted-foreground">Across {goalsData.total_count} goals</span>
          </div>

          <div className="glass-card rounded-2xl p-4 border border-border/80">
            <span className="text-[11px] font-semibold text-muted-foreground uppercase tracking-wider">
              Total Saved
            </span>
            <div className="text-lg sm:text-2xl font-black text-emerald-400 mt-1">
              {formatCurrency(Number(goalsData.total_saved_amount))}
            </div>
            <span className="text-[10px] text-emerald-500/90 font-semibold">
              {goalsData.overall_progress_percentage}% achieved
            </span>
          </div>

          <div className="glass-card rounded-2xl p-4 border border-border/80">
            <span className="text-[11px] font-semibold text-muted-foreground uppercase tracking-wider">
              Remaining Gap
            </span>
            <div className="text-lg sm:text-2xl font-black text-foreground mt-1">
              {formatCurrency(Number(goalsData.total_remaining_amount))}
            </div>
            <span className="text-[10px] text-muted-foreground">Needed to reach 100%</span>
          </div>

          <div className="glass-card rounded-2xl p-4 border border-border/80">
            <span className="text-[11px] font-semibold text-muted-foreground uppercase tracking-wider">
              Monthly Cash Surplus
            </span>
            <div className="text-lg sm:text-2xl font-black text-cyan-400 mt-1">
              {runwayData ? formatCurrency(Number(runwayData.monthly_surplus)) : "₹0"}
            </div>
            <span className="text-[10px] text-muted-foreground">Available funding fuel</span>
          </div>
        </div>
      )}

      {/* AI Multi-Goal Runway Intelligence Banner */}
      {runwayData && (
        <div className="rounded-2xl border border-primary/30 bg-primary/5 p-4 sm:p-5 shadow-sm space-y-2.5">
          <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-2">
            <div className="flex items-center gap-2">
              <Sparkles className="w-4 h-4 text-primary" />
              <h3 className="text-sm font-bold text-foreground">
                AI Savings Runway Intelligence
              </h3>
            </div>
            <span
              className={`inline-flex items-center gap-1 text-[11px] font-bold px-2.5 py-0.5 rounded-full border ${
                runwayData.is_fully_funded
                  ? "bg-emerald-500/10 text-emerald-400 border-emerald-500/30"
                  : "bg-amber-500/10 text-amber-400 border-amber-500/30"
              }`}
            >
              {runwayData.is_fully_funded ? "Fully Funded" : `${runwayData.surplus_coverage_pct}% Funded`}
            </span>
          </div>

          <p className="text-xs text-foreground/90 leading-relaxed">
            {runwayData.ai_runway_summary}
          </p>

          {runwayData.discretionary_reduction_tip && (
            <div className="flex items-start gap-2 pt-2 border-t border-border/40 text-xs text-muted-foreground">
              <Zap className="w-3.5 h-3.5 text-amber-400 shrink-0 mt-0.5" />
              <span>
                <strong className="text-foreground">AI Acceleration Tip:</strong>{" "}
                {runwayData.discretionary_reduction_tip}
              </span>
            </div>
          )}
        </div>
      )}

      {/* Filter Tabs */}
      <div className="flex items-center gap-2 border-b border-border/60 pb-3">
        {["all", "active", "completed"].map((tab) => (
          <button
            key={tab}
            onClick={() => setStatusFilter(tab)}
            className={`px-3.5 py-1.5 text-xs font-bold rounded-lg uppercase tracking-wider transition-all ${
              statusFilter === tab
                ? "bg-primary/20 text-primary border border-primary/30"
                : "text-muted-foreground hover:text-foreground hover:bg-muted/40"
            }`}
          >
            {tab}
          </button>
        ))}
      </div>

      {/* Goals Grid */}
      {loading && !goalsData ? (
        <div className="py-16 text-center text-sm text-muted-foreground animate-pulse">
          Loading your financial goals...
        </div>
      ) : items.length === 0 ? (
        <div className="glass-card rounded-2xl p-12 text-center border border-border/80 space-y-4">
          <div className="w-12 h-12 rounded-2xl bg-muted/40 mx-auto flex items-center justify-center text-muted-foreground">
            <Target className="w-6 h-6" />
          </div>
          <div>
            <h3 className="text-base font-bold text-foreground">No Goals Found</h3>
            <p className="text-xs text-muted-foreground mt-1 max-w-sm mx-auto">
              Start setting your target milestones like emergency buffers, trips, or dream purchases.
            </p>
          </div>
          <button
            onClick={() => {
              setGoalToEdit(null);
              setFormModalOpen(true);
            }}
            className="inline-flex items-center gap-2 px-4 py-2 rounded-xl bg-primary text-primary-foreground font-bold text-xs hover:opacity-90 shadow-lg shadow-primary/20"
          >
            <Plus className="w-4 h-4" />
            <span>Create Your First Goal</span>
          </button>
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4 sm:gap-5">
          {items.map((goal) => {
            const pacing = getPacingBadge(goal.runway?.pacing_status);
            const colorStyle = getColorAccent(goal.color);

            return (
              <div
                key={goal.id}
                className={`relative overflow-hidden rounded-2xl border bg-gradient-to-b ${colorStyle} bg-card/70 backdrop-blur-xl p-5 shadow-lg transition-all hover:scale-[1.01] flex flex-col justify-between space-y-4`}
              >
                {/* Card Top */}
                <div>
                  <div className="flex items-start justify-between gap-2">
                    <div className="space-y-1">
                      <span className="text-[10px] font-bold uppercase tracking-wider text-muted-foreground px-2 py-0.5 rounded-md bg-muted/60">
                        {goal.category}
                      </span>
                      <h3 className="text-base font-bold text-foreground truncate" title={goal.name}>
                        {goal.name}
                      </h3>
                    </div>

                    {/* Action Menu */}
                    <div className="flex items-center gap-1">
                      <button
                        onClick={() => {
                          setGoalToEdit(goal);
                          setFormModalOpen(true);
                        }}
                        className="p-1.5 rounded-lg text-muted-foreground hover:text-foreground hover:bg-muted/60 transition-colors"
                        title="Edit Goal"
                      >
                        <Edit2 className="w-3.5 h-3.5" />
                      </button>
                      <button
                        onClick={() => handleDeleteGoal(goal.id, goal.name)}
                        className="p-1.5 rounded-lg text-muted-foreground hover:text-rose-400 hover:bg-rose-500/10 transition-colors"
                        title="Delete Goal"
                      >
                        <Trash2 className="w-3.5 h-3.5" />
                      </button>
                    </div>
                  </div>

                  {/* Amounts & Progress */}
                  <div className="mt-4 space-y-2">
                    <div className="flex items-baseline justify-between">
                      <span className="text-xl font-black text-foreground tracking-tight">
                        {formatCurrency(Number(goal.current_amount))}
                      </span>
                      <span className="text-xs text-muted-foreground">
                        of {formatCurrency(Number(goal.target_amount))}
                      </span>
                    </div>

                    <div className="w-full bg-muted/60 rounded-full h-2 overflow-hidden">
                      <div
                        className={`h-2 rounded-full transition-all duration-500 ${
                          goal.is_completed ? "bg-emerald-400" : "bg-primary"
                        }`}
                        style={{ width: `${goal.progress_percentage}%` }}
                      />
                    </div>

                    <div className="flex items-center justify-between text-[11px] text-muted-foreground">
                      <span className="font-bold text-foreground">
                        {goal.progress_percentage}%
                      </span>
                      <span>
                        {goal.is_completed
                          ? "Fully Funded!"
                          : `${formatCurrency(Number(goal.remaining_amount))} left`}
                      </span>
                    </div>
                  </div>
                </div>

                {/* AI Runway & Deadline Insights */}
                <div className="space-y-2.5 pt-3 border-t border-border/50">
                  <div className="flex items-center justify-between text-xs">
                    <span className={`px-2 py-0.5 rounded-md text-[10px] font-bold uppercase tracking-wider border ${pacing.bg}`}>
                      {pacing.text}
                    </span>

                    {goal.target_date ? (
                      <span className="flex items-center gap-1 text-[11px] text-muted-foreground">
                        <Calendar className="w-3 h-3" />
                        <span>{new Date(goal.target_date).toLocaleDateString(undefined, { month: "short", day: "numeric", year: "numeric" })}</span>
                      </span>
                    ) : (
                      <span className="text-[10px] text-muted-foreground">No deadline</span>
                    )}
                  </div>

                  {/* Runway message */}
                  {goal.runway && (
                    <p className="text-[11px] text-muted-foreground line-clamp-2 leading-relaxed bg-muted/20 p-2 rounded-lg">
                      {goal.runway.pacing_message}
                    </p>
                  )}

                  {/* Speed-up advice chip */}
                  {goal.runway?.speedup_suggestion && !goal.is_completed && (
                    <div className="flex items-center gap-1 text-[10px] text-primary bg-primary/5 px-2 py-1 rounded-md border border-primary/20">
                      <Zap className="w-3 h-3 shrink-0 text-amber-400" />
                      <span className="truncate">{goal.runway.speedup_suggestion}</span>
                    </div>
                  )}

                  {/* Contribute Button */}
                  <button
                    onClick={() => {
                      setGoalToContribute(goal);
                      setContributeModalOpen(true);
                    }}
                    className="w-full flex items-center justify-center gap-1.5 py-2 rounded-xl bg-muted/40 hover:bg-muted/80 text-foreground font-semibold text-xs border border-border/60 hover:border-primary/40 transition-all"
                  >
                    <ArrowDownRight className="w-3.5 h-3.5 text-emerald-400" />
                    <span>Deposit / Withdraw Funds</span>
                  </button>
                </div>
              </div>
            );
          })}
        </div>
      )}

      {/* Modals */}
      <GoalFormModal
        isOpen={formModalOpen}
        onClose={() => setFormModalOpen(false)}
        onSuccess={loadData}
        goalToEdit={goalToEdit}
      />

      {goalToContribute && (
        <GoalContributeModal
          isOpen={contributeModalOpen}
          onClose={() => {
            setContributeModalOpen(false);
            setGoalToContribute(null);
          }}
          onSuccess={loadData}
          goal={goalToContribute}
        />
      )}
    </div>
  );
}
