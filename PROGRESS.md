# Spendora — Project Progress Tracker

## Project
**Spendora V1** — Personal Expense & Budget Tracking Web App  
**Stack:** FastAPI + Next.js + PostgreSQL (Supabase) + SQLAlchemy 2.0 async  
**Deployment:** Backend → Render | Frontend → Vercel | DB → Supabase

---

## Phase Overview

| Phase | Name | Status | Completed |
|-------|------|--------|-----------|
| 1 | Project Scaffolding & Folder Structure | ✅ Done | 2026-08-26 |
| 2 | Backend Foundation (FastAPI setup, DB config, models, Alembic) | ✅ Done | 2026-08-26 |
| 3 | Backend API Implementation (all endpoints) | ✅ Done | 2026-08-26 |
| 4 | Frontend Foundation (Next.js init, Tailwind, shadcn/ui) | 🔄 Next | — |
| 5 | Frontend Pages (Dashboard + Expenses) | ⬜ Pending | — |
| 6 | Integration, Validation & Testing | ⬜ Pending | — |
| 7 | CI/CD, Dockerfiles & Deployment | ⬜ Pending | — |

---

## Phase 1 — Project Scaffolding ✅

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
- [x] `__init__.py` for all app sub-packages (routers, schemas, services, repositories, tests)
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

## Phase 4 — Frontend Foundation ⬜

**Goal:** Scaffold Next.js app with all tooling wired up.

### Planned Tasks
- [ ] `npx create-next-app` — TypeScript + Tailwind + App Router
- [ ] Install shadcn/ui, Framer Motion, React Three Fiber, Recharts/Chart.js, Zod, React Hook Form
- [ ] `frontend/.env.example` — `NEXT_PUBLIC_API_URL`
- [ ] Global layout, theme, typography (Inter/Outfit from Google Fonts)
- [ ] Hamburger navigation (Dashboard + Expenses links)
- [ ] API client helpers in `frontend/lib/api.ts`
- [ ] Shared Zod schemas in `frontend/lib/schemas.ts`

---

## Phase 5 — Frontend Pages ⬜

**Goal:** Build complete UI for Dashboard and Expenses pages.

### Planned Tasks
- [ ] Dashboard: total spend, budget status, recent expenses, pie chart, trend chart, comparison, top categories, stats
- [ ] Expenses: paginated list, search, filter (date/category/amount/payment mode), sort
- [ ] Add Expense form (title, amount, date, category, payment_mode, notes — validated)
- [ ] Edit Expense form
- [ ] Delete Expense (confirmation dialog)
- [ ] Categories management (create, rename, safe-delete with reassign)
- [ ] Budget management (set overall + per-category budgets)
- [ ] Toast notifications for all CRUD actions
- [ ] Empty / Loading / Error states for every screen
- [ ] Framer Motion: page transitions, list enter/exit, hover/tap feedback
- [ ] React Three Fiber: one contained 3D visual (placement TBD — see Open Items)
- [ ] Responsive: mobile, tablet, desktop

---

## Phase 6 — Integration, Validation & Testing ⬜

**Goal:** End-to-end verified, all states covered, zero hardcoded data.

### Planned Tasks
- [ ] Connect frontend to backend (all 15+ endpoints)
- [ ] Verify all CRUD flows end-to-end
- [ ] Verify all filter/search/sort combinations
- [ ] Verify budget remaining recalculates after every expense change
- [ ] Verify category safe-delete (409 + reassign flow)
- [ ] Cross-device responsiveness (mobile/tablet/desktop)
- [ ] All empty/loading/error states verified
- [ ] Zero hardcoded financial values confirmed

---

## Phase 7 — CI/CD, Dockerfiles & Deployment ⬜

**Goal:** Production-ready with GitHub Actions pipeline and live deployment.

### Planned Tasks
- [ ] Finalize `backend/Dockerfile`
- [ ] Finalize `frontend/Dockerfile`
- [ ] GitHub Actions: lint → type-check → test → build → deploy
- [ ] Deploy backend to Render
- [ ] Deploy frontend to Vercel
- [ ] Supabase DB connection verified from production
- [ ] Smoke test on live deployment

---

## Open Items (Confirm Before Implementation)

| # | Item | Status |
|---|------|--------|
| 1 | Repository/service layer pattern — full 5-layer vs simplified direct SQLAlchemy in routes? | ❓ Pending |
| 2 | Payment Mode enum — finalize list (Cash, Card, UPI, Net Banking, Other proposed) | ❓ Pending |
| 3 | Near-limit budget threshold — confirm 80% default or different value? | ❓ Pending |
| 4 | FR-22 report views — filtered dashboard or separate screen? | ❓ Pending |
| 5 | React Three Fiber placement — which screen/moment gets the 3D treatment? | ❓ Pending |
