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
      <Card className="bg-white/5 border-white/10 backdrop-blur-md">
        <CardHeader className="flex flex-row items-center justify-between pb-2">
          <CardTitle className="text-sm font-medium text-slate-300">Total Spent</CardTitle>
          <IndianRupee className="h-4 w-4 text-emerald-400" />
        </CardHeader>
        <CardContent>
          <div className="text-2xl font-bold">₹{parseFloat(summary.total_spent).toLocaleString('en-IN', { minimumFractionDigits: 2 })}</div>
          <p className="text-xs text-muted-foreground mt-1">
            {summary.expense_count} transactions this month
          </p>
        </CardContent>
      </Card>

      <Card className="bg-white/5 border-white/10 backdrop-blur-md">
        <CardHeader className="flex flex-row items-center justify-between pb-2">
          <CardTitle className="text-sm font-medium text-slate-300">Budget Remaining</CardTitle>
          <Target className="h-4 w-4 text-blue-400" />
        </CardHeader>
        <CardContent>
          <div className="text-2xl font-bold">
            {summary.remaining_budget ? `₹${parseFloat(summary.remaining_budget).toLocaleString('en-IN', { minimumFractionDigits: 2 })}` : 'N/A'}
          </div>
          {summary.total_budget && (
            <div className="w-full bg-slate-800 rounded-full h-1.5 mt-3">
              <div 
                className={cn("h-1.5 rounded-full transition-all duration-500", 
                  summary.status === 'on_track' ? 'bg-emerald-400' : 
                  summary.status === 'near_limit' ? 'bg-amber-400' : 'bg-red-500'
                )}
                style={{ width: `${Math.min(summary.percentage_used, 100)}%` }}
              ></div>
            </div>
          )}
        </CardContent>
      </Card>

      <Card className="bg-white/5 border-white/10 backdrop-blur-md">
        <CardHeader className="flex flex-row items-center justify-between pb-2">
          <CardTitle className="text-sm font-medium text-slate-300">Avg Daily Spend</CardTitle>
          <Activity className="h-4 w-4 text-amber-400" />
        </CardHeader>
        <CardContent>
          <div className="text-2xl font-bold">₹{parseFloat(stats.avg_daily_spend).toLocaleString('en-IN', { minimumFractionDigits: 2 })}</div>
          <p className="text-xs text-muted-foreground mt-1">
            Based on {stats.period_month}
          </p>
        </CardContent>
      </Card>

      <Card className="bg-white/5 border-white/10 backdrop-blur-md">
        <CardHeader className="flex flex-row items-center justify-between pb-2">
          <CardTitle className="text-sm font-medium text-slate-300">Highest Expense</CardTitle>
          <TrendingDown className="h-4 w-4 text-rose-400" />
        </CardHeader>
        <CardContent>
          <div className="text-2xl font-bold">
            {stats.highest_expense_amount ? `₹${parseFloat(stats.highest_expense_amount).toLocaleString('en-IN', { minimumFractionDigits: 2 })}` : 'N/A'}
          </div>
          <p className="text-xs text-muted-foreground mt-1 truncate" title={stats.highest_expense_title || ''}>
            {stats.highest_expense_title || 'No expenses'}
          </p>
        </CardContent>
      </Card>
    </div>
  );
}
