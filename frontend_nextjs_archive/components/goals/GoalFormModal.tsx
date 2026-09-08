"use client";

import React, { useEffect } from "react";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { toast } from "sonner";
import { X, Target, Calendar, DollarSign, Tag, Palette, FileText } from "lucide-react";
import { goalsApi, Goal } from "@/lib/api";
import { goalFormSchema, GoalFormValues } from "@/lib/schemas";

interface GoalFormModalProps {
  isOpen: boolean;
  onClose: () => void;
  onSuccess: () => void;
  goalToEdit?: Goal | null;
}

const CATEGORIES = [
  "Savings",
  "Emergency Fund",
  "Travel",
  "Gadget",
  "Vehicle",
  "Home",
  "Education",
  "Investment",
  "Other",
];

const COLORS = [
  { label: "Emerald", value: "emerald", bg: "bg-emerald-500" },
  { label: "Teal", value: "teal", bg: "bg-teal-500" },
  { label: "Cyan", value: "cyan", bg: "bg-cyan-500" },
  { label: "Violet", value: "violet", bg: "bg-violet-500" },
  { label: "Rose", value: "rose", bg: "bg-rose-500" },
  { label: "Amber", value: "amber", bg: "bg-amber-500" },
];

export const GoalFormModal: React.FC<GoalFormModalProps> = ({
  isOpen,
  onClose,
  onSuccess,
  goalToEdit,
}) => {
  const isEditing = !!goalToEdit;

  const {
    register,
    handleSubmit,
    reset,
    watch,
    setValue,
    formState: { errors, isSubmitting },
  } = useForm<GoalFormValues>({
    resolver: zodResolver(goalFormSchema),
    defaultValues: {
      name: "",
      target_amount: "",
      current_amount: "0",
      target_date: "",
      category: "Savings",
      color: "emerald",
      notes: "",
    },
  });

  const selectedColor = watch("color") || "emerald";

  useEffect(() => {
    if (goalToEdit) {
      reset({
        name: goalToEdit.name,
        target_amount: String(goalToEdit.target_amount),
        current_amount: String(goalToEdit.current_amount),
        target_date: goalToEdit.target_date || "",
        category: goalToEdit.category || "Savings",
        color: goalToEdit.color || "emerald",
        notes: goalToEdit.notes || "",
      });
    } else {
      reset({
        name: "",
        target_amount: "",
        current_amount: "0",
        target_date: "",
        category: "Savings",
        color: "emerald",
        notes: "",
      });
    }
  }, [goalToEdit, reset, isOpen]);

  if (!isOpen) return null;

  const onSubmit = async (values: GoalFormValues) => {
    try {
      if (isEditing && goalToEdit) {
        await goalsApi.update(goalToEdit.id, {
          name: values.name,
          target_amount: values.target_amount,
          current_amount: values.current_amount || "0",
          target_date: values.target_date || null,
          category: values.category,
          color: values.color,
          notes: values.notes || null,
        });
        toast.success("Goal updated successfully!");
      } else {
        await goalsApi.create({
          name: values.name,
          target_amount: values.target_amount,
          current_amount: values.current_amount || "0",
          target_date: values.target_date || null,
          category: values.category,
          color: values.color,
          notes: values.notes || null,
        });
        toast.success("Goal created successfully!");
      }
      onSuccess();
      onClose();
    } catch (err: any) {
      toast.error(err.message || "Failed to save goal.");
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-background/80 backdrop-blur-sm animate-in fade-in duration-200">
      <div
        className="relative w-full max-w-lg rounded-2xl border border-border/80 bg-card p-6 shadow-2xl space-y-5"
        onClick={(e) => e.stopPropagation()}
      >
        {/* Header */}
        <div className="flex items-center justify-between pb-3 border-b border-border/60">
          <div className="flex items-center gap-2.5">
            <div className="w-9 h-9 rounded-xl bg-primary/10 text-primary flex items-center justify-center">
              <Target className="w-5 h-5" />
            </div>
            <div>
              <h2 className="text-lg font-bold text-foreground">
                {isEditing ? "Edit Financial Goal" : "Create New Goal"}
              </h2>
              <p className="text-xs text-muted-foreground">
                Set a target savings balance and deadline with AI runway pacing
              </p>
            </div>
          </div>
          <button
            onClick={onClose}
            className="p-1.5 rounded-lg text-muted-foreground hover:text-foreground hover:bg-muted/60 transition-colors"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Form */}
        <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
          {/* Goal Name */}
          <div className="space-y-1.5">
            <label className="text-xs font-semibold text-foreground flex items-center gap-1.5">
              <Target className="w-3.5 h-3.5 text-muted-foreground" />
              <span>Goal Name</span>
            </label>
            <input
              {...register("name")}
              type="text"
              placeholder="e.g. Japan Autumn Trip, MacBook Pro M4"
              className="w-full px-3.5 py-2 text-sm rounded-xl border border-border/80 bg-muted/20 text-foreground placeholder:text-muted-foreground/60 focus:outline-none focus:ring-2 focus:ring-primary/40"
            />
            {errors.name && (
              <p className="text-xs text-rose-500">{errors.name.message}</p>
            )}
          </div>

          {/* Target & Initial Amount */}
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-3.5">
            <div className="space-y-1.5">
              <label className="text-xs font-semibold text-foreground flex items-center gap-1.5">
                <DollarSign className="w-3.5 h-3.5 text-muted-foreground" />
                <span>Target Amount (₹)</span>
              </label>
              <input
                {...register("target_amount")}
                type="number"
                step="0.01"
                placeholder="100000"
                className="w-full px-3.5 py-2 text-sm rounded-xl border border-border/80 bg-muted/20 text-foreground placeholder:text-muted-foreground/60 focus:outline-none focus:ring-2 focus:ring-primary/40"
              />
              {errors.target_amount && (
                <p className="text-xs text-rose-500">{errors.target_amount.message}</p>
              )}
            </div>

            <div className="space-y-1.5">
              <label className="text-xs font-semibold text-foreground flex items-center gap-1.5">
                <DollarSign className="w-3.5 h-3.5 text-muted-foreground" />
                <span>Starting Balance (₹)</span>
              </label>
              <input
                {...register("current_amount")}
                type="number"
                step="0.01"
                placeholder="0"
                className="w-full px-3.5 py-2 text-sm rounded-xl border border-border/80 bg-muted/20 text-foreground placeholder:text-muted-foreground/60 focus:outline-none focus:ring-2 focus:ring-primary/40"
              />
              {errors.current_amount && (
                <p className="text-xs text-rose-500">{errors.current_amount.message}</p>
              )}
            </div>
          </div>

          {/* Deadline & Category */}
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-3.5">
            <div className="space-y-1.5">
              <label className="text-xs font-semibold text-foreground flex items-center gap-1.5">
                <Calendar className="w-3.5 h-3.5 text-muted-foreground" />
                <span>Target Deadline (Optional)</span>
              </label>
              <input
                {...register("target_date")}
                type="date"
                className="w-full px-3.5 py-2 text-sm rounded-xl border border-border/80 bg-muted/20 text-foreground placeholder:text-muted-foreground/60 focus:outline-none focus:ring-2 focus:ring-primary/40"
              />
            </div>

            <div className="space-y-1.5">
              <label className="text-xs font-semibold text-foreground flex items-center gap-1.5">
                <Tag className="w-3.5 h-3.5 text-muted-foreground" />
                <span>Category</span>
              </label>
              <select
                {...register("category")}
                className="w-full px-3.5 py-2 text-sm rounded-xl border border-border/80 bg-muted/20 text-foreground focus:outline-none focus:ring-2 focus:ring-primary/40"
              >
                {CATEGORIES.map((c) => (
                  <option key={c} value={c} className="bg-card text-foreground">
                    {c}
                  </option>
                ))}
              </select>
            </div>
          </div>

          {/* Color Accent Picker */}
          <div className="space-y-1.5">
            <label className="text-xs font-semibold text-foreground flex items-center gap-1.5">
              <Palette className="w-3.5 h-3.5 text-muted-foreground" />
              <span>Accent Theme</span>
            </label>
            <div className="flex items-center gap-2 pt-1">
              {COLORS.map((col) => (
                <button
                  type="button"
                  key={col.value}
                  onClick={() => setValue("color", col.value)}
                  className={`w-7 h-7 rounded-full ${col.bg} transition-transform ${
                    selectedColor === col.value
                      ? "ring-2 ring-primary ring-offset-2 ring-offset-background scale-110"
                      : "opacity-70 hover:opacity-100"
                  }`}
                  title={col.label}
                />
              ))}
            </div>
          </div>

          {/* Notes */}
          <div className="space-y-1.5">
            <label className="text-xs font-semibold text-foreground flex items-center gap-1.5">
              <FileText className="w-3.5 h-3.5 text-muted-foreground" />
              <span>Personal Notes (Optional)</span>
            </label>
            <textarea
              {...register("notes")}
              rows={2}
              placeholder="Why this goal matters, funding sources, etc."
              className="w-full px-3.5 py-2 text-sm rounded-xl border border-border/80 bg-muted/20 text-foreground placeholder:text-muted-foreground/60 focus:outline-none focus:ring-2 focus:ring-primary/40"
            />
          </div>

          {/* Actions */}
          <div className="flex items-center justify-end gap-3 pt-3 border-t border-border/60">
            <button
              type="button"
              onClick={onClose}
              className="px-4 py-2 text-sm font-medium rounded-xl border border-border/80 text-muted-foreground hover:text-foreground hover:bg-muted/50 transition-colors"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={isSubmitting}
              className="px-5 py-2 text-sm font-bold rounded-xl bg-primary text-primary-foreground shadow-lg shadow-primary/20 hover:opacity-90 disabled:opacity-50 transition-opacity"
            >
              {isSubmitting ? "Saving..." : isEditing ? "Update Goal" : "Create Goal"}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};
