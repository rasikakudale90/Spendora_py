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
      <DialogContent className="sm:max-w-[425px]">
        <DialogHeader>
          <DialogTitle className="text-destructive">Delete Expense</DialogTitle>
          <DialogDescription>
            Are you sure you want to delete this expense? This action cannot be undone.
          </DialogDescription>
        </DialogHeader>
        <DialogFooter className="pt-2">
          <Button variant="outline" onClick={() => onOpenChange(false)}>
            Cancel
          </Button>
          <Button variant="destructive" onClick={handleDelete} disabled={loading}>
            {loading ? "Deleting..." : "Delete"}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}
