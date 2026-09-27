const BASE_URL = '/api';

export const getAuthToken = () => localStorage.getItem('token');
export const setAuthToken = (token) => localStorage.setItem('token', token);
export const removeAuthToken = () => {
  localStorage.removeItem('token');
  localStorage.removeItem('user');
};
export const getUser = () => {
  const u = localStorage.getItem('user');
  return u ? JSON.parse(u) : null;
};
export const setUser = (user) => localStorage.setItem('user', JSON.stringify(user));

export const apiRequest = async (endpoint, options = {}) => {
  const token = getAuthToken();
  const headers = {
    'Content-Type': 'application/json',
    ...options.headers,
  };

  if (token) {
    headers['Authorization'] = `Bearer ${token}`;
  }

  const response = await fetch(`${BASE_URL}${endpoint}`, {
    ...options,
    headers,
  });

  if (response.status === 401) {
    removeAuthToken();
    window.location.href = '/login';
    throw new Error('Unauthorized');
  }

  const data = await response.json().catch(() => ({}));

  if (!response.ok) {
    const errorMsg = data.message || data.error || 'API Request failed';
    throw new Error(errorMsg);
  }

  return data;
};

// API Services
export const authApi = {
  login: (credentials) => apiRequest('/auth/login', { method: 'POST', body: JSON.stringify(credentials) }),
};

export const silosApi = {
  getUnits: () => apiRequest('/silos/units'),
  getUnitById: (id) => apiRequest(`/silos/units/${id}`),
  createUnit: (data) => apiRequest('/silos/units', { method: 'POST', body: JSON.stringify(data) }),
  updateThresholds: (id, targetMoisture, idealTempMin, idealTempMax) =>
    apiRequest(`/silos/units/${id}/thresholds?targetMoisture=${targetMoisture}&idealTempMin=${idealTempMin}&idealTempMax=${idealTempMax}`, { method: 'PUT' }),
  ingestTelemetry: (data) => apiRequest('/silos/telemetry', { method: 'POST', body: JSON.stringify(data) }),
  getTelemetryPaged: (siloId, page = 0, size = 20) =>
    apiRequest(siloId ? `/silos/${siloId}/telemetry?page=${page}&size=${size}` : `/silos/telemetry/all?page=${page}&size=${size}`),
  toggleSimulator: (enabled) => apiRequest(`/silos/telemetry/simulator/toggle?enabled=${enabled}`, { method: 'POST' }),
  getSimulatorStatus: () => apiRequest('/silos/telemetry/simulator/status'),
};

export const fanEventsApi = {
  getEvents: (siloId) => apiRequest(siloId ? `/fan-events?siloId=${siloId}` : '/fan-events'),
  manualOverride: (data) => apiRequest('/fan-events/override', { method: 'POST', body: JSON.stringify(data) }),
};

export const masterDataApi = {
  getContacts: (type) => apiRequest(type ? `/contacts?type=${type}` : '/contacts'),
  createContact: (data) => apiRequest('/contacts', { method: 'POST', body: JSON.stringify(data) }),
  getProducts: () => apiRequest('/products'),
  createProduct: (data) => apiRequest('/products', { method: 'POST', body: JSON.stringify(data) }),
};

export const accountingApi = {
  getAccounts: () => apiRequest('/accounts'),
  getAnalyticAccounts: () => apiRequest('/analytic-accounts'),
  getJournals: () => apiRequest('/journals'),
  getJournalEntries: (page = 0, size = 20) => apiRequest(`/journal-entries?page=${page}&size=${size}`),
  createJournalEntry: (data) => apiRequest('/journal-entries', { method: 'POST', body: JSON.stringify(data) }),
};

export const ordersApi = {
  getPurchaseOrders: (page = 0, size = 20) => apiRequest(`/purchase-orders?page=${page}&size=${size}`),
  createPurchaseOrder: (data) => apiRequest('/purchase-orders', { method: 'POST', body: JSON.stringify(data) }),
  convertToBill: (poId) => apiRequest(`/purchase-orders/${poId}/convert-to-bill`, { method: 'POST' }),
  getVendorBills: (page = 0, size = 20) => apiRequest(`/vendor-bills?page=${page}&size=${size}`),

  getSalesOrders: (page = 0, size = 20) => apiRequest(`/sales-orders?page=${page}&size=${size}`),
  createSalesOrder: (data) => apiRequest('/sales-orders', { method: 'POST', body: JSON.stringify(data) }),
  convertToInvoice: (soId) => apiRequest(`/sales-orders/${soId}/convert-to-invoice`, { method: 'POST' }),
  getCustomerInvoices: (page = 0, size = 20) => apiRequest(`/customer-invoices?page=${page}&size=${size}`),
};

export const paymentsApi = {
  getPayments: (page = 0, size = 20) => apiRequest(`/payments?page=${page}&size=${size}`),
  createPayment: (data) => apiRequest('/payments', { method: 'POST', body: JSON.stringify(data) }),
};

export const budgetApi = {
  getBudgets: () => apiRequest('/budgets'),
  createBudget: (data) => apiRequest('/budgets', { method: 'POST', body: JSON.stringify(data) }),
  getVariance: (id) => apiRequest(`/budgets/${id}/variance`),
  getAllVariances: () => apiRequest('/budgets/variances'),
};

export const reportsApi = {
  getBalanceSheet: () => apiRequest('/reports/balance-sheet'),
  getProfitAndLoss: (from, to) => apiRequest(from && to ? `/reports/profit-and-loss?from=${from}&to=${to}` : '/reports/profit-and-loss'),
  getBudgetSummary: () => apiRequest('/reports/budget-summary'),
};
