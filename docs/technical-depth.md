# Spendora V1 — Technical Architecture & Deep-Dive Reference

> **Document Version:** 1.0.0  
> **Last Updated:** 2026-08-26  
> **Prepared For:** Phase 8 CI/CD & Production Deployment

---

## 1. Executive Summary & Architecture Overview

**Spendora V1** is a high-performance personal expense and budget tracking web application built with a decoupled architecture:
- **Backend:** FastAPI (Python 3.11+) + SQLAlchemy 2.0 (asyncio + asyncpg) + PostgreSQL
- **Frontend:** Next.js 14 (App Router, React 18, TypeScript) + Tailwind CSS + Radix UI + Recharts + React Three Fiber
- **Database:** PostgreSQL (with Supabase compatibility) + Alembic async database migrations
- **Target Production Infrastructure:** 
  - Backend API: Render (Web Service / Docker)
  - Frontend SPA/SSR: Vercel (Edge / Serverless Node)
  - Database: Managed PostgreSQL (Supabase / Neon / Render Postgres)

```
┌─────────────────────────────────────────────────────────────┐
│                    Frontend (Next.js 14)                    │
│   • App Router (/dashboard, /expenses)                      │
│   • Client Components (Modals, Recharts, Three.js Hero)     │
│   • Server Communication via Type-safe api.ts client        │
└──────────────────────────────┬──────────────────────────────┘
                               │ JSON over HTTPS (/api/v1/*)
                               ▼
┌─────────────────────────────────────────────────────────────┐
│                    Backend API (FastAPI)                    │
│   • Routers (/categories, /expenses, /budgets, /dashboard)  │
│   • Services (Business logic, MoM calculations, validation) │
│   • Repositories (Async SQLAlchemy queries & aggregations)  │
│   • PostgreSQL Engine (asyncpg connection pool)             │
└──────────────────────────────┬──────────────────────────────┘
                               │ Async SQL / Migrations
                               ▼
┌─────────────────────────────────────────────────────────────┐
│                   PostgreSQL Database (Supabase)            │
│   • Tables: categories, expenses, budgets                   │
│   • Partial Unique Indexes, UUID PKs, Safe-delete foreigns │
└─────────────────────────────────────────────────────────────┘
```

---

## 2. Backend Design & Layered Architecture

The backend strictly implements a **clean 5-layer separation of concerns**:

### 2.1 Layer Breakdown
1. **Entrypoint & Lifespan (`backend/app/main.py`)**: 
   - Initializes FastAPI application with CORS middleware.
   - Async lifespan hook runs on startup to seed starter categories (`Food`, `Transport`, `Rent`, `Shopping`, `Education`, `Entertainment`, `Bills`, `Healthcare`, `Other`) idempotently.
   - Provides `/health` liveness probe.
2. **Routers (`backend/app/routers/`)**:
   - `category_router.py`: Category CRUD (`/api/v1/categories`)
   - `expense_router.py`: Expense CRUD + Search/Filter/Sort/Pagination (`/api/v1/expenses`)
   - `budget_router.py`: Budget management (`/api/v1/budgets`)
   - `dashboard_router.py`: 7 Analytics endpoints (`/api/v1/dashboard/*`)
3. **Services (`backend/app/services/`)**:
   - Encapsulates pure business logic, calculations, date window normalization, and threshold evaluations.
   - `category_service.py`: Enforces safe delete rules (rejects deletion with 409 Conflict if expenses are attached, or performs reassignments).
   - `budget_service.py`: Computes live spent, remaining amount, percentage utilized, and assigns dynamic status enum (`on_track`, `near_limit` [>=80% default], `over_budget` [>=100%]).
   - `dashboard_service.py`: Aggregates Month-over-Month (MoM) delta percentages, category breakdowns, daily/weekly averages, and highest expenses.
4. **Repositories (`backend/app/repositories/`)**:
   - Directly executes optimized async SQLAlchemy queries.
   - Utilizes `select()`, `func.coalesce()`, `func.sum()`, `func.count()`, and window functions without N+1 query overhead.
5. **Models & Schemas (`backend/app/models/` & `backend/app/schemas/`)**:
   - SQLAlchemy ORM models with `TimestampMixin` (`created_at`, `updated_at` with UTC timezone defaults).
   - Pydantic v2 schemas for strict input validation, date coercion, and output serialization.

---

## 3. Database Schema & Integrity

### 3.1 Entity Relationship Diagram

```
 +---------------------------------------------------------+
 |                       categories                        |
 +---------------------------------------------------------+
 | id          : UUID (PK, gen_random_uuid())              |
 | name        : VARCHAR(50) (UNIQUE, NOT NULL)            |
 | created_at  : TIMESTAMPTZ (DEFAULT now())               |
 | updated_at  : TIMESTAMPTZ (DEFAULT now())               |
 +----------------------------+----------------------------+
                              | 1
                              |
                              | 0..N
 +----------------------------v----------------------------+
 |                        expenses                         |
 +---------------------------------------------------------+
 | id           : UUID (PK, gen_random_uuid())             |
 | title        : VARCHAR(50) (NOT NULL)                   |
 | category_id  : UUID (FK -> categories.id, RESTRICT)     |
 | amount       : NUMERIC(10,2) (CHECK amount > 0)         |
 | expense_date : DATE (NOT NULL, <= CURRENT_DATE)         |
 | payment_mode : VARCHAR(20) (Enum: Cash, Card, UPI, etc.)|
 | notes        : VARCHAR(500) (NULLABLE)                  |
 | created_at   : TIMESTAMPTZ (DEFAULT now())              |
 | updated_at   : TIMESTAMPTZ (DEFAULT now())              |
 +---------------------------------------------------------+

 +---------------------------------------------------------+
 |                         budgets                         |
 +---------------------------------------------------------+
 | id           : UUID (PK, gen_random_uuid())             |
 | scope        : VARCHAR(20) ('overall' | 'category')     |
 | category_id  : UUID (FK -> categories.id, NULLABLE)     |
 | amount       : NUMERIC(10,2) (CHECK amount > 0)         |
 | period_month : DATE (Stored as first of month YYYY-MM-01)|
 | created_at   : TIMESTAMPTZ (DEFAULT now())              |
 | updated_at   : TIMESTAMPTZ (DEFAULT now())              |
 +---------------------------------------------------------+
   * Partial Unique Index: (period_month) WHERE scope='overall'
   * Partial Unique Index: (period_month, category_id) WHERE scope='category'
```

---

## 4. Frontend Architecture & Design System

### 4.1 Component Hierarchy & Pages
- **Root Layout (`frontend/app/layout.tsx`)**:
  - Sets global dark-theme tokens (`bg-slate-950`, `text-slate-100`).
  - Configures responsive navigation header with hamburger drawer for mobile viewports.
  - Mounts `<Toaster />` from `sonner` for rich feedback notifications.
- **Dashboard (`frontend/app/dashboard/page.tsx`)**:
  - Integrates Three.js / React Three Fiber interactive hero element.
  - 4 Key KPI Cards: Total Spent, Budget Remaining (with dynamic progress bar indicator), Avg Daily Spend, and Highest Expense.
  - Recharts Visualizations: Trend Bar Chart & Category Breakdown Donut/Pie Chart.
  - Recent Transactions table.
  - Quick-action modals: `CategoryManagerModal` & `BudgetManagerModal`.
- **Expenses Page (`frontend/app/expenses/page.tsx`)**:
  - Full-featured data table.
  - Real-time search by title, category filtering, multi-field sorting, and pagination.
  - `ExpenseFormModal`: Add / Edit with Zod schema validation.
  - `DeleteConfirmModal`: Safe destructive confirmation dialog.

---

## 5. Test Suite & Validation Matrix

### 5.1 Backend Pytest Suite (38/38 Passing)
- **Unit Validation Tests (`test_validation.py`)**: 20 tests verifying schema validation rules, negative amounts, future dates, long strings, and enum constraints.
- **Service Tests (`test_budget_service.py`, `test_dashboard_service.py`)**: Validates threshold logic (80% near-limit, 100% over-budget) and MoM delta math.
- **REST API Endpoint Tests (`test_api_*.py`)**: Full HTTP assertions covering all 15 endpoints.
- **End-to-End User Lifecycle Integration (`test_integration_lifecycle.py`)**: Simulates user onboarding -> seeding -> category creation -> budgeting -> expense logging -> dashboard verification -> category safe-delete conflict.

### 5.2 Integration & Build Verification
- **Frontend Build:** `npm run build` generates clean production static bundles with zero linting or TypeScript errors.
- **Contract Compatibility:** Validated date normalization (`YYYY-MM-01`) between frontend forms and backend models.

---

## 6. Phase 8 Production Deployment Blueprint

When resuming in the next session for **Phase 8**, execute the following deployment steps:

### Step 1: Dockerize Backend
- **File:** `backend/Dockerfile`
- Multi-stage Python 3.11 slim image.
- Expose port `8000`, run `uvicorn app.main:app --host 0.0.0.0 --port 8000`.

### Step 2: Dockerize Frontend
- **File:** `frontend/Dockerfile`
- Multi-stage standalone Next.js build.
- Expose port `3000`.

### Step 3: GitHub Actions CI/CD Pipeline
- **File:** `.github/workflows/ci-cd.yml`
- Stages:
  1. `backend-test`: Run `pytest` with async PostgreSQL service container.
  2. `frontend-build`: Run `npm run lint` and `npm run build`.
  3. `deploy`: Deploy backend to Render via deploy hook & frontend to Vercel.

### Step 4: Environment Variables Checklist
- **Backend (Render):**
  - `DATABASE_URL`: `postgresql+asyncpg://<user>:<password>@<supabase-host>:5432/<dbname>`
  - `BUDGET_NEAR_LIMIT_THRESHOLD`: `0.80`
  - `CORS_ORIGINS`: `https://spendora-py.vercel.app`
  - `JWT_SECRET_KEY`: `<cryptographically-secure-64-char-hex>`
  - `GOOGLE_CLIENT_ID`: `<google-oauth-client-id>.apps.googleusercontent.com`
  - `FRONTEND_URL`: `https://spendora-py.vercel.app`
  - `COOKIE_SECURE`: `true`
  - `EMAIL_API_URL`: `https://api.resend.com/emails` (or leave blank to fall back to SMTP)
  - `EMAIL_API_KEY`: `re_<resend-api-key>`
  - `EMAIL_FROM`: `Spendora <onboarding@resend.dev>` (or custom verified domain)
- **Frontend (Vercel):**
  - `NEXT_PUBLIC_API_URL`: `https://spendora-py.onrender.com`
  - `NEXT_PUBLIC_GOOGLE_CLIENT_ID`: `<google-oauth-client-id>.apps.googleusercontent.com`

---

## 7. Authentication & Email Provider Architecture

### 7.1 Security & Multi-Tenancy Architecture
- **Tenant Isolation:** Zero-trust row-level scoping across all repositories and service layers. Every entity query is filtered by `user_id`.
- **JWT Lifecycles:** 15-minute access tokens signed with HMAC-SHA256 (`HS256`) stored in-memory in the client; 30-day refresh tokens stored in HttpOnly, SameSite cookies with SHA-256 database hashing.
- **Rotation & Reuse Detection:** Every refresh operation rotates the token pair and updates `refresh_tokens.replaced_by`. If a revoked or already-replaced refresh token is presented, all active sessions for that user family are revoked immediately.
- **Rate Limiting:** SlowAPI enforces memory-based rate limiting per endpoint (`10/min` on register, `15/min` on login, `5/min` on password recovery).

### 7.2 100% Environment-Driven Email System
- **Provider-Agnostic Dispatcher:** `EmailService` automatically inspects `EMAIL_API_URL` and `EMAIL_API_KEY`:
  - **Production (Resend HTTP API):** Dispatches asynchronous HTTPS POST requests to `https://api.resend.com/emails` using `httpx.AsyncClient` with zero outbound SMTP port blocking risk.
  - **Local Development (Gmail SMTP Fallback):** Falls back to standard TLS SMTP (`smtp.gmail.com:587`) using `email.mime` and `smtplib` run inside `asyncio.to_thread`.
- **Transactional Templates:** Responsive, dark-glassmorphic HTML emails with emerald branding for Welcome Registration and Password Reset flows.
- **Keep-Alive Automation:** `.github/workflows/keep-alive.yml` fires a lightweight health check every 10 minutes (`*/10 * * * *`) against `https://spendora-py.onrender.com/health` to eliminate Render free-tier cold starts.

