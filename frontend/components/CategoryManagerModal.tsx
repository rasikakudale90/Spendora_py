import { useState, useEffect } from "react";
import { Dialog, DialogContent, DialogHeader, DialogTitle } from "@/components/ui/dialog";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { api, Category } from "@/lib/api";
import { toast } from "sonner";
import { Trash2, Edit2, Plus, Save, X } from "lucide-react";

interface CategoryManagerModalProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
}

export function CategoryManagerModal({ open, onOpenChange }: CategoryManagerModalProps) {
  const [categories, setCategories] = useState<Category[]>([]);
  const [loading, setLoading] = useState(false);
  const [newCatName, setNewCatName] = useState("");
  const [editingId, setEditingId] = useState<string | null>(null);
  const [editName, setEditName] = useState("");

  const fetchCategories = async () => {
    try {
      const data = await api.getCategories();
      setCategories(data);
    } catch (error) {
      toast.error("Failed to fetch categories");
    }
  };

  useEffect(() => {
    if (open) fetchCategories();
  }, [open]);

  const handleAdd = async () => {
    if (!newCatName.trim()) return;
    try {
      setLoading(true);
      await api.createCategory(newCatName.trim());
      setNewCatName("");
      fetchCategories();
      toast.success("Category added");
    } catch (error: any) {
      toast.error("Failed to add category", { description: error.message });
    } finally {
      setLoading(false);
    }
  };

  const handleEdit = async (id: string) => {
    if (!editName.trim()) return;
    try {
      setLoading(true);
      await api.renameCategory(id, editName.trim());
      setEditingId(null);
      fetchCategories();
      toast.success("Category renamed");
    } catch (error: any) {
      toast.error("Failed to rename category", { description: error.message });
    } finally {
      setLoading(false);
    }
  };

  const handleDelete = async (id: string) => {
    if (confirm("Are you sure you want to delete this category?")) {
      try {
        setLoading(true);
        await api.deleteCategory(id);
        fetchCategories();
        toast.success("Category deleted");
      } catch (error: any) {
        toast.error("Failed to delete category", { description: error.message });
      } finally {
        setLoading(false);
      }
    }
  };

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="sm:max-w-[425px] bg-slate-900 border-white/10 text-white">
        <DialogHeader>
          <DialogTitle>Manage Categories</DialogTitle>
        </DialogHeader>
        
        <div className="space-y-4 pt-4">
          <div className="flex gap-2">
            <Input 
              placeholder="New category name" 
              value={newCatName}
              onChange={(e) => setNewCatName(e.target.value)}
              className="bg-slate-800 border-white/10"
              onKeyDown={(e) => e.key === "Enter" && handleAdd()}
            />
            <Button onClick={handleAdd} disabled={loading || !newCatName.trim()} className="bg-emerald-500 hover:bg-emerald-600">
              <Plus className="w-4 h-4" />
            </Button>
          </div>

          <div className="space-y-2 max-h-[300px] overflow-y-auto pr-2">
            {categories.map((cat) => (
              <div key={cat.id} className="flex items-center justify-between p-2 rounded bg-slate-800/50 border border-white/5">
                {editingId === cat.id ? (
                  <div className="flex items-center gap-2 flex-1 mr-2">
                    <Input 
                      value={editName}
                      onChange={(e) => setEditName(e.target.value)}
                      className="bg-slate-900 h-8 text-sm"
                    />
                    <Button size="icon" variant="ghost" onClick={() => handleEdit(cat.id)} className="h-8 w-8 text-emerald-400">
                      <Save className="h-4 w-4" />
                    </Button>
                    <Button size="icon" variant="ghost" onClick={() => setEditingId(null)} className="h-8 w-8 text-slate-400">
                      <X className="h-4 w-4" />
                    </Button>
                  </div>
                ) : (
                  <>
                    <span className="font-medium text-sm">{cat.name}</span>
                    <div className="flex items-center gap-1">
                      <Button size="icon" variant="ghost" onClick={() => { setEditingId(cat.id); setEditName(cat.name); }} className="h-7 w-7 text-slate-400">
                        <Edit2 className="h-3 w-3" />
                      </Button>
                      <Button size="icon" variant="ghost" onClick={() => handleDelete(cat.id)} className="h-7 w-7 text-red-400 hover:text-red-300 hover:bg-red-900/20">
                        <Trash2 className="h-3 w-3" />
                      </Button>
                    </div>
                  </>
                )}
              </div>
            ))}
          </div>
        </div>
      </DialogContent>
    </Dialog>
  );
}
