import React, { useEffect, useState } from 'react';
import { reportsApi } from '../services/api';
import { ReportsIcon } from '../components/Icons';

export const ReportsPage = () => {
  const [activeTab, setActiveTab] = useState('balance');
  const [balanceSheet, setBalanceSheet] = useState(null);
  const [pnl, setPnl] = useState(null);

  const [fromDate, setFromDate] = useState('2026-01-01');
  const [toDate, setToDate] = useState(new Date().toISOString().split('T')[0]);

  const fetchBalanceSheet = async () => {
    try {
      const data = await reportsApi.getBalanceSheet();
      setBalanceSheet(data);
    } catch (err) {
      console.error(err);
    }
  };

  const fetchPnl = async () => {
    try {
      const data = await reportsApi.getProfitAndLoss(fromDate, toDate);
      setPnl(data);
    } catch (err) {
      console.error(err);
    }
  };

  useEffect(() => {
    fetchBalanceSheet();
    fetchPnl();
  }, []);

  const handlePnlFilter = (e) => {
    e.preventDefault();
    fetchPnl();
  };

  return (
    <div>
      <div className="page-header">
        <div className="page-title-group">
          <h1>Backend-Computed Financial Reports</h1>
          <p className="page-subtitle">Live Balance Sheet, Income Statement (P&L) and Equity calculations</p>
        </div>
      </div>

      <div style={{ display: 'flex', gap: '8px', marginBottom: '20px' }}>
        <button
          className={`btn ${activeTab === 'balance' ? 'btn-primary' : 'btn-secondary'}`}
          onClick={() => setActiveTab('balance')}
        >
          Balance Sheet Statement
        </button>
        <button
          className={`btn ${activeTab === 'pnl' ? 'btn-primary' : 'btn-secondary'}`}
          onClick={() => setActiveTab('pnl')}
        >
          Profit & Loss Statement
        </button>
      </div>

      {activeTab === 'balance' && balanceSheet ? (
        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '24px' }}>
          {/* ASSETS */}
          <div className="table-container">
            <div className="table-header-toolbar" style={{ backgroundColor: 'var(--status-green-bg)' }}>
              <h3 style={{ fontSize: '15px', color: 'var(--status-green)' }}>ASSETS</h3>
              <span className="mono" style={{ fontWeight: '700', fontSize: '16px', color: 'var(--status-green)' }}>
                ${Number(balanceSheet.totalAssets).toLocaleString('en-US', { minimumFractionDigits: 2 })}
              </span>
            </div>
            <table className="data-table">
              <thead>
                <tr>
                  <th>Code</th>
                  <th>Asset Account Name</th>
                  <th>Net Balance ($)</th>
                </tr>
              </thead>
              <tbody>
                {balanceSheet.assets.map((item, idx) => (
                  <tr key={idx}>
                    <td className="mono" style={{ color: 'var(--accent-amber-light)' }}>{item.code}</td>
                    <td style={{ fontWeight: '600' }}>{item.name}</td>
                    <td className="mono" style={{ fontWeight: '600' }}>
                      ${Number(item.balance).toLocaleString('en-US', { minimumFractionDigits: 2 })}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>

          {/* LIABILITIES & EQUITY */}
          <div>
            <div className="table-container" style={{ marginBottom: '24px' }}>
              <div className="table-header-toolbar" style={{ backgroundColor: 'var(--status-red-bg)' }}>
                <h3 style={{ fontSize: '15px', color: 'var(--status-red)' }}>LIABILITIES</h3>
                <span className="mono" style={{ fontWeight: '700', fontSize: '16px', color: 'var(--status-red)' }}>
                  ${Number(balanceSheet.totalLiabilities).toLocaleString('en-US', { minimumFractionDigits: 2 })}
                </span>
              </div>
              <table className="data-table">
                <thead>
                  <tr>
                    <th>Code</th>
                    <th>Liability Account Name</th>
                    <th>Net Balance ($)</th>
                  </tr>
                </thead>
                <tbody>
                  {balanceSheet.liabilities.map((item, idx) => (
                    <tr key={idx}>
                      <td className="mono" style={{ color: 'var(--accent-amber-light)' }}>{item.code}</td>
                      <td style={{ fontWeight: '600' }}>{item.name}</td>
                      <td className="mono" style={{ fontWeight: '600' }}>
                        ${Number(item.balance).toLocaleString('en-US', { minimumFractionDigits: 2 })}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>

            {/* NET EQUITY SUMMARY CARD */}
            <div className="card" style={{ borderLeft: '4px solid var(--accent-amber)' }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                <div>
                  <span className="kpi-label">TOTAL CAPITAL & NET EQUITY</span>
                  <div className="kpi-val" style={{ color: 'var(--accent-amber-light)' }}>
                    ${Number(balanceSheet.netEquity).toLocaleString('en-US', { minimumFractionDigits: 2 })}
                  </div>
                  <span className="kpi-sub">Assets minus Liabilities</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      ) : activeTab === 'pnl' && pnl ? (
        <div>
          {/* P&L Date Range Filter */}
          <div className="card" style={{ marginBottom: '24px', padding: '16px 24px' }}>
            <form onSubmit={handlePnlFilter} style={{ display: 'flex', gap: '16px', alignItems: 'center' }}>
              <span style={{ fontSize: '13px', fontWeight: '600', color: 'var(--text-secondary)' }}>Statement Date Range:</span>
              <div className="form-group" style={{ margin: 0, flexDirection: 'row', alignItems: 'center', gap: '8px' }}>
                <label className="form-label" style={{ margin: 0 }}>From:</label>
                <input type="date" className="form-control" value={fromDate} onChange={(e) => setFromDate(e.target.value)} />
              </div>
              <div className="form-group" style={{ margin: 0, flexDirection: 'row', alignItems: 'center', gap: '8px' }}>
                <label className="form-label" style={{ margin: 0 }}>To:</label>
                <input type="date" className="form-control" value={toDate} onChange={(e) => setToDate(e.target.value)} />
              </div>
              <button type="submit" className="btn btn-primary btn-sm">Filter P&L</button>
            </form>
          </div>

          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '24px' }}>
            {/* REVENUE */}
            <div className="table-container">
              <div className="table-header-toolbar" style={{ backgroundColor: 'var(--status-green-bg)' }}>
                <h3 style={{ fontSize: '15px', color: 'var(--status-green)' }}>OPERATIONAL REVENUES</h3>
                <span className="mono" style={{ fontWeight: '700', fontSize: '16px', color: 'var(--status-green)' }}>
                  ${Number(pnl.totalRevenue).toLocaleString('en-US', { minimumFractionDigits: 2 })}
                </span>
              </div>
              <table className="data-table">
                <thead>
                  <tr>
                    <th>Code</th>
                    <th>Revenue Account</th>
                    <th>Period Amount ($)</th>
                  </tr>
                </thead>
                <tbody>
                  {pnl.revenues.map((item, idx) => (
                    <tr key={idx}>
                      <td className="mono" style={{ color: 'var(--accent-amber-light)' }}>{item.code}</td>
                      <td style={{ fontWeight: '600' }}>{item.name}</td>
                      <td className="mono" style={{ fontWeight: '600', color: 'var(--status-green)' }}>
                        ${Number(item.amount).toLocaleString('en-US', { minimumFractionDigits: 2 })}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>

            {/* EXPENSES */}
            <div>
              <div className="table-container" style={{ marginBottom: '24px' }}>
                <div className="table-header-toolbar" style={{ backgroundColor: 'var(--status-red-bg)' }}>
                  <h3 style={{ fontSize: '15px', color: 'var(--status-red)' }}>OPERATING EXPENSES</h3>
                  <span className="mono" style={{ fontWeight: '700', fontSize: '16px', color: 'var(--status-red)' }}>
                    ${Number(pnl.totalExpenses).toLocaleString('en-US', { minimumFractionDigits: 2 })}
                  </span>
                </div>
                <table className="data-table">
                  <thead>
                    <tr>
                      <th>Code</th>
                      <th>Expense Account</th>
                      <th>Period Amount ($)</th>
                    </tr>
                  </thead>
                  <tbody>
                    {pnl.expenses.map((item, idx) => (
                      <tr key={idx}>
                        <td className="mono" style={{ color: 'var(--accent-amber-light)' }}>{item.code}</td>
                        <td style={{ fontWeight: '600' }}>{item.name}</td>
                        <td className="mono" style={{ fontWeight: '600', color: 'var(--status-red)' }}>
                          ${Number(item.amount).toLocaleString('en-US', { minimumFractionDigits: 2 })}
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>

              {/* NET PROFIT SUMMARY CARD */}
              <div className="card" style={{ borderLeft: `4px solid ${pnl.netProfit >= 0 ? 'var(--status-green)' : 'var(--status-red)'}` }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                  <div>
                    <span className="kpi-label">NET OPERATING PROFIT / (LOSS)</span>
                    <div className="kpi-val" style={{ color: pnl.netProfit >= 0 ? 'var(--status-green)' : 'var(--status-red)' }}>
                      ${Number(pnl.netProfit).toLocaleString('en-US', { minimumFractionDigits: 2 })}
                    </div>
                    <span className="kpi-sub">Total Revenues minus Total Operating Expenses</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      ) : null}
    </div>
  );
};
