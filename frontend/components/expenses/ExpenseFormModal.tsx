import { useState, useEffect } from "react";
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogFooter } from "@/components/ui/dialog";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { useForm, Controller } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { expenseFormSchema, ExpenseFormValues } from "@/lib/schemas";
import { api, Category, Expense, DailyBudgetAlert } from "@/lib/api";
import { toast } from "sonner";

interface ExpenseFormModalProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  expenseToEdit?: Expense | null;
  onSuccess: () => void;
  onDailyLimitBreached?: (alert: DailyBudgetAlert, date: string) => void;
}

export function ExpenseFormModal({ open, onOpenChange, expenseToEdit, onSuccess, onDailyLimitBreached }: ExpenseFormModalProps) {
  const [categories, setCategories] = useState<Category[]>([]);
  const [loading, setLoading] = useState(false);

  const getLocalTodayDateString = () => {
    const d = new Date();
    const year = d.getFullYear();
    const month = String(d.getMonth() + 1).padStart(2, "0");
    const day = String(d.getDate()).padStart(2, "0");
    return `${year}-${month}-${day}`;
  };
  const todayDateString = getLocalTodayDateString();

  const { register, handleSubmit, control, reset, formState: { errors } } = useForm<ExpenseFormValues>({
    resolver: zodResolver(expenseFormSchema),
    defaultValues: {
      title: "",
      amount: "",
      category_id: "",
      expense_date: todayDateString,
      payment_mode: "Card",
      notes: ""
    }
  });

  useEffect(() => {
    if (open) {
      api.getCategories().then(setCategories).catch(console.error);
      if (expenseToEdit) {
        reset({
          title: expenseToEdit.title,
          amount: expenseToEdit.amount.toString(),
          category_id: expenseToEdit.category_id,
          expense_date: expenseToEdit.expense_date.split("T")[0],
          payment_mode: expenseToEdit.payment_mode || undefined,
          notes: expenseToEdit.notes || undefined
        });
      } else {
        reset({
          title: "",
          amount: "",
          category_id: "",
          expense_date: todayDateString,
          payment_mode: "Card",
          notes: ""
        });
      }
    }
  }, [open, expenseToEdit, reset, todayDateString]);

  const onSubmit = async (data: ExpenseFormValues) => {
    try {
      setLoading(true);
      let res: Expense;
      if (expenseToEdit) {
        res = await api.updateExpense(expenseToEdit.id, data);
        toast.success("Expense updated successfully");
      } else {
        res = await api.createExpense(data);
        toast.success("Expense created successfully");
      }
      onSuccess();
      onOpenChange(false);
      if (res?.daily_budget_alert?.exceeded && onDailyLimitBreached) {
        onDailyLimitBreached(res.daily_budget_alert, data.expense_date);
      }
    } catch (error: any) {
      toast.error(expenseToEdit ? "Failed to update expense" : "Failed to create expense", {
        description: error.message
      });
    } finally {
      setLoading(false);
    }
  };

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="sm:max-w-[440px]">
        <DialogHeader>
          <DialogTitle>{expenseToEdit ? "Edit Expense" : "Add Expense"}</DialogTitle>
        </DialogHeader>
        <form onSubmit={handleSubmit(onSubmit)} className="space-y-4 pt-2">
          <div className="space-y-1.5">
            <Label htmlFor="title">Title</Label>
            <Input id="title" {...register("title")} placeholder="e.g. Grocery run" />
            {errors.title && <p className="text-xs text-rose-500">{errors.title.message}</p>}
          </div>
          
          <div className="grid grid-cols-2 gap-3.5">
            <div className="space-y-1.5">
              <Label htmlFor="amount">Amount (₹)</Label>
              <Input id="amount" type="number" step="0.01" {...register("amount")} placeholder="100.00" />
              {errors.amount && <p className="text-xs text-rose-500">{errors.amount.message}</p>}
            </div>
            
            <div className="space-y-1.5">
              <Label htmlFor="expense_date">Date</Label>
              <Input id="expense_date" type="date" max={todayDateString} {...register("expense_date")} />
              {errors.expense_date && <p className="text-xs text-rose-500">{errors.expense_date.message}</p>}
            </div>
          </div>

          <div className="space-y-1.5">
            <Label>Category</Label>
            <Controller
              control={control}
              name="category_id"
              render={({ field }) => (
                <Select onValueChange={field.onChange} value={field.value}>
                  <SelectTrigger>
                    <SelectValue placeholder="Select a category" />
                  </SelectTrigger>
                  <SelectContent>
                    {categories.map((c) => (
                      <SelectItem key={c.id} value={c.id}>{c.name}</SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              )}
            />
            {errors.category_id && <p className="text-xs text-rose-500">{errors.category_id.message}</p>}
          </div>

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
                    <SelectItem value="Cash">Cash</SelectItem>
                    <SelectItem value="Card">Card</SelectItem>
                    <SelectItem value="UPI">UPI</SelectItem>
                    <SelectItem value="Net Banking">Net Banking</SelectItem>
                    <SelectItem value="Other">Other</SelectItem>
                  </SelectContent>
                </Select>
              )}
            />
          </div>

          <div className="space-y-1.5">
            <Label htmlFor="notes">Notes (Optional)</Label>
            <Input id="notes" {...register("notes")} placeholder="Optional details..." />
            {errors.notes && <p className="text-xs text-rose-500">{errors.notes.message}</p>}
          </div>

          <DialogFooter className="pt-2">
            <Button type="button" variant="outline" onClick={() => onOpenChange(false)}>
              Cancel
            </Button>
            <Button type="submit" disabled={loading}>
              {loading ? "Saving..." : "Save Expense"}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  );
}
