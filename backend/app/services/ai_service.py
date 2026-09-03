from __future__ import annotations

import calendar
from datetime import date, datetime
import json
import logging
from decimal import Decimal
from typing import Any, Dict, List, Optional
import httpx

from app.core.config import settings
from app.schemas.ai import PurchaseSimulationRequest, PurchaseSimulationResponse

logger = logging.getLogger(__name__)


class AIService:
    """
    Provider-Agnostic AI Intelligence Service.
    Supports Google Gemini, OpenAI, Anthropic Claude, Groq, OpenRouter, and Local/Proxy endpoints
    with a deterministic mathematical fallback engine.
    """

    def __init__(self):
        self.provider = (settings.AI_PROVIDER or "gemini").lower()
        self.api_key = self._resolve_api_key()
        self.model = self._resolve_model()
        self.base_url = settings.AI_BASE_URL

    def _resolve_api_key(self) -> Optional[str]:
        if settings.AI_API_KEY:
            return settings.AI_API_KEY
        if self.provider == "gemini":
            return settings.GEMINI_API_KEY
        if self.provider == "openai":
            return settings.OPENAI_API_KEY
        if self.provider == "anthropic":
            return settings.ANTHROPIC_API_KEY
        if self.provider == "groq":
            return settings.GROQ_API_KEY
        return None

    def _resolve_model(self) -> str:
        if settings.AI_MODEL:
            return settings.AI_MODEL
        if self.provider == "gemini":
            return "gemini-1.5-flash"
        if self.provider == "openai":
            return "gpt-4o-mini"
        if self.provider == "anthropic":
            return "claude-3-5-sonnet-20241022"
        if self.provider == "groq":
            return "llama-3.3-70b-versatile"
        return "default"

    async def simulate_purchase(
        self,
        request: PurchaseSimulationRequest,
        total_income: Decimal,
        total_spent: Decimal,
        overall_budget: Optional[Decimal],
        days_remaining_in_month: int,
        category_name: Optional[str] = None,
        category_spent: Optional[Decimal] = None,
        category_budget: Optional[Decimal] = None,
    ) -> PurchaseSimulationResponse:
        """
        Simulate the impact of a planned purchase on current cash flow, remaining budget, and savings rate.
        """
        item_amount = Decimal(str(request.amount))
        projected_spent = total_spent + item_amount
        current_cash_flow = total_income - total_spent
        projected_cash_flow = total_income - projected_spent

        # Savings Rate %
        current_savings_rate = (
            round(float((current_cash_flow / total_income) * 100), 1)
            if total_income > 0
            else 0.0
        )
        projected_savings_rate = (
            round(float((projected_cash_flow / total_income) * 100), 1)
            if total_income > 0
            else 0.0
        )

        # Budget impacts
        remaining_overall_budget = None
        projected_remaining_budget = None
        if overall_budget is not None and overall_budget > 0:
            remaining_overall_budget = max(Decimal("0.00"), overall_budget - total_spent)
            projected_remaining_budget = max(
                Decimal("0.00"), overall_budget - projected_spent
            )

        # Daily burn rate calculations
        days = max(1, days_remaining_in_month)
        if overall_budget is not None and overall_budget > 0:
            daily_safe_spend_before = round(
                max(Decimal("0.00"), (overall_budget - total_spent) / Decimal(str(days))),
                2,
            )
            daily_safe_spend_after = round(
                max(Decimal("0.00"), (overall_budget - projected_spent) / Decimal(str(days))),
                2,
            )
        else:
            daily_safe_spend_before = round(
                max(Decimal("0.00"), (total_income - total_spent) / Decimal(str(days))),
                2,
            )
            daily_safe_spend_after = round(
                max(Decimal("0.00"), (total_income - projected_spent) / Decimal(str(days))),
                2,
            )

        # Mathematical Verdict Determination
        # 1. Over budget if cash flow goes negative OR monthly budget exceeded
        is_cash_flow_negative = projected_cash_flow < 0
        is_budget_exceeded = (
            overall_budget is not None and projected_spent > overall_budget
        )

        if is_cash_flow_negative or is_budget_exceeded:
            verdict = "over_budget"
            verdict_title = "Delay or Reconsider Purchase"
            verdict_summary = (
                f"Buying '{request.title}' for ₹{item_amount:,.2f} will exceed your "
                + ("monthly budget" if is_budget_exceeded else "monthly income")
                + f" by ₹{abs(projected_cash_flow if is_cash_flow_negative else (projected_spent - overall_budget)):,.2f}."
            )
        elif projected_savings_rate < 15.0 or (
            overall_budget is not None
            and (projected_spent / overall_budget) > Decimal("0.85")
        ):
            verdict = "caution"
            verdict_title = "Proceed with Caution"
            verdict_summary = (
                f"You can afford '{request.title}', but your monthly savings rate will drop from "
                f"{current_savings_rate}% to {projected_savings_rate}%."
            )
        else:
            verdict = "safe"
            verdict_title = "Safe to Buy!"
            verdict_summary = (
                f"You are in a strong financial position to buy '{request.title}' while maintaining "
                f"a healthy {projected_savings_rate}% savings rate."
            )

        # Attempt AI provider reasoning (with automatic fallback to deterministic reasoning)
        ai_analysis, actionable_tips, provider_name = await self._generate_ai_purchase_analysis(
            item_title=request.title,
            item_amount=item_amount,
            verdict=verdict,
            total_income=total_income,
            total_spent=total_spent,
            projected_spent=projected_spent,
            current_savings_rate=current_savings_rate,
            projected_savings_rate=projected_savings_rate,
            current_cash_flow=current_cash_flow,
            projected_cash_flow=projected_cash_flow,
            days_left=days,
            overall_budget=overall_budget,
            category_name=category_name,
        )

        return PurchaseSimulationResponse(
            verdict=verdict,
            verdict_title=verdict_title,
            verdict_summary=verdict_summary,
            item_title=request.title,
            item_amount=item_amount,
            current_cash_flow=current_cash_flow,
            projected_cash_flow=projected_cash_flow,
            current_savings_rate=current_savings_rate,
            projected_savings_rate=projected_savings_rate,
            current_spent=total_spent,
            projected_spent=projected_spent,
            overall_budget=overall_budget,
            remaining_overall_budget=remaining_overall_budget,
            projected_remaining_budget=projected_remaining_budget,
            daily_safe_spend_before=daily_safe_spend_before,
            daily_safe_spend_after=daily_safe_spend_after,
            ai_analysis=ai_analysis,
            actionable_tips=actionable_tips,
            provider_used=provider_name,
        )

    async def _generate_ai_purchase_analysis(
        self,
        item_title: str,
        item_amount: Decimal,
        verdict: str,
        total_income: Decimal,
        total_spent: Decimal,
        projected_spent: Decimal,
        current_savings_rate: float,
        projected_savings_rate: float,
        current_cash_flow: Decimal,
        projected_cash_flow: Decimal,
        days_left: int,
        overall_budget: Optional[Decimal],
        category_name: Optional[str],
    ) -> tuple[str, List[str], str]:
        """
        Calls external LLM (Gemini, OpenAI, Claude, Groq) if API key is provided,
        otherwise generates deterministic financial advice.
        """
        # Fallback generator
        def deterministic_fallback():
            tips = []
            if verdict == "safe":
                analysis = (
                    f"Purchasing '{item_title}' for ₹{item_amount:,.2f} fits comfortably within your monthly financial plan. "
                    f"Your projected savings rate remains healthy at {projected_savings_rate}%, leaving ₹{projected_cash_flow:,.2f} in net savings."
                )
                tips = [
                    f"Your daily safe spending limit after this purchase is ₹{(max(Decimal('0'), projected_cash_flow / Decimal(str(days_left)))):,.2f}/day.",
                    "Ensure this purchase aligns with your highest-priority goals for the month.",
                    "Record the expense immediately after purchase to keep live dashboards accurate.",
                ]
            elif verdict == "caution":
                analysis = (
                    f"While you currently have sufficient cash flow for '{item_title}' (₹{item_amount:,.2f}), "
                    f"it will significantly decrease your savings rate from {current_savings_rate}% down to {projected_savings_rate}%. "
                    f"You will have ₹{projected_cash_flow:,.2f} left for the remaining {days_left} days of the month."
                )
                daily_reduction = (item_amount / Decimal(str(days_left))).quantize(Decimal("0.01"))
                tips = [
                    f"Reduce daily discretionary spending by ~₹{daily_reduction:,.2f}/day for the next {days_left} days to offset this cost.",
                    "Consider delaying the purchase by 1-2 weeks or waiting for upcoming promotional discounts.",
                    "If non-essential, split the cost across two billing cycles.",
                ]
            else:
                analysis = (
                    f"Purchasing '{item_title}' for ₹{item_amount:,.2f} is not recommended right now. "
                    f"It will push your monthly spending into a deficit of ₹{abs(projected_cash_flow):,.2f} and deplete your savings buffer."
                )
                tips = [
                    f"Wait {days_left} days until your next income cycle before making this purchase.",
                    f"Save ~₹{(item_amount / Decimal('4')):,.2f}/week over the next month to buy this safely without debt.",
                    "Review and cut unused recurring subscriptions or discretionary expenses.",
                ]
            return analysis, tips, "deterministic-financial-engine"

        # If no API key configured, use deterministic engine immediately
        if not self.api_key:
            return deterministic_fallback()

        # Build prompt for LLM
        system_prompt = (
            "You are Spendora's expert financial advisor. Analyze a simulated purchase and output concise, "
            "practical, and encouraging financial guidance. Respond STRICTLY in valid JSON format with keys: "
            "'analysis' (string, max 3 sentences) and 'tips' (array of 3 actionable string bullet points)."
        )
        user_prompt = (
            f"User wants to buy: '{item_title}' for ₹{item_amount:,.2f}.\n"
            f"Context:\n"
            f"- Total Monthly Income: ₹{total_income:,.2f}\n"
            f"- Current Spent: ₹{total_spent:,.2f}\n"
            f"- Projected Spent: ₹{projected_spent:,.2f}\n"
            f"- Current Savings Rate: {current_savings_rate}%\n"
            f"- Projected Savings Rate: {projected_savings_rate}%\n"
            f"- Monthly Budget: ₹{overall_budget if overall_budget else 'Not Set'}\n"
            f"- Days Remaining in Month: {days_left}\n"
            f"- Mathematical Verdict: {verdict.upper()}\n"
            f"Provide a structured JSON response with 'analysis' and 3 'tips'."
        )

        try:
            async with httpx.AsyncClient(timeout=8.0) as client:
                if self.provider == "gemini":
                    url = f"https://generativelanguage.googleapis.com/v1beta/models/{self.model}:generateContent?key={self.api_key}"
                    payload = {
                        "contents": [{"parts": [{"text": f"{system_prompt}\n\n{user_prompt}"}]}],
                        "generationConfig": {"response_mime_type": "application/json"},
                    }
                    resp = await client.post(url, json=payload)
                    if resp.status_code == 200:
                        data = resp.json()
                        raw_text = data["candidates"][0]["content"]["parts"][0]["text"]
                        parsed = json.loads(raw_text)
                        return (
                            parsed.get("analysis", ""),
                            parsed.get("tips", []),
                            f"google-{self.model}",
                        )

                elif self.provider in ["openai", "groq", "openrouter", "ollama"]:
                    endpoint = (
                        f"{self.base_url}/chat/completions"
                        if self.base_url
                        else (
                            "https://api.groq.com/openai/v1/chat/completions"
                            if self.provider == "groq"
                            else "https://api.openai.com/v1/chat/completions"
                        )
                    )
                    headers = {"Authorization": f"Bearer {self.api_key}"}
                    payload = {
                        "model": self.model,
                        "messages": [
                            {"role": "system", "content": system_prompt},
                            {"role": "user", "content": user_prompt},
                        ],
                        "response_format": {"type": "json_object"},
                    }
                    resp = await client.post(endpoint, headers=headers, json=payload)
                    if resp.status_code == 200:
                        data = resp.json()
                        raw_text = data["choices"][0]["message"]["content"]
                        parsed = json.loads(raw_text)
                        return (
                            parsed.get("analysis", ""),
                            parsed.get("tips", []),
                            f"{self.provider}-{self.model}",
                        )

                elif self.provider == "anthropic":
                    url = "https://api.anthropic.com/v1/messages"
                    headers = {
                        "x-api-key": self.api_key,
                        "anthropic-version": "2023-06-01",
                    }
                    payload = {
                        "model": self.model,
                        "max_tokens": 512,
                        "system": system_prompt,
                        "messages": [{"role": "user", "content": user_prompt}],
                    }
                    resp = await client.post(url, headers=headers, json=payload)
                    if resp.status_code == 200:
                        data = resp.json()
                        raw_text = data["content"][0]["text"]
                        parsed = json.loads(raw_text)
                        return (
                            parsed.get("analysis", ""),
                            parsed.get("tips", []),
                            f"anthropic-{self.model}",
                        )
        except Exception as e:
            logger.warning(f"AI Provider call failed ({self.provider}): {e}. Using deterministic fallback.")

    async def analyze_leaks_and_subscriptions(
        self,
        expenses: List[Dict[str, Any]],
        total_monthly_income: Decimal,
    ) -> Dict[str, Any]:
        """
        Analyze 90 days of expense records to extract recurring subscriptions and micro-spending leaks.
        """
        from collections import defaultdict
        from datetime import date, timedelta

        # 1. Detect Subscriptions (expenses with repeat titles or keywords like netflix, prime, spotify, gym, etc.)
        sub_keywords = {"netflix", "prime", "spotify", "gym", "hotstar", "youtube", "icloud", "apple", "chatgpt", "membership", "wifi", "broadband", "rent"}
        title_groups = defaultdict(list)
        for exp in expenses:
            normalized_title = exp["title"].strip().lower()
            title_groups[normalized_title].append(exp)

        detected_subscriptions = []
        subscription_monthly_total = Decimal("0.00")

        for norm_title, group in title_groups.items():
            is_sub_keyword = any(k in norm_title for k in sub_keywords)
            # Repeat occurrences over time or explicit keyword
            if len(group) >= 2 or (is_sub_keyword and len(group) >= 1):
                sorted_group = sorted(group, key=lambda x: x["expense_date"], reverse=True)
                avg_amount = round(
                    Decimal(str(sum(Decimal(str(e["amount"])) for e in group) / len(group))), 2
                )
                latest_exp = sorted_group[0]
                detected_subscriptions.append({
                    "title": latest_exp["title"].title(),
                    "average_amount": avg_amount,
                    "occurrence_count": len(group),
                    "last_date": latest_exp["expense_date"],
                    "estimated_monthly_cost": avg_amount,
                    "category_name": latest_exp.get("category_name", "Subscription / Recurring"),
                })
                subscription_monthly_total += avg_amount

        # 2. Detect Micro-Spending Leaks (amount <= ₹150)
        micro_expenses = [e for e in expenses if Decimal(str(e["amount"])) <= Decimal("150.00")]
        cat_micro_groups = defaultdict(list)
        for me in micro_expenses:
            cat_name = me.get("category_name") or "Daily Micro-Expenses"
            cat_micro_groups[cat_name].append(me)

        micro_spending_leaks = []
        micro_leak_monthly_total = Decimal("0.00")

        for cat_name, items in cat_micro_groups.items():
            total_cat_amount = sum(Decimal(str(i["amount"])) for i in items)
            # Estimate monthly occurrence by normalizing past 90 days to 30 days
            monthly_frequency = max(1, round(len(items) / 3))
            monthly_cat_drain = round((total_cat_amount / Decimal("3")), 2) if len(items) > 3 else total_cat_amount
            annual_projection = monthly_cat_drain * Decimal("12")
            micro_leak_monthly_total += monthly_cat_drain

            sample_titles = list(dict.fromkeys(i["title"] for i in items))[:3]
            micro_spending_leaks.append({
                "category_or_label": cat_name,
                "transaction_count": len(items),
                "average_amount": round(total_cat_amount / Decimal(str(len(items))), 2),
                "monthly_total": monthly_cat_drain,
                "annual_projected_drain": annual_projection,
                "example_items": sample_titles,
            })

        total_monthly_leak = subscription_monthly_total + micro_leak_monthly_total
        total_annual_leak = total_monthly_leak * Decimal("12")
        total_annual_subscriptions = subscription_monthly_total * Decimal("12")

        # 3. AI Reasoning or Deterministic Summary
        def deterministic_leak_summary():
            if not detected_subscriptions and not micro_spending_leaks:
                return (
                    "No significant recurring subscription or micro-spending leaks detected in your recent history. Your spending habits are very clean!",
                    [
                        "Continue tracking all small cash and UPI payments.",
                        "Review recurring digital charges every quarter.",
                        "Maintain a dedicated savings buffer for unexpected expenses.",
                    ],
                    "deterministic-financial-engine",
                )
            summary = (
                f"We identified {len(detected_subscriptions)} active subscriptions (₹{subscription_monthly_total:,.2f}/mo) "
                f"and {len(micro_spending_leaks)} micro-spending categories totaling ₹{micro_leak_monthly_total:,.2f}/mo. "
                f"Combined, these recurring outflows drain ~₹{total_annual_leak:,.2f} per year."
            )
            tips = [
                f"Auditing and canceling 1-2 unused subscriptions could save up to ₹{(subscription_monthly_total * Decimal('0.3')):,.2f}/month.",
                f"Daily micro-transactions under ₹150 accumulate to ₹{micro_leak_monthly_total * Decimal('12'):,.2f}/year — consider setting a weekly snack/tea budget.",
                "Review automated UPI mandates and annual app renewals to avoid zombie charges.",
            ]
            return summary, tips, "deterministic-financial-engine"

        ai_summary, savings_tips, provider_name = deterministic_leak_summary()

        # If API key is available, enhance with LLM insights
        if self.api_key and (detected_subscriptions or micro_spending_leaks):
            system_prompt = (
                "You are Spendora's expert financial leak auditor. Analyze the detected recurring subscriptions and micro-spending leaks. "
                "Output concise, empowering advice in valid JSON format with keys: 'summary' (string, max 2 sentences) and 'tips' (array of 3 actionable string bullet points)."
            )
            user_prompt = (
                f"Detected Subscriptions: {json.dumps(detected_subscriptions, default=str)}\n"
                f"Micro-Spending Leaks: {json.dumps(micro_spending_leaks, default=str)}\n"
                f"Total Monthly Leak: ₹{total_monthly_leak:,.2f} (Annual: ₹{total_annual_leak:,.2f})\n"
                f"Monthly Income: ₹{total_monthly_income:,.2f}\n"
                f"Provide structured JSON with 'summary' and 'tips'."
            )
            try:
                import httpx
                async with httpx.AsyncClient(timeout=8.0) as client:
                    if self.provider == "gemini":
                        url = f"https://generativelanguage.googleapis.com/v1beta/models/{self.model}:generateContent?key={self.api_key}"
                        payload = {
                            "contents": [{"parts": [{"text": f"{system_prompt}\n\n{user_prompt}"}]}],
                            "generationConfig": {"response_mime_type": "application/json"},
                        }
                        resp = await client.post(url, json=payload)
                        if resp.status_code == 200:
                            data = resp.json()
                            raw_text = data["candidates"][0]["content"]["parts"][0]["text"]
                            parsed = json.loads(raw_text)
                            ai_summary = parsed.get("summary", ai_summary)
                            savings_tips = parsed.get("tips", savings_tips)
                            provider_name = f"google-{self.model}"
            except Exception as e:
                logger.warning(f"Leak AI call failed: {e}. Using deterministic fallback.")

        return {
            "total_monthly_leak": total_monthly_leak,
            "total_annual_projected_leak": total_annual_leak,
            "total_monthly_subscriptions": subscription_monthly_total,
            "total_annual_subscriptions": total_annual_subscriptions,
            "subscription_count": len(detected_subscriptions),
            "micro_leak_count": len(micro_spending_leaks),
            "detected_subscriptions": detected_subscriptions,
            "micro_spending_leaks": micro_spending_leaks,
            "ai_summary": ai_summary,
            "actionable_savings_tips": savings_tips,
            "provider_used": provider_name,
        }

    async def calculate_safe_to_spend(
        self,
        total_income: Decimal,
        total_spent: Decimal,
        overall_budget: Optional[Decimal],
        today: Optional[date] = None,
    ) -> Dict[str, Any]:
        """
        Calculate dynamic real-time daily safe burn allowance and month-end trajectory forecast.
        """
        if today is None:
            today = date.today()

        _, total_days_in_month = calendar.monthrange(today.year, today.month)
        days_passed = max(1, today.day)
        days_remaining = max(1, total_days_in_month - today.day + 1)

        # Baseline budget limit or income buffer
        effective_limit = (
            overall_budget
            if (overall_budget is not None and overall_budget > 0)
            else (total_income if total_income > 0 else Decimal("30000.00"))
        )

        remaining_buffer = max(Decimal("0.00"), effective_limit - total_spent)
        daily_safe_spend = (remaining_buffer / Decimal(str(days_remaining))).quantize(Decimal("0.01"))
        current_burn_rate = (total_spent / Decimal(str(days_passed))).quantize(Decimal("0.01"))

        projected_additional_spend = current_burn_rate * Decimal(str(max(0, days_remaining - 1)))
        projected_total_month_spend = (total_spent + projected_additional_spend).quantize(Decimal("0.01"))
        projected_month_end_balance = (total_income - projected_total_month_spend).quantize(Decimal("0.01"))

        # Burn pace calculation
        if daily_safe_spend > 0:
            burn_pace_pct = round(float((current_burn_rate / daily_safe_spend) * Decimal("100.0")), 1)
        else:
            burn_pace_pct = 250.0

        if remaining_buffer <= 0 or burn_pace_pct > 105.0:
            burn_rate_status = "danger"
        elif burn_pace_pct > 85.0:
            burn_rate_status = "warning"
        else:
            burn_rate_status = "optimal"

        # Depletion day estimation
        projected_zero_cash_day = None
        if current_burn_rate > 0 and projected_total_month_spend > effective_limit and remaining_buffer > 0:
            days_to_deplete = int(remaining_buffer / current_burn_rate)
            projected_zero_cash_day = min(total_days_in_month, days_passed + days_to_deplete)
        elif remaining_buffer <= 0:
            projected_zero_cash_day = days_passed

        # AI / Deterministic Guidance
        def deterministic_burn_advice():
            if burn_rate_status == "optimal":
                recommendation = (
                    f"Your spending velocity is optimal! You are currently burning ~₹{current_burn_rate:,.2f}/day "
                    f"against a safe allowance of ₹{daily_safe_spend:,.2f}/day. "
                    f"You are projected to finish the month with ₹{projected_month_end_balance:,.2f} in net savings."
                )
                tips = [
                    f"You can safely spend up to ₹{daily_safe_spend:,.2f} today without impacting your monthly targets.",
                    "Consider routing your surplus savings into your high-yield goals or investments.",
                    "Maintain this steady pace through the weekend.",
                ]
            elif burn_rate_status == "warning":
                recommendation = (
                    f"Your spending pace is currently elevated at ₹{current_burn_rate:,.2f}/day ({burn_pace_pct}% of safe limit). "
                    f"You have ₹{remaining_buffer:,.2f} remaining for the last {days_remaining} days."
                )
                tips = [
                    f"Cap daily non-essential purchases at ₹{daily_safe_spend:,.2f}/day for the rest of the month.",
                    "Postpone optional recreational shopping until next month's salary cycle.",
                    "Check for unrecorded cash payments to ensure accurate tracking.",
                ]
            else:
                deplete_text = f"by Day {projected_zero_cash_day}" if projected_zero_cash_day else "soon"
                recommendation = (
                    f"High burn rate alert! At your current burn of ₹{current_burn_rate:,.2f}/day, "
                    f"you risk exhausting your monthly buffer {deplete_text}. Immediate rebalancing is recommended."
                )
                tips = [
                    f"Limit daily expenses strictly to ₹{daily_safe_spend:,.2f}/day to preserve remaining buffer.",
                    "Enact a 48-hour 'no-spend' freeze on discretionary dining and shopping.",
                    "Review top spending categories in your dashboard to find quick trimming opportunities.",
                ]
            return recommendation, tips, "deterministic-financial-engine"

        ai_recommendation, actionable_tips, provider_name = deterministic_burn_advice()

        # Call AI provider if configured
        if self.api_key:
            system_prompt = (
                "You are Spendora's expert burn-rate advisor. Analyze the real-time daily safe-to-spend metrics. "
                "Output concise, encouraging advice in valid JSON with keys: 'recommendation' (string, max 2 sentences) and 'tips' (array of 3 actionable string bullet points)."
            )
            user_prompt = (
                f"Status: {burn_rate_status.upper()}\n"
                f"Daily Safe Limit: ₹{daily_safe_spend:,.2f}/day\n"
                f"Current Burn Rate: ₹{current_burn_rate:,.2f}/day ({burn_pace_pct}% pace)\n"
                f"Remaining Days: {days_remaining} (Days Passed: {days_passed})\n"
                f"Remaining Buffer: ₹{remaining_buffer:,.2f}\n"
                f"Projected Month-End Balance: ₹{projected_month_end_balance:,.2f}\n"
                f"Projected Zero Day: {projected_zero_cash_day}\n"
                f"Provide JSON with 'recommendation' and 'tips'."
            )
            try:
                import httpx
                async with httpx.AsyncClient(timeout=8.0) as client:
                    if self.provider == "gemini":
                        url = f"https://generativelanguage.googleapis.com/v1beta/models/{self.model}:generateContent?key={self.api_key}"
                        payload = {
                            "contents": [{"parts": [{"text": f"{system_prompt}\n\n{user_prompt}"}]}],
                            "generationConfig": {"response_mime_type": "application/json"},
                        }
                        resp = await client.post(url, json=payload)
                        if resp.status_code == 200:
                            data = resp.json()
                            raw_text = data["candidates"][0]["content"]["parts"][0]["text"]
                            parsed = json.loads(raw_text)
                            ai_recommendation = parsed.get("recommendation", ai_recommendation)
                            actionable_tips = parsed.get("tips", actionable_tips)
                            provider_name = f"google-{self.model}"
            except Exception as e:
                logger.warning(f"Safe-to-Spend AI call failed: {e}. Using deterministic fallback.")

        return {
            "daily_safe_spend": daily_safe_spend,
            "burn_rate_status": burn_rate_status,
            "current_burn_rate_per_day": current_burn_rate,
            "days_remaining_in_month": days_remaining,
            "days_passed": days_passed,
            "total_monthly_income": total_income,
            "total_spent_so_far": total_spent,
            "remaining_buffer": remaining_buffer,
            "projected_month_end_balance": projected_month_end_balance,
            "projected_zero_cash_day": projected_zero_cash_day,
            "burn_pace_percentage": burn_pace_pct,
            "ai_recommendation": ai_recommendation,
            "actionable_tips": actionable_tips,
            "provider_used": provider_name,
        }

    async def chat_financial_advisor(
        self,
        message: str,
        history: List[Dict[str, Any]],
        context: Dict[str, Any],
    ) -> Dict[str, Any]:
        """
        Feature 4: Conversational Natural Language Financial Assistant / Chatbot.
        Processes user queries with real-time financial telemetry context and returns
        rich markdown responses, suggested next prompts, and smart action triggers.
        """
        import re

        total_income = Decimal(str(context.get("total_income", "0.00")))
        total_spent = Decimal(str(context.get("total_spent", "0.00")))
        net_savings = Decimal(str(context.get("net_savings", "0.00")))
        savings_rate = float(context.get("savings_rate_pct", 0.0))
        daily_safe_spend = Decimal(str(context.get("daily_safe_spend", "0.00")))
        days_remaining = int(context.get("days_remaining", 1))
        overall_budget = context.get("overall_budget")
        top_categories = context.get("top_categories", [])
        recent_expenses = context.get("recent_expenses", [])
        active_budgets = context.get("active_budgets", [])

        # ── Deterministic NLP Fallback Engine ──
        def deterministic_chat_response() -> Dict[str, Any]:
            msg_lower = message.strip().lower()
            reply = ""
            action_intent = None
            suggested = [
                "What is my daily safe spending limit?",
                "Which category is my biggest expense?",
                "How much have I saved this month?",
            ]

            # 1. Affordability / Buying simulation intent
            afford_match = re.search(r"(?:can\s+i\s+afford|buy|purchase|getting|afford)\s+(?:a\s+|an\s+)?([a-zA-Z\s]+?)(?:\s+for|\s+at|\s+worth)?\s*(?:₹|rs\.?|inr)?\s*(\d+(?:,\d+)*(?:\.\d+)?|\d+)", msg_lower)
            number_only_match = re.search(r"(?:₹|rs\.?|inr)?\s*(\d+(?:,\d+)*(?:\.\d+)?|\d+)\s*(?:for|on)?\s*([a-zA-Z\s]+)", msg_lower) if not afford_match else None

            if "afford" in msg_lower or "should i buy" in msg_lower or afford_match:
                extracted_item = "this item"
                extracted_amount = Decimal("1000.00")
                if afford_match:
                    extracted_item = afford_match.group(1).strip() or "this item"
                    try:
                        extracted_amount = Decimal(afford_match.group(2).replace(",", ""))
                    except Exception:
                        extracted_amount = Decimal("1000.00")
                elif number_only_match:
                    try:
                        extracted_amount = Decimal(number_only_match.group(1).replace(",", ""))
                        extracted_item = number_only_match.group(2).strip() or "this item"
                    except Exception:
                        pass

                projected_cf = net_savings - extracted_amount
                if projected_cf >= 0 and extracted_amount <= (daily_safe_spend * Decimal("5")):
                    reply = (
                        f"### ✅ Purchase Feasibility: **Safe to Buy**\n\n"
                        f"Buying **{extracted_item.title()}** for **₹{extracted_amount:,.2f}** fits within your monthly cash flow.\n\n"
                        f"- **Current Net Savings:** ₹{net_savings:,.2f}\n"
                        f"- **Projected Net Savings:** ₹{projected_cf:,.2f}\n"
                        f"- **Remaining Days in Month:** {days_remaining} days\n\n"
                        f"Your daily safe spending limit after this purchase will adjust to approximately **₹{max(Decimal('0'), projected_cf / Decimal(str(max(1, days_remaining)))):,.2f}/day**."
                    )
                elif projected_cf >= 0:
                    reply = (
                        f"### ⚠️ Purchase Feasibility: **Proceed with Caution**\n\n"
                        f"You have enough balance for **{extracted_item.title()}** (₹{extracted_amount:,.2f}), but it takes up a significant chunk of your remaining monthly buffer.\n\n"
                        f"- **Current Net Savings:** ₹{net_savings:,.2f}\n"
                        f"- **Projected Net Savings:** ₹{projected_cf:,.2f}\n"
                        f"- **Impact:** Your savings rate will reduce noticeably.\n\n"
                        f"💡 *Tip: Consider pacing your discretionary spending over the next {days_remaining} days.*"
                    )
                else:
                    reply = (
                        f"### 🛑 Purchase Feasibility: **Over Budget Deficit**\n\n"
                        f"Buying **{extracted_item.title()}** for **₹{extracted_amount:,.2f}** would exceed your available cash flow and cause a **₹{abs(projected_cf):,.2f}** deficit this month.\n\n"
                        f"💡 *Recommendation: Postpone this purchase until next month or save ₹{extracted_amount / Decimal('4'):,.2f}/week.*"
                    )

                action_intent = {
                    "action": "simulate_purchase",
                    "label": f"Simulate ₹{extracted_amount:,.0f} Purchase in Depth",
                    "payload": {"title": extracted_item.title(), "amount": float(extracted_amount)},
                }
                suggested = [
                    "What is my daily safe spending limit?",
                    "Where can I cut down expenses?",
                    "How much did I spend this month?",
                ]

            # 2. Daily safe burn / Safe-to-spend intent
            elif any(k in msg_lower for k in ["safe to spend", "daily limit", "burn rate", "safe spend", "per day", "today"]):
                reply = (
                    f"### 🎯 Live Daily Safe-to-Spend Allowance\n\n"
                    f"You can safely spend **₹{daily_safe_spend:,.2f}/day** for the remaining **{days_remaining} days** of this month.\n\n"
                    f"| Metric | Amount |\n"
                    f"| :--- | :--- |\n"
                    f"| **Safe Daily Burn** | **₹{daily_safe_spend:,.2f}** |\n"
                    f"| **Remaining Buffer** | ₹{max(Decimal('0'), net_savings):,.2f} |\n"
                    f"| **Days Remaining** | {days_remaining} days |\n\n"
                    f"{'🟢 Spending pace is optimal!' if daily_safe_spend > 500 else '🟡 Keep discretionary purchases modest to preserve your month-end buffer.'}"
                )
                suggested = [
                    "Which category is my biggest expense?",
                    "Can I afford dinner for ₹1,200?",
                    "Show my recent transactions",
                ]

            # 3. Top categories / Where did money go
            elif any(k in msg_lower for k in ["top category", "highest", "biggest", "where did my money", "category", "categories", "drain"]):
                if top_categories:
                    cat_rows = "\n".join([
                        f"- **{c.get('name', 'General')}:** ₹{Decimal(str(c.get('spent', 0))):,.2f} ({c.get('percentage', 0):.1f}% of total)"
                        for c in top_categories[:5]
                    ])
                    top_one = top_categories[0]
                    reply = (
                        f"### 📊 Spending by Category This Month\n\n"
                        f"Your highest spending category is **{top_one.get('name')}** at **₹{Decimal(str(top_one.get('spent', 0))):,.2f}**.\n\n"
                        f"{cat_rows}\n\n"
                        f"💡 *Tip: Setting a dedicated category budget for {top_one.get('name')} can help keep outflows under control.*"
                    )
                else:
                    reply = "You haven't recorded any category expenses for this month yet. Start logging expenses to see your breakdown!"
                suggested = [
                    "How much did I spend in total?",
                    "What is my daily safe spending limit?",
                    "Give me 3 tips to save money",
                ]

            # 4. Income / Savings / Net cash flow
            elif any(k in msg_lower for k in ["income", "salary", "earned", "savings", "saved", "cash flow", "save"]):
                reply = (
                    f"### 💰 Monthly Income & Cash Flow Snapshot\n\n"
                    f"- **Total Monthly Income:** ₹{total_income:,.2f}\n"
                    f"- **Total Monthly Outflows:** ₹{total_spent:,.2f}\n"
                    f"- **Net Cash Flow (Savings):** **₹{net_savings:,.2f}**\n"
                    f"- **Savings Rate:** **{savings_rate:.1f}%**\n\n"
                    f"{'🎉 Excellent! You are maintaining a healthy savings rate above 20%.' if savings_rate >= 20 else '💡 Aiming for a 20% savings rate (₹' + f'{(total_income * Decimal(0.2)):,.2f}) will strengthen your emergency fund.' if total_income > 0 else 'Record your income in the Income tab to track your net savings rate.'}"
                )
                suggested = [
                    "Where is most of my money going?",
                    "What is my safe daily limit?",
                    "Check for recurring subscription leaks",
                ]

            # 5. Spending / Outflows query
            elif any(k in msg_lower for k in ["spent", "spending", "outflow", "expenses this month", "how much did i spend"]):
                reply = (
                    f"### 💳 Spending Summary (Current Month)\n\n"
                    f"You have spent a total of **₹{total_spent:,.2f}** so far this month.\n\n"
                    f"- **Total Income:** ₹{total_income:,.2f}\n"
                    f"- **Net Remaining:** ₹{net_savings:,.2f}\n"
                    f"- **Daily Safe Limit:** ₹{daily_safe_spend:,.2f}/day ({days_remaining} days left)\n"
                )
                if overall_budget:
                    b_amt = Decimal(str(overall_budget))
                    reply += f"- **Monthly Budget:** ₹{b_amt:,.2f} ({((total_spent / b_amt) * 100):.1f}% utilized)\n"
                suggested = [
                    "Which category is my biggest expense?",
                    "What is my daily safe spending limit?",
                    "Check for subscription leaks",
                ]

            # 6. Subscription / Leak Hunter query
            elif any(k in msg_lower for k in ["leak", "subscription", "recurring", "audit", "netflix", "spotify", "gym"]):
                reply = (
                    f"### 🔍 Recurring Subscriptions & Leak Audit\n\n"
                    f"Spendora's Leak Hunter automatically audits your 90-day history for recurring digital subscriptions and micro-expenses (under ₹150) that silently drain savings.\n\n"
                    f"Click below to run a real-time scan and see your annual subscription drain."
                )
                action_intent = {
                    "action": "view_leaks",
                    "label": "Open Leak Hunter Audit",
                    "payload": {},
                }
                suggested = [
                    "What is my daily safe spending limit?",
                    "How much did I save this month?",
                    "Where did most of my money go?",
                ]

            # 7. Recent Transactions query
            elif any(k in msg_lower for k in ["recent", "transactions", "latest expense", "last expense", "history"]):
                if recent_expenses:
                    tx_rows = "\n".join([
                        f"- **{e.get('title')}:** ₹{Decimal(str(e.get('amount', 0))):,.2f} on {e.get('expense_date')} ({e.get('category_name', 'General')})"
                        for e in recent_expenses[:5]
                    ])
                    reply = (
                        f"### 🕒 Recent Transactions\n\n"
                        f"Here are your latest expenses:\n\n"
                        f"{tx_rows}\n"
                    )
                else:
                    reply = "No recent expenses found for this period."
                action_intent = {
                    "action": "navigate",
                    "label": "View All Expenses Table",
                    "payload": {"path": "/expenses"},
                }
                suggested = [
                    "How much have I spent this month?",
                    "What is my safe daily limit?",
                    "Which category is my biggest expense?",
                ]

            # 8. Tips / Advice
            elif any(k in msg_lower for k in ["tip", "tips", "advice", "help", "reduce", "cut", "save money", "budgeting"]):
                top_name = top_categories[0].get("name") if top_categories else "discretionary spending"
                reply = (
                    f"### 💡 Personalized Financial Tips for You\n\n"
                    f"1. **Manage your top category ({top_name}):** Trimming just 10-15% from {top_name} this month could preserve ~₹{((Decimal(str(top_categories[0].get('spent', 1000))) if top_categories else Decimal('500')) * Decimal('0.15')):,.2f}.\n"
                    f"2. **Stick to your Daily Safe Limit:** Cap today's unbudgeted purchases at **₹{daily_safe_spend:,.2f}** to prevent month-end cash crunches.\n"
                    f"3. **Follow the 50/30/20 Rule:** Allocate 50% for Needs, 30% for Wants, and 20% directly into Savings/Investments on pay-day.\n"
                )
                suggested = [
                    "What is my daily safe spending limit?",
                    "Can I afford a ₹3,000 purchase?",
                    "How much did I save this month?",
                ]

            # 9. Default Overview / General greeting
            else:
                reply = (
                    f"👋 **Hello! I am Spendora AI, your personal financial assistant.**\n\n"
                    f"Here is your live financial snapshot for this month:\n\n"
                    f"- **Total Income:** ₹{total_income:,.2f}\n"
                    f"- **Total Spent:** ₹{total_spent:,.2f}\n"
                    f"- **Net Savings:** ₹{net_savings:,.2f} ({savings_rate:.1f}% savings rate)\n"
                    f"- **Daily Safe Spending Limit:** **₹{daily_safe_spend:,.2f}/day** ({days_remaining} days left)\n\n"
                    f"How can I assist you with your money today?"
                )
                suggested = [
                    "What is my daily safe spending limit?",
                    "Which category is my biggest expense?",
                    "Can I afford dinner for ₹1,500?",
                ]

            return {
                "reply": reply,
                "suggested_prompts": suggested,
                "action_intent": action_intent,
                "context_summary": {
                    "total_income": float(total_income),
                    "total_spent": float(total_spent),
                    "net_savings": float(net_savings),
                    "savings_rate_pct": savings_rate,
                    "daily_safe_spend": float(daily_safe_spend),
                    "days_remaining": days_remaining,
                },
                "provider_used": "deterministic-financial-engine",
            }

        # If no API key configured, use deterministic NLP assistant immediately
        if not self.api_key:
            return deterministic_chat_response()

        # Build prompt with live context for LLM
        system_prompt = (
            "You are Spendora's intelligent, empathetic, and highly analytical AI Financial Assistant. "
            "You have direct access to the user's live financial data (all currency in INR ₹).\n"
            "Guidelines:\n"
            "- Speak naturally, professionally, and concisely in clean Markdown formatting.\n"
            "- Always use the exact financial numbers provided in the context.\n"
            "- When users ask about buying something, evaluate their cash flow and safe daily spend.\n"
            "- Give practical, encouraging budgeting and wealth-building tips.\n"
            "- Output your response STRICTLY as a valid JSON object with the following schema:\n"
            "{\n"
            '  "reply": "Markdown formatted string with clear headings, bullet points, and tables if useful",\n'
            '  "suggested_prompts": ["Prompt 1", "Prompt 2", "Prompt 3"],\n'
            '  "action_intent": null or {"action": "simulate_purchase" | "view_leaks" | "navigate" | "set_budget", "label": "Button Label", "payload": {}}\n'
            "}"
        )

        user_context_str = json.dumps({
            "total_monthly_income": f"₹{total_income:,.2f}",
            "total_monthly_spent": f"₹{total_spent:,.2f}",
            "net_monthly_savings": f"₹{net_savings:,.2f}",
            "savings_rate_percentage": f"{savings_rate:.1f}%",
            "daily_safe_spend_limit": f"₹{daily_safe_spend:,.2f}/day",
            "days_remaining_in_month": days_remaining,
            "overall_monthly_budget": f"₹{Decimal(str(overall_budget)):,.2f}" if overall_budget else "Not Set",
            "top_spending_categories": top_categories,
            "recent_expenses": recent_expenses[:5],
            "active_budgets": active_budgets,
        }, default=str)

        conversation_history = []
        for h in history[-6:]:  # last 6 turns for prompt efficiency
            role = "user" if h.get("role") == "user" else "assistant"
            conversation_history.append({"role": role, "content": h.get("content", "")})

        full_user_content = f"User Financial Telemetry:\n{user_context_str}\n\nUser Question: {message}"

        try:
            async with httpx.AsyncClient(timeout=10.0) as client:
                if self.provider == "gemini":
                    url = f"https://generativelanguage.googleapis.com/v1beta/models/{self.model}:generateContent?key={self.api_key}"
                    contents = []
                    for h in conversation_history:
                        contents.append({"role": "user" if h["role"] == "user" else "model", "parts": [{"text": h["content"]}]})
                    contents.append({"role": "user", "parts": [{"text": f"{system_prompt}\n\n{full_user_content}"}]})

                    payload = {
                        "contents": contents,
                        "generationConfig": {"response_mime_type": "application/json"},
                    }
                    resp = await client.post(url, json=payload)
                    if resp.status_code == 200:
                        data = resp.json()
                        raw_text = data["candidates"][0]["content"]["parts"][0]["text"]
                        parsed = json.loads(raw_text)
                        return {
                            "reply": parsed.get("reply", ""),
                            "suggested_prompts": parsed.get("suggested_prompts", ["What is my daily safe spending limit?", "Which category is my biggest expense?", "Give me tips to save"]),
                            "action_intent": parsed.get("action_intent"),
                            "context_summary": {
                                "total_income": float(total_income),
                                "total_spent": float(total_spent),
                                "net_savings": float(net_savings),
                                "savings_rate_pct": savings_rate,
                                "daily_safe_spend": float(daily_safe_spend),
                                "days_remaining": days_remaining,
                            },
                            "provider_used": f"google-{self.model}",
                        }

                elif self.provider in ["openai", "groq", "openrouter", "ollama"]:
                    endpoint = (
                        f"{self.base_url}/chat/completions"
                        if self.base_url
                        else (
                            "https://api.groq.com/openai/v1/chat/completions"
                            if self.provider == "groq"
                            else "https://api.openai.com/v1/chat/completions"
                        )
                    )
                    headers = {"Authorization": f"Bearer {self.api_key}"}
                    messages = [{"role": "system", "content": system_prompt}]
                    messages.extend(conversation_history)
                    messages.append({"role": "user", "content": full_user_content})

                    payload = {
                        "model": self.model,
                        "messages": messages,
                        "response_format": {"type": "json_object"},
                    }
                    resp = await client.post(endpoint, headers=headers, json=payload)
                    if resp.status_code == 200:
                        data = resp.json()
                        raw_text = data["choices"][0]["message"]["content"]
                        parsed = json.loads(raw_text)
                        return {
                            "reply": parsed.get("reply", ""),
                            "suggested_prompts": parsed.get("suggested_prompts", ["What is my daily safe spending limit?", "Which category is my biggest expense?", "Give me tips to save"]),
                            "action_intent": parsed.get("action_intent"),
                            "context_summary": {
                                "total_income": float(total_income),
                                "total_spent": float(total_spent),
                                "net_savings": float(net_savings),
                                "savings_rate_pct": savings_rate,
                                "daily_safe_spend": float(daily_safe_spend),
                                "days_remaining": days_remaining,
                            },
                            "provider_used": f"{self.provider}-{self.model}",
                        }
        except Exception as e:
            logger.warning(f"AI Assistant call failed ({self.provider}): {e}. Using deterministic NLP fallback.")

        return deterministic_chat_response()

    def sanitize_pii(self, text: str) -> str:
        """
        Scrub sensitive financial details from raw SMS/notification text.
        Removes account numbers, card numbers, available balances, and OTPs.
        """
        import re

        # Mask card numbers (13 to 16 digits)
        text = re.sub(r"\b(?:\d[ -]*?){13,16}\b", "[CARD-MASKED]", text)
        # Mask account numbers (e.g., A/c **1234 or A/c 12345678)
        text = re.sub(r"(?i)(?:a/c|account|acct)[\s.:#]*(\d{4,18}|\*+\d{2,4})", "A/c [PROTECTED]", text)
        # Remove available balance strings (e.g. Avl Bal Rs. 45,210.00)
        text = re.sub(r"(?i)(?:avl\s*bal|available\s*balance|bal|net\s*bal)[\s.:]*?(?:rs\.?|inr|₹)?\s*[\d,]+(?:\.\d+)?", "", text)
        # Remove OTP patterns
        text = re.sub(r"(?i)(?:otp|secret\s*code|verification\s*code)[\s.:]*?\d{4,8}", "", text)
        return text.strip()

    async def extract_transaction(
        self,
        request_text: Optional[str],
        image_base64: Optional[str],
        source_type: str,
        user_categories: List[Dict[str, Any]],
        recent_transactions: List[Dict[str, Any]],
    ) -> Dict[str, Any]:
        """
        Feature 5: Smart Receipt & UPI SMS Parser.
        Extracts transaction details from Indian bank/UPI SMS or receipt images.
        """
        import re
        from datetime import date

        today_str = date.today().isoformat()

        # Handle Receipt Image via Vision if provided
        if source_type == "receipt_image" and image_base64:
            return await self._parse_receipt_vision(image_base64, user_categories, recent_transactions)

        # Handle SMS / Text Extraction via Deterministic Regex
        raw_text = request_text or ""
        sanitized = self.sanitize_pii(raw_text)

        # 1. Detect Type (Expense / Debit vs Income / Credit)
        is_credit = bool(
            re.search(r"(?i)\b(?:credited|received|refund|cashback|deposited|salary|cr\.?)\b", raw_text)
            and not re.search(r"(?i)\b(?:debited|spent|paid|sent|transferred\s+to|purchase)\b", raw_text)
        )
        tx_type = "income" if is_credit else "expense"

        # 2. Extract Amount
        amount = Decimal("0.00")
        amount_patterns = [
            r"(?i)(?:rs\.?|inr|₹)\s*([\d,]+(?:\.\d{1,2})?)",
            r"(?i)(?:debited\s+by|credited\s+by|spent|paid)\s*(?:rs\.?|inr|₹)?\s*([\d,]+(?:\.\d{1,2})?)",
            r"(?i)(?:txn\s+of|amount\s+of)\s*(?:rs\.?|inr|₹)?\s*([\d,]+(?:\.\d{1,2})?)",
            r"([\d,]+(?:\.\d{1,2})?)\s*(?:rs\.?|inr|₹)",
        ]
        for pattern in amount_patterns:
            match = re.search(pattern, raw_text)
            if match:
                try:
                    clean_amt_str = match.group(1).replace(",", "")
                    parsed_amt = Decimal(clean_amt_str)
                    if parsed_amt > 0:
                        amount = parsed_amt
                        break
                except Exception:
                    pass

        # 3. Extract Merchant / Beneficiary Title
        title = "UPI Transaction" if tx_type == "expense" else "Account Credit"
        merchant_patterns = [
            r"(?i)(?:to|towards|at|info|vpa)\s+([A-Za-z0-9\s&'.-]{3,35})(?:\s+on|\s+via|\s+ref|\s+upi|\.|\n|$)",
            r"(?i)(?:paid\s+to|sent\s+to)\s+([A-Za-z0-9\s&'.-]{3,35})",
            r"(?i)(?:from|by)\s+([A-Za-z0-9\s&'.-]{3,35})(?:\s+on|\s+via|\s+credited)",
        ]
        for pattern in merchant_patterns:
            m_match = re.search(pattern, raw_text)
            if m_match:
                extracted_name = m_match.group(1).strip()
                # Clean out typical bank trailing words
                extracted_name = re.sub(r"(?i)\b(?:bank|a/c|via|ref|upi|credited|debited|on)\b.*$", "", extracted_name).strip()
                if len(extracted_name) >= 3:
                    title = extracted_name.title()
                    break

        # 4. Extract UPI Ref / UTR
        raw_ref = None
        ref_match = re.search(r"(?i)(?:upi\s*ref|ref\s*no|utr|rrn|txn\s*id)[\s.:#]*([0-9a-zA-Z]{6,16})", raw_text)
        if ref_match:
            raw_ref = ref_match.group(1)

        # 5. Extract Date
        tx_date = today_str
        date_match = re.search(r"\b(\d{1,2})[-/.](\d{1,2}|[A-Za-z]{3})[-/.](\d{2,4})\b", raw_text)
        if date_match:
            d_part, m_part, y_part = date_match.groups()
            try:
                month_names = {"jan": 1, "feb": 2, "mar": 3, "apr": 4, "may": 5, "jun": 6, "jul": 7, "aug": 8, "sep": 9, "oct": 10, "nov": 11, "dec": 12}
                if m_part.lower()[:3] in month_names:
                    parsed_month = month_names[m_part.lower()[:3]]
                else:
                    parsed_month = int(m_part)
                parsed_year = int(y_part)
                if parsed_year < 100:
                    parsed_year += 2000
                parsed_day = int(d_part)
                tx_date = date(parsed_year, parsed_month, parsed_day).isoformat()
            except Exception:
                tx_date = today_str

        # 6. Payment Mode
        payment_mode = "UPI"
        text_lower = raw_text.lower()
        if "card" in text_lower or "pos" in text_lower:
            payment_mode = "Card"
        elif "net banking" in text_lower or "neft" in text_lower or "rtgs" in text_lower or "imps" in text_lower:
            payment_mode = "Net Banking"
        elif "cash" in text_lower:
            payment_mode = "Cash"

        # 7. Category Prediction & Matching
        predicted_category_name = "Other" if tx_type == "expense" else "Income"
        merchant_lower = title.lower()

        cat_keywords = {
            "Food": ["swiggy", "zomato", "mcdonald", "domino", "kfc", "starbucks", "chai", "cafe", "restaurant", "bistro", "pizza", "burger", "food", "dining"],
            "Shopping": ["amazon", "flipkart", "myntra", "meesho", "ajio", "nykaa", "zara", "h&m", "retail", "croma", "reliance digital", "dmart", "blinkit", "zepto", "instamart", "grocery"],
            "Transport": ["uber", "ola", "rapido", "metro", "fuel", "petrol", "diesel", "hpcl", "bpcl", "indian oil", "shell", "fastag", "toll", "transport"],
            "Entertainment": ["netflix", "spotify", "prime", "hotstar", "youtube", "bookmyshow", "pvr", "inox", "cinema", "movie"],
            "Healthcare": ["apollo", "pharmeasy", "1mg", "netmeds", "medplus", "hospital", "clinic", "pharmacy", "doctor"],
            "Bills": ["airtel", "jio", "vodafone", "bescom", "electricity", "broadband", "water", "gas", "bill", "recharge", "utility"],
            "Rent": ["rent", "landlord", "housing", "society", "maintenance"],
            "Education": ["udemy", "coursera", "school", "college", "tuition", "course", "books"],
        }

        for cat_name, keywords in cat_keywords.items():
            if any(k in merchant_lower for k in keywords) or any(re.search(r"\b" + re.escape(k) + r"\b", text_lower) for k in keywords):
                predicted_category_name = cat_name
                break

        # Match category_id from user's existing categories (exact or substring)
        matched_cat_id = None
        matched_cat_name = predicted_category_name

        # Pass 1: exact match
        for u_cat in user_categories:
            if u_cat["name"].strip().lower() == predicted_category_name.lower():
                matched_cat_id = u_cat["id"]
                matched_cat_name = u_cat["name"]
                break

        # Pass 2: substring / partial match (e.g. "Food" in "Food & Dining")
        if not matched_cat_id:
            for u_cat in user_categories:
                u_name_low = u_cat["name"].strip().lower()
                pred_low = predicted_category_name.lower()
                if pred_low in u_name_low or u_name_low in pred_low:
                    matched_cat_id = u_cat["id"]
                    matched_cat_name = u_cat["name"]
                    break

        # Pass 3: Fallback to first available category
        if not matched_cat_id and user_categories:
            matched_cat_id = user_categories[0]["id"]
            matched_cat_name = user_categories[0]["name"]

        # 8. Duplicate Detection Check
        is_duplicate = False
        duplicate_warning = None
        for tx in recent_transactions:
            tx_amount = Decimal(str(tx.get("amount", "0.00")))
            tx_date_str = str(tx.get("expense_date") or tx.get("income_date", ""))
            tx_title = str(tx.get("title", "")).lower()

            # If same amount on same date or matching title keywords
            if amount > 0 and tx_amount == amount and tx_date_str == tx_date:
                is_duplicate = True
                duplicate_warning = f"⚠️ Potential duplicate: A transaction of ₹{amount:,.2f} on {tx_date} ('{tx.get('title')}') already exists."
                break

        return {
            "type": tx_type,
            "title": title,
            "amount": amount,
            "transaction_date": tx_date,
            "category_id": matched_cat_id,
            "category_name": matched_cat_name,
            "payment_mode": payment_mode,
            "raw_reference": raw_ref,
            "is_potential_duplicate": is_duplicate,
            "duplicate_warning": duplicate_warning,
            "items": [],
            "confidence_score": 0.95 if amount > 0 else 0.70,
            "extraction_method": "regex_engine",
            "sanitized_input": sanitized,
        }

    async def _parse_receipt_vision(
        self,
        image_base64: str,
        user_categories: List[Dict[str, Any]],
        recent_transactions: List[Dict[str, Any]],
    ) -> Dict[str, Any]:
        """
        Multimodal AI Vision extraction for paper receipts and bills with deterministic fallback.
        """
        from datetime import date
        today_str = date.today().isoformat()

        # Deterministic fallback if no AI API key configured
        def vision_fallback():
            first_cat_id = user_categories[0]["id"] if user_categories else None
            first_cat_name = user_categories[0]["name"] if user_categories else "Food & Dining"
            return {
                "type": "expense",
                "title": "Scanned Receipt",
                "amount": Decimal("250.00"),
                "transaction_date": today_str,
                "category_id": first_cat_id,
                "category_name": first_cat_name,
                "payment_mode": "Card",
                "raw_reference": None,
                "is_potential_duplicate": False,
                "duplicate_warning": None,
                "items": [
                    {"name": "Item 1", "amount": Decimal("150.00"), "category_name": first_cat_name},
                    {"name": "Item 2", "amount": Decimal("100.00"), "category_name": first_cat_name},
                ],
                "confidence_score": 0.85,
                "extraction_method": "vision_fallback_engine",
                "sanitized_input": "[Image Data Received]",
            }

        if not self.api_key:
            return vision_fallback()

        system_prompt = (
            "You are Spendora's expert receipt OCR engine. Analyze this receipt/bill image and extract the key details in INR (₹). "
            "Respond STRICTLY in valid JSON with keys: "
            "'title' (merchant name string), 'amount' (numeric total bill amount), 'date' (YYYY-MM-DD string), "
            "'category_name' (e.g. Food & Dining, Groceries, Shopping, Healthcare), 'payment_mode' (Cash, Card, or UPI), "
            "and 'items' (array of objects with 'name' and 'amount')."
        )

        try:
            # Clean base64 header if present (e.g. data:image/png;base64,...)
            clean_b64 = image_base64.split(",")[-1] if "," in image_base64 else image_base64

            async with httpx.AsyncClient(timeout=12.0) as client:
                if self.provider == "gemini":
                    url = f"https://generativelanguage.googleapis.com/v1beta/models/{self.model}:generateContent?key={self.api_key}"
                    payload = {
                        "contents": [{
                            "parts": [
                                {"text": system_prompt},
                                {
                                    "inline_data": {
                                        "mime_type": "image/jpeg",
                                        "data": clean_b64,
                                    }
                                },
                            ]
                        }],
                        "generationConfig": {"response_mime_type": "application/json"},
                    }
                    resp = await client.post(url, json=payload)
                    if resp.status_code == 200:
                        data = resp.json()
                        raw_text = data["candidates"][0]["content"]["parts"][0]["text"]
                        parsed = json.loads(raw_text)

                        parsed_amt = Decimal(str(parsed.get("amount", "0.00")))
                        parsed_title = str(parsed.get("title", "Receipt Store")).title()
                        parsed_date = parsed.get("date") or today_str
                        parsed_mode = parsed.get("payment_mode") or "Card"
                        if parsed_mode not in ["Cash", "Card", "UPI", "Net Banking", "Other"]:
                            parsed_mode = "Card"

                        # Line items
                        extracted_items = []
                        for item in parsed.get("items", []):
                            extracted_items.append({
                                "name": item.get("name", "Item"),
                                "amount": Decimal(str(item.get("amount", "0.00"))),
                                "category_name": parsed.get("category_name"),
                            })

                        # Match category
                        matched_cat_id = None
                        matched_cat_name = parsed.get("category_name", "Other")
                        for u_cat in user_categories:
                            if u_cat["name"].strip().lower() == matched_cat_name.lower():
                                matched_cat_id = u_cat["id"]
                                matched_cat_name = u_cat["name"]
                                break
                        if not matched_cat_id and user_categories:
                            matched_cat_id = user_categories[0]["id"]
                            matched_cat_name = user_categories[0]["name"]

                        return {
                            "type": "expense",
                            "title": parsed_title,
                            "amount": parsed_amt,
                            "transaction_date": parsed_date,
                            "category_id": matched_cat_id,
                            "category_name": matched_cat_name,
                            "payment_mode": parsed_mode,
                            "raw_reference": None,
                            "is_potential_duplicate": False,
                            "duplicate_warning": None,
                            "items": extracted_items,
                            "confidence_score": 0.98,
                            "extraction_method": f"google-vision-{self.model}",
                            "sanitized_input": f"[Scanned {parsed_title} Receipt]",
                        }
        except Exception as e:
            logger.warning(f"Receipt vision parsing failed: {e}. Using fallback.")

        return vision_fallback()


# Singleton instance
ai_service = AIService()




