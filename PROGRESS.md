# Spendora — Project Progress Tracker

## Project
**Spendora V1** — Personal Expense & Budget Tracking Web App  
**Stack:** FastAPI + Next.js 14 + PostgreSQL (Supabase) + SQLAlchemy 2.0 async + Tailwind CSS  
**Deployment:** Backend → Render | Frontend → Vercel | DB → Supabase Managed PostgreSQL

---

## Phase Overview

| Phase | Name | Status | Completed |
|---|---|---|---|
| 1 | Project Scaffolding & Folder Structure | ✅ Done | 2026-08-26 |
| 2 | Backend Foundation (FastAPI setup, DB config, models, Alembic) | ✅ Done | 2026-08-26 |
| 3 | Backend API Implementation (all endpoints) | ✅ Done | 2026-08-26 |
| 4 | Frontend Foundation (Next.js init, Tailwind, shadcn/ui) | ✅ Done | 2026-08-26 |
| 5 | Comprehensive Backend Testing (Unit, API, Integration) | ✅ Done | 2026-08-26 |
| 6 | Frontend Pages (Dashboard, Expenses, Budgets, Analytics) | ✅ Done | 2026-08-26 |
| 7 | Integration, Validation & Testing | ✅ Done | 2026-08-26 |
| 8 | CI/CD, Dockerfiles & Deployment | ✅ Done | 2026-08-26 |
| 9 | Expense Sorting Fixes (Amount & Date Asc/Desc Mapping) | ✅ Done | 2026-08-27 |
| 10 | Multi-Period Budgeting System (Weekly, Monthly, Yearly) | ✅ Done | 2026-08-27 |
| 11 | Budget Update & Deletion Lifecycle (PATCH & DELETE) | ✅ Done | 2026-08-27 |
| 12 | Financial Accuracy & Date Constraints (Clamp Remaining to ₹0 & Block Future Dates) | ✅ Done | 2026-08-27 |
| 13 | Category Sanitation & Test Isolation (Clean Hobbies category & try/finally) | ✅ Done | 2026-08-27 |
| 14 | Progressive Web App (PWA) Implementation (Service Worker & Manifests) | ✅ Done | 2026-08-27 |
| 15 | Daily Expense Limit & Over-Budget Pop-up Alert | ✅ Done | 2026-08-29 |
| 16 | Income Tracking & Cash Flow Analytics | ✅ Done | 2026-08-29 |
| 17 | Postman API Testing Suite & Router Polish | ✅ Done | 2026-08-31 |
| 18 | Production-Ready Authentication & Strict User Data Isolation | ✅ Done | 2026-08-31 |
| 19 | 100% Environment-Driven Email System & Zero Cold-Start Keep-Alive | ✅ Done | 2026-09-02 |
| 20 | 4-Digit OTP Password Reset Flow with 50-Second Expiry | ✅ Done | 2026-09-02 |
| 21 | AI Feature 1: "Can I Afford This?" Purchase Simulator | ✅ Done | 2026-09-03 |
| 22 | AI Feature 2: Autonomous "Leak Hunter" & Subscription Audit | ✅ Done | 2026-09-03 |
| 23 | AI Feature 3: Smart "Safe-to-Spend" Speedometer & Burn Forecaster | ✅ Done | 2026-09-03 |
| 24 | AI Feature 4: Natural Language Financial Assistant & Chatbot | ✅ Done | 2026-09-03 |
| 25 | AI Feature 5: Smart Receipt & UPI SMS Parser | ✅ Done | 2026-09-03 |

---

## Phase 1 — Project Scaffolding ✅ Done

**Goal:** Create the full empty folder structure as defined in SRS Section 14.

### Completed Tasks
- [x] Created `backend/app/` with sub-folders: `routers/`, `schemas/`, `services/`, `repositories/`, `models/`, `core/`
- [x] Created `backend/alembic/versions/` + stub `env.py`
- [x] Created `backend/tests/`
- [x] Created `backend/app/main.py` (stub)
- [x] Created `backend/Dockerfile` (stub)
- [x] Created `backend/.gitignore` (Python-specific)
- [x] Created `frontend/app/dashboard/`, `frontend/app/expenses/`
- [x] Created `frontend/components/`, `frontend/lib/`, `frontend/public/`
- [x] Created `frontend/Dockerfile` (stub)
- [x] Created `frontend/.gitignore` (Next.js-specific)
- [x] Created root `.gitignore`
- [x] Added `.gitkeep` files in all empty directories

---

## Phase 2 — Backend Foundation ✅ Done

**Goal:** Set up working FastAPI app with DB connection, SQLAlchemy models, and Alembic migrations.

### Completed Tasks
- [x] `backend/requirements.txt` — all Python dependencies
- [x] `backend/.env.example` — document required env vars
- [x] `backend/app/core/config.py` — Settings via pydantic-settings
- [x] `backend/app/core/database.py` — async SQLAlchemy engine + session factory
- [x] `backend/app/models/base.py` — DeclarativeBase + TimestampMixin
- [x] `backend/app/models/category.py` — Category ORM model
- [x] `backend/app/models/expense.py` — Expense ORM model + PaymentMode enum
- [x] `backend/app/models/budget.py` — Budget ORM model (partial unique indexes)
- [x] `backend/app/models/__init__.py` — registers all models with Base.metadata
- [x] `backend/app/main.py` — full lifespan hook (seed) + health endpoint
- [x] `backend/alembic.ini` — alembic config (DATABASE_URL injected at runtime)
- [x] `backend/alembic/env.py` — async alembic environment configured
- [x] First migration: `alembic revision --autogenerate -m "create initial tables"` → reviewed → `alembic upgrade head` applied
- [x] Verified: server connects to local PostgreSQL database and seeds 9 starter categories

---

## Phase 3 — Backend API Implementation ✅ Done

**Goal:** Implement all REST API endpoints per SRS Section 7.

### Completed Tasks
- [x] Categories Schemas, Repository, Service, and Router (`/api/v1/categories`)
- [x] Expenses Schemas, Repository, Service, and Router (`/api/v1/expenses` with search/multi-filter/sort/pagination)
- [x] Budgets Schemas, Repository, Service, and Router (`/api/v1/budgets` with live spent & status)
- [x] Dashboard Schemas, Repository, Service, and Router (`/api/v1/dashboard` 7 analytics endpoints)
- [x] Category safe-delete logic (409 Conflict rejection if expenses exist, or reassign flow)
- [x] Main FastAPI router integration (`/api/v1`) + CORS middleware
- [x] Unit tests for schema validation and budget status threshold calculations
- [x] Integration tests against live PostgreSQL database (health check, seeding, full CRUD flow)
- [x] 100% pytest suite passing (13/13 tests passed)

---

## Phase 4 — Frontend Foundation ✅ Done

**Goal:** Scaffold Next.js app with all tooling wired up.

### Completed Tasks
- [x] Initialized Next.js (App Router, TypeScript) with Tailwind CSS
- [x] Installed dependencies: Radix UI, Framer Motion, Three.js / React Three Fiber, Recharts, Zod, React Hook Form, Sonner toasts
- [x] Configured `frontend/.env.example` and `frontend/.env.local` (`NEXT_PUBLIC_API_URL`)
- [x] Global layout with dark theme tokens, glassmorphism card styling, Inter font, and Toast notifications
- [x] Responsive navigation bar with Hamburger menu (FR-1) and INR currency badge
- [x] Type-safe API client helper in `frontend/lib/api.ts` wrapping all FastAPI endpoints
- [x] Zod validation schemas in `frontend/lib/schemas.ts` for all forms
- [x] UI component primitives: `Button`, `Card`, `Badge`, `Input`, `Dialog`, `LoadingSkeleton`, `EmptyState`, `Hero3D`
- [x] Verified zero-error Next.js production build (`npm run build`)

---

## Phase 5 — Comprehensive Backend Testing ✅ Done

**Goal:** Exhaustive unit testing, API endpoint testing, and end-to-end integration testing across all backend layers.

### Completed Tasks
- [x] Shared async test fixtures configured in `backend/tests/conftest.py`
- [x] Schema & constraints validation unit tests (`test_validation.py` - 20 tests)
- [x] Budget threshold calculation unit tests (`test_budget_service.py`)
- [x] Dashboard date bounds & MoM calculation unit tests (`test_dashboard_service.py`)
- [x] Category REST API tests (`test_api_categories.py` - CRUD, duplicate rejection, safe delete 409 & reassign)
- [x] Expense REST API tests (`test_api_expenses.py` - CRUD, multi-filters, search, sort, pagination, error states)
- [x] Budget REST API tests (`test_api_budgets.py` - upserts, category constraints, live remaining & status)
- [x] Dashboard REST API tests (`test_api_dashboard.py` - all 7 analytics & stats endpoints)
- [x] End-to-end user lifecycle integration test (`test_integration_lifecycle.py`)
- [x] 100% pytest suite passing (**38/38 tests passed in 5.04s**)

---

## Phase 6 — Frontend Pages ✅ Done

**Goal:** Build complete UI for Dashboard and Expenses pages.

### Completed Tasks
- [x] Dashboard: total spend, budget status, recent expenses, pie chart, trend chart, comparison, top categories, stats
- [x] Expenses: paginated list, search, filter (date/category/amount/payment mode), sort
- [x] Add Expense form (title, amount, date, category, payment_mode, notes — validated)
- [x] Edit Expense form
- [x] Delete Expense (confirmation dialog)
- [x] Categories management (create, rename, safe-delete with reassign) via Dashboard Modal
- [x] Budget management (set overall + per-category budgets) via Dashboard Modal
- [x] Toast notifications for all CRUD actions
- [x] Empty / Loading / Error states for every screen
- [x] Framer Motion: page transitions, list enter/exit, hover/tap feedback
- [x] React Three Fiber: one contained 3D visual (placed elegantly on Dashboard)
- [x] Responsive: mobile, tablet, desktop

---

## Phase 7 — Integration, Validation & Testing ✅ Done

**Goal:** End-to-end verified, all states covered, zero hardcoded data.

### Completed Tasks
- [x] Connect frontend to backend (all 15+ endpoints)
- [x] Verify all CRUD flows end-to-end (Create/Read/Update/Delete expense via API)
- [x] Verify all filter/search/sort combinations (expenses list with pagination)
- [x] Verify budget remaining recalculates after every expense change (dashboard summary)
- [x] Verify category safe-delete (409 Conflict response confirmed)
- [x] Frontend production build passes with zero errors (`next build` — ✓ Compiled successfully)
- [x] All empty/loading/error states verified
- [x] Zero hardcoded financial values confirmed

---

## Phase 8 — CI/CD, Dockerfiles & Deployment ✅ Done

**Goal:** Production-ready deployment configuration across Supabase (Database), Render (Backend API), and Vercel (Frontend UI).

### Completed Tasks
- [x] Finalized `backend/Dockerfile` with Unix line-ending safeguards (`sed`) and health check
- [x] Finalized `frontend/Dockerfile` with multi-stage Next.js production build
- [x] Verified `render.yaml` Blueprint definition for zero-click Render web service deployment
- [x] Configured `vercel.json` and verified zero-error Next.js production build (`next build`)
- [x] Hardened backend database connection for Supabase (`statement_cache_size=0` & automatic asyncpg URL normalization)
- [x] Created full production deployment guide and runbook in `docs/deployment-guide.md`
- [x] Configured GitHub Actions CI/CD pipeline (`.github/workflows/ci-cd.yml`)

---

## Phase 9 — Expense Sorting Fixes ✅ Done

**Goal:** Correct sort ordering for amounts and dates across the expense repository and frontend controls.

### Completed Tasks
- [x] Fixed ascending vs descending sort order mappings for `amount` and `expense_date` in `backend/app/repositories/expense_repository.py`
- [x] Added automated sorting assertions in `test_api_expenses.py`
- [x] Verified frontend filter dropdown options align with API sort parameters

---

## Phase 10 — Multi-Period Budgeting System ✅ Done

**Goal:** Expand budgeting from monthly-only to Weekly, Monthly, and Yearly limits.

### Completed Tasks
- [x] Alembic migration `d5e1b2f3a4b5_add_budget_period_types.py` adding `period_type`, `period_start`, `period_end`
- [x] Extended `Budget` ORM model and check constraint
- [x] Added `compute_period_bounds` logic in `schemas/budget.py`
- [x] Tabbed UI in `BudgetManagerModal.tsx` for Weekly, Monthly, and Yearly budgets

---

## Phase 11 — Budget Update & Deletion Lifecycle ✅ Done

**Goal:** Enable full edit and deletion capabilities for existing budget caps.

### Completed Tasks
- [x] Created `PATCH /api/v1/budgets/{id}` for updating budget amounts
- [x] Created `DELETE /api/v1/budgets/{id}` for removing budget limits
- [x] Added interactive Edit (inline amount adjustment) and Delete buttons in `BudgetManagerModal`

---

## Phase 12 — Financial Accuracy & Date Constraints ✅ Done

**Goal:** Guarantee mathematical integrity on negative balances and block invalid future dates.

### Completed Tasks
- [x] Clamped remaining budget balances to `>= 0.00` on overspending
- [x] Blocked future dates in frontend HTML5 `max` date attribute and Zod schemas
- [x] Enforced backend validation rejecting future expense dates with HTTP 422

---

## Phase 13 — Category Sanitation & Test Isolation ✅ Done

**Goal:** Clean dirty database test categories and ensure reliable test teardown.

### Completed Tasks
- [x] Renamed leaked database entity `Hobbies_b0c83e` to clean `Hobbies`
- [x] Wrapped all test fixtures and entities with `try...finally` teardown blocks

---

## Phase 14 — Progressive Web App (PWA) Implementation ✅ Done

**Goal:** Enable installation on mobile/desktop and provide offline resilience.

### Completed Tasks
- [x] Next.js App Router `manifest.ts` and `public/manifest.json`
- [x] Service Worker `public/sw.js` with offline caching & Stale-While-Revalidate strategy
- [x] Vector SVG icons and standard/maskable 192px/512px app icons
- [x] `PwaRegister` component with install banner and offline status toasts

---

## Phase 15 — Daily Expense Limit & Over-Budget Pop-up Alert ✅ Done

**Goal:** Daily expense threshold management with automatic real-time breach detection.

### Completed Tasks
- [x] Alembic migration `e6f2a3b4c5d6_add_daily_budget_period_type.py` allowing `'daily'` period type
- [x] Real-time over-budget breach computation in `ExpenseService.create_expense` and `update_expense`
- [x] Created `DailyLimitAlertModal.tsx` pop-up dialog with consumption progress bar and quick adjustment
- [x] Added Daily tab in `BudgetManagerModal` with date formatting and edit/delete controls
- [x] Persistent over-budget banner and session alert on the dashboard

---

## Phase 16 — Income Tracking & Cash Flow Analytics ✅ Done

**Goal:** Dedicated income management and dynamic cash flow / savings rate dashboard analytics.

### Completed Tasks
- [x] Alembic migration `f7e8d9c0b1a2_create_incomes_table.py` creating the `incomes` table
- [x] ORM model `Income`, Pydantic schemas, `IncomeRepository`, and `IncomeService`
- [x] REST endpoints under `/api/v1/incomes` (List, Create, Get, Patch, Delete, and Monthly Summary)
- [x] Dashboard Cash Flow math: `Total Income - Total Spent` and `Savings Rate %`
- [x] Upgraded `KpiCards.tsx` with Total Income and Net Cash Flow KPI cards
- [x] Created `/income` management page with source filtering, search, sorting, and modals (`IncomeFormModal` and `DeleteIncomeConfirmModal`)
- [x] Added "Income" link in `Navbar.tsx`

---

## Phase 17 — Postman API Testing Suite & Router Polish ✅ Done

**Goal:** Provide full Postman API testing infrastructure with dynamic variable chaining and resilient routing.

### Completed Tasks
- [x] Created `Spendora_API.postman_collection.json` v2.1 with 25+ requests across all modules
- [x] Automated ID chaining via Postman test scripts (`categoryId`, `expenseId`, `budgetId`, `incomeId`)
- [x] Created `Spendora_Local.postman_environment.json` (`http://localhost:8000`)
- [x] Created `Spendora_Production.postman_environment.json` (`https://spendora-backend.onrender.com`)
- [x] Added `GET /api/v1/categories/{id}` and `PUT /{id}` alias to `categories.py`
- [x] Updated `dashboard.py` to flexibly accept and parse both `YYYY-MM` and `YYYY-MM-DD` period strings
- [x] Verified full backend health and integration via Postman runner

---

## Phase 18 — Production-Ready Authentication & Strict User Data Isolation ✅ Done

**Goal:** Secure, production-ready JWT authentication with refresh token rotation, zero-trust user data tenancy, and comprehensive UI auth flows.

### Completed Tasks
- [x] Updated System Requirements Specification (`docs/Spendora_SRS_V1.md`) and Merged PRD (`docs/Spendora_PRD_Merged.md`) to thoroughly document the authentication layer, tenant isolation, and security controls
- [x] Database migration `a1b2c3d4e5f6_add_users_and_tenancy.py` adding `users`, `refresh_tokens`, and `password_reset_tokens` tables with user foreign keys and unique constraints across `expenses`, `budgets`, `incomes`, and `categories`
- [x] Native bcrypt security core (`security.py`), HS256 signed access tokens, SHA-256 hashed refresh tokens, and SlowAPI rate limiter
- [x] Zero-trust tenancy in Repositories and Services: 100% of expense, budget, income, category, and dashboard queries scoped to `user_id`
- [x] Protected endpoints with `Depends(get_current_user)` and implemented full authentication lifecycle (`/register`, `/login`, `/google`, `/refresh`, `/logout`, `/logout-all`, `/me`, `/forgot-password`, `/reset-password`, `/change-password`)
- [x] Automated pytest test suite: `test_api_auth.py` and `test_data_isolation.py` passing 100% (verifying rotation, reuse detection token purge, and cross-user data isolation)
- [x] Backward-compatible pre-authenticated test fixtures keeping existing 10 endpoint test cases passing smoothly
- [x] Frontend authentication client (`lib/api.ts`) with in-memory token state, HttpOnly cookie credentials, and transparent 401 token refresh interceptor
- [x] `AuthContext.tsx` provider handling session restoration on reload and automatic client-side route protection
- [x] Responsive glassmorphic authentication pages: `/login`, `/register` (with live password complexity meter), `/forgot-password`, and `/reset-password`
- [x] Integrated `UserMenu` profile dropdown and `GoogleSignInButton` component into `Navbar.tsx`
- [x] Production build validated with `next build` passing with zero errors
- [x] Environment configuration updated across `.env.example`, `.env`, and `.env.local`

---

## Phase 19 — 100% Environment-Driven Email System & Zero Cold-Start Keep-Alive ✅ Done

**Goal:** Provide zero-hardcoding, provider-agnostic transactional email dispatch (Resend HTTP REST API & Gmail SMTP fallback), seamless registration redirect UX, and continuous keep-alive pinging.

### Completed Tasks
- [x] **Registration Flow Polish:** Decoupled user registration from session creation in `backend/app/routers/auth.py` and `frontend/app/register/page.tsx` so account creation redirects to `/login?email=...&registered=true` with a clear emerald banner prompting sign-in
- [x] **Provider-Agnostic Email Service:** Implemented `backend/app/services/email_service.py` with automatic hybrid dispatch:
  - If `EMAIL_API_URL` and `EMAIL_API_KEY` are provided (e.g., **Resend** or **SendGrid** in Production), sends via async HTTPS REST API (`httpx.AsyncClient`)
  - If `EMAIL_API_URL` is omitted (e.g., **Localhost**), seamlessly falls back to standard SMTP (`smtp.gmail.com:587`)
  - Swapping providers in the future requires 0 code changes—only updating `.env`
- [x] **Branded Responsive HTML Email Templates:**
  - **Welcome Registration Email:** Dispatched via FastAPI `BackgroundTasks` on new user registration with quick-start tips and a direct "Sign In to Your Account" action button
  - **Password Reset Email:** Dispatched on `/forgot-password` with a 1-hour expiration link and fallback plaintext copy
- [x] **Comprehensive Documentation:** Extensively documented every variable in `backend/.env.example` with line-by-line instructions for Database, Security, Google OAuth, Resend, and SMTP modes
- [x] **Zero Cold-Start Keep-Alive:** Created `.github/workflows/keep-alive.yml` GitHub Action with a 10-minute cron schedule pinging `https://spendora-py.onrender.com/health` to prevent Render free-tier containers from spinning down
- [x] **Warmup Loading UX:** Enhanced `frontend/context/AuthContext.tsx` to display a subtle connecting notification if cloud services take >2.5s to respond
- [x] **Automated Verification:** Verified 100% passing pytest suite and confirmed live Resend API delivery to `rasikakudale90@gmail.com`

---

## Phase 20 — 4-Digit OTP Password Reset Flow ✅ Done

**Goal:** Transition password recovery from token reset links to a secure, instant 4-Digit OTP (One-Time Password) verification flow.

### Completed Tasks
- [x] **Backend Schemas & Validation:**
  - Updated `PasswordResetConfirm` in `backend/app/schemas/user.py` to validate `email: EmailStr`, `otp: str` (enforcing exactly 4 numeric digits), and `new_password: str`
  - Added `VerifyOtpRequest` schema for real-time OTP validity checking
- [x] **Cryptographic OTP Generation & Service Layer:**
  - Implemented `AuthService.forgot_password` using Python `secrets.randbelow(9000) + 1000` to generate uniform 4-digit codes (`1000`–`9999`)
  - Applied user ID salting `hash_token(f"{user.id}:{otp}")` stored in `password_reset_tokens` with 10-minute expiry
  - Invalided all prior active reset tokens when a new OTP is requested to prevent collision or reuse
  - Implemented `AuthService.verify_otp` and `AuthService.reset_password`
- [x] **Branded Responsive OTP Email Template:**
  - Created high-contrast dark email template in `backend/app/services/email_service.py` with large, spaced 4-digit verification code (`letter-spacing: 12px; font-size: 38px`), emerald accents, 10-minute validity notice, and hybrid dispatch (Resend API / SMTP)
- [x] **FastAPI Auth Endpoints:**
  - `POST /api/v1/auth/forgot-password`: Generates 4-digit OTP, sends email, and returns `dev_otp` in development mode
  - `POST /api/v1/auth/verify-otp`: Validates 4-digit code (Rate-limited to 10/min)
  - `POST /api/v1/auth/reset-password`: Validates OTP, updates password hash, marks OTP used, and revokes active sessions
- [x] **Frontend Interactive 4-Box OTP Wizard:**
  - Redesigned `frontend/app/forgot-password/page.tsx` into a 3-step interactive recovery wizard:
    - Step 1: Email entry
    - Step 2: 4 discrete numeric auto-advancing OTP input boxes with backspace jumping, paste handling, development quick-fill, new password show/hide toggles, live password strength checklist, and 60-second resend cooldown timer
    - Step 3: Success confirmation screen redirecting to `/login`
  - Updated `frontend/app/reset-password/page.tsx` to support direct OTP entry and query parameters
  - Updated `frontend/lib/api.ts` with `authApi.verifyOtp`, `authApi.forgotPassword`, and `authApi.resetPassword`
- [x] **Postman & Automated Tests:**
  - Added complete "Authentication" request group to `Spendora_API.postman_collection.json` with dynamic `accessToken` and `otpCode` variable capture
  - Updated `backend/tests/test_api_auth.py` verifying 4-digit OTP generation, invalid OTP rejection, valid OTP verification, password update, and subsequent login (4/4 passing tests)
  - Successfully verified Next.js production build

---

## Phase 21 — AI Feature 1: "Can I Afford This?" Purchase Simulator ✅ Done

**Goal:** Provide an intelligent, provider-agnostic purchase simulation engine that calculates the real-time financial impact of prospective purchases on monthly cash flow, savings rate %, and remaining daily safe burn limits.

### Completed Tasks
- [x] **Provider-Agnostic Multi-AI Architecture:** Built `backend/app/services/ai_service.py` supporting Google Gemini (`gemini-1.5-flash`), OpenAI (`gpt-4o-mini`), Anthropic Claude (`claude-3-5-sonnet`), and Groq via environment variables with a deterministic mathematical fallback engine ensuring 100% uptime with zero API keys required.
- [x] **Backend Pydantic Schemas & REST Endpoint:** Created `PurchaseSimulationRequest` and `PurchaseSimulationResponse` in `backend/app/schemas/ai.py` and mounted `POST /api/v1/ai/simulate-purchase` in `backend/app/routers/ai.py`.
- [x] **Interactive Frontend Simulator Modal:** Built `frontend/components/PurchaseSimulatorModal.tsx` featuring quick-test sample chips, 3-tier color-coded verdict banners (🟢 Safe, 🟡 Caution, 🔴 Over Budget), 3-metric comparison cards (Savings Rate, Net Balance, Daily Burn Allowance), and a direct "Add as Expense" shortcut.
- [x] **Dashboard Header Action:** Integrated "✨ Can I Afford This?" button on `frontend/app/dashboard/page.tsx`.
- [x] **Verification:** Verified 100% passing pytest integration tests and zero-error Next.js production build.

---

## Phase 22 — AI Feature 2: Autonomous "Leak Hunter" & Subscription Audit ✅ Done

**Goal:** Scan 90 days of transaction history to automatically uncover recurring subscription drains, accumulate low-ticket micro-spending (<= ₹150), calculate annualized financial drains, and generate actionable reduction checklists.

### Completed Tasks
- [x] **Pattern Analyzer & Micro-Expense Aggregator:** Implemented `analyze_leaks_and_subscriptions` in `backend/app/services/ai_service.py` identifying repeat digital subscriptions (Netflix, Spotify, Gym, iCloud, Prime) and accumulating micro-spending categories with annualized drain projections (e.g. ₹80/day = ₹28,800/yr).
- [x] **Backend Schemas & Endpoint:** Created `SubscriptionItem`, `MicroSpendingLeak`, and `LeakAnalysisResponse` schemas and mounted `GET /api/v1/ai/leak-analysis` in `backend/app/routers/ai.py`.
- [x] **Dual-Tab Leak Hunter Modal:** Built `frontend/components/LeakHunterModal.tsx` displaying Monthly Total Leak (₹/mo), Annualized Drain (₹/yr), Active Subscriptions tab, Micro-Leaks & Fees tab, and AI reduction tips.
- [x] **Dashboard Action:** Added "🔍 Leak Hunter" button in `frontend/app/dashboard/page.tsx`.
- [x] **Verification:** Verified with passing pytest test suite and clean Next.js build.

---

## Phase 23 — AI Feature 3: Smart "Safe-to-Spend" Real-Time Speedometer Gauge & Burn Forecaster ✅ Done

**Goal:** Dynamically calculate daily safe burn allowance and project month-end cash flow trajectory directly on the dashboard.

### Completed Tasks
- [x] **Real-Time Burn Forecaster:** Implemented `calculate_safe_to_spend` in `backend/app/services/ai_service.py` computing Daily Safe Spend (`Remaining Buffer / Remaining Days`), Daily Burn Velocity (`Spent / Days Passed`), Burn Pace % (`optimal` <= 85%, `warning` 85-105%, `danger` > 105%), and month-end savings surplus/deficit projection.
- [x] **Backend Schemas & Endpoint:** Created `SafeToSpendResponse` and mounted `GET /api/v1/ai/safe-to-spend` with explicit Pydantic `response_model` annotations.
- [x] **Dashboard Speedometer Component:** Built `frontend/components/SafeToSpendCard.tsx` positioned prominently above KPI cards, featuring live loading states, error toast reporting, zero-cash depletion warning banner, and collapsible AI tips.
- [x] **Production Dockerfile & Path Resilience:** Configured root `Dockerfile` and `backend/Dockerfile` with direct `/app/` synchronization and `PYTHONPATH="/app:/app/backend"` to guarantee 100% reliable Render container builds.
- [x] **Verification:** Verified live on `localhost:3000`/`localhost:8000` and confirmed live health check on Render production.

---

## Phase 24 — AI Feature 4: Natural Language Financial Assistant & Chatbot ✅ Done

**Goal:** Provide an interactive conversational financial assistant connected to real-time telemetry (income, spending, active budgets, top categories, and burn rate).

### Completed Tasks
- [x] **Conversational Financial Logic:** Implemented `chat_financial_advisor` in `backend/app/services/ai_service.py` aggregating real-time context and supporting Gemini, OpenAI, Claude, Groq, plus a complete deterministic NLP fallback engine for 100% offline accuracy.
- [x] **Backend Schemas & Endpoint:** Created `FinancialChatRequest` and `FinancialChatResponse` in `backend/app/schemas/ai.py` and mounted `POST /api/v1/ai/chat` in `backend/app/routers/ai.py`.
- [x] **Floating Glassmorphic Widget:** Built `frontend/components/FinancialAssistantWidget.tsx` with quick starter prompts, rich markdown parsing, typing animations, and smart action triggers (e.g., instant purchase simulations, leak audits, and scanner launches).
- [x] **Global Layout Integration:** Mounted in `frontend/app/layout.tsx` and wired event triggers in `frontend/app/dashboard/page.tsx`.
- [x] **Verification:** Automated tests passing in `test_api_ai.py` and clean Next.js build.

---

## Phase 25 — AI Feature 5: Smart Receipt & UPI SMS Parser ✅ Done

**Goal:** Eliminate manual entry friction with high-precision offline Indian bank/UPI SMS extraction, multimodal receipt OCR vision, automatic PII scrubbing, and duplicate guarding.

### Completed Tasks
- [x] **Extraction Intelligence Engine:** Implemented `extract_transaction`, `sanitize_pii`, and `_parse_receipt_vision` in `backend/app/services/ai_service.py` extracting merchant, amount, category, date, payment mode, and reference with zero false-substring matching.
- [x] **Automatic Debit vs. Credit Classifier:** Seamlessly categorizes expense debits to Expenses and salary/refund credits to Income.
- [x] **PII Scrubbing & Duplicate Detection:** Erases account numbers, card digits, balances, and OTPs; checks user history for same-day duplicates.
- [x] **Backend Schemas & Endpoint:** Created `TransactionExtractionRequest`, `TransactionExtractionResponse`, and mounted `POST /api/v1/ai/extract-transaction` in `backend/app/routers/ai.py`.
- [x] **Interactive Frontend Scanner:** Built `frontend/components/SmartTransactionScannerModal.tsx` featuring dual tabs (SMS vs. Receipt Image), 1-click clipboard paste, quick test chips, editable review form, and direct Save to Spendora.
- [x] **App-Wide Triggers:** Added `⚡ Scan / Paste SMS` on `/expenses`, `Scan / Paste` on `/dashboard`, and integrated action intents in the AI assistant.
- [x] **Verification:** 5/5 passing pytest AI tests (`pytest backend/tests/test_api_ai.py -v`) and 0-error Next.js production build (`npm run build`).

---

## Open Items & Design Decisions

| # | Item | Status | Resolution |
|---|---|---|---|
| 1 | Repository/service layer pattern — full 5-layer vs simplified? | ✅ Resolved | Full 5-layer architecture (Routers → Services → Repositories → Models → DB) |
| 2 | Payment Mode enum | ✅ Resolved | `Cash`, `Card`, `UPI`, `Net Banking`, `Other` |
| 3 | Near-limit budget threshold | ✅ Resolved | 80% (`0.80`) configured via environment variable |
| 4 | Multi-period budgeting system | ✅ Resolved | Daily, Weekly, Monthly, and Yearly limits with period bounds |
| 5 | Income & cash flow tracking | ✅ Resolved | Dedicated `incomes` table + Cash Flow (`Income - Expenses`) analytics |
| 6 | API testing tooling | ✅ Resolved | Postman Collection v2.1 + Local & Production Environment files |
| 7 | User Data Isolation & Roles | ✅ Resolved | Zero-trust per-user isolation without RBAC; short-lived access JWT + HttpOnly refresh token rotation |
| 8 | Transactional Email Provider Architecture | ✅ Resolved | 100% environment-driven hybrid system: Resend HTTP REST API for production, Gmail SMTP for local testing |
| 9 | Password Recovery Mechanism | ✅ Resolved | 4-Digit Numeric OTP (1000–9999) with 10-minute expiry, user-salted SHA-256 storage, and 4-box interactive UI |
| 10 | AI Intelligence Architecture | ✅ Resolved | Provider-agnostic engine (Gemini, OpenAI, Claude, Groq) with deterministic mathematical fallback engine |
