"use client";

import { useEffect, useState } from "react";
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogFooter } from "@/components/ui/dialog";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { useForm, Controller } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { incomeFormSchema, IncomeFormValues } from "@/lib/schemas";
import { api, Income } from "@/lib/api";
import { toast } from "sonner";
import { DollarSign, Sparkles } from "lucide-react";

interface IncomeFormModalProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  incomeToEdit?: Income | null;
  onSuccess: () => void;
}

const INCOME_SOURCES = [
  "Salary",
  "Freelance",
  "Investment",
  "Business",
  "Rental",
  "Gift",
  "Other",
];

const PAYMENT_MODES = [
  "Bank Transfer",
  "Cash",
  "UPI",
  "Cheque",
  "Other",
];

export function IncomeFormModal({
  open,
  onOpenChange,
  incomeToEdit,
  onSuccess,
}: IncomeFormModalProps) {
  const [loading, setLoading] = useState(false);

  const getLocalTodayDateString = () => {
    const d = new Date();
    const year = d.getFullYear();
    const month = String(d.getMonth() + 1).padStart(2, "0");
    const day = String(d.getDate()).padStart(2, "0");
    return `${year}-${month}-${day}`;
  };
  const todayDateString = getLocalTodayDateString();

  const {
    register,
    handleSubmit,
    control,
    reset,
    formState: { errors },
  } = useForm<IncomeFormValues>({
    resolver: zodResolver(incomeFormSchema),
    defaultValues: {
      title: "",
      amount: "",
      income_date: todayDateString,
      source: "Salary",
      payment_mode: "Bank Transfer",
      notes: "",
    },
  });

  useEffect(() => {
    if (open) {
      if (incomeToEdit) {
        reset({
          title: incomeToEdit.title,
          amount: incomeToEdit.amount.toString(),
          income_date: incomeToEdit.income_date.split("T")[0],
          source: incomeToEdit.source || "Salary",
          payment_mode: incomeToEdit.payment_mode || undefined,
          notes: incomeToEdit.notes || undefined,
        });
      } else {
        reset({
          title: "",
          amount: "",
          income_date: todayDateString,
          source: "Salary",
          payment_mode: "Bank Transfer",
          notes: "",
        });
      }
    }
  }, [open, incomeToEdit, reset, todayDateString]);

  const onSubmit = async (data: IncomeFormValues) => {
    try {
      setLoading(true);
      if (incomeToEdit) {
        await api.updateIncome(incomeToEdit.id, data);
        toast.success("Income updated successfully");
      } else {
        await api.createIncome(data);
        toast.success("Income recorded successfully");
      }
      onSuccess();
      onOpenChange(false);
    } catch (error: any) {
      toast.error(incomeToEdit ? "Failed to update income" : "Failed to record income", {
        description: error.message,
      });
    } finally {
      setLoading(false);
    }
  };

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="sm:max-w-[440px]">
        <DialogHeader>
          <DialogTitle className="flex items-center gap-2">
            <Sparkles className="w-4 h-4 text-emerald-500" />
            {incomeToEdit ? "Edit Income" : "Add Income"}
          </DialogTitle>
        </DialogHeader>

        <form onSubmit={handleSubmit(onSubmit)} className="space-y-4 pt-2">
          {/* Title */}
          <div className="space-y-1.5">
            <Label htmlFor="title">Title / Description</Label>
            <Input
              id="title"
              {...register("title")}
              placeholder="e.g. Monthly Salary, Freelance Client"
            />
            {errors.title && <p className="text-xs text-rose-500">{errors.title.message}</p>}
          </div>

          {/* Amount & Date */}
          <div className="grid grid-cols-2 gap-3.5">
            <div className="space-y-1.5">
              <Label htmlFor="amount">Amount (₹)</Label>
              <Input
                id="amount"
                type="number"
                step="0.01"
                {...register("amount")}
                placeholder="50000.00"
              />
              {errors.amount && <p className="text-xs text-rose-500">{errors.amount.message}</p>}
            </div>

            <div className="space-y-1.5">
              <Label htmlFor="income_date">Date Received</Label>
              <Input
                id="income_date"
                type="date"
                max={todayDateString}
                {...register("income_date")}
              />
              {errors.income_date && (
                <p className="text-xs text-rose-500">{errors.income_date.message}</p>
              )}
            </div>
          </div>

          {/* Source */}
          <div className="space-y-1.5">
            <Label>Income Source</Label>
            <Controller
              control={control}
              name="source"
              render={({ field }) => (
                <Select onValueChange={field.onChange} value={field.value}>
                  <SelectTrigger>
                    <SelectValue placeholder="Select source" />
                  </SelectTrigger>
                  <SelectContent>
                    {INCOME_SOURCES.map((src) => (
                      <SelectItem key={src} value={src}>
                        {src}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              )}
            />
            {errors.source && <p className="text-xs text-rose-500">{errors.source.message}</p>}
          </div>

          {/* Payment Mode */}
          <div className="space-y-1.5">
            <Label>Payment Mode</Label>
            <Controller
              control={control}
              name="payment_mode"
              render={({ field }) => (
                <Select onValueChange={field.onChange} value={field.value}>
                  <SelectTrigger>
                    <SelectValue placeholder="Select payment mode" />
                  </SelectTrigger>
                  <SelectContent>
                    {PAYMENT_MODES.map((mode) => (
                      <SelectItem key={mode} value={mode}>
                        {mode}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              )}
            />
          </div>

          {/* Notes */}
          <div className="space-y-1.5">
            <Label htmlFor="notes">Notes (Optional)</Label>
            <Input
              id="notes"
              {...register("notes")}
              placeholder="e.g. Q3 bonus or project milestone"
            />
            {errors.notes && <p className="text-xs text-rose-500">{errors.notes.message}</p>}
          </div>

          {/* Footer Actions */}
          <DialogFooter className="pt-2">
            <Button type="button" variant="outline" onClick={() => onOpenChange(false)}>
              Cancel
            </Button>
            <Button
              type="submit"
              disabled={loading}
              className="bg-emerald-600 hover:bg-emerald-700 text-white"
            >
              {loading ? "Saving..." : "Save Income"}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  );
}
