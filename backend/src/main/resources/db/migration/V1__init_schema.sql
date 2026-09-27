-- Schema initialization for Silo Aeration Hub

-- Users table
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    role VARCHAR(20) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_users_username ON users(username);

-- Silos table
CREATE TABLE silos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(30) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    capacity_tons DOUBLE NOT NULL,
    crop_type VARCHAR(50) NOT NULL,
    target_moisture_threshold DOUBLE NOT NULL DEFAULT 14.5,
    ideal_temp_min DOUBLE NOT NULL DEFAULT 10.0,
    ideal_temp_max DOUBLE NOT NULL DEFAULT 25.0,
    fan_state VARCHAR(10) NOT NULL DEFAULT 'OFF',
    last_telemetry_at TIMESTAMP NULL
);
CREATE INDEX idx_silos_code ON silos(code);

-- Telemetry table (Time-series data, heavily indexed on silo_id + timestamp)
CREATE TABLE silo_telemetry (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    silo_id BIGINT NOT NULL,
    moisture_percent DOUBLE NOT NULL,
    temperature_celsius DOUBLE NOT NULL,
    timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_telemetry_silo FOREIGN KEY (silo_id) REFERENCES silos(id) ON DELETE CASCADE
);
CREATE INDEX idx_telemetry_silo_ts ON silo_telemetry(silo_id, timestamp DESC);
CREATE INDEX idx_telemetry_ts ON silo_telemetry(timestamp DESC);

-- Fan Events Audit Trail table
CREATE TABLE fan_events (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    silo_id BIGINT NOT NULL,
    action VARCHAR(10) NOT NULL, -- ON, OFF
    triggered_by VARCHAR(20) NOT NULL, -- SYSTEM, MANUAL
    telemetry_id BIGINT NULL,
    reason VARCHAR(255) NOT NULL,
    timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_fanevent_silo FOREIGN KEY (silo_id) REFERENCES silos(id) ON DELETE CASCADE
);
CREATE INDEX idx_fanevent_silo_ts ON fan_events(silo_id, timestamp DESC);

-- Contact Master (Customers & Vendors)
CREATE TABLE contacts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(30) NOT NULL UNIQUE,
    name VARCHAR(120) NOT NULL,
    type VARCHAR(20) NOT NULL, -- CUSTOMER, VENDOR
    email VARCHAR(100),
    phone VARCHAR(30),
    address VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_contacts_type ON contacts(type);

-- Product Master
CREATE TABLE products (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(30) NOT NULL UNIQUE,
    name VARCHAR(120) NOT NULL,
    type VARCHAR(20) NOT NULL, -- SERVICE, GOODS
    unit_price DECIMAL(12, 2) NOT NULL,
    tax_rate DECIMAL(5, 2) NOT NULL DEFAULT 0.00,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Analytic Account (Storage Facilities / Silo Clusters for budget tracking)
CREATE TABLE analytic_accounts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(30) NOT NULL UNIQUE,
    name VARCHAR(120) NOT NULL,
    description VARCHAR(255)
);

-- Chart of Accounts
CREATE TABLE accounts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(30) NOT NULL UNIQUE,
    name VARCHAR(120) NOT NULL,
    type VARCHAR(20) NOT NULL, -- ASSET, LIABILITY, INCOME, EXPENSE
    parent_id BIGINT NULL,
    CONSTRAINT fk_account_parent FOREIGN KEY (parent_id) REFERENCES accounts(id) ON DELETE SET NULL
);
CREATE INDEX idx_accounts_type ON accounts(type);

-- Journals
CREATE TABLE journals (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(10) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    type VARCHAR(20) NOT NULL -- SALES, PURCHASE, CASH, BANK
);

-- Journal Entries (Header)
CREATE TABLE journal_entries (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    entry_number VARCHAR(50) NOT NULL UNIQUE,
    journal_id BIGINT NOT NULL,
    entry_date DATE NOT NULL,
    reference VARCHAR(100),
    state VARCHAR(20) NOT NULL DEFAULT 'POSTED',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_entry_journal FOREIGN KEY (journal_id) REFERENCES journals(id)
);
CREATE INDEX idx_entry_date ON journal_entries(entry_date);

-- Journal Items (Lines - Double entry items)
CREATE TABLE journal_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    entry_id BIGINT NOT NULL,
    account_id BIGINT NOT NULL,
    analytic_account_id BIGINT NULL,
    label VARCHAR(200),
    debit DECIMAL(12, 2) NOT NULL DEFAULT 0.00,
    credit DECIMAL(12, 2) NOT NULL DEFAULT 0.00,
    CONSTRAINT fk_item_entry FOREIGN KEY (entry_id) REFERENCES journal_entries(id) ON DELETE CASCADE,
    CONSTRAINT fk_item_account FOREIGN KEY (account_id) REFERENCES accounts(id),
    CONSTRAINT fk_item_analytic FOREIGN KEY (analytic_account_id) REFERENCES analytic_accounts(id)
);
CREATE INDEX idx_item_entry ON journal_items(entry_id);
CREATE INDEX idx_item_account ON journal_items(account_id);
CREATE INDEX idx_item_analytic ON journal_items(analytic_account_id);

-- Purchase Orders
CREATE TABLE purchase_orders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    po_number VARCHAR(50) NOT NULL UNIQUE,
    vendor_id BIGINT NOT NULL,
    order_date DATE NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT', -- DRAFT, CONFIRMED, BILLED, PAID
    total_amount DECIMAL(12, 2) NOT NULL DEFAULT 0.00,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_po_vendor FOREIGN KEY (vendor_id) REFERENCES contacts(id)
);

CREATE TABLE purchase_order_lines (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    po_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity INT NOT NULL,
    unit_price DECIMAL(12, 2) NOT NULL,
    tax_rate DECIMAL(5, 2) NOT NULL DEFAULT 0.00,
    subtotal DECIMAL(12, 2) NOT NULL,
    CONSTRAINT fk_pol_po FOREIGN KEY (po_id) REFERENCES purchase_orders(id) ON DELETE CASCADE,
    CONSTRAINT fk_pol_product FOREIGN KEY (product_id) REFERENCES products(id)
);

-- Vendor Bills
CREATE TABLE vendor_bills (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    bill_number VARCHAR(50) NOT NULL UNIQUE,
    po_id BIGINT NULL,
    vendor_id BIGINT NOT NULL,
    bill_date DATE NOT NULL,
    due_date DATE NOT NULL,
    total_amount DECIMAL(12, 2) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'UNPAID', -- UNPAID, PAID
    journal_entry_id BIGINT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_bill_vendor FOREIGN KEY (vendor_id) REFERENCES contacts(id),
    CONSTRAINT fk_bill_po FOREIGN KEY (po_id) REFERENCES purchase_orders(id),
    CONSTRAINT fk_bill_je FOREIGN KEY (journal_entry_id) REFERENCES journal_entries(id)
);

-- Sales Orders
CREATE TABLE sales_orders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    so_number VARCHAR(50) NOT NULL UNIQUE,
    customer_id BIGINT NOT NULL,
    order_date DATE NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT', -- DRAFT, CONFIRMED, INVOICED, PAID
    total_amount DECIMAL(12, 2) NOT NULL DEFAULT 0.00,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_so_customer FOREIGN KEY (customer_id) REFERENCES contacts(id)
);

CREATE TABLE sales_order_lines (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    so_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity INT NOT NULL,
    unit_price DECIMAL(12, 2) NOT NULL,
    tax_rate DECIMAL(5, 2) NOT NULL DEFAULT 0.00,
    subtotal DECIMAL(12, 2) NOT NULL,
    CONSTRAINT fk_sol_so FOREIGN KEY (so_id) REFERENCES sales_orders(id) ON DELETE CASCADE,
    CONSTRAINT fk_sol_product FOREIGN KEY (product_id) REFERENCES products(id)
);

-- Customer Invoices
CREATE TABLE customer_invoices (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    invoice_number VARCHAR(50) NOT NULL UNIQUE,
    so_id BIGINT NULL,
    customer_id BIGINT NOT NULL,
    invoice_date DATE NOT NULL,
    due_date DATE NOT NULL,
    total_amount DECIMAL(12, 2) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'UNPAID', -- UNPAID, PAID
    journal_entry_id BIGINT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_inv_customer FOREIGN KEY (customer_id) REFERENCES contacts(id),
    CONSTRAINT fk_inv_so FOREIGN KEY (so_id) REFERENCES sales_orders(id),
    CONSTRAINT fk_inv_je FOREIGN KEY (journal_entry_id) REFERENCES journal_entries(id)
);

-- Payments
CREATE TABLE payments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    payment_number VARCHAR(50) NOT NULL UNIQUE,
    payment_date DATE NOT NULL,
    amount DECIMAL(12, 2) NOT NULL,
    payment_type VARCHAR(20) NOT NULL, -- INBOUND (Customer receipt), OUTBOUND (Vendor payment)
    partner_id BIGINT NOT NULL,
    bank_account_id BIGINT NOT NULL,
    bill_id BIGINT NULL,
    invoice_id BIGINT NULL,
    journal_entry_id BIGINT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_pay_partner FOREIGN KEY (partner_id) REFERENCES contacts(id),
    CONSTRAINT fk_pay_bank FOREIGN KEY (bank_account_id) REFERENCES accounts(id),
    CONSTRAINT fk_pay_bill FOREIGN KEY (bill_id) REFERENCES vendor_bills(id),
    CONSTRAINT fk_pay_invoice FOREIGN KEY (invoice_id) REFERENCES customer_invoices(id),
    CONSTRAINT fk_pay_je FOREIGN KEY (journal_entry_id) REFERENCES journal_entries(id)
);

-- Budgets
CREATE TABLE budgets (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(120) NOT NULL,
    allocated_amount DECIMAL(12, 2) NOT NULL,
    account_id BIGINT NOT NULL,
    analytic_account_id BIGINT NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    CONSTRAINT fk_budget_account FOREIGN KEY (account_id) REFERENCES accounts(id),
    CONSTRAINT fk_budget_analytic FOREIGN KEY (analytic_account_id) REFERENCES analytic_accounts(id)
);
