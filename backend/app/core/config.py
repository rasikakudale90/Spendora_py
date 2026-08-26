"""
Application settings loaded from environment variables.
Uses pydantic-settings for type-safe, validated configuration.
"""
from pydantic import field_validator
from pydantic_settings import BaseSettings, SettingsConfigDict


class Settings(BaseSettings):
    # PostgreSQL connection string — must use asyncpg driver
    # e.g. postgresql+asyncpg://user:password@host:port/dbname
    DATABASE_URL: str

    # Budget near-limit alert threshold (0.0 – 1.0)
    # Default: 0.80 → alert when 80% of monthly budget is consumed
    BUDGET_NEAR_LIMIT_THRESHOLD: float = 0.80

    # CORS Origins (comma-separated or '*' for all)
    # e.g. "https://spendora.vercel.app,http://localhost:3000"
    CORS_ORIGINS: str = "*"

    # Web server port (injected dynamically by Render)
    PORT: int = 8000

    @field_validator("DATABASE_URL", mode="before")
    @classmethod
    def assemble_db_connection(cls, v: str) -> str:
        if isinstance(v, str):
            if v.startswith("postgres://"):
                return v.replace("postgres://", "postgresql+asyncpg://", 1)
            elif v.startswith("postgresql://") and not v.startswith("postgresql+asyncpg://"):
                return v.replace("postgresql://", "postgresql+asyncpg://", 1)
        return v

    model_config = SettingsConfigDict(
        env_file=".env",
        env_file_encoding="utf-8",
        extra="ignore",
    )


# Singleton instance — import this throughout the app
settings = Settings()
