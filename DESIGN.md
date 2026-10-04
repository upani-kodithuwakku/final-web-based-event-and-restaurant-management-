# Restaurant & Event Management System — Frontend Design Guide
### Airbnb-Inspired Design System · Group 06 · SLIIT 2026

---

## 1. Design Philosophy

> **"Belong Anywhere"** — Warm, welcoming, and human-centered.

We draw direct inspiration from Airbnb's design language:
- **Warmth over coldness** — coral/rausch tones instead of cold blues
- **White space is intentional** — generous padding, breathing room
- **Cards are the core unit** — every data object lives in a card
- **Typography does the heavy lifting** — size hierarchy communicates importance
- **Micro-interactions delight** — hover states, smooth transitions, skeleton loaders

---

## 2. Color Palette

### Primary Brand Colors

| Token | Hex | Usage |
|-------|-----|-------|
| `--color-rausch` | `#FF5A5F` | Primary CTA buttons, active states, brand accent |
| `--color-rausch-dark` | `#D93025` | Hover on primary buttons |
| `--color-rausch-light` | `#FFEBEC` | Backgrounds, badges, soft highlights |
| `--color-babu` | `#00A699` | Success states, confirmed reservations, available |
| `--color-arches` | `#FC642D` | Warnings, pending status, secondary accent |
| `--color-hof` | `#484848` | Primary text |
| `--color-foggy` | `#767676` | Secondary text, captions |

### Neutral Palette

| Token | Hex | Usage |
|-------|-----|-------|
| `--color-white` | `#FFFFFF` | Card backgrounds, modal surfaces |
| `--color-gray-50` | `#F7F7F7` | Page background |
| `--color-gray-100` | `#EBEBEB` | Dividers, borders, skeleton |
| `--color-gray-200` | `#DDDDDD` | Input borders, card borders |
| `--color-gray-300` | `#B0B0B0` | Placeholder text |
| `--color-gray-900` | `#222222` | Headings, strong text |

### Semantic Status Colors

| Status | Color | Hex | Used For |
|--------|-------|-----|----------|
| CONFIRMED | Babu Green | `#00A699` | Reservation confirmed |
| PENDING | Arches Orange | `#FC642D` | Awaiting confirmation |
| CHECKED_IN | Blue | `#0066FF` | Guest arrived |
| COMPLETED | Gray | `#767676` | Done |
| CANCELLED | Red | `#FF5A5F` | Cancelled |
| NO_SHOW | Dark Gray | `#484848` | No show |
| AVAILABLE | Babu Green | `#00A699` | Table free |
| OCCUPIED | Rausch | `#FF5A5F` | Table in use |
| OUT_OF_SERVICE | Gray | `#B0B0B0` | Maintenance |

---

## 3. Typography

### Font Stack
```css
font-family: 'Circular', 'Cereal', -apple-system, BlinkMacSystemFont,
             'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
```
> Use **Inter** from Google Fonts as the free Airbnb Cereal substitute.

### Type Scale

| Name | Size | Weight | Line Height | Usage |
|------|------|--------|-------------|-------|
| `display` | 40px / 2.5rem | 800 | 1.1 | Hero headings |
| `h1` | 32px / 2rem | 700 | 1.2 | Page titles |
| `h2` | 26px / 1.625rem | 700 | 1.25 | Section headings |
| `h3` | 22px / 1.375rem | 600 | 1.3 | Card headings |
| `h4` | 18px / 1.125rem | 600 | 1.4 | Sub-headings |
| `body-lg` | 18px / 1.125rem | 400 | 1.6 | Large body text |
| `body` | 16px / 1rem | 400 | 1.5 | Default body |
| `body-sm` | 14px / 0.875rem | 400 | 1.5 | Secondary text |
| `caption` | 12px / 0.75rem | 400 | 1.4 | Labels, captions |
| `overline` | 11px / 0.6875rem | 600 | 1.4 | UPPERCASE labels |

---

## 4. Spacing System

Base unit: **8px**

```
4px   → xs   — icon gaps, tight padding
8px   → sm   — inline spacing
16px  → md   — default padding
24px  → lg   — card padding
32px  → xl   — section gaps
48px  → 2xl  — large section gaps
64px  → 3xl  — page-level gaps
96px  → 4xl  — hero sections
```

---

## 5. Border Radius

| Token | Value | Usage |
|-------|-------|-------|
| `--radius-sm` | `8px` | Badges, tags, chips |
| `--radius-md` | `12px` | Buttons, inputs, small cards |
| `--radius-lg` | `16px` | Cards, modals, panels |
| `--radius-xl` | `24px` | Feature cards, hero images |
| `--radius-full` | `9999px` | Pills, avatars, circular buttons |

---

## 6. Shadows (Airbnb Elevation)

```css
/* Level 1 — Subtle card lift */
--shadow-sm: 0 1px 2px rgba(0,0,0,0.08), 0 4px 12px rgba(0,0,0,0.05);

/* Level 2 — Card hover */
--shadow-md: 0 6px 16px rgba(0,0,0,0.12);

/* Level 3 — Dropdown, tooltip */
--shadow-lg: 0 8px 28px rgba(0,0,0,0.14), 0 2px 4px rgba(0,0,0,0.06);

/* Level 4 — Modal, overlay */
--shadow-xl: 0 16px 40px rgba(0,0,0,0.18);
```

---

## 7. Component Specifications

### 7.1 Buttons

#### Primary (Rausch)
```
Background : #FF5A5F
Text       : #FFFFFF
Padding    : 14px 24px
Border     : none
Radius     : 8px
Font       : 16px, weight 600
Hover BG   : #E0474C
Active BG  : #C73E43
Transition : background 0.2s ease
```

#### Secondary (Outline)
```
Background : transparent
Text       : #222222
Border     : 1px solid #222222
Padding    : 14px 24px
Radius     : 8px
Font       : 16px, weight 600
Hover BG   : #F7F7F7
```

#### Ghost / Text
```
Background : transparent
Text       : #FF5A5F
Border     : none
Padding    : 14px 24px
Hover      : underline
```

#### Button Sizes
| Size | Padding | Font |
|------|---------|------|
| `sm` | `10px 16px` | 14px |
| `md` | `14px 24px` | 16px |
| `lg` | `18px 32px` | 18px |

---

### 7.2 Cards

```
Background     : #FFFFFF
Border         : 1px solid #EBEBEB
Border Radius  : 16px
Padding        : 24px
Shadow         : --shadow-sm
Hover Shadow   : --shadow-md
Hover Transform: translateY(-2px)
Transition     : all 0.2s ease
```

#### Reservation Card Layout
```
┌─────────────────────────────────────┐
│  [Table Icon]  T03 · 4 Guests        │
│  ─────────────────────────────────  │
│  📅 Sat, 20 Sep 2026 · 7:00 PM      │
│  👤 Kasun Perera · 0771234567       │
│  🪑 Window Seat                      │
│                                     │
│  [CONFIRMED ●]        [Cancel] [→]  │
└─────────────────────────────────────┘
```

---

### 7.3 Form Inputs

```
Background      : #FFFFFF
Border          : 1px solid #DDDDDD
Border (focus)  : 2px solid #222222
Border Radius   : 8px
Padding         : 14px 16px
Font Size       : 16px
Color           : #222222
Placeholder     : #B0B0B0
Label           : 12px, weight 600, uppercase, #484848
```

#### Floating Label Input (Airbnb Style)
```
Default: Label sits inside input at 16px
Focus  : Label floats up → 11px, bold, #222222
Filled : Same as focus
Error  : Border → #FF5A5F, helper text below in #FF5A5F
```

---

### 7.4 Status Badges

```css
/* Base */
.badge {
  padding: 4px 10px;
  border-radius: 9999px;
  font-size: 12px;
  font-weight: 600;
  letter-spacing: 0.02em;
  text-transform: uppercase;
}

/* Variants */
.badge-confirmed   { background: #E6F7F6; color: #00A699; }
.badge-pending     { background: #FEF0EA; color: #FC642D; }
.badge-checked-in  { background: #EBF2FF; color: #0066FF; }
.badge-completed   { background: #F0F0F0; color: #767676; }
.badge-cancelled   { background: #FFEBEC; color: #FF5A5F; }
.badge-available   { background: #E6F7F6; color: #00A699; }
.badge-occupied    { background: #FFEBEC; color: #FF5A5F; }
```

---

### 7.5 Navigation

#### Top Navigation Bar
```
Height      : 80px
Background  : #FFFFFF
Border-bottom: 1px solid #EBEBEB
Shadow      : 0 1px 0 rgba(0,0,0,0.06)
Logo        : #FF5A5F rausch color
Links       : 14px, weight 600, #484848
Active Link : #222222 + bottom border 2px #FF5A5F
User Avatar : 32px circle, border 1px #DDDDDD
```

#### Sidebar (Staff/Admin)
```
Width       : 240px
Background  : #FFFFFF
Border-right: 1px solid #EBEBEB
Item Padding: 12px 20px
Item Radius : 8px
Active BG   : #FFF0F0  (rausch-light)
Active Text : #FF5A5F
Active Icon : #FF5A5F
Hover BG    : #F7F7F7
Icon Size   : 20px
```

---

### 7.6 Table / Floor Map

```
Table Card (Available)  : border 2px solid #00A699, bg #E6F7F6
Table Card (Reserved)   : border 2px solid #FC642D, bg #FEF0EA
Table Card (Occupied)   : border 2px solid #FF5A5F, bg #FFEBEC
Table Card (OOS)        : border 2px solid #DDDDDD, bg #F7F7F7, opacity 0.6
Table Shape             : border-radius 12px (rect) or 50% (round)
Size                    : 80×80px (2-top), 100×100px (4-top), 120×80px (6-top)
```

---

### 7.7 Modal / Drawer

```
Overlay     : rgba(0, 0, 0, 0.5)
Background  : #FFFFFF
Radius      : 16px 16px 0 0  (bottom sheet) or 16px (centered)
Max Width   : 600px (centered), 480px (drawer)
Header      : 24px bold, border-bottom 1px #EBEBEB, padding 24px
Body        : padding 24px
Footer      : border-top 1px #EBEBEB, padding 16px 24px
Close Button: top-right, 32px circle, hover bg #F7F7F7
```

---

### 7.8 Calendar / Date Picker

```
Airbnb-style dual calendar
Selected Start: #222222 bg, white text
Selected Range: #F7F7F7 bg, #222222 text
Selected End  : #222222 bg, white text
Hover         : #F7F7F7
Today         : underline or dot
Disabled      : #B0B0B0, strikethrough, pointer-events none
```

---

## 8. Page Layouts

### 8.1 Customer Dashboard
```
┌──────────────────────────────────────────────┐
│  NAV: Logo · Search · My Trips · Profile ↓   │
├──────────────────────────────────────────────┤
│                                              │
│  Hero: "Reserve your table tonight"          │
│  [Search Bar: Date | Time | Guests | Search] │
│                                              │
├──────────────────────────────────────────────┤
│  My Upcoming Reservations (horizontal scroll)│
│  [Card] [Card] [Card] →                      │
├──────────────────────────────────────────────┤
│  Available Tables                            │
│  [Filter: All | Window | Indoor | Outdoor]   │
│  [Card] [Card] [Card]                        │
│  [Card] [Card] [Card]                        │
└──────────────────────────────────────────────┘
```

### 8.2 Admin / Staff Dashboard
```
┌──────────┬───────────────────────────────────┐
│          │  Header: Today, Sep 13 · Good AM  │
│ SIDEBAR  ├───────────────────────────────────┤
│          │  Stats Row:                       │
│ Dashboard│  [Reservations] [Tables] [Revenue]│
│ Tables   │  [ Events ]     [Orders]          │
│ Reserv.  ├───────────────────────────────────┤
│ Events   │  Floor Map (Live Table Status)    │
│ Orders   │  [T01●] [T02●] [T03●] [T04●]     │
│ Staff    │  [T05●] [T06●] [T07●] [T08●]     │
│ Inventory├───────────────────────────────────┤
│ Reports  │  Today's Reservations Timeline    │
│          │  [11:00 - Kasun ·T03· 4pax ✓]    │
│ Settings │  [13:00 - Nimal ·T07· 6pax ●]    │
└──────────┴───────────────────────────────────┘
```

### 8.3 Reservation Booking Flow (3 Steps)
```
Step 1: Search
  → Date Picker (Airbnb dual calendar)
  → Time Picker (dropdown: 11:00 AM slots)
  → Guest Count (stepper: − 2 +)

Step 2: Choose Table
  → Available table cards (grid)
  → Card: Table number, capacity, location, image

Step 3: Confirm
  → Summary card
  → Contact details form
  → Special requests textarea
  → [Confirm Reservation] button
```

---

## 9. Icons

Use **Heroicons** (outline style) — matches Airbnb's clean line icon style.

```bash
npm install @heroicons/react
```

| Use Case | Icon |
|----------|------|
| Table | `TableCellsIcon` |
| Calendar | `CalendarDaysIcon` |
| Clock | `ClockIcon` |
| Users / Guests | `UsersIcon` |
| Location | `MapPinIcon` |
| Check / Confirm | `CheckCircleIcon` |
| Cancel / X | `XCircleIcon` |
| Menu / Food | `CakeIcon` |
| Events | `SparklesIcon` |
| Billing | `CreditCardIcon` |
| Inventory | `ArchiveBoxIcon` |
| Staff | `UserGroupIcon` |
| Settings | `Cog6ToothIcon` |
| Notification | `BellIcon` |
| Search | `MagnifyingGlassIcon` |

---

## 10. Animations & Transitions

```css
/* Standard transition */
transition: all 0.2s ease;

/* Card hover lift */
card:hover {
  transform: translateY(-4px);
  box-shadow: var(--shadow-md);
}

/* Button press */
button:active {
  transform: scale(0.97);
}

/* Page enter */
@keyframes fadeInUp {
  from { opacity: 0; transform: translateY(16px); }
  to   { opacity: 1; transform: translateY(0);    }
}
.page-enter { animation: fadeInUp 0.3s ease; }

/* Skeleton loader */
@keyframes shimmer {
  from { background-position: -200% 0; }
  to   { background-position:  200% 0; }
}
.skeleton {
  background: linear-gradient(90deg, #EBEBEB 25%, #F7F7F7 50%, #EBEBEB 75%);
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
  border-radius: 8px;
}
```

---

## 11. Responsive Breakpoints

| Breakpoint | Width | Layout |
|------------|-------|--------|
| `mobile` | < 640px | Single column, bottom navigation |
| `tablet` | 640–1024px | 2-column grid, collapsible sidebar |
| `desktop` | 1024–1280px | 3-column grid, full sidebar |
| `wide` | > 1280px | Max-width 1280px, centered |

```css
/* Tailwind config */
screens: {
  sm:  '640px',
  md:  '768px',
  lg:  '1024px',
  xl:  '1280px',
}
```

---

## 12. Tailwind CSS Config

```js
// tailwind.config.js
export default {
  content: ['./src/**/*.{js,ts,jsx,tsx}'],
  theme: {
    extend: {
      colors: {
        rausch:   { DEFAULT: '#FF5A5F', dark: '#D93025', light: '#FFEBEC' },
        babu:     { DEFAULT: '#00A699' },
        arches:   { DEFAULT: '#FC642D' },
        hof:      { DEFAULT: '#484848' },
        foggy:    { DEFAULT: '#767676' },
      },
      fontFamily: {
        sans: ['Inter', 'system-ui', 'sans-serif'],
      },
      boxShadow: {
        'card':       '0 1px 2px rgba(0,0,0,0.08), 0 4px 12px rgba(0,0,0,0.05)',
        'card-hover': '0 6px 16px rgba(0,0,0,0.12)',
        'modal':      '0 16px 40px rgba(0,0,0,0.18)',
      },
      borderRadius: {
        'xl2': '16px',
        'xl3': '24px',
      },
    },
  },
}
```

---

## 13. Key UI Patterns

### Search Bar (Airbnb pill style)
```
┌──────────────────────────────────────────────┐
│  📅 Date  │  🕐 Time  │  👥 Guests │ [Search]│
└──────────────────────────────────────────────┘
Border-radius: 9999px (pill)
Background: white
Shadow: --shadow-md
Height: 64px
Dividers: 1px vertical #EBEBEB
Search button: #FF5A5F circle (48px), white search icon
```

### Stat Card (Dashboard)
```
┌──────────────────┐
│  🗓             │
│  24              │  ← Large number, 32px bold
│  Today's         │  ← Label, 14px #767676
│  Reservations    │
│  ↑ 12% vs yesterday│  ← Trend, 12px green/red
└──────────────────┘
```

### Empty State
```
     [Illustration / Icon]
     No reservations yet
     Book your first table to get started
     [Make a Reservation →]
```

---

## 14. Frontend Stack Summary

```
Framework    : React 18 + Vite
Language     : TypeScript
Styling      : Tailwind CSS
Icons        : @heroicons/react
HTTP Client  : Axios
Routing      : React Router v6
State        : React Context + useState
Date/Time    : date-fns
Forms        : React Hook Form + Zod validation
Notifications: react-hot-toast (Airbnb-style toasts)
Calendar     : react-day-picker (styled to match)
```

---

## 15. File Structure (Frontend)

```
restaurant-event-frontend/
├── src/
│   ├── assets/
│   │   └── logo.svg
│   ├── components/
│   │   ├── ui/             ← Reusable: Button, Card, Badge, Input, Modal
│   │   ├── layout/         ← Navbar, Sidebar, Footer
│   │   └── features/       ← ReservationCard, TableMap, BookingForm
│   ├── pages/
│   │   ├── auth/           ← Login, Register
│   │   ├── customer/       ← Dashboard, Reservations, Events, Profile
│   │   ├── admin/          ← Dashboard, Tables, Staff, Reports
│   │   └── shared/         ← NotFound, Unauthorized
│   ├── hooks/              ← useAuth, useReservations, useNotifications
│   ├── services/           ← api.ts, auth.service.ts, reservation.service.ts
│   ├── context/            ← AuthContext, NotificationContext
│   ├── types/              ← Reservation, User, Table interfaces
│   └── utils/              ← formatDate, formatCurrency, cn()
```

---

*Design System v1.0 · Restaurant & Event Management System · Group 06 · SLIIT 2026*
