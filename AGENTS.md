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
- **`routers/`**: HTTP endpoints (`categories.py`, `expenses.py`, `budgets.py`, `dashboard.py`, `api_router.py`)
- **`services/`**: Business logic, budget threshold computations, category reassignment (`budget_service.py`, `dashboard_service.py`, `category_service.py`, `expense_service.py`)
- **`repositories/`**: Raw async SQLAlchemy queries, pagination, search, aggregations
- **`schemas/`**: Pydantic v2 schemas for request validation & response serialization
- **`models/`**: SQLAlchemy 2.0 ORM models (`Category`, `Expense`, `Budget`, `PaymentMode`)
- **`core/`**: Configuration (`config.py` with auto-normalizing `DATABASE_URL` validator) and async engine (`database.py` with `statement_cache_size=0` for Supabase poolers)

### 2. Frontend Architecture
`frontend/`
- **`app/`**: Next.js App Router (`/` Landing redirect, `/dashboard` Full analytics & modals, `/expenses` Filtering & pagination table)
- **`components/`**: Modular UI components (`Button`, `Card`, `Badge`, `Dialog`, `LoadingSkeleton`, `EmptyState`, `Hero3D`, `ExpenseForm`, `BudgetModal`, `CategoryModal`, `ToastNotification`)
- **`lib/`**: Type-safe HTTP client (`api.ts`), Zod validation schemas (`schemas.ts`), utils (`utils.ts`)

### 3. Database & Migrations
- **Alembic:** Located in `backend/alembic/`. Migrations read `DATABASE_URL` dynamically from environment.
- **Seeding:** Automatically checks and seeds 9 standard categories on lifespan startup if empty.
- **Enums & Formats:** `PaymentMode` (`Cash`, `Card`, `UPI`, `Net Banking`, `Other`). `period_month` is always formatted as `YYYY-MM-01` date object.

---

## 🔒 Security & Environment Rules
1. **Never commit `.env` or `.env.local` files.** Always update `.env.example`.
2. **Environment-Driven URLs:** `NEXT_PUBLIC_API_URL` for frontend, `DATABASE_URL`, `CORS_ORIGINS`, `BUDGET_NEAR_LIMIT_THRESHOLD`, `PORT` for backend.
3. **Responsive UI:** All screens tested on mobile, tablet, and desktop viewports.
4. **Git Discipline:** Clean commit messages and immediate push to `main` upon task completion.

---

## 📊 Completed Phases (1 through 8)
- ✅ **Phase 1:** Project Scaffolding & Folder Structure
- ✅ **Phase 2:** Backend Foundation (FastAPI, asyncpg, models, Alembic)
- ✅ **Phase 3:** Backend API Implementation (all 15+ endpoints)
- ✅ **Phase 4:** Frontend Foundation (Next.js, Tailwind, Radix UI, Framer Motion, 3D visual)
- ✅ **Phase 5:** Comprehensive Testing (38/38 pytest tests passing)
- ✅ **Phase 6:** Complete Frontend Pages & Modals
- ✅ **Phase 7:** End-to-end Integration & Validation (0 hardcoded values)
- ✅ **Phase 8:** CI/CD (GitHub Actions), Dockerfiles (Root & Backend), Supabase pooler compatibility, and Render/Vercel configuration
