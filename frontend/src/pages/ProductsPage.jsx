import React, { useEffect, useState } from 'react';
import { masterDataApi } from '../services/api';
import { ProductsIcon } from '../components/Icons';

export const ProductsPage = () => {
  const [products, setProducts] = useState([]);
  const [showModal, setShowModal] = useState(false);

  const [code, setCode] = useState('');
  const [name, setName] = useState('');
  const [type, setType] = useState('SERVICE');
  const [unitPrice, setUnitPrice] = useState(100.0);
  const [taxRate, setTaxRate] = useState(5.0);

  const fetchProducts = async () => {
    try {
      const data = await masterDataApi.getProducts();
      setProducts(data);
    } catch (err) {
      console.error(err);
    }
  };

  useEffect(() => {
    fetchProducts();
  }, []);

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      await masterDataApi.createProduct({
        code,
        name,
        type,
        unitPrice: Number(unitPrice),
        taxRate: Number(taxRate),
      });
      setShowModal(false);
      setCode('');
      setName('');
      fetchProducts();
    } catch (err) {
      alert('Error creating product: ' + err.message);
    }
  };

  return (
    <div>
      <div className="page-header">
        <div className="page-title-group">
          <h1>Product & Service Master</h1>
          <p className="page-subtitle">Grain storage conditioning services, sensors & aeration fan overhaul goods</p>
        </div>
        <button className="btn btn-primary" onClick={() => setShowModal(true)}>
          + Add Product / Service
        </button>
      </div>

      <div className="table-container">
        <table className="data-table">
          <thead>
            <tr>
              <th>Code</th>
              <th>Product / Service Name</th>
              <th>Type</th>
              <th>Unit Price ($)</th>
              <th>Tax Rate (%)</th>
            </tr>
          </thead>
          <tbody>
            {products.map((p) => (
              <tr key={p.id}>
                <td className="mono" style={{ color: 'var(--accent-amber-light)', fontWeight: '600' }}>
                  {p.code}
                </td>
                <td style={{ fontWeight: '600' }}>{p.name}</td>
                <td>
                  <span className={`badge ${p.type === 'SERVICE' ? 'badge-on' : 'badge-warning'}`}>
                    {p.type}
                  </span>
                </td>
                <td className="mono">${Number(p.unitPrice).toFixed(2)}</td>
                <td className="mono">{p.taxRate}%</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {showModal && (
        <div className="modal-overlay">
          <div className="modal-content">
            <div className="modal-header">
              <h3>Add Product / Service Record</h3>
              <button className="btn btn-secondary btn-sm" onClick={() => setShowModal(false)}>✕</button>
            </div>
            <form onSubmit={handleSubmit}>
              <div className="form-group">
                <label className="form-label">Product Code</label>
                <input type="text" className="form-control" value={code} onChange={(e) => setCode(e.target.value)} placeholder="SRV-TEMP-CALIB" required />
              </div>
              <div className="form-group">
                <label className="form-label">Name</label>
                <input type="text" className="form-control" value={name} onChange={(e) => setName(e.target.value)} placeholder="Temperature Sensor Calibration" required />
              </div>
              <div className="form-group">
                <label className="form-label">Product Type</label>
                <select className="form-control" value={type} onChange={(e) => setType(e.target.value)}>
                  <option value="SERVICE">Service (Grain Conditioning, Rental, Calibration)</option>
                  <option value="GOODS">Goods (Sensors, Aeration Fan Parts)</option>
                </select>
              </div>
              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '12px' }}>
                <div className="form-group">
                  <label className="form-label">Unit Price ($)</label>
                  <input type="number" step="0.01" className="form-control" value={unitPrice} onChange={(e) => setUnitPrice(e.target.value)} required />
                </div>
                <div className="form-group">
                  <label className="form-label">Tax Rate (%)</label>
                  <input type="number" step="0.01" className="form-control" value={taxRate} onChange={(e) => setTaxRate(e.target.value)} required />
                </div>
              </div>
              <div className="modal-footer">
                <button type="button" className="btn btn-secondary" onClick={() => setShowModal(false)}>Cancel</button>
                <button type="submit" className="btn btn-primary">Save Product</button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};
