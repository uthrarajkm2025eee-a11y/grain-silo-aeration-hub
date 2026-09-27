import React, { useEffect, useState } from 'react';
import { masterDataApi } from '../services/api';
import { ContactsIcon } from '../components/Icons';

export const ContactsPage = () => {
  const [contacts, setContacts] = useState([]);
  const [filterType, setFilterType] = useState('');
  const [showModal, setShowModal] = useState(false);

  const [code, setCode] = useState('');
  const [name, setName] = useState('');
  const [type, setType] = useState('CUSTOMER');
  const [email, setEmail] = useState('');
  const [phone, setPhone] = useState('');
  const [address, setAddress] = useState('');

  const fetchContacts = async () => {
    try {
      const data = await masterDataApi.getContacts(filterType);
      setContacts(data);
    } catch (err) {
      console.error(err);
    }
  };

  useEffect(() => {
    fetchContacts();
  }, [filterType]);

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      await masterDataApi.createContact({ code, name, type, email, phone, address });
      setShowModal(false);
      setCode('');
      setName('');
      setEmail('');
      setPhone('');
      setAddress('');
      fetchContacts();
    } catch (err) {
      alert('Error creating contact: ' + err.message);
    }
  };

  return (
    <div>
      <div className="page-header">
        <div className="page-title-group">
          <h1>Contact Master Directory</h1>
          <p className="page-subtitle">Agricultural Cooperatives (Customers) & Fan Maintenance Vendors (Vendors)</p>
        </div>
        <button className="btn btn-primary" onClick={() => setShowModal(true)}>
          + Create Contact Record
        </button>
      </div>

      <div className="table-container">
        <div className="table-header-toolbar">
          <div style={{ display: 'flex', gap: '12px', alignItems: 'center' }}>
            <span style={{ fontSize: '13px', color: 'var(--text-muted)' }}>Filter by Type:</span>
            <select
              className="form-control"
              style={{ width: '200px' }}
              value={filterType}
              onChange={(e) => setFilterType(e.target.value)}
            >
              <option value="">All Contact Types</option>
              <option value="CUSTOMER">Agricultural Cooperatives (Customers)</option>
              <option value="VENDOR">Maintenance Vendors</option>
            </select>
          </div>
          <span style={{ fontSize: '12px', color: 'var(--text-muted)' }}>{contacts.length} records</span>
        </div>
        <table className="data-table">
          <thead>
            <tr>
              <th>Code</th>
              <th>Organization / Name</th>
              <th>Type</th>
              <th>Email</th>
              <th>Phone</th>
              <th>Address</th>
            </tr>
          </thead>
          <tbody>
            {contacts.map((c) => (
              <tr key={c.id}>
                <td className="mono" style={{ color: 'var(--accent-amber-light)', fontWeight: '600' }}>
                  {c.code}
                </td>
                <td style={{ fontWeight: '600' }}>{c.name}</td>
                <td>
                  <span className={`badge ${c.type === 'CUSTOMER' ? 'badge-customer' : 'badge-vendor'}`}>
                    {c.type === 'CUSTOMER' ? 'Cooperative Customer' : 'Vendor'}
                  </span>
                </td>
                <td className="mono">{c.email || 'N/A'}</td>
                <td className="mono">{c.phone || 'N/A'}</td>
                <td style={{ fontSize: '13px' }}>{c.address || 'N/A'}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {showModal && (
        <div className="modal-overlay">
          <div className="modal-content">
            <div className="modal-header">
              <h3>Create Contact Record</h3>
              <button className="btn btn-secondary btn-sm" onClick={() => setShowModal(false)}>✕</button>
            </div>
            <form onSubmit={handleSubmit}>
              <div className="form-group">
                <label className="form-label">Contact Code</label>
                <input type="text" className="form-control" value={code} onChange={(e) => setCode(e.target.value)} placeholder="CUST-004" required />
              </div>
              <div className="form-group">
                <label className="form-label">Organization Name</label>
                <input type="text" className="form-control" value={name} onChange={(e) => setName(e.target.value)} placeholder="Valley Grain Co-Op" required />
              </div>
              <div className="form-group">
                <label className="form-label">Discriminator Type</label>
                <select className="form-control" value={type} onChange={(e) => setType(e.target.value)}>
                  <option value="CUSTOMER">Customer (Agricultural Cooperative)</option>
                  <option value="VENDOR">Vendor (Fan Maintenance / Utility)</option>
                </select>
              </div>
              <div className="form-group">
                <label className="form-label">Email</label>
                <input type="email" className="form-control" value={email} onChange={(e) => setEmail(e.target.value)} />
              </div>
              <div className="form-group">
                <label className="form-label">Phone</label>
                <input type="text" className="form-control" value={phone} onChange={(e) => setPhone(e.target.value)} />
              </div>
              <div className="form-group">
                <label className="form-label">Address</label>
                <input type="text" className="form-control" value={address} onChange={(e) => setAddress(e.target.value)} />
              </div>
              <div className="modal-footer">
                <button type="button" className="btn btn-secondary" onClick={() => setShowModal(false)}>Cancel</button>
                <button type="submit" className="btn btn-primary">Save Contact</button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};
