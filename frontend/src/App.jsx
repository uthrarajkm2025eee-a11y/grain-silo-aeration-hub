import React, { useState, useEffect } from 'react';
import { getAuthToken, getUser, removeAuthToken } from './services/api';
import { Sidebar } from './components/Sidebar';
import { LoginPage } from './pages/LoginPage';
import { DashboardPage } from './pages/DashboardPage';
import { TelemetryPage } from './pages/TelemetryPage';
import { SilosPage } from './pages/SilosPage';
import { ContactsPage } from './pages/ContactsPage';
import { ProductsPage } from './pages/ProductsPage';
import { AccountsPage } from './pages/AccountsPage';
import { PurchaseOrdersPage } from './pages/PurchaseOrdersPage';
import { SalesOrdersPage } from './pages/SalesOrdersPage';
import { BillsInvoicesPage } from './pages/BillsInvoicesPage';
import { PaymentsPage } from './pages/PaymentsPage';
import { BudgetsPage } from './pages/BudgetsPage';
import { ReportsPage } from './pages/ReportsPage';
import './styles/design-system.css';

export function App() {
  const [user, setUserState] = useState(getUser());
  const [activePage, setActivePage] = useState('dashboard');

  useEffect(() => {
    const u = getUser();
    if (u) setUserState(u);
  }, []);

  const handleLoginSuccess = (loginData) => {
    setUserState({
      username: loginData.username,
      fullName: loginData.fullName,
      role: loginData.role,
    });
    setActivePage('dashboard');
  };

  const handleLogout = () => {
    removeAuthToken();
    setUserState(null);
  };

  if (!user || !getAuthToken()) {
    return <LoginPage onLoginSuccess={handleLoginSuccess} />;
  }

  const renderContent = () => {
    switch (activePage) {
      case 'dashboard':
        return <DashboardPage setActivePage={setActivePage} />;
      case 'telemetry':
        return <TelemetryPage user={user} />;
      case 'silos':
        return <SilosPage user={user} />;
      case 'contacts':
        return <ContactsPage />;
      case 'products':
        return <ProductsPage />;
      case 'accounts':
        return <AccountsPage />;
      case 'purchase-orders':
        return <PurchaseOrdersPage />;
      case 'sales-orders':
        return <SalesOrdersPage />;
      case 'bills-invoices':
        return <BillsInvoicesPage setActivePage={setActivePage} />;
      case 'payments':
        return <PaymentsPage />;
      case 'budgets':
        return <BudgetsPage />;
      case 'reports':
        return <ReportsPage />;
      default:
        return <DashboardPage setActivePage={setActivePage} />;
    }
  };

  return (
    <div className="app-container">
      <Sidebar
        activePage={activePage}
        setActivePage={setActivePage}
        user={user}
        onLogout={handleLogout}
      />
      <main className="main-content">
        {renderContent()}
      </main>
    </div>
  );
}

export default App;
