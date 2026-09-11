# Spendora — Product Requirements Document (PRD)
## Smart Personal Expense, Budget & AI Financial Copilot

---

## 1. Executive Summary & Vision

**Product Name:** Spendora  
**Product Type:** Smart Personal Finance & AI-Powered Money Management Platform (Web & Native Android Mobile App)  
**Primary Currency:** Indian Rupee (₹ / INR)

### 1.1 Product Summary
Spendora is a modern, intuitive personal finance platform designed to take the stress, confusion, and manual effort out of managing personal money. It allows individuals to effortlessly record expenses and incomes, organize spending into personalized categories, set flexible budgets across any time period (daily, weekly, monthly, yearly), and track meaningful savings goals. 

Beyond basic record-keeping, Spendora acts as a **proactive AI financial copilot** — alerting users before they overspend, auditing hidden recurring subscriptions, calculating a single daily "Safe-to-Spend" number, analyzing financial health across 5 core pillars, reading receipts and bank SMS messages, and answering money questions in natural, friendly language.

### 1.2 The Core User Journey
Spendora centers around a fast, rewarding, and continuous financial feedback loop:

$$\text{Log Activity / Scan Receipt} \longrightarrow \text{Instant Visual Breakdown} \longrightarrow \text{Real-Time Budget & Safe-to-Spend Signals} \longrightarrow \text{AI Guidance & Goal Milestones} \longrightarrow \text{Financial Freedom}$$

### 1.3 Core Product Philosophy
- **Effortless Speed:** Adding an expense or scanning a bill takes seconds. Daily money tracking should never feel like homework.
- **Crystal Clear Simplicity:** No confusing banking jargon, hidden menus, or complex accounting equations. Everything is explained in plain, actionable language.
- **Privacy & Ownership:** Financial data belongs strictly to the user. Every user has a private, locked financial space with zero data sharing and optional biometric vault protection.
- **Proactive, Not Passive:** Traditional apps only show where money went after it is gone. Spendora warns users *before* they overspend and helps them safely budget for life goals.
- **100% Real Data:** Every chart, total, and AI recommendation is grounded in real transactions — never placeholder or simulated numbers.

---

## 2. Real-World Problems Spendora Solves

| Everyday Problem | How Spendora Solves It |
|---|---|
| **"Where did all my money go this month?"** | Instantly visualizes spending patterns through dynamic donut charts, category rankings, and month-over-month comparisons. |
| **"Can I afford this impulse purchase right now?"** | The **Purchase Simulator** tests potential purchases against upcoming bills and remaining budgets, giving a clear Green / Yellow / Red verdict. |
| **"Budgeting by month is too abstract and hard to follow."** | The **Safe-to-Spend Speedometer** breaks down monthly budgets into a single, rolling daily spending allowance that automatically adjusts. |
| **"Small, forgotten daily purchases add up without noticing."** | The **Autonomous Leak Hunter** uncovers recurring digital subscriptions and micro-spending leaks, projecting their annual cost. |
| **"Manual data entry is tedious and easily forgotten."** | The **Smart Transaction Scanner** reads paper receipts, bills, and bank payment SMS messages with instant 1-tap logging. |
| **"I want to save for a vacation/emergency fund but don't know how much to set aside."** | **Smart Savings Goals** track goal progress, project completion dates, and suggest realistic category trims to reach goals faster. |
| **"Finance apps feel cold, stressful, and complicated."** | A sleek, customizable dark/light theme, interactive conversational AI advisor, and celebratory milestone achievements. |

---

## 3. Target Audience

Spendora is tailored for anyone who wants clarity, control, and peace of mind over their personal finances:

1. **Young Professionals & Freelancers:** Managing irregular income streams, multiple payment modes (UPI, cards, cash), and aiming to build healthy savings habits.
2. **Students & Young Adults:** Tracking daily allowances, shared college expenses, and learning disciplined budgeting for the first time.
3. **Goal-Oriented Savers:** Saving for milestone purchases (vacations, gadgets, vehicles, emergency funds) and wanting clear visual timelines.
4. **Busy Everyday Individuals:** Needing an ultra-fast way to record transactions via receipts and SMS without typing out details manually.

*Note: Spendora is focused exclusively on personal money management and is not intended for corporate bookkeeping, multi-user business payroll, or stock trading.*

---

## 4. Key Product Features & Capabilities

```
Spendora Platform
├── 1. Account & Vault Security
│   ├── Email & Password Registration with 4-Digit OTP Recovery
│   ├── One-Tap Google Sign-In
│   ├── Hardware Biometric Lock (Fingerprint / Face Unlock)
│   └── Multi-Device Session Control
├── 2. Core Money Tracking & Cash Flow
│   ├── Expense Tracking (with Multi-Filters, Search & Sorting)
│   ├── Income Tracking & Source Breakdown
│   ├── Multi-Account Ledger Rail (UPI, Cards, Bank, Cash)
│   └── Consolidated Liquidity Hero Card (with Balance Privacy Mask)
├── 3. Multi-Period Budgets & Smart Goals
│   ├── Daily, Weekly, Monthly, and Yearly Budget Limits
│   ├── Live Over-Budget & Near-Limit Alerts
│   └── Milestone Savings Goals (Deposit/Withdraw & Timeline Forecasts)
├── 4. Spendora 7-Feature AI Intelligence Suite
│   ├── 1. "Can I Afford This?" Purchase Simulator
│   ├── 2. Autonomous Leak Hunter & Subscription Audit
│   ├── 3. Smart "Safe-to-Spend" Burn Speedometer
│   ├── 4. Natural Language Financial Chatbot & Advisor
│   ├── 5. Smart Receipt OCR & Bank SMS Parser
│   ├── 6. 5-Pillar Financial Health Radar (0-100 Score)
│   └── 7. Goal Runway Acceleration Engine
└── 5. Statements, Analytics & Visual Design
    ├── Monthly Financial Statement Summary & One-Tap CSV Export
    ├── Spending Clusters & Category Drill-Downs
    └── Dynamic Light & Dark Theme Customization
```

---

### 4.1 Account, Identity & Vault Security
- **Simple & Frictionless Sign-In:** Users can create an account using their email and password or sign in with a single tap via Google.
- **Fast 4-Digit OTP Password Reset:** If a password is forgotten, Spendora delivers a secure 4-digit code directly to the user's email with a live countdown timer for effortless account recovery.
- **Native Biometric Vault:** On mobile devices, users can lock Spendora behind their phone's fingerprint or face recognition sensor to ensure financial privacy when handing their phone to friends or family.
- **Privacy Mask Toggle:** Users can hide their total balance and sensitive numbers on the dashboard with a single tap (`••••••••`), keeping their screen private in public spaces.
- **Multi-Device Control:** Users can view active sessions and log out of all devices remotely if a device is ever lost or replaced.

---

### 4.2 Core Money Tracking & Cash Flow Management
- **Fast Expense Logging:** Record an expense in under 10 seconds with a title, amount, category, date, payment method, and optional notes.
- **Income Tracking:** Log all earnings and salary deposits to maintain a complete picture of total inflows versus outflows.
- **Net Cash Flow Analytics:** The dashboard automatically calculates **Net Cash Flow** ($\text{Income} - \text{Expenses}$) and live **Savings Rate %**, clearly indicating if the user is operating at a surplus or deficit.
- **Multi-Account Ledger Rail:** Categorize money across realistic daily payment methods:
  - ⚡ **UPI & Online Wallets** (GPay, PhonePe, Paytm)
  - 💳 **Cards & Bank Accounts** (Debit/Credit Cards, Net Banking)
  - 💵 **Cash Reserve** (Physical Cash)
  - 🌐 **All Ledgers** (Consolidated view)
- **Advanced Search & Multi-Filters:** Instantly find past transactions by searching keywords, picking date ranges (today, this week, this month, custom), isolating specific categories, or sorting by amount.
- **Flexible Category Management:** Comes with intuitive default categories (Food, Transport, Housing, Shopping, Entertainment, Healthcare, Bills, Education, Other) and allows users to create, edit, or safely reassign custom categories.

---

### 4.3 Multi-Period Budgets & Smart Savings Goals
- **Flexible Budget Timeframes:** Users can set spending guardrails tailored to how they actually live:
  - **Daily Limits:** Great for controlling daily food, coffee, or commute expenses.
  - **Weekly Limits:** Ideal for managing weekend leisure and grocery runs.
  - **Monthly Limits:** Standard monthly caps for general lifestyle and living costs.
  - **Yearly Limits:** Perfect for annual vacations, insurance premiums, or festive shopping.
- **Real-Time Visual Alerts:** Color-coded status badges update instantly with every transaction:
  - 🟢 **On Track:** Under 80% of budget spent.
  - 🟡 **Near Limit:** Between 80% and 100% of budget spent.
  - 🔴 **Over Budget:** Exceeded the set limit with instant warning popups.
- **Target Savings Goals:** Set meaningful life targets (e.g., *"Emergency Fund ₹1,00,000"*, *"Goa Trip ₹25,000"*).
- **Deposit & Withdraw Flow:** Log direct contributions toward goals and track percentage completion with celebratory progress bars.

---

### 4.4 The 7-Feature Spendora AI Intelligence Suite

#### 1. "Can I Afford This?" Purchase Decision Simulator
Before spending money on a non-essential or large item (e.g., *"New Headphones ₹8,500"*), users can test the purchase in the simulator. Spendora checks remaining cash flow, active category budgets, and upcoming expenses to deliver an immediate, honest recommendation:
- 🟢 **Safe to Buy:** You have ample surplus; your savings goals remain 100% on track.
- 🟡 **Proceed with Caution:** You can afford this if you reduce spending in other discretionary areas this week.
- 🔴 **Delay Purchase:** This will put you into a budget deficit; suggests a better date or savings plan.

#### 2. Autonomous "Leak Hunter" & Subscription Audit
Spendora automatically reviews transaction history over the last 90 days to find invisible money leaks:
- Detects recurring digital subscriptions (Netflix, Spotify, Gym, iCloud, Amazon Prime).
- Aggregates daily micro-spending (e.g., snacks, tea, convenience fees) and reveals their cumulative annual drain (e.g., *"Your daily ₹60 chai spend equals ₹21,900 per year"*).
- Provides an interactive checklist of recommended subscriptions to pause or cancel.

#### 3. Smart "Safe-to-Spend" Real-Time Burn Speedometer
Instead of guessing how much money is left for the month, Spendora provides a single, live daily number:
- Displays a speedometer gauge indicating **Burn Velocity** (Optimal, Moderate, Danger).
- Calculates your **Daily Safe Allowance** ($\text{Remaining Buffer} \div \text{Days Left in Month}$).
- If you spend less today, tomorrow's safe limit goes up; if you spend more, tomorrow adjusts automatically so you still end the month in positive surplus.

#### 4. Natural Language Financial Chatbot & Conversational Advisor
Users can ask real questions in everyday language and receive instant, personalized answers grounded in their real financial data:
- *"How much did I spend on dining out this week?"*
- *"Am I saving enough for my emergency fund?"*
- *"Give me a summary of my finances this month."*
- Includes quick starter suggestion chips, clean structured response tables, and one-tap action triggers.

#### 5. Smart Receipt & Bank SMS Transaction Scanner
Users can skip manual form typing entirely:
- **Receipt & Bill OCR:** Upload a photo of a restaurant receipt, store invoice, or paper bill. Spendora automatically detects the vendor name, total amount, and date.
- **Bank SMS Parser:** Paste incoming bank SMS alerts (HDFC, SBI, ICICI, Axis, Kotak, GPay, PhonePe, Paytm). Spendora instantly extracts the amount, merchant, and payment mode with **automatic privacy masking** (card/account numbers and sensitive balance info are never stored).

#### 6. 5-Pillar Financial Health Score Radar
Spendora evaluates overall financial stability using an objective 0–100 composite prestige score displayed on an interactive spider radar chart across 5 core pillars:
1. **Savings Discipline:** Consistency in generating monthly net cash flow.
2. **Budget Adherence:** Staying within set category and overall budget caps.
3. **Burn Stability:** Maintaining a steady, predictable daily spending pace.
4. **Cash Cushion:** Having liquid reserves to handle unexpected expenses.
5. **Leak Control:** Keeping recurring subscriptions and micro-spending low.
*Includes 3 customized "Score Booster Actions" prioritizing the highest-impact steps to improve your score.*

#### 7. Smart Goals Runway & Accelerated Completion Engine
Spendora continuously compares monthly surplus against active savings goals:
- Predicts the exact month and year each goal will be fully funded based on current saving speed.
- Suggests smart category trade-offs (e.g., *"Reduce Food Delivery by ₹1,200/month to achieve your Laptop Goal 2 months earlier"*).

---

### 4.5 Financial Reports, Exports & Visual Customization
- **Monthly Financial Statements:** View an executive summary of total inflows, total outflows, net savings, and category distribution for any selected month.
- **One-Tap CSV Data Export:** Download comprehensive, spreadsheet-ready CSV statements for both expenses and income to share with accountants or keep offline backups.
- **Dynamic Light & Dark Themes:** Seamlessly switch between a sleek Midnight Obsidian dark mode (designed for low eye strain) and a clean, high-contrast light mode.
- **Interactive Spending Clusters:** Tap any category segment directly from the dashboard to open an itemized breakdown of recent expenses in that category.

---

## 5. Non-Technical User Experience & Screen Overview

| Screen / Area | Purpose & What the User Sees |
|---|---|
| **Overview Dashboard** | The primary command center. Features the Consolidated Liquidity hero card (with balance hide toggle), quick action pods (`+ Expense`, `+ Income`, `Scan Bill`, `Simulate`), dynamic account rail, live Safe-to-Spend speedometer, Recent Activity ledger, and Spending Clusters breakdown. |
| **Expenses & Ledger** | Comprehensive list of all past transactions. Users can search by keyword, filter by custom date ranges or categories, sort by highest/lowest amounts, and view/edit/delete individual entries with full confirmation. |
| **Incomes & Cash Flow** | Dedicated income tracker displaying all salary and earning sources, monthly total inflows, and historical earning records. |
| **Budgets & Guardrails** | Tabbed multi-period manager (Daily, Weekly, Monthly, Yearly). Shows visual progress bars, remaining budget balances, over-budget warnings, and fast edit/delete options. |
| **Savings Goals & Vault** | Visual savings roadmap showing active milestones, progress bars, deposit/withdraw contribution dialogs, and AI runway forecasts. |
| **AI Assistant Sheet** | Floating conversational drawer for chatting with Spendora, receiving tailored financial insights, and running instant smart simulations. |
| **Notifications Drawer** | Dedicated alert tray notifying users of high burn velocity, subscription renewals, budget limit warnings, and positive savings milestones. |
| **Biometric Vault Overlay** | Full-screen security lock that requests fingerprint or face verification before revealing sensitive financial balances. |

---

## 6. Privacy, Security & Data Principles

1. **Zero-Trust User Data Isolation:** Every user's data is strictly partitioned. No user can ever view, search, or interact with another user's financial records.
2. **Bank-Grade Password Protection:** All passwords and authentication credentials are encrypted using modern cryptographic standards; plain-text passwords are never stored or visible to anyone.
3. **Confidential AI Interactions:** Financial questions and transaction data analyzed by the AI engine remain private and are strictly used to generate the user's personal insights.
4. **Automatic PII Scrubbing on Scans:** When scanning bank SMS messages, account numbers, debit card digits, and bank balances are automatically scrubbed and discarded before transaction extraction.
5. **No Fake or Simulated Data:** Spendora never generates fictitious data or hardcoded placeholder balances. If an account is new, it warmly guides the user to record their first real transaction.
6. **Graceful Error Prevention:** Destructive actions (deleting expenses, removing categories, resetting budgets) always require clear user confirmation, ensuring no accidental data loss.

---

## 7. Success Criteria & Impact Goals

- **Daily Logging Speed:** Adding an expense or scanning a bill takes **less than 15 seconds**.
- **Financial Awareness:** 100% of active users can answer *"How much can I safely spend today?"* within 3 seconds of opening the app.
- **Budget Protection:** Over 80% of budget-setting users stay within their monthly spending limits by following Safe-to-Spend and simulator recommendations.
- **Savings Acceleration:** Users with active savings goals achieve their milestones faster by acting on AI score booster recommendations.
- **Frictionless Habit Building:** Users return consistently throughout the week due to fast, responsive, and stress-free financial feedback.

---

## 8. Future Horizon & Product Roadmap

- **Phase A (Current V1 — Completed):** Full Web & Native Android App, Multi-Period Budgets, Income & Cash Flow, 7-Feature AI Suite, Biometric Vault, CSV Statements, Dynamic Themes, and In-Database Financial Assistant.
- **Phase B (Upcoming Enhancements):**
  - Interactive PDF statement exports with visual pie charts and graphs.
  - Split-expense tracking for roommates and group dinners.
  - Recurring transaction scheduling (automated auto-pay reminders).
  - Multi-currency support for international travel and remote earnings.
- **Phase C (Long-Term Vision):**
  - Family & household shared budgeting spaces with customizable permission controls.
  - Automated smart savings sweeps to partner high-yield savings accounts.
