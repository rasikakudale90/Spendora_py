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
| 26 | Production Deployment Hardening & Live AI Verification | ✅ Done | 2026-09-04 |
| 27 | AI Features 6 & 7: Financial Health Score Radar & Smart Goals Runway | ✅ Done | 2026-09-07 |
| 28 | In-Database RAG Architecture for AI Financial Assistant | ✅ Done | 2026-09-07 |
| 29 | Safe Web Archiving & Android Foundation (Kotlin + Compose) | ✅ Done | 2026-09-08 |
| 30 | Android Network Core, Auth & 4-Digit OTP Wizard | ✅ Done | 2026-09-08 |
| 31 | Core Financials (Dashboard, Expenses, Income) | ✅ Done | 2026-09-08 |
| 32 | Multi-Period Budgets & Smart Savings Goals Runway | ✅ Done | 2026-09-08 |
| 33 | Full 7-Feature Spendora AI Suite on Mobile | ✅ Done | 2026-09-08 |
| 34 | Final Polish, Complete Build Verification & Android Documentation | ✅ Done | 2026-09-08 |
| 35 | Native Android Google OAuth & Credential Manager Integration | ✅ Done | 2026-09-09 |
| 36 | Native Android UI/UX Overhaul, Category Lifecycle Fixes & Polish | ✅ Done | 2026-09-09 |
| 37 | Dedicated AI Feature Sheets & Independent Click Lifecycle | ✅ Done | 2026-09-09 |
| 38 | Native Android Brand Identity & Adaptive App Launcher Logo | ✅ Done | 2026-09-09 |
| 39 | Refined S Logo Proportions & Animated Spendora-to-S Launch Sequence | ✅ Done | 2026-09-09 |
| 40 | Zero-Hardcoding Dynamic Light & Dark Theme Architecture | ✅ Done | 2026-09-09 |

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

## Phase 26 — Production Deployment Hardening & Live AI Verification ✅ Done

**Goal:** Eliminate deployment breakages on Render and Vercel, resolve container startup runtime exceptions, synchronize all environment requirements, and introduce client-side fallback resilience for uninterrupted UI availability.

### Root Cause Analysis (Render Failure Incident)
- **The Missing Import (`NameError`):** In `backend/app/services/ai_service.py`, `calculate_safe_to_spend` used `today: Optional[date] = None`. In Python 3.12 (Render's Docker image), class method type hints are evaluated at class creation time. Because `date` was imported locally inside the method body rather than at the top of the file, Python raised `NameError: name 'date' is not defined` and crashed on boot.
- **The 404 Symptom:** When the new container crashed on startup, Render aborted the deployment and continued serving the previous working container (from Feature 2), which lacked `/api/v1/ai/safe-to-spend`, causing `404 Not Found` in the frontend dashboard.
- **The Solution:** Added `from __future__ import annotations`, top-level `from datetime import date, datetime`, synchronized root `requirements.txt`, and made Dockerfiles context-agnostic. Added client-side fallback in `SafeToSpendCard.tsx` so the dashboard gauge never fails even during server restarts.

### Completed Tasks
- [x] **Python 3.12 Type Annotation Fix:** Resolved `NameError: name 'date' is not defined` inside `calculate_safe_to_spend` by adding `from __future__ import annotations`, top-level `from datetime import date, datetime`, and `import calendar` in `backend/app/services/ai_service.py`.
- [x] **Root Dependencies Synchronization:** Synced root `requirements.txt` with `backend/requirements.txt` ensuring all auth, security (`pyjwt`, `passlib`, `bcrypt`, `slowapi`), and AI dependencies are installed regardless of build context.
- [x] **Context-Agnostic Resilient Dockerfiles:** Updated both root `Dockerfile` and `backend/Dockerfile` with multi-context fallback (`COPY requirements.txt* backend/requirements.tx[t]`) and directory synchronization, allowing seamless builds from both `.` and `./backend`.
- [x] **Client-Side Zero-Fail Resilient Speedometer:** Enhanced `frontend/components/SafeToSpendCard.tsx` with a deterministic mathematical fallback engine consuming `DashboardSummary`. The card displays live burn rate velocity, safe burn limits, and trajectory projections even during cold starts or deployment updates without showing broken error states.
- [x] **Live End-to-End Production Verification:** Validated live deployment on Render (`https://spendora-py.onrender.com`) via authenticated tests returning HTTP 200 OK across all 5 AI endpoints (`simulate-purchase`, `leak-analysis`, `safe-to-spend`, `chat`, `extract-transaction`).
- [x] **Frontend Production Build:** Verified Next.js production bundle with 0 errors and 0 ESLint warnings.

---

## Phase 27 — AI Features 6 & 7: Financial Health Score Radar & Smart Goals Runway ✅ Done

**Goal:** Provide a comprehensive 0–100 FICO-style financial health scorecard with spider radar visualizations, and an intelligent savings goals runway engine linking day-to-day spending cuts directly to dream milestone completion dates.

### Completed Tasks
- [x] **Goals Database Layer & Migration:** Created `goals` ORM model in `backend/app/models/goal.py` with zero-trust tenancy, check constraints (`target_amount > 0`, `current_amount >= 0`), and Alembic migration `b2c3d4e5f6a7_add_goals_table.py`. Added `goals` relationship to `User` model and exported in `app.models`.
- [x] **Goals Schemas, Repository, Service & Router:** Built full 5-layer architecture for goals in `backend/app/schemas/goal.py`, `backend/app/repositories/goal_repository.py`, `backend/app/services/goal_service.py`, and mounted `GET`, `POST`, `PATCH`, `DELETE`, and `POST /{id}/contribute` in `backend/app/routers/goals.py` and `api_router.py`.
- [x] **AI Financial Health Score Engine:** Implemented `calculate_financial_health_score` in `backend/app/services/ai_service.py` evaluating 5 weighted dimensions: Savings Discipline (25%), Budget Adherence (25%), Burn Stability (20%), Cash Cushion (15%), and Leak Control (15%). Generates a composite score (0-100), tier badges (`elite`, `healthy`, `vulnerable`, `critical`), and 3 prioritized score boosters (`+8 PTS`). Added `GET /api/v1/ai/health-score` in `backend/app/routers/ai.py`.
- [x] **AI Smart Goals Runway Engine:** Implemented `analyze_goals_runway` in `backend/app/services/ai_service.py` evaluating multi-goal funding requirements against live monthly cash surplus, assigning pacing statuses (`on_track`, `ahead`, `at_risk`, `behind`, `completed`), and suggesting discretionary category reduction trade-offs. Added `GET /api/v1/ai/goals-runway` in `backend/app/routers/ai.py`.
- [x] **Frontend Financial Health Scorecard Widget:** Built `frontend/components/dashboard/FinancialHealthCard.tsx` featuring composite score circular ring, interactive Recharts Spider Radar chart, 5-pillar progress breakdown, prioritized booster action cards, and resilient client-side mathematical fallback. Mounted on `/dashboard`.
- [x] **Frontend Goals Management System:** Built `frontend/app/goals/page.tsx` with KPI overview strip, multi-goal AI runway alert banner, and interactive goal cards. Built `GoalFormModal.tsx` for create/edit and `GoalContributeModal.tsx` for deposit/withdraw with instant forecast previews. Added Goals link with `Target` icon to `Navbar.tsx`.
- [x] **Automated Testing:** Added `backend/tests/test_goals.py` (lifecycle and multi-user zero-trust tenancy) and `backend/tests/test_api_health_score.py` (health score and runway calculations). 100% test suite passing (4/4 passed).
- [x] **Production Build:** Verified Next.js production build (`npm run build`) compiles cleanly with 0 errors and 0 warnings.

---

## Phase 28 — In-Database RAG Architecture for Spendora AI Financial Assistant ✅ Done

**Goal:** Transform Spendora's AI Financial Assistant from answering only canned sample questions to answering all custom natural language inquiries regarding the user's specific financial data (merchants, category budgets, active savings goals, cash flow, and temporal spending) via a zero-cost, 100% free-tier compatible in-database Retrieval-Augmented Generation (RAG) architecture.

### Root Cause Analysis & Solution
- **The Issue:** The AI chatbot endpoint (`POST /api/v1/ai/chat`) previously only loaded 5 recent expenses and top-level monthly totals into context. If a user asked about a specific merchant (e.g. Starbucks, Amazon), category drill-down, or goals, the LLM lacked the facts to answer, and the deterministic fallback dropped through to a generic greeting: *"👋 Hello! I am Spendora AI... Here is your live financial snapshot"*.
- **The In-Database RAG Solution:** Built an in-database semantic and fuzzy retrieval engine (`ILIKE`, date bounds, category bindings, and goal relationships) directly querying PostgreSQL with zero latency, zero vector DB subscription costs, and strict tenant isolation (`current_user.id`).

### Completed Tasks
- [x] **RAG Entity & Intent Analyzer:** Built `_extract_rag_query_metadata` in `backend/app/routers/ai.py` detecting candidate merchant terms, matching user category names, resolving temporal bounds (`today`, `yesterday`, `this week`, `this month`, `last month`, ISO dates), and classifying domain intents (`is_goal`, `is_budget`, `is_income`, `is_afford`, `is_safe_spend`, `is_leak`, `is_highest`).
- [x] **Multi-Store Targeted In-Database Retrieval:** In `routers/ai.py`, dynamically queries `ExpenseRepository` for search matches and sums, `CategoryRepository` for drill-downs, `BudgetRepository` for breached (`spent > amount`) and near-limit (`spent >= 0.8 * amount`) budgets, `GoalRepository` for active milestone progress % and remaining runway, and `IncomeRepository` for source breakdowns.
- [x] **Augmented LLM Prompt Engineering:** Structured full RAG facts (`retrieved_knowledge` + `financial_telemetry`) in `backend/app/services/ai_service.py` commanding the model to ground all figures, itemize purchases in markdown tables, and explicitly notify users when 0 matching records exist for a queried item.
- [x] **Deterministic RAG Fallback Engine:** Upgraded `deterministic_chat_response` in `ai_service.py` with intelligent handlers for merchant search (with itemized date/amount tables or zero-match notices), category budget comparisons, goals progress tables with 1-click navigation to `/goals`, budget breach alerts with 1-click `set_budget` triggers, income source breakdowns, and tailored data-grounded summaries.
- [x] **Frontend Markdown Table & Starter Prompts Upgrade:** Updated `frontend/components/FinancialAssistantWidget.tsx` with responsive multi-column table styling (`overflow-x-auto` with dynamic grid templates) and refreshed starter prompt chips showcasing Savings Goals and Budget Alerts.
- [x] **Automated Testing:** Added `test_rag_chat_merchant_search_and_zero_matches` and `test_rag_chat_category_and_goals` in `backend/tests/test_api_ai.py`. All 7 AI tests passing (100% pass rate).
- [x] **Production Build:** Verified Next.js production build (`npm run build`) compiles cleanly with 0 errors and 0 warnings across all 13 routes.

---

## Phase 29 — Safe Web Archiving & Android Foundation (Kotlin + Compose) ✅ Done

**Goal:** Safely archive the complete Next.js 14 web frontend without data loss, scaffold a modern 100% Native Android application using Kotlin and Jetpack Compose, configure build environments targeting the live Render backend, and establish Spendora's Dark Material 3 theme design tokens.

### Completed Tasks
- [x] **Safe Next.js Web Archiving:** Renamed `frontend/` to `frontend_nextjs_archive/` preserving 100% of web pages, components, configs, and build scripts.
- [x] **Android Project Scaffolding:** Initialized `android/` with standard Gradle wrapper config (`gradle-8.4-bin.zip`), root `build.gradle.kts`, `settings.gradle.kts`, and `app/build.gradle.kts` (AGP 8.2.2 + Kotlin 1.9.22 + Jetpack Compose BOM 2024.02.00).
- [x] **Render Production Integration:** Set default `BASE_URL = "https://spendora-py.onrender.com"` directly inside `app/build.gradle.kts` via `buildConfigField`.
- [x] **Network Security Configuration:** Created `network_security_config.xml` enabling secure production HTTPS with cleartext support for local dev (`10.0.2.2`, `localhost`).
- [x] **Spendora Material 3 Dark Theme:** Implemented `Color.kt`, `Type.kt`, and `Theme.kt` with Deep Slate backgrounds (`#0F172A`), Card Slate (`#1E293B`), Emerald Green (`#10B981`), Rose Red (`#F43F5E`), and Indigo Primary (`#6366F1`).
- [x] **Android Manifest & Entry Activity:** Configured `AndroidManifest.xml` with Internet permissions and created `SpendoraApp.kt` and `MainActivity.kt`.

---

## Phase 30 — Android Network Core, Auth & 4-Digit OTP Wizard ✅ Done

**Goal:** Build production-grade authentication and token lifecycle management for Spendora Android, including secure EncryptedSharedPreferences session caching, OkHttp authentication interceptors with automatic token rotation, and Jetpack Compose screens for Login, Registration, and the interactive 4-Digit OTP password recovery wizard.

### Completed Tasks
- [x] **Data Models & DTOs:** Created `UserModels.kt` mapping FastAPI Pydantic schemas (`UserDto`, `UserLoginRequest`, `UserRegisterRequest`, `AuthSuccessResponse`, `PasswordResetRequest`, `VerifyOtpRequest`, `PasswordResetConfirm`).
- [x] **Secure Storage Layer:** Implemented `SessionManager.kt` leveraging `EncryptedSharedPreferences` with AES256-GCM encryption for storing JWT access tokens, refresh tokens, and logged-in user profiles.
- [x] **OkHttp Interceptor & Authenticator:** Built `AuthInterceptor.kt` injecting `Authorization: Bearer <token>` and `Cookie` headers, and `TokenAuthenticator.kt` automatically intercepting `401 Unauthorized` responses to execute background token rotation via `/api/v1/auth/refresh` and retry requests.
- [x] **Retrofit API Client:** Configured `ApiClient.kt` targeting Render backend with timeout policies, logging, and `AuthApi` interfaces.
- [x] **Repository Layer:** Implemented `AuthRepository.kt` with coroutines, `Dispatchers.IO`, and error response parsing.
- [x] **Auth ViewModel:** Built `AuthViewModel.kt` managing Login/Register states and a live 50-second countdown timer for the OTP flow.
- [x] **Material 3 UI Components:** Created `SpendoraTextField`, `SpendoraButton`, `SpendoraCard`, and `PasswordStrengthIndicator` in `CommonWidgets.kt`.
- [x] **Auth Screens:** Built `LoginScreen.kt`, `RegisterScreen.kt`, and the 3-step interactive `OtpForgotPasswordScreen.kt` with 4 discrete auto-advancing OTP boxes, dev quick-fill, and live password criteria checklist.
- [x] **Navigation Setup:** Configured `Screen.kt` and `AppNavigation.kt` with session-aware `startDestination` and wired into `MainActivity.kt`.

---

## Phase 31 — Core Financial Management (Dashboard, Expenses, Income) ✅ Done

**Goal:** Implement full core financial management on Native Android with Jetpack Compose, including summary KPIs, net cash flow tracking, multi-category and payment mode filtering, paginated expense and income records, interactive Add/Edit bottom sheets, and the main bottom navigation bar.

### Completed Tasks
- [x] **Data Models & DTOs:** Implemented `ExpenseModels.kt`, `IncomeModels.kt`, and `DashboardModels.kt` mapping FastAPI schemas (`CategoryDto`, `ExpenseDto`, `IncomeDto`, `DashboardSummary`, `PaymentMode`, `DailyBudgetAlert`, `MonthlyIncomeSummary`).
- [x] **Retrofit API Interfaces:** Created `ExpenseApi.kt`, `IncomeApi.kt`, and `DashboardApi.kt` and mounted in `ApiClient.kt`.
- [x] **Repository Layer:** Implemented `ExpenseRepository.kt`, `IncomeRepository.kt`, and `DashboardRepository.kt` with coroutines, `Dispatchers.IO`, and error response handling.
- [x] **ViewModels:** Created `DashboardViewModel.kt`, `ExpenseViewModel.kt`, and `IncomeViewModel.kt` handling state flows, multi-parameter filtering, sorting, and pagination.
- [x] **Reusable UI & Transaction Cards:** Built `TransactionCards.kt` (`KpiCard`, `ExpenseItemRow`, `IncomeItemRow`, and INR currency formatter `formatInr`), `BottomNavBar.kt`, `ExpenseFormSheet.kt`, and `IncomeFormSheet.kt`.
- [x] **Dashboard Screen:** Built `DashboardScreen.kt` with User Greeting, Quick Action Buttons (+ Expense, + Income, AI Assistant trigger), 4 KPI summary cards (Income, Spend, Net Cash Flow, Savings Rate %), Category Breakdown, and Recent Expenses with real-time deletion.
- [x] **Expenses Screen:** Built `ExpensesScreen.kt` with live search, category filter chips, ascending/descending sort toggle, paginated list, FAB modal, and Delete confirmation dialogs.
- [x] **Income Screen:** Built `IncomeScreen.kt` with monthly income summary banner, source filtering (`Salary`, `Freelance`, `Investment`, `Gift`), paginated list, and Add/Edit modal.
- [x] **Navigation & Integration:** Configured `AppNavigation.kt` with Material 3 Bottom Navigation Bar and updated `MainActivity.kt`.

---

## Phase 32 — Multi-Period Budgets & Smart Savings Goals Runway ✅ Done

**Goal:** Implement multi-period budget tracking (Daily, Weekly, Monthly, Yearly) and smart savings goals with real-time deposit/withdraw contribution workflows on native Android.

### Completed Tasks
- [x] **Data Models & DTOs:** Built `BudgetModels.kt` and `GoalModels.kt` mapping FastAPI schemas (`BudgetDto`, `BudgetCreateRequest`, `BudgetUpdateRequest`, `GoalDto`, `GoalCreateRequest`, `GoalUpdateRequest`, `GoalContributeRequest`).
- [x] **Retrofit API Interfaces:** Created `BudgetApi.kt` and `GoalApi.kt` registered in `ApiClient.kt`.
- [x] **Repository Layer:** Implemented `BudgetRepository.kt` and `GoalRepository.kt` with coroutines, `Dispatchers.IO`, and error parsing.
- [x] **ViewModels:** Created `BudgetViewModel.kt` and `GoalViewModel.kt` managing period filtering, live budget remaining computation, and milestone progress.
- [x] **Budget Cards & Form Sheet:** Built `BudgetCards.kt` (`PeriodTabRow`, `BudgetCard` with color-coded status badges and dynamic progress bar) and `BudgetFormSheet.kt` (overall and per-category budget creation/editing with period switcher).
- [x] **Goal Cards & Modals:** Built `GoalCards.kt` (`GoalCard` with AI pacing badge, speedup suggestions, deposit/withdraw action triggers), `GoalFormSheet.kt` (goal creation/editing), and `GoalContributeSheet.kt` (deposit and withdraw flow with live balance preview).
- [x] **Screens & Navigation:** Built `BudgetsScreen.kt` and `GoalsScreen.kt`; wired routes in `AppNavigation.kt`.

---

## Phase 33 — Full 7-Feature Spendora AI Suite on Mobile ✅ Done

**Goal:** Port all 7 proprietary Spendora AI intelligence capabilities to Native Android Jetpack Compose with custom Canvas visualizations.

### Completed Tasks
- [x] **Data Models & DTOs:** Created `AiModels.kt` mapping all 7 AI endpoints (`PurchaseSimulationRequest/Response`, `LeakAnalysisResponse`, `SafeToSpendResponse`, `FinancialChatRequest/Response`, `TransactionExtractionRequest/Response`, `FinancialHealthScoreResponse`, `GoalsRunwayResponse`).
- [x] **Retrofit API & Repository:** Created `AiApi.kt` and `AiRepository.kt` connected to live Render backend.
- [x] **AI ViewModel:** Built `AiViewModel.kt` managing state flows for all 7 AI features.
- [x] **Custom Canvas Visualizations:**
  - `SafeToSpendGauge.kt`: Speedometer arc with needle indicator, color gradients, and dynamic burn pace metrics.
  - `FinancialHealthRadar.kt`: 5-pillar spider radar chart plotting Savings Discipline, Budget Adherence, Burn Stability, Cash Cushion, and Leak Control.
- [x] **Interactive Action Sheets:**
  - `PurchaseSimulatorSheet.kt`: 3-tier verdict banners, 3-metric comparison cards, quick-test sample chips, and direct "Add as Expense" shortcut.
  - `LeakHunterSheet.kt`: Active subscriptions, micro-spending leaks (<= ₹150), and annualized drain projections.
  - `SmartScannerSheet.kt`: SMS and Receipt scanner with clipboard paste, Indian bank SMS regex parser, and PII masking.
- [x] **Conversational Assistant:** Built `FinancialAssistantScreen.kt` with message history, quick prompt chips, and Markdown table rendering.
- [x] **Dashboard Integration:** Mounted `SafeToSpendGauge` and `FinancialHealthRadar` on `DashboardScreen.kt`.

---

## Phase 34 — Final Polish, Complete Build Verification & Android Documentation ✅ Done

**Goal:** Verify entire mobile build, eliminate all warnings, and produce comprehensive documentation for Android native deployment.

### Completed Tasks
- [x] **Build Verification:** Successfully built production debug APK at `app/build/outputs/apk/debug/app-debug.apk` with 0 errors.
- [x] **Comprehensive Documentation:** Created `docs/android-guide.md` with architecture breakdown, ADB device deployment instructions, security model, and complete 7 AI feature documentation.
- [x] **Memory Synchronization:** Updated `AGENTS.md` and repository memory; pushed full repository to GitHub `main`.

---

## Phase 35 — Native Android Google OAuth & Credential Manager Integration ✅ Done

**Goal:** Integrate modern Google One-Tap Sign-In via Android Credential Manager targeting the backend `/api/v1/auth/google` endpoint.

### Completed Tasks
- [x] **Google Credential Manager Libraries:** Added `androidx.credentials:credentials:1.3.0`, `androidx.credentials:credentials-play-services-auth:1.3.0`, and `com.google.android.libraries.identity.googleid:googleid:1.1.1`.
- [x] **Web Client ID & SHA-1 Configuration:** Integrated Google Web Client ID `1046881337432-bkvc5rdkignjlhurgc9grct4ksri9l05.apps.googleusercontent.com` via `buildConfigField` and registered debug SHA-1 `E6:78:3C:60:26:6B:2A:24:DB:73:4C:F3:69:8F:04:60:3A:98:7D:30` in Google Cloud Console.
- [x] **Branded UI Component:** Built `GoogleLogoIcon` custom vector and `GoogleSignInButton.kt` with Material 3 styling and loading states.
- [x] **Auth Layer Integration:** Added `googleSignIn` in `AuthApi.kt`, `AuthRepository.kt`, and `AuthViewModel.kt`.
- [x] **Screen Mounting:** Mounted Google Sign-In button on `LoginScreen.kt` and `RegisterScreen.kt`.

---

## Phase 36 — Native Android UI/UX Overhaul, Category Lifecycle Fixes & Formatting Polish ✅ Done

**Goal:** Resolve UUID parse errors, fix category dropdown selection race conditions, overhaul visual design to Midnight Obsidian & Electric Neon palette, make Canvas widgets responsive across screen sizes, and polish financial formatting.

### Completed Tasks
- [x] **UUID String vs Int Normalization:** Resolved `NumberFormatException` during sign-in by updating model entity IDs (`id`, `user_id`, `category_id`) across all DTOs, API interfaces, Repositories, ViewModels, and Sheets from `Int` to `String` (PostgreSQL UUID compatibility).
- [x] **Midnight Obsidian & Electric Neon Theme:** Updated `Color.kt` and `Theme.kt` with deep obsidian background (`#070B12`), dark navy cards (`#131D33`), electric indigo primary (`#6366F1`), vibrant emerald (`#10B981`), and soft rose (`#FF4D6D`).
- [x] **Canvas Dynamic Responsiveness:** Wrapped `SafeToSpendGauge.kt` and `FinancialHealthRadar.kt` in `BoxWithConstraints` for proportional auto-scaling across all device widths.
- [x] **Keyboard Overlap Protection:** Added `Modifier.imePadding()` and `verticalScroll` across `ExpenseFormSheet`, `IncomeFormSheet`, `BudgetFormSheet`, `GoalFormSheet`, and `GoalContributeSheet`.
- [x] **Category Initialization & Selection Fix:**
  - Added `LaunchedEffect(categories)` in `ExpenseFormSheet.kt` to auto-select the first available category upon loading.
  - Added explicit category validation error display and loading fallback.
  - Added `loadCategories()` and `loadExpenses()` in `LaunchedEffect(Unit)` on `DashboardScreen.kt`, `ExpensesScreen.kt`, and `BudgetsScreen.kt`.
- [x] **FastAPI Schema Alignment:** Aligned `ExpenseApi.kt` and `ExpenseRepository.kt` `createExpense` / `updateExpense` return types directly with FastAPI's `ExpenseResponse` root schema; corrected date filter query parameters to `date_from` and `date_to`.
- [x] **Formatting & Action Button Polish:**
  - Removed `-` and `+` prefixes from `ExpenseItemRow` and `IncomeItemRow` amount displays.
  - Replaced solid red/green action buttons on `DashboardScreen.kt` with sophisticated `SurfaceElevated` cards featuring subtle icon badges.
  - Unified `IncomeFormSheet` CTA button to use `PrimaryGradient`.
- [x] **Verification:** Verified `./gradlew assembleDebug` builds with `BUILD SUCCESSFUL` (0 errors) and pushed to GitHub `main`.

---

## Phase 37 — Dedicated AI Feature Sheets & Independent Click Lifecycle ✅ Done

**Goal:** Disconnect all AI dashboard widgets from routing to the AI chat assistant, and create dedicated interactive detail sheets for Safe-to-Spend and Financial Health scoring.

### Completed Tasks
- [x] **Safe-to-Spend Detail Sheet (`SafeToSpendDetailSheet.kt`):** Built dedicated bottom sheet modal showing daily safe burn allowance, live burn pace ratio, days remaining vs passed, remaining buffer, projected month-end balance, AI spending strategy callout, smart action checklist, and live re-evaluation trigger.
- [x] **Financial Health Detail Sheet (`FinancialHealthDetailSheet.kt`):** Built dedicated bottom sheet modal showing 0–100 composite prestige score, tier badges (Elite, Healthy, Vulnerable, Critical), deep dive into all 5 pillars with progress bars and status tags (Savings Discipline, Budget Adherence, Burn Stability, Cash Cushion, Leak Control), itemized AI score booster roadmap, and recalculation trigger.
- [x] **Dashboard Click Handler Decoupling:**
  - Removed `onCardClick = onOpenAiAssistant` from both `SafeToSpendGauge` and `FinancialHealthRadar` on `DashboardScreen.kt`.
  - Wired `SafeToSpendGauge` click to open `SafeToSpendDetailSheet`.
  - Wired `FinancialHealthRadar` click to open `FinancialHealthDetailSheet`.
  - Maintained dedicated AI Chatbot access strictly through the top action bar sparkling button.
- [x] **Build Verification:** Successfully verified `./gradlew assembleDebug` with `BUILD SUCCESSFUL` (0 errors).

---

## Phase 38 — Native Android Brand Identity & Adaptive App Launcher Logo ✅ Done

**Goal:** Design authentic Spendora vector branding assets, adaptive launcher icons, and in-app brand emblem replacing default Android icons and temporary placeholders.

### Completed Tasks
- [x] **Vector Asset Design:**
  - `ic_launcher_background.xml`: Midnight Obsidian gradient (`#070B12` to `#131D33`).
  - `ic_launcher_foreground.xml`: Glowing Spendora "S" financial ribbon gradient (`#38BDF8` → `#6366F1` → `#10B981`) with 4-point sparkle star accent in adaptive icon safe bounds.
  - `ic_spendora_logo.xml`: Full 512x512 vector squircle logo with ambient glow aura.
  - `ic_launcher.xml` & `ic_launcher_round.xml` adaptive icon definitions in both `mipmap-anydpi-v26/` and `drawable/`.
- [x] **Manifest Configuration:** Updated `AndroidManifest.xml` with `android:icon="@drawable/ic_launcher"` and `android:roundIcon="@drawable/ic_launcher_round"`.
- [x] **SpendoraLogo Composable (`SpendoraLogo.kt`):** Built scalable Compose component for high-fidelity logo rendering with squircle containers and subtle border glows.
- [x] **In-App Screen Branding:**
  - Integrated `SpendoraLogo(size = 72.dp)` on `LoginScreen.kt` and `RegisterScreen.kt` (replacing text `✦` placeholders).
  - Integrated `SpendoraLogo(size = 42.dp)` on `DashboardScreen.kt` header row next to user greeting.
- [x] **Build Verification:** Successfully built debug APK with `./gradlew assembleDebug` (`BUILD SUCCESSFUL` in 48s, 0 errors).

---

## Phase 39 — Refined S Logo Proportions & Animated Spendora-to-S Launch Sequence ✅ Done

**Goal:** Refine the Spendora S logo ribbon proportions to eliminate bulkiness, and build a cinematic splash launch animation that smoothly collapses the "Spendora" wordmark into the iconic glowing "S" emblem.

### Completed Tasks
- [x] **Refined S Logo Proportions:**
  - Reduced stroke width on `ic_spendora_logo.xml` from 44 to 28dp with 35% more breathing room within the squircle frame.
  - Reduced stroke width on `ic_launcher_foreground.xml` from 9 to 6dp with centered bounds and proportional 4-point sparkle star.
- [x] **Animated SplashScreen (`SplashScreen.kt`):**
  - **Stage 1 (Intro):** "Spendora" wordmark enters with gradient "S" (`CyanInfoLight` → `PrimaryIndigoLight` → `EmeraldSuccessLight`) and subtle brand tagline.
  - **Stage 2 (Morphing):** Letters `"pendora"` collapse horizontally into `"S"`, while the glowing squircle `SpendoraLogo` expands with spring physics and an ambient radial aura pulse.
  - **Stage 3 (Navigation):** Automatically routes to Dashboard (if authenticated) or Login screen, seamlessly popping `Screen.Splash` off the backstack.
- [x] **Navigation Setup:** Registered `Screen.Splash` in `Screen.kt` and wired as `startDestination` in `AppNavigation.kt`.
- [x] **Build Verification:** Verified `./gradlew assembleDebug` builds with `BUILD SUCCESSFUL in 45s` (0 errors).

---

## Phase 40 — Zero-Hardcoding Dynamic Light & Dark Theme Architecture ✅ Done

**Goal:** Implement a comprehensive dynamic Light Mode and Dark Mode system for Spendora Native Android with **strict zero-hardcoding of colors** when switching modes, ensuring all components and canvas graphics resolve tokens dynamically.

### Completed Tasks
- [x] **Theme Preference & State Management:**
  - Added `themeMode` StateFlow and preferences persistence (`setThemeMode`, `getThemeMode`) in `SessionManager.kt`.
  - Exposed `themeMode` in `AuthRepository.kt` and `AuthViewModel.kt` with a one-tap `toggleTheme()` function.
- [x] **Zero-Hardcoded Semantic Color System (`Color.kt`):**
  - Defined semantic `SpendoraColors` data class containing dynamic tokens: `background`, `surface`, `surfaceCard`, `surfaceElevated`, `border`, `borderGlow`, `textPrimary`, `textSecondary`, `textMuted`, `primary`, `primaryLight`, `primaryDark`, `accent`, `emerald`, `amber`, `rose`, `cyan`, and `purple`.
  - Configured `SpendoraDarkColors` (Midnight Obsidian `#070B12`, `#131D33`, `#6366F1`) and `SpendoraLightColors` (Pure Pearl Slate `#F8FAFC`, `#FFFFFF`, `#0F172A`, `#64748B`, `#4F46E5`).
  - Created dynamic `@Composable get()` delegates on `Color.kt` companion objects for seamless, backwards-compatible, zero-hardcoding theme access across legacy references.
- [x] **Dynamic Theme Provider (`Theme.kt`):**
  - Created `LocalSpendoraColors` static composition local and `SpendoraTheme.colors` accessor.
  - Configured dynamic `DarkColorScheme` and `LightColorScheme` matching Material 3 specifications.
  - Dynamically updated Android system bars (`isAppearanceLightStatusBars = !isDark` and `isAppearanceLightNavigationBars = !isDark`) based on the active theme mode.
- [x] **Typography Color Decoupling (`Type.kt`):** Removed static color locks from typography definitions so text color inherits dynamically from semantic tokens.
- [x] **Interactive Sun ☀️ / Moon 🌙 Toggle (`DashboardScreen.kt`):**
  - Added a responsive, glassmorphic theme toggle button to the dashboard header row.
  - Updated custom Compose Canvas draw scopes in `SafeToSpendGauge.kt` and `FinancialHealthRadar.kt` to capture dynamic theme tokens before rendering.
- [x] **Build Verification:** Successfully built debug APK with `./gradlew assembleDebug` (`BUILD SUCCESSFUL in 1m 52s`, 0 errors).

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
| 11 | Production Docker & Python 3.12 Type Evaluation | ✅ Resolved | Mandatory `from __future__ import annotations` and top-level imports; context-agnostic Dockerfile supporting root `.` and `./backend` |
| 12 | Financial Health Scoring & Smart Goals Architecture | ✅ Resolved | 5-weighted pillars (Savings 25%, Budgets 25%, Burn 20%, Cushion 15%, Leaks 15%) + dedicated `goals` table with deposit/withdraw contribution tracking and cash flow surplus runway acceleration |
| 13 | In-Database RAG Architecture for Conversational AI | ✅ Resolved | In-database dynamic entity & temporal extraction querying PostgreSQL via ILIKE and relational bounds; 0 vector DB subscription cost, 0ms external latency, zero-trust tenant isolation |
| 14 | Mobile Frontend Architecture & Web Archiving | ✅ Resolved | Next.js preserved in `frontend_nextjs_archive/`; Native Android built in `android/` with Kotlin + Jetpack Compose + Material 3 + Retrofit targeting `https://spendora-py.onrender.com` |
| 15 | Android Model IDs & PostgreSQL UUID Compatibility | ✅ Resolved | All model entity IDs (`id`, `user_id`, `category_id`) typed strictly as `String` across DTOs and database calls to support standard UUIDs without `NumberFormatException` |
| 16 | Google OAuth Android Architecture | ✅ Resolved | Credential Manager with `googleid` library, Web Client ID defined via `buildConfigField`, and debug SHA-1 registered in Google Cloud Console |
| 17 | Android UI/UX & Canvas Responsiveness | ✅ Resolved | Midnight Obsidian theme `#070B12`, `BoxWithConstraints` scaling for AI gauges, `imePadding` on all form sheets, and clean INR formatting |
| 18 | Zero-Hardcoded Light & Dark Theme Token Architecture | ✅ Resolved | `LocalSpendoraColors` + `SpendoraTheme.colors` dynamic composition local tokens persisted via `SessionManager.kt` with live header toggle |


