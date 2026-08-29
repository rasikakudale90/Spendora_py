"use client";

import { useState, useEffect } from "react";
import { ExpensesTableSkeleton } from "@/components/LoadingSkeleton";
import { api, Expense, Category, ExpenseListResponse } from "@/lib/api";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { Badge } from "@/components/ui/badge";
import { Plus, Search, Filter, Edit, Trash2, ChevronLeft, ChevronRight } from "lucide-react";
import { toast } from "sonner";
import { ExpenseFormModal } from "@/components/expenses/ExpenseFormModal";
import { DeleteConfirmModal } from "@/components/expenses/DeleteConfirmModal";
import { DailyLimitAlertModal } from "@/components/DailyLimitAlertModal";
import { BudgetManagerModal } from "@/components/BudgetManagerModal";
import { DailyBudgetAlert } from "@/lib/api";

export default function ExpensesPage() {
  const [loading, setLoading] = useState(true);
  const [data, setData] = useState<ExpenseListResponse | null>(null);
  const [categories, setCategories] = useState<Category[]>([]);
  
  // Modals state
  const [isFormOpen, setIsFormOpen] = useState(false);
  const [isDeleteOpen, setIsDeleteOpen] = useState(false);
  const [expenseToEdit, setExpenseToEdit] = useState<Expense | null>(null);
  const [expenseToDelete, setExpenseToDelete] = useState<string | null>(null);

  // Daily Limit Alert state
  const [dailyAlert, setDailyAlert] = useState<DailyBudgetAlert | null>(null);
  const [isAlertOpen, setIsAlertOpen] = useState(false);
  const [alertDate, setAlertDate] = useState<string>("");
  const [isBudgetModalOpen, setIsBudgetModalOpen] = useState(false);

  // Filters state
  const [page, setPage] = useState(1);
  const [search, setSearch] = useState("");
  const [categoryId, setCategoryId] = useState("all");
  const [sortBy, setSortBy] = useState("expense_date");
  const [sortOrder, setSortOrder] = useState<"asc" | "desc">("desc");

  const fetchExpenses = async () => {
    try {
      setLoading(true);
      const params: any = { page, page_size: 10 };
      if (search) params.search = search;
      if (categoryId !== "all") params.category_id = categoryId;
      params.sort_by = sortBy;
      params.sort_order = sortOrder;

      const res = await api.getExpenses(params);
      setData(res);
    } catch (error: any) {
      toast.error("Failed to load expenses", { description: error.message });
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    api.getCategories().then(setCategories).catch(console.error);
  }, []);

  useEffect(() => {
    fetchExpenses();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [page, search, categoryId, sortBy, sortOrder]);

  const handleEdit = (expense: Expense) => {
    setExpenseToEdit(expense);
    setIsFormOpen(true);
  };

  const handleDelete = (id: string) => {
    setExpenseToDelete(id);
    setIsDeleteOpen(true);
  };

  const openNewForm = () => {
    setExpenseToEdit(null);
    setIsFormOpen(true);
  };

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl sm:text-3xl font-bold bg-gradient-to-r from-slate-900 via-slate-700 to-slate-500 dark:from-white dark:via-slate-200 dark:to-slate-400 bg-clip-text text-transparent">
            Expenses
          </h1>
          <p className="text-sm text-muted-foreground mt-1">
            Manage your daily transactions and outgoings
          </p>
        </div>
        <Button onClick={openNewForm} className="gap-2">
          <Plus className="w-4 h-4" /> Add Expense
        </Button>
      </div>

      <div className="flex flex-col md:flex-row gap-4 items-center p-4 rounded-2xl glass-card">
        <div className="relative w-full md:w-96">
          <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground" />
          <Input 
            placeholder="Search expenses..." 
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            className="pl-9"
          />
        </div>
        <div className="flex gap-2.5 w-full md:w-auto overflow-x-auto">
          <Select value={categoryId} onValueChange={(v) => { setCategoryId(v); setPage(1); }}>
            <SelectTrigger className="w-[160px]">
              <Filter className="w-4 h-4 mr-2 text-muted-foreground" />
              <SelectValue placeholder="Category" />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="all">All Categories</SelectItem>
              {categories.map(c => <SelectItem key={c.id} value={c.id}>{c.name}</SelectItem>)}
            </SelectContent>
          </Select>

          <Select value={`${sortBy}-${sortOrder}`} onValueChange={(v) => {
            const [by, order] = v.split('-');
            setSortBy(by);
            setSortOrder(order as "asc" | "desc");
            setPage(1);
          }}>
            <SelectTrigger className="w-[170px]">
              <SelectValue placeholder="Sort by" />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="expense_date-desc">Date (Newest)</SelectItem>
              <SelectItem value="expense_date-asc">Date (Oldest)</SelectItem>
              <SelectItem value="amount-desc">Amount (Highest)</SelectItem>
              <SelectItem value="amount-asc">Amount (Lowest)</SelectItem>
            </SelectContent>
          </Select>
        </div>
      </div>

      {loading ? (
        <ExpensesTableSkeleton />
      ) : (
        <div className="rounded-2xl glass-card overflow-hidden">
          <Table>
            <TableHeader className="bg-muted/40">
              <TableRow className="border-border/60 hover:bg-transparent">
                <TableHead className="font-semibold text-muted-foreground">Date</TableHead>
                <TableHead className="font-semibold text-muted-foreground">Title</TableHead>
                <TableHead className="font-semibold text-muted-foreground">Category</TableHead>
                <TableHead className="font-semibold text-muted-foreground">Mode</TableHead>
                <TableHead className="text-right font-semibold text-muted-foreground">Amount</TableHead>
                <TableHead className="text-right font-semibold text-muted-foreground">Actions</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {data?.items.length === 0 ? (
                <TableRow>
                  <TableCell colSpan={6} className="text-center py-12 text-muted-foreground">
                    No expenses found matching your filters.
                  </TableCell>
                </TableRow>
              ) : (
                data?.items.map((expense) => (
                  <TableRow key={expense.id} className="border-border/40 hover:bg-muted/30 transition-colors">
                    <TableCell className="whitespace-nowrap text-muted-foreground">
                      {new Intl.DateTimeFormat('en-IN', { month: 'short', day: '2-digit', year: 'numeric' }).format(new Date(expense.expense_date))}
                    </TableCell>
                    <TableCell className="font-medium text-foreground">{expense.title}</TableCell>
                    <TableCell>
                      <Badge variant="outline">{expense.category?.name || "Uncategorized"}</Badge>
                    </TableCell>
                    <TableCell>
                      {expense.payment_mode && <span className="text-xs text-muted-foreground font-medium">{expense.payment_mode}</span>}
                    </TableCell>
                    <TableCell className="text-right font-bold text-foreground">
                      ₹{parseFloat(expense.amount).toLocaleString('en-IN', { minimumFractionDigits: 2 })}
                    </TableCell>
                    <TableCell className="text-right">
                      <div className="flex justify-end gap-1">
                        <Button variant="ghost" size="icon" onClick={() => handleEdit(expense)} className="h-8 w-8 text-muted-foreground hover:text-foreground">
                          <Edit className="h-4 w-4" />
                        </Button>
                        <Button variant="ghost" size="icon" onClick={() => handleDelete(expense.id)} className="h-8 w-8 text-muted-foreground hover:text-rose-500">
                          <Trash2 className="h-4 w-4" />
                        </Button>
                      </div>
                    </TableCell>
                  </TableRow>
                ))
              )}
            </TableBody>
          </Table>

          {data && data.total_pages > 1 && (
            <div className="flex items-center justify-between px-4 py-3 border-t border-border/60 bg-muted/20">
              <div className="text-sm text-muted-foreground">
                Showing page {data.page} of {data.total_pages} ({data.total} total)
              </div>
              <div className="flex gap-2">
                <Button 
                  variant="outline" 
                  size="sm" 
                  onClick={() => setPage(p => Math.max(1, p - 1))}
                  disabled={page === 1}
                >
                  <ChevronLeft className="h-4 w-4" />
                </Button>
                <Button 
                  variant="outline" 
                  size="sm" 
                  onClick={() => setPage(p => Math.min(data.total_pages, p + 1))}
                  disabled={page === data.total_pages}
                >
                  <ChevronRight className="h-4 w-4" />
                </Button>
              </div>
            </div>
          )}
        </div>
      )}

      <ExpenseFormModal 
        open={isFormOpen} 
        onOpenChange={setIsFormOpen} 
        expenseToEdit={expenseToEdit}
        onSuccess={fetchExpenses}
        onDailyLimitBreached={(alert, date) => {
          setDailyAlert(alert);
          setAlertDate(date);
          setIsAlertOpen(true);
        }}
      />

      <DeleteConfirmModal 
        open={isDeleteOpen}
        onOpenChange={setIsDeleteOpen}
        expenseId={expenseToDelete}
        onSuccess={fetchExpenses}
      />

      <DailyLimitAlertModal
        open={isAlertOpen}
        onOpenChange={setIsAlertOpen}
        alertData={dailyAlert}
        targetDate={alertDate}
        onOpenBudgetManager={() => setIsBudgetModalOpen(true)}
      />

      <BudgetManagerModal
        open={isBudgetModalOpen}
        onOpenChange={setIsBudgetModalOpen}
        initialPeriod="daily"
        onSuccess={fetchExpenses}
      />
    </div>
  );
}
