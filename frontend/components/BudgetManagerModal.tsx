import { useState, useEffect } from "react";
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogFooter } from "@/components/ui/dialog";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { api, Category, BudgetListResponse } from "@/lib/api";
import { toast } from "sonner";

interface BudgetManagerModalProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  onSuccess?: () => void;
}

export function BudgetManagerModal({ open, onOpenChange, onSuccess }: BudgetManagerModalProps) {
  const [categories, setCategories] = useState<Category[]>([]);
  const [budgets, setBudgets] = useState<BudgetListResponse | null>(null);
  const [loading, setLoading] = useState(false);
  
  const now = new Date();
  const currentMonth = `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}`;
  const currentMonthDate = `${currentMonth}-01`;
  
  const [scope, setScope] = useState<"overall" | "category">("overall");
  const [categoryId, setCategoryId] = useState<string>("");
  const [amount, setAmount] = useState<string>("");

  const fetchData = async () => {
    try {
      const [cats, buds] = await Promise.all([
        api.getCategories(),
        api.getBudgets(currentMonthDate)
      ]);
      setCategories(cats);
      setBudgets(buds);
    } catch (error) {
      toast.error("Failed to load budgets");
    }
  };

  useEffect(() => {
    if (open) fetchData();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [open]);

  const handleSetBudget = async () => {
    if (!amount || isNaN(parseFloat(amount)) || parseFloat(amount) <= 0) {
      toast.error("Please enter a valid amount");
      return;
    }
    if (scope === "category" && (!categoryId || categoryId === "none")) {
      toast.error("Please select a category");
      return;
    }

    try {
      setLoading(true);
      await api.setBudget({
        scope,
        category_id: scope === "category" ? categoryId : null,
        amount,
        period_month: currentMonthDate
      });
      toast.success("Budget set successfully");
      setAmount("");
      fetchData();
      if (onSuccess) onSuccess();
    } catch (error: any) {
      toast.error("Failed to set budget", { description: error.message });
    } finally {
      setLoading(false);
    }
  };

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="sm:max-w-[450px] bg-slate-900 border-white/10 text-white">
        <DialogHeader>
          <DialogTitle>Manage Budgets</DialogTitle>
        </DialogHeader>
        
        <div className="space-y-6 pt-4">
          <div className="space-y-4 bg-slate-800/30 p-4 rounded-lg border border-white/5">
            <h3 className="font-medium text-sm text-slate-300">Set New Budget</h3>
            <div className="grid grid-cols-2 gap-4">
              <div className="space-y-2">
                <Label>Type</Label>
                <Select value={scope} onValueChange={(v: "overall"|"category") => setScope(v)}>
                  <SelectTrigger className="bg-slate-800 border-white/10">
                    <SelectValue />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="overall">Overall</SelectItem>
                    <SelectItem value="category">By Category</SelectItem>
                  </SelectContent>
                </Select>
              </div>

              {scope === "category" && (
                <div className="space-y-2">
                  <Label>Category</Label>
                  <Select value={categoryId} onValueChange={setCategoryId}>
                    <SelectTrigger className="bg-slate-800 border-white/10">
                      <SelectValue placeholder="Select..." />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value="none">Select...</SelectItem>
                      {categories.map((c) => (
                        <SelectItem key={c.id} value={c.id}>{c.name}</SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                </div>
              )}
            </div>

            <div className="space-y-2">
              <Label>Amount (₹)</Label>
              <div className="flex gap-2">
                <Input 
                  type="number"
                  placeholder="e.g. 50000"
                  value={amount}
                  onChange={(e) => setAmount(e.target.value)}
                  className="bg-slate-800 border-white/10"
                />
                <Button onClick={handleSetBudget} disabled={loading} className="bg-blue-600 hover:bg-blue-700">
                  Save
                </Button>
              </div>
            </div>
          </div>

          <div className="space-y-3">
            <h3 className="font-medium text-sm text-slate-300">Current Budgets ({currentMonth})</h3>
            
            {budgets?.overall_budget && (
              <div className="flex justify-between items-center p-3 bg-slate-800/50 rounded border border-white/5">
                <div>
                  <span className="font-bold">Overall Budget</span>
                  <div className="text-xs text-muted-foreground mt-1">
                    Spent: ₹{parseFloat(budgets.overall_budget.spent).toLocaleString('en-IN')}
                  </div>
                </div>
                <div className="text-right">
                  <div className="font-bold text-emerald-400">₹{parseFloat(budgets.overall_budget.amount).toLocaleString('en-IN')}</div>
                  <div className="text-xs text-muted-foreground mt-1">
                    {budgets.overall_budget.percentage_used.toFixed(1)}% used
                  </div>
                </div>
              </div>
            )}

            {budgets?.category_budgets.map(b => (
              <div key={b.id} className="flex justify-between items-center p-3 bg-slate-800/50 rounded border border-white/5">
                <div>
                  <span className="font-medium text-sm">{b.category_name}</span>
                  <div className="text-xs text-muted-foreground mt-1">
                    Spent: ₹{parseFloat(b.spent).toLocaleString('en-IN')}
                  </div>
                </div>
                <div className="text-right">
                  <div className="font-medium text-sm text-blue-400">₹{parseFloat(b.amount).toLocaleString('en-IN')}</div>
                  <div className="text-xs text-muted-foreground mt-1">
                    {b.percentage_used.toFixed(1)}% used
                  </div>
                </div>
              </div>
            ))}
            
            {!budgets?.overall_budget && (!budgets?.category_budgets || budgets.category_budgets.length === 0) && (
              <p className="text-sm text-muted-foreground text-center py-4">No budgets set for this month.</p>
            )}
          </div>
        </div>
      </DialogContent>
    </Dialog>
  );
}
