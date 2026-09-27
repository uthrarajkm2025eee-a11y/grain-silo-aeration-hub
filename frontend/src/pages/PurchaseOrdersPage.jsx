import React, { useEffect, useState } from 'react';
import { ordersApi, masterDataApi } from '../services/api';

export const PurchaseOrdersPage = () => {
  const [orders, setOrders] = useState([]);
  const [vendors, setVendors] = useState([]);
  const [products, setProducts] = useState([]);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(1);
  const [showModal, setShowModal] = useState(false);

  const [vendorId, setVendorId] = useState('');
  const [orderDate, setOrderDate] = useState(new Date().toISOString().split('T')[0]);
  const [items, setItems] = useState([{ productId: '', quantity: 1 }]);

  const fetchData = async () => {
    try {
      const [pos, vends, prods] = await Promise.all([
        ordersApi.getPurchaseOrders(page, 20),
        masterDataApi.getContacts('VENDOR'),
        masterDataApi.getProducts(),
      ]);
      setOrders(pos.content || []);
      setTotalPages(pos.totalPages || 1);
      setVendors(vends);
      setProducts(prods);
      if (vends.length > 0) setVendorId(vends[0].id);
      if (prods.length > 0) setItems([{ productId: prods[0].id, quantity: 1 }]);
    } catch (err) {
      console.error(err);
    }
  };

  useEffect(() => {
    fetchData();
  }, [page]);

  const handleAddItem = () => {
    setItems([...items, { productId: products[0]?.id || '', quantity: 1 }]);
  };

  const handleItemChange = (index, field, value) => {
    const newItems = [...items];
    newItems[index][field] = field === 'quantity' ? Number(value) : value;
    setItems(newItems);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      await ordersApi.createPurchaseOrder({
        vendorId: Number(vendorId),
        orderDate,
        items,
      });
      setShowModal(false);
      fetchData();
    } catch (err) {
      alert('Error creating PO: ' + err.message);
    }
  };

  const handleConvertToBill = async (poId) => {
    try {
      await ordersApi.convertToBill(poId);
      alert('Successfully converted PO to Vendor Bill! Balanced Journal Entry created.');
      fetchData();
    } catch (err) {
      alert('Conversion error: ' + err.message);
    }
  };

  return (
    <div>
      <div className="page-header">
        <div className="page-title-group">
          <h1>Purchase Orders</h1>
          <p className="page-subtitle">Manage aeration equipment POs, vendor orders & bill conversions</p>
        </div>
        <button className="btn btn-primary" onClick={() => setShowModal(true)}>
          + Create Purchase Order
        </button>
      </div>

      <div className="table-container">
        <table className="data-table">
          <thead>
            <tr>
              <th>PO Number</th>
              <th>Order Date</th>
              <th>Vendor</th>
              <th>Line Items</th>
              <th>Total Amount ($)</th>
              <th>Status</th>
              <th>Action</th>
            </tr>
          </thead>
          <tbody>
            {orders.map((po) => (
              <tr key={po.id}>
                <td className="mono" style={{ color: 'var(--accent-amber-light)', fontWeight: '600' }}>
                  {po.poNumber}
                </td>
                <td className="mono">{po.orderDate}</td>
                <td style={{ fontWeight: '600' }}>{po.vendorName}</td>
                <td>
                  <div style={{ fontSize: '12px' }}>
                    {po.lines.map((l) => (
                      <div key={l.id}>
                        {l.quantity}x {l.productName} (${Number(l.subtotal).toFixed(2)})
                      </div>
                    ))}
                  </div>
                </td>
                <td className="mono" style={{ fontWeight: '600' }}>
                  ${Number(po.totalAmount).toLocaleString('en-US', { minimumFractionDigits: 2 })}
                </td>
                <td>
                  <span className={`badge ${
                    po.status === 'PAID' ? 'badge-paid' :
                    po.status === 'BILLED' ? 'badge-billed' :
                    po.status === 'CONFIRMED' ? 'badge-warning' : 'badge-draft'
                  }`}>
                    {po.status}
                  </span>
                </td>
                <td>
                  {po.status === 'CONFIRMED' ? (
                    <button className="btn btn-primary btn-sm" onClick={() => handleConvertToBill(po.id)}>
                      Convert to Vendor Bill
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
              <h3>Create Purchase Order</h3>
              <button className="btn btn-secondary btn-sm" onClick={() => setShowModal(false)}>✕</button>
            </div>
            <form onSubmit={handleSubmit}>
              <div className="form-group">
                <label className="form-label">Vendor</label>
                <select className="form-control" value={vendorId} onChange={(e) => setVendorId(e.target.value)}>
                  {vendors.map((v) => (
                    <option key={v.id} value={v.id}>{v.code} - {v.name}</option>
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
                <button type="submit" className="btn btn-primary">Create PO</button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};
