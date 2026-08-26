"""
Application settings loaded from environment variables.
Uses pydantic-settings for type-safe, validated configuration.
"""
from pydantic_settings import BaseSettings, SettingsConfigDict


class Settings(BaseSettings):
    # PostgreSQL connection string — must use asyncpg driver
    # e.g. postgresql+asyncpg://user:password@host:port/dbname
    DATABASE_URL: str

    # Budget near-limit alert threshold (0.0 – 1.0)
    # Default: 0.80 → alert when 80% of monthly budget is consumed
    BUDGET_NEAR_LIMIT_THRESHOLD: float = 0.80

    model_config = SettingsConfigDict(
        env_file=".env",
        env_file_encoding="utf-8",
        extra="ignore",
    )


# Singleton instance — import this throughout the app
settings = Settings()
