import React, { useEffect, useState } from 'react';
import { silosApi, fanEventsApi } from '../services/api';
import { FanIcon, MoistureIcon, TempIcon, RefreshIcon, AlertIcon } from '../components/Icons';
import { ResponsiveContainer, LineChart, Line, XAxis, YAxis, Tooltip, CartesianGrid, Legend } from 'recharts';

export const TelemetryPage = ({ user }) => {
  const [silos, setSilos] = useState([]);
  const [selectedSiloId, setSelectedSiloId] = useState(1);
  const [telemetryList, setTelemetryList] = useState([]);
  const [fanEvents, setFanEvents] = useState([]);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(1);
  const [loading, setLoading] = useState(false);

  // Manual Ingest Form State
  const [ingestMoisture, setIngestMoisture] = useState(15.5);
  const [ingestTemp, setIngestTemp] = useState(19.0);
  const [showIngestModal, setShowIngestModal] = useState(false);

  // Manual Fan Override State
  const [showOverrideModal, setShowOverrideModal] = useState(false);
  const [overrideAction, setOverrideAction] = useState('ON');
  const [overrideReason, setOverrideReason] = useState('Operator preventative moisture circulation');

  const isAdmin = user?.role === 'ADMIN';

  const fetchData = async () => {
    try {
      setLoading(true);
      const units = await silosApi.getUnits();
      setSilos(units);

      const telemPage = await silosApi.getTelemetryPaged(selectedSiloId, page, 20);
      setTelemetryList(telemPage.content || []);
      setTotalPages(telemPage.totalPages || 1);

      const events = await fanEventsApi.getEvents(selectedSiloId);
      setFanEvents(events);
    } catch (err) {
      console.error('Error fetching telemetry:', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchData();
    const interval = setInterval(fetchData, 6000);
    return () => clearInterval(interval);
  }, [selectedSiloId, page]);

  const handleIngestSubmit = async (e) => {
    e.preventDefault();
    try {
      await silosApi.ingestTelemetry({
        siloId: Number(selectedSiloId),
        moisturePercent: Number(ingestMoisture),
        temperatureCelsius: Number(ingestTemp),
      });
      setShowIngestModal(false);
      fetchData();
    } catch (err) {
      alert('Ingest error: ' + err.message);
    }
  };

  const handleOverrideSubmit = async (e) => {
    e.preventDefault();
    try {
      await fanEventsApi.manualOverride({
        siloId: Number(selectedSiloId),
        action: overrideAction,
        reason: overrideReason,
      });
      setShowOverrideModal(false);
      fetchData();
    } catch (err) {
      alert('Override error: ' + err.message);
    }
  };

  const selectedSilo = silos.find((s) => s.id === Number(selectedSiloId)) || silos[0];

  // Recharts formatted data (reverse to chronological order left-to-right)
  const chartData = [...telemetryList].reverse().map((t) => ({
    time: new Date(t.timestamp).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit', second: '2-digit' }),
    moisture: t.moisturePercent,
    temperature: t.temperatureCelsius,
    threshold: selectedSilo?.targetMoistureThreshold || 14.5,
  }));

  return (
    <div>
      <div className="page-header">
        <div className="page-title-group">
          <h1>Telemetry & Aeration Fan Monitor</h1>
          <p className="page-subtitle">Time-series moisture data, temperature windows & fan event logs</p>
        </div>
        <div style={{ display: 'flex', gap: '12px' }}>
          <button className="btn btn-secondary" onClick={() => setShowIngestModal(true)}>
            + Ingest Reading
          </button>
          {isAdmin && (
            <button className="btn btn-primary" onClick={() => setShowOverrideModal(true)}>
              <FanIcon size={16} />
              Manual Fan Override
            </button>
          )}
        </div>
      </div>

      {/* Silo Selector Bar */}
      <div style={{ display: 'flex', gap: '12px', marginBottom: '24px', alignItems: 'center' }}>
        <label className="form-label" style={{ margin: 0 }}>Select Silo Unit:</label>
        <select
          className="form-control"
          style={{ width: '280px' }}
          value={selectedSiloId}
          onChange={(e) => {
            setSelectedSiloId(Number(e.target.value));
            setPage(0);
          }}
        >
          {silos.map((s) => (
            <option key={s.id} value={s.id}>
              {s.code} - {s.name} ({s.cropType})
            </option>
          ))}
        </select>

        {selectedSilo && (
          <div style={{ marginLeft: 'auto', display: 'flex', gap: '16px', alignItems: 'center' }}>
            <span style={{ fontSize: '13px', color: 'var(--text-muted)' }}>
              Target Moisture: <strong style={{ color: 'var(--accent-amber-light)' }}>{selectedSilo.targetMoistureThreshold}%</strong>
            </span>
            <span style={{ fontSize: '13px', color: 'var(--text-muted)' }}>
              Temp Window: <strong>{selectedSilo.idealTempMin}°C - {selectedSilo.idealTempMax}°C</strong>
            </span>
            <span className={`badge ${selectedSilo.fanState === 'ON' ? 'badge-on' : 'badge-off'}`}>
              <FanIcon size={12} spinning={selectedSilo.fanState === 'ON'} />
              FAN {selectedSilo.fanState}
            </span>
          </div>
        )}
      </div>

      {/* Telemetry Trend Chart */}
      <div className="card" style={{ marginBottom: '24px', padding: '24px' }}>
        <h3 style={{ fontSize: '15px', marginBottom: '16px' }}>Moisture & Temperature Time Series (Live Feed)</h3>
        <div style={{ width: '100%', height: 320 }}>
          <ResponsiveContainer width="100%" height="100%">
            <LineChart data={chartData}>
              <CartesianGrid strokeDasharray="3 3" stroke="#3a342c" />
              <XAxis dataKey="time" stroke="#9ca3af" tick={{ fontSize: 11 }} />
              <YAxis yAxisId="left" stroke="#d97706" tick={{ fontSize: 11 }} domain={[10, 25]} unit="%" />
              <YAxis yAxisId="right" orientation="right" stroke="#3b82f6" tick={{ fontSize: 11 }} domain={[0, 40]} unit="°C" />
              <Tooltip contentStyle={{ backgroundColor: '#25211c', borderColor: '#5c5246', color: '#f3f4f6' }} />
              <Legend />
              <Line yAxisId="left" type="monotone" dataKey="moisture" name="Moisture %" stroke="#f59e0b" strokeWidth={2} dot={{ r: 3 }} />
              <Line yAxisId="left" type="monotone" dataKey="threshold" name="Target Threshold %" stroke="#ef4444" strokeDasharray="5 5" strokeWidth={2} dot={false} />
              <Line yAxisId="right" type="monotone" dataKey="temperature" name="Temp °C" stroke="#3b82f6" strokeWidth={2} dot={{ r: 3 }} />
            </LineChart>
          </ResponsiveContainer>
        </div>
      </div>

      {/* Data Tables Layout: Telemetry Readings & Fan Audit Log */}
      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '24px' }}>
        {/* Telemetry Table */}
        <div className="table-container">
          <div className="table-header-toolbar">
            <h3 style={{ fontSize: '15px' }}>Telemetry Readings</h3>
            <span style={{ fontSize: '12px', color: 'var(--text-muted)' }}>{telemetryList.length} readings</span>
          </div>
          <table className="data-table">
            <thead>
              <tr>
                <th>Timestamp</th>
                <th>Moisture %</th>
                <th>Temp °C</th>
                <th>Status</th>
              </tr>
            </thead>
            <tbody>
              {telemetryList.map((t) => {
                const isHigh = t.moisturePercent > (selectedSilo?.targetMoistureThreshold || 14.5);
                return (
                  <tr key={t.id}>
                    <td className="mono" style={{ fontSize: '12px' }}>
                      {new Date(t.timestamp).toLocaleTimeString()}
                    </td>
                    <td className="mono" style={{ color: isHigh ? 'var(--status-red)' : 'var(--status-green)', fontWeight: '600' }}>
                      {t.moisturePercent}%
                    </td>
                    <td className="mono">{t.temperatureCelsius}°C</td>
                    <td>
                      {isHigh ? (
                        <span className="badge badge-danger">High Moisture</span>
                      ) : (
                        <span className="badge badge-on">Normal</span>
                      )}
                    </td>
                  </tr>
                );
              })}
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

        {/* Fan Events Audit Log */}
        <div className="table-container">
          <div className="table-header-toolbar">
            <h3 style={{ fontSize: '15px' }}>Fan State Audit Log</h3>
            <span style={{ fontSize: '12px', color: 'var(--text-muted)' }}>{fanEvents.length} events</span>
          </div>
          <table className="data-table">
            <thead>
              <tr>
                <th>Time</th>
                <th>Action</th>
                <th>Source</th>
                <th>Reason</th>
              </tr>
            </thead>
            <tbody>
              {fanEvents.map((evt) => (
                <tr key={evt.id}>
                  <td className="mono" style={{ fontSize: '12px' }}>
                    {new Date(evt.timestamp).toLocaleTimeString()}
                  </td>
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
                  <td style={{ fontSize: '12px' }}>{evt.reason}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>

      {/* Ingest Telemetry Modal */}
      {showIngestModal && (
        <div className="modal-overlay">
          <div className="modal-content">
            <div className="modal-header">
              <h3>Ingest Telemetry Reading</h3>
              <button className="btn btn-secondary btn-sm" onClick={() => setShowIngestModal(false)}>✕</button>
            </div>
            <form onSubmit={handleIngestSubmit}>
              <div className="form-group">
                <label className="form-label">Silo Unit</label>
                <select className="form-control" value={selectedSiloId} onChange={(e) => setSelectedSiloId(Number(e.target.value))}>
                  {silos.map((s) => (
                    <option key={s.id} value={s.id}>{s.code} - {s.name}</option>
                  ))}
                </select>
              </div>
              <div className="form-group">
                <label className="form-label">Moisture Percent (%)</label>
                <input
                  type="number"
                  step="0.1"
                  className="form-control"
                  value={ingestMoisture}
                  onChange={(e) => setIngestMoisture(e.target.value)}
                  required
                />
              </div>
              <div className="form-group">
                <label className="form-label">Temperature (°C)</label>
                <input
                  type="number"
                  step="0.1"
                  className="form-control"
                  value={ingestTemp}
                  onChange={(e) => setIngestTemp(e.target.value)}
                  required
                />
              </div>
              <div className="modal-footer">
                <button type="button" className="btn btn-secondary" onClick={() => setShowIngestModal(false)}>Cancel</button>
                <button type="submit" className="btn btn-primary">Submit Ingestion</button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Fan Override Modal */}
      {showOverrideModal && (
        <div className="modal-overlay">
          <div className="modal-content">
            <div className="modal-header">
              <h3>Manual Fan Override (Admin)</h3>
              <button className="btn btn-secondary btn-sm" onClick={() => setShowOverrideModal(false)}>✕</button>
            </div>
            <form onSubmit={handleOverrideSubmit}>
              <div className="form-group">
                <label className="form-label">Target Silo</label>
                <input type="text" className="form-control" value={`${selectedSilo?.code} - ${selectedSilo?.name}`} disabled />
              </div>
              <div className="form-group">
                <label className="form-label">Fan Action</label>
                <select className="form-control" value={overrideAction} onChange={(e) => setOverrideAction(e.target.value)}>
                  <option value="ON">Turn Fan ON</option>
                  <option value="OFF">Turn Fan OFF</option>
                </select>
              </div>
              <div className="form-group">
                <label className="form-label">Reason / Justification</label>
                <input
                  type="text"
                  className="form-control"
                  value={overrideReason}
                  onChange={(e) => setOverrideReason(e.target.value)}
                  required
                />
              </div>
              <div className="modal-footer">
                <button type="button" className="btn btn-secondary" onClick={() => setShowOverrideModal(false)}>Cancel</button>
                <button type="submit" className="btn btn-primary">Execute Override</button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};
