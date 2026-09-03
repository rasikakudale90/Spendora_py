"use client";

import React, { useState } from "react";
import { aiApi, PurchaseSimulationResponse, Category } from "@/lib/api";
import { toast } from "sonner";
import {
  Sparkles,
  ShoppingBag,
  TrendingDown,
  TrendingUp,
  AlertTriangle,
  CheckCircle2,
  XCircle,
  Clock,
  ArrowRight,
  ShieldCheck,
  RefreshCw,
  X,
  PlusCircle,
  Lightbulb,
  Zap,
} from "lucide-react";

interface PurchaseSimulatorModalProps {
  isOpen: boolean;
  onClose: () => void;
  categories: Category[];
  onAddAsExpense?: (item: { title: string; amount: string; category_id?: string }) => void;
}

const QUICK_EXAMPLES = [
  { title: "Smartwatch", amount: "12000" },
  { title: "Weekend Dinner", amount: "2500" },
  { title: "Wireless Headphones", amount: "6500" },
  { title: "Gym Membership", amount: "8000" },
];

export function PurchaseSimulatorModal({
  isOpen,
  onClose,
  categories,
  onAddAsExpense,
}: PurchaseSimulatorModalProps) {
  const [title, setTitle] = useState("");
  const [amount, setAmount] = useState("");
  const [categoryId, setCategoryId] = useState("");
  const [isLoading, setIsLoading] = useState(false);
  const [result, setResult] = useState<PurchaseSimulationResponse | null>(null);

  if (!isOpen) return null;

  const handleSimulate = async (e?: React.FormEvent) => {
    if (e) e.preventDefault();
    if (!title.trim()) {
      toast.error("Please enter a purchase title");
      return;
    }
    const numAmount = parseFloat(amount);
    if (isNaN(numAmount) || numAmount <= 0) {
      toast.error("Please enter a valid amount greater than ₹0");
      return;
    }

    setIsLoading(true);
    try {
      const res = await aiApi.simulatePurchase({
        title: title.trim(),
        amount: numAmount,
        category_id: categoryId || null,
      });
      setResult(res);
    } catch (err: any) {
      toast.error(err.message || "Failed to simulate purchase");
    } finally {
      setIsLoading(false);
    }
  };

  const handleReset = () => {
    setResult(null);
    setTitle("");
    setAmount("");
    setCategoryId("");
  };

  const handleQuickPick = (item: { title: string; amount: string }) => {
    setTitle(item.title);
    setAmount(item.amount);
    setResult(null);
  };

  const handleProceedToExpense = () => {
    if (!result) return;
    if (onAddAsExpense) {
      onAddAsExpense({
        title: result.item_title,
        amount: String(result.item_amount),
        category_id: categoryId || undefined,
      });
    }
    onClose();
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-background/80 backdrop-blur-md animate-in fade-in duration-200">
      <div className="glass-card relative w-full max-w-xl max-h-[90vh] overflow-y-auto rounded-3xl border border-border/80 p-6 sm:p-8 shadow-2xl bg-card/95">
        {/* Close Button */}
        <button
          onClick={onClose}
          className="absolute top-5 right-5 p-2 rounded-full text-muted-foreground hover:text-foreground hover:bg-muted/60 transition-colors"
          aria-label="Close modal"
        >
          <X className="w-5 h-5" />
        </button>

        {/* Header */}
        <div className="flex items-center gap-3 mb-6">
          <div className="w-12 h-12 rounded-2xl bg-gradient-to-tr from-indigo-500 to-purple-500 text-white flex items-center justify-center font-bold shadow-lg shadow-indigo-500/20">
            <Sparkles className="w-6 h-6" />
          </div>
          <div>
            <h2 className="text-xl font-black text-foreground flex items-center gap-2">
              Can I Afford This?
              <span className="text-[10px] uppercase font-bold tracking-wider px-2 py-0.5 rounded-full bg-indigo-500/10 text-indigo-500 border border-indigo-500/20">
                AI Simulator
              </span>
            </h2>
            <p className="text-xs text-muted-foreground mt-0.5">
              Simulate purchase impact on your live monthly budget, cash flow, and savings.
            </p>
          </div>
        </div>

        {/* Form */}
        <form onSubmit={handleSimulate} className="space-y-4">
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
            <div>
              <label className="block text-xs font-semibold text-foreground mb-1">
                Item / Purchase Title
              </label>
              <div className="relative">
                <ShoppingBag className="w-4 h-4 text-muted-foreground absolute left-3 top-1/2 -translate-y-1/2" />
                <input
                  type="text"
                  required
                  value={title}
                  onChange={(e) => setTitle(e.target.value)}
                  placeholder="e.g. Sony Headphones"
                  className="w-full pl-9 pr-3 py-2.5 rounded-xl bg-muted/40 border border-border/80 focus:outline-none focus:ring-2 focus:ring-primary/40 text-sm text-foreground placeholder:text-muted-foreground/50 transition-all"
                />
              </div>
            </div>

            <div>
              <label className="block text-xs font-semibold text-foreground mb-1">
                Amount (₹)
              </label>
              <div className="relative">
                <span className="text-muted-foreground absolute left-3 top-1/2 -translate-y-1/2 font-semibold text-sm">
                  ₹
                </span>
                <input
                  type="number"
                  step="0.01"
                  min="1"
                  required
                  value={amount}
                  onChange={(e) => setAmount(e.target.value)}
                  placeholder="e.g. 8500"
                  className="w-full pl-8 pr-3 py-2.5 rounded-xl bg-muted/40 border border-border/80 focus:outline-none focus:ring-2 focus:ring-primary/40 text-sm text-foreground placeholder:text-muted-foreground/50 font-mono font-medium transition-all"
                />
              </div>
            </div>
          </div>

          {/* Optional Category */}
          <div>
            <label className="block text-xs font-semibold text-foreground mb-1">
              Target Category (Optional)
            </label>
            <select
              value={categoryId}
              onChange={(e) => setCategoryId(e.target.value)}
              className="w-full px-3 py-2.5 rounded-xl bg-muted/40 border border-border/80 focus:outline-none focus:ring-2 focus:ring-primary/40 text-sm text-foreground transition-all"
            >
              <option value="">-- General / Discretionary --</option>
              {categories.map((cat) => (
                <option key={cat.id} value={cat.id}>
                  {cat.name}
                </option>
              ))}
            </select>
          </div>

          {/* Quick Example Chips */}
          {!result && (
            <div className="pt-1">
              <span className="text-[11px] font-semibold text-muted-foreground flex items-center gap-1 mb-1.5">
                <Zap className="w-3 h-3 text-amber-500" /> Quick Test:
              </span>
              <div className="flex flex-wrap gap-1.5">
                {QUICK_EXAMPLES.map((ex, i) => (
                  <button
                    key={i}
                    type="button"
                    onClick={() => handleQuickPick(ex)}
                    className="text-xs px-2.5 py-1 rounded-lg bg-muted/40 hover:bg-muted/70 border border-border/60 text-muted-foreground hover:text-foreground font-medium transition-colors"
                  >
                    {ex.title} (₹{Number(ex.amount).toLocaleString("en-IN")})
                  </button>
                ))}
              </div>
            </div>
          )}

          {/* Submit Button */}
          <button
            type="submit"
            disabled={isLoading || !title || !amount}
            className="w-full py-3 px-4 rounded-xl bg-gradient-to-r from-indigo-600 to-purple-600 text-white font-semibold text-sm hover:opacity-95 transition-all shadow-md shadow-indigo-500/20 flex items-center justify-center gap-2 disabled:opacity-50"
          >
            {isLoading ? (
              <>
                <RefreshCw className="w-4 h-4 animate-spin" />
                Simulating Financial Impact...
              </>
            ) : (
              <>
                <Sparkles className="w-4 h-4" />
                Run AI Purchase Simulation
              </>
            )}
          </button>
        </form>

        {/* RESULTS SECTION */}
        {result && (
          <div className="mt-6 pt-6 border-t border-border/60 space-y-4 animate-in fade-in slide-in-from-bottom-2 duration-300">
            {/* Verdict Header Banner */}
            <div
              className={`p-4 rounded-2xl border flex items-start gap-3.5 shadow-sm ${
                result.verdict === "safe"
                  ? "bg-emerald-500/10 border-emerald-500/30 text-emerald-950 dark:text-emerald-100"
                  : result.verdict === "caution"
                  ? "bg-amber-500/10 border-amber-500/30 text-amber-950 dark:text-amber-100"
                  : "bg-rose-500/10 border-rose-500/30 text-rose-950 dark:text-rose-100"
              }`}
            >
              <div className="mt-0.5">
                {result.verdict === "safe" && (
                  <CheckCircle2 className="w-6 h-6 text-emerald-500" />
                )}
                {result.verdict === "caution" && (
                  <AlertTriangle className="w-6 h-6 text-amber-500" />
                )}
                {result.verdict === "over_budget" && (
                  <XCircle className="w-6 h-6 text-rose-500" />
                )}
              </div>
              <div className="flex-1">
                <div className="flex items-center justify-between">
                  <h3 className="font-bold text-sm sm:text-base">
                    {result.verdict_title}
                  </h3>
                  <span
                    className={`text-[10px] uppercase tracking-wider font-extrabold px-2 py-0.5 rounded-full ${
                      result.verdict === "safe"
                        ? "bg-emerald-500 text-white"
                        : result.verdict === "caution"
                        ? "bg-amber-500 text-slate-950"
                        : "bg-rose-500 text-white"
                    }`}
                  >
                    {result.verdict.replace("_", " ")}
                  </span>
                </div>
                <p className="text-xs mt-1 opacity-90 leading-relaxed">
                  {result.verdict_summary}
                </p>
              </div>
            </div>

            {/* Impact Metric Cards */}
            <div className="grid grid-cols-2 sm:grid-cols-3 gap-2.5">
              {/* Savings Rate */}
              <div className="p-3 rounded-xl bg-muted/40 border border-border/60">
                <span className="text-[11px] font-semibold text-muted-foreground block">
                  Savings Rate
                </span>
                <div className="flex items-center gap-1.5 mt-1">
                  <span className="text-sm font-bold text-foreground">
                    {result.current_savings_rate}%
                  </span>
                  <ArrowRight className="w-3 h-3 text-muted-foreground" />
                  <span
                    className={`text-sm font-bold ${
                      result.projected_savings_rate >= 20
                        ? "text-emerald-500"
                        : result.projected_savings_rate >= 10
                        ? "text-amber-500"
                        : "text-rose-500"
                    }`}
                  >
                    {result.projected_savings_rate}%
                  </span>
                </div>
              </div>

              {/* Net Cash Flow */}
              <div className="p-3 rounded-xl bg-muted/40 border border-border/60">
                <span className="text-[11px] font-semibold text-muted-foreground block">
                  Net Balance
                </span>
                <div className="flex items-center gap-1.5 mt-1 font-mono">
                  <span className="text-xs font-semibold text-foreground">
                    ₹{Number(result.current_cash_flow).toLocaleString("en-IN")}
                  </span>
                  <ArrowRight className="w-3 h-3 text-muted-foreground" />
                  <span
                    className={`text-xs font-bold ${
                      Number(result.projected_cash_flow) >= 0
                        ? "text-foreground"
                        : "text-rose-500"
                    }`}
                  >
                    ₹{Number(result.projected_cash_flow).toLocaleString("en-IN")}
                  </span>
                </div>
              </div>

              {/* Daily Safe Spend */}
              <div className="p-3 rounded-xl bg-muted/40 border border-border/60 col-span-2 sm:col-span-1">
                <span className="text-[11px] font-semibold text-muted-foreground block">
                  Daily Burn Allowance
                </span>
                <div className="flex items-center gap-1.5 mt-1 font-mono">
                  <span className="text-xs font-semibold text-foreground">
                    ₹{Number(result.daily_safe_spend_before).toLocaleString("en-IN")}
                  </span>
                  <ArrowRight className="w-3 h-3 text-muted-foreground" />
                  <span className="text-xs font-bold text-indigo-500">
                    ₹{Number(result.daily_safe_spend_after).toLocaleString("en-IN")}/d
                  </span>
                </div>
              </div>
            </div>

            {/* AI Advisor Narrative */}
            <div className="p-4 rounded-2xl bg-indigo-500/5 border border-indigo-500/20 space-y-2">
              <div className="flex items-center justify-between">
                <span className="text-xs font-bold text-indigo-500 flex items-center gap-1.5">
                  <Lightbulb className="w-3.5 h-3.5" /> AI Advisor Insights
                </span>
                <span className="text-[10px] text-muted-foreground font-mono">
                  {result.provider_used}
                </span>
              </div>
              <p className="text-xs text-foreground/90 leading-relaxed">
                {result.ai_analysis}
              </p>
            </div>

            {/* Actionable Recommendations Checklist */}
            {result.actionable_tips && result.actionable_tips.length > 0 && (
              <div className="p-3.5 rounded-xl bg-muted/30 border border-border/60 space-y-1.5">
                <span className="text-[11px] font-bold text-muted-foreground uppercase tracking-wider block mb-1">
                  Actionable Next Steps:
                </span>
                {result.actionable_tips.map((tip, idx) => (
                  <div key={idx} className="flex items-start gap-2 text-xs text-foreground/90">
                    <ShieldCheck className="w-3.5 h-3.5 text-indigo-500 mt-0.5 shrink-0" />
                    <span>{tip}</span>
                  </div>
                ))}
              </div>
            )}

            {/* Modal Actions */}
            <div className="flex items-center gap-2 pt-2">
              <button
                type="button"
                onClick={handleReset}
                className="flex-1 py-2.5 px-4 rounded-xl border border-border/80 bg-muted/30 hover:bg-muted/60 text-xs font-semibold text-foreground transition-colors"
              >
                Test Another Item
              </button>
              {onAddAsExpense && (
                <button
                  type="button"
                  onClick={handleProceedToExpense}
                  className="flex-1 py-2.5 px-4 rounded-xl bg-primary text-white text-xs font-semibold hover:bg-primary/90 transition-all flex items-center justify-center gap-1.5 shadow-sm"
                >
                  <PlusCircle className="w-3.5 h-3.5" />
                  Add as Expense
                </button>
              )}
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
