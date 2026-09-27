import React, { useEffect, useState } from 'react';
import { silosApi, fanEventsApi } from '../services/api';
import { SiloIcon, FanIcon, MoistureIcon, TempIcon, AlertIcon, RefreshIcon } from '../components/Icons';

export const DashboardPage = ({ setActivePage }) => {
  const [silos, setSilos] = useState([]);
  const [recentEvents, setRecentEvents] = useState([]);
  const [simulatorActive, setSimulatorActive] = useState(true);
  const [loading, setLoading] = useState(true);

  const loadData = async () => {
    try {
      setLoading(true);
      const [units, events, simStatus] = await Promise.all([
        silosApi.getUnits(),
        fanEventsApi.getEvents(),
        silosApi.getSimulatorStatus(),
      ]);
      setSilos(units);
      setRecentEvents(events.slice(0, 5));
      setSimulatorActive(simStatus.simulatorEnabled);
    } catch (err) {
      console.error('Failed to load dashboard data:', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadData();
    const interval = setInterval(loadData, 5000);
    return () => clearInterval(interval);
  }, []);

  const handleToggleSimulator = async () => {
    try {
      const res = await silosApi.toggleSimulator(!simulatorActive);
      setSimulatorActive(res.simulatorEnabled);
    } catch (err) {
      alert('Failed to toggle simulator');
    }
  };

  const activeFans = silos.filter((s) => s.fanState === 'ON').length;
  const totalCapacity = silos.reduce((acc, s) => acc + s.capacityTons, 0);

  return (
    <div>
      <div className="page-header">
        <div className="page-title-group">
          <h1>Silo Operations Dashboard</h1>
          <p className="page-subtitle">Real-time agricultural grain conditioning & automated aeration monitoring</p>
        </div>
        <div style={{ display: 'flex', gap: '12px' }}>
          <button
            className={`btn ${simulatorActive ? 'btn-primary' : 'btn-secondary'}`}
            onClick={handleToggleSimulator}
          >
            <RefreshIcon size={16} />
            <span>Simulator: {simulatorActive ? 'ON' : 'OFF'}</span>
          </button>
          <button className="btn btn-secondary" onClick={loadData}>
            <RefreshIcon size={16} />
            <span>Refresh</span>
          </button>
        </div>
      </div>

      {/* KPI Cards */}
      <div className="card-grid">
        <div className="card kpi-card">
          <div>
            <div className="kpi-label">Active Silo Units</div>
            <div className="kpi-val">{silos.length}</div>
            <div className="kpi-sub">Total Storage Capacity: {totalCapacity.toLocaleString()} Tons</div>
          </div>
          <div className="kpi-icon">
            <SiloIcon size={24} />
          </div>
        </div>

        <div className="card kpi-card">
          <div>
            <div className="kpi-label">Aeration Fans Active</div>
            <div className="kpi-val" style={{ color: activeFans > 0 ? 'var(--accent-amber-light)' : 'var(--text-muted)' }}>
              {activeFans} / {silos.length}
            </div>
            <div className="kpi-sub">{activeFans > 0 ? 'Circulating air to reduce moisture' : 'All fans idle in standby'}</div>
          </div>
          <div className="kpi-icon">
            <FanIcon size={24} spinning={activeFans > 0} />
          </div>
        </div>

        <div className="card kpi-card">
          <div>
            <div className="kpi-label">Telemetry Ingestion Status</div>
            <div className="kpi-val" style={{ color: 'var(--status-green)', fontSize: '20px' }}>
              ONLINE
            </div>
            <div className="kpi-sub">Simulated Telemetry Stream active</div>
          </div>
          <div className="kpi-icon" style={{ backgroundColor: 'var(--status-green-bg)', color: 'var(--status-green)' }}>
            <MoistureIcon size={24} />
          </div>
        </div>
      </div>

      {/* Silos Operational Grid */}
      <h3 style={{ marginBottom: '16px', fontSize: '16px' }}>Silo Units Operational Status</h3>
      <div className="card-grid" style={{ marginBottom: '32px' }}>
        {silos.map((silo) => {
          const isFanOn = silo.fanState === 'ON';

          return (
            <div key={silo.id} className="card" style={{ position: 'relative' }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '12px' }}>
                <div>
                  <span style={{ fontSize: '11px', fontFamily: 'var(--font-mono)', color: 'var(--accent-amber)' }}>{silo.code}</span>
                  <h4 style={{ fontSize: '16px', marginTop: '2px' }}>{silo.name}</h4>
                  <span style={{ fontSize: '12px', color: 'var(--text-muted)' }}>Crop: {silo.cropType}</span>
                </div>
                <span className={`badge ${isFanOn ? 'badge-on' : 'badge-off'}`}>
                  <FanIcon size={12} spinning={isFanOn} />
                  FAN {silo.fanState}
                </span>
              </div>

              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '12px', padding: '12px', backgroundColor: 'var(--bg-surface)', borderRadius: 'var(--radius-md)', margin: '12px 0' }}>
                <div>
                  <span style={{ fontSize: '11px', color: 'var(--text-muted)', display: 'block' }}>Capacity</span>
                  <span style={{ fontFamily: 'var(--font-mono)', fontWeight: '600' }}>{silo.capacityTons} Tons</span>
                </div>
                <div>
                  <span style={{ fontSize: '11px', color: 'var(--text-muted)', display: 'block' }}>Moisture Threshold</span>
                  <span style={{ fontFamily: 'var(--font-mono)', fontWeight: '600', color: 'var(--accent-amber-light)' }}>{silo.targetMoistureThreshold}%</span>
                </div>
                <div>
                  <span style={{ fontSize: '11px', color: 'var(--text-muted)', display: 'block' }}>Safe Temp Range</span>
                  <span style={{ fontFamily: 'var(--font-mono)', fontWeight: '600' }}>{silo.idealTempMin}°C - {silo.idealTempMax}°C</span>
                </div>
                <div>
                  <span style={{ fontSize: '11px', color: 'var(--text-muted)', display: 'block' }}>Last Reading</span>
                  <span style={{ fontFamily: 'var(--font-mono)', fontSize: '11px', color: 'var(--text-muted)' }}>
                    {silo.lastTelemetryAt ? new Date(silo.lastTelemetryAt).toLocaleTimeString() : 'N/A'}
                  </span>
                </div>
              </div>

              <div style={{ display: 'flex', justifyContent: 'flex-end', marginTop: '12px' }}>
                <button
                  className="btn btn-secondary btn-sm"
                  onClick={() => setActivePage('telemetry')}
                >
                  View Telemetry Stream →
                </button>
              </div>
            </div>
          );
        })}
      </div>

      {/* Fan Events Audit Trail Snippet */}
      <div className="table-container">
        <div className="table-header-toolbar">
          <h3 style={{ fontSize: '15px' }}>Recent Aeration Fan System Events</h3>
          <button className="btn btn-secondary btn-sm" onClick={() => setActivePage('telemetry')}>
            View Full Audit Log
          </button>
        </div>
        <table className="data-table">
          <thead>
            <tr>
              <th>Timestamp</th>
              <th>Silo</th>
              <th>Action</th>
              <th>Triggered By</th>
              <th>Reason</th>
            </tr>
          </thead>
          <tbody>
            {recentEvents.length === 0 ? (
              <tr>
                <td colSpan="5" style={{ textAlign: 'center', color: 'var(--text-muted)', padding: '24px' }}>
                  No fan events recorded yet.
                </td>
              </tr>
            ) : (
              recentEvents.map((evt) => (
                <tr key={evt.id}>
                  <td className="mono" style={{ fontSize: '12px' }}>
                    {new Date(evt.timestamp).toLocaleString()}
                  </td>
                  <td style={{ fontWeight: '600' }}>{evt.siloCode} - {evt.siloName}</td>
                  <td>
                    <span className={`badge ${evt.action === 'ON' ? 'badge-on' : 'badge-off'}`}>
                      {evt.action}
                    </span>
                  </td>
                  <td>
                    <span className={`badge ${evt.triggeredBy === 'SYSTEM' ? 'badge-warning' : 'badge-danger'}`}>
                      {evt.triggeredBy}
                    </span>
                  </td>
                  <td style={{ fontSize: '13px', color: 'var(--text-secondary)' }}>{evt.reason}</td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
};
