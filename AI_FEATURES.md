# 🤖 Spendora AI — Core Intelligent Financial Features

A concise overview of the 5 AI-powered financial intelligence features built into Spendora.

---

### 1. 🛒 "Can I Afford This?" — Purchase Decision Simulator
- **Endpoint:** `POST /api/v1/ai/simulate-purchase`
- **Description:** Simulates the real-time financial impact of any prospective purchase against monthly cash flow, category budgets, and savings goals. Provides an instant 3-tier verdict (`Safe`, `Caution`, `Over-Budget`) with before/after metric comparisons and direct 1-click "Add as Expense" logging.

---

### 2. 🔍 Autonomous "Leak Hunter" & Subscription Audit
- **Endpoint:** `GET /api/v1/ai/leak-analysis`
- **Description:** Scans 90 days of transaction history to detect recurring recurring digital subscriptions (Netflix, Spotify, Gym) and silent micro-spending leaks (<= ₹150). Computes annualized drain projections and generates actionable AI cancellation tips.

---

### 3. ⏱️ Smart "Safe-to-Spend" Speedometer & Burn Forecaster
- **Endpoint:** `GET /api/v1/ai/safe-to-spend`
- **Description:** Real-time speedometer dashboard widget that computes the user's daily safe burn allowance (`Remaining Buffer / Days Remaining`), spending velocity pace (`Optimal`, `Warning`, `Danger`), month-end net savings trajectory, and day-of-depletion forecast.

---

### 4. 💬 Natural Language Financial Assistant & Conversational Chatbot
- **Endpoint:** `POST /api/v1/ai/chat`
- **Description:** Floating glassmorphic conversational advisor connected to live financial telemetry (income, spending, active budgets, top categories, and burn rate). Supports natural language questions ("Can I afford dinner?", "Where did my money go?"), rich Markdown answers, suggested quick prompts, and contextual action buttons.

---

### 5. 🧾 Smart Receipt & UPI SMS Parser
- **Endpoint:** `POST /api/v1/ai/extract-transaction`
- **Description:** Eliminates manual data entry by extracting merchant, amount, date, payment mode, and category from raw Indian bank/UPI SMS notifications or paper receipt images. Includes automated PII scrubbing (masks account/card numbers and balances), debit vs. credit auto-classification, and duplicate transaction guarding.
