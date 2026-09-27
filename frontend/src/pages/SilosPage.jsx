import React, { useEffect, useState } from 'react';
import { silosApi } from '../services/api';
import { SiloIcon, FanIcon, MoistureIcon, TempIcon } from '../components/Icons';

export const SilosPage = ({ user }) => {
  const [silos, setSilos] = useState([]);
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [showEditModal, setShowEditModal] = useState(false);
  const [selectedSilo, setSelectedSilo] = useState(null);

  // New Silo Form State
  const [code, setCode] = useState('');
  const [name, setName] = useState('');
  const [capacityTons, setCapacityTons] = useState(5000);
  const [cropType, setCropType] = useState('Hard Red Winter Wheat');

  // Edit Threshold Form State
  const [targetMoisture, setTargetMoisture] = useState(14.5);
  const [idealTempMin, setIdealTempMin] = useState(10.0);
  const [idealTempMax, setIdealTempMax] = useState(25.0);

  const isAdmin = user?.role === 'ADMIN';

  const fetchSilos = async () => {
    try {
      const data = await silosApi.getUnits();
      setSilos(data);
    } catch (err) {
      console.error(err);
    }
  };

  useEffect(() => {
    fetchSilos();
  }, []);

  const handleCreateSubmit = async (e) => {
    e.preventDefault();
    try {
      await silosApi.createUnit({
        code,
        name,
        capacityTons: Number(capacityTons),
        cropType,
        targetMoistureThreshold: 14.5,
        idealTempMin: 10.0,
        idealTempMax: 25.0,
      });
      setShowCreateModal(false);
      fetchSilos();
    } catch (err) {
      alert('Error creating silo: ' + err.message);
    }
  };

  const openEditModal = (silo) => {
    setSelectedSilo(silo);
    setTargetMoisture(silo.targetMoistureThreshold);
    setIdealTempMin(silo.idealTempMin);
    setIdealTempMax(silo.idealTempMax);
    setShowEditModal(true);
  };

  const handleEditSubmit = async (e) => {
    e.preventDefault();
    try {
      await silosApi.updateThresholds(selectedSilo.id, Number(targetMoisture), Number(idealTempMin), Number(idealTempMax));
      setShowEditModal(false);
      fetchSilos();
    } catch (err) {
      alert('Error updating thresholds: ' + err.message);
    }
  };

  return (
    <div>
      <div className="page-header">
        <div className="page-title-group">
          <h1>Silos & Climate Threshold Management</h1>
          <p className="page-subtitle">Configure grain storage assets, capacity, moisture limits and safe atmospheric window</p>
        </div>
        {isAdmin && (
          <button className="btn btn-primary" onClick={() => setShowCreateModal(true)}>
            + Register New Silo Asset
          </button>
        )}
      </div>

      <div className="table-container">
        <table className="data-table">
          <thead>
            <tr>
              <th>Code</th>
              <th>Silo Name</th>
              <th>Crop Type</th>
              <th>Capacity</th>
              <th>Target Moisture</th>
              <th>Safe Temp Window</th>
              <th>Fan State</th>
              <th>Action</th>
            </tr>
          </thead>
          <tbody>
            {silos.map((silo) => (
              <tr key={silo.id}>
                <td className="mono" style={{ color: 'var(--accent-amber-light)', fontWeight: '600' }}>
                  {silo.code}
                </td>
                <td style={{ fontWeight: '600' }}>{silo.name}</td>
                <td>{silo.cropType}</td>
                <td className="mono">{silo.capacityTons.toLocaleString()} Tons</td>
                <td className="mono" style={{ color: 'var(--accent-amber-light)', fontWeight: '600' }}>
                  {silo.targetMoistureThreshold}%
                </td>
                <td className="mono">{silo.idealTempMin}°C - {silo.idealTempMax}°C</td>
                <td>
                  <span className={`badge ${silo.fanState === 'ON' ? 'badge-on' : 'badge-off'}`}>
                    <FanIcon size={12} spinning={silo.fanState === 'ON'} />
                    {silo.fanState}
                  </span>
                </td>
                <td>
                  {isAdmin ? (
                    <button className="btn btn-secondary btn-sm" onClick={() => openEditModal(silo)}>
                      Configure Limits
                    </button>
                  ) : (
                    <span style={{ fontSize: '11px', color: 'var(--text-muted)' }}>Read Only</span>
                  )}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {/* Create Silo Modal */}
      {showCreateModal && (
        <div className="modal-overlay">
          <div className="modal-content">
            <div className="modal-header">
              <h3>Register New Grain Silo Asset</h3>
              <button className="btn btn-secondary btn-sm" onClick={() => setShowCreateModal(false)}>✕</button>
            </div>
            <form onSubmit={handleCreateSubmit}>
              <div className="form-group">
                <label className="form-label">Silo Asset Code</label>
                <input type="text" className="form-control" value={code} onChange={(e) => setCode(e.target.value)} placeholder="SILO-C2" required />
              </div>
              <div className="form-group">
                <label className="form-label">Silo Name</label>
                <input type="text" className="form-control" value={name} onChange={(e) => setName(e.target.value)} placeholder="South Field Soy Silo C2" required />
              </div>
              <div className="form-group">
                <label className="form-label">Capacity (Tons)</label>
                <input type="number" className="form-control" value={capacityTons} onChange={(e) => setCapacityTons(e.target.value)} required />
              </div>
              <div className="form-group">
                <label className="form-label">Stored Crop Type</label>
                <input type="text" className="form-control" value={cropType} onChange={(e) => setCropType(e.target.value)} required />
              </div>
              <div className="modal-footer">
                <button type="button" className="btn btn-secondary" onClick={() => setShowCreateModal(false)}>Cancel</button>
                <button type="submit" className="btn btn-primary">Create Silo</button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Edit Thresholds Modal */}
      {showEditModal && selectedSilo && (
        <div className="modal-overlay">
          <div className="modal-content">
            <div className="modal-header">
              <h3>Configure Thresholds: {selectedSilo.code}</h3>
              <button className="btn btn-secondary btn-sm" onClick={() => setShowEditModal(false)}>✕</button>
            </div>
            <form onSubmit={handleEditSubmit}>
              <div className="form-group">
                <label className="form-label">Target Moisture Threshold (%)</label>
                <input
                  type="number"
                  step="0.1"
                  className="form-control"
                  value={targetMoisture}
                  onChange={(e) => setTargetMoisture(e.target.value)}
                  required
                />
              </div>
              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '12px' }}>
                <div className="form-group">
                  <label className="form-label">Safe Temp Min (°C)</label>
                  <input
                    type="number"
                    step="0.5"
                    className="form-control"
                    value={idealTempMin}
                    onChange={(e) => setIdealTempMin(e.target.value)}
                    required
                  />
                </div>
                <div className="form-group">
                  <label className="form-label">Safe Temp Max (°C)</label>
                  <input
                    type="number"
                    step="0.5"
                    className="form-control"
                    value={idealTempMax}
                    onChange={(e) => setIdealTempMax(e.target.value)}
                    required
                  />
                </div>
              </div>
              <div className="modal-footer">
                <button type="button" className="btn btn-secondary" onClick={() => setShowEditModal(false)}>Cancel</button>
                <button type="submit" className="btn btn-primary">Save Thresholds</button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};
