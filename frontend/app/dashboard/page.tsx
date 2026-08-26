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
import { Settings, Folder, Target } from "lucide-react";
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
          <h1 className="text-2xl sm:text-3xl font-bold bg-gradient-to-r from-white via-slate-200 to-slate-400 bg-clip-text text-transparent">
            Financial Dashboard
          </h1>
          <p className="text-sm text-muted-foreground mt-1">
            Real-time spending analysis and monthly budget control
          </p>
        </div>
        <div className="flex gap-2">
          <Button variant="outline" onClick={() => setIsCatModalOpen(true)} className="border-white/10 bg-slate-900/50 hover:bg-white/5 gap-2">
            <Folder className="w-4 h-4 text-emerald-400" /> <span className="hidden sm:inline">Categories</span>
          </Button>
          <Button variant="outline" onClick={() => setIsBudgetModalOpen(true)} className="border-white/10 bg-slate-900/50 hover:bg-white/5 gap-2">
            <Target className="w-4 h-4 text-blue-400" /> <span className="hidden sm:inline">Budgets</span>
          </Button>
        </div>
      </div>
      
      {/* 3D Visual Moment - Placed tastefully at the top right of a banner if desired, or kept here */}
      <div className="w-full h-32 rounded-xl overflow-hidden border border-white/10 relative">
        <div className="absolute inset-0 z-10 pointer-events-none bg-gradient-to-r from-background to-transparent" />
        <div className="absolute left-6 top-1/2 -translate-y-1/2 z-20">
          <h2 className="text-xl font-bold text-white">Spendora V1</h2>
          <p className="text-muted-foreground text-sm">Take control of your finances</p>
        </div>
        <Hero3D />
      </div>

      <KpiCards summary={summary} stats={stats} />
      
      <DashboardCharts breakdown={breakdown} trend={trend} />

      <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-7 mt-4">
        <Card className="col-span-4 bg-white/5 border-white/10 backdrop-blur-md">
          <CardHeader>
            <CardTitle>Recent Activity</CardTitle>
          </CardHeader>
          <CardContent>
            {recent.length === 0 ? (
              <p className="text-muted-foreground text-sm py-4 text-center">No recent expenses found.</p>
            ) : (
              <div className="space-y-4">
                {recent.map((expense) => (
                  <div key={expense.id} className="flex items-center justify-between border-b border-white/5 pb-4 last:border-0 last:pb-0">
                    <div>
                      <p className="font-medium">{expense.title}</p>
                      <p className="text-xs text-muted-foreground">
                        {new Intl.DateTimeFormat('en-IN', { month: 'short', day: '2-digit', year: 'numeric' }).format(new Date(expense.expense_date))} • {expense.category?.name}
                      </p>
                    </div>
                    <div className="flex items-center gap-3">
                      {expense.payment_mode && (
                        <Badge variant="outline" className="text-xs">{expense.payment_mode}</Badge>
                      )}
                      <span className="font-bold">₹{parseFloat(expense.amount).toLocaleString('en-IN')}</span>
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
