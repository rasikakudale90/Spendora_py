import { z } from "zod";

export const expenseFormSchema = z.object({
  title: z
    .string()
    .min(1, "Title is required")
    .max(50, "Title cannot exceed 50 characters"),
  category_id: z.string().uuid("Please select a valid category"),
  amount: z
    .string()
    .min(1, "Amount is required")
    .refine((val) => !isNaN(parseFloat(val)) && parseFloat(val) > 0, {
      message: "Amount must be greater than 0",
    }),
  expense_date: z
    .string()
    .min(1, "Date is required")
    .refine((val) => new Date(val) <= new Date(new Date().setHours(23, 59, 59, 999)), {
      message: "Expense date cannot be in the future",
    }),
  payment_mode: z.enum(["Cash", "Card", "UPI", "Net Banking", "Other"]).optional(),
  notes: z.string().max(500, "Notes cannot exceed 500 characters").optional(),
});

export type ExpenseFormValues = z.infer<typeof expenseFormSchema>;

export const categoryFormSchema = z.object({
  name: z
    .string()
    .min(1, "Category name is required")
    .max(50, "Category name cannot exceed 50 characters"),
});

export type CategoryFormValues = z.infer<typeof categoryFormSchema>;

export const budgetFormSchema = z
  .object({
    scope: z.enum(["overall", "category"]),
    category_id: z.string().optional().nullable(),
    amount: z
      .string()
      .min(1, "Budget amount is required")
      .refine((val) => !isNaN(parseFloat(val)) && parseFloat(val) > 0, {
        message: "Amount must be greater than 0",
      }),
    period_month: z.string().min(1, "Month is required"),
  })
  .refine(
    (data) => {
      if (data.scope === "category") {
        return !!data.category_id && data.category_id !== "";
      }
      return true;
    },
    {
      message: "Please select a category for category-specific budget",
      path: ["category_id"],
    }
  );

export type BudgetFormValues = z.infer<typeof budgetFormSchema>;
