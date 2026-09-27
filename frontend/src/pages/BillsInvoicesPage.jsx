import React, { useEffect, useState } from 'react';
import { ordersApi, paymentsApi, accountingApi } from '../services/api';

export const BillsInvoicesPage = ({ setActivePage }) => {
  const [bills, setBills] = useState([]);
  const [invoices, setInvoices] = useState([]);
  const [activeTab, setActiveTab] = useState('bills');
  const [showPaymentModal, setShowPaymentModal] = useState(false);

  const [selectedDoc, setSelectedDoc] = useState(null);
  const [docType, setDocType] = useState('BILL'); // BILL or INVOICE
  const [paymentDate, setPaymentDate] = useState(new Date().toISOString().split('T')[0]);
  const [bankAccountId, setBankAccountId] = useState(13);
  const [bankAccounts, setBankAccounts] = useState([]);

  const fetchData = async () => {
    try {
      const [bList, iList, accList] = await Promise.all([
        ordersApi.getVendorBills(0, 50),
        ordersApi.getCustomerInvoices(0, 50),
        accountingApi.getAccounts(),
      ]);
      setBills(bList.content || []);
      setInvoices(iList.content || []);
      const cashBankAccs = accList.filter((a) => a.type === 'ASSET');
      setBankAccounts(cashBankAccs);
      if (cashBankAccs.length > 0) setBankAccountId(cashBankAccs[0].id);
    } catch (err) {
      console.error(err);
    }
  };

  useEffect(() => {
    fetchData();
  }, []);

  const openPaymentModal = (doc, type) => {
    setSelectedDoc(doc);
    setDocType(type);
    setShowPaymentModal(true);
  };

  const handlePaymentSubmit = async (e) => {
    e.preventDefault();
    try {
      await paymentsApi.createPayment({
        paymentDate,
        amount: Number(selectedDoc.totalAmount),
        paymentType: docType === 'BILL' ? 'OUTBOUND' : 'INBOUND',
        partnerId: docType === 'BILL' ? selectedDoc.vendorId : selectedDoc.customerId,
        bankAccountId: Number(bankAccountId),
        billId: docType === 'BILL' ? selectedDoc.id : null,
        invoiceId: docType === 'INVOICE' ? selectedDoc.id : null,
      });
      alert(`Payment recorded successfully for ${docType === 'BILL' ? selectedDoc.billNumber : selectedDoc.invoiceNumber}! Journal Entry posted.`);
      setShowPaymentModal(false);
      fetchData();
    } catch (err) {
      alert('Error recording payment: ' + err.message);
    }
  };

  return (
    <div>
      <div className="page-header">
        <div className="page-title-group">
          <h1>Bills & Invoices Ledger</h1>
          <p className="page-subtitle">Track accounts payable (Vendor Bills) & accounts receivable (Customer Invoices)</p>
        </div>
      </div>

      <div style={{ display: 'flex', gap: '8px', marginBottom: '20px' }}>
        <button
          className={`btn ${activeTab === 'bills' ? 'btn-primary' : 'btn-secondary'}`}
          onClick={() => setActiveTab('bills')}
        >
          Vendor Bills (Payables)
        </button>
        <button
          className={`btn ${activeTab === 'invoices' ? 'btn-primary' : 'btn-secondary'}`}
          onClick={() => setActiveTab('invoices')}
        >
          Customer Invoices (Receivables)
        </button>
      </div>

      {activeTab === 'bills' ? (
        <div className="table-container">
          <div className="table-header-toolbar">
            <h3 style={{ fontSize: '15px' }}>Vendor Bills Log</h3>
            <span style={{ fontSize: '12px', color: 'var(--text-muted)' }}>{bills.length} bills</span>
          </div>
          <table className="data-table">
            <thead>
              <tr>
                <th>Bill Number</th>
                <th>PO Reference</th>
                <th>Vendor</th>
                <th>Bill Date</th>
                <th>Due Date</th>
                <th>Total Amount ($)</th>
                <th>Status</th>
                <th>Action</th>
              </tr>
            </thead>
            <tbody>
              {bills.map((b) => (
                <tr key={b.id}>
                  <td className="mono" style={{ color: 'var(--accent-amber-light)', fontWeight: '600' }}>
                    {b.billNumber}
                  </td>
                  <td className="mono">{b.poNumber || 'Direct Bill'}</td>
                  <td style={{ fontWeight: '600' }}>{b.vendorName}</td>
                  <td className="mono">{b.billDate}</td>
                  <td className="mono">{b.dueDate}</td>
                  <td className="mono" style={{ fontWeight: '600' }}>
                    ${Number(b.totalAmount).toLocaleString('en-US', { minimumFractionDigits: 2 })}
                  </td>
                  <td>
                    <span className={`badge ${b.status === 'PAID' ? 'badge-paid' : 'badge-unpaid'}`}>
                      {b.status}
                    </span>
                  </td>
                  <td>
                    {b.status === 'UNPAID' ? (
                      <button className="btn btn-primary btn-sm" onClick={() => openPaymentModal(b, 'BILL')}>
                        Pay Vendor Bill
                      </button>
                    ) : (
                      <span style={{ fontSize: '12px', color: 'var(--text-muted)' }}>Settled</span>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      ) : (
        <div className="table-container">
          <div className="table-header-toolbar">
            <h3 style={{ fontSize: '15px' }}>Customer Invoices Log</h3>
            <span style={{ fontSize: '12px', color: 'var(--text-muted)' }}>{invoices.length} invoices</span>
          </div>
          <table className="data-table">
            <thead>
              <tr>
                <th>Invoice Number</th>
                <th>SO Reference</th>
                <th>Customer</th>
                <th>Invoice Date</th>
                <th>Due Date</th>
                <th>Total Amount ($)</th>
                <th>Status</th>
                <th>Action</th>
              </tr>
            </thead>
            <tbody>
              {invoices.map((inv) => (
                <tr key={inv.id}>
                  <td className="mono" style={{ color: 'var(--accent-amber-light)', fontWeight: '600' }}>
                    {inv.invoiceNumber}
                  </td>
                  <td className="mono">{inv.soNumber || 'Direct Invoice'}</td>
                  <td style={{ fontWeight: '600' }}>{inv.customerName}</td>
                  <td className="mono">{inv.invoiceDate}</td>
                  <td className="mono">{inv.dueDate}</td>
                  <td className="mono" style={{ fontWeight: '600' }}>
                    ${Number(inv.totalAmount).toLocaleString('en-US', { minimumFractionDigits: 2 })}
                  </td>
                  <td>
                    <span className={`badge ${inv.status === 'PAID' ? 'badge-paid' : 'badge-unpaid'}`}>
                      {inv.status}
                    </span>
                  </td>
                  <td>
                    {inv.status === 'UNPAID' ? (
                      <button className="btn btn-primary btn-sm" onClick={() => openPaymentModal(inv, 'INVOICE')}>
                        Record Customer Receipt
                      </button>
                    ) : (
                      <span style={{ fontSize: '12px', color: 'var(--text-muted)' }}>Settled</span>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {/* Record Payment Modal */}
      {showPaymentModal && selectedDoc && (
        <div className="modal-overlay">
          <div className="modal-content">
            <div className="modal-header">
              <h3>
                {docType === 'BILL' ? `Pay Vendor Bill: ${selectedDoc.billNumber}` : `Record Receipt for Invoice: ${selectedDoc.invoiceNumber}`}
              </h3>
              <button className="btn btn-secondary btn-sm" onClick={() => setShowPaymentModal(false)}>✕</button>
            </div>
            <form onSubmit={handlePaymentSubmit}>
              <div className="form-group">
                <label className="form-label">Partner</label>
                <input type="text" className="form-control" value={docType === 'BILL' ? selectedDoc.vendorName : selectedDoc.customerName} disabled />
              </div>
              <div className="form-group">
                <label className="form-label">Settlement Amount ($)</label>
                <input type="number" step="0.01" className="form-control mono" value={selectedDoc.totalAmount} disabled />
              </div>
              <div className="form-group">
                <label className="form-label">Payment Date</label>
                <input type="date" className="form-control" value={paymentDate} onChange={(e) => setPaymentDate(e.target.value)} required />
              </div>
              <div className="form-group">
                <label className="form-label">Bank Account Asset</label>
                <select className="form-control" value={bankAccountId} onChange={(e) => setBankAccountId(e.target.value)}>
                  {bankAccounts.map((a) => (
                    <option key={a.id} value={a.id}>{a.code} - {a.name}</option>
                  ))}
                </select>
              </div>
              <div className="modal-footer">
                <button type="button" className="btn btn-secondary" onClick={() => setShowPaymentModal(false)}>Cancel</button>
                <button type="submit" className="btn btn-primary">Process Payment & Post Journal</button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};
