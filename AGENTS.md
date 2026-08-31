# Spendora — Repository Context & Agent Memory (AGENTS.md)

## 📌 Project Overview
**Spendora V1** is a high-performance personal expense and budget tracking web application built with a modern, decoupled full-stack architecture.

- **Stack:** FastAPI (Python 3.12/3.14) + Next.js 14 (App Router, TypeScript, Tailwind CSS) + PostgreSQL (Supabase) + SQLAlchemy 2.0 (asyncpg)
- **Deployment Architecture:**
  - **Backend:** Render Web Service (FastAPI running via Uvicorn in Docker)
  - **Frontend:** Vercel (Next.js production build)
  - **Database:** Supabase Managed PostgreSQL (Session Pooler mode on port 5432)
- **Repository:** `https://github.com/rasikakudale90/Spendora_py`

---

## 🏛️ Architecture & Code Conventions

### 1. Backend Architecture (5-Layer Pattern)
`backend/app/`
- **`routers/`**: HTTP endpoints (`categories.py`, `expenses.py`, `budgets.py` [GET, POST, PATCH, DELETE], `dashboard.py`, `api_router.py`)
- **`services/`**: Business logic, multi-period budget threshold computations, category reassignment (`budget_service.py`, `dashboard_service.py`, `category_service.py`, `expense_service.py`)
- **`repositories/`**: Raw async SQLAlchemy queries, pagination, search, aggregations (`budget_repository.py` with multi-period bounds, `category_repository.py`, `expense_repository.py`)
- **`schemas/`**: Pydantic v2 schemas (`budget.py` with `period_type` bounds & `BudgetUpdate`, `expense.py` with past-or-today date validation)
- **`models/`**: SQLAlchemy 2.0 ORM models (`Category`, `Expense`, `Budget` with `period_type`, `period_start`, `period_end`, `PaymentMode`)
- **`core/`**: Configuration (`config.py` with auto-normalizing `DATABASE_URL` validator) and async engine (`database.py` with `statement_cache_size=0` for Supabase poolers)

### 2. Frontend Architecture
`frontend/`
- **`app/`**: Next.js App Router (`/` Landing redirect, `/dashboard` Full analytics & modals, `/expenses` Filtering & pagination table, `manifest.ts` PWA manifest)
- **`components/`**: Modular UI components (`Button`, `Card`, `Badge`, `Dialog`, `LoadingSkeleton`, `EmptyState`, `Hero3D`, `ExpenseFormModal`, `BudgetManagerModal` [with Weekly/Monthly/Yearly tabs, Edit & Delete], `CategoryModal`, `PwaRegister` [Install banner & offline toasts])
- **`lib/`**: Type-safe HTTP client (`api.ts` with full CRUD for expenses, budgets, categories), Zod validation schemas (`schemas.ts`), utils (`utils.ts`)
- **`public/`**: Progressive Web App assets (`sw.js` Service Worker with offline caching, `manifest.json`, `icons/` standard 192/512px & maskable adaptive icons, vector SVG icons)

### 3. Database & Migrations
- **Alembic:** Located in `backend/alembic/`. Migrations read `DATABASE_URL` dynamically from environment.
  - Migration `d5e1b2f3a4b5_add_budget_period_types.py` added `period_type` (`weekly`, `monthly`, `yearly`), `period_start`, `period_end`, check constraint, and composite unique indexes.
- **Seeding:** Automatically checks and seeds standard starter categories on lifespan startup if empty.
- **Enums & Formats:** `PaymentMode` (`Cash`, `Card`, `UPI`, `Net Banking`, `Other`). `period_type` (`daily`, `weekly`, `monthly`, `yearly`). `period_start` and `period_end` are ISO date objects. Remaining balances clamped to `>= 0.00`.

---

## 🔒 Security & Environment Rules
1. **Never commit `.env` or `.env.local` files.** Always update `.env.example`.
2. **Environment-Driven URLs:** `NEXT_PUBLIC_API_URL` for frontend, `DATABASE_URL`, `CORS_ORIGINS`, `BUDGET_NEAR_LIMIT_THRESHOLD`, `PORT` for backend.
3. **Responsive UI:** All screens tested on mobile, tablet, and desktop viewports.
4. **Git Discipline:** Clean commit messages and immediate push to `main` upon task completion.

---

## 📊 Completed Phases (1 through 17)
- ✅ **Phase 1:** Project Scaffolding & Folder Structure
- ✅ **Phase 2:** Backend Foundation (FastAPI, asyncpg, models, Alembic)
- ✅ **Phase 3:** Backend API Implementation (all 15+ endpoints)
- ✅ **Phase 4:** Frontend Foundation (Next.js, Tailwind, Radix UI, Framer Motion, 3D visual)
- ✅ **Phase 5:** Comprehensive Testing (pytest test suite passing)
- ✅ **Phase 6:** Complete Frontend Pages & Modals
- ✅ **Phase 7:** End-to-end Integration & Validation (0 hardcoded values)
- ✅ **Phase 8:** CI/CD (GitHub Actions), Dockerfiles (Root & Backend), Supabase pooler compatibility, and Render/Vercel configuration
- ✅ **Phase 9:** Expense Sorting Fixes (Corrected `sort_order` mapping for `amount` and `expense_date` ascending/descending)
- ✅ **Phase 10:** Multi-Period Budgeting System (Weekly, Monthly, and Yearly budget limits, Alembic migration `d5e1b2f3a4b5`, period bounds logic, and tabbed period UI)
- ✅ **Phase 11:** Budget Update & Deletion Lifecycle (`PATCH /api/v1/budgets/{id}` and `DELETE /api/v1/budgets/{id}` endpoints, repository methods, and interactive Edit/Delete controls in `BudgetManagerModal`)
- ✅ **Phase 12:** Financial Accuracy & Date Constraints (Remaining budget clamped to ₹0.00 on overspending; future expense dates blocked in native datepicker and Zod validation)
- ✅ **Phase 13:** Category Sanitation & Test Isolation (Renamed `Hobbies_b0c83e` to clean `Hobbies` in database; introduced `try...finally` in lifecycle tests to prevent test entity leakage)
- ✅ **Phase 14:** Progressive Web App (PWA) Implementation (Web App Manifests `manifest.ts` & `manifest.json`, Service Worker `sw.js` with offline caching and Stale-While-Revalidate, standard & maskable icons, `PwaRegister` install prompt banner, and Next.js 14 viewport configuration)
- ✅ **Phase 15:** Daily Expense Limit & Over-Budget Pop-up Alert (Added `'daily'` period type across database constraint `ck_budgets_period_type_valid` via migration `e6f2a3b4c5d6`, ORM models, Pydantic schemas, and frontend API client. Added `DailyBudgetAlert` schema and automatic real-time breach detection on expense creation/update. Created `DailyLimitAlertModal` pop-up dialog, configured Daily tab in `BudgetManagerModal` with full edit/delete lifecycle, and integrated persistent over-budget banner and session alert on the dashboard)
- ✅ **Phase 16:** Income Tracking & Cash Flow Analytics (Added dedicated `incomes` database table via Alembic migration `f7e8d9c0b1a2`, ORM model `Income`, Pydantic schemas, `IncomeRepository`, `IncomeService`, and REST endpoints under `/api/v1/incomes`. Integrated monthly total income, net savings [Cash Flow = Income - Expenses], and savings rate % into the dashboard summary and KPI cards. Created dedicated `/income` management page with source filtering, search, sorting, and responsive `IncomeFormModal` and `DeleteIncomeConfirmModal`).
- ✅ **Phase 17:** Postman API Testing Suite & Router Polish (Built complete `Spendora_API.postman_collection.json` v2.1 with dynamic variable chaining [baseUrl, categoryId, expenseId, budgetId, incomeId], created `Spendora_Local.postman_environment.json` & `Spendora_Production.postman_environment.json`, added `GET /api/v1/categories/{id}` and `PUT` alias in categories router, and enhanced dashboard router to flexibly parse both `YYYY-MM` and `YYYY-MM-DD` period strings).
