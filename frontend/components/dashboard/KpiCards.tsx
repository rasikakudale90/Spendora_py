import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { DashboardSummary, DashboardStats } from "@/lib/api";
import { IndianRupee, TrendingDown, Target, Activity } from "lucide-react";
import { cn } from "@/lib/utils";

interface KpiCardsProps {
  summary: DashboardSummary | null;
  stats: DashboardStats | null;
}

export function KpiCards({ summary, stats }: KpiCardsProps) {
  if (!summary || !stats) return null;

  return (
    <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
      <Card className="glass-card glass-card-hover">
        <CardHeader className="flex flex-row items-center justify-between pb-2">
          <CardTitle className="text-sm font-medium text-muted-foreground">Total Spent</CardTitle>
          <div className="w-8 h-8 rounded-lg bg-emerald-500/10 flex items-center justify-center">
            <IndianRupee className="h-4 w-4 text-emerald-600 dark:text-emerald-400" />
          </div>
        </CardHeader>
        <CardContent>
          <div className="text-2xl font-bold tracking-tight text-foreground">
            ₹{parseFloat(summary.total_spent).toLocaleString('en-IN', { minimumFractionDigits: 2 })}
          </div>
          <p className="text-xs text-muted-foreground mt-1">
            {summary.expense_count} transactions this month
          </p>
        </CardContent>
      </Card>

      <Card className="glass-card glass-card-hover">
        <CardHeader className="flex flex-row items-center justify-between pb-2">
          <CardTitle className="text-sm font-medium text-muted-foreground">Monthly Budget Remaining</CardTitle>
          <div className="w-8 h-8 rounded-lg bg-blue-500/10 flex items-center justify-center">
            <Target className="h-4 w-4 text-blue-600 dark:text-blue-400" />
          </div>
        </CardHeader>
        <CardContent>
          <div className="text-2xl font-bold tracking-tight text-foreground">
            {summary.remaining_budget !== undefined && summary.remaining_budget !== null 
              ? `₹${Math.max(0, parseFloat(summary.remaining_budget)).toLocaleString('en-IN', { minimumFractionDigits: 2 })}` 
              : 'N/A'}
          </div>
          {summary.total_budget ? (
            <div className="space-y-1.5 mt-2">
              <div className="flex justify-between text-xs text-muted-foreground">
                <span>Monthly Cap: ₹{parseFloat(summary.total_budget).toLocaleString('en-IN')}</span>
                <span>{summary.percentage_used.toFixed(1)}%</span>
              </div>
              <div className="w-full bg-slate-200 dark:bg-slate-800 rounded-full h-1.5 overflow-hidden">
                <div 
                  className={cn("h-1.5 rounded-full transition-all duration-500", 
                    summary.status === 'on_track' ? 'bg-emerald-500' : 
                    summary.status === 'near_limit' ? 'bg-amber-500' : 'bg-rose-500'
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

      <Card className="glass-card glass-card-hover">
        <CardHeader className="flex flex-row items-center justify-between pb-2">
          <CardTitle className="text-sm font-medium text-muted-foreground">Avg Daily Spend</CardTitle>
          <div className="w-8 h-8 rounded-lg bg-amber-500/10 flex items-center justify-center">
            <Activity className="h-4 w-4 text-amber-600 dark:text-amber-400" />
          </div>
        </CardHeader>
        <CardContent>
          <div className="text-2xl font-bold tracking-tight text-foreground">
            ₹{parseFloat(stats.avg_daily_spend).toLocaleString('en-IN', { minimumFractionDigits: 2 })}
          </div>
          <p className="text-xs text-muted-foreground mt-1">
            Based on {stats.period_month}
          </p>
        </CardContent>
      </Card>

      <Card className="glass-card glass-card-hover">
        <CardHeader className="flex flex-row items-center justify-between pb-2">
          <CardTitle className="text-sm font-medium text-muted-foreground">Highest Expense</CardTitle>
          <div className="w-8 h-8 rounded-lg bg-rose-500/10 flex items-center justify-center">
            <TrendingDown className="h-4 w-4 text-rose-600 dark:text-rose-400" />
          </div>
        </CardHeader>
        <CardContent>
          <div className="text-2xl font-bold tracking-tight text-foreground">
            {stats.highest_expense_amount ? `₹${parseFloat(stats.highest_expense_amount).toLocaleString('en-IN', { minimumFractionDigits: 2 })}` : 'N/A'}
          </div>
          <p className="text-xs text-muted-foreground mt-1 truncate" title={stats.highest_expense_title || ''}>
            {stats.highest_expense_title || 'No expenses recorded'}
          </p>
        </CardContent>
      </Card>
    </div>
  );
}
