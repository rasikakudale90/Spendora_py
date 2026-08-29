"use client";

import { useEffect, useState } from "react";
import { api, Income, IncomeListResponse, IncomeSummaryResponse } from "@/lib/api";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { Badge } from "@/components/ui/badge";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import {
  Plus,
  Search,
  Edit,
  Trash2,
  ChevronLeft,
  ChevronRight,
  TrendingUp,
  Wallet,
  PiggyBank,
  Sparkles,
} from "lucide-react";
import { toast } from "sonner";
import { IncomeFormModal } from "@/components/income/IncomeFormModal";
import { DeleteIncomeConfirmModal } from "@/components/income/DeleteIncomeConfirmModal";

const SOURCES = [
  "all",
  "Salary",
  "Freelance",
  "Investment",
  "Business",
  "Rental",
  "Gift",
  "Other",
];

export default function IncomePage() {
  const [loading, setLoading] = useState(true);
  const [data, setData] = useState<IncomeListResponse | null>(null);
  const [summary, setSummary] = useState<IncomeSummaryResponse | null>(null);

  // Modals state
  const [isFormOpen, setIsFormOpen] = useState(false);
  const [isDeleteOpen, setIsDeleteOpen] = useState(false);
  const [incomeToEdit, setIncomeToEdit] = useState<Income | null>(null);
  const [incomeToDelete, setIncomeToDelete] = useState<string | null>(null);

  // Filters state
  const [page, setPage] = useState(1);
  const [search, setSearch] = useState("");
  const [sourceFilter, setSourceFilter] = useState("all");
  const [sortBy, setSortBy] = useState("income_date");
  const [sortOrder, setSortOrder] = useState<"asc" | "desc">("desc");

  const fetchData = async () => {
    try {
      setLoading(true);
      const params: any = { page, page_size: 10 };
      if (search) params.search = search;
      if (sourceFilter !== "all") params.source = sourceFilter;
      params.sort_by = sortBy;
      params.sort_order = sortOrder;

      const [listRes, sumRes] = await Promise.all([
        api.getIncomes(params),
        api.getIncomeSummary(),
      ]);

      setData(listRes);
      setSummary(sumRes);
    } catch (error: any) {
      toast.error("Failed to load incomes", { description: error.message });
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchData();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [page, search, sourceFilter, sortBy, sortOrder]);

  const handleEdit = (income: Income) => {
    setIncomeToEdit(income);
    setIsFormOpen(true);
  };

  const handleDelete = (id: string) => {
    setIncomeToDelete(id);
    setIsDeleteOpen(true);
  };

  const openNewForm = () => {
    setIncomeToEdit(null);
    setIsFormOpen(true);
  };

  const getSourceBadgeColor = (source: string) => {
    switch (source.toLowerCase()) {
      case "salary":
        return "bg-emerald-500/10 text-emerald-600 dark:text-emerald-400 border-emerald-500/30";
      case "freelance":
        return "bg-blue-500/10 text-blue-600 dark:text-blue-400 border-blue-500/30";
      case "investment":
        return "bg-purple-500/10 text-purple-600 dark:text-purple-400 border-purple-500/30";
      case "business":
        return "bg-amber-500/10 text-amber-600 dark:text-amber-400 border-amber-500/30";
      case "rental":
        return "bg-teal-500/10 text-teal-600 dark:text-teal-400 border-teal-500/30";
      default:
        return "bg-slate-500/10 text-slate-600 dark:text-slate-400 border-slate-500/30";
    }
  };

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl sm:text-3xl font-bold bg-gradient-to-r from-emerald-600 via-teal-500 to-cyan-500 bg-clip-text text-transparent">
            Income Streams
          </h1>
          <p className="text-sm text-muted-foreground mt-1">
            Track salaries, dividends, freelance projects, and earnings
          </p>
        </div>
        <Button
          onClick={openNewForm}
          className="gap-2 bg-emerald-600 hover:bg-emerald-700 text-white shadow-md shadow-emerald-600/20"
        >
          <Plus className="w-4 h-4" /> Add Income
        </Button>
      </div>

      {/* KPI Cards */}
      <div className="grid gap-4 md:grid-cols-3">
        <Card className="glass-card glass-card-hover">
          <CardHeader className="flex flex-row items-center justify-between pb-2">
            <CardTitle className="text-sm font-medium text-muted-foreground">
              Total Income This Month
            </CardTitle>
            <div className="w-8 h-8 rounded-lg bg-emerald-500/10 flex items-center justify-center">
              <TrendingUp className="h-4 w-4 text-emerald-600 dark:text-emerald-400" />
            </div>
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold text-emerald-600 dark:text-emerald-400">
              ₹
              {summary?.total_income
                ? parseFloat(summary.total_income).toLocaleString("en-IN", {
                    minimumFractionDigits: 2,
                  })
                : "0.00"}
            </div>
            <p className="text-xs text-muted-foreground mt-1">
              {summary?.income_count || 0} earnings recorded this month
            </p>
          </CardContent>
        </Card>

        <Card className="glass-card glass-card-hover">
          <CardHeader className="flex flex-row items-center justify-between pb-2">
            <CardTitle className="text-sm font-medium text-muted-foreground">
              Top Income Source
            </CardTitle>
            <div className="w-8 h-8 rounded-lg bg-blue-500/10 flex items-center justify-center">
              <Wallet className="h-4 w-4 text-blue-600 dark:text-blue-400" />
            </div>
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold text-foreground">
              {summary?.breakdown_by_source?.[0]?.source || "None"}
            </div>
            <p className="text-xs text-muted-foreground mt-1">
              {summary?.breakdown_by_source?.[0]
                ? `₹${parseFloat(summary.breakdown_by_source[0].total_amount).toLocaleString(
                    "en-IN"
                  )} (${summary.breakdown_by_source[0].percentage}%)`
                : "No earnings recorded yet"}
            </p>
          </CardContent>
        </Card>

        <Card className="glass-card glass-card-hover">
          <CardHeader className="flex flex-row items-center justify-between pb-2">
            <CardTitle className="text-sm font-medium text-muted-foreground">
              Total Entries
            </CardTitle>
            <div className="w-8 h-8 rounded-lg bg-teal-500/10 flex items-center justify-center">
              <PiggyBank className="h-4 w-4 text-teal-600 dark:text-teal-400" />
            </div>
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold text-foreground">
              {data?.total || 0}
            </div>
            <p className="text-xs text-muted-foreground mt-1">
              Lifetime income transactions
            </p>
          </CardContent>
        </Card>
      </div>

      {/* Filter and Search Bar */}
      <div className="flex flex-col md:flex-row gap-4 items-center p-4 rounded-2xl glass-card">
        <div className="relative w-full md:w-96">
          <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground" />
          <Input
            placeholder="Search incomes or notes..."
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            className="pl-9"
          />
        </div>

        <div className="flex flex-wrap items-center gap-3 w-full md:w-auto md:ml-auto">
          {/* Source Filter */}
          <Select value={sourceFilter} onValueChange={(val) => setSourceFilter(val)}>
            <SelectTrigger className="w-full sm:w-[150px]">
              <SelectValue placeholder="Source" />
            </SelectTrigger>
            <SelectContent>
              {SOURCES.map((s) => (
                <SelectItem key={s} value={s}>
                  {s === "all" ? "All Sources" : s}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>

          {/* Sort By */}
          <Select value={sortBy} onValueChange={(val) => setSortBy(val)}>
            <SelectTrigger className="w-full sm:w-[150px]">
              <SelectValue placeholder="Sort By" />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="income_date">Date</SelectItem>
              <SelectItem value="amount">Amount</SelectItem>
              <SelectItem value="title">Title</SelectItem>
              <SelectItem value="source">Source</SelectItem>
            </SelectContent>
          </Select>

          {/* Sort Order */}
          <Select
            value={sortOrder}
            onValueChange={(val: "asc" | "desc") => setSortOrder(val)}
          >
            <SelectTrigger className="w-full sm:w-[130px]">
              <SelectValue placeholder="Order" />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="desc">Newest / High</SelectItem>
              <SelectItem value="asc">Oldest / Low</SelectItem>
            </SelectContent>
          </Select>
        </div>
      </div>

      {/* Incomes Table */}
      <div className="rounded-2xl glass-card overflow-hidden">
        <Table>
          <TableHeader>
            <TableRow className="hover:bg-transparent border-border/50">
              <TableHead>Title & Remarks</TableHead>
              <TableHead>Source</TableHead>
              <TableHead>Date</TableHead>
              <TableHead>Payment Mode</TableHead>
              <TableHead className="text-right">Amount</TableHead>
              <TableHead className="w-[100px] text-right">Actions</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {loading ? (
              <TableRow>
                <TableCell colSpan={6} className="h-32 text-center text-muted-foreground text-sm">
                  Loading income records...
                </TableCell>
              </TableRow>
            ) : !data?.items || data.items.length === 0 ? (
              <TableRow>
                <TableCell colSpan={6} className="h-40 text-center">
                  <div className="flex flex-col items-center justify-center space-y-2">
                    <Sparkles className="w-8 h-8 text-emerald-500/40" />
                    <p className="font-medium text-foreground text-sm">No income records found</p>
                    <p className="text-xs text-muted-foreground">
                      Click &quot;Add Income&quot; to record your first income stream.
                    </p>
                  </div>
                </TableCell>
              </TableRow>
            ) : (
              data.items.map((income) => (
                <TableRow key={income.id} className="border-border/50 hover:bg-muted/40">
                  <TableCell>
                    <p className="font-semibold text-sm text-foreground">{income.title}</p>
                    {income.notes && (
                      <p className="text-xs text-muted-foreground mt-0.5 line-clamp-1">
                        {income.notes}
                      </p>
                    )}
                  </TableCell>
                  <TableCell>
                    <Badge variant="outline" className={getSourceBadgeColor(income.source)}>
                      {income.source}
                    </Badge>
                  </TableCell>
                  <TableCell className="text-xs text-muted-foreground">
                    {new Intl.DateTimeFormat("en-IN", {
                      month: "short",
                      day: "2-digit",
                      year: "numeric",
                    }).format(new Date(income.income_date))}
                  </TableCell>
                  <TableCell>
                    {income.payment_mode ? (
                      <Badge variant="secondary" className="text-xs font-normal">
                        {income.payment_mode}
                      </Badge>
                    ) : (
                      <span className="text-xs text-muted-foreground">—</span>
                    )}
                  </TableCell>
                  <TableCell className="text-right font-bold text-emerald-600 dark:text-emerald-400 text-sm">
                    +₹
                    {parseFloat(income.amount).toLocaleString("en-IN", {
                      minimumFractionDigits: 2,
                    })}
                  </TableCell>
                  <TableCell className="text-right">
                    <div className="flex items-center justify-end gap-1">
                      <Button
                        variant="ghost"
                        size="icon"
                        onClick={() => handleEdit(income)}
                        className="h-8 w-8 text-muted-foreground hover:text-foreground"
                      >
                        <Edit className="h-3.5 w-3.5" />
                      </Button>
                      <Button
                        variant="ghost"
                        size="icon"
                        onClick={() => handleDelete(income.id)}
                        className="h-8 w-8 text-muted-foreground hover:text-rose-500"
                      >
                        <Trash2 className="h-3.5 w-3.5" />
                      </Button>
                    </div>
                  </TableCell>
                </TableRow>
              ))
            )}
          </TableBody>
        </Table>

        {/* Pagination */}
        {data && data.total_pages > 1 && (
          <div className="flex items-center justify-between px-6 py-4 border-t border-border/50">
            <p className="text-xs text-muted-foreground">
              Showing page <span className="font-semibold text-foreground">{page}</span> of{" "}
              <span className="font-semibold text-foreground">{data.total_pages}</span>
            </p>
            <div className="flex items-center gap-2">
              <Button
                variant="outline"
                size="sm"
                onClick={() => setPage((p) => Math.max(1, p - 1))}
                disabled={page === 1}
              >
                <ChevronLeft className="h-4 w-4" />
              </Button>
              <Button
                variant="outline"
                size="sm"
                onClick={() => setPage((p) => Math.min(data.total_pages, p + 1))}
                disabled={page === data.total_pages}
              >
                <ChevronRight className="h-4 w-4" />
              </Button>
            </div>
          </div>
        )}
      </div>

      {/* Modals */}
      <IncomeFormModal
        open={isFormOpen}
        onOpenChange={setIsFormOpen}
        incomeToEdit={incomeToEdit}
        onSuccess={fetchData}
      />

      <DeleteIncomeConfirmModal
        open={isDeleteOpen}
        onOpenChange={setIsDeleteOpen}
        incomeId={incomeToDelete}
        onSuccess={fetchData}
      />
    </div>
  );
}
