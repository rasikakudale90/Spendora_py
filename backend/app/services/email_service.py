"""
Email service for sending transactional emails (password reset, notifications)
via standard SMTP (e.g. Gmail SMTP) with asynchronous background dispatch.
"""
import asyncio
import logging
import smtplib
from email.header import Header
from email.mime.multipart import MIMEMultipart
from email.mime.text import MIMEText

import httpx

from app.core.config import settings

logger = logging.getLogger(__name__)


async def _dispatch_email(to_email: str, subject: str, html_body: str) -> bool:
    """
    100% Environment-Driven Email Dispatcher.
    - If EMAIL_API_URL and EMAIL_API_KEY are configured (e.g. Resend, SendGrid, Mailgun in Production),
      dispatches via asynchronous HTTPS REST API.
    - Otherwise, seamlessly falls back to SMTP (e.g. Gmail SMTP for local testing).
    """
    # ── 1. HTTP REST API Provider (Resend, SendGrid, etc.) ──────────────────────
    if settings.EMAIL_API_URL and settings.EMAIL_API_KEY:
        sender = settings.EMAIL_FROM or f"{settings.EMAIL_FROM_NAME} <onboarding@resend.dev>"
        payload = {
            "from": sender,
            "to": [to_email],
            "subject": subject,
            "html": html_body,
        }
        headers = {
            "Authorization": f"Bearer {settings.EMAIL_API_KEY}",
            "Content-Type": "application/json",
        }
        try:
            async with httpx.AsyncClient(timeout=15.0) as client:
                response = await client.post(settings.EMAIL_API_URL, json=payload, headers=headers)
                if response.status_code in (200, 201, 202):
                    print(f"[Spendora Email API] Successfully delivered '{subject}' to {to_email}")
                    logger.info("Successfully dispatched email via HTTP API to %s", to_email)
                    return True
                else:
                    print(f"[Spendora Email API ERROR] {response.status_code} - {response.text}")
                    logger.error("Failed to dispatch email via HTTP API: %s %s", response.status_code, response.text)
                    return False
        except Exception as exc:
            print(f"[Spendora Email API EXCEPTION] Failed to send to {to_email}: {exc}")
            logger.error("Exception in HTTP email dispatch: %s", exc)
            return False

    # ── 2. Standard SMTP Fallback (Gmail SMTP for local dev) ─────────────────────
    return await asyncio.to_thread(_send_smtp_sync, to_email, subject, html_body)


def _send_smtp_sync(to_email: str, subject: str, html_body: str) -> bool:
    """Synchronous SMTP worker function executed in an asyncio thread pool."""
    if not settings.SMTP_USER or not settings.SMTP_PASSWORD:
        logger.warning(
            "SMTP is not configured (SMTP_USER or SMTP_PASSWORD missing). "
            "Email to %s was not sent.",
            to_email,
        )
        return False

    sender_email = settings.SMTP_FROM_EMAIL or settings.SMTP_USER
    sender_name = settings.SMTP_FROM_NAME or "Spendora"

    msg = MIMEMultipart("alternative")
    msg["Subject"] = Header(subject, "utf-8")
    msg["From"] = f"{Header(sender_name, 'utf-8').encode()} <{sender_email}>"
    msg["To"] = to_email

    # Plaintext fallback
    plain_text = "Welcome to Spendora! Please view this email in an HTML-compatible client."
    msg.attach(MIMEText(plain_text, "plain", "utf-8"))
    msg.attach(MIMEText(html_body, "html", "utf-8"))

    try:
        with smtplib.SMTP(settings.SMTP_HOST, settings.SMTP_PORT, timeout=20) as server:
            if settings.SMTP_TLS:
                server.starttls()
            server.login(settings.SMTP_USER, settings.SMTP_PASSWORD)
            server.send_message(msg)
        print(f"[Spendora SMTP] Successfully delivered email to: {to_email}")
        logger.info("Successfully sent email '%s' to %s", subject, to_email)
        return True
    except Exception as exc:
        print(f"[Spendora SMTP ERROR] Failed to send email to {to_email}: {exc}")
        logger.error("Failed to send email to %s via SMTP: %s", to_email, exc)
        return False


async def send_password_reset_email(to_email: str, reset_token: str) -> bool:
    """
    Constructs a responsive, branded HTML password reset email and dispatches
    it asynchronously using Gmail SMTP.
    """
    frontend_base = settings.FRONTEND_URL.rstrip("/")
    reset_url = f"{frontend_base}/reset-password?token={reset_token}"

    subject = "Reset your Spendora password"
    html_content = f"""<!DOCTYPE html>
<html>
<head>
  <meta charset="utf-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>Reset your Spendora password</title>
</head>
<body style="margin: 0; padding: 0; background-color: #0b0f19; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif; color: #f3f4f6;">
  <table width="100%" border="0" cellspacing="0" cellpadding="0" style="background-color: #0b0f19; padding: 40px 16px;">
    <tr>
      <td align="center">
        <table width="100%" max-width="520" border="0" cellspacing="0" cellpadding="0" style="max-width: 520px; background-color: #111827; border: 1px solid #1f2937; border-radius: 24px; padding: 36px 32px; box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.5);">
          <!-- Logo & Brand Header -->
          <tr>
            <td align="center" style="padding-bottom: 24px;">
              <table border="0" cellspacing="0" cellpadding="0">
                <tr>
                  <td style="width: 44px; height: 44px; background: linear-gradient(135deg, #10b981, #14b8a6); border-radius: 14px; text-align: center; vertical-align: middle; font-size: 22px; font-weight: bold; color: #022c22;">
                    ₹
                  </td>
                  <td style="padding-left: 14px; font-size: 24px; font-weight: 800; color: #10b981; letter-spacing: -0.5px;">
                    Spendora
                  </td>
                </tr>
              </table>
            </td>
          </tr>

          <!-- Heading -->
          <tr>
            <td style="font-size: 20px; font-weight: 700; color: #ffffff; text-align: center; padding-bottom: 12px;">
              Reset Your Password
            </td>
          </tr>

          <!-- Message -->
          <tr>
            <td style="font-size: 14px; line-height: 22px; color: #9ca3af; text-align: center; padding-bottom: 28px;">
              We received a request to reset the password for your Spendora account associated with <strong style="color: #e5e7eb;">{to_email}</strong>.<br><br>
              Click the button below to choose a new password.
            </td>
          </tr>

          <!-- Action Button -->
          <tr>
            <td align="center" style="padding-bottom: 28px;">
              <a href="{reset_url}" target="_blank" style="display: inline-block; background-color: #10b981; color: #022c22; font-size: 14px; font-weight: 700; text-decoration: none; padding: 14px 32px; border-radius: 12px; box-shadow: 0 4px 14px rgba(16, 185, 129, 0.3);">
                Reset Password
              </a>
            </td>
          </tr>

          <!-- Expiration Notice -->
          <tr>
            <td style="font-size: 12px; line-height: 18px; color: #6b7280; text-align: center; border-top: 1px solid #1f2937; padding-top: 20px;">
              This link is valid for <strong>1 hour</strong>. If you did not request a password reset, you can safely ignore this email — your account remains secure.
            </td>
          </tr>

          <!-- Fallback URL -->
          <tr>
            <td style="font-size: 11px; line-height: 16px; color: #4b5563; word-break: break-all; text-align: center; padding-top: 16px;">
              If the button doesn't work, copy and paste this link into your browser:<br>
              <a href="{reset_url}" style="color: #10b981; text-decoration: underline;">{reset_url}</a>
            </td>
          </tr>
        </table>

        <!-- Footer -->
        <table width="100%" max-width="520" border="0" cellspacing="0" cellpadding="0" style="max-width: 520px; padding-top: 24px; text-align: center;">
          <tr>
            <td style="font-size: 12px; color: #4b5563;">
              © Spendora V1 • Personal Expense & Budget Tracking
            </td>
          </tr>
        </table>
      </td>
    </tr>
  </table>
</body>
</html>"""

    # Unified dispatcher: sends via HTTP REST API (Resend) if configured, else fallback to SMTP
    return await _dispatch_email(to_email, subject, html_content)


async def send_welcome_registration_email(to_email: str, full_name: str | None = None) -> bool:
    """
    Constructs a responsive, branded HTML welcome email upon user registration
    and dispatches it asynchronously using Gmail SMTP.
    """
    frontend_base = settings.FRONTEND_URL.rstrip("/")
    login_url = f"{frontend_base}/login?email={to_email}"
    name_display = full_name.strip() if full_name and full_name.strip() else "there"

    subject = "Welcome to Spendora! 🚀"
    html_content = f"""<!DOCTYPE html>
<html>
<head>
  <meta charset="utf-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>Welcome to Spendora</title>
</head>
<body style="margin: 0; padding: 0; background-color: #0b0f19; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif; color: #f3f4f6;">
  <table width="100%" border="0" cellspacing="0" cellpadding="0" style="background-color: #0b0f19; padding: 40px 16px;">
    <tr>
      <td align="center">
        <table width="100%" max-width="520" border="0" cellspacing="0" cellpadding="0" style="max-width: 520px; background-color: #111827; border: 1px solid #1f2937; border-radius: 24px; padding: 36px 32px; box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.5);">
          <!-- Logo & Brand Header -->
          <tr>
            <td align="center" style="padding-bottom: 24px;">
              <table border="0" cellspacing="0" cellpadding="0">
                <tr>
                  <td style="width: 44px; height: 44px; background: linear-gradient(135deg, #10b981, #14b8a6); border-radius: 14px; text-align: center; vertical-align: middle; font-size: 22px; font-weight: bold; color: #022c22;">
                    ₹
                  </td>
                  <td style="padding-left: 14px; font-size: 24px; font-weight: 800; color: #10b981; letter-spacing: -0.5px;">
                    Spendora
                  </td>
                </tr>
              </table>
            </td>
          </tr>

          <!-- Heading -->
          <tr>
            <td style="font-size: 22px; font-weight: 700; color: #ffffff; text-align: center; padding-bottom: 12px;">
              Welcome, {name_display}! 🎉
            </td>
          </tr>

          <!-- Message -->
          <tr>
            <td style="font-size: 14px; line-height: 22px; color: #9ca3af; text-align: center; padding-bottom: 24px;">
              Your Spendora account is now created and ready to use.<br>
              Take control of your personal finances with real-time expense tracking, multi-period budget thresholds, and cash flow analytics.
            </td>
          </tr>

          <!-- Action Button -->
          <tr>
            <td align="center" style="padding-bottom: 28px;">
              <a href="{login_url}" target="_blank" style="display: inline-block; background-color: #10b981; color: #022c22; font-size: 14px; font-weight: 700; text-decoration: none; padding: 14px 36px; border-radius: 12px; box-shadow: 0 4px 14px rgba(16, 185, 129, 0.3);">
                Sign In to Your Account
              </a>
            </td>
          </tr>

          <!-- Quick Features Highlights -->
          <tr>
            <td style="border-top: 1px solid #1f2937; padding-top: 20px; font-size: 13px; color: #d1d5db;">
              <p style="margin: 0 0 10px 0; font-weight: 600; color: #10b981;">What you can do right now:</p>
              <ul style="margin: 0; padding-left: 20px; color: #9ca3af; line-height: 20px;">
                <li style="margin-bottom: 6px;">Set <strong>Daily, Weekly, Monthly, or Yearly</strong> budgets with real-time limit alerts.</li>
                <li style="margin-bottom: 6px;">Log expenses with payment modes (UPI, Card, Cash) and custom categories.</li>
                <li style="margin-bottom: 6px;">Record multiple income streams to track your net savings and cash flow.</li>
              </ul>
            </td>
          </tr>
        </table>

        <!-- Footer -->
        <table width="100%" max-width="520" border="0" cellspacing="0" cellpadding="0" style="max-width: 520px; padding-top: 24px; text-align: center;">
          <tr>
            <td style="font-size: 12px; color: #4b5563;">
              © Spendora V1 • Personal Expense & Budget Tracking
            </td>
          </tr>
        </table>
      </td>
    </tr>
  </table>
</body>
</html>"""

    # Unified dispatcher: sends via HTTP REST API (Resend) if configured, else fallback to SMTP
    return await _dispatch_email(to_email, subject, html_content)
