import React, { useEffect, useState } from 'react';
import { paymentsApi } from '../services/api';

export const PaymentsPage = () => {
  const [payments, setPayments] = useState([]);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(1);

  const fetchPayments = async () => {
    try {
      const data = await paymentsApi.getPayments(page, 20);
      setPayments(data.content || []);
      setTotalPages(data.totalPages || 1);
    } catch (err) {
      console.error(err);
    }
  };

  useEffect(() => {
    fetchPayments();
  }, [page]);

  return (
    <div>
      <div className="page-header">
        <div className="page-title-group">
          <h1>Payments & Cash Disbursements Ledger</h1>
          <p className="page-subtitle">Settlement audit log for customer receipts and vendor payments</p>
        </div>
      </div>

      <div className="table-container">
        <table className="data-table">
          <thead>
            <tr>
              <th>Payment Number</th>
              <th>Payment Date</th>
              <th>Type</th>
              <th>Partner</th>
              <th>Bank Account</th>
              <th>Reference Doc</th>
              <th>Amount ($)</th>
            </tr>
          </thead>
          <tbody>
            {payments.map((p) => (
              <tr key={p.id}>
                <td className="mono" style={{ color: 'var(--accent-amber-light)', fontWeight: '600' }}>
                  {p.paymentNumber}
                </td>
                <td className="mono">{p.paymentDate}</td>
                <td>
                  <span className={`badge ${p.paymentType === 'INBOUND' ? 'badge-on' : 'badge-danger'}`}>
                    {p.paymentType}
                  </span>
                </td>
                <td style={{ fontWeight: '600' }}>{p.partnerName}</td>
                <td className="mono" style={{ fontSize: '13px' }}>{p.bankAccountName}</td>
                <td className="mono" style={{ fontSize: '12px' }}>
                  {p.billNumber ? `Bill: ${p.billNumber}` : p.invoiceNumber ? `Invoice: ${p.invoiceNumber}` : 'Direct Deposit'}
                </td>
                <td className="mono" style={{ fontWeight: '600', color: p.paymentType === 'INBOUND' ? 'var(--status-green)' : 'var(--text-primary)' }}>
                  ${Number(p.amount).toLocaleString('en-US', { minimumFractionDigits: 2 })}
                </td>
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
    </div>
  );
};
