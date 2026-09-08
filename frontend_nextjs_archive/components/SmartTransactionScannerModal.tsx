"use client";

import React, { useState, useRef } from "react";
import { api, Category, PaymentMode, TransactionExtractionResponse } from "@/lib/api";
import { toast } from "sonner";
import {
  Scan,
  Receipt,
  MessageSquareText,
  UploadCloud,
  ClipboardPaste,
  CheckCircle2,
  AlertTriangle,
  ArrowRight,
  Sparkles,
  RefreshCw,
  X,
  ShieldCheck,
  Zap,
  ArrowDownLeft,
  ArrowUpRight,
  Layers,
  FileText,
} from "lucide-react";

interface SmartTransactionScannerModalProps {
  isOpen: boolean;
  onClose: () => void;
  categories: Category[];
  onSuccess?: () => void;
}

const SAMPLE_SMS = [
  {
    label: "Swiggy ₹450",
    text: "Sent Rs. 450.00 to Swiggy on 12-09-2026 via UPI Ref 429381029182. Avl Bal Rs. 32,450.00",
  },
  {
    label: "Uber Ride ₹280",
    text: "Paid Rs. 280.00 to Uber India via UPI Txn ID 8291039401. Net Bal Rs. 14,200",
  },
  {
    label: "Amazon ₹1,499",
    text: "Txn of Rs. 1,499.00 spent on your Card ending **9201 at Amazon Retail on 10-09-2026.",
  },
  {
    label: "Salary ₹65,000",
    text: "A/c *1234 credited by Rs. 65,000.00 on 01-09-2026 by InfoTech Salary. Net Bal Rs. 85,000",
  },
];

export function SmartTransactionScannerModal({
  isOpen,
  onClose,
  categories,
  onSuccess,
}: SmartTransactionScannerModalProps) {
  const [activeTab, setActiveTab] = useState<"sms" | "receipt">("sms");
  const [smsText, setSmsText] = useState("");
  const [imagePreview, setImagePreview] = useState<string | null>(null);
  const [imageBase64, setImageBase64] = useState<string | null>(null);
  const [isScanning, setIsScanning] = useState(false);
  const [isSaving, setIsSaving] = useState(false);

  // Extracted preview fields
  const [extractedData, setExtractedData] = useState<TransactionExtractionResponse | null>(null);
  const [editType, setEditType] = useState<"expense" | "income">("expense");
  const [editTitle, setEditTitle] = useState("");
  const [editAmount, setEditAmount] = useState("");
  const [editCategoryId, setEditCategoryId] = useState("");
  const [editDate, setEditDate] = useState("");
  const [editPaymentMode, setEditPaymentMode] = useState<PaymentMode>("UPI");
  const [editNotes, setEditNotes] = useState("");

  const fileInputRef = useRef<HTMLInputElement>(null);

  if (!isOpen) return null;

  const handlePasteClipboard = async () => {
    try {
      if (!navigator.clipboard?.readText) {
        toast.error("Clipboard access not permitted in this browser");
        return;
      }
      const text = await navigator.clipboard.readText();
      if (!text.trim()) {
        toast.info("Clipboard is empty");
        return;
      }
      setSmsText(text);
      toast.success("Pasted from clipboard!");
    } catch {
      toast.error("Unable to read clipboard. Please paste manually.");
    }
  };

  const handleImageFile = (file: File) => {
    if (!file.type.startsWith("image/")) {
      toast.error("Please upload an image file (PNG, JPG, WebP)");
      return;
    }
    if (file.size > 5 * 1024 * 1024) {
      toast.error("Receipt image must be under 5MB");
      return;
    }

    const reader = new FileReader();
    reader.onload = () => {
      const b64 = reader.result as string;
      setImagePreview(b64);
      setImageBase64(b64);
    };
    reader.readAsDataURL(file);
  };

  const handleDrop = (e: React.DragEvent) => {
    e.preventDefault();
    if (e.dataTransfer.files && e.dataTransfer.files[0]) {
      handleImageFile(e.dataTransfer.files[0]);
    }
  };

  const handleScan = async () => {
    if (activeTab === "sms" && !smsText.trim()) {
      toast.error("Please enter or paste transaction text");
      return;
    }
    if (activeTab === "receipt" && !imageBase64) {
      toast.error("Please upload or capture a receipt image");
      return;
    }

    setIsScanning(true);
    try {
      const response = await api.extractTransaction({
        text: activeTab === "sms" ? smsText : undefined,
        image_base64: activeTab === "receipt" ? imageBase64 || undefined : undefined,
        source_type: activeTab === "sms" ? "sms_text" : "receipt_image",
      });

      setExtractedData(response);
      setEditType(response.type);
      setEditTitle(response.title);
      setEditAmount(String(response.amount));
      setEditCategoryId(response.category_id || (categories[0]?.id ?? ""));
      setEditDate(response.transaction_date);
      setEditPaymentMode(response.payment_mode);
      setEditNotes(
        response.raw_reference ? `Ref: ${response.raw_reference}` : ""
      );

      toast.success("Transaction extracted successfully!");
    } catch (err: any) {
      toast.error(err.message || "Failed to parse transaction");
    } finally {
      setIsScanning(false);
    }
  };

  const handleSaveToSpendora = async () => {
    if (!editTitle.trim()) {
      toast.error("Please provide a title");
      return;
    }
    const numAmt = parseFloat(editAmount);
    if (isNaN(numAmt) || numAmt <= 0) {
      toast.error("Please enter a valid amount greater than ₹0");
      return;
    }
    if (!editDate) {
      toast.error("Please select a date");
      return;
    }

    setIsSaving(true);
    try {
      if (editType === "expense") {
        await api.createExpense({
          title: editTitle.trim(),
          amount: numAmt,
          category_id: editCategoryId || categories[0]?.id,
          expense_date: editDate,
          payment_mode: editPaymentMode,
          notes: editNotes.trim() || undefined,
        });
        toast.success(`Logged expense of ₹${numAmt.toLocaleString("en-IN")}!`);
      } else {
        await api.createIncome({
          title: editTitle.trim(),
          amount: numAmt,
          income_date: editDate,
          source: editTitle.trim(),
          payment_mode: editPaymentMode,
          notes: editNotes.trim() || undefined,
        });
        toast.success(`Logged income of ₹${numAmt.toLocaleString("en-IN")}!`);
      }

      if (onSuccess) onSuccess();
      onClose();
    } catch (err: any) {
      toast.error(err.message || "Failed to record transaction");
    } finally {
      setIsSaving(false);
    }
  };

  const resetScanner = () => {
    setExtractedData(null);
    setSmsText("");
    setImagePreview(null);
    setImageBase64(null);
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-950/80 backdrop-blur-md animate-in fade-in duration-200">
      <div className="relative w-full max-w-2xl max-h-[90vh] overflow-y-auto bg-slate-900 border border-slate-800 rounded-2xl shadow-2xl p-6 text-slate-100 flex flex-col gap-5">
        {/* Header */}
        <div className="flex items-center justify-between border-b border-slate-800/80 pb-4">
          <div className="flex items-center gap-3">
            <div className="w-10 h-10 rounded-xl bg-gradient-to-tr from-cyan-600 to-emerald-600 flex items-center justify-center shadow-lg shadow-cyan-500/20">
              <Scan className="w-5 h-5 text-white" />
            </div>
            <div>
              <div className="flex items-center gap-2">
                <h2 className="text-lg font-bold text-white tracking-tight">
                  Smart Receipt & SMS Parser
                </h2>
                <span className="px-2 py-0.5 text-[10px] font-semibold tracking-wider uppercase rounded-full bg-cyan-500/20 text-cyan-300 border border-cyan-500/30">
                  AI Feature 5
                </span>
              </div>
              <p className="text-xs text-slate-400">
                0-friction transaction logging with automatic PII scrubbing & duplicate guarding.
              </p>
            </div>
          </div>
          <button
            onClick={onClose}
            className="p-1.5 rounded-lg text-slate-400 hover:text-white hover:bg-slate-800 transition-colors"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Tab Selector */}
        {!extractedData && (
          <div className="flex gap-2 p-1 bg-slate-950/60 rounded-xl border border-slate-800">
            <button
              onClick={() => setActiveTab("sms")}
              className={`flex-1 flex items-center justify-center gap-2 py-2 px-3 text-xs font-semibold rounded-lg transition-all ${
                activeTab === "sms"
                  ? "bg-gradient-to-r from-cyan-600 to-emerald-600 text-white shadow-md"
                  : "text-slate-400 hover:text-slate-200 hover:bg-slate-800/40"
              }`}
            >
              <MessageSquareText className="w-4 h-4" />
              Paste UPI / Bank SMS
            </button>
            <button
              onClick={() => setActiveTab("receipt")}
              className={`flex-1 flex items-center justify-center gap-2 py-2 px-3 text-xs font-semibold rounded-lg transition-all ${
                activeTab === "receipt"
                  ? "bg-gradient-to-r from-cyan-600 to-emerald-600 text-white shadow-md"
                  : "text-slate-400 hover:text-slate-200 hover:bg-slate-800/40"
              }`}
            >
              <Receipt className="w-4 h-4" />
              Scan Receipt / Invoice
            </button>
          </div>
        )}

        {/* Input Intake State */}
        {!extractedData ? (
          <div className="space-y-4">
            {activeTab === "sms" ? (
              <div className="space-y-3">
                <div className="flex items-center justify-between">
                  <label className="text-xs font-medium text-slate-300">
                    Paste raw bank or UPI notification text:
                  </label>
                  <button
                    type="button"
                    onClick={handlePasteClipboard}
                    className="flex items-center gap-1.5 text-xs text-cyan-400 hover:text-cyan-300 font-medium transition-colors"
                  >
                    <ClipboardPaste className="w-3.5 h-3.5" />
                    Paste Clipboard
                  </button>
                </div>

                <div className="relative">
                  <textarea
                    rows={4}
                    value={smsText}
                    onChange={(e) => setSmsText(e.target.value)}
                    placeholder="e.g. Sent Rs. 450.00 to Swiggy on 12-09-2026 via UPI Ref 429381029182. Avl Bal Rs. 32,450.00"
                    className="w-full px-3.5 py-2.5 bg-slate-950/70 border border-slate-700/80 rounded-xl text-xs text-slate-100 placeholder:text-slate-500 focus:outline-none focus:ring-2 focus:ring-cyan-500/50 resize-none font-mono"
                  />
                  <div className="absolute bottom-2.5 right-2.5 flex items-center gap-1 text-[10px] text-emerald-400 bg-emerald-950/80 border border-emerald-800/60 px-2 py-0.5 rounded-md">
                    <ShieldCheck className="w-3 h-3" />
                    PII Scrubbed
                  </div>
                </div>

                {/* Quick Samples */}
                <div>
                  <span className="text-[11px] text-slate-400 font-medium block mb-1.5">
                    Quick test samples:
                  </span>
                  <div className="flex flex-wrap gap-1.5">
                    {SAMPLE_SMS.map((sample, idx) => (
                      <button
                        key={idx}
                        type="button"
                        onClick={() => setSmsText(sample.text)}
                        className="text-[11px] px-2.5 py-1 rounded-lg bg-slate-800/70 hover:bg-slate-800 text-slate-300 hover:text-white border border-slate-700/60 transition-colors flex items-center gap-1"
                      >
                        <Zap className="w-3 h-3 text-cyan-400" />
                        {sample.label}
                      </button>
                    ))}
                  </div>
                </div>
              </div>
            ) : (
              <div className="space-y-3">
                <input
                  type="file"
                  ref={fileInputRef}
                  onChange={(e) =>
                    e.target.files?.[0] && handleImageFile(e.target.files[0])
                  }
                  accept="image/*"
                  className="hidden"
                />

                {!imagePreview ? (
                  <div
                    onDragOver={(e) => e.preventDefault()}
                    onDrop={handleDrop}
                    onClick={() => fileInputRef.current?.click()}
                    className="border-2 border-dashed border-slate-700 hover:border-cyan-500/70 bg-slate-950/40 rounded-xl p-8 flex flex-col items-center justify-center gap-2 cursor-pointer transition-colors group"
                  >
                    <div className="w-12 h-12 rounded-full bg-slate-800 group-hover:bg-cyan-950/60 flex items-center justify-center transition-colors">
                      <UploadCloud className="w-6 h-6 text-slate-400 group-hover:text-cyan-400 transition-colors" />
                    </div>
                    <span className="text-xs font-semibold text-slate-200">
                      Click to upload receipt or drag & drop
                    </span>
                    <span className="text-[11px] text-slate-500">
                      Supports JPG, PNG, WebP up to 5MB
                    </span>
                  </div>
                ) : (
                  <div className="relative rounded-xl overflow-hidden border border-slate-700 bg-slate-950 max-h-56 flex items-center justify-center group">
                    {/* eslint-disable-next-line @next/next/no-img-element */}
                    <img
                      src={imagePreview}
                      alt="Receipt Preview"
                      className="max-h-56 object-contain"
                    />
                    <button
                      type="button"
                      onClick={() => {
                        setImagePreview(null);
                        setImageBase64(null);
                      }}
                      className="absolute top-2 right-2 p-1 bg-slate-900/80 hover:bg-slate-900 text-slate-300 rounded-md border border-slate-700 transition-colors"
                    >
                      <X className="w-4 h-4" />
                    </button>
                  </div>
                )}
              </div>
            )}

            {/* Scan Action Button */}
            <button
              onClick={handleScan}
              disabled={isScanning || (activeTab === "sms" && !smsText.trim()) || (activeTab === "receipt" && !imageBase64)}
              className="w-full py-2.5 px-4 rounded-xl bg-gradient-to-r from-cyan-500 to-emerald-500 hover:from-cyan-400 hover:to-emerald-400 disabled:opacity-50 disabled:cursor-not-allowed text-white font-semibold text-xs flex items-center justify-center gap-2 shadow-lg shadow-cyan-500/20 transition-all"
            >
              {isScanning ? (
                <>
                  <RefreshCw className="w-4 h-4 animate-spin" />
                  Extracting with Spendora Engine...
                </>
              ) : (
                <>
                  <Sparkles className="w-4 h-4" />
                  Extract Details
                </>
              )}
            </button>
          </div>
        ) : (
          /* Extracted Preview & Edit Mode */
          <div className="space-y-4">
            {/* Extraction Method Banner */}
            <div className="flex items-center justify-between p-2.5 rounded-xl bg-slate-950/60 border border-slate-800">
              <div className="flex items-center gap-2">
                <CheckCircle2 className="w-4 h-4 text-emerald-400" />
                <span className="text-xs font-semibold text-slate-200">
                  Transaction Auto-Detected
                </span>
              </div>
              <span className="text-[10px] uppercase font-mono px-2 py-0.5 rounded bg-cyan-950 text-cyan-300 border border-cyan-800">
                {extractedData.extraction_method.replace(/_/g, " ")} (
                {Math.round(extractedData.confidence_score * 100)}% conf)
              </span>
            </div>

            {/* Duplicate Warning */}
            {extractedData.is_potential_duplicate && extractedData.duplicate_warning && (
              <div className="p-3 rounded-xl bg-amber-950/40 border border-amber-800/80 text-amber-200 text-xs flex items-start gap-2.5">
                <AlertTriangle className="w-4 h-4 text-amber-400 shrink-0 mt-0.5" />
                <div>
                  <span className="font-semibold block mb-0.5">
                    Duplicate Warning Guard
                  </span>
                  <span>{extractedData.duplicate_warning}</span>
                </div>
              </div>
            )}

            {/* Edit Form */}
            <div className="space-y-3 bg-slate-950/40 p-3.5 rounded-xl border border-slate-800/80">
              {/* Type Switcher */}
              <div className="flex items-center justify-between pb-2 border-b border-slate-800">
                <span className="text-xs font-medium text-slate-400">
                  Transaction Type:
                </span>
                <div className="flex gap-1 bg-slate-900 p-0.5 rounded-lg border border-slate-800">
                  <button
                    type="button"
                    onClick={() => setEditType("expense")}
                    className={`flex items-center gap-1 px-3 py-1 text-xs font-semibold rounded-md transition-colors ${
                      editType === "expense"
                        ? "bg-rose-500/20 text-rose-300 border border-rose-500/40"
                        : "text-slate-400 hover:text-white"
                    }`}
                  >
                    <ArrowDownLeft className="w-3.5 h-3.5 text-rose-400" />
                    Expense
                  </button>
                  <button
                    type="button"
                    onClick={() => setEditType("income")}
                    className={`flex items-center gap-1 px-3 py-1 text-xs font-semibold rounded-md transition-colors ${
                      editType === "income"
                        ? "bg-emerald-500/20 text-emerald-300 border border-emerald-500/40"
                        : "text-slate-400 hover:text-white"
                    }`}
                  >
                    <ArrowUpRight className="w-3.5 h-3.5 text-emerald-400" />
                    Income
                  </button>
                </div>
              </div>

              {/* Title & Amount Grid */}
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
                <div>
                  <label className="text-[11px] font-medium text-slate-400 block mb-1">
                    {editType === "expense" ? "Merchant / Title" : "Source / Title"}
                  </label>
                  <input
                    type="text"
                    value={editTitle}
                    onChange={(e) => setEditTitle(e.target.value)}
                    className="w-full px-3 py-2 bg-slate-900 border border-slate-700/80 rounded-lg text-xs text-white focus:outline-none focus:ring-1 focus:ring-cyan-500"
                  />
                </div>

                <div>
                  <label className="text-[11px] font-medium text-slate-400 block mb-1">
                    Amount (₹)
                  </label>
                  <input
                    type="number"
                    step="0.01"
                    value={editAmount}
                    onChange={(e) => setEditAmount(e.target.value)}
                    className="w-full px-3 py-2 bg-slate-900 border border-slate-700/80 rounded-lg text-xs text-emerald-400 font-mono font-bold focus:outline-none focus:ring-1 focus:ring-cyan-500"
                  />
                </div>
              </div>

              {/* Category, Date & Mode Grid */}
              <div className="grid grid-cols-1 sm:grid-cols-3 gap-3">
                {editType === "expense" ? (
                  <div>
                    <label className="text-[11px] font-medium text-slate-400 block mb-1">
                      Category
                    </label>
                    <select
                      value={editCategoryId}
                      onChange={(e) => setEditCategoryId(e.target.value)}
                      className="w-full px-2.5 py-2 bg-slate-900 border border-slate-700/80 rounded-lg text-xs text-white focus:outline-none focus:ring-1 focus:ring-cyan-500"
                    >
                      {categories.map((c) => (
                        <option key={c.id} value={c.id}>
                          {c.name}
                        </option>
                      ))}
                    </select>
                  </div>
                ) : (
                  <div>
                    <label className="text-[11px] font-medium text-slate-400 block mb-1">
                      Classification
                    </label>
                    <div className="px-3 py-2 bg-slate-900/60 border border-slate-800 rounded-lg text-xs text-emerald-300 font-medium">
                      Income Credit
                    </div>
                  </div>
                )}

                <div>
                  <label className="text-[11px] font-medium text-slate-400 block mb-1">
                    Date
                  </label>
                  <input
                    type="date"
                    value={editDate}
                    max={new Date().toISOString().split("T")[0]}
                    onChange={(e) => setEditDate(e.target.value)}
                    className="w-full px-3 py-1.5 bg-slate-900 border border-slate-700/80 rounded-lg text-xs text-white focus:outline-none focus:ring-1 focus:ring-cyan-500"
                  />
                </div>

                <div>
                  <label className="text-[11px] font-medium text-slate-400 block mb-1">
                    Payment Mode
                  </label>
                  <select
                    value={editPaymentMode}
                    onChange={(e) => setEditPaymentMode(e.target.value as PaymentMode)}
                    className="w-full px-2.5 py-2 bg-slate-900 border border-slate-700/80 rounded-lg text-xs text-white focus:outline-none focus:ring-1 focus:ring-cyan-500"
                  >
                    <option value="UPI">UPI</option>
                    <option value="Card">Card</option>
                    <option value="Net Banking">Net Banking</option>
                    <option value="Cash">Cash</option>
                    <option value="Other">Other</option>
                  </select>
                </div>
              </div>

              {/* Itemized breakdown if present */}
              {extractedData.items && extractedData.items.length > 0 && (
                <div className="pt-2 border-t border-slate-800">
                  <span className="text-[11px] font-semibold text-slate-400 block mb-1.5 flex items-center gap-1.5">
                    <Layers className="w-3.5 h-3.5 text-cyan-400" />
                    Itemized Receipt Line Items ({extractedData.items.length})
                  </span>
                  <div className="space-y-1 max-h-24 overflow-y-auto">
                    {extractedData.items.map((it, idx) => (
                      <div
                        key={idx}
                        className="flex items-center justify-between text-xs px-2 py-1 bg-slate-900/60 rounded border border-slate-800"
                      >
                        <span className="text-slate-300">{it.name}</span>
                        <span className="font-mono text-emerald-400">
                          ₹{Number(it.amount).toLocaleString("en-IN")}
                        </span>
                      </div>
                    ))}
                  </div>
                </div>
              )}
            </div>

            {/* Action Buttons */}
            <div className="flex gap-2 pt-1">
              <button
                type="button"
                onClick={resetScanner}
                className="flex-1 py-2.5 px-3 rounded-xl bg-slate-800 hover:bg-slate-700 text-slate-200 text-xs font-semibold transition-colors"
              >
                Scan Another
              </button>
              <button
                type="button"
                onClick={handleSaveToSpendora}
                disabled={isSaving}
                className="flex-[2] py-2.5 px-4 rounded-xl bg-gradient-to-r from-emerald-500 to-cyan-500 hover:from-emerald-400 hover:to-cyan-400 disabled:opacity-50 text-white text-xs font-bold flex items-center justify-center gap-2 shadow-lg shadow-emerald-500/20 transition-all"
              >
                {isSaving ? (
                  <>
                    <RefreshCw className="w-4 h-4 animate-spin" />
                    Recording in Spendora...
                  </>
                ) : (
                  <>
                    <CheckCircle2 className="w-4 h-4" />
                    Save as {editType === "expense" ? "Expense" : "Income"}
                  </>
                )}
              </button>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
