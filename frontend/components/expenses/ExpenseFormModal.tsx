import { useState, useEffect } from "react";
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogFooter } from "@/components/ui/dialog";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { useForm, Controller } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { expenseFormSchema, ExpenseFormValues } from "@/lib/schemas";
import { api, Category, PaymentMode, Expense } from "@/lib/api";
import { toast } from "sonner";

interface ExpenseFormModalProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  expenseToEdit?: Expense | null;
  onSuccess: () => void;
}

export function ExpenseFormModal({ open, onOpenChange, expenseToEdit, onSuccess }: ExpenseFormModalProps) {
  const [categories, setCategories] = useState<Category[]>([]);
  const [loading, setLoading] = useState(false);

  const { register, handleSubmit, control, reset, formState: { errors } } = useForm<ExpenseFormValues>({
    resolver: zodResolver(expenseFormSchema),
    defaultValues: {
      title: "",
      amount: "",
      category_id: "",
      expense_date: new Date().toISOString().split("T")[0],
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
          expense_date: new Date().toISOString().split("T")[0],
          payment_mode: "Card",
          notes: ""
        });
      }
    }
  }, [open, expenseToEdit, reset]);

  const onSubmit = async (data: ExpenseFormValues) => {
    try {
      setLoading(true);
      if (expenseToEdit) {
        await api.updateExpense(expenseToEdit.id, data);
        toast.success("Expense updated successfully");
      } else {
        await api.createExpense(data);
        toast.success("Expense created successfully");
      }
      onSuccess();
      onOpenChange(false);
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
      <DialogContent className="sm:max-w-[425px] bg-slate-900 border-white/10 text-white">
        <DialogHeader>
          <DialogTitle>{expenseToEdit ? "Edit Expense" : "Add Expense"}</DialogTitle>
        </DialogHeader>
        <form onSubmit={handleSubmit(onSubmit)} className="space-y-4 pt-4">
          <div className="space-y-2">
            <Label htmlFor="title">Title</Label>
            <Input id="title" {...register("title")} className="bg-slate-800 border-white/10" placeholder="Grocery run" />
            {errors.title && <p className="text-xs text-red-400">{errors.title.message}</p>}
          </div>
          
          <div className="grid grid-cols-2 gap-4">
            <div className="space-y-2">
              <Label htmlFor="amount">Amount</Label>
              <Input id="amount" type="number" step="0.01" {...register("amount")} className="bg-slate-800 border-white/10" placeholder="100.00" />
              {errors.amount && <p className="text-xs text-red-400">{errors.amount.message}</p>}
            </div>
            
            <div className="space-y-2">
              <Label htmlFor="expense_date">Date</Label>
              <Input id="expense_date" type="date" {...register("expense_date")} className="bg-slate-800 border-white/10 [color-scheme:dark]" />
              {errors.expense_date && <p className="text-xs text-red-400">{errors.expense_date.message}</p>}
            </div>
          </div>

          <div className="space-y-2">
            <Label>Category</Label>
            <Controller
              control={control}
              name="category_id"
              render={({ field }) => (
                <Select onValueChange={field.onChange} value={field.value}>
                  <SelectTrigger className="bg-slate-800 border-white/10">
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
            {errors.category_id && <p className="text-xs text-red-400">{errors.category_id.message}</p>}
          </div>

          <div className="space-y-2">
            <Label>Payment Mode</Label>
            <Controller
              control={control}
              name="payment_mode"
              render={({ field }) => (
                <Select onValueChange={field.onChange} value={field.value}>
                  <SelectTrigger className="bg-slate-800 border-white/10">
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

          <div className="space-y-2">
            <Label htmlFor="notes">Notes (Optional)</Label>
            <Input id="notes" {...register("notes")} className="bg-slate-800 border-white/10" placeholder="Optional details..." />
            {errors.notes && <p className="text-xs text-red-400">{errors.notes.message}</p>}
          </div>

          <DialogFooter className="pt-4">
            <Button type="button" variant="outline" onClick={() => onOpenChange(false)} className="border-white/10 bg-transparent hover:bg-white/5">
              Cancel
            </Button>
            <Button type="submit" disabled={loading} className="bg-emerald-500 hover:bg-emerald-600 text-white">
              {loading ? "Saving..." : "Save Expense"}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  );
}
