"""
Seed script to populate realistic dummy expenses and monthly budgets on the live Render API.
Uses standard library urllib to run anywhere without extra dependencies.
"""
import json
import urllib.request
from datetime import date

API_BASE = "https://spendora-py.onrender.com/api/v1"

def api_post(url, data):
    req = urllib.request.Request(
        url,
        data=json.dumps(data).encode("utf-8"),
        headers={"Content-Type": "application/json"},
        method="POST"
    )
    with urllib.request.urlopen(req) as response:
        return json.loads(response.read().decode("utf-8"))

def api_get(url):
    req = urllib.request.Request(url, headers={"Content-Type": "application/json"})
    with urllib.request.urlopen(req) as response:
        return json.loads(response.read().decode("utf-8"))

def seed():
    print(f"Connecting to live API at {API_BASE}...")
    
    # 1. Fetch categories
    cats = api_get(f"{API_BASE}/categories")
    categories = {c["name"]: c["id"] for c in cats}
    print(f"Found {len(categories)} categories: {list(categories.keys())}")

    # 2. Set August 2026 Budgets
    today_month = date.today().strftime("%Y-%m-01")
    print(f"Setting overall and category budgets for period: {today_month}...")
    
    # Overall monthly budget
    api_post(f"{API_BASE}/budgets", {
        "scope": "overall",
        "amount": 55000.00,
        "period_month": today_month,
        "category_id": None
    })
    
    # Food category budget
    if "Food" in categories:
        api_post(f"{API_BASE}/budgets", {
            "scope": "category",
            "amount": 12000.00,
            "period_month": today_month,
            "category_id": categories["Food"]
        })
    
    # Shopping category budget
    if "Shopping" in categories:
        api_post(f"{API_BASE}/budgets", {
            "scope": "category",
            "amount": 10000.00,
            "period_month": today_month,
            "category_id": categories["Shopping"]
        })

    # 3. Create Sample Expenses
    sample_expenses = [
        # Rent & Utilities
        {"title": "Monthly Apartment Rent", "amount": 22000.00, "category": "Rent", "mode": "Net Banking", "date": "2026-08-01", "notes": "August rent payment"},
        {"title": "High-Speed Fiber Internet", "amount": 1199.00, "category": "Bills", "mode": "UPI", "date": "2026-08-03", "notes": "Monthly broadband bill"},
        {"title": "Electricity & Water Bill", "amount": 2450.00, "category": "Bills", "mode": "Net Banking", "date": "2026-08-05", "notes": "State electricity board"},
        
        # Food & Groceries
        {"title": "Organic Groceries & Staples", "amount": 3450.00, "category": "Food", "mode": "Card", "date": "2026-08-04", "notes": "Nature's Basket grocery run"},
        {"title": "Team Dinner & Drinks", "amount": 2800.00, "category": "Food", "mode": "UPI", "date": "2026-08-09", "notes": "Italian restaurant dinner with team"},
        {"title": "Starbucks Artisan Coffee", "amount": 420.00, "category": "Food", "mode": "UPI", "date": "2026-08-12", "notes": "Caramel Macchiato & croissant"},
        {"title": "Weekly Supermarket Pantry", "amount": 1850.00, "category": "Food", "mode": "Card", "date": "2026-08-16", "notes": "Fresh vegetables, fruits, dairy"},
        {"title": "Weekend Brunch", "amount": 1350.00, "category": "Food", "mode": "UPI", "date": "2026-08-22", "notes": "Sunday cafe brunch"},
        
        # Shopping & Tech
        {"title": "Mechanical Keyboard & Keycaps", "amount": 4999.00, "category": "Shopping", "mode": "Card", "date": "2026-08-07", "notes": "Keychron wireless keyboard"},
        {"title": "Zara Summer Shirts", "amount": 3290.00, "category": "Shopping", "mode": "Card", "date": "2026-08-14", "notes": "Casual linen shirts"},
        {"title": "Ergonomic Desk Mat", "amount": 899.00, "category": "Shopping", "mode": "UPI", "date": "2026-08-18", "notes": "Dual-sided leather desk pad"},
        
        # Transport & Fuel
        {"title": "Uber Rides to Tech Park", "amount": 680.00, "category": "Transport", "mode": "UPI", "date": "2026-08-08", "notes": "Office commute"},
        {"title": "Vehicle Petrol Refuel", "amount": 2500.00, "category": "Transport", "mode": "Card", "date": "2026-08-15", "notes": "Full tank fuel at Shell"},
        {"title": "Metro Smart Card Recharge", "amount": 500.00, "category": "Transport", "mode": "UPI", "date": "2026-08-20", "notes": "Monthly metro balance"},
        
        # Entertainment & Subscriptions
        {"title": "IMAX Movie Tickets & Popcorn", "amount": 1100.00, "category": "Entertainment", "mode": "UPI", "date": "2026-08-10", "notes": "Weekend movie with friends"},
        {"title": "Spotify Family & Netflix HD", "amount": 799.00, "category": "Entertainment", "mode": "Card", "date": "2026-08-11", "notes": "Monthly streaming services"},
        
        # Healthcare & Wellness
        {"title": "Health Insurance Premium", "amount": 3500.00, "category": "Healthcare", "mode": "Net Banking", "date": "2026-08-06", "notes": "Quarterly health cover"},
        {"title": "Pharmacy & Daily Vitamins", "amount": 650.00, "category": "Healthcare", "mode": "UPI", "date": "2026-08-19", "notes": "Multivitamins and first aid"},
        
        # Education & Courses
        {"title": "Advanced Python & AI Course", "amount": 1999.00, "category": "Education", "mode": "Card", "date": "2026-08-13", "notes": "Udemy specialization course"},
    ]

    print(f"Creating {len(sample_expenses)} sample expenses...")
    created_count = 0
    for exp in sample_expenses:
        cat_id = categories.get(exp["category"])
        if not cat_id:
            continue
        
        payload = {
            "title": exp["title"],
            "amount": exp["amount"],
            "expense_date": exp["date"],
            "category_id": cat_id,
            "payment_mode": exp["mode"],
            "notes": exp.get("notes")
        }
        try:
            api_post(f"{API_BASE}/expenses", payload)
            created_count += 1
        except Exception as e:
            print(f"Failed to create {exp['title']}: {e}")

    print(f"Successfully seeded {created_count}/{len(sample_expenses)} expenses!")

if __name__ == "__main__":
    seed()
