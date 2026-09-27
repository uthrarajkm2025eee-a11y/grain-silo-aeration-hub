import React from 'react';
import {
  SiloIcon,
  FanIcon,
  MoistureIcon,
  LedgerIcon,
  DollarIcon,
  ContactsIcon,
  ProductsIcon,
  ReportsIcon,
  BudgetIcon,
  LogoutIcon
} from './Icons';

export const Sidebar = ({ activePage, setActivePage, user, onLogout }) => {
  const isAdmin = user?.role === 'ADMIN';

  return (
    <aside className="sidebar">
      <div className="sidebar-header">
        <div className="logo-icon">
          <SiloIcon size={20} />
        </div>
        <div className="sidebar-brand">
          SILO AERATION
          <span>QUALITY HUB v1.0</span>
        </div>
      </div>

      <nav className="sidebar-nav">
        <div className="nav-section-title">Operations</div>
        <button
          className={`nav-item ${activePage === 'dashboard' ? 'active' : ''}`}
          onClick={() => setActivePage('dashboard')}
        >
          <SiloIcon />
          <span>Dashboard</span>
        </button>

        <button
          className={`nav-item ${activePage === 'telemetry' ? 'active' : ''}`}
          onClick={() => setActivePage('telemetry')}
        >
          <FanIcon />
          <span>Telemetry & Fans</span>
        </button>

        <button
          className={`nav-item ${activePage === 'silos' ? 'active' : ''}`}
          onClick={() => setActivePage('silos')}
        >
          <MoistureIcon />
          <span>Silos & Thresholds</span>
        </button>

        <div className="nav-section-title">Master Data</div>
        <button
          className={`nav-item ${activePage === 'contacts' ? 'active' : ''}`}
          onClick={() => setActivePage('contacts')}
        >
          <ContactsIcon />
          <span>Contacts (Co-Ops)</span>
        </button>

        <button
          className={`nav-item ${activePage === 'products' ? 'active' : ''}`}
          onClick={() => setActivePage('products')}
        >
          <ProductsIcon />
          <span>Products & Services</span>
        </button>

        <div className="nav-section-title">Finance & Orders</div>
        <button
          className={`nav-item ${activePage === 'purchase-orders' ? 'active' : ''}`}
          onClick={() => setActivePage('purchase-orders')}
        >
          <DollarIcon />
          <span>Purchase Orders</span>
        </button>

        <button
          className={`nav-item ${activePage === 'sales-orders' ? 'active' : ''}`}
          onClick={() => setActivePage('sales-orders')}
        >
          <DollarIcon />
          <span>Sales Orders</span>
        </button>

        <button
          className={`nav-item ${activePage === 'bills-invoices' ? 'active' : ''}`}
          onClick={() => setActivePage('bills-invoices')}
        >
          <LedgerIcon />
          <span>Bills & Invoices</span>
        </button>

        <button
          className={`nav-item ${activePage === 'payments' ? 'active' : ''}`}
          onClick={() => setActivePage('payments')}
        >
          <DollarIcon />
          <span>Payments Ledger</span>
        </button>

        <button
          className={`nav-item ${activePage === 'accounts' ? 'active' : ''}`}
          onClick={() => setActivePage('accounts')}
        >
          <LedgerIcon />
          <span>Chart of Accounts</span>
        </button>

        <div className="nav-section-title">Analytics</div>
        <button
          className={`nav-item ${activePage === 'budgets' ? 'active' : ''}`}
          onClick={() => setActivePage('budgets')}
        >
          <BudgetIcon />
          <span>Facility Budgets</span>
        </button>

        <button
          className={`nav-item ${activePage === 'reports' ? 'active' : ''}`}
          onClick={() => setActivePage('reports')}
        >
          <ReportsIcon />
          <span>Financial Reports</span>
        </button>
      </nav>

      <div className="sidebar-footer">
        <div className="user-info">
          <span className="user-name">{user?.fullName || 'User'}</span>
          <span className="user-role">{user?.role}</span>
        </div>
        <button className="btn btn-secondary btn-sm" onClick={onLogout} title="Logout">
          <LogoutIcon size={16} />
        </button>
      </div>
    </aside>
  );
};
