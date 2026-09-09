---
name: Spendora Dark Telemetry
colors:
  surface: '#0f131b'
  surface-dim: '#0f131b'
  surface-bright: '#353942'
  surface-container-lowest: '#0a0e16'
  surface-container-low: '#181c24'
  surface-container: '#1c2028'
  surface-container-high: '#262a33'
  surface-container-highest: '#31353e'
  on-surface: '#dfe2ee'
  on-surface-variant: '#bcc9cd'
  inverse-surface: '#dfe2ee'
  inverse-on-surface: '#2c3039'
  outline: '#869397'
  outline-variant: '#3d494c'
  surface-tint: '#4cd7f6'
  primary: '#4cd7f6'
  on-primary: '#003640'
  primary-container: '#06b6d4'
  on-primary-container: '#00424f'
  inverse-primary: '#00687a'
  secondary: '#d0bcff'
  on-secondary: '#3c0091'
  secondary-container: '#571bc1'
  on-secondary-container: '#c4abff'
  tertiary: '#4edea3'
  on-tertiary: '#003824'
  tertiary-container: '#1bbd85'
  on-tertiary-container: '#00452e'
  error: '#ffb4ab'
  on-error: '#690005'
  error-container: '#93000a'
  on-error-container: '#ffdad6'
  primary-fixed: '#acedff'
  primary-fixed-dim: '#4cd7f6'
  on-primary-fixed: '#001f26'
  on-primary-fixed-variant: '#004e5c'
  secondary-fixed: '#e9ddff'
  secondary-fixed-dim: '#d0bcff'
  on-secondary-fixed: '#23005c'
  on-secondary-fixed-variant: '#5516be'
  tertiary-fixed: '#6ffbbe'
  tertiary-fixed-dim: '#4edea3'
  on-tertiary-fixed: '#002113'
  on-tertiary-fixed-variant: '#005236'
  background: '#0f131b'
  on-background: '#dfe2ee'
  surface-variant: '#31353e'
typography:
  display-lg:
    fontFamily: Space Grotesk
    fontSize: 40px
    fontWeight: '700'
    lineHeight: 48px
    letterSpacing: -0.03em
  display-lg-mobile:
    fontFamily: Space Grotesk
    fontSize: 32px
    fontWeight: '700'
    lineHeight: 38px
    letterSpacing: -0.02em
  headline-lg:
    fontFamily: Space Grotesk
    fontSize: 28px
    fontWeight: '600'
    lineHeight: 36px
    letterSpacing: -0.02em
  headline-md:
    fontFamily: Space Grotesk
    fontSize: 22px
    fontWeight: '600'
    lineHeight: 28px
    letterSpacing: -0.01em
  headline-sm:
    fontFamily: Space Grotesk
    fontSize: 18px
    fontWeight: '600'
    lineHeight: 24px
  body-lg:
    fontFamily: Hanken Grotesk
    fontSize: 16px
    fontWeight: '400'
    lineHeight: 24px
  body-md:
    fontFamily: Hanken Grotesk
    fontSize: 14px
    fontWeight: '400'
    lineHeight: 20px
  body-sm:
    fontFamily: Hanken Grotesk
    fontSize: 12px
    fontWeight: '400'
    lineHeight: 16px
  label-lg:
    fontFamily: JetBrains Mono
    fontSize: 14px
    fontWeight: '500'
    lineHeight: 18px
    letterSpacing: 0.02em
  label-md:
    fontFamily: JetBrains Mono
    fontSize: 12px
    fontWeight: '500'
    lineHeight: 16px
    letterSpacing: 0.04em
  label-sm:
    fontFamily: JetBrains Mono
    fontSize: 10px
    fontWeight: '600'
    lineHeight: 14px
    letterSpacing: 0.06em
  telemetry-metric:
    fontFamily: JetBrains Mono
    fontSize: 26px
    fontWeight: '700'
    lineHeight: 32px
    letterSpacing: -0.02em
rounded:
  sm: 0.25rem
  DEFAULT: 0.5rem
  md: 0.75rem
  lg: 1rem
  xl: 1.5rem
  full: 9999px
spacing:
  space-2xs: 0.25rem
  space-xs: 0.5rem
  space-sm: 0.75rem
  space-md: 1rem
  space-lg: 1.25rem
  space-xl: 1.5rem
  space-2xl: 2rem
  space-3xl: 2.5rem
  screen-margin-mobile: 1rem
  screen-margin-tablet: 1.5rem
  card-padding: 1.25rem
  gutter-mobile: 0.75rem
  gutter-tablet: 1rem
---

# Spendora — Design System & Visual Specification

*Extracted from Stitch MCP (`projects/4900497969130245707`)*

---

## 1. Brand & Style Vision

The Spendora design system projects precision, financial autonomy, and high-performance engineering for modern personal wealth management. Tailored for analytical professionals, digital asset trackers, and design-conscious mobile users, the aesthetic creates an aura of deep focus, command, and institutional security.

The stylistic foundation fuses **Deep OLED Glassmorphism** with subtle **Neumorphic Ambient Radiance**:
- **Obsidian OLED Backdrops:** Preserves device power, increases battery efficiency on mobile screens, and eliminates visual clutter.
- **Translucent Frosted Glass Surfaces:** Delivers visual depth and clear hierarchy without opaque barriers.
- **Tactile Internal Glows & 1px Specular Edges:** Echoes aerospace avionics and high-end fintech terminals.
- **Neon Telemetry Indicators:** Unambiguous glanceability for burn rate velocity, category budgets, net savings runway, and cash flow.

---

## 2. Color Palette

The palette is engineered specifically for OLED panels and low-light mobile ergonomics, maximizing contrast while eliminating chromatic vibration.

### 2.1 Core Semantic Roles

| Role | Color Hex | Sample / Name | Purpose & Usage |
| :--- | :--- | :--- | :--- |
| **Primary Accent** | `#06B6D4` / `#4CD7F6` | Electric Cyan | Real-time telemetry, active focus states, dynamic trend lines, primary CTAs, interactive data nodes |
| **Secondary Accent** | `#8B5CF6` / `#D0BCFF` | Quantum Violet | System-level projections, secondary analytical modules, investment clusters, ambient glow underlays |
| **Positive Cashflow** | `#10B981` / `#4EDEA3` | Emerald Green | Income, asset appreciation, savings surplus, safe burn rates, favorable differentials |
| **Negative Outflow** | `#F43F5E` / `#FFB4AB` | Crimson Rose | Budget overruns, expense vectors, debt alerts, leak detection, danger burn paces |
| **Neutral Deep Canvas** | `#080A0F` / `#0F131B` | Obsidian Black | Ground canvas base. Minimizes OLED power draw, guarantees high contrast ratio |
| **Elevated Surface** | `#0E121A` / `#181C24` | Slate Surface | Container root base for cards, sheets, and glassmorphic underlays |

### 2.2 Material 3 Named Colors Reference

| Token Name | Hex Code | Role |
| :--- | :--- | :--- |
| `background` | `#0F131B` | Global background canvas |
| `surface` | `#0F131B` | Base surface layer |
| `surface_container_lowest` | `#0A0E16` | Lowest background recess / input containers |
| `surface_container_low` | `#181C24` | Card & section container background |
| `surface_container` | `#1C2028` | Standard container surface |
| `surface_container_high` | `#262A33` | Interactive elevated elements |
| `surface_container_highest`| `#31353E` | Modals & top sheet overlays |
| `surface_bright` | `#353942` | Highlighted surface sections |
| `on_surface` | `#DFE2EE` | Primary readable text on surfaces |
| `on_surface_variant` | `#BCC9CD` | Muted / secondary helper text |
| `primary` | `#4CD7F6` | Primary brand accent highlight |
| `primary_container` | `#06B6D4` | Primary active accent fill |
| `on_primary` | `#003640` | Text/icons on primary fill |
| `on_primary_container` | `#00424F` | High-contrast text on primary container |
| `secondary` | `#D0BCFF` | Secondary accent highlight |
| `secondary_container` | `#571BC1` | Secondary component background |
| `on_secondary` | `#3C0091` | Text/icons on secondary fill |
| `tertiary` | `#4EDEA3` | Financial gain / emerald accent |
| `tertiary_container` | `#1BBD85` | Income badge / surplus container |
| `error` | `#FFB4AB` | Danger highlight |
| `error_container` | `#93000A` | Over-budget / alert container |
| `outline` | `#869397` | Standard component border |
| `outline_variant` | `#3D494C` | Subtle divider border |

### 2.3 Specular & Glass Highlights
- **Border Sheen:** `rgba(255, 255, 255, 0.08)` for structural separation; dynamic focus increases to `rgba(6, 182, 212, 0.40)`.
- **Frosted Fill Layers:** Ranging from `rgba(14, 18, 26, 0.65)` on secondary panels to `rgba(18, 24, 38, 0.85)` on critical financial cards.

---

## 3. Typography

The typographic hierarchy separates structural layout, conversational prose, and numerical telemetry into three distinct typefaces:

1. **Space Grotesk (Headlines & Display):** Geometric, tech-forward construction evoking algorithmic precision. Drives brand statements, page titles, modal headers, and hero banners.
2. **Hanken Grotesk (Body & Prose):** Humanist neutrality providing effortless reading for descriptions, transaction notes, and help text.
3. **JetBrains Mono (Financial Telemetry & Monospace Data):** Monospaced precision dedicated to currency amounts, ledger entries, percentage yields, mathematical ratios, and compact UI tags. Uses tabular numerals (`tnum`) for vertical alignment.

### 3.1 Type Scale Specifications

| Style Token | Font Family | Size | Weight | Line Height | Letter Spacing | Usage Context |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| `display-lg` | Space Grotesk | `40px` | `700` (Bold) | `48px` | `-0.03em` | Desktop Hero / Main Screen Titles |
| `display-lg-mobile` | Space Grotesk | `32px` | `700` (Bold) | `38px` | `-0.02em` | Mobile Hero Balance Displays |
| `headline-lg` | Space Grotesk | `28px` | `600` (SemiBold) | `36px` | `-0.02em` | Primary Section Titles, Modal Headers |
| `headline-md` | Space Grotesk | `22px` | `600` (SemiBold) | `28px` | `-0.01em` | Card Titles, Sheet Headers |
| `headline-sm` | Space Grotesk | `18px` | `600` (SemiBold) | `24px` | `0` | Sub-section Headers, Group Titles |
| `body-lg` | Hanken Grotesk | `16px` | `400` (Regular) | `24px` | `0` | Primary Content, Explanations |
| `body-md` | Hanken Grotesk | `14px` | `400` (Regular) | `20px` | `0` | Standard Body, Form Labels, Table Cells |
| `body-sm` | Hanken Grotesk | `12px` | `400` (Regular) | `16px` | `0` | Footers, Secondary Captions, Subtext |
| `telemetry-metric`| JetBrains Mono | `26px` | `700` (Bold) | `32px` | `-0.02em` | Large Financial Figures, Safe-to-Spend Values |
| `label-lg` | JetBrains Mono | `14px` | `500` (Medium) | `18px` | `0.02em` | Transaction Currency Readouts, Key Data |
| `label-md` | JetBrains Mono | `12px` | `500` (Medium) | `16px` | `0.04em` | Timestamps, Category Tags, Small Values |
| `label-sm` | JetBrains Mono | `10px` | `600` (SemiBold) | `14px` | `0.06em` | Micro Badges, Pill Indicators, Status Counters |

---

## 4. Spacing & Layout System

Built around an **8pt progressive grid system**, optimized for thumb-reach ergonomics on mobile devices:

### 4.1 Spacing Scale
- `space-2xs`: `0.25rem` (`4px`) — Micro icon gaps, inline badge padding
- `space-xs`: `0.5rem` (`8px`) — List item vertical gaps, chip padding
- `space-sm`: `0.75rem` (`12px`) — Compact card gap, form input vertical gap
- `space-md`: `1.0rem` (`16px`) — Standard grid spacing, button padding, outer mobile screen margins
- `space-lg`: `1.25rem` (`20px`) — Internal card padding (`card-padding`)
- `space-xl`: `1.5rem` (`24px`) — Tablet margin, section separator
- `space-2xl`: `2.0rem` (`32px`) — Large block dividers
- `space-3xl`: `2.5rem` (`40px`) — Hero section padding

### 4.2 Layout Grid & Margins
- **Mobile Grid:** Single-column layout with `16px` outer screen margins. Cards stretch edge-to-edge within margins with `20px` internal padding.
- **Tablet / Desktop Grid:** Adaptive 8 to 12-column grid with `24px` margins and `16px` gutters.
- **Safe Touch Targets:** Interactive controls maintain a minimum touch bounding box of `48x48dp`.

---

## 5. Elevation, Surface Tiers & Neumorphism

Elevation is created via translucent atmospheric stacking, backdrop blurs, and neon perimeter emissions rather than heavy drop shadows:

- **Tier 0 (Root Base):** Solid `#080A0F`. Non-interactive background with 200px radial blurs of cyan (`rgba(6, 182, 212, 0.05)`) or violet (`rgba(139, 92, 246, 0.04)`).
- **Tier 1 (Base Cards & Group Containers):** `rgba(14, 18, 26, 0.70)`, 16px backdrop blur, bordered with `1px solid rgba(255, 255, 255, 0.06)`, shadow `0 8px 32px 0 rgba(0, 0, 0, 0.45)`.
- **Tier 2 (Interactive Cards & Telemetry Pods):** `rgba(18, 24, 38, 0.80)`, 24px backdrop blur, bordered with `1px solid rgba(255, 255, 255, 0.10)`, top specular highlight `linear-gradient(180deg, rgba(255, 255, 255, 0.15) 0%, rgba(255, 255, 255, 0.02) 100%)`.
- **Tier 3 (Modals, Action Sheets, Floating Controls):** `rgba(22, 30, 46, 0.92)`, 32px backdrop blur, bordered with `1px solid rgba(6, 182, 212, 0.30)`, dual shadow `0 16px 48px rgba(0, 0, 0, 0.75), 0 0 24px rgba(6, 182, 212, 0.15)`.

---

## 6. Shapes & Corner Radii

- `rounded-sm`: `0.25rem` (`4px`) — Micro tags, progress bar fills
- `rounded-md` / `DEFAULT`: `0.5rem` (`8px`) — Category pills, filter chips, micro-metrics
- `rounded-lg`: `0.75rem` / `1.0rem` (`12px`–`16px`) — Interactive buttons, text fields, segmented tabs
- `rounded-xl`: `1.5rem` (`24px`) — Financial ledger cards, analytics pods, bottom sheets
- `rounded-full`: `9999px` — FAB buttons, avatar badges, status orbs

---

## 7. Component Guidelines

### Buttons
- **Primary Telemetry Action:** Gradient `linear-gradient(135deg, #06B6D4 0%, #0891B2 100%)`, text `#080A0F`, 16px radius, height 52px, outer bloom `0 4px 20px rgba(6, 182, 212, 0.35)`.
- **Secondary Glass Action:** Fill `rgba(255, 255, 255, 0.04)`, border `1px solid rgba(255, 255, 255, 0.12)`, text `#FFFFFF`.
- **Destructive Action:** Low-alpha rose fill `rgba(244, 63, 94, 0.08)`, border `1px solid rgba(244, 63, 94, 0.25)`, text `#F43F5E`.

### Cards & Ledger Pods
- **Hero Balance Card:** Violet-to-cyan gradient underlay (`rgba(139, 92, 246, 0.15)` to `rgba(6, 182, 212, 0.05)`), 24px blur, JetBrains Mono currency metric.
- **Glass Ledger Card:** Obsidian base `rgba(14, 18, 26, 0.75)`, 20px padding, 24px radius, 1px white specular border.

### Input Fields
- **Container:** Dark inset `rgba(8, 10, 15, 0.85)`, height 52px, radius 16px, border `1px solid rgba(255, 255, 255, 0.08)`.
- **Focus State:** Border changes to `1px solid #06B6D4` with ambient halo `0 0 12px rgba(6, 182, 212, 0.25)`.

### Financial Telemetry Widgets
- **Safe-to-Spend Velocity Gauge:** Semi-circular arc track in `rgba(255, 255, 255, 0.05)` with electric cyan-to-violet gradient progress fill.
- **Micro Trendspark:** Monoline SVGs with gradient drop fade (Emerald for positive income/savings, Crimson for high expense spikes).

---

## 8. Stitch Project Screen Catalog

| Screen Title | Screen ID | Type / Resolution |
| :--- | :--- | :--- |
| **Spendora - Overview Dashboard** | `0c47a08bea0c4f2090f3566042a633a6` | Mobile (`780 x 2872`) |
| **Spendora - Budgets & Limits** | `8f03809566d5409885a5f6bb312d5db3` | Mobile (`780 x 2958`) |
| **Spendora - Record Expense** | `028f4de62c9f491784ce151deea3d778` | Mobile (`780 x 2100`) |
| **Spendora - Analytics & Insights** | `c6ecfb195223483fa596d30ea117430b` | Mobile (`780 x 3300`) |
| **Spendora Logo (Emblem Vector)** | `40ae75427a0346f986c5224a3ab5383c` | Vector Icon (`512 x 512`) |
| **Tech Professional 3D Avatar** | `a978f361a9a044ff80165b952c3e6b46` | Portrait Asset (`1024 x 1024`) |
