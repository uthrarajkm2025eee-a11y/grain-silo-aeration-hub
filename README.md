# Autonomous Agricultural Grain Silo Aeration & Quality Optimization Hub

A complete, production-quality full-stack web application designed for agricultural co-ops and silo facility operations directors to autonomously monitor grain moisture levels, manage aeration fan assets, enforce double-entry financial accounting, and analyze facility budgets.

---

## 🌾 Tech Stack & Architecture

### Backend
- **Framework:** Java 17+, Spring Boot 3.2.x, Spring Data JPA, Spring Security (JWT Stateless Authentication)
- **Database:** MySQL 8 (with Flyway database migrations `V1__init_schema.sql` and `V2__seed_data.sql`), plus out-of-the-box H2 fallback
- **Performance & DB Optimization:**
  - Tuned HikariCP connection pooling defaults (`maximum-pool-size: 20`, `minimum-idle: 5`)
  - Indexed composite keys for telemetry time-series: `(silo_id, timestamp DESC)` and foreign key indices across all tables
  - Enforced `JOIN FETCH` queries and DTO projections to prevent N+1 queries
  - Disabled Open-Session-In-View (`open-in-view: false`) to guarantee strict transactional boundaries

### Frontend
- **Framework:** React 18 + Vite
- **Design System:** Custom hand-crafted CSS design system (`design-system.css`) — no generic component libraries or AI-slop templates.
- **Color Palette:** Domain-tailored Amber/Wheat/Soil-Brown industrial dark theme (`#12100e` dark soil bg, `#d97706` amber accents, `#fbbf24` harvest gold, `#10b981` grain green active indicators).
- **Typography:** Google Fonts pairing: **Space Grotesk** (Industrial headings) + **Inter** (Body) + **JetBrains Mono** (Ledger & telemetry metrics).
- **Icons & Visualization:** Custom inline SVG icons + Recharts line and bar charts styled to match the dark amber industrial palette.

---

## 🔐 Actors & Auth Roles

| Role | Username | Password | Permissions |
|---|---|---|---|
| **Admin (Silo Operations Director)** | `admin` | `admin123` | Full access: climate thresholds, fan override, financial reports, budgets |
| **Invoicing User (Facility Accountant)** | `accountant` | `acc123` | Financial access: Purchase Orders, Sales Orders, Bills, Invoices, Payments, Ledger |

---

## ⚡ Key Features

1. **Telemetry Ingestion & Automated Aeration Control:**
   - Synchronous evaluation of aeration rules on telemetry insert:
     - `Fan ON` if `moisturePercent > targetMoistureThreshold` (default 14.5%) AND `temperatureCelsius` is within the safe atmospheric window (`10.0°C – 25.0°C`).
     - `Fan OFF` if moisture target is achieved or ambient temperature exits safe window.
   - Comprehensive audit log (`fan_events`) tracking `action` (ON/OFF), `triggeredBy` (SYSTEM/MANUAL), `telemetryId`, and `reason`.
   - Toggleable simulated telemetry generator (`@Scheduled`) for dynamic live demo testing.

2. **Strict Double-Entry Accounting Engine:**
   - Automated double-entry Journal Entry posting on:
     - Purchase Order → Vendor Bill conversion (`Debit Expense, Credit Vendor Payables`)
     - Sales Order → Customer Invoice conversion (`Debit Accounts Receivable, Credit Conditioning Revenue`)
     - Inbound/Outbound Payment settlements (`Debit/Credit Cash Bank`)
   - Enforced validation check at service layer: `debits == credits` (`Math.abs(totalDebit - totalCredit) < 0.001`).

3. **Analytic Facility Budgeting & Backend Reports:**
   - Storage Facility Analytic Accounts ("Cluster A North", "Cluster B East").
   - Period budget allocations and real-time actual spend aggregation from posted journal items.
   - Live backend-computed **Balance Sheet** (Assets vs Liabilities & Net Equity) and **Profit & Loss Statement** (Revenues minus Expenses with date range filtering).

---

## 🚀 Getting Started

### 1. Backend Setup & Running

#### Option A: Zero-Config Run (Using H2 Profile out-of-the-box)
```bash
cd backend
mvn spring-boot:run
```
The backend starts at `http://localhost:8080` and seeds the database automatically using Flyway migrations.

#### Option B: MySQL 8 Mode
Ensure MySQL 8 is running locally and a database named `silo_db` exists, then run:
```bash
cd backend
mvn spring-boot:run -Dspring-boot.run.profiles=mysql
```
Or pass environment variables: `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, `DB_PASS`.

---

### 2. Frontend Setup & Running

```bash
cd frontend
npm install
npm run dev
```
The frontend dev server launches at `http://localhost:5173`. Open your browser and log in with the admin or accountant credentials above.

---

## 🧪 Postman API Collection

A ready-to-import Postman collection is included in the project root:
- File: `silo_aeration_hub_postman_collection.json`
- Includes pre-configured requests for Authentication, Silos & Telemetry, Contacts, Products, Orders, Invoices, Payments, Budgets, and Financial Reports.

---

## 📡 API Endpoint Overview

- `POST /api/auth/login` - Authenticate user & get JWT token
- `GET /api/silos/units` - List all silo units & operational fan states
- `POST /api/silos/units` - Register new silo unit (Admin only)
- `PUT /api/silos/units/{id}/thresholds` - Update moisture & temperature thresholds (Admin only)
- `POST /api/silos/telemetry` - Ingest live telemetry reading & evaluate aeration rules
- `GET /api/silos/{id}/telemetry` - Retrieve paginated time-series telemetry data
- `GET /api/fan-events?siloId=` - View fan state change audit log
- `POST /api/fan-events/override` - Perform manual fan override (Admin only)
- `POST /api/purchase-orders/{id}/convert-to-bill` - Convert PO to Vendor Bill & auto-post double-entry JE
- `POST /api/sales-orders/{id}/convert-to-invoice` - Convert SO to Invoice & auto-post double-entry JE
- `POST /api/payments` - Process payment settlement & post bank journal entry
- `GET /api/reports/balance-sheet` - Backend-computed Balance Sheet
- `GET /api/reports/profit-and-loss?from=&to=` - Backend-computed P&L Statement
- `GET /api/budgets/{id}/variance` - Budget variance & actual spend breakdown
