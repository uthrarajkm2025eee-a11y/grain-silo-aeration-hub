-- Seed Data for Silo Aeration Hub

-- Passwords: admin -> admin123, accountant -> acc123
INSERT INTO users (username, password, full_name, role) VALUES 
('admin', '$2a$10$08fCX6zVqUtKuC6nmPLsze99Q54RbMoQZRIDMPcD86dDjYHHLeZvO', 'Silo Operations Director', 'ADMIN'),
('accountant', '$2a$10$SyhzLiEpx/mWeNcxQZvQweW038zrHCsixGodWCLIu74YQThjolQLO', 'Facility Accountant', 'ACCOUNTANT');

-- Silo Units
INSERT INTO silos (code, name, capacity_tons, crop_type, target_moisture_threshold, ideal_temp_min, ideal_temp_max, fan_state) VALUES
('SILO-A1', 'North Field Wheat Silo A1', 5000.0, 'Hard Red Winter Wheat', 14.5, 10.0, 25.0, 'OFF'),
('SILO-A2', 'North Field Corn Silo A2', 4500.0, 'Yellow Dent Corn', 15.0, 12.0, 24.0, 'OFF'),
('SILO-B1', 'East Prairie Soy Silo B1', 6000.0, 'Yellow Soybeans', 13.5, 10.0, 22.0, 'OFF'),
('SILO-B2', 'East Prairie Barley Silo B2', 3500.0, 'Malting Barley', 14.0, 8.0, 20.0, 'OFF');

-- Initial Telemetry Seed
INSERT INTO silo_telemetry (silo_id, moisture_percent, temperature_celsius, timestamp) VALUES
(1, 14.1, 18.5, NOW()),
(2, 15.6, 21.0, NOW()),
(3, 13.2, 16.0, NOW()),
(4, 14.8, 19.2, NOW());

-- Initial Fan Events Audit
INSERT INTO fan_events (silo_id, action, triggered_by, telemetry_id, reason, timestamp) VALUES
(2, 'ON', 'SYSTEM', 2, 'Moisture (15.6%) exceeded threshold (15.0%) within ideal temp range (21.0°C)', NOW());

UPDATE silos SET fan_state = 'ON', last_telemetry_at = NOW() WHERE id = 2;
UPDATE silos SET last_telemetry_at = NOW() WHERE id IN (1, 3, 4);

-- Contacts Master
INSERT INTO contacts (code, name, type, email, phone, address) VALUES
('CUST-001', 'Midwest Grain Co-Op Association', 'CUSTOMER', 'orders@midwestgrain.com', '+1-555-0192', '104 Harvest Way, Des Moines, IA'),
('CUST-002', 'Prairie Valley Farmers Cooperative', 'CUSTOMER', 'finance@prairievalley.org', '+1-555-0381', '789 Crop Line Road, Omaha, NE'),
('VEND-001', 'Apex Aeration & Fan Systems Inc.', 'VENDOR', 'support@apexaeration.com', '+1-555-0822', '45 Industrial Park Dr, Moline, IL'),
('VEND-002', 'AgriTech Sensor Solutions LLC', 'VENDOR', 'billing@agritechsensors.com', '+1-555-0911', '302 Tech Highway, Cedar Rapids, IA');

-- Products Master
INSERT INTO products (code, name, type, unit_price, tax_rate) VALUES
('SRV-COND', 'Grain Storage Conditioning (per Ton/Month)', 'SERVICE', 4.50, 5.00),
('SRV-RENT', 'Silo Storage Space Rental (per Month)', 'SERVICE', 1200.00, 5.00),
('GOODS-SENS', 'Silo Multi-Depth Moisture & Temp Sensor', 'GOODS', 650.00, 8.00),
('SRV-FAN-OVER', 'Aeration Fan Overhaul & Calibration', 'SERVICE', 1850.00, 0.00);

-- Analytic Accounts (Storage Facilities)
INSERT INTO analytic_accounts (id, code, name, description) VALUES
(1, 'ANALYTIC-A', 'Grain Silo Cluster A (North)', 'North Quad Silos A1 & A2 Facility'),
(2, 'ANALYTIC-B', 'Grain Silo Cluster B (East)', 'East Prairie Silos B1 & B2 Facility');

-- Chart of Accounts
-- ASSETS
INSERT INTO accounts (id, code, name, type, parent_id) VALUES
(10, '1000', 'Assets', 'ASSET', NULL),
(11, '1010', 'Silo Infrastructure', 'ASSET', 10),
(12, '1020', 'Aeration Fan Assets', 'ASSET', 10),
(13, '1030', 'Cash & Bank Account', 'ASSET', 10),
(14, '1040', 'Accounts Receivable (Customers)', 'ASSET', 10);

-- LIABILITIES
INSERT INTO accounts (id, code, name, type, parent_id) VALUES
(20, '2000', 'Liabilities', 'LIABILITY', NULL),
(21, '2010', 'Fan Vendor Creditors (Accounts Payable)', 'LIABILITY', 20),
(22, '2020', 'Power Utility Payables', 'LIABILITY', 20);

-- INCOME
INSERT INTO accounts (id, code, name, type, parent_id) VALUES
(30, '3000', 'Income', 'INCOME', NULL),
(31, '3010', 'Grain Conditioning Fees Revenue', 'INCOME', 30),
(32, '3020', 'Silo Storage Rental Revenue', 'INCOME', 30);

-- EXPENSES
INSERT INTO accounts (id, code, name, type, parent_id) VALUES
(40, '4000', 'Expenses', 'EXPENSE', NULL),
(41, '4010', 'Aeration Fan Electricity Costs', 'EXPENSE', 40),
(42, '4020', 'Sensor Maintenance Expenses', 'EXPENSE', 40);

-- Journals
INSERT INTO journals (id, code, name, type) VALUES
(1, 'SLS', 'Sales Journal', 'SALES'),
(2, 'PUR', 'Purchase Journal', 'PURCHASE'),
(3, 'CSH', 'Cash Journal', 'CASH'),
(4, 'BNK', 'Bank Journal', 'BANK');

-- Initial Balance Entry (Bank setup)
INSERT INTO journal_entries (id, entry_number, journal_id, entry_date, reference, state) VALUES
(1, 'JE/2026/0001', 4, CURRENT_DATE(), 'Capital Initialization', 'POSTED');

INSERT INTO journal_items (entry_id, account_id, analytic_account_id, label, debit, credit) VALUES
(1, 13, NULL, 'Initial Bank Deposit', 150000.00, 0.00),
(1, 11, 1, 'Initial Silo Asset Valuation', 250000.00, 0.00),
(1, 12, 2, 'Initial Aeration Fan Assets', 80000.00, 0.00),
(1, 32, NULL, 'Initial Capital Allocation Equity', 0.00, 480000.00);

-- Sample Budgets
INSERT INTO budgets (name, allocated_amount, account_id, analytic_account_id, start_date, end_date) VALUES
('Q3 Aeration Electricity Budget Cluster A', 8500.00, 41, 1, '2026-07-01', '2026-09-30'),
('Q3 Sensor & Fan Maintenance Cluster B', 6000.00, 42, 2, '2026-07-01', '2026-09-30');
