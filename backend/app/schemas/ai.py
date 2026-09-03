from decimal import Decimal
from typing import List, Literal, Optional
from uuid import UUID
from pydantic import BaseModel, Field


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
