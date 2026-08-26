import { useState } from "react";
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogDescription, DialogFooter } from "@/components/ui/dialog";
import { Button } from "@/components/ui/button";
import { api } from "@/lib/api";
import { toast } from "sonner";

interface DeleteConfirmModalProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  expenseId: string | null;
  onSuccess: () => void;
}

export function DeleteConfirmModal({ open, onOpenChange, expenseId, onSuccess }: DeleteConfirmModalProps) {
  const [loading, setLoading] = useState(false);

  const handleDelete = async () => {
    if (!expenseId) return;
    try {
      setLoading(true);
      await api.deleteExpense(expenseId);
      toast.success("Expense deleted successfully");
      onSuccess();
      onOpenChange(false);
    } catch (error: any) {
      toast.error("Failed to delete expense", {
        description: error.message
      });
    } finally {
      setLoading(false);
    }
  };

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="sm:max-w-[425px] bg-slate-900 border-red-500/20 text-white">
        <DialogHeader>
          <DialogTitle className="text-red-400">Delete Expense</DialogTitle>
          <DialogDescription className="text-slate-400">
            Are you sure you want to delete this expense? This action cannot be undone.
          </DialogDescription>
        </DialogHeader>
        <DialogFooter className="pt-4">
          <Button variant="outline" onClick={() => onOpenChange(false)} className="border-white/10 bg-transparent hover:bg-white/5">
            Cancel
          </Button>
          <Button variant="destructive" onClick={handleDelete} disabled={loading} className="bg-red-500 hover:bg-red-600 text-white">
            {loading ? "Deleting..." : "Delete"}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}
