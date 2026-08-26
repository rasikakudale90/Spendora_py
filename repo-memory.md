# Spendora — Repo Memory (Session Context)

> **READ THIS FIRST every session before doing anything.**
> This file is the condensed truth of the entire project — tech stack, data model, APIs, rules, and current state.
> Saves you from re-reading the full SRS (408 lines) and AGENTS.md every time.

---

## 1. What is Spendora?
Single-user personal **expense & budget tracking** web app. Single currency: **INR (₹)**. No auth in V1. No multi-user.

---

## 2. Tech Stack (Non-Negotiable)

| Layer | Choice |
|---|---|
| Backend | **FastAPI** (Python, async-first) |
| Frontend | **Next.js** (App Router, TypeScript) |
| Database | **PostgreSQL** via Supabase |
| ORM | **SQLAlchemy 2.0 async** + asyncpg driver |
| Migrations | **Alembic** (autogenerate → manual review → apply) |
| Styling | **Tailwind CSS + shadcn/ui** |
| Validation (FE) | **Zod + React Hook Form** |
| Validation (BE) | **Pydantic** |
| Charts | Recharts or Chart.js (implementer's choice) |
| Animations | **Framer Motion** (subtle scope only) |
| 3D | **React Three Fiber** (one contained moment, TBD) |
| PKs | **UUID** on all tables |
| Currency math | **NUMERIC/Decimal only** — never floats |

---

## 3. Deployment Targets

| Component | Platform |
|---|---|
| Database | Supabase (managed PostgreSQL) |
| Backend | Render (containerized via `backend/Dockerfile`) |
| Frontend | Vercel (Next.js native, `frontend/Dockerfile` for CI parity) |

**Local dev:** Python venv + uvicorn (backend), `npm run dev` (frontend). No Docker for local dev.

---

## 4. Folder Structure (SRS Section 14)

```
Spendora_py/
├── .gitignore                    # root
├── AGENTS(1).md                  # project rules for agents
├── PROGRESS.md                   # phase tracker (update after every phase)
├── repo-memory.md                # THIS FILE — read at session start
├── docs/
│   └── Spendora_SRS_V1.md        # full SRS (only read if details needed)
├── backend/
│   ├── .gitignore                # Python-specific
│   ├── Dockerfile                # backend container
│   ├── requirements.txt          # [Phase 2]
│   ├── alembic.ini               # [Phase 2]
│   ├── app/
│   │   ├── main.py               # FastAPI entrypoint + lifespan/seed
│   │   ├── routers/              # thin route handlers
│   │   ├── schemas/              # Pydantic request/response models
│   │   ├── services/             # business logic
│   │   ├── repositories/         # SQLAlchemy query layer
│   │   ├── models/               # ORM models
│   │   └── core/                 # config.py, database.py
│   ├── alembic/
│   │   ├── versions/
│   │   └── env.py
│   └── tests/
└── frontend/
    ├── .gitignore                # Next.js-specific
    ├── Dockerfile
    ├── app/
    │   ├── dashboard/
    │   └── expenses/
    ├── components/               # shadcn/ui-based
    ├── lib/                      # Zod schemas, API client helpers
    └── public/
```

---

## 5. Data Model (3 Tables — all UUID PKs)

### Category
```
id           UUID   PK
name         VARCHAR(50)  NOT NULL, UNIQUE
created_at   TIMESTAMPTZ  NOT NULL
updated_at   TIMESTAMPTZ  NOT NULL
```
**Auto-seeded on startup (if empty):** Food, Transport, Rent, Shopping, Education, Entertainment, Bills, Healthcare, Other

### Expense
```
id             UUID   PK
title          VARCHAR(50)   NOT NULL
category_id    UUID          FK → category.id, NOT NULL
amount         NUMERIC(12,2) NOT NULL, CHECK > 0
expense_date   DATE          NOT NULL, CHECK <= CURRENT_DATE
notes          TEXT          NULL
payment_mode   VARCHAR(20)   NULL  (enum TBD — see Open Items)
created_at     TIMESTAMPTZ   NOT NULL
updated_at     TIMESTAMPTZ   NOT NULL
```
Indexes: `expense_date`, `category_id`, text index on `title`/`notes`

### Budget
```
id             UUID   PK
scope          VARCHAR(10)   NOT NULL, CHECK IN ('overall','category')
category_id    UUID          FK → category.id, NULL (required if scope='category')
amount         NUMERIC(12,2) NOT NULL, CHECK > 0
period_month   DATE          NOT NULL  (stored as first-of-month: 2026-08-01)
created_at     TIMESTAMPTZ   NOT NULL
updated_at     TIMESTAMPTZ   NOT NULL
```
Unique: one overall budget per `period_month`; one per-category budget per (`category_id`, `period_month`)

---

## 6. API Endpoints (Base: `/api/v1`)

### Categories
| Method | Path | Description |
|---|---|---|
| GET | `/categories` | List with expense counts |
| POST | `/categories` | Create |
| PATCH | `/categories/{id}` | Rename |
| DELETE | `/categories/{id}?reassign_to={id}` | Delete / reassign |

### Expenses
| Method | Path | Description |
|---|---|---|
| GET | `/expenses` | Paginated list (params: search, date_from, date_to, category_id, min_amount, max_amount, payment_mode, sort_by, sort_order, page, page_size) |
| POST | `/expenses` | Create |
| GET | `/expenses/{id}` | Single expense |
| PUT | `/expenses/{id}` | Update |
| DELETE | `/expenses/{id}` | Delete |

### Budgets
| Method | Path |
|---|---|
| GET | `/budgets?period_month=YYYY-MM-01` |
| POST | `/budgets` |

### Dashboard
| Method | Path |
|---|---|
| GET | `/dashboard/summary` |
| GET | `/dashboard/recent-expenses` |
| GET | `/dashboard/category-breakdown` |
| GET | `/dashboard/trend` |
| GET | `/dashboard/comparison` |
| GET | `/dashboard/top-categories` |
| GET | `/dashboard/stats` |

### Health
| Method | Path |
|---|---|
| GET | `/health` | (no DB dependency) |

**Response rules:**
- Monetary values: string or fixed-precision (e.g. `"850.00"`)
- Errors: `{"detail": "message", "code": "error_code"}`
- Validation errors: `422` with field-level detail

---

## 7. Key Business Rules (Never Violate)

1. Expense amount > 0 always (DB CHECK + API)
2. Future expense dates rejected (DB CHECK + API)
3. Every expense must have a category (FK NOT NULL)
4. Delete expense → client-side confirmation first
5. Delete category → never orphan expenses (409 Conflict OR reassign)
6. Dashboard totals always from live DB queries — never cached/hardcoded
7. Budget remaining = `Monthly Budget − Total Monthly Expenses` (recalculates on every expense change)
8. Negative remaining = over-budget
9. All monetary math uses NUMERIC/Decimal — never floats
10. No hardcoded financial values in production code

---

## 8. Validation Rules

| Field | Rule | Where |
|---|---|---|
| Expense title | Required, max 50 chars | Zod + Pydantic |
| Expense amount | Required, > 0 | Zod + Pydantic + DB CHECK |
| Expense date | Required, <= today | Zod + Pydantic + DB CHECK |
| Category | Required, must exist | Pydantic FK |
| Category name | Required, unique | Pydantic + DB UNIQUE |
| Budget amount | Required, > 0 | Zod + Pydantic + DB CHECK |

---

## 9. Frontend Scope Rules

- **Framer Motion:** page transitions, hover/tap feedback, list enter/exit ONLY. No animated charts, no count-up numbers, no animated budget changes.
- **React Three Fiber:** one contained visual moment (exact placement TBD — see Open Items)
- **Responsive:** Tailwind default breakpoints (`sm/md/lg/xl`). No custom breakpoints.
- **Toasts:** "Expense added/updated/deleted successfully", "Category created successfully", "Budget updated successfully"
- **Every screen needs:** empty state, loading state, success state, error state

---

## 10. AGENTS.md Rules (Always Follow)

1. All config/secrets via environment variables — never hardcode
2. Never read/modify/expose `.env` files — use `.env.example` only
3. Follow SRS folder structure, naming, architecture strictly
4. No inline CSS — use designated style files only
5. Every UI change must be responsive (mobile + tablet + desktop)
6. No hardcoded constants — use env vars / config / constants files
7. Only implement what's in scope for the current phase
8. Reuse existing components/utilities before creating new ones
9. After changes: verify functionality + fix any regressions before marking done
10. After every completed task: `git commit` + `git push` with clear message

---

## 11. Open Items (Must Confirm Before Phase 2 Implementation)

| # | Decision | Default Proposed |
|---|---|---|
| 1 | Backend layering: full 5-layer (routers/schemas/services/repos/models) OR simplified? | Full 5-layer |
| 2 | Payment Mode enum values | Cash, Card, UPI, Net Banking, Other |
| 3 | Near-limit budget threshold | 80% |
| 4 | FR-22 report views: filtered dashboard OR separate screen? | Filtered dashboard |
| 5 | React Three Fiber placement: which screen/moment? | TBD |

---

## 12. Current Phase Status

**Last completed:** Phase 1 — Project Scaffolding (2026-08-26)  
**Next up:** Phase 2 — Backend Foundation

**See [PROGRESS.md](./PROGRESS.md) for full task-level tracking.**

---

## 13. Session Notes (append here as work progresses)

| Date | Note |
|---|---|
| 2026-08-26 | Phase 1 complete — folder structure scaffolded per SRS Section 14 |
| 2026-08-26 | Phase 2 (schema) — models, core config, alembic setup done. Remaining: run first migration + verify server |
