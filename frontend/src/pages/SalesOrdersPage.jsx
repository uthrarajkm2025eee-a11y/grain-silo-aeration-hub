import React, { useEffect, useState } from 'react';
import { ordersApi, masterDataApi } from '../services/api';

export const SalesOrdersPage = () => {
  const [orders, setOrders] = useState([]);
  const [customers, setCustomers] = useState([]);
  const [products, setProducts] = useState([]);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(1);
  const [showModal, setShowModal] = useState(false);

  const [customerId, setCustomerId] = useState('');
  const [orderDate, setOrderDate] = useState(new Date().toISOString().split('T')[0]);
  const [items, setItems] = useState([{ productId: '', quantity: 100 }]);

  const fetchData = async () => {
    try {
      const [sos, custs, prods] = await Promise.all([
        ordersApi.getSalesOrders(page, 20),
        masterDataApi.getContacts('CUSTOMER'),
        masterDataApi.getProducts(),
      ]);
      setOrders(sos.content || []);
      setTotalPages(sos.totalPages || 1);
      setCustomers(custs);
      setProducts(prods);
      if (custs.length > 0) setCustomerId(custs[0].id);
      if (prods.length > 0) setItems([{ productId: prods[0].id, quantity: 100 }]);
    } catch (err) {
      console.error(err);
    }
  };

  useEffect(() => {
    fetchData();
  }, [page]);

  const handleAddItem = () => {
    setItems([...items, { productId: products[0]?.id || '', quantity: 100 }]);
  };

  const handleItemChange = (index, field, value) => {
    const newItems = [...items];
    newItems[index][field] = field === 'quantity' ? Number(value) : value;
    setItems(newItems);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      await ordersApi.createSalesOrder({
        customerId: Number(customerId),
        orderDate,
        items,
      });
      setShowModal(false);
      fetchData();
    } catch (err) {
      alert('Error creating SO: ' + err.message);
    }
  };

  const handleConvertToInvoice = async (soId) => {
    try {
      await ordersApi.convertToInvoice(soId);
      alert('Successfully converted SO to Customer Invoice! Balanced Journal Entry created.');
      fetchData();
    } catch (err) {
      alert('Conversion error: ' + err.message);
    }
  };

  return (
    <div>
      <div className="page-header">
        <div className="page-title-group">
          <h1>Sales Orders</h1>
          <p className="page-subtitle">Grain conditioning & silo storage contracts with Agricultural Cooperatives</p>
        </div>
        <button className="btn btn-primary" onClick={() => setShowModal(true)}>
          + Create Sales Order
        </button>
      </div>

      <div className="table-container">
        <table className="data-table">
          <thead>
            <tr>
              <th>SO Number</th>
              <th>Order Date</th>
              <th>Cooperative Customer</th>
              <th>Services & Goods</th>
              <th>Total Amount ($)</th>
              <th>Status</th>
              <th>Action</th>
            </tr>
          </thead>
          <tbody>
            {orders.map((so) => (
              <tr key={so.id}>
                <td className="mono" style={{ color: 'var(--accent-amber-light)', fontWeight: '600' }}>
                  {so.soNumber}
                </td>
                <td className="mono">{so.orderDate}</td>
                <td style={{ fontWeight: '600' }}>{so.customerName}</td>
                <td>
                  <div style={{ fontSize: '12px' }}>
                    {so.lines.map((l) => (
                      <div key={l.id}>
                        {l.quantity}x {l.productName} (${Number(l.subtotal).toFixed(2)})
                      </div>
                    ))}
                  </div>
                </td>
                <td className="mono" style={{ fontWeight: '600' }}>
                  ${Number(so.totalAmount).toLocaleString('en-US', { minimumFractionDigits: 2 })}
                </td>
                <td>
                  <span className={`badge ${
                    so.status === 'PAID' ? 'badge-paid' :
                    so.status === 'INVOICED' ? 'badge-warning' :
                    so.status === 'CONFIRMED' ? 'badge-customer' : 'badge-draft'
                  }`}>
                    {so.status}
                  </span>
                </td>
                <td>
                  {so.status === 'CONFIRMED' ? (
                    <button className="btn btn-primary btn-sm" onClick={() => handleConvertToInvoice(so.id)}>
                      Convert to Customer Invoice
                    </button>
                  ) : (
                    <span style={{ fontSize: '12px', color: 'var(--text-muted)' }}>No Action Needed</span>
                  )}
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

      {showModal && (
        <div className="modal-overlay">
          <div className="modal-content">
            <div className="modal-header">
              <h3>Create Sales Order</h3>
              <button className="btn btn-secondary btn-sm" onClick={() => setShowModal(false)}>✕</button>
            </div>
            <form onSubmit={handleSubmit}>
              <div className="form-group">
                <label className="form-label">Agricultural Cooperative Customer</label>
                <select className="form-control" value={customerId} onChange={(e) => setCustomerId(e.target.value)}>
                  {customers.map((c) => (
                    <option key={c.id} value={c.id}>{c.code} - {c.name}</option>
                  ))}
                </select>
              </div>
              <div className="form-group">
                <label className="form-label">Order Date</label>
                <input type="date" className="form-control" value={orderDate} onChange={(e) => setOrderDate(e.target.value)} required />
              </div>

              <h4 style={{ fontSize: '13px', marginTop: '16px', marginBottom: '8px' }}>Order Lines</h4>
              {items.map((item, idx) => (
                <div key={idx} style={{ display: 'grid', gridTemplateColumns: '3fr 1fr 40px', gap: '8px', marginBottom: '8px' }}>
                  <select className="form-control" value={item.productId} onChange={(e) => handleItemChange(idx, 'productId', e.target.value)}>
                    {products.map((p) => (
                      <option key={p.id} value={p.id}>{p.name} (${p.unitPrice})</option>
                    ))}
                  </select>
                  <input type="number" min="1" className="form-control" value={item.quantity} onChange={(e) => handleItemChange(idx, 'quantity', e.target.value)} required />
                  {items.length > 1 && (
                    <button type="button" className="btn btn-danger btn-sm" onClick={() => setItems(items.filter((_, i) => i !== idx))}>✕</button>
                  )}
                </div>
              ))}
              <button type="button" className="btn btn-secondary btn-sm" onClick={handleAddItem} style={{ marginTop: '4px' }}>
                + Add Item
              </button>

              <div className="modal-footer">
                <button type="button" className="btn btn-secondary" onClick={() => setShowModal(false)}>Cancel</button>
                <button type="submit" className="btn btn-primary">Create Sales Order</button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};
