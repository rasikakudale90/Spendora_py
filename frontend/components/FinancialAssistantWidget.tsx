"use client";

import React, { useState, useRef, useEffect } from "react";
import { useAuth } from "@/context/AuthContext";
import { api, ChatMessage, FinancialActionIntent } from "@/lib/api";
import {
  Sparkles,
  MessageSquare,
  X,
  Send,
  Trash2,
  Minimize2,
  Bot,
  User,
  ArrowRight,
  TrendingUp,
  ShieldAlert,
  Search,
  Zap,
  RefreshCw,
} from "lucide-react";
import { useRouter } from "next/navigation";
import { toast } from "sonner";
import { cn } from "@/lib/utils";

const STARTER_PROMPTS = [
  { label: "Safe to Spend?", query: "What is my safe daily spending limit for today?" },
  { label: "Top Category", query: "Which category is draining most of my money this month?" },
  { label: "Savings Rate", query: "How much did I save this month and what is my savings rate?" },
  { label: "3 Tips to Save", query: "Give me 3 personalized tips to save money right now." },
];

export function FinancialAssistantWidget() {
  const { user } = useAuth();
  const router = useRouter();

  const [isOpen, setIsOpen] = useState(false);
  const [messages, setMessages] = useState<ChatMessage[]>([]);
  const [inputMessage, setInputMessage] = useState("");
  const [isLoading, setIsLoading] = useState(false);
  const [suggestedPrompts, setSuggestedPrompts] = useState<string[]>([]);
  const [lastActionIntent, setLastActionIntent] = useState<FinancialActionIntent | null>(null);

  const messagesEndRef = useRef<HTMLDivElement>(null);
  const inputRef = useRef<HTMLInputElement>(null);

  // Auto-scroll chat to latest message
  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: "smooth" });
  };

  useEffect(() => {
    if (isOpen) {
      scrollToBottom();
      // Focus input when opened
      setTimeout(() => inputRef.current?.focus(), 150);
    }
  }, [isOpen, messages, isLoading]);

  // Handle starter greeting if empty
  useEffect(() => {
    if (isOpen && messages.length === 0) {
      setMessages([
        {
          role: "assistant",
          content: `👋 **Hi ${user?.full_name?.split(" ")[0] || "there"}! I'm Spendora AI, your personal financial advisor.**\n\nI have real-time access to your income, expenses, active budgets, and safe burn velocity. Ask me anything about your finances or pick a topic below!`,
          timestamp: new Date().toLocaleTimeString([], { hour: "2-digit", minute: "2-digit" }),
        },
      ]);
    }
  }, [isOpen, user, messages.length]);

  // Listen for global custom trigger event to open chat
  useEffect(() => {
    const handleOpenAssistant = (e: any) => {
      setIsOpen(true);
      if (e.detail?.query) {
        handleSendMessage(e.detail.query);
      }
    };
    window.addEventListener("open-ai-assistant", handleOpenAssistant);
    return () => window.removeEventListener("open-ai-assistant", handleOpenAssistant);
  }, [messages]);

  if (!user) return null; // Only available for logged-in users

  const handleSendMessage = async (customText?: string) => {
    const text = customText || inputMessage.trim();
    if (!text || isLoading) return;

    const userMsg: ChatMessage = {
      role: "user",
      content: text,
      timestamp: new Date().toLocaleTimeString([], { hour: "2-digit", minute: "2-digit" }),
    };

    const newHistory = [...messages, userMsg];
    setMessages(newHistory);
    setInputMessage("");
    setIsLoading(true);
    setSuggestedPrompts([]);
    setLastActionIntent(null);

    try {
      const response = await api.chatWithAssistant({
        message: text,
        history: newHistory.slice(-6),
      });

      const assistantMsg: ChatMessage = {
        role: "assistant",
        content: response.reply,
        timestamp: new Date().toLocaleTimeString([], { hour: "2-digit", minute: "2-digit" }),
      };

      setMessages((prev) => [...prev, assistantMsg]);
      setSuggestedPrompts(response.suggested_prompts || []);
      if (response.action_intent) {
        setLastActionIntent(response.action_intent);
      }
    } catch (error: any) {
      toast.error("Failed to get response from AI Assistant", {
        description: error.message || "Please try again.",
      });
      const errorMsg: ChatMessage = {
        role: "assistant",
        content: "⚠️ I encountered a temporary connection glitch. Please try asking again!",
        timestamp: new Date().toLocaleTimeString([], { hour: "2-digit", minute: "2-digit" }),
      };
      setMessages((prev) => [...prev, errorMsg]);
    } finally {
      setIsLoading(false);
    }
  };

  const handleKeyDown = (e: React.KeyboardEvent<HTMLInputElement>) => {
    if (e.key === "Enter" && !e.shiftKey) {
      e.preventDefault();
      handleSendMessage();
    }
  };

  const handleClearChat = () => {
    setMessages([]);
    setSuggestedPrompts([]);
    setLastActionIntent(null);
  };

  const handleActionClick = (actionIntent: FinancialActionIntent) => {
    if (actionIntent.action === "simulate_purchase") {
      window.dispatchEvent(
        new CustomEvent("open-purchase-simulator", { detail: actionIntent.payload })
      );
      toast.info("Opening Purchase Decision Simulator...");
    } else if (actionIntent.action === "view_leaks") {
      window.dispatchEvent(new CustomEvent("open-leak-hunter"));
      toast.info("Opening Autonomous Leak Hunter...");
    } else if (actionIntent.action === "navigate") {
      const path = actionIntent.payload?.path || "/expenses";
      router.push(path);
    } else if (actionIntent.action === "set_budget") {
      window.dispatchEvent(new CustomEvent("open-budget-manager"));
    } else if (actionIntent.action === "scan_receipt" || (actionIntent.action as any) === "extract_transaction") {
      window.dispatchEvent(new CustomEvent("open-transaction-scanner"));
      toast.info("Opening Smart Receipt & SMS Parser...");
    }
  };

  // Format simple markdown helper
  const renderMarkdown = (text: string) => {
    const lines = text.split("\n");
    return (
      <div className="space-y-1.5 text-xs sm:text-sm leading-relaxed">
        {lines.map((line, idx) => {
          const trimmed = line.trim();
          if (!trimmed) return <div key={idx} className="h-1" />;

          // Heading 3
          if (trimmed.startsWith("### ")) {
            return (
              <h4 key={idx} className="font-bold text-foreground text-sm sm:text-base pt-1 flex items-center gap-1.5">
                {trimmed.replace("### ", "")}
              </h4>
            );
          }

          // Bullet points
          if (trimmed.startsWith("- ") || trimmed.startsWith("* ")) {
            const content = trimmed.substring(2);
            return (
              <div key={idx} className="flex items-start gap-2 pl-1">
                <span className="text-emerald-500 font-bold">•</span>
                <span className="flex-1" dangerouslySetInnerHTML={{ __html: formatInline(content) }} />
              </div>
            );
          }

          // Numbered lists
          if (/^\d+\.\s/.test(trimmed)) {
            const num = trimmed.match(/^\d+\./)?.[0];
            const content = trimmed.replace(/^\d+\.\s*/, "");
            return (
              <div key={idx} className="flex items-start gap-2 pl-1">
                <span className="text-teal-500 font-semibold">{num}</span>
                <span className="flex-1" dangerouslySetInnerHTML={{ __html: formatInline(content) }} />
              </div>
            );
          }

          // Table divider line
          if (trimmed.includes("| :---") || trimmed.includes("|:---")) {
            return null;
          }

          // Table row
          if (trimmed.startsWith("|") && trimmed.endsWith("|")) {
            const cells = trimmed.split("|").filter((c) => c.trim().length > 0);
            return (
              <div key={idx} className="grid grid-cols-2 gap-2 p-1.5 rounded-lg bg-slate-200/50 dark:bg-white/5 text-xs">
                {cells.map((cell, cIdx) => (
                  <span
                    key={cIdx}
                    className={cIdx === 0 ? "text-muted-foreground font-medium" : "text-foreground font-bold text-right"}
                    dangerouslySetInnerHTML={{ __html: formatInline(cell.trim()) }}
                  />
                ))}
              </div>
            );
          }

          // Normal paragraph
          return (
            <p key={idx} dangerouslySetInnerHTML={{ __html: formatInline(trimmed) }} />
          );
        })}
      </div>
    );
  };

  const formatInline = (str: string) => {
    return str
      .replace(/\*\*(.*?)\*\*/g, '<strong class="text-foreground font-semibold">$1</strong>')
      .replace(/\*(.*?)\*/g, '<em class="text-muted-foreground">$1</em>')
      .replace(/₹([\d,]+(?:\.\d+)?)/g, '<span class="text-emerald-600 dark:text-emerald-400 font-bold">₹$1</span>');
  };

  return (
    <>
      {/* ── Floating Action Trigger Button (Bottom Right) ── */}
      {!isOpen && (
        <div className="fixed bottom-5 right-5 z-50 group">
          <button
            onClick={() => setIsOpen(true)}
            className="relative flex items-center gap-2.5 px-4 py-3 rounded-full bg-gradient-to-r from-emerald-500 via-teal-500 to-cyan-500 text-slate-950 font-bold shadow-xl shadow-emerald-500/25 hover:shadow-emerald-500/40 hover:scale-105 active:scale-95 transition-all duration-300"
            aria-label="Open Spendora AI Financial Assistant"
          >
            <span className="relative flex h-3 w-3">
              <span className="animate-ping absolute inline-flex h-full w-full rounded-full bg-slate-950 opacity-75"></span>
              <span className="relative inline-flex rounded-full h-3 w-3 bg-slate-950"></span>
            </span>
            <Sparkles className="w-5 h-5 text-slate-950 animate-pulse" />
            <span className="text-sm font-bold tracking-tight">Ask Spendora AI</span>
          </button>
        </div>
      )}

      {/* ── Conversational Chat Drawer / Dialog ── */}
      {isOpen && (
        <div className="fixed bottom-4 right-4 sm:bottom-6 sm:right-6 z-50 w-[95vw] sm:w-[440px] h-[580px] max-h-[88vh] rounded-3xl glass-card backdrop-blur-2xl border border-white/20 dark:border-white/10 shadow-2xl flex flex-col overflow-hidden animate-in zoom-in-95 duration-200">
          
          {/* Header */}
          <div className="p-4 border-b border-border/70 bg-slate-100/80 dark:bg-slate-900/80 backdrop-blur-md flex items-center justify-between">
            <div className="flex items-center gap-3">
              <div className="w-9 h-9 rounded-2xl bg-gradient-to-tr from-emerald-500 to-teal-400 flex items-center justify-center shadow-md shadow-emerald-500/20">
                <Sparkles className="w-5 h-5 text-slate-950" />
              </div>
              <div>
                <div className="flex items-center gap-1.5">
                  <h3 className="font-bold text-sm text-foreground">Spendora AI Assistant</h3>
                  <span className="text-[10px] uppercase font-bold tracking-wider px-1.5 py-0.5 rounded-full bg-emerald-500/15 text-emerald-600 dark:text-emerald-400 border border-emerald-500/30">
                    Live
                  </span>
                </div>
                <p className="text-[11px] text-muted-foreground flex items-center gap-1">
                  <span className="w-1.5 h-1.5 rounded-full bg-emerald-500 animate-pulse" />
                  Real-time telemetry connected
                </p>
              </div>
            </div>

            <div className="flex items-center gap-1">
              <button
                onClick={handleClearChat}
                title="Clear Conversation"
                className="p-1.5 rounded-xl text-muted-foreground hover:text-foreground hover:bg-slate-200/60 dark:hover:bg-white/10 transition-colors"
              >
                <Trash2 className="w-4 h-4" />
              </button>
              <button
                onClick={() => setIsOpen(false)}
                title="Minimize Assistant"
                className="p-1.5 rounded-xl text-muted-foreground hover:text-foreground hover:bg-slate-200/60 dark:hover:bg-white/10 transition-colors"
              >
                <X className="w-4 h-4" />
              </button>
            </div>
          </div>

          {/* Messages Scroll View */}
          <div className="flex-1 overflow-y-auto p-4 space-y-4 scroll-smooth">
            {messages.map((msg, idx) => (
              <div
                key={idx}
                className={cn(
                  "flex gap-2.5",
                  msg.role === "user" ? "justify-end" : "justify-start"
                )}
              >
                {msg.role === "assistant" && (
                  <div className="w-7 h-7 rounded-xl bg-emerald-500/10 border border-emerald-500/20 flex items-center justify-center shrink-0 mt-0.5">
                    <Bot className="w-4 h-4 text-emerald-500" />
                  </div>
                )}

                <div
                  className={cn(
                    "max-w-[85%] rounded-2xl px-4 py-3 shadow-sm",
                    msg.role === "user"
                      ? "bg-gradient-to-r from-emerald-600 to-teal-600 text-white rounded-br-sm"
                      : "bg-slate-100 dark:bg-slate-900/90 border border-border/80 text-foreground rounded-bl-sm"
                  )}
                >
                  {msg.role === "user" ? (
                    <p className="text-xs sm:text-sm font-medium">{msg.content}</p>
                  ) : (
                    renderMarkdown(msg.content)
                  )}

                  <div className="mt-1 flex items-center justify-end text-[10px] opacity-60">
                    <span>{msg.timestamp}</span>
                  </div>
                </div>

                {msg.role === "user" && (
                  <div className="w-7 h-7 rounded-xl bg-teal-500/10 border border-teal-500/20 flex items-center justify-center shrink-0 mt-0.5">
                    <User className="w-4 h-4 text-teal-500" />
                  </div>
                )}
              </div>
            ))}

            {/* Smart Action Intent Card */}
            {lastActionIntent && (
              <div className="p-3 rounded-2xl bg-gradient-to-r from-indigo-500/10 via-purple-500/10 to-teal-500/10 border border-indigo-500/30 flex items-center justify-between gap-3 animate-in fade-in-50 duration-300">
                <div className="flex items-center gap-2.5">
                  <div className="w-8 h-8 rounded-xl bg-indigo-500/20 flex items-center justify-center shrink-0">
                    <Zap className="w-4 h-4 text-indigo-500" />
                  </div>
                  <div>
                    <p className="text-xs font-semibold text-foreground">Recommended Action</p>
                    <p className="text-[11px] text-muted-foreground">{lastActionIntent.label}</p>
                  </div>
                </div>
                <button
                  onClick={() => handleActionClick(lastActionIntent)}
                  className="px-3 py-1.5 rounded-xl bg-indigo-600 hover:bg-indigo-700 text-white text-xs font-semibold transition-all flex items-center gap-1 shrink-0 shadow-sm"
                >
                  <span>Open</span>
                  <ArrowRight className="w-3.5 h-3.5" />
                </button>
              </div>
            )}

            {/* Typing / Loading Wave */}
            {isLoading && (
              <div className="flex gap-2.5 items-center">
                <div className="w-7 h-7 rounded-xl bg-emerald-500/10 border border-emerald-500/20 flex items-center justify-center shrink-0">
                  <Bot className="w-4 h-4 text-emerald-500 animate-spin" />
                </div>
                <div className="px-4 py-3 rounded-2xl rounded-bl-sm bg-slate-100 dark:bg-slate-900/90 border border-border/80 flex items-center gap-1.5">
                  <span className="w-2 h-2 rounded-full bg-emerald-500 animate-bounce" style={{ animationDelay: "0ms" }} />
                  <span className="w-2 h-2 rounded-full bg-teal-500 animate-bounce" style={{ animationDelay: "150ms" }} />
                  <span className="w-2 h-2 rounded-full bg-cyan-500 animate-bounce" style={{ animationDelay: "300ms" }} />
                  <span className="text-xs text-muted-foreground ml-1.5">Analyzing your financial telemetry...</span>
                </div>
              </div>
            )}

            <div ref={messagesEndRef} />
          </div>

          {/* Starter / Suggested Chips */}
          <div className="px-3 py-2 border-t border-border/50 bg-slate-50/50 dark:bg-slate-950/40">
            <div className="flex items-center gap-1.5 overflow-x-auto no-scrollbar py-1">
              {(suggestedPrompts.length > 0 ? suggestedPrompts : STARTER_PROMPTS.map(p => p.query)).map(
                (prompt, idx) => (
                  <button
                    key={idx}
                    onClick={() => handleSendMessage(prompt)}
                    disabled={isLoading}
                    className="shrink-0 px-2.5 py-1 rounded-lg text-[11px] font-medium bg-slate-200/60 dark:bg-white/5 hover:bg-emerald-500/15 hover:text-emerald-600 dark:hover:text-emerald-400 border border-border/70 hover:border-emerald-500/30 transition-all text-muted-foreground"
                  >
                    {prompt}
                  </button>
                )
              )}
            </div>
          </div>

          {/* Input Box */}
          <div className="p-3 bg-slate-100/90 dark:bg-slate-900/90 border-t border-border/70 backdrop-blur-md">
            <div className="flex items-center gap-2">
              <input
                ref={inputRef}
                type="text"
                value={inputMessage}
                onChange={(e) => setInputMessage(e.target.value)}
                onKeyDown={handleKeyDown}
                placeholder="Ask Spendora (e.g. 'Can I afford ₹2,000 dinner?')..."
                disabled={isLoading}
                className="flex-1 bg-white dark:bg-slate-950/80 border border-border rounded-xl px-3.5 py-2.5 text-xs sm:text-sm text-foreground placeholder:text-muted-foreground focus:outline-none focus:ring-2 focus:ring-emerald-500/30 focus:border-emerald-500 transition-all"
              />
              <button
                onClick={() => handleSendMessage()}
                disabled={isLoading || !inputMessage.trim()}
                className="w-10 h-10 rounded-xl bg-gradient-to-tr from-emerald-500 to-teal-400 text-slate-950 flex items-center justify-center font-bold hover:scale-105 active:scale-95 disabled:opacity-40 disabled:hover:scale-100 transition-all shadow-md shadow-emerald-500/20 shrink-0"
              >
                <Send className="w-4 h-4" />
              </button>
            </div>
          </div>
        </div>
      )}
    </>
  );
}
