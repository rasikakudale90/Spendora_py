from fastapi import APIRouter

from app.routers.categories import router as categories_router
from app.routers.expenses import router as expenses_router
from app.routers.budgets import router as budgets_router
from app.routers.dashboard import router as dashboard_router
from app.routers.incomes import router as incomes_router

api_router = APIRouter(prefix="/api/v1")

api_router.include_router(categories_router)
api_router.include_router(expenses_router)
api_router.include_router(budgets_router)
api_router.include_router(dashboard_router)
api_router.include_router(incomes_router)
