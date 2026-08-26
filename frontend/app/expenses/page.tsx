"use client";

import { Card, CardHeader, CardTitle, CardDescription, CardContent } from "@/components/ui/card";
import { ExpensesTableSkeleton } from "@/components/LoadingSkeleton";

export default function ExpensesPage() {
  return (
    <div className="space-y-8">
      <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl sm:text-3xl font-bold bg-gradient-to-r from-white via-slate-200 to-slate-400 bg-clip-text text-transparent">
            Expenses Management
          </h1>
          <p className="text-sm text-muted-foreground mt-1">
            Record, filter, search, and manage your day-to-day transactions
          </p>
        </div>
      </div>
      <ExpensesTableSkeleton />
    </div>
  );
}
