import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { format } from 'date-fns';
import { CalendarDaysIcon, TableCellsIcon, UsersIcon, CheckCircleIcon, ClockIcon } from '@heroicons/react/24/outline';
import { useApp } from '../../context/AppContext';
import { Badge } from '../../components/UI';
import { errorMessage, reservationApi } from '../../services/api';
import { tableTitle } from '../../data';

const statusClass = (s: string) => ({ AVAILABLE: 'available', RESERVED: 'reserved', OCCUPIED: 'occupied', OUT_OF_SERVICE: 'out-of-service' }[s] ?? '');

export default function AdminDashboard() {
  const { tables, setTables } = useApp();
  const today = format(new Date(), 'yyyy-MM-dd');
  const [todayRes, setTodayRes] = useState<Awaited<ReturnType<typeof reservationApi.daily>>>([]);
  const [busy, setBusy] = useState(false);
  const [err, setErr] = useState('');

  useEffect(() => {
    reservationApi.tables().then(setTables).catch(() => {});
    reservationApi.daily(today).then(setTodayRes).catch(() => {});
  }, []);

  const upcoming = todayRes.filter(r => ['PENDING', 'CONFIRMED', 'CHECKED_IN'].includes(r.status));
  const available = tables.filter(t => t.currentStatus === 'AVAILABLE').length;
  const occupied  = tables.filter(t => t.currentStatus === 'OCCUPIED').length;
  const reserved  = tables.filter(t => t.currentStatus === 'RESERVED').length;
  const oos       = tables.filter(t => t.currentStatus === 'OUT_OF_SERVICE').length;

  const doAction = async (id: number, action: string) => {
    setBusy(true); setErr('');
    try {
      const updated = await reservationApi.action(id, action);
      setTodayRes(all => all.map(r => r.id === id ? updated : r));
    } catch (e) { setErr(errorMessage(e)); }
    finally { setBusy(false); }
  };

  return (
    <div className="page-enter">
      <div className="admin-header">
        <p className="eyebrow">{format(new Date(), 'EEEE, d MMMM yyyy')}</p>
        <h1>Good {new Date().getHours() < 12 ? 'morning' : new Date().getHours() < 18 ? 'afternoon' : 'evening'} 👋</h1>
        <p>Here's what's happening at Gather today.</p>
      </div>

      {err && <p className="error">{err}</p>}

      <div className="stat-grid">
        <div className="stat-card">
          <div className="stat-icon" style={{ background: 'var(--rausch-light)' }}><CalendarDaysIcon style={{ color: 'var(--rausch)' }} /></div>
          <div className="stat-value">{upcoming.length}</div>
          <div className="stat-label">Active reservations today</div>
        </div>
        <div className="stat-card">
          <div className="stat-icon" style={{ background: 'var(--babu-light)' }}><TableCellsIcon style={{ color: 'var(--babu)' }} /></div>
          <div className="stat-value">{available}</div>
          <div className="stat-label">Tables available</div>
          <div className="stat-trend up">↑ {occupied} occupied, {reserved} reserved</div>
        </div>
        <div className="stat-card">
          <div className="stat-icon" style={{ background: 'var(--arches-light)' }}><UsersIcon style={{ color: 'var(--arches)' }} /></div>
          <div className="stat-value">{occupied}</div>
          <div className="stat-label">Tables occupied now</div>
        </div>
        <div className="stat-card">
          <div className="stat-icon" style={{ background: 'var(--gray-100)' }}><CheckCircleIcon style={{ color: 'var(--foggy)' }} /></div>
          <div className="stat-value">{todayRes.filter(r => r.status === 'COMPLETED').length}</div>
          <div className="stat-label">Completed today</div>
        </div>
      </div>

      <section className="floor-section">
        <div className="row-between" style={{ marginBottom: 16 }}>
          <h2>Live floor map</h2>
          <Link to="/admin/tables" className="text-button">Manage tables →</Link>
        </div>
        <div className="floor-map">
          {tables.filter(t => t.currentStatus !== 'OUT_OF_SERVICE').map(t => (
            <div key={t.id} className={`table-tile ${statusClass(t.currentStatus)}`}>
              <span className="tile-num">{t.tableNumber}</span>
              <span className="tile-cap">{t.capacity} guests</span>
              <span className="tile-status">{t.currentStatus.replace('_', ' ').toLowerCase()}</span>
            </div>
          ))}
          {oos > 0 && (
            <div className="table-tile out-of-service">
              <span className="tile-num">+{oos}</span>
              <span className="tile-cap">OOS</span>
            </div>
          )}
        </div>
      </section>

      <section>
        <div className="row-between" style={{ marginBottom: 16 }}>
          <h2>Today's reservations</h2>
          <Link to="/admin/reservations" className="text-button">Full calendar →</Link>
        </div>
        {upcoming.length === 0 ? (
          <p className="muted small">No active reservations for today.</p>
        ) : (
          <div className="timeline">
            {upcoming.sort((a, b) => a.startTime.localeCompare(b.startTime)).map(r => (
              <div key={r.id} className="timeline-item">
                <span className="tl-time"><ClockIcon style={{ width: 14, height: 14, display: 'inline', marginRight: 4 }} />{r.startTime.slice(0, 5)}</span>
                <div className="tl-body">
                  <b>{r.contactName}</b>
                  <p>{r.table.tableNumber} · {tableTitle(r.table.location)} · {r.guestCount} guests</p>
                </div>
                <Badge status={r.status} />
                <div className="tl-actions">
                  {r.status === 'CONFIRMED' && (
                    <button className="primary" onClick={() => doAction(r.id, 'check-in')} disabled={busy}>Check in</button>
                  )}
                  {r.status === 'CHECKED_IN' && (
                    <button className="primary" onClick={() => doAction(r.id, 'complete')} disabled={busy}>Complete</button>
                  )}
                </div>
              </div>
            ))}
          </div>
        )}
      </section>
    </div>
  );
}
