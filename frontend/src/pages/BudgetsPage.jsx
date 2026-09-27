import React, { useEffect, useState } from 'react';
import { budgetApi, accountingApi } from '../services/api';
import { ResponsiveContainer, BarChart, Bar, XAxis, YAxis, Tooltip, CartesianGrid, Legend } from 'recharts';

export const BudgetsPage = () => {
  const [variances, setVariances] = useState([]);
  const [accounts, setAccounts] = useState([]);
  const [analytics, setAnalytics] = useState([]);
  const [showModal, setShowModal] = useState(false);

  const [name, setName] = useState('');
  const [allocatedAmount, setAllocatedAmount] = useState(10000.0);
  const [accountId, setAccountId] = useState(41);
  const [analyticAccountId, setAnalyticAccountId] = useState(1);
  const [startDate, setStartDate] = useState('2026-07-01');
  const [endDate, setEndDate] = useState('2026-09-30');

  const fetchData = async () => {
    try {
      const [vars, accs, anas] = await Promise.all([
        budgetApi.getAllVariances(),
        accountingApi.getAccounts(),
        accountingApi.getAnalyticAccounts(),
      ]);
      setVariances(vars);
      setAccounts(accs.filter((a) => a.type === 'EXPENSE' || a.type === 'ASSET'));
      setAnalytics(anas);
      if (accs.length > 0) setAccountId(accs[0].id);
      if (anas.length > 0) setAnalyticAccountId(anas[0].id);
    } catch (err) {
      console.error(err);
    }
  };

  useEffect(() => {
    fetchData();
  }, []);

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      await budgetApi.createBudget({
        name,
        allocatedAmount: Number(allocatedAmount),
        accountId: Number(accountId),
        analyticAccountId: Number(analyticAccountId),
        startDate,
        endDate,
      });
      setShowModal(false);
      setName('');
      fetchData();
    } catch (err) {
      alert('Error creating budget: ' + err.message);
    }
  };

  const chartData = variances.map((v) => ({
    name: v.name.length > 25 ? v.name.substring(0, 25) + '...' : v.name,
    Allocated: Number(v.allocatedAmount),
    Actual: Number(v.actualAmount),
  }));

  return (
    <div>
      <div className="page-header">
        <div className="page-title-group">
          <h1>Facility Analytic Budgets</h1>
          <p className="page-subtitle">Period budget allocations per storage facility analytic account & real-time variance analysis</p>
        </div>
        <button className="btn btn-primary" onClick={() => setShowModal(true)}>
          + Create Period Budget
        </button>
      </div>

      {/* Variance Bar Chart */}
      <div className="card" style={{ marginBottom: '24px', padding: '24px' }}>
        <h3 style={{ fontSize: '15px', marginBottom: '16px' }}>Allocated vs Actual Expense Comparison</h3>
        <div style={{ width: '100%', height: 300 }}>
          <ResponsiveContainer width="100%" height="100%">
            <BarChart data={chartData}>
              <CartesianGrid strokeDasharray="3 3" stroke="#3a342c" />
              <XAxis dataKey="name" stroke="#9ca3af" tick={{ fontSize: 11 }} />
              <YAxis stroke="#9ca3af" tick={{ fontSize: 11 }} />
              <Tooltip contentStyle={{ backgroundColor: '#25211c', borderColor: '#5c5246', color: '#f3f4f6' }} />
              <Legend />
              <Bar dataKey="Allocated" fill="#d97706" />
              <Bar dataKey="Actual" fill="#10b981" />
            </BarChart>
          </ResponsiveContainer>
        </div>
      </div>

      <div className="table-container">
        <table className="data-table">
          <thead>
            <tr>
              <th>Budget Name</th>
              <th>Facility Analytic Account</th>
              <th>Ledger Account</th>
              <th>Period</th>
              <th>Allocated ($)</th>
              <th>Actual Spend ($)</th>
              <th>Variance ($)</th>
              <th>Variance %</th>
            </tr>
          </thead>
          <tbody>
            {variances.map((v) => {
              const isOver = v.varianceAmount > 0;
              return (
                <tr key={v.budgetId}>
                  <td style={{ fontWeight: '600' }}>{v.name}</td>
                  <td><span className="badge badge-warning">{v.analyticAccountName}</span></td>
                  <td>{v.accountName}</td>
                  <td className="mono" style={{ fontSize: '12px' }}>{v.startDate} to {v.endDate}</td>
                  <td className="mono" style={{ fontWeight: '600' }}>
                    ${Number(v.allocatedAmount).toLocaleString('en-US', { minimumFractionDigits: 2 })}
                  </td>
                  <td className="mono" style={{ fontWeight: '600', color: 'var(--status-green)' }}>
                    ${Number(v.actualAmount).toLocaleString('en-US', { minimumFractionDigits: 2 })}
                  </td>
                  <td className="mono" style={{ color: isOver ? 'var(--status-red)' : 'var(--status-green)', fontWeight: '600' }}>
                    ${Number(v.varianceAmount).toLocaleString('en-US', { minimumFractionDigits: 2 })}
                  </td>
                  <td>
                    <span className={`badge ${isOver ? 'badge-danger' : 'badge-on'}`}>
                      {v.variancePercent.toFixed(1)}%
                    </span>
                  </td>
                </tr>
              );
            })}
          </tbody>
        </table>
      </div>

      {showModal && (
        <div className="modal-overlay">
          <div className="modal-content">
            <div className="modal-header">
              <h3>Create Period Budget Allocation</h3>
              <button className="btn btn-secondary btn-sm" onClick={() => setShowModal(false)}>✕</button>
            </div>
            <form onSubmit={handleSubmit}>
              <div className="form-group">
                <label className="form-label">Budget Name</label>
                <input type="text" className="form-control" value={name} onChange={(e) => setName(e.target.value)} placeholder="Q4 Fan Electricity Budget Cluster A" required />
              </div>
              <div className="form-group">
                <label className="form-label">Allocated Amount ($)</label>
                <input type="number" step="100" className="form-control" value={allocatedAmount} onChange={(e) => setAllocatedAmount(e.target.value)} required />
              </div>
              <div className="form-group">
                <label className="form-label">Ledger Expense Account</label>
                <select className="form-control" value={accountId} onChange={(e) => setAccountId(e.target.value)}>
                  {accounts.map((a) => (
                    <option key={a.id} value={a.id}>{a.code} - {a.name}</option>
                  ))}
                </select>
              </div>
              <div className="form-group">
                <label className="form-label">Analytic Storage Facility Account</label>
                <select className="form-control" value={analyticAccountId} onChange={(e) => setAnalyticAccountId(e.target.value)}>
                  {analytics.map((an) => (
                    <option key={an.id} value={an.id}>{an.code} - {an.name}</option>
                  ))}
                </select>
              </div>
              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '12px' }}>
                <div className="form-group">
                  <label className="form-label">Start Date</label>
                  <input type="date" className="form-control" value={startDate} onChange={(e) => setStartDate(e.target.value)} required />
                </div>
                <div className="form-group">
                  <label className="form-label">End Date</label>
                  <input type="date" className="form-control" value={endDate} onChange={(e) => setEndDate(e.target.value)} required />
                </div>
              </div>
              <div className="modal-footer">
                <button type="button" className="btn btn-secondary" onClick={() => setShowModal(false)}>Cancel</button>
                <button type="submit" className="btn btn-primary">Allocate Budget</button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};
