# Spendora — Software Requirements Specification (SRS)
## Version 1 (V1) — Expense & Budget Tracking Web Application

*Derived from the Spendora PRD (Merged). This SRS translates that PRD into concrete technical requirements for implementation.*

---

## 1. Introduction

### 1.1 Purpose
This document specifies the technical requirements, architecture, data model, and API contract for Spendora V1 — a single-user personal expense and budget tracking web application. It is written to be directly actionable by a development team (human or AI agent) building against it.

### 1.2 Scope
V1 delivers the core loop: **Record spending → Organize spending → Understand spending → Control spending.** It is single-user, single-currency (INR), and has no authentication layer. Income tracking, multi-user support, bank sync, and recurring transactions are explicitly out of scope for V1 (see PRD Section 5.2 and Section 16 for the phased roadmap).

### 1.3 Definitions
| Term | Meaning |
|---|---|
| FR | Functional Requirement |
| NFR | Non-Functional Requirement |
| ORM | Object-Relational Mapper |
| SSR/CSR | Server/Client-Side Rendering |

### 1.4 References
- Spendora PRD (Merged), v1
- FastAPI, Next.js, PostgreSQL, SQLAlchemy, Alembic official documentation

---

## 2. Overall Description

### 2.1 Product Perspective
Spendora V1 is a new, standalone web application replacing an earlier Spring Boot–based build. It is a greenfield implementation on the stack defined in Section 3.

### 2.2 User Classes
Single user class: an individual tracking personal expenses. No roles, no permissions tiers (no auth in V1).

### 2.3 Constraints
- No authentication/authorization layer in V1 — deployment is not intended for public multi-tenant use.
- Single fixed currency: INR (₹), 2 decimal places.
- No native mobile app — responsive web only.

### 2.4 Assumptions
- Local/private or single-tenant hosted deployment.
- Budget periods default to monthly.
- Expense dates are always "today or earlier."

---

## 3. Technology Stack

| Layer | Choice | Notes |
|---|---|---|
| Backend framework | **FastAPI** (Python) | Async-first, auto-generates OpenAPI docs |
| Frontend framework | **Next.js** | React-based, file-system routing |
| Database | **PostgreSQL** | Hosted on Supabase in deployment |
| ORM | **SQLAlchemy 2.0 (async)** | Declarative models, async sessions |
| DB driver | **asyncpg** | Async PostgreSQL driver |
| Migrations | **Alembic** | Autogenerate from models, manually reviewed before apply |
| Frontend styling | **Tailwind CSS + shadcn/ui** | Utility-first CSS + accessible component primitives |
| Frontend validation | **Zod + React Hook Form** | Schema-based, type-safe form validation |
| Charts | Recharts or Chart.js (implementer's choice) | Pie/donut for category breakdown, bar/line for trend |
| Motion | **Framer Motion** | Subtle scope only (see Section 11.3) |
| 3D | **React Three Fiber** | Scoped, not applied broadly (see Section 11.4) |
| API style | REST, no generated client | Versioned under `/api/v1/...` |
| Primary keys | **UUID** (all tables) | Avoids sequential ID leakage, safe for future multi-tenant use |

---

## 4. System Architecture

```text
Next.js Frontend (Vercel)
        │  REST calls (/api/v1/...)
        ▼
FastAPI Backend (Render, containerized)
        │  SQLAlchemy async session
        ▼
PostgreSQL Database (Supabase)
```

- **Frontend** — pages/routes for Dashboard and Expenses, forms, charts, client-side validation.
- **Backend** — FastAPI routers per resource, Pydantic schemas for request/response validation, service layer for business logic, SQLAlchemy models for persistence.
- **Database** — PostgreSQL, three core tables (Expense, Category, Budget) plus Alembic version tracking.

### 4.1 Backend Layering (recommended default — confirm before implementation)
```text
routers/      → FastAPI route handlers (thin, delegate to services)
schemas/      → Pydantic request/response models
services/     → Business logic (budget calc, validation rules, category-deletion rules)
repositories/ → SQLAlchemy query layer (data access only)
models/       → SQLAlchemy ORM models
```
This separation keeps CRUD, budget calculations, and filtering independently testable per NFR (PRD Section 13). **Open item:** confirm this repository/service split is the desired pattern before scaffolding — an alternative is routers calling SQLAlchemy directly for a smaller V1.

---

## 5. Data Model

### 5.1 Category
| Column | Type | Constraints |
|---|---|---|
| id | UUID | PK, default `gen_random_uuid()` |
| name | VARCHAR(50) | NOT NULL, UNIQUE |
| created_at | TIMESTAMPTZ | NOT NULL, default now() |
| updated_at | TIMESTAMPTZ | NOT NULL, default now(), on update now() |

**Starter categories (auto-seeded on startup if table is empty):** Food, Transport, Rent, Shopping, Education, Entertainment, Bills, Healthcare, Other.

### 5.2 Expense
| Column | Type | Constraints |
|---|---|---|
| id | UUID | PK |
| title | VARCHAR(50) | NOT NULL |
| category_id | UUID | FK → category.id, NOT NULL |
| amount | NUMERIC(12,2) | NOT NULL, CHECK (amount > 0) |
| expense_date | DATE | NOT NULL, CHECK (expense_date <= CURRENT_DATE) |
| notes | TEXT | NULL |
| payment_mode | VARCHAR(20) | NULL — see 5.2.1 |
| created_at | TIMESTAMPTZ | NOT NULL, default now() |
| updated_at | TIMESTAMPTZ | NOT NULL, default now(), on update now() |

Indexes: `expense_date`, `category_id`, and a composite/text index to support search on `title`/`notes`.

**5.2.1 Payment Mode — open item.** PRD lists "Cash, Card, UPI, etc." without a closed set. Recommended default enum for V1: `Cash, Card, UPI, Net Banking, Other`. **Confirm this list before implementation** — it drives both the DB constraint and the filter dropdown (FR-15).

### 5.3 Budget
| Column | Type | Constraints |
|---|---|---|
| id | UUID | PK |
| scope | VARCHAR(10) | NOT NULL, CHECK (scope IN ('overall','category')) |
| category_id | UUID | FK → category.id, NULL (required if scope='category', NULL if scope='overall') |
| amount | NUMERIC(12,2) | NOT NULL, CHECK (amount > 0) |
| period_month | DATE | NOT NULL (stored as first-of-month, e.g. 2026-08-01) |
| created_at | TIMESTAMPTZ | NOT NULL, default now() |
| updated_at | TIMESTAMPTZ | NOT NULL, default now(), on update now() |

Unique constraint: one overall budget per `period_month`; one per-category budget per (`category_id`, `period_month`).

### 5.4 Category Deletion Rule (enforced in service layer)
A category with existing expenses cannot be deleted outright. The API must either reject deletion with a `409 Conflict` and a list of affected expense counts, or accept a `reassign_to_category_id` parameter that moves expenses before deleting the category. No expense may be orphaned.

---

## 6. Database, Migrations & Seeding

- **ORM:** SQLAlchemy 2.0, fully async (`AsyncSession`, `asyncpg` driver).
- **Session management:** per-request session via FastAPI `Depends`, ensuring clean lifecycle per HTTP request.
- **Migrations:** Alembic. Workflow: define/change SQLAlchemy models → `alembic revision --autogenerate` → **manually review the generated migration** → `alembic upgrade head`. Autogenerated migrations are never applied without review.
- **Seeding:** On FastAPI startup (lifespan hook), check if the `category` table is empty; if so, insert the starter categories listed in 5.1. This is idempotent and requires no manual seed command.

---

## 7. API Design

Base path: **`/api/v1`** (versioned from day one).

### 7.1 Categories
| Method | Path | Description |
|---|---|---|
| GET | `/api/v1/categories` | List categories with expense counts (FR-9) |
| POST | `/api/v1/categories` | Create category (FR-6) |
| PATCH | `/api/v1/categories/{id}` | Rename category (FR-7) |
| DELETE | `/api/v1/categories/{id}?reassign_to={id}` | Delete/reassign (FR-8) |

### 7.2 Expenses
| Method | Path | Description |
|---|---|---|
| GET | `/api/v1/expenses` | Paginated list; query params: `search`, `date_from`, `date_to`, `category_id`, `min_amount`, `max_amount`, `payment_mode`, `sort_by`, `sort_order`, `page`, `page_size` (FR-3, FR-11–16) |
| POST | `/api/v1/expenses` | Create expense (FR-2) |
| GET | `/api/v1/expenses/{id}` | Retrieve single expense |
| PUT | `/api/v1/expenses/{id}` | Update expense (FR-4) |
| DELETE | `/api/v1/expenses/{id}` | Delete expense, confirmation handled client-side (FR-5) |

### 7.3 Budgets
| Method | Path | Description |
|---|---|---|
| GET | `/api/v1/budgets?period_month=YYYY-MM-01` | Get overall + per-category budgets for a period |
| POST | `/api/v1/budgets` | Create/update a budget goal (FR-26) |

### 7.4 Dashboard
| Method | Path | Description |
|---|---|---|
| GET | `/api/v1/dashboard/summary` | Total spend, remaining budget, status (FR-17, FR-21, FR-27, FR-28) |
| GET | `/api/v1/dashboard/recent-expenses` | Recent N expenses (FR-18) |
| GET | `/api/v1/dashboard/category-breakdown` | Pie/donut chart data (FR-19) |
| GET | `/api/v1/dashboard/trend` | Bar/line chart data over time (FR-20) |
| GET | `/api/v1/dashboard/comparison` | Month-over-month % change (FR-23) |
| GET | `/api/v1/dashboard/top-categories` | Ranked top spending categories (FR-24) |
| GET | `/api/v1/dashboard/stats` | Average daily/weekly spend, highest expense, count (FR-25) |

### 7.5 Health
| Method | Path | Description |
|---|---|---|
| GET | `/health` | Liveness only — returns `200 OK`, no DB dependency |

### 7.6 Response Conventions
- All monetary values serialized as strings or fixed-precision numbers to avoid floating-point drift (e.g. `"850.00"`, not `850.0`).
- Errors follow a consistent shape: `{"detail": "message", "code": "error_code"}`.
- Pydantic schemas validate all request bodies; validation errors return `422` with field-level detail.

---

## 8. Functional Requirements

*(Mirrors PRD Section 6 — see PRD for full user-story rationale. IDs preserved for traceability.)*

### 8.1 Navigation
- **FR-1 (P0):** Hamburger menu — Dashboard (view-only) and Expenses (full CRUD + search/filter/sort).

### 8.2 Expense CRUD
- **FR-2–FR-5 (P0):** Add, View (paginated), Edit, Delete (with confirmation).

### 8.3 Category Management
- **FR-6–FR-8 (P0):** Create, Rename, Delete (safe — see Section 5.4).
- **FR-9 (P1):** View categories with usage counts.
- **FR-10 (P2):** Starter categories auto-seeded (see Section 6).

### 8.4 Search, Filter & Sort
- **FR-11 (P1):** Search by title/notes text.
- **FR-12–FR-15 (P0/P1):** Filter by date range, category, amount range, payment mode — all combinable.
- **FR-16 (P1):** Sort by date, amount, category, title — ascending/descending.

### 8.5 Dashboard & Analytics
- **FR-17–FR-21 (P0):** Total spend, recent expenses, category pie/donut, trend chart, budget status.
- **FR-22 (P0):** Daily/weekly/monthly report views — implemented as the dashboard filtered by period (confirm this interpretation, or scope as a separate screen if not).
- **FR-23–FR-25 (P1/P2):** Month-over-month comparison, top categories, averages.

### 8.6 Budget Management
- **FR-26–FR-28 (P0/P1):** Set overall + per-category budgets; live remaining balance; status indicator.
- **Formula:** `Remaining = Monthly Budget − Total Monthly Expenses`.
- **Near-Limit threshold:** default **80%** of budget consumed, configurable via a settings constant (confirm default before implementation).

### 8.7 Data Export
- **FR-29 (P2, nice-to-have):** CSV/PDF/Excel export. Deferred to Phase 2 if V1 timeline is tight.

### 8.8 Data Integrity
- **FR-30 (P0):** No hardcoded/demo data in the finished application — all dashboard values come from real stored data.

---

## 9. Validation Rules

| Field | Rule | Enforced |
|---|---|---|
| Expense title | Required, max 50 chars | Zod (client) + Pydantic (server) |
| Expense amount | Required, numeric, > 0 | Zod + Pydantic + DB CHECK constraint |
| Expense date | Required, ≤ today | Zod + Pydantic + DB CHECK constraint |
| Category | Required (must exist) | Pydantic FK validation |
| Category name | Required, unique | Pydantic + DB UNIQUE constraint |
| Budget amount | Required, numeric, > 0 | Zod + Pydantic + DB CHECK constraint |

All validation errors surface as clear inline messages in the form (PRD Section 9.2).

---

## 10. UI/UX & Frontend Architecture

### 10.1 General
Clean, simple, consistent across breakpoints. Empty, loading, success, and error states implemented for every screen (PRD Section 3.2, 9.4).

### 10.2 Responsive Strategy
Tailwind's default breakpoint scale (`sm`/`md`/`lg`/`xl`) governs mobile/tablet/desktop layouts. No custom breakpoint set for V1.

### 10.3 Framer Motion Scope (V1 — subtle only)
- Page/route transitions
- Hover/tap feedback on buttons and interactive elements
- List item enter/exit animations (expense list, category list)
- **Explicitly excluded from V1:** animated chart transitions, number count-up animations, animated budget-status changes (deferred — these add complexity without serving the "under 30 seconds" usability goal).

### 10.4 React Three Fiber Scope
Real WebGL 3D via React Three Fiber. **Recommended scope:** a single, contained visual moment (e.g., an empty-state illustration or a budget-status hero visual) rather than distributed across the UI, to control bundle size and complexity. **Confirm exact placement before implementation.**

### 10.5 Feedback Messages
Toast/inline confirmations: "Expense added successfully," "Expense updated successfully," "Expense deleted successfully," "Category created successfully," "Budget updated successfully" (PRD Section 9.3).

---

## 11. Non-Functional Requirements

| Category | Requirement |
|---|---|
| Performance | Dashboard/report queries backed by indexed columns (`expense_date`, `category_id`); expense list paginated server-side. |
| Scalability | Layered architecture (Section 4) allows Phase 2+ features without core rewrites. |
| Reliability | All CRUD operations wrapped in try/except with meaningful HTTP error codes; no silent failures. |
| Maintainability | Repository/service separation (Section 4.1, pending confirmation) keeps business logic testable independent of routes. |
| Testability | Unit tests for budget calculations, category-deletion logic, filter/sort combinations; integration tests for API endpoints. |
| Responsiveness | Verified on mobile, tablet, and desktop breakpoints per Section 10.2. |

---

## 12. Business Rules

1. Expense amounts must always be greater than zero (DB CHECK constraint).
2. Future expense dates are rejected (DB CHECK constraint + API validation).
3. Every expense must belong to a category (FK NOT NULL).
4. Deleting an expense requires client-side confirmation before the DELETE call fires.
5. Deleting a category must never orphan expenses (Section 5.4).
6. Dashboard totals are always computed from live queries — never cached/hardcoded values.
7. Budget remaining recalculates on every expense create/update/delete.
8. Negative remaining budget = over-budget status.
9. All monetary math uses `NUMERIC`/`Decimal` types — never floats — to avoid rounding errors.
10. No hardcoded financial values anywhere in production code.

---

## 13. Security Scope (V1)

No authentication, registration, password management, or user roles. Deployment (Section 15) should not be treated as public-multi-tenant; Render/Vercel deployment is single-instance, single-user. Authentication becomes a dedicated Phase 2+ effort if the app is exposed more broadly.

---

## 14. Repository Structure

```text
spendora/
├── .gitignore                  # root-level (IDE, OS files, env files)
├── backend/
│   ├── .gitignore              # Python-specific (venv, __pycache__, .env)
│   ├── Dockerfile              # backend container image
│   ├── app/
│   │   ├── main.py             # FastAPI app entrypoint, lifespan/seed hook
│   │   ├── routers/
│   │   ├── schemas/
│   │   ├── services/
│   │   ├── repositories/
│   │   ├── models/
│   │   └── core/                # settings, DB session factory
│   ├── alembic/
│   │   ├── versions/
│   │   └── env.py
│   └── tests/
└── frontend/
    ├── .gitignore               # Next.js-specific (node_modules, .next, .env.local)
    ├── Dockerfile               # frontend container image
    ├── app/                     # Next.js app router: dashboard/, expenses/
    ├── components/              # shadcn/ui-based components
    ├── lib/                     # Zod schemas, API client helpers
    └── public/
```

- **Three `.gitignore` files** (root, backend, frontend) — each scoped to its own tooling's ignore patterns.
- **Separate Dockerfiles** for frontend and backend — independent build/deploy lifecycles.

---

## 15. Local Development & Deployment

### 15.1 Local Development (no Docker)
1. Backend: Python virtual environment, `pip install -r requirements.txt`, run against a local or Supabase dev-branch PostgreSQL instance, `uvicorn app.main:app --reload`.
2. Frontend: `npm install`, `npm run dev` against `NEXT_PUBLIC_API_URL` pointing at the local backend.
3. No containers involved at this stage — fastest iteration loop for early development.

### 15.2 Deployment (containerized)
| Component | Platform | Notes |
|---|---|---|
| Database | **Supabase** | Managed PostgreSQL |
| Backend | **Render** | Deployed from `backend/Dockerfile` |
| Frontend | **Vercel** | Next.js-native deployment (Vercel builds directly; Dockerfile primarily for local parity/CI) |

This Supabase + Vercel + Render combination is the deployment target from the start of the project, not a later migration.

### 15.3 Run → Test → Deploy Discipline
Each phase (starting with V1) is built, tested, and deployed before the next phase begins — no parallel scope expansion (PRD Section 16).

---

## 16. CI/CD

- GitHub Actions pipeline:
  1. Lint + type-check (backend: ruff/mypy; frontend: eslint/tsc)
  2. Run backend test suite (pytest) against a test database
  3. Build both Docker images (backend) and Next.js build (frontend)
  4. On merge to main: deploy backend to Render, frontend to Vercel

---

## 17. Testing Strategy

| Layer | Approach |
|---|---|
| Backend unit tests | Budget calculation logic, category-deletion rules, validation functions |
| Backend integration tests | API endpoints against a test PostgreSQL instance (pytest + httpx AsyncClient) |
| Frontend | Component tests for forms (React Hook Form + Zod validation paths) |
| Manual/E2E | Full user flows from PRD Section 7 before each deployment |

---

## 18. Definition of Done — V1

Mirrors PRD Section 15 in full — all items must be verified before V1 is considered shippable, including: full expense CRUD, dynamic categories, combined search/filter/sort, dashboard with ≥2 charts, budget goals with live remaining balance and status, hamburger navigation, zero hardcoded data, full empty/loading/success/error state coverage, and cross-device responsiveness — tested end-to-end and deployed before Phase 2 begins.

---

## 19. Open Items (confirm before/during implementation)

1. **Repository/service layering** (Section 4.1) — confirmed pattern, or simplify for V1?
2. **Payment Mode enum values** (Section 5.2.1) — finalize the fixed list.
3. **Near-Limit budget threshold** (Section 8.6) — confirm 80% default or choose another.
4. **Daily/Weekly/Monthly report views** (FR-22) — same dashboard filtered by period, or a separate screen?
5. **React Three Fiber placement** (Section 10.4) — confirm which specific screen/moment gets the 3D treatment.

---

## 20. Future Roadmap Reference

See PRD Section 16 for the full Phase 2–6 roadmap (login/sync, social/sharing, smart/AI features, security/personalization, monetization). V1's architecture (Section 4) is designed to absorb these without a core rewrite.
