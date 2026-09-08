# Spendora Android Native Application Guide

## 📌 Overview
**Spendora Android** is a native Android financial management application built using **Kotlin**, **Jetpack Compose (Material 3)**, **Coroutines/Flow**, and **Retrofit 2**. It is connected to Spendora's FastAPI backend deployed on Render (`https://spendora-py.onrender.com`) and backed by Supabase PostgreSQL.

---

## 🏛️ Architecture & Project Structure

```
android/app/src/main/java/com/spendora/app/
├── data/
│   ├── api/             # Retrofit REST interfaces (Auth, Expenses, Incomes, Budgets, Goals, AI)
│   ├── local/           # EncryptedSharedPreferences (AES256-GCM SessionManager)
│   ├── model/           # Kotlin Data Transfer Objects & Enum Models
│   ├── remote/          # ApiClient, AuthInterceptor, TokenAuthenticator (auto 401 refresh)
│   └── repository/      # Repository layer with zero-crash Result<T> error encapsulation
├── ui/
│   ├── components/      # Reusable Spendora Material 3 cards, KPI widgets, sheets, and bottom nav
│   │   └── ai/          # Custom Canvas Speedometer Gauge, Spider Radar Chart, AI Modals
│   ├── navigation/      # Screen routes & AppNavigation with session-aware start destination
│   ├── screens/         # Feature screens (Auth, Dashboard, Expenses, Income, Budgets, Goals, AI Assistant)
│   ├── theme/           # Spendora Deep Slate Dark Mode design system (Color, Type, Theme)
│   └── viewmodel/       # AndroidViewModels managing state flows and reactive event buses
├── MainActivity.kt      # Single activity hosting Jetpack Compose NavHost
└── SpendoraApp.kt       # Application class
```

---

## 🚀 How to Build and Run

### 1. Requirements
- **JDK:** Java 17+ (e.g., OpenJDK 17/21)
- **Android SDK:** Platform SDK 34, Build Tools 34.0.0, Platform Tools (ADB)
- **Gradle:** Wrapper included (`gradle-8.4`)

### 2. Build Debug APK
Open a terminal in the `android/` directory and execute:
```bash
# Windows CMD / PowerShell
cd e:\Spendora_py\android
.\gradlew.bat assembleDebug
```
The resulting APK will be generated at:
`android/app/build/outputs/apk/debug/app-debug.apk`

### 3. Install to Physical Device via USB (ADB)
1. Enable **Developer Options** and **USB Debugging** on your phone.
2. Connect your device via USB cable and allow the USB Debugging prompt on the screen.
3. Run:
```bash
.\gradlew.bat installDebug
```

---

## 🤖 Spendora AI Suite on Mobile (All 7 Features)

1. **"Can I Afford This?" Purchase Decision Simulator**
   - Live predictive impact testing before making a purchase.
   - Compares daily safe burn, projected month-end balance, and category limits.
   - Quick sample chips & 1-tap "Add as Expense" shortcut.

2. **Autonomous "Leak Hunter" & Subscription Audit**
   - Scans 90 days of transactions for recurring charges (Netflix, Spotify, Gym, iCloud).
   - Identifies micro-spending leaks (food delivery, coffee under ₹150) and projects annualized drain.

3. **Safe-to-Spend Speedometer Gauge & Burn Forecaster**
   - Custom Jetpack Compose Canvas speedometer arc gauge.
   - Displays real-time daily safe burn allowance and pace percentage (optimal, warning, danger).

4. **Natural Language Conversational Financial Advisor**
   - Dedicated interactive AI chat screen with rich message bubbles.
   - Data-grounded via in-database RAG against your live spending, category balances, and active goals.

5. **Smart Receipt & UPI SMS Parser**
   - One-tap clipboard paste of Indian banking SMS alerts (HDFC, SBI, ICICI, Axis, GPay, PhonePe, Paytm).
   - Automated PII scrubbing and duplicate transaction detection.

6. **AI Financial Health Score & 5-Pillar Spider Radar Chart**
   - Custom Jetpack Compose Canvas radar chart evaluating Savings Discipline, Budget Adherence, Burn Stability, Cash Cushion, and Leak Control into a 0-100 prestige score.
   - Actionable score booster recommendations.

7. **Smart Savings Goals Runway & Pacing Milestones**
   - Interactive goal tracking with deposit/withdraw contribution lifecycles.
   - AI runway forecasting with completion estimates and discretionary cut recommendations.

---

## 🔒 Security & Data Tenancy
- **Authentication:** JWT Access Token (15m) + HttpOnly Refresh Token (30d) with auto-rotation via `TokenAuthenticator`.
- **Encrypted Local Storage:** AES256-GCM encrypted preferences for tokens and user session data.
- **Strict Multi-Tenancy:** Zero data leakage across users guaranteed at the API and repository level.
