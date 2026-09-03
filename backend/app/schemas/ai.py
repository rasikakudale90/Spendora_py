from datetime import date
from decimal import Decimal
from typing import List, Literal, Optional
from uuid import UUID
from pydantic import BaseModel, Field


# ── Feature 1: Purchase Decision Simulator Schemas ──────────────────────────

class PurchaseSimulationRequest(BaseModel):
    title: str = Field(..., min_length=1, max_length=100, description="Name or description of the potential purchase")
    amount: Decimal = Field(..., gt=0, description="Estimated purchase amount in INR")
    category_id: Optional[UUID] = Field(None, description="Optional target expense category")


class PurchaseSimulationResponse(BaseModel):
    verdict: Literal["safe", "caution", "over_budget"]
    verdict_title: str
    verdict_summary: str
    item_title: str
    item_amount: Decimal
    current_cash_flow: Decimal
    projected_cash_flow: Decimal
    current_savings_rate: float
    projected_savings_rate: float
    current_spent: Decimal
    projected_spent: Decimal
    overall_budget: Optional[Decimal] = None
    remaining_overall_budget: Optional[Decimal] = None
    projected_remaining_budget: Optional[Decimal] = None
    daily_safe_spend_before: Decimal
    daily_safe_spend_after: Decimal
    ai_analysis: str
    actionable_tips: List[str]
    provider_used: str


# ── Feature 2: Leak Hunter & Subscription Audit Schemas ─────────────────────

class SubscriptionItem(BaseModel):
    title: str
    average_amount: Decimal
    occurrence_count: int
    last_date: date
    estimated_monthly_cost: Decimal
    category_name: Optional[str] = "Subscription / Recurring"


class MicroSpendingLeak(BaseModel):
    category_or_label: str
    transaction_count: int
    average_amount: Decimal
    monthly_total: Decimal
    annual_projected_drain: Decimal
    example_items: List[str]


class LeakAnalysisResponse(BaseModel):
    total_monthly_leak: Decimal
    total_annual_projected_leak: Decimal
    total_monthly_subscriptions: Decimal
    total_annual_subscriptions: Decimal
    subscription_count: int
    micro_leak_count: int
    detected_subscriptions: List[SubscriptionItem]
    micro_spending_leaks: List[MicroSpendingLeak]
    ai_summary: str
    actionable_savings_tips: List[str]
    provider_used: str


# ── Feature 3: Safe-to-Spend Real-Time Gauge & Burn Forecaster ──────────────

class SafeToSpendResponse(BaseModel):
    daily_safe_spend: Decimal
    burn_rate_status: Literal["optimal", "warning", "danger"]
    current_burn_rate_per_day: Decimal
    days_remaining_in_month: int
    days_passed: int
    total_monthly_income: Decimal
    total_spent_so_far: Decimal
    remaining_buffer: Decimal
    projected_month_end_balance: Decimal
    projected_zero_cash_day: Optional[int] = None
    burn_pace_percentage: float
    ai_recommendation: str
    actionable_tips: List[str]
    provider_used: str


# ── Feature 4: Natural Language Financial Assistant / Chatbot Schemas ────────

class ChatMessage(BaseModel):
    role: Literal["user", "assistant", "system"]
    content: str
    timestamp: Optional[str] = None


class FinancialActionIntent(BaseModel):
    action: Literal["simulate_purchase", "view_leaks", "navigate", "set_budget", "add_expense", "none"]
    label: str
    payload: Optional[dict] = None


class FinancialChatRequest(BaseModel):
    message: str = Field(..., min_length=1, max_length=1000, description="User's query in natural language")
    history: Optional[List[ChatMessage]] = Field(default=[], description="Recent conversation history for multi-turn context")


class FinancialChatResponse(BaseModel):
    reply: str
    suggested_prompts: List[str]
    action_intent: Optional[FinancialActionIntent] = None
    context_summary: dict
    provider_used: str


# ── Feature 5: Smart Receipt & UPI SMS Parser Schemas ───────────────────────

class ExtractedItem(BaseModel):
    name: str
    amount: Decimal
    category_name: Optional[str] = None


class TransactionExtractionRequest(BaseModel):
    text: Optional[str] = Field(None, description="Pasted SMS or notification text")
    image_base64: Optional[str] = Field(None, description="Base64 encoded receipt/bill image data")
    source_type: Literal["sms_text", "receipt_image"] = Field("sms_text", description="Input modality")


class TransactionExtractionResponse(BaseModel):
    type: Literal["expense", "income"]
    title: str
    amount: Decimal
    transaction_date: str
    category_id: Optional[UUID] = None
    category_name: Optional[str] = None
    payment_mode: Literal["Cash", "Card", "UPI", "Net Banking", "Other"] = "UPI"
    raw_reference: Optional[str] = None
    is_potential_duplicate: bool = False
    duplicate_warning: Optional[str] = None
    items: List[ExtractedItem] = []
    confidence_score: float = 0.95
    extraction_method: str = "regex_engine"
    sanitized_input: str


