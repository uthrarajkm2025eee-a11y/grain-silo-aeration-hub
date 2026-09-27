import React, { useEffect, useState } from 'react';
import { accountingApi } from '../services/api';
import { LedgerIcon } from '../components/Icons';

export const AccountsPage = () => {
  const [accounts, setAccounts] = useState([]);
  const [journals, setJournals] = useState([]);
  const [journalEntries, setJournalEntries] = useState([]);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(1);
  const [activeTab, setActiveTab] = useState('chart'); // 'chart' or 'ledger'
  const [showModal, setShowModal] = useState(false);

  // New Journal Entry Form State
  const [journalId, setJournalId] = useState(1);
  const [entryDate, setEntryDate] = useState(new Date().toISOString().split('T')[0]);
  const [reference, setReference] = useState('');
  const [items, setItems] = useState([
    { accountId: 13, label: 'Debit Cash/Bank', debit: 1000, credit: 0 },
    { accountId: 31, label: 'Credit Conditioning Fees', debit: 0, credit: 1000 },
  ]);

  const fetchData = async () => {
    try {
      const [accs, jnls, entries] = await Promise.all([
        accountingApi.getAccounts(),
        accountingApi.getJournals(),
        accountingApi.getJournalEntries(page, 20),
      ]);
      setAccounts(accs);
      setJournals(jnls);
      setJournalEntries(entries.content || []);
      setTotalPages(entries.totalPages || 1);
    } catch (err) {
      console.error(err);
    }
  };

  useEffect(() => {
    fetchData();
  }, [page]);

  const handleAddItem = () => {
    setItems([...items, { accountId: accounts[0]?.id || 10, label: '', debit: 0, credit: 0 }]);
  };

  const handleItemChange = (index, field, value) => {
    const newItems = [...items];
    newItems[index][field] = field === 'accountId' ? Number(value) : field === 'label' ? value : Number(value);
    setItems(newItems);
  };

  const handleRemoveItem = (index) => {
    setItems(items.filter((_, i) => i !== index));
  };

  const totalDebit = items.reduce((sum, item) => sum + (Number(item.debit) || 0), 0);
  const totalCredit = items.reduce((sum, item) => sum + (Number(item.credit) || 0), 0);
  const isBalanced = Math.abs(totalDebit - totalCredit) < 0.01 && totalDebit > 0;

  const handleSubmitEntry = async (e) => {
    e.preventDefault();
    if (!isBalanced) {
      alert(`Journal Entry is unbalanced! Total Debits ($${totalDebit}) must equal Total Credits ($${totalCredit})`);
      return;
    }

    try {
      await accountingApi.createJournalEntry({
        journalId: Number(journalId),
        entryDate,
        reference,
        items,
      });
      setShowModal(false);
      fetchData();
    } catch (err) {
      alert('Error creating journal entry: ' + err.message);
    }
  };

  return (
    <div>
      <div className="page-header">
        <div className="page-title-group">
          <h1>Double-Entry Accounting & Ledger</h1>
          <p className="page-subtitle">Chart of accounts tree, journal balances and balanced double-entry transaction log</p>
        </div>
        <div style={{ display: 'flex', gap: '12px' }}>
          <button className="btn btn-primary" onClick={() => setShowModal(true)}>
            + Post Manual Journal Entry
          </button>
        </div>
      </div>

      {/* Tabs */}
      <div style={{ display: 'flex', gap: '8px', marginBottom: '20px' }}>
        <button
          className={`btn ${activeTab === 'chart' ? 'btn-primary' : 'btn-secondary'}`}
          onClick={() => setActiveTab('chart')}
        >
          Chart of Accounts Tree
        </button>
        <button
          className={`btn ${activeTab === 'ledger' ? 'btn-primary' : 'btn-secondary'}`}
          onClick={() => setActiveTab('ledger')}
        >
          General Ledger Journal Entries
        </button>
      </div>

      {activeTab === 'chart' ? (
        <div className="table-container">
          <div className="table-header-toolbar">
            <h3 style={{ fontSize: '15px' }}>Chart of Accounts Structure</h3>
            <span style={{ fontSize: '12px', color: 'var(--text-muted)' }}>{accounts.length} accounts registered</span>
          </div>
          <table className="data-table">
            <thead>
              <tr>
                <th>Account Code</th>
                <th>Account Name</th>
                <th>Category Type</th>
                <th>Parent Account</th>
                <th>Live Net Balance ($)</th>
              </tr>
            </thead>
            <tbody>
              {accounts.map((acc) => (
                <tr key={acc.id}>
                  <td className="mono" style={{ color: 'var(--accent-amber-light)', fontWeight: '600' }}>
                    {acc.code}
                  </td>
                  <td style={{ fontWeight: acc.parentId ? 'normal' : '700', paddingLeft: acc.parentId ? '32px' : '16px' }}>
                    {acc.name}
                  </td>
                  <td>
                    <span className={`badge ${
                      acc.type === 'ASSET' ? 'badge-on' :
                      acc.type === 'LIABILITY' ? 'badge-danger' :
                      acc.type === 'INCOME' ? 'badge-warning' : 'badge-off'
                    }`}>
                      {acc.type}
                    </span>
                  </td>
                  <td style={{ fontSize: '13px', color: 'var(--text-muted)' }}>
                    {acc.parentName || 'Top Level'}
                  </td>
                  <td className="mono" style={{ fontWeight: '600' }}>
                    ${Number(acc.balance || 0).toLocaleString('en-US', { minimumFractionDigits: 2 })}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      ) : (
        <div className="table-container">
          <div className="table-header-toolbar">
            <h3 style={{ fontSize: '15px' }}>Journal Entries Log</h3>
          </div>
          <table className="data-table">
            <thead>
              <tr>
                <th>Entry Number</th>
                <th>Date</th>
                <th>Journal</th>
                <th>Reference</th>
                <th>Double Entry Lines</th>
                <th>Status</th>
              </tr>
            </thead>
            <tbody>
              {journalEntries.map((e) => (
                <tr key={e.id}>
                  <td className="mono" style={{ color: 'var(--accent-amber-light)', fontWeight: '600' }}>
                    {e.entryNumber}
                  </td>
                  <td className="mono">{e.entryDate}</td>
                  <td><span className="badge badge-warning">{e.journalCode}</span></td>
                  <td>{e.reference || 'N/A'}</td>
                  <td>
                    <div style={{ display: 'flex', flexDirection: 'column', gap: '4px' }}>
                      {e.items.map((item) => (
                        <div key={item.id} style={{ fontSize: '12px', display: 'flex', gap: '8px' }}>
                          <span className="mono" style={{ color: 'var(--text-muted)' }}>[{item.accountCode}]</span>
                          <span style={{ flex: 1 }}>{item.accountName}</span>
                          <span className="mono" style={{ color: item.debit > 0 ? 'var(--status-green)' : 'var(--text-muted)' }}>
                            Dr: ${Number(item.debit).toFixed(2)}
                          </span>
                          <span className="mono" style={{ color: item.credit > 0 ? 'var(--accent-amber-light)' : 'var(--text-muted)' }}>
                            Cr: ${Number(item.credit).toFixed(2)}
                          </span>
                        </div>
                      ))}
                    </div>
                  </td>
                  <td><span className="badge badge-posted">{e.state}</span></td>
                </tr>
              ))}
            </tbody>
          </table>
          <div className="pagination-bar">
            <button className="btn btn-secondary btn-sm" disabled={page === 0} onClick={() => setPage((p) => p - 1)}>
              ← Prev
            </button>
            <span>Page {page + 1} of {totalPages}</span>
            <button className="btn btn-secondary btn-sm" disabled={page >= totalPages - 1} onClick={() => setPage((p) => p + 1)}>
              Next →
            </button>
          </div>
        </div>
      )}

      {/* Manual Journal Entry Modal */}
      {showModal && (
        <div className="modal-overlay">
          <div className="modal-content" style={{ maxWidth: '680px' }}>
            <div className="modal-header">
              <h3>Post Balanced Double-Entry Journal Entry</h3>
              <button className="btn btn-secondary btn-sm" onClick={() => setShowModal(false)}>✕</button>
            </div>
            <form onSubmit={handleSubmitEntry}>
              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '12px' }}>
                <div className="form-group">
                  <label className="form-label">Journal</label>
                  <select className="form-control" value={journalId} onChange={(e) => setJournalId(e.target.value)}>
                    {journals.map((j) => (
                      <option key={j.id} value={j.id}>{j.code} - {j.name}</option>
                    ))}
                  </select>
                </div>
                <div className="form-group">
                  <label className="form-label">Entry Date</label>
                  <input type="date" className="form-control" value={entryDate} onChange={(e) => setEntryDate(e.target.value)} required />
                </div>
              </div>
              <div className="form-group">
                <label className="form-label">Reference Description</label>
                <input type="text" className="form-control" value={reference} onChange={(e) => setReference(e.target.value)} placeholder="Monthly Aeration Power Settlement" />
              </div>

              {/* Items Table */}
              <h4 style={{ fontSize: '13px', marginTop: '16px', marginBottom: '8px' }}>Journal Entry Items (Debits = Credits)</h4>
              {items.map((item, idx) => (
                <div key={idx} style={{ display: 'grid', gridTemplateColumns: '2fr 2fr 1fr 1fr 40px', gap: '8px', marginBottom: '8px', alignItems: 'center' }}>
                  <select className="form-control" value={item.accountId} onChange={(e) => handleItemChange(idx, 'accountId', e.target.value)}>
                    {accounts.map((a) => (
                      <option key={a.id} value={a.id}>{a.code} - {a.name}</option>
                    ))}
                  </select>
                  <input type="text" className="form-control" placeholder="Label" value={item.label} onChange={(e) => handleItemChange(idx, 'label', e.target.value)} />
                  <input type="number" step="0.01" className="form-control" placeholder="Debit ($)" value={item.debit} onChange={(e) => handleItemChange(idx, 'debit', e.target.value)} />
                  <input type="number" step="0.01" className="form-control" placeholder="Credit ($)" value={item.credit} onChange={(e) => handleItemChange(idx, 'credit', e.target.value)} />
                  {items.length > 2 && (
                    <button type="button" className="btn btn-danger btn-sm" onClick={() => handleRemoveItem(idx)}>✕</button>
                  )}
                </div>
              ))}
              <button type="button" className="btn btn-secondary btn-sm" onClick={handleAddItem} style={{ marginTop: '8px' }}>
                + Add Item Line
              </button>

              {/* Balance Summary Indicator */}
              <div style={{
                marginTop: '16px',
                padding: '12px',
                backgroundColor: isBalanced ? 'var(--status-green-bg)' : 'var(--status-red-bg)',
                border: `1px solid ${isBalanced ? 'rgba(16,185,129,0.3)' : 'rgba(239,68,68,0.3)'}`,
                borderRadius: 'var(--radius-md)',
                display: 'flex',
                justifyContent: 'space-between',
                alignItems: 'center'
              }}>
                <span className="mono" style={{ fontWeight: '600' }}>
                  Total Debits: ${totalDebit.toFixed(2)} | Total Credits: ${totalCredit.toFixed(2)}
                </span>
                <span className={`badge ${isBalanced ? 'badge-on' : 'badge-danger'}`}>
                  {isBalanced ? 'BALANCED' : 'UNBALANCED'}
                </span>
              </div>

              <div className="modal-footer">
                <button type="button" className="btn btn-secondary" onClick={() => setShowModal(false)}>Cancel</button>
                <button type="submit" className="btn btn-primary" disabled={!isBalanced}>Post Entry</button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};
