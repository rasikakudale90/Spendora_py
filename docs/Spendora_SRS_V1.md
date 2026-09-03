# Spendora — Software Requirements Specification (SRS)
## Version 1 (V1) — Expense & Budget Tracking Web Application

*Derived from the Spendora PRD (Merged). This SRS translates that PRD into concrete technical requirements for implementation.*

---

## 1. Introduction

### 1.1 Purpose
This document specifies the technical requirements, architecture, data model, security specifications, and API contract for Spendora — a high-performance personal expense, income, and budget tracking web application with secure, production-ready multi-user authentication and strict data isolation. It is written to be directly actionable by developers and AI agents building and maintaining the system.

### 1.2 Scope
Spendora delivers the complete personal finance loop: **Record → Organize → Understand → Control.**
The system features a robust, secure authentication and authorization architecture supporting:
- Multi-user data isolation (each user accesses exclusively their own financial records).
- Dual authentication methods: Email/Password (with BCrypt hashing) and Sign in with Google (OAuth 2.0 / OpenID Connect).
- Dual JWT token lifecycle: short-lived Access Tokens (15 min) in memory and long-lived rotated Refresh Tokens (30 days) stored in HttpOnly, Secure, SameSite cookies with SHA-256 server-side hashing and revocation.
- Comprehensive session control: Secure Logout, Logout from All Devices, and silent automatic token refresh.
- Self-service password recovery (Forgot Password, Reset Password via signed tokens, and Change Password).
- Single fixed currency: INR (₹), 2 decimal places.

### 1.3 Definitions
| Term | Meaning |
|---|---|
| FR | Functional Requirement |
| NFR | Non-Functional Requirement |
| ORM | Object-Relational Mapper |
| JWT | JSON Web Token (RFC 7519) |
| OIDC | OpenID Connect (Google Identity Services) |
| CSRF | Cross-Site Request Forgery |
| SSR/CSR | Server/Client-Side Rendering |

### 1.4 References
- Spendora PRD (Merged), v1
- OAuth 2.0 Authorization Framework (RFC 6749) & OpenID Connect Core 1.0
- OWASP Session Management & Password Storage Cheat Sheets
- FastAPI, Next.js, PostgreSQL, SQLAlchemy, Alembic official documentation

---

## 2. Overall Description

### 2.1 Product Perspective
Spendora is a modern full-stack web application built on a decoupled architecture (FastAPI backend + Next.js App Router frontend + Supabase managed PostgreSQL). It provides enterprise-grade data isolation across all financial operations.

### 2.2 User Classes
- **Authenticated User**: Individual managing personal expenses, incomes, budgets, and categories. Each authenticated user has private data ownership.
- **System / Zero RBAC**: No admin roles or permissions tiers exist. Authorization is strictly derived from the validated JWT context (`current_user.id`). No client-supplied user identifiers are ever trusted for data access.

### 2.3 Constraints
- **Strict Data Isolation**: Zero-trust access model. Every database operation on expenses, incomes, budgets, and custom categories is automatically scoped to `user_id == current_user.id`. Cross-user access returns `404 Not Found`.
- **Token Storage**: Refresh tokens are strictly stored in `HttpOnly; Secure; SameSite=Lax` cookies. Never store refresh tokens or sensitive credentials in client `localStorage`.
- **Single Fixed Currency**: INR (₹), 2 decimal places.
- **Responsive Web & PWA**: Mobile, tablet, and desktop responsive web with Progressive Web App capabilities.

### 2.4 Assumptions
- Managed PostgreSQL deployment on Supabase.
- Google OAuth credentials configured with client ID and secret.
- Expense dates are restricted to today or past dates (no future dates).

---

## 3. Technology Stack

| Layer | Choice | Notes |
|---|---|---|
| Backend framework | **FastAPI** (Python) | Async-first, auto-generates OpenAPI docs |
| Frontend framework | **Next.js 14** | App Router, React 18, TypeScript |
| Database | **PostgreSQL** | Hosted on Supabase (Session Pooler mode) |
| ORM | **SQLAlchemy 2.0 (async)** | Declarative models, async sessions |
| DB driver | **asyncpg** | High-performance async PostgreSQL driver |
| Migrations | **Alembic** | Version-controlled database migrations |
| **Authentication & Tokens** | **PyJWT + Passlib (BCrypt)** | HS256 JWT, short-lived access tokens, salt rounds |
| **Google Sign-In** | **Google Identity Services + google-auth** | OAuth 2.0 / OpenID Connect ID token verification |
| **Rate Limiting** | **SlowAPI** | In-memory / Redis-ready rate limits on login/auth |
| Frontend styling | **Tailwind CSS + Radix UI** | Utility-first CSS + accessible component primitives |
| Frontend validation | **Zod + React Hook Form** | Schema-based, type-safe client form validation |
| Charts | **Recharts** | Donut charts, bar/line monthly cash flow & trends |
| Motion | **Framer Motion** | Subtle page transitions, modal feedback |
| 3D | **React Three Fiber** | Contained hero visual |
| API style | REST | Versioned under `/api/v1/...` |
| Primary keys | **UUID** (all tables) | Multi-tenant safe, non-enumerable |

---

## 4. System Architecture

```text
               ┌────────────────────────────────────────────────────────┐
               │              Next.js 14 Frontend (Vercel)              │
               │  - AuthContext (Access Token in Memory)                │
               │  - Auto-Refresh Interceptor                            │
               │  - Google Identity Services (GIS) Sign-In Button       │
               └───────────┬────────────────────────────────┬───────────┘
                           │                                │
                 Bearer Access Token (Headers)     HttpOnly Refresh Cookie
                           │                                │
                           ▼                                ▼
               ┌────────────────────────────────────────────────────────┐
               │              FastAPI Backend (Render)                  │
               │  - Depends(get_current_user) SecurityContext           │
               │  - Google Token Verification (google-auth)             │
               │  - Rate Limiting (SlowAPI)                             │
               │  - Routers → Services → Repositories                   │
               └───────────────────────────┬────────────────────────────┘
                                           │
                              SQLAlchemy Async (Scoped to user_id)
                                           │
                                           ▼
               ┌────────────────────────────────────────────────────────┐
               │             PostgreSQL Database (Supabase)             │
               │  - users (credentials, Google IDs)                     │
               │  - refresh_tokens (hashed SHA-256 tokens & revocation) │
               │  - expenses, incomes, budgets (scoped by user_id FK)   │
               └────────────────────────────────────────────────────────┘
```

### 4.1 Backend Layering (5-Layer Pattern)
```text
routers/      → FastAPI route handlers with Depends(get_current_user)
schemas/      → Pydantic v2 request/response models & password rules
services/     → Business logic, token generation, Google verification, data isolation
repositories/ → Raw async SQLAlchemy queries with mandatory .where(user_id == current_user.id)
models/       → SQLAlchemy 2.0 ORM models with User relationships & FK constraints
core/         → JWT config, security helpers (BCrypt, token hashing), database engine
```

---

## 5. Data Model

### 5.1 User
| Column | Type | Constraints |
|---|---|---|
| id | UUID | PK, default `gen_random_uuid()` |
| email | VARCHAR(255) | NOT NULL, UNIQUE, indexed, lowercase |
| hashed_password | VARCHAR(255) | NULL (NULL for OAuth-only accounts) |
| full_name | VARCHAR(100) | NULL |
| avatar_url | VARCHAR(500) | NULL |
| is_active | BOOLEAN | NOT NULL, default `true` |
| is_verified | BOOLEAN | NOT NULL, default `false` |
| auth_provider | VARCHAR(20) | NOT NULL, default `'email'` (`'email'`, `'google'`) |
| google_id | VARCHAR(255) | NULL, UNIQUE, indexed |
| created_at | TIMESTAMPTZ | NOT NULL, default `now()` |
| updated_at | TIMESTAMPTZ | NOT NULL, default `now()`, on update `now()` |

### 5.2 Refresh Token
| Column | Type | Constraints |
|---|---|---|
| id | UUID | PK, default `gen_random_uuid()` |
| user_id | UUID | FK → `users.id` ON DELETE CASCADE, NOT NULL, indexed |
| token_hash | VARCHAR(64) | NOT NULL, UNIQUE, indexed (SHA-256 hash of raw token) |
| expires_at | TIMESTAMPTZ | NOT NULL |
| revoked | BOOLEAN | NOT NULL, default `false` |
| user_agent | VARCHAR(255) | NULL |
| ip_address | VARCHAR(45) | NULL |
| created_at | TIMESTAMPTZ | NOT NULL, default `now()` |

### 5.3 Password Reset Token
| Column | Type | Constraints |
|---|---|---|
| id | UUID | PK, default `gen_random_uuid()` |
| user_id | UUID | FK → `users.id` ON DELETE CASCADE, NOT NULL, indexed |
| token_hash | VARCHAR(64) | NOT NULL, UNIQUE, indexed |
| expires_at | TIMESTAMPTZ | NOT NULL (15 min validity) |
| used | BOOLEAN | NOT NULL, default `false` |
| created_at | TIMESTAMPTZ | NOT NULL, default `now()` |

### 5.4 Category
| Column | Type | Constraints |
|---|---|---|
| id | UUID | PK, default `gen_random_uuid()` |
| user_id | UUID | FK → `users.id` ON DELETE CASCADE, NULL (NULL for global starter categories) |
| name | VARCHAR(50) | NOT NULL |
| created_at | TIMESTAMPTZ | NOT NULL, default `now()` |
| updated_at | TIMESTAMPTZ | NOT NULL, default `now()`, on update `now()` |

**Unique constraint:** Composite unique on `(name, coalesce(user_id, '00000000-0000-0000-0000-000000000000'))` so category names are unique per user.
**Starter categories:** Food, Transport, Rent, Shopping, Education, Entertainment, Bills, Healthcare, Other (seeded globally with `user_id IS NULL`).

### 5.5 Expense
| Column | Type | Constraints |
|---|---|---|
| id | UUID | PK, default `gen_random_uuid()` |
| user_id | UUID | FK → `users.id` ON DELETE CASCADE, NOT NULL, indexed |
| title | VARCHAR(50) | NOT NULL |
| category_id | UUID | FK → `categories.id`, NOT NULL |
| amount | NUMERIC(12,2) | NOT NULL, CHECK (amount > 0) |
| expense_date | DATE | NOT NULL, CHECK (expense_date <= CURRENT_DATE) |
| notes | TEXT | NULL |
| payment_mode | VARCHAR(20) | NULL (Allowed: `Cash`, `Card`, `UPI`, `Net Banking`, `Other`) |
| created_at | TIMESTAMPTZ | NOT NULL, default `now()` |
| updated_at | TIMESTAMPTZ | NOT NULL, default `now()`, on update `now()` |

Indexes: `(user_id, expense_date)`, `(user_id, category_id)`.

### 5.6 Budget
| Column | Type | Constraints |
|---|---|---|
| id | UUID | PK, default `gen_random_uuid()` |
| user_id | UUID | FK → `users.id` ON DELETE CASCADE, NOT NULL, indexed |
| scope | VARCHAR(10) | NOT NULL, CHECK (scope IN ('overall','category')) |
| category_id | UUID | FK → `categories.id`, NULL (required if scope='category', NULL if scope='overall') |
| amount | NUMERIC(12,2) | NOT NULL, CHECK (amount > 0) |
| period_type | VARCHAR(20) | NOT NULL, default `'monthly'` (`daily`, `weekly`, `monthly`, `yearly`) |
| period_start | DATE | NOT NULL |
| period_end | DATE | NOT NULL |
| period_month | DATE | NOT NULL |
| created_at | TIMESTAMPTZ | NOT NULL, default `now()` |
| updated_at | TIMESTAMPTZ | NOT NULL, default `now()`, on update `now()` |

Unique constraints (scoped per user):
- One overall budget per `(user_id, period_type, period_start)`.
- One category budget per `(user_id, category_id, period_type, period_start)`.

### 5.7 Income
| Column | Type | Constraints |
|---|---|---|
| id | UUID | PK, default `gen_random_uuid()` |
| user_id | UUID | FK → `users.id` ON DELETE CASCADE, NOT NULL, indexed |
| title | VARCHAR(100) | NOT NULL |
| amount | NUMERIC(12,2) | NOT NULL, CHECK (amount > 0) |
| income_date | DATE | NOT NULL |
| source | VARCHAR(50) | NOT NULL, default `'Salary'` (`Salary`, `Freelance`, `Investment`, `Business`, `Rental`, `Gift`, `Other`) |
| payment_mode | VARCHAR(30) | NULL (`Bank Transfer`, `Cash`, `UPI`, `Cheque`, `Other`) |
| notes | TEXT | NULL |
| created_at | TIMESTAMPTZ | NOT NULL, default `now()` |
| updated_at | TIMESTAMPTZ | NOT NULL, default `now()`, on update `now()` |

### 5.8 Financial Goals (Reverse Budgeting)
| Column | Type | Constraints |
|---|---|---|
| id | UUID | PK, default `gen_random_uuid()` |
| user_id | UUID | FK → `users.id` ON DELETE CASCADE, NOT NULL, indexed |
| title | VARCHAR(100) | NOT NULL |
| target_amount | NUMERIC(12,2) | NOT NULL, CHECK (target_amount > 0) |
| current_amount | NUMERIC(12,2) | NOT NULL, default `0.00`, CHECK (current_amount >= 0) |
| target_date | DATE | NOT NULL |
| category_id | UUID | FK → `categories.id` NULL (optional linked funding category) |
| notes | TEXT | NULL |
| created_at | TIMESTAMPTZ | NOT NULL, default `now()` |
| updated_at | TIMESTAMPTZ | NOT NULL, default `now()`, on update `now()` |

### 5.9 Category Deletion Rule (enforced in service layer)
A category with existing expenses belonging to the user cannot be deleted outright. The API rejects deletion with `409 Conflict` unless a `reassign_to_category_id` parameter is supplied to move the user's expenses. No expense may be orphaned. Global starter categories cannot be deleted or renamed by standard users.

---

## 6. Database, Migrations & Seeding

- **ORM:** SQLAlchemy 2.0, fully async (`AsyncSession`, `asyncpg` driver).
- **Session management:** per-request session via FastAPI `Depends`, ensuring clean lifecycle per HTTP request.
- **Migrations:** Alembic. Workflow: define/change SQLAlchemy models → `alembic revision --autogenerate` → **manually review the generated migration** → `alembic upgrade head`. Autogenerated migrations are never applied without review.
- **Seeding:** On FastAPI startup (lifespan hook), check if the `category` table is empty; if so, insert the starter categories listed in 5.1. This is idempotent and requires no manual seed command.

---

## 7. API Design

Base path: **`/api/v1`** (versioned from day one).

### 7.1 Authentication & Session Management
All endpoints in this group are rate-limited via SlowAPI. Public endpoints accept credentials; session endpoints require either bearer token or valid refresh cookie.

| Method | Path | Auth Required | Description |
|---|---|---|---|
| POST | `/api/v1/auth/register` | No | Register new user with email, password, full_name. Returns user info + Access Token + sets HttpOnly Refresh Token cookie |
| POST | `/api/v1/auth/login` | No | Login with email and password. Returns user info + Access Token + sets HttpOnly Refresh Token cookie |
| POST | `/api/v1/auth/google` | No | Google Sign-In: verifies Google OpenID Connect `id_token` on backend, finds/creates user, returns Access Token + sets HttpOnly Refresh Token cookie |
| POST | `/api/v1/auth/refresh` | Cookie | Rotates refresh token: validates cookie, revokes old token, issues new Access Token + sets new HttpOnly Refresh Token cookie |
| POST | `/api/v1/auth/logout` | Yes | Revokes the current session's refresh token and clears the HttpOnly cookie |
| POST | `/api/v1/auth/logout-all` | Yes | Revokes all active refresh tokens for the user in the database and clears the cookie |
| GET | `/api/v1/auth/me` | Yes | Returns current authenticated user profile |
| POST | `/api/v1/auth/forgot-password` | No | Generates secure password reset token (15 min validity) |
| POST | `/api/v1/auth/reset-password` | No | Validates reset token and sets new BCrypt-hashed password |
| POST | `/api/v1/auth/change-password` | Yes | Verifies current password and updates to new password |

### 7.2 Categories (Scoped to User + Global Starters)
Requires `Authorization: Bearer <token>`. User sees global starter categories + their own custom categories.

| Method | Path | Description |
|---|---|---|
| GET | `/api/v1/categories` | List categories available to the user with expense counts |
| POST | `/api/v1/categories` | Create custom category for the authenticated user |
| GET | `/api/v1/categories/{id}` | Retrieve single category |
| PATCH | `/api/v1/categories/{id}` | Rename custom category (cannot modify global starter categories) |
| DELETE | `/api/v1/categories/{id}?reassign_to={id}` | Delete custom category with reassign |

### 7.3 Expenses (Strict User Data Isolation)
Requires `Authorization: Bearer <token>`. Backend injects `user_id == current_user.id`.

| Method | Path | Description |
|---|---|---|
| GET | `/api/v1/expenses` | Paginated list scoped to authenticated user; query params: `search`, `date_from`, `date_to`, `category_id`, `min_amount`, `max_amount`, `payment_mode`, `sort_by`, `sort_order`, `page`, `page_size` |
| POST | `/api/v1/expenses` | Create expense for authenticated user (real-time daily budget breach detection) |
| GET | `/api/v1/expenses/{id}` | Retrieve single expense (returns 404 if not owned by authenticated user) |
| PUT / PATCH | `/api/v1/expenses/{id}` | Update expense owned by authenticated user |
| DELETE | `/api/v1/expenses/{id}` | Delete expense owned by authenticated user |

### 7.4 Budgets (Multi-Period, Scoped to User)
Requires `Authorization: Bearer <token>`.

| Method | Path | Description |
|---|---|---|
| GET | `/api/v1/budgets?period_type=monthly&period_start=YYYY-MM-DD` | List overall and category budgets for user and period |
| POST | `/api/v1/budgets` | Create/upsert a budget goal for authenticated user |
| PATCH | `/api/v1/budgets/{id}` | Update budget amount owned by authenticated user |
| DELETE | `/api/v1/budgets/{id}` | Delete budget goal owned by authenticated user |

### 7.5 Incomes (Cash Flow & Income Tracking)
Requires `Authorization: Bearer <token>`.

| Method | Path | Description |
|---|---|---|
| GET | `/api/v1/incomes` | Paginated list of user incomes with filters (`source`, `payment_mode`, `date_from`, `date_to`) |
| POST | `/api/v1/incomes` | Record new income for authenticated user |
| GET | `/api/v1/incomes/{id}` | Retrieve single income owned by user |
| PATCH | `/api/v1/incomes/{id}` | Update income owned by user |
| DELETE | `/api/v1/incomes/{id}` | Delete income owned by user |
| GET | `/api/v1/incomes/summary?period=YYYY-MM` | Total monthly income and source breakdown for user |

### 7.6 Dashboard (User Analytics & Cash Flow)
Requires `Authorization: Bearer <token>`. All analytics aggregate exclusively the authenticated user's records.

| Method | Path | Description |
|---|---|---|
| GET | `/api/v1/dashboard/summary?period=YYYY-MM` | Total spend, total income, net cash flow, savings rate %, budget status |
| GET | `/api/v1/dashboard/recent-expenses` | Recent N expenses for authenticated user |
| GET | `/api/v1/dashboard/category-breakdown` | Category spending pie/donut chart data |
| GET | `/api/v1/dashboard/trend` | Trend bar/line chart over time |
| GET | `/api/v1/dashboard/comparison` | Month-over-month comparison |
| GET | `/api/v1/dashboard/top-categories` | Ranked top spending categories |
### 7.7 AI Financial Intelligence & Smart Recommendations
Requires `Authorization: Bearer <token>`. Scoped strictly to authenticated user's isolated financial ledger.

| Method | Path | Auth | Description |
|---|---|---|---|
| POST | `/api/v1/ai/simulate-purchase` | Yes | Simulates purchase impact on cash flow, remaining budget, and savings rate. Returns verdict (`safe`, `caution`, `over_budget`), impact metrics, and AI advice |
| POST | `/api/v1/ai/scan-receipt` | Yes | Multimodal OCR parsing of receipt image/base64 via Gemini Vision. Returns structured JSON (`title`, `amount`, `date`, `category_id`, `payment_mode`) |
| GET | `/api/v1/ai/leak-analysis` | Yes | Analyzes micro-spending (<₹150), repeat delivery fees, and active subscriptions with annualized financial drains |
| GET | `/api/v1/ai/safe-to-spend` | Yes | Dynamic daily burn rate algorithm: computes today's safe spending allowance and adaptive daily budget schedule |
| GET | `/api/v1/ai/goals` | Yes | List user financial goals with progress % and AI-recommended category budget adjustments |
| POST | `/api/v1/ai/goals` | Yes | Create a new financial savings target goal |
| DELETE | `/api/v1/ai/goals/{id}` | Yes | Delete a financial savings target goal |
| GET | `/api/v1/ai/monthly-wrapped?period=YYYY-MM` | Yes | Generates personalized end-of-month financial narrative, money persona, top milestones, and next month challenges |

### 7.8 Health
| Method | Path | Description |
|---|---|---|
| GET | `/health` | Liveness only — returns `200 OK`, public |

### 7.9 Response Conventions
- All monetary values serialized as strings or fixed-precision numbers to avoid floating-point drift (e.g. `"850.00"`, not `850.0`).
- Errors follow a consistent shape: `{"detail": "message", "code": "error_code"}`.
- Pydantic schemas validate all request bodies; validation errors return `422` with field-level detail.

---

## 8. Functional Requirements

*(Mirrors PRD Section 6 — see PRD for full user-story rationale. IDs preserved for traceability.)*

### 8.0 Authentication & User Tenancy
- **FR-Auth-1 (P0) — User Registration:** New users register with `email`, `password`, and optional `full_name`. Validates email uniqueness (case-insensitive) and password strength (≥8 characters, uppercase, lowercase, number, special symbol). Stores BCrypt password hash. Returns JWT Access Token (15 min) and sets HttpOnly Refresh Token cookie (30 days).
- **FR-Auth-2 (P0) — User Login:** Authenticates credentials. On success, issues short-lived Access Token and sets new rotated Refresh Token cookie. On failure, returns generic `401 Unauthorized` without revealing whether the email exists.
- **FR-Auth-3 (P0) — Google OAuth 2.0 / OpenID Connect:** Sign in with Google verifies Google `id_token` on backend via Google's token verification library. Automatically creates user or links existing user by verified Google email. Issues identical application JWT Access Token + HttpOnly Refresh Token.
- **FR-Auth-4 (P0) — Refresh Token Rotation:** Client calls `/api/v1/auth/refresh` automatically before Access Token expiry using the HttpOnly cookie. Backend validates token hash, revokes old refresh token, generates a new refresh token, updates DB hash, and sets new cookie. If a revoked token is presented, triggers security revocation of all active sessions for that user.
- **FR-Auth-5 (P0) — Secure Logout & Logout All Devices:** Single logout revokes current session refresh token in database and clears the cookie. Logout all devices revokes all refresh tokens belonging to the user.
- **FR-Auth-6 (P1) — Password Recovery (Forgot/Reset Password via 4-Digit OTP with 50s Expiry):** `/auth/forgot-password` generates a single-use, 4-digit numeric OTP valid for exactly 50 seconds stored as a salted SHA-256 hash. `/auth/verify-otp` and `/auth/reset-password` validate OTP hash within 50s, update password with BCrypt hash, mark token used, and invalidate existing refresh tokens.
- **FR-Auth-7 (P1) — Change Password:** Authenticated user can change password by providing current password and new password.
- **FR-Auth-8 (P0) — Strict User Data Isolation (Zero Trust):** All expenses, incomes, budgets, and custom categories are strictly private to the user. The backend derives `user_id` exclusively from the validated JWT token claims. Any query or mutation on an entity not owned by the user returns `404 Not Found`.

### 8.1 Navigation
- **FR-1 (P0):** Responsive navigation with Dashboard, Expenses, Income, User Profile Menu (Avatar, Name, Change Password, Logout, Logout All), and INR currency badge. Unauthenticated users are redirected to `/login`.

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
- **FR-22 (P0):** Daily/weekly/monthly report views — implemented as the dashboard filtered by period.
- **FR-23–FR-25 (P1/P2):** Month-over-month comparison, top categories, averages.

### 8.6 Budget Management
- **FR-26–FR-28 (P0/P1):** Set overall + per-category budgets; live remaining balance; status indicator.
- **Formula:** `Remaining = Monthly Budget − Total Monthly Expenses`.
- **Near-Limit threshold:** default **80%** of budget consumed, configurable via a settings constant.

### 8.7 Data Export
- **FR-29 (P2, nice-to-have):** CSV/PDF/Excel export. Deferred to Phase 2 if V1 timeline is tight.

### 8.8 Data Integrity
- **FR-30 (P0):** No hardcoded/demo data in the finished application — all dashboard values come from real stored data.

### 8.9 AI Financial Intelligence & Smart Recommendations
- **FR-AI-1 (P0) — "Can I Afford This?" Purchase Decision Simulator:** Takes `title` and `amount` (plus optional `category_id` and `target_date`). Backend pulls user's monthly income, total spent, remaining overall budget, remaining daily budget, and calculates projected month-end balance. Formats structured JSON schema response with verdict (`safe` / `caution` / `over_budget`), budget delta, savings rate impact %, and AI reasoning summary. Includes deterministic local math fallback.
- **FR-AI-2 (P1) — Smart Receipt & UPI Screenshot Scanner (AI Vision):** Accepts image binary/base64 payload (client-compressed to ≤1MB). Dispatches to Gemini 1.5 Flash Vision API with structured response schema extracting `title`, `amount`, `date`, predicted `category_id`, and `payment_mode` (`Cash`, `Card`, `UPI`, `Net Banking`, `Other`).
- **FR-AI-3 (P0) — Autonomous "Leak Hunter" & Subscription Audit:** Evaluates recurring transactions over 30/60/90 days (defined as transactions with identical/similar titles occurring at 25-35 day intervals or daily micro-transactions under ₹150). Aggregates monthly and annualized impact, delivering actionable leak reduction tips.
- **FR-AI-4 (P0) — Dynamic "Safe-to-Spend" Daily Burn Rate Dial:** Algorithmic calculation: `Safe_Today = (Remaining_Monthly_Budget - Projected_Fixed_Bills) / Remaining_Days_In_Month`. Adapts in real-time based on past daily spend velocity. Returns today's allowance, week forecast array, and status flag.
- **FR-AI-5 (P1) — Goal-Driven "Reverse Budgeting" & Target Savings Engine:** Allows users to create savings targets with `title`, `target_amount`, `current_amount`, and `target_date`. Calculates monthly contribution requirement: `Required_Monthly = (Target_Amount - Current_Amount) / Months_Left`. Analyzes discretionary spending categories and outputs recommended category budget reductions.
- **FR-AI-6 (P2) — "Spendora Monthly Wrapped" Visual Financial Story:** Monthly aggregation pipeline producing structured story slides: top spending category, total savings, money persona classification (based on savings rate %: `<0% Overspender`, `0-10% Minimalist`, `10-25% Steady Saver`, `>25% Wealth Builder`), biggest transaction, and 3 tailored monthly financial challenges.

---

## 9. Validation Rules

| Field | Rule | Enforced |
|---|---|---|
| User email | Required, valid email format, normalized to lowercase, unique | Pydantic `EmailStr` + DB UNIQUE constraint |
| User password | Required, min 8 chars, at least 1 uppercase, 1 lowercase, 1 number, 1 special character | Zod (client) + Pydantic validator (server) |
| User full_name | Optional, max 100 chars | Zod + Pydantic |
| Google ID Token | Required for Google Auth, valid Google signature, verified `aud` | `google-auth` backend verifier |
| Expense title | Required, max 50 chars | Zod (client) + Pydantic (server) |
| Expense amount | Required, numeric, > 0 | Zod + Pydantic + DB CHECK constraint |
| Expense date | Required, ≤ today | Zod + Pydantic + DB CHECK constraint |
| Income amount | Required, numeric, > 0 | Zod + Pydantic + DB CHECK constraint |
| Category | Required (must exist) | Pydantic FK validation |
| Category name | Required, unique per user | Pydantic + DB UNIQUE constraint |
| Budget amount | Required, numeric, > 0 | Zod + Pydantic + DB CHECK constraint |
| Refresh token | Required in cookie for `/auth/refresh` | Cookie verification + DB hash match |

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
| Performance | Dashboard/report queries backed by indexed columns (`(user_id, expense_date)`, `(user_id, category_id)`); expense list paginated server-side. |
| Scalability | Layered 5-layer architecture allows easy scaling and clean separation of concerns. |
| Reliability | All CRUD operations wrapped in try/except with meaningful HTTP error codes; no silent failures. |
| Maintainability | Repository/service separation keeps business logic testable independent of routes. |
| Testability | Automated test suite for auth flows, token rotation, data isolation, budget limits, and expense CRUD. |
| Responsiveness | Verified on mobile, tablet, and desktop breakpoints per Section 10.2. |
| Security | Production-grade JWT rotation, BCrypt hashing, HttpOnly cookies, zero client trust, and rate limiting. |

---

## 12. Business Rules

1. Expense amounts must always be greater than zero (DB CHECK constraint).
2. Future expense dates are rejected (DB CHECK constraint + API validation).
3. Every expense, budget, and income record must belong to an authenticated user (`user_id` FK NOT NULL).
4. **Zero-Trust User Tenancy:** The backend derives `user_id` strictly from the validated JWT token claims (`current_user.id`). No client-sent `userId` parameter is ever trusted for authorization.
5. Cross-user isolation: User A cannot read, modify, or delete User B's resources. Access attempts across boundaries return `404 Not Found`.
6. Deleting an expense requires client-side confirmation before the DELETE call fires.
7. Deleting a category must never orphan expenses belonging to the user (Section 5.8).
8. Dashboard totals are always computed from live queries scoped to `current_user.id` — never cached or hardcoded.
9. Budget remaining recalculates on every expense create/update/delete.
10. Negative remaining budget = over-budget status (clamped to `₹0.00` in UI display).
11. All monetary math uses `NUMERIC`/`Decimal` types — never floats — to avoid rounding errors.
12. No hardcoded financial values anywhere in production code.

---

## 13. Security Architecture & Specifications

### 13.1 Password & Credential Security
- **Hashing Algorithm:** BCrypt (`passlib[bcrypt]`) with auto-salted rounds (work factor ≥ 12).
- Plaintext passwords are never stored in the database, printed in log statements, or reflected in API responses.
- Passwords must satisfy strength constraints (min 8 characters, uppercase, lowercase, digit, special symbol).

### 13.2 JWT Token Lifecycle & Rotation
- **Access Token:**
  - Algorithm: `HS256`.
  - Expiration: **15 minutes**.
  - Payload claims: `sub` (`user_id`), `email`, `type: "access"`, `iat`, `exp`.
  - Transmission: `Authorization: Bearer <token>` HTTP header.
  - Storage: In-memory only in frontend state (`AuthContext`); never in `localStorage` or `sessionStorage`.
- **Refresh Token:**
  - Expiration: **30 days**.
  - Transmission & Storage: `HttpOnly; Secure; SameSite=Lax; Path=/api/v1/auth` cookie. Unreachable by client JavaScript (XSS mitigation).
  - Database Storage: Only a cryptographic hash (`SHA-256`) of the refresh token is stored in the `refresh_tokens` table.
  - **Rotation:** Every call to `/api/v1/auth/refresh` revokes the incoming token in the database, generates a fresh token pair, and sets a new rotated cookie.
  - **Token Reuse Detection:** If an already-revoked refresh token is presented, all active refresh tokens for that user are immediately revoked (session hijacking protection).
- **Session Revocation:**
  - `/api/v1/auth/logout`: Revokes the current session's refresh token and clears the cookie (`Max-Age=0`).
  - `/api/v1/auth/logout-all`: Revokes all active refresh tokens for the user across all devices.

### 13.3 Google Sign-In (OAuth 2.0 / OpenID Connect)
- Frontend obtains an OpenID Connect `id_token` from Google Identity Services (GIS).
- Frontend sends `id_token` to `POST /api/v1/auth/google`.
- Backend validates the token signature, issuer (`accounts.google.com`), expiration, and audience (`GOOGLE_CLIENT_ID`) using `google-auth`.
- If valid, the backend creates or links the account by email, assigns `auth_provider='google'`, and issues application JWT Access & Refresh tokens.

### 13.4 Rate Limiting & Abuse Prevention
- Sensitive auth endpoints are rate-limited via `SlowAPI`:
  - `POST /api/v1/auth/login`: 5 requests per minute per IP.
  - `POST /api/v1/auth/register`: 3 requests per minute per IP.
  - `POST /api/v1/auth/forgot-password`: 3 requests per minute per IP.
  - `POST /api/v1/auth/refresh`: 20 requests per minute per IP.

### 13.5 CORS & Transport Security
- **HTTPS:** Enforced in production deployments (Render/Vercel).
- **CORS:** Configured with explicit origins (`CORS_ORIGINS`), `allow_credentials=True` (required for HttpOnly cookies), and allowed headers (`Authorization`, `Content-Type`).
- **CSRF Protection:** Cookie-based endpoints enforce `SameSite=Lax` (or `SameSite=Strict`), protecting against cross-site request forgery.

### 13.6 Environment Secrets Management
- All secrets (`JWT_SECRET_KEY`, `GOOGLE_CLIENT_ID`, `DATABASE_URL`) are read strictly from environment variables.
- Secrets are never hardcoded or committed to version control. `.env.example` documents all required keys without values.

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
