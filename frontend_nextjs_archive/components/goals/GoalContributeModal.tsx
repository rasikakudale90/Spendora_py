"use client";

import React, { useState } from "react";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { toast } from "sonner";
import { X, ArrowDownRight, ArrowUpRight, DollarSign, Sparkles } from "lucide-react";
import { goalsApi, Goal } from "@/lib/api";
import { goalContributeSchema, GoalContributeValues } from "@/lib/schemas";
import { formatCurrency } from "@/lib/utils";

interface GoalContributeModalProps {
  isOpen: boolean;
  onClose: () => void;
  onSuccess: () => void;
  goal: Goal;
}

const QUICK_AMOUNTS = [1000, 2500, 5000, 10000];

export const GoalContributeModal: React.FC<GoalContributeModalProps> = ({
  isOpen,
  onClose,
  onSuccess,
  goal,
}) => {
  const [action, setAction] = useState<"deposit" | "withdraw">("deposit");

  const {
    register,
    handleSubmit,
    setValue,
    watch,
    reset,
    formState: { errors, isSubmitting },
  } = useForm<GoalContributeValues>({
    resolver: zodResolver(goalContributeSchema),
    defaultValues: {
      amount: "",
      action: "deposit",
      notes: "",
    },
  });

  const enteredAmount = parseFloat(watch("amount") || "0") || 0;
  const currentVal = Number(goal.current_amount) || 0;
  const targetVal = Number(goal.target_amount) || 1;

  const newBalance =
    action === "deposit"
      ? currentVal + enteredAmount
      : Math.max(0, currentVal - enteredAmount);

  const newProgress = Math.min(100, (newBalance / targetVal) * 100);

  if (!isOpen) return null;

  const onSubmit = async (values: GoalContributeValues) => {
    try {
      await goalsApi.contribute(goal.id, {
        amount: values.amount,
        action,
        notes: values.notes || null,
      });

      toast.success(
        action === "deposit"
          ? `Deposited ${formatCurrency(Number(values.amount))} into '${goal.name}'!`
          : `Withdrew ${formatCurrency(Number(values.amount))} from '${goal.name}'`
      );
      reset();
      onSuccess();
      onClose();
    } catch (err: any) {
      toast.error(err.message || "Failed to process contribution.");
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-background/80 backdrop-blur-sm animate-in fade-in duration-200">
      <div
        className="relative w-full max-w-md rounded-2xl border border-border/80 bg-card p-6 shadow-2xl space-y-5"
        onClick={(e) => e.stopPropagation()}
      >
        {/* Header */}
        <div className="flex items-center justify-between pb-3 border-b border-border/60">
          <div>
            <h2 className="text-lg font-bold text-foreground">
              {action === "deposit" ? "Add to Goal" : "Withdraw Funds"}
            </h2>
            <p className="text-xs text-muted-foreground truncate max-w-[280px]">
              {goal.name} (Current: {formatCurrency(currentVal)})
            </p>
          </div>
          <button
            onClick={onClose}
            className="p-1.5 rounded-lg text-muted-foreground hover:text-foreground hover:bg-muted/60 transition-colors"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Action Toggle */}
        <div className="grid grid-cols-2 p-1 rounded-xl bg-muted/30 border border-border/60">
          <button
            type="button"
            onClick={() => {
              setAction("deposit");
              setValue("action", "deposit");
            }}
            className={`flex items-center justify-center gap-1.5 py-2 text-xs font-bold rounded-lg transition-all ${
              action === "deposit"
                ? "bg-emerald-500 text-slate-950 shadow-md"
                : "text-muted-foreground hover:text-foreground"
            }`}
          >
            <ArrowDownRight className="w-4 h-4" />
            <span>Deposit Funds</span>
          </button>
          <button
            type="button"
            onClick={() => {
              setAction("withdraw");
              setValue("action", "withdraw");
            }}
            className={`flex items-center justify-center gap-1.5 py-2 text-xs font-bold rounded-lg transition-all ${
              action === "withdraw"
                ? "bg-rose-500 text-white shadow-md"
                : "text-muted-foreground hover:text-foreground"
            }`}
          >
            <ArrowUpRight className="w-4 h-4" />
            <span>Withdraw Funds</span>
          </button>
        </div>

        {/* Quick Amount Chips */}
        <div className="space-y-1.5">
          <span className="text-xs text-muted-foreground">Quick Add</span>
          <div className="grid grid-cols-4 gap-2">
            {QUICK_AMOUNTS.map((amt) => (
              <button
                type="button"
                key={amt}
                onClick={() => setValue("amount", String(amt))}
                className="py-1.5 px-2 text-xs font-semibold rounded-lg border border-border/70 hover:border-primary/50 hover:bg-primary/5 text-foreground transition-all"
              >
                +{formatCurrency(amt)}
              </button>
            ))}
          </div>
        </div>

        {/* Form */}
        <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
          <div className="space-y-1.5">
            <label className="text-xs font-semibold text-foreground flex items-center gap-1.5">
              <DollarSign className="w-3.5 h-3.5 text-muted-foreground" />
              <span>Amount (₹)</span>
            </label>
            <input
              {...register("amount")}
              type="number"
              step="0.01"
              placeholder="e.g. 5000"
              className="w-full px-3.5 py-2 text-sm rounded-xl border border-border/80 bg-muted/20 text-foreground placeholder:text-muted-foreground/60 focus:outline-none focus:ring-2 focus:ring-primary/40"
            />
            {errors.amount && (
              <p className="text-xs text-rose-500">{errors.amount.message}</p>
            )}
          </div>

          {/* Dynamic Progress Forecast Preview */}
          <div className="p-3 rounded-xl border border-border/50 bg-muted/20 space-y-2">
            <div className="flex items-center justify-between text-xs">
              <span className="text-muted-foreground">Projected Balance:</span>
              <span className="font-bold text-foreground">
                {formatCurrency(newBalance)} / {formatCurrency(targetVal)}
              </span>
            </div>
            <div className="w-full bg-muted/60 rounded-full h-2 overflow-hidden">
              <div
                className={`h-2 rounded-full transition-all duration-300 ${
                  newProgress >= 100 ? "bg-emerald-400" : "bg-primary"
                }`}
                style={{ width: `${newProgress}%` }}
              />
            </div>
            <div className="flex items-center justify-between text-[10px] text-muted-foreground">
              <span>Current: {goal.progress_percentage}%</span>
              <span className="font-bold text-foreground">New: {newProgress.toFixed(1)}%</span>
            </div>
          </div>

          <div className="space-y-1.5">
            <label className="text-xs font-semibold text-foreground">
              Notes (Optional)
            </label>
            <input
              {...register("notes")}
              type="text"
              placeholder="e.g. Monthly salary savings transfer"
              className="w-full px-3.5 py-2 text-sm rounded-xl border border-border/80 bg-muted/20 text-foreground placeholder:text-muted-foreground/60 focus:outline-none focus:ring-2 focus:ring-primary/40"
            />
          </div>

          {/* Actions */}
          <div className="flex items-center justify-end gap-3 pt-3 border-t border-border/60">
            <button
              type="button"
              onClick={onClose}
              className="px-4 py-2 text-sm font-medium rounded-xl border border-border/80 text-muted-foreground hover:text-foreground hover:bg-muted/50 transition-colors"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={isSubmitting || enteredAmount <= 0}
              className={`px-5 py-2 text-sm font-bold rounded-xl shadow-lg transition-all ${
                action === "deposit"
                  ? "bg-emerald-500 text-slate-950 hover:bg-emerald-400 shadow-emerald-500/20"
                  : "bg-rose-500 text-white hover:bg-rose-600 shadow-rose-500/20"
              } disabled:opacity-50`}
            >
              {isSubmitting
                ? "Processing..."
                : action === "deposit"
                ? "Confirm Deposit"
                : "Confirm Withdrawal"}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};
