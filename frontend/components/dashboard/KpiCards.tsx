import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { DashboardSummary, DashboardStats } from "@/lib/api";
import { IndianRupee, TrendingDown, Target, Activity, TrendingUp, PiggyBank } from "lucide-react";
import { cn } from "@/lib/utils";

interface KpiCardsProps {
  summary: DashboardSummary | null;
  stats: DashboardStats | null;
}

export function KpiCards({ summary, stats }: KpiCardsProps) {
  if (!summary || !stats) return null;

  const totalIncome = summary.total_income ? parseFloat(summary.total_income) : 0;
  const netSavings = summary.net_savings ? parseFloat(summary.net_savings) : 0;
  const savingsRate = summary.savings_rate ?? 0;

  return (
    <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-5">
      {/* Total Income Card */}
      <Card className="glass-card glass-card-hover border-emerald-500/20">
        <CardHeader className="flex flex-row items-center justify-between pb-2">
          <CardTitle className="text-sm font-medium text-muted-foreground">Total Income</CardTitle>
          <div className="w-8 h-8 rounded-lg bg-emerald-500/10 flex items-center justify-center">
            <TrendingUp className="h-4 w-4 text-emerald-600 dark:text-emerald-400" />
          </div>
        </CardHeader>
        <CardContent>
          <div className="text-2xl font-bold tracking-tight text-emerald-600 dark:text-emerald-400">
            ₹{totalIncome.toLocaleString("en-IN", { minimumFractionDigits: 2 })}
          </div>
          <p className="text-xs text-muted-foreground mt-1">
            Recorded this month
          </p>
        </CardContent>
      </Card>

      {/* Total Spent Card */}
      <Card className="glass-card glass-card-hover">
        <CardHeader className="flex flex-row items-center justify-between pb-2">
          <CardTitle className="text-sm font-medium text-muted-foreground">Total Spent</CardTitle>
          <div className="w-8 h-8 rounded-lg bg-rose-500/10 flex items-center justify-center">
            <IndianRupee className="h-4 w-4 text-rose-600 dark:text-rose-400" />
          </div>
        </CardHeader>
        <CardContent>
          <div className="text-2xl font-bold tracking-tight text-foreground">
            ₹{parseFloat(summary.total_spent).toLocaleString("en-IN", { minimumFractionDigits: 2 })}
          </div>
          <p className="text-xs text-muted-foreground mt-1">
            {summary.expense_count} transactions this month
          </p>
        </CardContent>
      </Card>

      {/* Net Cash Flow / Savings Card */}
      <Card className="glass-card glass-card-hover">
        <CardHeader className="flex flex-row items-center justify-between pb-2">
          <CardTitle className="text-sm font-medium text-muted-foreground">Net Cash Flow</CardTitle>
          <div
            className={cn(
              "w-8 h-8 rounded-lg flex items-center justify-center",
              netSavings >= 0 ? "bg-teal-500/10" : "bg-rose-500/10"
            )}
          >
            {netSavings >= 0 ? (
              <PiggyBank className="h-4 w-4 text-teal-600 dark:text-teal-400" />
            ) : (
              <TrendingDown className="h-4 w-4 text-rose-600 dark:text-rose-400" />
            )}
          </div>
        </CardHeader>
        <CardContent>
          <div
            className={cn(
              "text-2xl font-bold tracking-tight",
              netSavings >= 0 ? "text-foreground" : "text-rose-600 dark:text-rose-400"
            )}
          >
            {netSavings >= 0 ? "+" : ""}₹
            {netSavings.toLocaleString("en-IN", { minimumFractionDigits: 2 })}
          </div>
          <p className="text-xs text-muted-foreground mt-1">
            {savingsRate > 0
              ? `${savingsRate.toFixed(1)}% savings rate`
              : netSavings < 0
              ? "Deficit this month"
              : "Income = Outflow"}
          </p>
        </CardContent>
      </Card>

      {/* Monthly Budget Remaining Card */}
      <Card className="glass-card glass-card-hover">
        <CardHeader className="flex flex-row items-center justify-between pb-2">
          <CardTitle className="text-sm font-medium text-muted-foreground">Budget Remaining</CardTitle>
          <div className="w-8 h-8 rounded-lg bg-blue-500/10 flex items-center justify-center">
            <Target className="h-4 w-4 text-blue-600 dark:text-blue-400" />
          </div>
        </CardHeader>
        <CardContent>
          <div className="text-2xl font-bold tracking-tight text-foreground">
            {summary.remaining_budget !== undefined && summary.remaining_budget !== null
              ? `₹${Math.max(0, parseFloat(summary.remaining_budget)).toLocaleString("en-IN", { minimumFractionDigits: 2 })}`
              : "N/A"}
          </div>
          {summary.total_budget ? (
            <div className="space-y-1.5 mt-2">
              <div className="flex justify-between text-xs text-muted-foreground">
                <span>Cap: ₹{parseFloat(summary.total_budget).toLocaleString("en-IN")}</span>
                <span>{summary.percentage_used.toFixed(1)}%</span>
              </div>
              <div className="w-full bg-slate-200 dark:bg-slate-800 rounded-full h-1.5 overflow-hidden">
                <div
                  className={cn(
                    "h-1.5 rounded-full transition-all duration-500",
                    summary.status === "on_track"
                      ? "bg-emerald-500"
                      : summary.status === "near_limit"
                      ? "bg-amber-500"
                      : "bg-rose-500"
                  )}
                  style={{ width: `${Math.min(summary.percentage_used, 100)}%` }}
                ></div>
              </div>
            </div>
          ) : (
            <p className="text-xs text-muted-foreground mt-1">No monthly budget set</p>
          )}
        </CardContent>
      </Card>

      {/* Avg Daily Spend Card */}
      <Card className="glass-card glass-card-hover">
        <CardHeader className="flex flex-row items-center justify-between pb-2">
          <CardTitle className="text-sm font-medium text-muted-foreground">Avg Daily Spend</CardTitle>
          <div className="w-8 h-8 rounded-lg bg-amber-500/10 flex items-center justify-center">
            <Activity className="h-4 w-4 text-amber-600 dark:text-amber-400" />
          </div>
        </CardHeader>
        <CardContent>
          <div className="text-2xl font-bold tracking-tight text-foreground">
            ₹{parseFloat(stats.avg_daily_spend).toLocaleString("en-IN", { minimumFractionDigits: 2 })}
          </div>
          <p className="text-xs text-muted-foreground mt-1">Based on {stats.period_month}</p>
        </CardContent>
      </Card>
    </div>
  );
}
