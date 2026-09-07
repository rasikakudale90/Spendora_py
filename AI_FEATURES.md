# 🤖 Spendora AI — Intelligent Financial Engine (Complete Suite)

A comprehensive reference for the **7 AI-powered financial intelligence features** built into Spendora V1. 

Spendora uses a **hybrid dual-engine architecture**:
1. **Multi-LLM Provider Intelligence:** Supports Google Gemini, OpenAI, Anthropic Claude, and Groq via unified provider-agnostic interfaces.
2. **Deterministic Mathematical & Heuristic Fallback Engine:** 100% offline-ready, instantaneous, and zero-cost fallback guaranteeing Spendora never crashes or degrades even during external API downtime, rate limits, or network cold starts.

---

## 📊 AI Features Matrix

| # | Feature Name | Primary Endpoint | UI Component / Page | Engine Type |
|---|---|---|---|---|
| **1** | 🛒 Purchase Simulator | `POST /api/v1/ai/simulate-purchase` | `PurchaseSimulatorModal.tsx` | Math + LLM Reasoning |
| **2** | 🔍 Autonomous Leak Hunter | `GET /api/v1/ai/leak-analysis` | `LeakHunterModal.tsx` | 90-Day Transaction Analyzer |
| **3** | ⏱️ Safe-to-Spend Speedometer | `GET /api/v1/ai/safe-to-spend` | `SafeToSpendCard.tsx` | Real-Time Burn Forecaster |
| **4** | 💬 Conversational Advisor | `POST /api/v1/ai/chat` | `FinancialAssistantWidget.tsx` | Streaming Contextual Chatbot |
| **5** | 🧾 Smart Receipt & SMS Parser | `POST /api/v1/ai/extract-transaction` | `SmartTransactionScannerModal.tsx` | Deterministic Regex + Vision OCR |
| **6** | 🎯 Financial Health Radar | `GET /api/v1/ai/health-score` | `FinancialHealthCard.tsx` | 5-Pillar FICO-Style Scoring |
| **7** | 🏆 Smart Goals & Runway | `GET /api/v1/ai/goals-runway` | `/goals` Page & Modals | Cash Flow Runway Forecaster |

---

## 🚀 Detailed Feature Specifications

### 1. 🛒 "Can I Afford This?" — Purchase Decision Simulator
- **Endpoints:** `POST /api/v1/ai/simulate-purchase`
- **Backend Service:** `simulate_purchase()` in `backend/app/services/ai_service.py`
- **Frontend Component:** `frontend/components/PurchaseSimulatorModal.tsx`
- **Trigger Locations:** Dashboard header button, Financial Assistant action prompt.
- **Description:** Simulates the real-time financial impact of a planned purchase against monthly cash flow, category budgets, and savings targets before the money is spent.
- **Key Capabilities:**
  - Instant 3-tier verdict: `Safe` (🟢), `Caution` (🟡), or `Over-Budget` (🔴).
  - Side-by-side before vs. after comparison: Net Cash Flow, Savings Rate %, Remaining Budget, and Daily Safe Spend.
  - Direct 1-click **"Add as Expense"** shortcut to record the purchase upon decision.
  - Quick-test sample chips (₹500 Book, ₹3,500 Dinner, ₹18,000 Gadget, ₹65,000 Trip).

---

### 2. 🔍 Autonomous "Leak Hunter" & Subscription Audit
- **Endpoints:** `GET /api/v1/ai/leak-analysis`
- **Backend Service:** `analyze_leaks_and_subscriptions()` in `backend/app/services/ai_service.py`
- **Frontend Component:** `frontend/components/LeakHunterModal.tsx`
- **Trigger Locations:** Dashboard action button, Chatbot action intents.
- **Description:** Deep scans 90 days of transaction history to uncover hidden recurring digital subscriptions and accumulated micro-spending leaks.
- **Key Capabilities:**
  - **Active Subscriptions Audit:** Identifies repeat digital billing (Netflix, Spotify, Gym, iCloud, Hotstar, Prime) with monthly & annual drain totals.
  - **Micro-Spending Leak Detection:** Aggregates low-ticket daily charges (<= ₹150) across categories (Tea/Coffee, Snacks, Quick Delivery, Cab Surcharges).
  - **Annualized Impact Projection:** Reveals the true yearly cost of small daily habits (e.g. ₹80/day = ₹29,200/year).
  - Actionable AI cancellation and consolidation checklist.

---

### 3. ⏱️ Smart "Safe-to-Spend" Real-Time Speedometer & Burn Forecaster
- **Endpoints:** `GET /api/v1/ai/safe-to-spend`
- **Backend Service:** `calculate_safe_to_spend()` in `backend/app/services/ai_service.py`
- **Frontend Component:** `frontend/components/SafeToSpendCard.tsx`
- **Placement:** Prominently mounted on `frontend/app/dashboard/page.tsx` above KPI cards.
- **Description:** Live dynamic burn speedometer calculating how much the user can safely spend per day for the remainder of the month without breaching budgets or depleting net savings.
- **Key Capabilities:**
  - **Daily Safe Burn Allowance:** `(Remaining Buffer) / (Days Remaining in Month)`.
  - **Current Burn Velocity:** `(Total Spent) / (Days Passed)`.
  - **Pacing Status:** `Optimal` (<= 85% pace), `Warning` (85–105% pace), `Danger` (> 105% pace).
  - Month-end cash surplus / deficit trajectory forecast and Day-of-Depletion projection.
  - Client-side zero-fail fallback engine consuming `DashboardSummary` for 100% uptime.

---

### 4. 💬 Natural Language Financial Assistant & Conversational Chatbot
- **Endpoints:** `POST /api/v1/ai/chat`
- **Backend Service:** `chat_financial_advisor()` in `backend/app/services/ai_service.py`
- **Frontend Component:** `frontend/components/FinancialAssistantWidget.tsx`
- **Placement:** Floating bottom-right glassmorphic widget mounted globally in `app/layout.tsx`.
- **Description:** Real-time personal financial advisor connected directly to the user's live telemetry (income, spending, active budgets, top categories, and safe burn rate).
- **Key Capabilities:**
  - Answers natural language questions (*"How much can I spend this weekend?"*, *"Where is most of my money going?"*, *"Can I afford a ₹4,000 dinner tonight?"*).
  - Markdown-rendered responses with bold financial metrics, bullet points, and tips.
  - Suggested contextual quick prompts.
  - Actionable intents that automatically trigger native modals (e.g. opening Purchase Simulator or SMS Scanner directly from chat).

---

### 5. 🧾 Smart Receipt & UPI SMS Parser
- **Endpoints:** `POST /api/v1/ai/extract-transaction`
- **Backend Service:** `extract_transaction()` & `sanitize_pii()` in `backend/app/services/ai_service.py`
- **Frontend Component:** `frontend/components/SmartTransactionScannerModal.tsx`
- **Trigger Locations:** `/expenses` header (`⚡ Scan / Paste SMS`), `/dashboard` header, and Assistant widget.
- **Description:** Eliminates manual bookkeeping by extracting transaction details from pasted Indian bank/UPI SMS notifications or paper receipt images.
- **Key Capabilities:**
  - **Deterministic Banking Regex Engine:** High-precision parsing for HDFC, SBI, ICICI, Axis, Kotak, GPay, PhonePe, and Paytm notifications.
  - **Automated PII Scrubbing:** Erases account numbers, card digits, bank balances, and OTPs before processing.
  - **Auto Debit vs. Credit Classifier:** Seamlessly routes debits to Expenses and credits to Incomes.
  - **Duplicate Transaction Guard:** Flags same-day identical transactions to prevent accidental double-logging.
  - 1-click clipboard paste and sample test notification chips.

---

### 6. 🎯 AI Financial Health Score & 5-Pillar Spider Radar
- **Endpoints:** `GET /api/v1/ai/health-score`
- **Backend Service:** `calculate_financial_health_score()` in `backend/app/services/ai_service.py`
- **Frontend Component:** `frontend/components/dashboard/FinancialHealthCard.tsx`
- **Placement:** Mounted directly on `frontend/app/dashboard/page.tsx` above KPI cards.
- **Description:** Holistic FICO-style 0–100 financial health scorecard evaluating overall financial discipline across 5 weighted dimensions.
- **Key Capabilities:**
  - **5 Weighted Pillars:**
    1. *Savings Discipline (25%):* Net savings rate vs. 20% benchmark.
    2. *Budget Adherence (25%):* % of active budgets respected under 100%.
    3. *Burn Stability (20%):* Daily burn rate vs. ideal burn allowance.
    4. *Cash Flow Cushion (15%):* Net cash surplus relative to total living costs.
    5. *Leak Control (15%):* Recurring subscriptions and micro-spending ratio.
  - **Prestige Tiers:** `Elite Wealth Builder` (90–100), `Financially Stable` (75–89), `High Burn / Vulnerable` (60–74), `Critical Deficit Alert` (< 60).
  - **Spider Radar Chart:** Visualized with interactive Recharts radar chart mapping strengths and weaknesses.
  - **3 Prioritized Score Boosters:** Actionable steps displaying predicted score point jumps (e.g. `+8 PTS`).
  - Zero-fail client-side mathematical fallback for offline and loading states.

---

### 7. 🏆 Smart Goals & AI Savings Runway Engine
- **Endpoints:**
  - `GET /api/v1/goals` — List user goals with live runway forecast
  - `POST /api/v1/goals` — Create new savings goal
  - `GET /api/v1/goals/{id}` — Get single goal detail
  - `PATCH /api/v1/goals/{id}` — Update goal
  - `POST /api/v1/goals/{id}/contribute` — Deposit or withdraw funds
  - `DELETE /api/v1/goals/{id}` — Delete goal
  - `GET /api/v1/ai/goals-runway` — Multi-goal surplus allocation analysis
- **Backend Service:** `GoalService` & `analyze_goals_runway()` in `ai_service.py`
- **Database Model:** `Goal` in `backend/app/models/goal.py` (Alembic migration `b2c3d4e5f6a7`)
- **Frontend Page & Modals:**
  - Dedicated `/goals` management page (`frontend/app/goals/page.tsx`).
  - `GoalFormModal.tsx` for goal creation & editing with deadlines and custom color themes.
  - `GoalContributeModal.tsx` for quick deposits/withdrawals with dynamic progress forecasts.
  - "Goals" (`Target` icon) in primary navigation (`Navbar.tsx`).
- **Description:** Connects daily spending discipline with aspirational dreams (emergency funds, travel, gadgets, vehicle down payments).
- **Key Capabilities:**
  - **Runway Pacing Status:** Evaluates monthly funding requirements against net cash flow (`Income - Expenses`) to assign real-time badges (`On Track`, `Ahead`, `At Risk`, `Behind`, `Completed`).
  - **Multi-Goal Funding Coverage:** Displays total required monthly savings and whether live cash flow fully funds all active goals.
  - **AI Acceleration Trade-Offs:** Recommends specific discretionary cuts (e.g., *"Trimming 30% from Dining saves ₹1,500/mo, closing 45% of your funding gap"*).

---

## 🔒 Free-Tier & Multi-Tenancy Architecture
- **Zero Cloud Cost:** All 7 features operate within free-tier limits (concise prompt tokens < 600) with 100% deterministic fallback engines that require zero external API credits.
- **Zero-Trust Multi-Tenancy:** Every repository query and AI context payload strictly filters by `current_user.id`. User data is 100% isolated.
