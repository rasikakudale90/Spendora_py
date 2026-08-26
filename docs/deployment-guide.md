# Spendora V1 — Production Deployment Runbook

> **Target Stack:** Supabase (Database) + Render (Backend API) + Vercel (Frontend UI)  
> **Repository:** `https://github.com/rasikakudale90/Spendora_py`

---

## 📋 Overview of Deployment Architecture

```
                  ┌─────────────────────────────────┐
                  │       Vercel (Frontend)         │
                  │   https://spendora.vercel.app   │
                  └────────────────┬────────────────┘
                                   │ HTTPS REST API
                                   ▼
                  ┌─────────────────────────────────┐
                  │        Render (Backend)         │
                  │ https://spendora.onrender.com   │
                  └────────────────┬────────────────┘
                                   │ PostgreSQL (asyncpg)
                                   ▼
                  ┌─────────────────────────────────┐
                  │       Supabase (Database)       │
                  │   Managed PostgreSQL Database   │
                  └─────────────────────────────────┘
```

---

## Part 1: Supabase Database Setup (5 Minutes)

1. **Log in to Supabase**: Go to [https://supabase.com](https://supabase.com) and click **New Project**.
2. **Project Details**:
   - **Name:** `spendora-db`
   - **Database Password:** Choose a strong password (save this safely!).
   - **Region:** Choose the region closest to your users (e.g. `us-east-1` / `ap-south-1`).
   - **Pricing Plan:** Free tier.
3. **Get the Connection String**:
   - Navigate to **Project Settings** (gear icon) $\rightarrow$ **Database**.
   - Under **Connection string**, select the **URI** tab.
   - Switch mode to **Session** (port 5432) or **Transaction** (port 6543).
   - Format: `postgresql://postgres.[project-ref]:[YOUR-PASSWORD]@aws-0-[region].pooler.supabase.com:5432/postgres`
4. **Format for SQLAlchemy asyncpg**:
   - Change the prefix from `postgresql://` or `postgres://` to `postgresql+asyncpg://`:
   ```env
   DATABASE_URL=postgresql+asyncpg://postgres.[project-ref]:[YOUR-PASSWORD]@aws-0-[region].pooler.supabase.com:5432/postgres
   ```

---

## Part 2: Render Backend Deployment (5 Minutes)

### Option A: Using 1-Click Render Blueprint (Recommended)
1. Log in to [https://render.com](https://render.com).
2. Click **New +** $\rightarrow$ **Blueprint**.
3. Connect your repository: `rasikakudale90/Spendora_py`.
4. Render will automatically detect `render.yaml`.
5. Enter the `DATABASE_URL` you got from Supabase in Part 1.
6. Click **Apply**. Render will automatically build the Docker container and start the server!

### Option B: Manual Web Service Setup
1. In Render Dashboard, click **New +** $\rightarrow$ **Web Service**.
2. Connect your GitHub repository: `rasikakudale90/Spendora_py`.
3. Configure the service:
   - **Name:** `spendora-backend`
   - **Region:** Same region as Supabase (e.g., Oregon / Ohio).
   - **Runtime:** `Docker` (or `Python 3`).
   - **Docker Command / Context:**
     - **Docker Build Context Directory:** `backend`
     - **Dockerfile Path:** `backend/Dockerfile`
   - **Instance Type:** `Free`.
4. **Environment Variables**:
   Add the following under the **Environment** tab:
   | Key | Value | Notes |
   |-----|-------|-------|
   | `DATABASE_URL` | `postgresql+asyncpg://postgres...` | Your Supabase connection string from Part 1 |
   | `BUDGET_NEAR_LIMIT_THRESHOLD` | `0.80` | Default 80% limit warning |
   | `CORS_ORIGINS` | `*` | Or your Vercel URL (e.g. `https://spendora.vercel.app`) |
   | `PORT` | `8000` | (Render sets this automatically) |
5. Click **Create Web Service**.
6. Once deployed, note down your Render service URL (e.g. `https://spendora-backend.onrender.com`).
7. Verify health check: Visit `https://spendora-backend.onrender.com/health` (should return `{"status": "ok"}`).
8. Verify API documentation: Visit `https://spendora-backend.onrender.com/docs`.

---

## Part 3: Vercel Frontend Deployment (3 Minutes)

1. Log in to [https://vercel.com](https://vercel.com).
2. Click **Add New...** $\rightarrow$ **Project**.
3. Import your GitHub repository: `rasikakudale90/Spendora_py`.
4. **Configure Project**:
   - **Framework Preset:** `Next.js` (automatically detected).
   - **Root Directory:** Click **Edit** and choose `frontend`.
   - **Build Command:** `npm run build`
   - **Output Directory:** `.next`
5. **Environment Variables**:
   Add the public API URL environment variable:
   | Key | Value | Example |
   |-----|-------|---------|
   | `NEXT_PUBLIC_API_URL` | `https://<YOUR-RENDER-APP>.onrender.com` | `https://spendora-backend.onrender.com` |
6. Click **Deploy**.
7. Vercel will build and deploy your Next.js application in ~60 seconds.

---

## Part 4: Production Smoke Test & Verification

Once both Render and Vercel are deployed:

1. Open your live Vercel URL (e.g., `https://spendora.vercel.app/dashboard`).
2. **Test Theme Toggle**: Click the Sun/Moon button in the top navbar to verify dark/light mode transition.
3. **Test Category Creation**: Click **Categories** modal $\rightarrow$ Add `"Dining Out"` $\rightarrow$ Confirm toast notification.
4. **Test Budget Setting**: Click **Budgets** modal $\rightarrow$ Set Overall Budget to `₹50,000` $\rightarrow$ Confirm KPI progress bar updates.
5. **Test Expense Logging**: Navigate to `/expenses` $\rightarrow$ Click **Add Expense** $\rightarrow$ Add `₹1,200` for `"Dinner with friends"` under `"Dining Out"` using `"UPI"`.
6. **Verify Dashboard Metrics**:
   - Total spent displays `₹1,200.00`.
   - Remaining budget updates dynamically.
   - Recharts Spending Trend and Category Breakdown render data correctly.
7. **Verify Database Persistence**: Open Supabase Table Editor and confirm rows in `categories`, `expenses`, and `budgets` tables.

---

## 🔒 Security Best Practices Checklist
- [x] Passwords & API secrets are never committed to version control (`.gitignore` protects local `.env` files).
- [x] Production Dockerfiles run non-root users (`nextjs` user in Frontend).
- [x] Database migrations run automatically via `alembic upgrade head` in `backend/entrypoint.sh`.
- [x] CORS middleware is configurable via `CORS_ORIGINS` to prevent unauthorized cross-origin requests.
- [x] Automated CI/CD runs test suites on every pull request and push to `main`.
