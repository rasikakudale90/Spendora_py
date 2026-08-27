"use client";

import { useState, useEffect } from "react";
import { DashboardSkeleton } from "@/components/LoadingSkeleton";
import Hero3D from "@/components/Hero3D";
import { KpiCards } from "@/components/dashboard/KpiCards";
import { DashboardCharts } from "@/components/dashboard/Charts";
import { api, DashboardSummary, DashboardStats, CategoryBreakdownItem, TrendItem, Expense } from "@/lib/api";
import { Card, CardHeader, CardTitle, CardContent } from "@/components/ui/card";
import { toast } from "sonner";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Folder, Target } from "lucide-react";
import { CategoryManagerModal } from "@/components/CategoryManagerModal";
import { BudgetManagerModal } from "@/components/BudgetManagerModal";

export default function DashboardPage() {
  const [loading, setLoading] = useState(true);
  const [summary, setSummary] = useState<DashboardSummary | null>(null);
  const [stats, setStats] = useState<DashboardStats | null>(null);
  const [breakdown, setBreakdown] = useState<CategoryBreakdownItem[]>([]);
  const [trend, setTrend] = useState<TrendItem[]>([]);
  const [recent, setRecent] = useState<Expense[]>([]);

  // Modals
  const [isCatModalOpen, setIsCatModalOpen] = useState(false);
  const [isBudgetModalOpen, setIsBudgetModalOpen] = useState(false);

  const loadDashboard = async () => {
    try {
      setLoading(true);
      const [sumRes, statRes, breakRes, trendRes, recentRes] = await Promise.all([
        api.getDashboardSummary(),
        api.getDashboardStats(),
        api.getCategoryBreakdown(),
        api.getTrend(),
        api.getRecentExpenses(5)
      ]);
      
      setSummary(sumRes);
      setStats(statRes);
      setBreakdown(breakRes);
      setTrend(trendRes);
      setRecent(recentRes);
    } catch (error: any) {
      toast.error("Failed to load dashboard data", {
        description: error.message
      });
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadDashboard();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const refreshData = () => {
    loadDashboard();
  };

  if (loading) return <DashboardSkeleton />;

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl sm:text-3xl font-bold bg-gradient-to-r from-slate-900 via-slate-700 to-slate-500 dark:from-white dark:via-slate-200 dark:to-slate-400 bg-clip-text text-transparent">
            Financial Dashboard
          </h1>
          <p className="text-sm text-muted-foreground mt-1">
            Real-time spending analysis and multi-period budget control (Weekly, Monthly, Yearly)
          </p>
        </div>
        <div className="flex gap-2">
          <Button variant="outline" onClick={() => setIsCatModalOpen(true)} className="gap-2">
            <Folder className="w-4 h-4 text-emerald-600 dark:text-emerald-400" /> <span className="hidden sm:inline">Categories</span>
          </Button>
          <Button variant="outline" onClick={() => setIsBudgetModalOpen(true)} className="gap-2">
            <Target className="w-4 h-4 text-blue-600 dark:text-blue-400" /> <span className="hidden sm:inline">Budgets</span>
          </Button>
        </div>
      </div>
      
      {/* 3D Visual Moment Banner */}
      <div className="w-full h-32 rounded-2xl overflow-hidden border border-border/70 bg-gradient-to-r from-emerald-500/10 via-teal-500/5 to-cyan-500/10 dark:from-emerald-950/40 dark:via-slate-900/40 dark:to-teal-950/40 relative shadow-sm">
        <div className="absolute inset-0 z-10 pointer-events-none bg-gradient-to-r from-background/90 via-background/40 to-transparent" />
        <div className="absolute left-6 top-1/2 -translate-y-1/2 z-20">
          <h2 className="text-xl font-bold text-foreground">Spendora V1</h2>
          <p className="text-muted-foreground text-sm">Take control of your personal finances</p>
        </div>
        <Hero3D />
      </div>

      <KpiCards summary={summary} stats={stats} />
      
      <DashboardCharts breakdown={breakdown} trend={trend} />

      <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-7 mt-4">
        <Card className="col-span-4 glass-card">
          <CardHeader>
            <CardTitle className="text-base font-semibold">Recent Activity</CardTitle>
          </CardHeader>
          <CardContent>
            {recent.length === 0 ? (
              <p className="text-muted-foreground text-sm py-4 text-center">No recent expenses found.</p>
            ) : (
              <div className="space-y-3">
                {recent.map((expense) => (
                  <div key={expense.id} className="flex items-center justify-between border-b border-border/50 pb-3.5 last:border-0 last:pb-0">
                    <div>
                      <p className="font-medium text-sm text-foreground">{expense.title}</p>
                      <p className="text-xs text-muted-foreground mt-0.5">
                        {new Intl.DateTimeFormat('en-IN', { month: 'short', day: '2-digit', year: 'numeric' }).format(new Date(expense.expense_date))} • {expense.category?.name}
                      </p>
                    </div>
                    <div className="flex items-center gap-3">
                      {expense.payment_mode && (
                        <Badge variant="outline" className="text-xs">{expense.payment_mode}</Badge>
                      )}
                      <span className="font-bold text-sm text-foreground">₹{parseFloat(expense.amount).toLocaleString('en-IN')}</span>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </CardContent>
        </Card>
      </div>

      <CategoryManagerModal open={isCatModalOpen} onOpenChange={setIsCatModalOpen} />
      <BudgetManagerModal open={isBudgetModalOpen} onOpenChange={setIsBudgetModalOpen} onSuccess={refreshData} />
    </div>
  );
}
