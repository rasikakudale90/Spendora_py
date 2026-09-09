# Spendora Mobile — Frontend Requirements Specification (FRS)

*A comprehensive technical and design specification for the Native Android Application based on the Spendora backend architecture and Stitch MCP design system.*

---

## 1. Executive Summary & Architecture Overview

Spendora is a high-performance personal wealth and expense intelligence platform. The mobile frontend interacts with a decoupled FastAPI asynchronous backend powered by PostgreSQL, JWT authentication, and a multi-provider AI engine (Gemini, OpenAI, Claude, Groq, and mathematical fallback).

```
┌────────────────────────────────────────────────────────────────────────┐
│                   Spendora Android Native App                          │
│           (Jetpack Compose + Material 3 + Retrofit 2 + Coroutines)     │
└───────────────────────────────────┬────────────────────────────────────┘
                                    │ HTTPS / REST (Port 443 / 8000)
                                    ▼
┌────────────────────────────────────────────────────────────────────────┐
│                        FastAPI Backend Engine                          │
│  ┌───────────────┬────────────────┬───────────────┬─────────────────┐  │
│  │ Auth & OAuth  │ Core Financial │ Multi-Period  │  7-Feature AI   │  │
│  │ (JWT + 50s OTP│ (Expenses,     │ Budgets &     │  Intelligence   │  │
│  │  + Google)    │  Incomes)      │ Goals Runway  │  (RAG + NLP)    │  │
│  └───────┬───────┴────────┬───────┴───────┬───────┴────────┬────────┘  │
│          │                │               │                │           │
│          ▼                ▼               ▼                ▼           │
│  ┌──────────────────────────────────────────────────────────────────┐  │
│  │             PostgreSQL Database (Supabase Session Pooler)        │  │
│  └──────────────────────────────────────────────────────────────────┘  │
└────────────────────────────────────────────────────────────────────────┘
```

---

## 2. Domain Model & Entity Dictionary

| Entity | Primary Key | Attributes & Constraints | Relationships |
| :--- | :--- | :--- | :--- |
| **`User`** | `id` (UUID) | `email` (Unique, indexed), `hashed_password` (Nullable for OAuth), `full_name` (String), `avatar_url` (String), `auth_provider` (`"local"` / `"google"`), `is_active` (Boolean) | Owns Expenses, Incomes, Budgets, Goals, Categories, RefreshTokens |
| **`Category`** | `id` (UUID) | `name` (1–50 chars), `color` (Hex `#RRGGBB`), `icon` (String icon key), `is_system` (Boolean), `user_id` (UUID, null for system presets) | Referenced by Expenses and Budgets |
| **`Expense`** | `id` (UUID) | `title` (1–100 chars), `amount` (Decimal > 0), `expense_date` (Date ≤ today), `payment_mode` (`"Cash"`, `"Card"`, `"UPI"`, `"Net Banking"`, `"Other"`), `notes` (Optional String) | Belongs to `User` and `Category` |
| **`Income`** | `id` (UUID) | `title` (1–100 chars), `amount` (Decimal > 0), `income_date` (Date ≤ today), `source` (`"Salary"`, `"Freelance"`, `"Investment"`, `"Business"`, `"Gift"`, `"Other"`), `notes` (Optional String) | Belongs to `User` |
| **`Budget`** | `id` (UUID) | `scope` (`"overall"` / `"category"`), `category_id` (Nullable UUID), `amount` (Decimal > 0), `period_type` (`"daily"`, `"weekly"`, `"monthly"`, `"yearly"`), `period_start` (Date), `period_end` (Date) | Belongs to `User` and optional `Category` |
| **`Goal`** | `id` (UUID) | `name` (1–100 chars), `target_amount` (Decimal > 0), `current_amount` (Decimal ≥ 0), `target_date` (Optional Date), `category` (String), `color` (String token), `status` (`"active"`, `"completed"`, `"paused"`), `notes` (Optional String) | Belongs to `User` |
| **`RefreshToken`** | `id` (UUID) | `token_hash` (SHA-256), `expires_at` (DateTime, 30 days), `revoked` (Boolean), `replaced_by` (Optional UUID), `user_agent` (String), `ip_address` (String) | Belongs to `User` |
| **`PasswordResetToken`** | `id` (UUID) | `otp_hash` (Salted SHA-256 of 4-digit code), `expires_at` (DateTime, 50 seconds), `used` (Boolean) | Belongs to `User` |

---

## 3. Authentication & Security Engine

```
       ┌───────────────── User Logs In / Refreshes ─────────────────┐
       ▼                                                            ▼
┌──────────────┐                                            ┌──────────────┐
│ Access Token │ (JWT Bearer, 15-min validity)              │RefreshToken  │ (30-day validity,
│ (Memory/RAM) │ Attached in `Authorization: Bearer <token>`│ (Secure Store│  Automatic Rotation
└──────────────┘                                            └──────┬───────┘  on 401 response)
                                                                   │
                                                                   ▼
                                                            ┌──────────────┐
                                                            │ Rotation &   │ If reused -> Revoke
                                                            │ Reuse Guard  │ all user tokens
                                                            └──────────────┘
```

### 3.1 Session Lifecycle Rules
1. **Access Token:** Short-lived JWT (15 minutes). Attached to HTTP requests via header `Authorization: Bearer <access_token>`.
2. **Refresh Token:** Long-lived credential (30 days) stored securely via Android `EncryptedSharedPreferences` / `MasterKeys` (AES-256-GCM).
3. **Automatic 401 Interceptor:** When an API call returns `HTTP 401 Unauthorized`, an OkHttp `Authenticator` transparently invokes `POST /api/v1/auth/refresh`, rotates both tokens, and retries the original failed call without user disruption.
4. **4-Digit OTP Recovery:** Password recovery generates a 4-digit numeric code (`1000`–`9999`) with a **strict 50-second expiry window**. Prior codes are automatically invalidated.

---

## 4. API Endpoints & Contract Specification

### 4.1 Authentication Endpoints (`/api/v1/auth`)

| Method | Endpoint | Request Body | Success Response | Error Codes |
| :--- | :--- | :--- | :--- | :--- |
| `POST` | `/api/v1/auth/register` | `{ email, password, full_name }` | `201 Created` → `{ message, user: { id, email, full_name, auth_provider, is_active } }` | `400` (Email exists), `422` (Validation) |
| `POST` | `/api/v1/auth/login` | `{ email, password }` | `200 OK` → `{ access_token, token_type, expires_in, user }` (Sets cookie/refresh token) | `401` (Invalid credentials), `429` (Rate limit) |
| `POST` | `/api/v1/auth/google` | `{ credential }` (Google ID Token) | `200 OK` → `{ access_token, token_type, expires_in, user }` | `400` (Invalid Google token) |
| `POST` | `/api/v1/auth/refresh` | Cookie or `{ refresh_token }` | `200 OK` → `{ access_token, token_type, expires_in, user }` | `401` (Expired/Revoked token) |
| `POST` | `/api/v1/auth/logout` | Empty (Cookie/Header) | `200 OK` → `{ message: "Logged out successfully" }` | `200` (Idempotent) |
| `POST` | `/api/v1/auth/logout-all`| Auth Bearer | `200 OK` → `{ message: "Logged out of all devices successfully" }` | `401` (Unauthorized) |
| `GET` | `/api/v1/auth/me` | Auth Bearer | `200 OK` → `UserResponse` (`id`, `email`, `full_name`, `avatar_url`, `auth_provider`) | `401` (Unauthorized) |
| `POST` | `/api/v1/auth/forgot-password` | `{ email }` | `200 OK` → `{ message: "If this email is registered, a 4-digit verification code has been sent." }` | `429` (5 req/min limiter) |
| `POST` | `/api/v1/auth/verify-otp` | `{ email, otp: "1234" }` | `200 OK` → `{ valid: true, message: "Verification code is valid." }` | `400` (Invalid/Expired OTP) |
| `POST` | `/api/v1/auth/reset-password` | `{ email, otp: "1234", new_password }` | `200 OK` → `{ message: "Password reset successfully. You can now log in..." }` | `400` (Invalid/Expired OTP) |
| `POST` | `/api/v1/auth/change-password` | `{ current_password, new_password }` | `200 OK` → `{ message: "Password changed successfully" }` | `400` (Wrong current password) |

---

### 4.2 Dashboard & Analytics Endpoints (`/api/v1/dashboard`)

| Method | Endpoint | Query Parameters | Response Payload Description |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/v1/dashboard/summary` | `period_month` (`YYYY-MM` or `YYYY-MM-DD`) | Total spend, total income, net savings (cash flow), savings rate %, remaining budget, budget status (`"on_track"`, `"near_limit"`, `"over_budget"`), daily budget alert if breached |
| `GET` | `/api/v1/dashboard/recent-expenses` | `limit` (default: 5, max: 50) | List of recent `ExpenseResponse` items |
| `GET` | `/api/v1/dashboard/category-breakdown`| `period_month` | List of categories with total spent, percentage of total spend, color, icon |
| `GET` | `/api/v1/dashboard/trend` | `period_month` | Time-series data points (`date`, `amount`) for trend charts |
| `GET` | `/api/v1/dashboard/comparison` | `period_month` | Current month spend vs previous month spend, percentage change, direction (`"increased"` / `"decreased"`) |
| `GET` | `/api/v1/dashboard/top-categories` | `period_month`, `limit` (default: 5) | Top ranked spending categories with amounts and transaction counts |
| `GET` | `/api/v1/dashboard/stats` | `period_month` | Average daily spend, average weekly spend, highest single expense, total transaction count |

---

### 4.3 Expenses Endpoints (`/api/v1/expenses`)

| Method | Endpoint | Query / Body | Response Payload |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/v1/expenses` | `search`, `date_from`, `date_to`, `category_id`, `min_amount`, `max_amount`, `payment_mode`, `sort_by` (`expense_date`, `amount`, `title`, `created_at`), `sort_order` (`asc`, `desc`), `page`, `page_size` | `{ items: [ExpenseResponse], total: int, page: int, page_size: int, total_pages: int, total_amount: Decimal }` |
| `POST` | `/api/v1/expenses` | `{ title, amount, expense_date, category_id, payment_mode, notes }` | `201 Created` → `ExpenseResponse` (Contains `id`, `category_name`, `category_color`, `category_icon`) |
| `GET` | `/api/v1/expenses/{id}` | Path `id` (UUID) | Single `ExpenseResponse` |
| `PATCH` / `PUT` | `/api/v1/expenses/{id}` | `{ title?, amount?, expense_date?, category_id?, payment_mode?, notes? }` | Updated `ExpenseResponse` |
| `DELETE` | `/api/v1/expenses/{id}` | Path `id` (UUID) | `{ message: "Expense deleted successfully" }` |

---

### 4.4 Income Endpoints (`/api/v1/incomes`)

| Method | Endpoint | Query / Body | Response Payload |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/v1/incomes` | `search`, `date_from`, `date_to`, `source`, `min_amount`, `max_amount`, `sort_by`, `sort_order`, `page`, `page_size` | `{ items: [IncomeResponse], total: int, page: int, page_size: int, total_pages: int, total_amount: Decimal }` |
| `GET` | `/api/v1/incomes/summary` | `period_month` | `{ total_income: Decimal, period_month: str, source_breakdown: [ { source, total_amount, count, percentage } ] }` |
| `POST` | `/api/v1/incomes` | `{ title, amount, income_date, source, notes }` | `201 Created` → `IncomeResponse` |
| `GET` | `/api/v1/incomes/{id}` | Path `id` (UUID) | Single `IncomeResponse` |
| `PATCH` / `PUT` | `/api/v1/incomes/{id}` | `{ title?, amount?, income_date?, source?, notes? }` | Updated `IncomeResponse` |
| `DELETE` | `/api/v1/incomes/{id}` | Path `id` (UUID) | `{ message: "Income deleted successfully" }` |

---

### 4.5 Budgets Endpoints (`/api/v1/budgets`)

| Method | Endpoint | Query / Body | Response Payload |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/v1/budgets` | `period_date` (Date), `period_type` (`"daily"`, `"weekly"`, `"monthly"`, `"yearly"`) | `{ overall_budget: BudgetResponse?, category_budgets: [BudgetResponse], period_type, period_start, period_end }` |
| `POST` | `/api/v1/budgets` | `{ scope: "overall"|"category", category_id?, amount, period_type, period_start }` | Created / Updated `BudgetResponse` with live `spent`, `remaining` (clamped to ₹0.00 on breach), `percentage_used`, `status` |
| `PATCH` | `/api/v1/budgets/{id}` | `{ amount }` | Updated `BudgetResponse` |
| `DELETE` | `/api/v1/budgets/{id}` | Path `id` (UUID) | `{ message: "Budget deleted successfully" }` |

---

### 4.6 Savings Goals Endpoints (`/api/v1/goals`)

| Method | Endpoint | Query / Body | Response Payload |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/v1/goals` | `status` (`"active"`, `"completed"`, `"paused"`, `"all"`) | `{ items: [GoalResponse], total_goals: int, active_goals: int, completed_goals: int, total_target_amount: Decimal, total_saved_amount: Decimal }` *(Includes computed live AI runway forecasting for each goal)* |
| `POST` | `/api/v1/goals` | `{ name, target_amount, current_amount?, target_date?, category, color, notes? }` | `201 Created` → `GoalResponse` |
| `GET` | `/api/v1/goals/{id}` | Path `id` (UUID) | Single `GoalResponse` with live runway metrics |
| `PATCH` | `/api/v1/goals/{id}` | `{ name?, target_amount?, target_date?, category?, color?, notes?, status? }` | Updated `GoalResponse` |
| `POST` | `/api/v1/goals/{id}/contribute` | `{ amount: Decimal, operation: "deposit"|"withdraw", notes? }` | `200 OK` → Updated `GoalResponse` with recomputed progress % and remaining balance |
| `DELETE` | `/api/v1/goals/{id}` | Path `id` (UUID) | `204 No Content` |

---

### 4.7 Categories Endpoints (`/api/v1/categories`)

| Method | Endpoint | Query / Body | Response Payload |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/v1/categories` | None | List of `CategoryWithCountResponse` (System presets + Custom categories with expense counts) |
| `POST` | `/api/v1/categories` | `{ name, color, icon }` | `201 Created` → `CategoryResponse` |
| `GET` | `/api/v1/categories/{id}` | Path `id` (UUID) | Single `CategoryResponse` |
| `PATCH` / `PUT` | `/api/v1/categories/{id}` | `{ name?, color?, icon? }` | Updated `CategoryResponse` (System presets protected) |
| `DELETE` | `/api/v1/categories/{id}` | `reassign_to` (Optional Target UUID) | `{ message: "Category deleted successfully" }` *(Returns 409 Conflict if category has expenses and `reassign_to` is omitted)* |

---

## 5. The 7-Feature Spendora AI Financial Intelligence Suite

```
┌────────────────────────────────────────────────────────────────────────┐
│                   Spendora 7-Feature AI Intelligence                   │
├────────────────────────────┬───────────────────────────────────────────┤
│ 1. Purchase Simulator      │ Decision engine: "Can I Afford This?"     │
│ 2. Leak Hunter & Audits    │ 90-day subscription & micro-leak scanner  │
│ 3. Safe-to-Spend Speedometer│ Daily burn rate & month-end forecaster   │
│ 4. Conversational Advisor  │ In-Database RAG natural language assistant│
│ 5. Smart Transaction Intake│ OCR receipt & Indian banking SMS regex    │
│ 6. Financial Health Radar  │ 5-Pillar Spider Radar (0-100 score)       │
│ 7. Smart Goals Runway      │ Multi-goal runway & surplus allocation    │
└────────────────────────────┴───────────────────────────────────────────┘
```

### Feature 1: "Can I Afford This?" Purchase Decision Simulator
- **Endpoint:** `POST /api/v1/ai/simulate-purchase`
- **Request:** `{ title: "Sony WH-1000XM5", amount: 24999.00, category_id?: UUID }`
- **Response Data:**
  - `verdict`: `"safe"` (Green) / `"caution"` (Amber) / `"over_budget"` (Rose)
  - `verdict_title` & `verdict_summary`
  - `current_cash_flow` vs `projected_cash_flow`
  - `current_savings_rate` vs `projected_savings_rate`
  - `daily_safe_spend_before` vs `daily_safe_spend_after`
  - `ai_analysis`: Markdown textual reasoning
  - `actionable_tips`: Bullet points on trade-offs
- **Mobile UI Pattern:** Bottom Sheet / Modal with 3-metric comparison cards and direct **"Add as Expense"** shortcut.

---

### Feature 2: Autonomous "Leak Hunter" & Subscription Audit
- **Endpoint:** `GET /api/v1/ai/leak-analysis`
- **Response Data:**
  - `total_monthly_leak` & `total_annual_projected_leak`
  - `total_monthly_subscriptions` & `total_annual_subscriptions`
  - `detected_subscriptions`: List of recurring services (`title`, `average_amount`, `occurrence_count`, `last_date`, `estimated_monthly_cost`)
  - `micro_spending_leaks`: List of sub-₹150 habit drains (`category_or_label`, `monthly_total`, `annual_projected_drain`, `example_items`)
  - `actionable_savings_tips`: AI recommendations to plug leaks.
- **Mobile UI Pattern:** Dual-tab Bottom Sheet (Active Subscriptions vs Micro-Leaks) with annualized drain badges.

---

### Feature 3: Smart "Safe-to-Spend" Speedometer & Burn Forecaster
- **Endpoint:** `GET /api/v1/ai/safe-to-spend`
- **Response Data:**
  - `daily_safe_spend`: Max safe spending allowed today without depleting surplus
  - `burn_rate_status`: `"optimal"` (≤85% pace) / `"warning"` (85–105%) / `"danger"` (>105%)
  - `current_burn_rate_per_day`: Actual average spend per day
  - `burn_pace_percentage`: Ratio of actual burn to safe burn
  - `days_remaining_in_month` & `days_passed`
  - `projected_month_end_balance`: Forecasted net savings at month-end
  - `projected_zero_cash_day`: Day of month when cash buffer runs dry (if in danger)
  - `ai_recommendation` & `actionable_tips`
- **Mobile UI Pattern:** Custom Canvas arc speedometer gauge on the Dashboard with instant tap-to-expand details.

---

### Feature 4: Natural Language Financial Assistant (In-Database RAG)
- **Endpoint:** `POST /api/v1/ai/chat`
- **Request:** `{ message: "How much did I spend on Starbucks this month?", history: [...] }`
- **Engine Logic:**
  1. Extracts temporal bounds (this month, last 30 days), category references, and merchant keywords.
  2. Queries user's private SQLite/Postgres database in real time (Zero-cost RAG).
  3. Formulates grounded truth facts and feeds into LLM with fallback deterministic NLP engine.
- **Response Data:**
  - `reply`: Markdown response with tables, lists, and bold telemetry values.
  - `suggested_prompts`: Quick chips for follow-up queries.
  - `action_intent`: Deep link triggers (`simulate_purchase`, `view_leaks`, `set_budget`).
- **Mobile UI Pattern:** Dedicated conversational screen with auto-scrolling chat history, prompt starter chips, and Markdown table rendering.

---

### Feature 5: Smart Receipt OCR & Indian Banking SMS Parser
- **Endpoint:** `POST /api/v1/ai/extract-transaction`
- **Request:**
  - SMS Text Mode: `{ text: "Rs 450.00 spent on HDFC Card xx1234 at SWIGGY on 09-SEP-26. Bal: Rs 12,450", source_type: "sms_text" }`
  - Receipt Image Mode: `{ image_base64: "<base64_data>", source_type: "receipt_image" }`
- **Engine Logic:**
  - Deterministic high-precision regex matching HDFC, SBI, ICICI, Axis, Kotak, GPay, PhonePe, Paytm, and CRED.
  - Automatic PII Scrubbing: Redacts bank account digits, card numbers, and raw balances from AI prompts.
  - Duplicate Transaction Detector: Flags potential duplicates if an identical amount exists on the same date.
- **Response Data:**
  - `type`: `"expense"` or `"income"`
  - `title`, `amount`, `transaction_date`, `category_id`, `category_name`, `payment_mode`
  - `is_potential_duplicate` & `duplicate_warning`
  - `confidence_score` & `sanitized_input`
- **Mobile UI Pattern:** Dual-tab scanner sheet with 1-tap **"Paste from Clipboard"**, sample test chips, review form, and Save button.

---

### Feature 6: Financial Health Score Radar
- **Endpoint:** `GET /api/v1/ai/financial-health`
- **Response Data:**
  - `composite_score`: 0–100 overall score
  - `tier`: `"elite"` (85–100), `"healthy"` (70–84), `"vulnerable"` (50–69), `"critical"` (<50)
  - `tier_title`: E.g. *"Elite Wealth Builder"* / *"Financially Stable"*
  - `pillars`: 5 calibrated dimensions:
    1. **Savings Discipline** (25% weight)
    2. **Budget Adherence** (25% weight)
    3. **Burn Rate Stability** (20% weight)
    4. **Cash Flow Cushion** (15% weight)
    5. **Micro-Leak Control** (15% weight)
  - `score_boosters`: Prioritized list of specific actions with point values (e.g. *"+8 pts: Reduce Dining Out by ₹2,000"*).
- **Mobile UI Pattern:** Custom Canvas 5-axis Spider Radar Chart mounted on the Dashboard with detailed breakdown sheet.

---

### Feature 7: Smart Goals Runway & Acceleration Forecaster
- **Endpoint:** `GET /api/v1/ai/goals-runway`
- **Response Data:**
  - `monthly_surplus`: Current monthly net savings (`Income - Expenses`)
  - `surplus_coverage_pct`: % of required monthly goal savings covered by surplus
  - `is_fully_funded`: True if monthly surplus exceeds total required savings
  - `goals`: Itemized goal runway items with `required_monthly`, `projected_completion_date`, `pacing_status` (`ahead`, `on_track`, `at_risk`, `behind`), `pacing_message`, and `speedup_suggestion`.
  - `discretionary_reduction_tip`: AI suggestion to cut specific category to achieve goals faster.
- **Mobile UI Pattern:** Top banner on `/goals` screen with surplus coverage gauge and individual pacing badges on goal cards.

---

## 6. End-to-End User Journeys

### User Journey 1: Secure Authentication & Google OAuth Sign-In
```
[App Launched] ──▶ Check Encrypted Refresh Token
                   ├── (Found & Valid) ──▶ [Dashboard Screen]
                   └── (None/Expired) ──▶ [Login Screen]
                                            ├── [Google One-Tap] ──▶ Verify with /api/v1/auth/google ──▶ [Dashboard]
                                            ├── [Email & Password] ──▶ /api/v1/auth/login ──▶ [Dashboard]
                                            └── [Create Account] ──▶ [Register Screen] ──▶ /api/v1/auth/register ──▶ [Login Screen]
```

### User Journey 2: 4-Digit OTP Password Recovery (50s Expiry)
```
[Login Screen] ──▶ Tap "Forgot Password?"
                   └──▶ Step 1: Enter Email ──▶ POST /auth/forgot-password ──▶ Triggers 4-digit OTP & 50s Timer
                        └──▶ Step 2: 4-Box OTP Input with Live 50s Countdown Timer
                             ├── (Timer expires) ──▶ Display "Code Expired" badge + "Resend OTP" button
                             └── (Valid code entered) ──▶ Auto-validates via POST /auth/verify-otp
                                  └──▶ Step 3: Enter New Password (with live 5-point strength checklist)
                                       └──▶ POST /auth/reset-password ──▶ Success Toast ──▶ Redirect to [Login Screen]
```

### User Journey 3: Smart Intake (SMS / Receipt) to Expense Creation
```
[Any Screen] ──▶ Tap Floating Scanner / OCR Action
                 └──▶ [Smart Scanner Sheet]
                      ├── [Paste SMS] ──▶ POST /ai/extract-transaction ──▶ Instant auto-fill form
                      └── [Pick Receipt Image] ──▶ Base64 Encode ──▶ POST /ai/extract-transaction
                           └── Form Review (Title, Amount, Category, Payment Mode, Date)
                                ├── (Duplicate Detected) ──▶ Surface Amber Warning Banner
                                └── Tap "Save to Spendora" ──▶ POST /expenses ──▶ Updates Dashboard & Budgets
```

### User Journey 4: Pre-Purchase Affordability Check
```
[Dashboard / Assistant] ──▶ Tap "Simulate Purchase"
                            └──▶ [Purchase Simulator Sheet]
                                 └── Enter Item ("MacBook Pro") & Amount (₹1,50,000)
                                      └──▶ POST /ai/simulate-purchase
                                           └── Display Verdict Banner (Safe / Caution / Over-Budget)
                                                ├── Delta metrics: Cash flow before vs after
                                                ├── Daily safe burn impact
                                                └── Action: "Add as Expense Now" or "Save as Goal"
```

---

## 7. UI/UX & Design Tokens (Stitch MCP Alignment)

All screens and components strictly adhere to [design.md](file:///e:/Spendora_py/design.md):

### 7.1 Color Palette Architecture
- **Obsidian Dark Canvas:** `#080A0F` (Base), `#0F131B` (Surface), `#181C24` (Cards), `#262A33` (Elevated)
- **Primary Telemetry Accent:** `#06B6D4` (Electric Cyan) / `#4CD7F6` (Bright Variant)
- **Secondary Projection Accent:** `#8B5CF6` (Quantum Violet) / `#D0BCFF`
- **Positive Cashflow:** `#10B981` (Emerald) / `#4EDEA3`
- **Negative Outflow:** `#F43F5E` (Crimson Rose) / `#FFB4AB`
- **Specular Border:** `rgba(255, 255, 255, 0.08)` and `1px solid #262E3B`

### 7.2 Typography Scale
- **Display & Headlines:** `Space Grotesk` (Bold `40sp`, SemiBold `28sp`, `22sp`, `18sp` with `-0.03em` to `-0.01em` letter spacing)
- **Body & Captions:** `Hanken Grotesk` (Regular `16sp`, `14sp`, `12sp`)
- **Financial Figures & Telemetry:** `JetBrains Mono` with Tabular Numerals (`tnum`) (`26sp` Bold for KPIs, `14sp` for amounts, `12sp` for tags, `10sp` for micro-badges)

### 7.3 Component Geometry Rules
- **Cards & Sheets (`rounded-xl`):** `24dp` or `20dp` radius with `20dp` internal padding (`space-lg`).
- **Buttons & Text Fields (`rounded-lg`):** `16dp` radius with `52dp` touch target height.
- **Chips & Switcher Tabs (`rounded-full`):** `9999dp` pill geometry with `32dp` height.

---

## 8. Mobile Non-Functional Requirements & Resilience

1. **Zero Hardcoded Values:** All endpoints inherit from dynamic `BuildConfig.BASE_URL` with runtime tenant isolation.
2. **Offline Graceful Fallback:** When the network is unreachable, AI features automatically compute deterministic mathematical projections on device.
3. **Zero Token Leakage:** Refresh tokens are stored in hardware-backed KeyStore AES256-GCM.
4. **PII Redaction:** Account numbers, OTPs, and sensitive financial identities are scrubbed before reaching external LLM providers.
5. **Fluid Android Navigation:** 5-tab Material 3 bottom bar (Dashboard, Expenses, Income, Budgets, Goals) with deep-link support.
