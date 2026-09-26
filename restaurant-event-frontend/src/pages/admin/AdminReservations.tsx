import { useState, useEffect } from 'react';
import { format } from 'date-fns';
import { CalendarDaysIcon, ClockIcon, UsersIcon, MagnifyingGlassIcon } from '@heroicons/react/24/outline';
import { Badge, SectionHeading } from '../../components/UI';
import { errorMessage, reservationApi } from '../../services/api';
import { tableTitle } from '../../data';
import type { Reservation } from '../../types';

const ACTIONS: Record<string, { label: string; next: string }[]> = {
  CONFIRMED:  [{ label: 'Check in', next: 'check-in' }, { label: 'No-show', next: 'no-show' }],
  CHECKED_IN: [{ label: 'Complete', next: 'complete' }],
  PENDING:    [{ label: 'Confirm', next: 'confirm' }, { label: 'No-show', next: 'no-show' }],
};

export default function AdminReservations() {
  const [date, setDate] = useState(format(new Date(), 'yyyy-MM-dd'));
  const [statusFilter, setStatusFilter] = useState('');
  const [rows, setRows] = useState<Reservation[]>([]);
  const [loading, setLoading] = useState(false);
  const [busy, setBusy] = useState(false);
  const [err, setErr] = useState('');
  const [query, setQuery] = useState('');

  const load = async () => {
    setLoading(true); setErr('');
    try { setRows(await reservationApi.daily(date)); }
    catch (e) { setErr(errorMessage(e)); }
    finally { setLoading(false); }
  };

  useEffect(() => { void load(); }, [date]);

  const doAction = async (id: number, action: string) => {
    setBusy(true); setErr('');
    try {
      const updated = await reservationApi.action(id, action);
      setRows(all => all.map(r => r.id === id ? updated : r));
    } catch (e) { setErr(errorMessage(e)); }
    finally { setBusy(false); }
  };

  const filtered = rows.filter(r => {
    const matchStatus = !statusFilter || r.status === statusFilter;
    const matchQuery = !query || r.contactName.toLowerCase().includes(query.toLowerCase()) || r.bookingReference.toLowerCase().includes(query.toLowerCase());
    return matchStatus && matchQuery;
  });

  return (
    <div className="page-enter">
      <SectionHeading eyebrow="STAFF VIEW" title="Reservation calendar" description="Check in guests, mark completions, and manage today's floor." />

      <div style={{ display: 'flex', gap: 12, flexWrap: 'wrap', marginBottom: 24, alignItems: 'center' }}>
        <label style={{ flexDirection: 'row', alignItems: 'center', gap: 8, textTransform: 'none', letterSpacing: 0, fontSize: 14, fontWeight: 600, minWidth: 0 }}>
          <CalendarDaysIcon style={{ width: 16, height: 16 }} />
          <input type="date" value={date} onChange={e => setDate(e.target.value)} style={{ border: '1.5px solid var(--gray-200)', borderRadius: 'var(--radius-sm)', padding: '8px 12px', fontSize: 14, background: 'var(--white)' }} />
        </label>
        <select value={statusFilter} onChange={e => setStatusFilter(e.target.value)} style={{ padding: '8px 14px', border: '1.5px solid var(--gray-200)', borderRadius: 'var(--radius-sm)', fontSize: 14, background: 'var(--white)', cursor: 'pointer' }}>
          <option value="">All statuses</option>
          {['PENDING', 'CONFIRMED', 'CHECKED_IN', 'COMPLETED', 'CANCELLED', 'NO_SHOW'].map(s => <option key={s} value={s}>{s.replace('_', ' ')}</option>)}
        </select>
        <div className="inline-search" style={{ flex: 1, minWidth: 180 }}>
          <MagnifyingGlassIcon />
          <input placeholder="Search by name or reference…" value={query} onChange={e => setQuery(e.target.value)} />
        </div>
      </div>

      {err && <p className="error">{err}</p>}

      {loading ? (
        <div className="skeleton" style={{ height: 200 }} />
      ) : filtered.length === 0 ? (
        <p className="muted small">No reservations found for {date}.</p>
      ) : (
        <div className="timeline">
          {filtered.sort((a, b) => a.startTime.localeCompare(b.startTime)).map(r => (
            <div key={r.id} className="timeline-item">
              <span className="tl-time">{r.startTime.slice(0, 5)}</span>
              <div className="tl-body">
                <b>{r.contactName}</b>
                <p>
                  {tableTitle(r.table.location)} · {r.table.tableNumber} ·
                  <UsersIcon style={{ width: 12, height: 12, display: 'inline', marginLeft: 4, marginRight: 2 }} />
                  {r.guestCount} ·
                  <ClockIcon style={{ width: 12, height: 12, display: 'inline', marginLeft: 6, marginRight: 2 }} />
                  {r.startTime.slice(0, 5)}
                  <span style={{ fontSize: 11, letterSpacing: '.04em', color: 'var(--gray-300)', marginLeft: 8 }}>{r.bookingReference}</span>
                </p>
                {r.specialRequest && <p style={{ fontStyle: 'italic', color: 'var(--foggy)', fontSize: 12 }}>"{r.specialRequest}"</p>}
              </div>
              <Badge status={r.status} />
              <div className="tl-actions">
                {(ACTIONS[r.status] ?? []).map(({ label, next }) => (
                  <button key={next} className={next === 'check-in' || next === 'complete' || next === 'confirm' ? 'primary' : ''} disabled={busy} onClick={() => doAction(r.id, next)}>
                    {label}
                  </button>
                ))}
              </div>
            </div>
          ))}
        </div>
      )}

      <p className="small muted" style={{ marginTop: 16 }}>
        Showing {filtered.length} reservation{filtered.length !== 1 ? 's' : ''} for {date}.
      </p>
    </div>
  );
}
