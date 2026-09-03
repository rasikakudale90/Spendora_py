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


# Singleton instance
ai_service = AIService()

