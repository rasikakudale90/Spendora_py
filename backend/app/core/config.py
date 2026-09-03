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

    # ── Authentication & Security ─────────────────────────────────
    # JWT signing secret — in production, inject securely via env var
    JWT_SECRET_KEY: str = "spendora-production-jwt-secret-key-32-chars-min-change-me"
    JWT_ALGORITHM: str = "HS256"
    ACCESS_TOKEN_EXPIRE_MINUTES: int = 15
    REFRESH_TOKEN_EXPIRE_DAYS: int = 30

    # Google OAuth 2.0 / OpenID Connect Client ID
    GOOGLE_CLIENT_ID: str | None = None

    # Frontend URL (used for CORS and cookies)
    FRONTEND_URL: str = "http://localhost:3000"

    # Cookie security settings
    COOKIE_SECURE: bool = False  # Set to True in production (HTTPS)
    COOKIE_SAMESITE: str = "lax"

    # ── Email Provider Configuration (100% Environment-Driven) ──
    # HTTP REST API provider (e.g. Resend, SendGrid, Mailgun)
    EMAIL_API_URL: str | None = None
    EMAIL_API_KEY: str | None = None
    EMAIL_FROM: str | None = None
    EMAIL_FROM_NAME: str = "Spendora"

    # SMTP Configuration (Fallback for local dev or traditional SMTP relays)
    SMTP_HOST: str = "smtp.gmail.com"
    SMTP_PORT: int = 587
    SMTP_USER: str | None = None
    SMTP_PASSWORD: str | None = None
    SMTP_FROM_EMAIL: str | None = None
    SMTP_FROM_NAME: str = "Spendora"
    SMTP_TLS: bool = True

    # ── AI Intelligence Provider (100% Environment-Driven & Swappable) ──
    # Supported providers: "gemini", "openai", "anthropic", "groq", "openrouter", "ollama"
    AI_PROVIDER: str = "gemini"
    AI_API_KEY: str | None = None
    AI_MODEL: str | None = None
    AI_BASE_URL: str | None = None

    # Provider-specific keys (auto-detected if AI_API_KEY is not set directly)
    GEMINI_API_KEY: str | None = None
    OPENAI_API_KEY: str | None = None
    ANTHROPIC_API_KEY: str | None = None
    GROQ_API_KEY: str | None = None

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
