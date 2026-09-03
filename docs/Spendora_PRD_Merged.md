# Spendora — Product Requirements Document (PRD)
## Personal Expense & Budget Tracking Web Application

*Merged and refined from the Spendora and FinTrack PRD drafts.*

---

## 1. Overview

**Product Name:** Spendora

**Product Type:** Personal Expense and Budget Tracking Web Application

**Summary:** Spendora is a simple, user-friendly personal finance web app that lets users log daily expenses, track income, organize spending into categories, and instantly see the impact on charts and a live budget. Spendora provides secure personal user accounts: every user signs in to their own completely private workspace, ensuring that personal financial data is protected and accessible across devices.

**Core Product Loop:**

> **Sign in to private account → Record spending & income → See it reflected on personal dashboard → Understand spending habits → Stay on budget**

Spendora is built around **personal user accounts with complete data privacy**. Each user has their own private space with independent expenses, budgets, and categories. Users can sign up with an email and password or use one-click Google Sign-In.

### 1.1 Vision

Spendora should answer three questions quickly, at any time, for every user:

1. **How much have I spent and saved?**
2. **Where is my money going?**
3. **Am I within my budget, or over it?**

### 1.2 Product Philosophy

- **Simple:** A user should be able to understand the app without instructions.
- **Fast:** Adding an expense should take less than 30 seconds.
- **Private & Secure:** Your financial life is private. Only you can view, edit, or manage your data; no other user can ever see or touch your records.
- **Convenient:** Log in with email/password or instantly with Google; stay signed in securely without constant re-entry.
- **Useful:** Dashboard information should help users make better spending decisions.
- **Data-driven:** Every number shown comes from real stored user data — never hardcoded or demo values.
- **Built to last:** The architecture supports later features without requiring the core to be rebuilt.

---

## 2. Problem Statement

Most people don't track their expenses consistently. They rely on phone notes, spreadsheets, memory, or scattered records — approaches that create real problems:

- Expenses are easily forgotten or never logged.
- Historical transactions are hard to find, search, or filter.
- Financial notes kept on a single device can be lost, snooped on, or lack privacy.
- Spending categories are unclear or inconsistent.
- Users can't easily identify their largest spending areas.
- It's difficult to compare spending across different periods.
- Users don't know how much of their monthly budget remains.
- Raw transaction records give no visual insight into habits.

Spendora solves this with one centralized, private application for recording, organizing, and analyzing personal expenses — turning a passive log into an active budgeting tool with full account privacy across devices.

---

## 3. Goals

### 3.1 Primary Goals

| Goal | Why It Matters |
|---|---|
| Secure personal accounts & private workspaces | Financial records must remain private and accessible only to the owner |
| One-click Google Sign-In & simple email signup | Getting into the app should be frictionless |
| Add an expense in under 30 seconds | Easy logging is the difference between a habit and an abandoned app |
| Store expenses reliably in a real database | No data loss, no fake numbers |
| Allow editing/deleting incorrect expenses | Records must stay accurate over time |
| Let users organize expenses with custom categories | Spending should be organized the way the user actually thinks about it |
| Provide a dashboard with meaningful spending & cash flow info | Turns raw data into a quick answer to "how am I doing?" |
| Provide charts for visual spending analysis | Patterns and spikes are easier to see than to calculate |
| Support search, filter, and sort on expenses (usable together) | A log is only useful if old entries are easy to find |
| Support an overall and per-category budget with live remaining balance | Turns the app from a log into real budgeting |
| Password recovery & multi-device session controls | Users can recover lost passwords and log out of all devices if needed |
| Keep the app simple and focused | Ship something intuitive, fast, and practical |

### 3.2 Usability Goals

- Account sign-up or Google Sign-In takes less than 15 seconds.
- Expense creation should take less than 30 seconds.
- Navigation should be simple, clean, and predictable.
- Important financial information should be visible without excessive navigation.
- Forms should provide clear validation messages.
- Empty, loading, success, and error states must be handled for every screen.

### 3.3 Success Metrics

**Usage & Engagement**
- Number of active users and signup completion rate
- Number of expenses and incomes logged per week
- Frequency of dashboard and chart views
- Search/filter usage frequency

**Usability**
- Average time to sign in or add an expense (target: **under 30 seconds**)
- Expense creation completion rate / low error rate during entry

**Budgeting**
- % of users who set a budget goal
- Frequency of budget updates
- Number of users checking budget status

**Retention & Trust**
- User return rate after 7, 30, and 60 days

---

## 4. Target Users

**Primary user:** Any individual who wants a personal, private, and easy-to-use tool to track their personal spending and budgets.

**Suitable for:**
- Working professionals and freelancers managing personal cash flow
- Students tracking monthly allowances and daily expenses
- Budget-conscious individuals building savings habits
- Anyone who wants their personal financial records kept securely private and accessible across their devices

**Not intended for:**
- Corporate accounting teams or large enterprise billing
- Shared group expense splitting (roommates/couples) — planned for a later phase
- Investment trading and stock portfolio management
- Direct banking transactions or money transfers

---

## 5. Scope

### 5.1 In Scope

- **Secure User Accounts:** Personal sign-up, email/password login, and Sign in with Google
- **Complete Privacy & Data Isolation:** Each user sees exclusively their own expenses, incomes, budgets, and custom categories
- **Session & Device Security:** Secure login session, easy logout, and "Log out of all devices"
- **Account Self-Service:** Password recovery (Forgot / Reset Password) and Change Password
- **Full Expense Tracking:** Add, view, edit, and delete expenses (with confirmation)
- **Income Tracking & Cash Flow:** Record income, track monthly cash flow (`Income - Expenses`), and view savings rate %
- **Dynamic Categories:** User-created categories alongside starter suggestions
- **Multi-Period Budgets:** Daily, weekly, monthly, and yearly budget caps with real-time over-budget warnings
- **Search, Filter & Sort:** Filter by date range, category, amount, payment mode, and text search
- **Visual Dashboard:** Donut charts, trend charts, recent activity, and top spending areas
- **Single Fixed Currency:** Indian Rupee (₹ / INR), 2 decimal places
- **Mobile-First & PWA:** Installable web app running smoothly on mobile, tablet, and desktop browsers
- **Real Data Only:** 100% database-backed personal records — no demo or hardcoded numbers

### 5.2 Out of Scope

- Multi-user joint accounts or shared household budgets (planned for Phase 3)
- Direct bank account sync or automated SMS reading
- Multi-currency conversion
- Stock/crypto investment tracking
- Automated invoice scanning / receipt OCR
- Automated recurring bank transfers

---

## 6. Functional Requirements & User Stories

### 6.0 User Accounts, Authentication & Privacy

| ID | Capability | Description | Priority | User Story |
|---|---|---|---|---|
| FR-Auth-1 | **Account Creation** | Register for a free personal account using email and a secure password | P0 | As a new user, I want to create a private account with my email and password so my financial data is safely stored for me. |
| FR-Auth-2 | **Email & Password Login** | Sign in securely to access personal financial records | P0 | As a returning user, I want to log in quickly so I can access my personal dashboard. |
| FR-Auth-3 | **Sign in with Google** | One-click instant login or sign-up using an existing Google account | P0 | As a user, I want to sign in with Google so I can get into Spendora in one click without typing another password. |
| FR-Auth-4 | **Complete Data Privacy (Isolation)** | Every user's expenses, budgets, incomes, and custom categories are completely private; no user can ever see or modify another user's data | P0 | As a user, I want my financial information to be strictly private so no other user can see what I spend or earn. |
| FR-Auth-5 | **Seamless & Secure Sessions** | The app securely remembers authenticated sessions so users don't have to repeatedly re-enter passwords, while automatically keeping sessions fresh | P0 | As a user, I want to stay smoothly logged in on my device without having to type my password every few minutes. |
| FR-Auth-6 | **Secure Logout** | Easily log out from the current device with one click | P0 | As a user, I want to log out when I'm done on a shared computer so nobody else can see my finances. |
| FR-Auth-7 | **Log Out from All Devices** | End all active sessions across all devices from the user menu | P1 | As a user, I want to log out of all devices if I misplace my phone or want to ensure full account safety. |
| FR-Auth-8 | **Forgot & Reset Password** | Self-service password recovery if a user forgets their login password | P1 | As a user, I want to reset my password via email so I never get permanently locked out of my account. |
| FR-Auth-9 | **Change Password** | Update password anytime from the user profile settings | P1 | As a user, I want to change my password whenever I wish to maintain good personal security. |

### 6.1 Navigation

| ID | Requirement | Priority | User Story |
|---|---|---|---|
| FR-1 | Responsive navigation bar with links to **Dashboard**, **Expenses**, **Income**, and a **User Profile Menu** (showing user name/avatar, Change Password, Logout, and Logout All Devices) | P0 | As a user, I want clean navigation so I can move between tracking expenses, income, budgets, and managing my account with ease. |

### 6.2 Expense Fields & Validation

| Field | Required | Description |
|---|---|---|
| Title | Yes | Short description of the expense (max 50 characters) |
| Category | Yes | Category associated with the expense; pick existing or create new |
| Amount | Yes | Amount spent, displayed as ₹ with 2 decimal places (e.g. ₹1,234.00) |
| Date | Yes | Date the expense occurred; defaults to today, cannot be in the future |
| Notes | No | Additional free-text detail |
| Payment Mode | No | Cash, Card, UPI, etc. |

**Example**
```text
Title: Groceries
Category: Food
Amount: ₹850.00
Date: 26 August 2026
Notes: Weekly groceries
Payment Mode: UPI
```

| Validation Rule | Why |
|---|---|
| Title is required, max 50 characters | Keeps the expense list scannable |
| Amount must be numeric and greater than zero | Prevents bad data from skewing totals and charts |
| Date cannot be in the future | Keeps the log honest to actual spending |
| Category must be selected | Every expense must be organized |
| Invalid submissions show clear inline errors | Users should never be left guessing what went wrong |

### 6.3 Expense CRUD

| ID | Action | Description | Priority | User Story |
|---|---|---|---|---|
| FR-2 | Add | Create a new expense entry | P0 | As a user, I want to quickly add an expense so logging spending doesn't feel like a chore. |
| FR-3 | View | See all logged expenses in a paginated list | P0 | As a user, I want to view my past expenses so I can review my spending history. |
| FR-4 | Edit | Update any field of an existing expense | P0 | As a user, I want to edit my past expenses so my records stay accurate. |
| FR-5 | Delete | Remove an expense, with a confirmation step | P0 | As a user, I want to delete an expense (with confirmation) so I don't lose data by accident. |

### 6.4 Category Management

Categories are dynamic — the user builds their own list rather than choosing from a fixed set.

| ID | Action | Description | Priority | User Story |
|---|---|---|---|---|
| FR-6 | Create | Add a new category by name, while logging an expense or from a category list | P0 | As a user, I want to create my own categories so my spending is organized the way I actually think about it. |
| FR-7 | Rename | Edit an existing category's name | P0 | As a user, I want to rename a category so my organization stays consistent over time. |
| FR-8 | Delete | Remove a category — only if unused, or reassign/cascade linked expenses with a warning | P0 | As a user, I want to safely delete a category without losing or orphaning expense data. |
| FR-9 | View | See the list of categories along with how many expenses use each | P1 | As a user, I want to see how many expenses are in each category so I understand my usage. |
| FR-10 | Starter categories | Ship with common defaults (Food, Transport, Rent, Shopping, Education, Entertainment, Bills, Healthcare, Other) so the app isn't empty on first use — fully editable | P2 | As a new user, I want to see some starter categories so the app feels usable from day one. |

**Category deletion rule:** A category containing existing expenses must never be deleted silently. The app must either prevent deletion until expenses are reassigned, or prompt the user to select a replacement category. No expense may become orphaned.

### 6.5 Search, Filter & Sort

All capabilities below must work together (e.g. filter by "Food" category, then sort by highest amount).

| ID | Capability | Details | Priority | User Story |
|---|---|---|---|---|
| FR-11 | Search | By title or notes text | P1 | As a user, I want to search my expenses by title or note so I can quickly find a specific transaction. |
| FR-12 | Filter — Date | Today / this week / this month / custom range | P0 | As a user, I want to filter by date range so I can review a specific period. |
| FR-13 | Filter — Category | Isolate spend on a specific category | P0 | As a user, I want to filter by category so I can see how much I spent in one area. |
| FR-14 | Filter — Amount Range | Optional min/max | P1 | As a user, I want to filter by amount range so I can find larger or smaller transactions. |
| FR-15 | Filter — Payment Mode | Cash / Card / UPI, etc. | P1 | As a user, I want to filter by payment mode so I can see how I paid for things. |
| FR-16 | Sort | By date, amount, category, or title; ascending/descending | P1 | As a user, I want to sort my expenses so I can quickly scan the highest, most recent, or grouped entries. |

### 6.6 Dashboard & Analytics

The dashboard is the main information screen. Its purpose is to answer: **"How am I spending my money?"**

| ID | Requirement | Why | Priority | User Story |
|---|---|---|---|---|
| FR-17 | Total amount spent (overall, and current month by default) | The single most-asked question | P0 | As a user, I want to see my total spend so I immediately know where I stand. |
| FR-18 | Quick view of recent expenses | Snapshot without opening the full list | P0 | As a user, I want to see recent expenses on the dashboard without opening the full list. |
| FR-19 | Pie/donut chart — spending by category | Instantly shows where money is going | P0 | As a user, I want a category breakdown chart so I know where my money is going. |
| FR-20 | Bar/line chart — spending over time | Reveals patterns and spikes | P0 | As a user, I want to see my spending trend over time so I can spot patterns or spikes. |
| FR-21 | Budget status vs. goal | Turns the dashboard into a budgeting tool, not just a log | P0 | As a user, I want to see my budget status on the dashboard so I know if I'm on track. |
| FR-22 | Daily/Weekly/Monthly report views | Lets the user analyze spending across time periods | P0 | As a user, I want to view expenses by day, week, and month so I can analyze habits over time. |
| FR-23 | Month-over-month comparison with % change | Tells the user if they're improving | P1 | As a user, I want to compare this month to last month so I know if I'm improving. |
| FR-24 | Top spending categories, ranked | Surfaces the biggest spending areas without digging | P1 | As a user, I want to see my top spending categories so I know where to cut back. |
| FR-25 | Average daily/weekly spend, highest expense, expense count | Gives a normalized sense of spending pace | P2 | As a user, I want to see averages so I can gauge my daily/weekly pace. |

### 6.7 Budget Management

Budget tracking is what turns Spendora from a simple expense log into a real budgeting application — **this is the heart of V1.**

| ID | Requirement | Priority | User Story |
|---|---|---|---|
| FR-26 | Set an overall monthly budget goal and optional per-category budget limits | P0 | As a user, I want to set a budget goal so I can catch overspending early. |
| FR-27 | Live remaining-balance tracking, recalculated whenever an expense is added, edited, or deleted | P0 | As a user, I want my remaining budget to update live so I always know where I stand. |
| FR-28 | Status indicator — **On Track / Near Limit / Over Budget** | P1 | As a user, I want to be warned when I'm close to or over my budget so I can adjust in time. |

**Formula:** `Remaining Budget = Monthly Budget − Total Monthly Expenses`

```text
Monthly Budget: ₹15,000
Spent:          ₹9,200
Remaining:      ₹5,800
Status:         On Track
```

The exact "Near Limit" threshold should be configurable in the implementation, with a sensible default. A negative remaining balance means the budget has been exceeded.

### 6.8 Data Export (Nice-to-Have)

| ID | Requirement | Why | Priority | User Story |
|---|---|---|---|---|
| FR-29 | Export expenses (filtered or full) as CSV/PDF/Excel | Backup and analysis outside the app | P2 | As a user, I want to export my expenses so I can back them up or analyze them elsewhere. |

Include only if time permits in V1; otherwise, defer to Phase 2 without blocking the release.

### 6.9 Data Integrity Principle

| ID | Requirement | Priority | User Story |
|---|---|---|---|
| FR-30 | No hardcoded or demo data at any stage — all data (expenses, categories, budgets, dashboard totals, charts) is dynamically created, stored, and fetched from the real data layer | P0 | As a new user, I want an honest empty state built from real data, so the app reflects my actual usage from day one. |

Demo/placeholder data may exist temporarily during development but must never remain in the finished application.

### 6.10 AI-Powered Financial Intelligence & Smart Recommendations

Spendora transforms from a passive expense log into an active financial advisor by leveraging intelligent AI reasoning. All AI features are designed to be intuitive, non-intrusive, and 100% focused on helping users make better real-time money decisions without financial jargon.

| ID | Feature | Non-Technical Description | Priority | User Story |
|---|---|---|---|---|
| FR-AI-1 | **"Can I Afford This?" Purchase Decision Simulator** | Before making an impulsive or large purchase, the user inputs an item name and price (e.g. *"Smartwatch ₹12,000"*). Spendora calculates remaining cash flow, active budgets, and upcoming bills to provide an instant color-coded verdict: 🟢 **Safe to Buy** (savings intact), 🟡 **Caution** (tells how much to trim from other categories this week), or 🔴 **Delay Purchase** (will cause a budget deficit; suggests best date to buy). | P0 | As a user, I want to test potential purchases before spending money so I never accidentally blow my monthly budget on impulse buys. |
| FR-AI-2 | **Smart Receipt & UPI Screenshot Scanner (AI Vision)** | User uploads or snaps a picture of a paper bill, restaurant receipt, or UPI payment confirmation screen. Spendora automatically reads the merchant title, total amount, transaction date, and payment method, pre-populating the "Add Expense" form for effortless 1-tap confirmation. | P1 | As a user, I want to snap receipts and payment screenshots so I don't have to manually type expense details every single time. |
| FR-AI-3 | **Autonomous "Leak Hunter" & Subscription Audit** | Automatically scans past transactions to surface hidden money drains: calculates the annualized cost of daily micro-expenses (e.g. *"Your daily ₹80 snack spend totals ₹29,200 per year"*), aggregates food delivery convenience charges, and lists all active recurring digital subscriptions. | P0 | As a user, I want Spendora to uncover hidden spending leaks and recurring charges so I can cut unnecessary waste without thinking. |
| FR-AI-4 | **Dynamic "Safe-to-Spend" Daily Burn Rate Dial** | Converts abstract monthly budgets into a single, live daily spending allowance on the dashboard (e.g. *"Today's Safe-to-Spend: ₹750"*). If the user spends less today, the surplus rolls over; if they overspend, tomorrow's daily limits automatically re-balance so the month still ends 100% on budget. | P0 | As a user, I want a single daily spending number so I know exactly how much I can spend each day without doing mental math. |
| FR-AI-5 | **Goal-Driven "Reverse Budgeting" & Target Savings Engine** | Users set tangible financial goals (e.g. *"Trip to Goa ₹30,000 in 4 months"* or *"Emergency Fund ₹1,00,000"*). Spendora calculates required monthly savings and automatically suggests specific category budget adjustments (e.g. *"Trim Shopping by ₹1,500/mo and Dining by ₹1,000/mo"*) to achieve the target on time. | P1 | As a user, I want to save for specific life goals with custom AI budget plans so I achieve my dreams without falling into debt. |
| FR-AI-6 | **"Spendora Monthly Wrapped" Visual Financial Story** | On the 1st of every month, Spendora presents a celebratory, interactive slide story highlighting the user's monthly "Money Persona" (e.g. *The Strategic Saver*), top financial wins, category milestones, and 3 custom money challenges for the new month. | P2 | As a user, I want a visual monthly summary of my financial achievements so managing money feels rewarding and motivating. |


---

## 7. Key User Flows

| Flow | Steps |
|---|---|
| **Create an Account** | Register → Enter email & password (or choose "Sign in with Google") → Instant account creation → Directed to clean, private personal dashboard |
| **Sign In** | Login → Enter credentials (or click "Sign in with Google") → Directed to personal dashboard with previous expenses & budgets loaded |
| **Add an expense** | Expenses → Add New → Fill form (pick or create category) → Save → Expense appears in list → Dashboard & budget update |
| **Edit an expense** | Expenses → Select expense → Edit → Update fields → Save → Dashboard & budget recalculated |
| **Delete an expense** | Expenses → Select expense → Delete → Confirm → Dashboard & budget recalculated |
| **Find an expense** | Expenses → Search / Filter / Sort → Matching expenses → View / Edit / Delete |
| **Set a budget** | Budget Modal → Select period (Daily/Weekly/Monthly/Yearly) → Enter amount → Save → Remaining balance & status shown |
| **Track income & cash flow** | Income → Add income → Enter source and amount → View monthly cash flow (`Income - Expenses`) on dashboard |
| **Forgot password** | Login → Forgot Password → Enter email → Follow secure reset instructions → Set new password → Sign in |
| **Log out** | User Menu → Click "Log Out" (ends session on current device) or "Log out of all devices" (secures account everywhere) |

---

## 8. Navigation Structure

```text
Spendora
│
├── Public / Onboarding
│   ├── Login (Email/Password or Sign in with Google)
│   ├── Register (New account creation)
│   ├── Forgot Password
│   └── Reset Password
│
└── Authenticated Private Space
    ├── Dashboard   (spending summary, cash flow, charts, budget status, recent activity)
    ├── Expenses    (add / view / edit / delete, search, filter, sort, category management)
    ├── Income      (add / view / edit / delete income, source breakdown, monthly totals)
    └── User Menu   (Profile view, Change Password, Log Out, Log Out All Devices)
```

---

## 9. UI/UX Requirements

### 9.1 General Design
Clean, simple, responsive, easy to understand, and consistent across desktop and mobile browsers with dark mode support.

### 9.2 Forms
- Clearly label every field and mark required fields.
- Validate input inline with understandable error messages (e.g. password strength rules, positive amounts).
- Provide clear Save and Cancel actions.

### 9.3 Feedback
Provide confirmation after important actions, e.g.:
- "Account created successfully"
- "Signed in successfully"
- "Logged out successfully"
- "Expense added successfully"
- "Category created successfully"
- "Budget updated successfully"

### 9.4 Empty States
If there are no expenses recorded yet, the dashboard shows helpful, welcoming guidance rather than blank or broken charts:

> Welcome to Spendora! No expenses recorded yet. Add your first expense or set a budget to begin taking control of your spending.

---

## 10. Currency

A single fixed currency: **Indian Rupee (₹ / INR)**, displayed consistently with 2 decimal places (e.g. `₹1,250.00`).

---

## 11. Data Requirements

Persistent storage is required for:

**User Account**
- Unique ID, Email address, Encrypted password (optional for Google users), Name, Avatar, Sign-in method, Created & updated timestamps

**Active Sessions**
- Secure session identifiers to keep trusted devices signed in safely, with support for single logout or all-device logout

**Expense**
- Owner (User ID), Title, Category, Amount, Date, Notes, Payment Mode, Created & updated timestamps

**Category**
- Name, Owner (User ID or global starter category), Created & updated timestamps

**Budget**
- Owner (User ID), Amount, Scope (overall or category), Period type (daily, weekly, monthly, yearly), Period dates

**Income**
- Owner (User ID), Title, Amount, Date, Source, Payment Mode, Notes, Created & updated timestamps

---

## 12. Business Rules

1. **Complete Data Privacy:** Every expense, income, budget, and custom category is strictly personal. No user can see, modify, or delete another user's financial records.
2. Expense amounts must always be greater than zero.
3. Future expense dates are not allowed.
4. Every expense must belong to a category.
5. Deleting an expense requires confirmation.
6. Deleting a category must not orphan its expenses.
7. Dashboard totals and cash flow are calculated live from real stored user transactions.
8. Budget remaining amount must update automatically whenever expense data changes.
9. A negative remaining budget indicates the user has exceeded the budget (clamped to ₹0.00 in display).
10. All financial calculations must use exact precision handling.
11. No financial values may be hardcoded in the application.

---

## 13. Non-Functional Requirements

| Category | Requirement |
|---|---|
| **Privacy & Security** | Industry-standard password encryption; secure, protected login sessions; complete user data isolation. |
| **Performance** | Quick dashboard and list loading; adding an expense takes under 30 seconds. |
| **Scalability** | Clean multi-user architecture supporting continuous feature additions. |
| **Reliability** | Safe transactions; no accidental data loss; confirmation before deletions. |
| **Responsiveness** | Seamless experience across mobile phones, tablets, laptops, and desktops. Progressive Web App (PWA) installable. |

---

## 14. Security & Privacy Principles (Non-Technical)

1. **Your Data Is Only Yours:** Spendora is built with strict privacy walls. User A can never see, search, or tamper with User B's spending, income, or budgets.
2. **Passwords Are Never Stored Plainly:** All passwords are transformed using industry-standard mathematical encryption before being saved. No one — not even app administrators — can see your actual password.
3. **One-Click Google Sign-In:** Users can log in using their verified Google identity, avoiding the need to remember additional passwords while keeping their account protected by Google's security.
4. **Safe Login Sessions:** Login credentials are kept in tamper-proof, protected browser storage that cannot be intercepted by external malicious scripts.
5. **Full Device Control:** If you ever log in from a public computer or lose a device, you can instantly log out of all active devices from your account settings.
6. **Spam & Abuse Protection:** Login and password reset attempts are protected against repetitive automated guessing.

---

## 15. Definition of Done

- [ ] User can create a new account with email/password or sign in with Google.
- [ ] User can log in, log out, and log out from all devices.
- [ ] User can reset forgotten passwords safely.
- [ ] Every user's data is 100% private — users only ever see their own expenses, income, budgets, and categories.
- [ ] User can add, view, edit, and delete expenses and incomes.
- [ ] Categories are flexible — users have starter suggestions plus custom categories.
- [ ] Multi-period budgets (daily, weekly, monthly, yearly) alert users when approaching or exceeding limits.
- [ ] Search, date/category/amount/payment-mode filtering, and sorting all work together smoothly.
- [ ] Dashboard shows spending, income, cash flow, recent activity, and visual trend charts.
- [ ] Application is installable as a PWA and responsive across desktop, tablet, and mobile.
- [ ] All data is 100% real and database-backed — zero demo or hardcoded figures.

---

## 16. Future Roadmap

| Phase | Theme | Features |
|---|---|---|
| **Core Product** | Personal Accounts & Finance | Secure accounts (Email/Google), multi-device sessions, full expense & income tracking, cash flow, multi-period budgets, PWA, categories |
| **Next Phase** | Automation & Exports | CSV/PDF/Excel report downloads, recurring transactions (subscriptions, rent), multiple wallets/accounts |
| **Future Phase** | Sharing & Family | Joint household budgeting, roommate expense splitting |
| **Advanced Phase** | Smart Tools | Receipt photo upload, AI-assisted categorization, multi-currency support |

---

## 17. Risks & Mitigation

| Risk | Impact | Mitigation |
|---|---|---|
| Accidental data access between users | Critical | Enforce strict user ownership verification at the database level on every action |
| Forgotten passwords | Medium | Provide instant self-service password reset |
| Poor usability / slow logging | High | Keep expense entry short and simple; target under 30 seconds |
| Data loss | High | Reliable database persistence and safe confirmation on delete |
| Account takeover on shared devices | Medium | Provide "Log out of all devices" and short-lived session refresh |
| Security risk once multi-device/login is introduced | Medium | Test authentication and sync thoroughly before each relevant phase ships |

---

## 18. Dependencies

- Frontend application
- Backend/API layer
- Persistent database
- Charting library (pie/donut + bar/line)
- Form validation
- Appropriate date and currency handling
- Export library for CSV/PDF/Excel (Phase 1 nice-to-have, full support in Phase 2)
- Authentication mechanism (introduced Phase 2 onward)
- Notification system (Phase 5)
- Payment gateway (Phase 6, for premium plans)

Authentication, payment gateways, notification systems, and banking integrations are **not V1 dependencies**.

---

## 19. Assumptions

- Single-user app in V1 — no login needed.
- One fixed currency (INR) — no multi-currency support in V1.
- Budget goal defaults to monthly.
- Expense date must be today or earlier, never future-dated.
- Local or private deployment for V1 (no public internet exposure).

---

## 20. Stakeholders

- Product Owner
- Development Team
- QA/Testing Team
- End Users (primary feedback source for each phase)

---

## 21. Product Architecture Direction

Spendora should be designed as a layered web application:

```text
Frontend
   ↓
Backend API
   ↓
Business Logic
   ↓
Database
```

- **Frontend** — UI, forms, navigation, charts, user interactions.
- **Backend** — CRUD operations, validation, business rules, budget calculations, analytics, API responses.
- **Database** — persistent storage of expenses, categories, and budgets.

Technology stack, database schema, API endpoints, and folder structure should be documented separately in the Software Requirements Specification (SRS).

---

## 22. Final Product Definition

Spendora V1 is a **simple personal expense and budget tracking web application** — not a complete banking or financial management platform. Its purpose is to provide a reliable loop:

> **Record spending → Organize spending → Understand spending → Control spending**

The product should prioritize usability and correctness over feature count. A successful V1 lets a user open Spendora, record an expense within seconds, immediately see the effect on their totals and budget, and later find and analyze that expense easily.

> **Keep the core simple, keep the data real, and make every important number useful.**
