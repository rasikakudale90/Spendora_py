"use client";

import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogDescription,
  DialogFooter,
} from "@/components/ui/dialog";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { AlertTriangle, TrendingUp, Sliders, ShieldAlert } from "lucide-react";
import { DailyBudgetAlert } from "@/lib/api";

interface DailyLimitAlertModalProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  alertData: DailyBudgetAlert | null;
  targetDate?: string;
  onOpenBudgetManager?: () => void;
}

export function DailyLimitAlertModal({
  open,
  onOpenChange,
  alertData,
  targetDate,
  onOpenBudgetManager,
}: DailyLimitAlertModalProps) {
  if (!alertData) return null;

  const limitAmount = parseFloat(alertData.limit_amount);
  const totalSpent = parseFloat(alertData.total_spent);
  const exceededAmount = parseFloat(alertData.exceeded_amount);
  const percentageUsed = alertData.percentage_used;

  const formattedDate = targetDate
    ? new Date(targetDate).toLocaleDateString("en-IN", {
        weekday: "short",
        month: "short",
        day: "numeric",
        year: "numeric",
      })
    : "Today";

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="sm:max-w-[480px] p-0 overflow-hidden border-rose-500/30 dark:border-rose-500/20 shadow-2xl shadow-rose-950/20">
        {/* Animated Warning Banner Header */}
        <div className="relative p-6 bg-gradient-to-br from-rose-500/15 via-rose-500/5 to-amber-500/10 border-b border-rose-500/20">
          <div className="flex items-start gap-4">
            <div className="w-12 h-12 rounded-2xl bg-rose-500/15 dark:bg-rose-500/25 border border-rose-500/30 flex items-center justify-center shrink-0 shadow-inner">
              <ShieldAlert className="w-6 h-6 text-rose-600 dark:text-rose-400 animate-pulse" />
            </div>
            <div className="space-y-1">
              <div className="flex items-center gap-2">
                <Badge
                  variant="outline"
                  className="bg-rose-500/10 text-rose-600 dark:text-rose-400 border-rose-300 dark:border-rose-800 text-[10px] font-bold tracking-wider uppercase px-2 py-0.5"
                >
                  Budget Breached
                </Badge>
                <span className="text-xs text-muted-foreground font-medium">
                  {formattedDate}
                </span>
              </div>
              <DialogTitle className="text-xl font-bold text-foreground tracking-tight">
                Daily Limit Exceeded
              </DialogTitle>
              <DialogDescription className="text-xs text-muted-foreground leading-relaxed">
                Your spending has crossed your configured daily expense limit.
              </DialogDescription>
            </div>
          </div>
        </div>

        {/* Content Body */}
        <div className="p-6 space-y-5">
          {/* Key Metrics Cards */}
          <div className="grid grid-cols-3 gap-3">
            <div className="p-3 rounded-xl bg-muted/40 border border-border/60 text-center">
              <p className="text-[11px] text-muted-foreground font-medium">Daily Limit</p>
              <p className="text-base font-bold text-foreground mt-0.5">
                ₹{limitAmount.toLocaleString("en-IN", { minimumFractionDigits: 2 })}
              </p>
            </div>
            <div className="p-3 rounded-xl bg-rose-500/10 border border-rose-500/20 text-center">
              <p className="text-[11px] text-rose-600 dark:text-rose-400 font-medium">Total Spent</p>
              <p className="text-base font-bold text-rose-600 dark:text-rose-400 mt-0.5">
                ₹{totalSpent.toLocaleString("en-IN", { minimumFractionDigits: 2 })}
              </p>
            </div>
            <div className="p-3 rounded-xl bg-amber-500/10 border border-amber-500/20 text-center">
              <p className="text-[11px] text-amber-600 dark:text-amber-400 font-medium">Exceeded By</p>
              <p className="text-base font-bold text-amber-600 dark:text-amber-400 mt-0.5">
                +₹{exceededAmount.toLocaleString("en-IN", { minimumFractionDigits: 2 })}
              </p>
            </div>
          </div>

          {/* Progress Bar with Percentage */}
          <div className="space-y-2 p-3.5 rounded-xl bg-muted/30 border border-border/50">
            <div className="flex justify-between items-center text-xs">
              <span className="font-medium text-foreground flex items-center gap-1.5">
                <TrendingUp className="w-3.5 h-3.5 text-rose-500" />
                Budget Consumption
              </span>
              <span className="font-bold text-rose-600 dark:text-rose-400">
                {percentageUsed.toFixed(1)}% Used
              </span>
            </div>
            <div className="w-full bg-slate-200 dark:bg-slate-800 rounded-full h-2.5 overflow-hidden">
              <div
                className="h-2.5 rounded-full bg-gradient-to-r from-amber-500 to-rose-500 transition-all duration-500"
                style={{ width: `${Math.min(percentageUsed, 100)}%` }}
              />
            </div>
          </div>

          {/* Advice Notice */}
          <div className="flex items-start gap-2.5 p-3 rounded-lg bg-amber-500/5 border border-amber-500/20 text-xs text-amber-700 dark:text-amber-300">
            <AlertTriangle className="w-4 h-4 shrink-0 mt-0.5 text-amber-600 dark:text-amber-400" />
            <p>
              You can adjust this daily limit or delete it anytime in the Budget Management modal.
            </p>
          </div>
        </div>

        {/* Action Buttons */}
        <DialogFooter className="px-6 py-4 bg-muted/20 border-t border-border/50 flex flex-col sm:flex-row gap-2 sm:justify-end">
          <Button
            type="button"
            variant="outline"
            onClick={() => onOpenChange(false)}
            className="sm:w-auto w-full text-xs"
          >
            Dismiss Alert
          </Button>
          {onOpenBudgetManager && (
            <Button
              type="button"
              onClick={() => {
                onOpenChange(false);
                onOpenBudgetManager();
              }}
              className="sm:w-auto w-full text-xs gap-1.5 bg-gradient-to-r from-blue-600 to-indigo-600 hover:from-blue-700 hover:to-indigo-700 text-white shadow-md shadow-blue-500/20"
            >
              <Sliders className="w-3.5 h-3.5" />
              Adjust Daily Limit
            </Button>
          )}
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}
