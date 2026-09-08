import { useState, useEffect } from "react";
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogDescription } from "@/components/ui/dialog";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { Badge } from "@/components/ui/badge";
import { api, Category, Budget, BudgetListResponse, PeriodType } from "@/lib/api";
import { toast } from "sonner";
import { Calendar, CheckCircle2, AlertTriangle, AlertCircle, Sparkles, Pencil, Trash2, X } from "lucide-react";

interface BudgetManagerModalProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  onSuccess?: () => void;
  initialPeriod?: PeriodType;
}

export function BudgetManagerModal({ open, onOpenChange, onSuccess, initialPeriod }: BudgetManagerModalProps) {
  const [categories, setCategories] = useState<Category[]>([]);
  const [budgets, setBudgets] = useState<BudgetListResponse | null>(null);
  const [loading, setLoading] = useState(false);
  const [actionLoading, setActionLoading] = useState(false);
  const [activePeriod, setActivePeriod] = useState<PeriodType>(initialPeriod || "monthly");
  
  const [scope, setScope] = useState<"overall" | "category">("overall");
  const [categoryId, setCategoryId] = useState<string>("");
  const [amount, setAmount] = useState<string>("");
  const [editingBudget, setEditingBudget] = useState<Budget | null>(null);

  const fetchData = async (period: PeriodType = activePeriod) => {
    try {
      setLoading(true);
      const [cats, buds] = await Promise.all([
        api.getCategories(),
        api.getBudgets(undefined, period)
      ]);
      setCategories(cats);
      setBudgets(buds);
    } catch (error) {
      toast.error("Failed to load budgets");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (open) {
      const targetPeriod = initialPeriod || activePeriod;
      setActivePeriod(targetPeriod);
      fetchData(targetPeriod);
      resetForm();
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [open, initialPeriod]);

  const resetForm = () => {
    setEditingBudget(null);
    setScope("overall");
    setCategoryId("");
    setAmount("");
  };

  const handlePeriodChange = (period: PeriodType) => {
    setActivePeriod(period);
    resetForm();
  };

  const startEdit = (budget: Budget) => {
    setEditingBudget(budget);
    setScope(budget.scope);
    setCategoryId(budget.category_id || "");
    setAmount(budget.amount);
  };

  const handleSaveBudget = async () => {
    if (!amount || isNaN(parseFloat(amount)) || parseFloat(amount) <= 0) {
      toast.error("Please enter a valid positive amount");
      return;
    }
    if (scope === "category" && (!categoryId || categoryId === "none")) {
      toast.error("Please select a category");
      return;
    }

    try {
      setActionLoading(true);
      if (editingBudget) {
        await api.updateBudget(editingBudget.id, amount);
        toast.success(`${getPeriodLabel()} budget updated successfully`);
      } else {
        await api.setBudget({
          scope,
          category_id: scope === "category" ? categoryId : null,
          amount,
          period_type: activePeriod,
        });
        toast.success(`${getPeriodLabel()} budget saved successfully`);
      }
      resetForm();
      fetchData(activePeriod);
      if (onSuccess) onSuccess();
    } catch (error: any) {
      toast.error("Failed to save budget", { description: error.message });
    } finally {
      setActionLoading(false);
    }
  };

  const handleDeleteBudget = async (budget: Budget) => {
    const label = budget.scope === "overall" ? "Overall budget" : `${budget.category_name || "Category"} budget`;
    try {
      setActionLoading(true);
      await api.deleteBudget(budget.id);
      toast.success(`${label} deleted successfully`);
      if (editingBudget?.id === budget.id) {
        resetForm();
      }
      fetchData(activePeriod);
      if (onSuccess) onSuccess();
    } catch (error: any) {
      toast.error("Failed to delete budget", { description: error.message });
    } finally {
      setActionLoading(false);
    }
  };

  const getPeriodLabel = () => {
    if (activePeriod === "daily") return "Daily";
    if (activePeriod === "weekly") return "Weekly";
    if (activePeriod === "yearly") return "Yearly";
    return "Monthly";
  };

  const formatPeriodRange = () => {
    if (!budgets?.period_start || !budgets?.period_end) return "";
    const s = new Date(budgets.period_start);
    const e = new Date(budgets.period_end);

    if (activePeriod === "daily") {
      const todayStr = new Date().toISOString().split("T")[0];
      const isToday = budgets.period_start === todayStr;
      return `${isToday ? "Today, " : ""}${s.toLocaleDateString("en-IN", { month: "short", day: "numeric", year: "numeric" })}`;
    }
    if (activePeriod === "weekly") {
      return `${s.toLocaleDateString("en-IN", { month: "short", day: "numeric" })} – ${e.toLocaleDateString("en-IN", { month: "short", day: "numeric", year: "numeric" })}`;
    }
    if (activePeriod === "yearly") {
      return `${s.getFullYear()}`;
    }
    return `${s.toLocaleDateString("en-IN", { month: "long", year: "numeric" })}`;
  };

  const getStatusBadge = (status: "on_track" | "near_limit" | "over_budget") => {
    if (status === "over_budget") {
      return (
        <Badge variant="outline" className="bg-rose-500/10 text-rose-600 dark:text-rose-400 border-rose-200 dark:border-rose-900/50 text-[11px] gap-1">
          <AlertCircle className="w-3 h-3" /> Over Budget
        </Badge>
      );
    }
    if (status === "near_limit") {
      return (
        <Badge variant="outline" className="bg-amber-500/10 text-amber-600 dark:text-amber-400 border-amber-200 dark:border-amber-900/50 text-[11px] gap-1">
          <AlertTriangle className="w-3 h-3" /> Near Limit
        </Badge>
      );
    }
    return (
      <Badge variant="outline" className="bg-emerald-500/10 text-emerald-600 dark:text-emerald-400 border-emerald-200 dark:border-emerald-900/50 text-[11px] gap-1">
        <CheckCircle2 className="w-3 h-3" /> On Track
      </Badge>
    );
  };

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="sm:max-w-[520px] max-h-[90vh] overflow-y-auto">
        <DialogHeader>
          <div className="flex items-center justify-between">
            <DialogTitle className="text-xl font-bold flex items-center gap-2">
              <Sparkles className="w-5 h-5 text-blue-500" />
              Budget Management
            </DialogTitle>
          </div>
          <DialogDescription className="text-xs text-muted-foreground">
            Create, update, or remove spending caps by Daily, Weekly, Monthly, or Yearly horizons
          </DialogDescription>
        </DialogHeader>

        {/* Period Selector Tabs */}
        <div className="flex items-center p-1 bg-muted/70 rounded-xl border border-border/60 gap-1 mt-2">
          {(["daily", "weekly", "monthly", "yearly"] as PeriodType[]).map((period) => (
            <button
              key={period}
              type="button"
              onClick={() => handlePeriodChange(period)}
              className={`flex-1 py-1.5 px-3 text-xs font-semibold rounded-lg transition-all capitalize ${
                activePeriod === period
                  ? "bg-background text-foreground shadow-sm border border-border/80"
                  : "text-muted-foreground hover:text-foreground"
              }`}
            >
              {period === "daily" ? "Daily" : period === "weekly" ? "Weekly" : period === "monthly" ? "Monthly" : "Yearly"}
            </button>
          ))}
        </div>

        {/* Period Context Subheading */}
        <div className="flex items-center justify-between text-xs text-muted-foreground px-1">
          <span className="flex items-center gap-1.5 font-medium text-foreground">
            <Calendar className="w-3.5 h-3.5 text-blue-500" />
            {getPeriodLabel()} Period:
          </span>
          <span className="font-semibold text-foreground/80 bg-muted/50 px-2 py-0.5 rounded-md border border-border/40">
            {formatPeriodRange() || "Current"}
          </span>
        </div>
        
        <div className="space-y-5 pt-1">
          {/* Create / Update Budget Form Card */}
          <div className={`space-y-4 p-4 rounded-xl border transition-all ${
            editingBudget ? "bg-blue-500/5 border-blue-500/30 dark:bg-blue-950/20 dark:border-blue-800/40" : "bg-muted/40 border-border/60"
          } shadow-sm`}>
            <div className="flex items-center justify-between">
              <h3 className="font-semibold text-sm text-foreground flex items-center gap-2">
                <span>{editingBudget ? `Edit ${getPeriodLabel()} Budget` : `Set New ${getPeriodLabel()} Budget`}</span>
                {editingBudget && (
                  <Badge variant="secondary" className="text-[10px] bg-blue-500/10 text-blue-600 dark:text-blue-400">
                    Editing Mode
                  </Badge>
                )}
              </h3>
              {editingBudget && (
                <Button variant="ghost" size="sm" onClick={resetForm} className="h-6 text-xs gap-1 text-muted-foreground hover:text-foreground">
                  <X className="w-3.5 h-3.5" /> Cancel
                </Button>
              )}
            </div>

            <div className="grid grid-cols-2 gap-3.5">
              <div className="space-y-1.5">
                <Label className="text-xs">Scope</Label>
                <Select 
                  value={scope} 
                  onValueChange={(v: "overall"|"category") => setScope(v)}
                  disabled={!!editingBudget}
                >
                  <SelectTrigger className="h-9">
                    <SelectValue />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="overall">Overall Total</SelectItem>
                    <SelectItem value="category">Category Specific</SelectItem>
                  </SelectContent>
                </Select>
              </div>

              {scope === "category" && (
                <div className="space-y-1.5">
                  <Label className="text-xs">Category</Label>
                  <Select 
                    value={categoryId} 
                    onValueChange={setCategoryId}
                    disabled={!!editingBudget}
                  >
                    <SelectTrigger className="h-9">
                      <SelectValue placeholder="Select..." />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value="none">Select category...</SelectItem>
                      {categories.map((c) => (
                        <SelectItem key={c.id} value={c.id}>{c.name}</SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                </div>
              )}
            </div>

            <div className="space-y-1.5">
              <Label className="text-xs">{getPeriodLabel()} Limit Amount (₹)</Label>
              <div className="flex gap-2">
                <Input 
                  type="number"
                  placeholder={activePeriod === "daily" ? "e.g. 1000" : activePeriod === "weekly" ? "e.g. 5000" : activePeriod === "yearly" ? "e.g. 500000" : "e.g. 40000"}
                  value={amount}
                  onChange={(e) => setAmount(e.target.value)}
                  className="h-9"
                />
                <Button onClick={handleSaveBudget} disabled={actionLoading || loading} size="sm" className="shrink-0 px-4">
                  {editingBudget ? "Update" : "Save"}
                </Button>
              </div>
            </div>
          </div>

          {/* Active Budgets List for Current Period */}
          <div className="space-y-3">
            <h3 className="font-semibold text-sm text-foreground flex items-center justify-between">
              <span>Configured {getPeriodLabel()} Budgets</span>
              <span className="text-xs text-muted-foreground font-normal">Manage & update</span>
            </h3>
            
            {budgets?.overall_budget && (
              <div className={`p-3.5 rounded-xl border transition-all space-y-2 ${
                editingBudget?.id === budgets.overall_budget.id 
                  ? "bg-blue-500/10 border-blue-500/50" 
                  : "bg-muted/40 border-border/50 hover:border-border"
              }`}>
                <div className="flex justify-between items-start">
                  <div>
                    <div className="flex items-center gap-2">
                      <span className="font-bold text-sm text-foreground">Overall {getPeriodLabel()} Budget</span>
                      {getStatusBadge(budgets.overall_budget.status)}
                    </div>
                    <div className="text-xs text-muted-foreground mt-1">
                      Spent: ₹{parseFloat(budgets.overall_budget.spent).toLocaleString('en-IN', { minimumFractionDigits: 2 })}
                    </div>
                  </div>
                  <div className="flex items-center gap-3">
                    <div className="text-right">
                      <div className="font-bold text-sm text-emerald-600 dark:text-emerald-400">
                        ₹{parseFloat(budgets.overall_budget.amount).toLocaleString('en-IN', { minimumFractionDigits: 2 })}
                      </div>
                      <div className="text-xs text-muted-foreground mt-1">
                        Rem: ₹{Math.max(0, parseFloat(budgets.overall_budget.remaining)).toLocaleString('en-IN', { minimumFractionDigits: 2 })}
                      </div>
                    </div>
                    <div className="flex items-center gap-1 border-l border-border/60 pl-2">
                      <Button
                        variant="ghost"
                        size="icon"
                        onClick={() => startEdit(budgets.overall_budget!)}
                        className="h-7 w-7 text-muted-foreground hover:text-blue-600 dark:hover:text-blue-400"
                        title="Edit Budget"
                      >
                        <Pencil className="w-3.5 h-3.5" />
                      </Button>
                      <Button
                        variant="ghost"
                        size="icon"
                        onClick={() => handleDeleteBudget(budgets.overall_budget!)}
                        className="h-7 w-7 text-muted-foreground hover:text-rose-600 dark:hover:text-rose-400"
                        title="Delete Budget"
                      >
                        <Trash2 className="w-3.5 h-3.5" />
                      </Button>
                    </div>
                  </div>
                </div>
                <div className="w-full bg-slate-200 dark:bg-slate-800 rounded-full h-1.5 overflow-hidden">
                  <div 
                    className={`h-1.5 rounded-full transition-all duration-500 ${
                      budgets.overall_budget.status === 'on_track' ? 'bg-emerald-500' :
                      budgets.overall_budget.status === 'near_limit' ? 'bg-amber-500' : 'bg-rose-500'
                    }`}
                    style={{ width: `${Math.min(budgets.overall_budget.percentage_used, 100)}%` }}
                  />
                </div>
              </div>
            )}

            {budgets?.category_budgets.map(b => (
              <div key={b.id} className={`p-3.5 rounded-xl border transition-all space-y-2 ${
                editingBudget?.id === b.id 
                  ? "bg-blue-500/10 border-blue-500/50" 
                  : "bg-muted/40 border-border/50 hover:border-border"
              }`}>
                <div className="flex justify-between items-start">
                  <div>
                    <div className="flex items-center gap-2">
                      <span className="font-semibold text-sm text-foreground">{b.category_name}</span>
                      {getStatusBadge(b.status)}
                    </div>
                    <div className="text-xs text-muted-foreground mt-1">
                      Spent: ₹{parseFloat(b.spent).toLocaleString('en-IN', { minimumFractionDigits: 2 })}
                    </div>
                  </div>
                  <div className="flex items-center gap-3">
                    <div className="text-right">
                      <div className="font-semibold text-sm text-blue-600 dark:text-blue-400">
                        ₹{parseFloat(b.amount).toLocaleString('en-IN', { minimumFractionDigits: 2 })}
                      </div>
                      <div className="text-xs text-muted-foreground mt-1">
                        Rem: ₹{Math.max(0, parseFloat(b.remaining)).toLocaleString('en-IN', { minimumFractionDigits: 2 })}
                      </div>
                    </div>
                    <div className="flex items-center gap-1 border-l border-border/60 pl-2">
                      <Button
                        variant="ghost"
                        size="icon"
                        onClick={() => startEdit(b)}
                        className="h-7 w-7 text-muted-foreground hover:text-blue-600 dark:hover:text-blue-400"
                        title="Edit Budget"
                      >
                        <Pencil className="w-3.5 h-3.5" />
                      </Button>
                      <Button
                        variant="ghost"
                        size="icon"
                        onClick={() => handleDeleteBudget(b)}
                        className="h-7 w-7 text-muted-foreground hover:text-rose-600 dark:hover:text-rose-400"
                        title="Delete Budget"
                      >
                        <Trash2 className="w-3.5 h-3.5" />
                      </Button>
                    </div>
                  </div>
                </div>
                <div className="w-full bg-slate-200 dark:bg-slate-800 rounded-full h-1.5 overflow-hidden">
                  <div 
                    className={`h-1.5 rounded-full transition-all duration-500 ${
                      b.status === 'on_track' ? 'bg-emerald-500' :
                      b.status === 'near_limit' ? 'bg-amber-500' : 'bg-rose-500'
                    }`}
                    style={{ width: `${Math.min(b.percentage_used, 100)}%` }}
                  />
                </div>
              </div>
            ))}
            
            {!budgets?.overall_budget && (!budgets?.category_budgets || budgets.category_budgets.length === 0) && (
              <div className="text-center py-6 border border-dashed border-border/60 rounded-xl bg-muted/20">
                <p className="text-xs font-medium text-muted-foreground">No {activePeriod} budgets configured for this period.</p>
                <p className="text-[11px] text-muted-foreground/70 mt-1">Use the form above to set a {getPeriodLabel().toLowerCase()} limit.</p>
              </div>
            )}
          </div>
        </div>
      </DialogContent>
    </Dialog>
  );
}
