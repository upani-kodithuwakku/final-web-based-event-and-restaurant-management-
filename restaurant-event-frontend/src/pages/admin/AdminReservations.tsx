import { useState, useEffect } from 'react';
import { format } from 'date-fns';
import { CalendarDaysIcon, ClockIcon, UsersIcon, MagnifyingGlassIcon, PlusIcon } from '@heroicons/react/24/outline';
import { useApp } from '../../context/AppContext';
import { Badge, Modal, SectionHeading } from '../../components/UI';
import { errorMessage, reservationApi } from '../../services/api';
import { tableTitle } from '../../data';
import type { Reservation } from '../../types';

const ACTIONS: Record<string, { label: string; next: string }[]> = {
  CONFIRMED:  [{ label: 'Check in', next: 'check-in' }, { label: 'No-show', next: 'no-show' }],
  CHECKED_IN: [{ label: 'Complete', next: 'complete' }],
  PENDING:    [{ label: 'Confirm', next: 'confirm' }, { label: 'No-show', next: 'no-show' }],
};

const EDITABLE = ['PENDING', 'CONFIRMED'];
const todayStr = () => format(new Date(), 'yyyy-MM-dd');

type ReservationForm = {
  customerEmail: string; tableId: number; reservationDate: string; startTime: string;
  guestCount: number; contactName: string; contactPhone: string; specialRequest: string;
};
const EMPTY_FORM: ReservationForm = {
  customerEmail: '', tableId: 0, reservationDate: todayStr(), startTime: '19:00',
  guestCount: 2, contactName: '', contactPhone: '', specialRequest: '',
};

export default function AdminReservations() {
  const { tables, setTables } = useApp();
  const [modal, setModal] = useState<Reservation | 'new' | null>(null);
  const [form, setForm] = useState<ReservationForm>(EMPTY_FORM);
  const [formErr, setFormErr] = useState('');
  const [saving, setSaving] = useState(false);
  const [cancelTarget, setCancelTarget] = useState<Reservation | null>(null);
  const [cancelReason, setCancelReason] = useState('');
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

  useEffect(() => {
    if (tables.length === 0) reservationApi.tables().then(setTables).catch(() => {});
  }, []);

  const bookableTables = tables.filter(t => t.isActive && t.currentStatus !== 'OUT_OF_SERVICE');

  const openNew = () => {
    setForm({ ...EMPTY_FORM, reservationDate: date < todayStr() ? todayStr() : date, tableId: bookableTables[0]?.id ?? 0 });
    setFormErr(''); setModal('new');
  };

  const openEdit = (r: Reservation) => {
    setForm({
      customerEmail: '', tableId: r.table.id, reservationDate: r.reservationDate, startTime: r.startTime.slice(0, 5),
      guestCount: r.guestCount, contactName: r.contactName, contactPhone: r.contactPhone, specialRequest: r.specialRequest ?? '',
    });
    setFormErr(''); setModal(r);
  };

  const saveForm = async () => {
    setSaving(true); setFormErr('');
    try {
      const { customerEmail, ...details } = form;
      const body = { ...details, specialRequest: form.specialRequest || undefined };
      const saved = modal === 'new'
        ? await reservationApi.adminCreate({ ...body, customerEmail })
        : await reservationApi.adminUpdate((modal as Reservation).id, body);
      setModal(null);
      if (saved.reservationDate !== date) setDate(saved.reservationDate);
      else await load();
    } catch (e) { setFormErr(errorMessage(e)); }
    finally { setSaving(false); }
  };

  const confirmCancel = async () => {
    if (!cancelTarget) return;
    setSaving(true); setFormErr('');
    try {
      const updated = await reservationApi.adminCancel(cancelTarget.id, cancelReason);
      setRows(all => all.map(r => r.id === updated.id ? updated : r));
      setCancelTarget(null); setCancelReason('');
    } catch (e) { setFormErr(errorMessage(e)); }
    finally { setSaving(false); }
  };

  const formValid = form.tableId > 0 && form.guestCount >= 1 && form.contactName.trim() !== '' && form.contactPhone.trim() !== ''
    && (modal !== 'new' || form.customerEmail.includes('@'));

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
      <SectionHeading eyebrow="STAFF VIEW" title="Reservation calendar" description="Check in guests, mark completions, and manage today's floor."
        action={<button className="button primary" onClick={openNew}><PlusIcon style={{ width: 16, height: 16 }} /> New reservation</button>} />

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
                {r.status === 'CANCELLED' && r.cancelReason && <p style={{ fontSize: 12, color: 'var(--foggy)' }}>Cancelled: {r.cancelReason}</p>}
                {r.specialRequest && <p style={{ fontStyle: 'italic', color: 'var(--foggy)', fontSize: 12 }}>"{r.specialRequest}"</p>}
              </div>
              <Badge status={r.status} />
              <div className="tl-actions">
                {(ACTIONS[r.status] ?? []).map(({ label, next }) => (
                  <button key={next} className={next === 'check-in' || next === 'complete' || next === 'confirm' ? 'primary' : ''} disabled={busy} onClick={() => doAction(r.id, next)}>
                    {label}
                  </button>
                ))}
                {EDITABLE.includes(r.status) && <button disabled={busy} onClick={() => openEdit(r)}>Edit</button>}
                {EDITABLE.includes(r.status) && <button disabled={busy} onClick={() => { setCancelTarget(r); setCancelReason(''); setFormErr(''); }}>Cancel</button>}
              </div>
            </div>
          ))}
        </div>
      )}

      <p className="small muted" style={{ marginTop: 16 }}>
        Showing {filtered.length} reservation{filtered.length !== 1 ? 's' : ''} for {date}.
      </p>

      {modal !== null && (
        <Modal title={modal === 'new' ? 'New reservation' : `Edit ${(modal as Reservation).bookingReference}`} onClose={() => setModal(null)}>
          <div className="input-group">
            {modal === 'new' && (
              <label>Customer email<input type="email" value={form.customerEmail} onChange={e => setForm({ ...form, customerEmail: e.target.value })} placeholder="customer@example.com" /></label>
            )}
            <label>Table
              <select value={form.tableId} onChange={e => setForm({ ...form, tableId: Number(e.target.value) })}>
                {bookableTables.map(t => <option key={t.id} value={t.id}>{t.tableNumber} · {t.location.toLowerCase()} · seats {t.capacity}</option>)}
                {modal !== 'new' && !bookableTables.some(t => t.id === form.tableId) && <option value={form.tableId}>{(modal as Reservation).table.tableNumber} (current)</option>}
              </select>
            </label>
            <div className="form-row">
              <label>Date<input type="date" min={todayStr()} value={form.reservationDate} onChange={e => setForm({ ...form, reservationDate: e.target.value })} /></label>
              <label>Time<input type="time" value={form.startTime} onChange={e => setForm({ ...form, startTime: e.target.value })} /></label>
            </div>
            <label>Guests<input type="number" min={1} max={30} value={form.guestCount} onChange={e => setForm({ ...form, guestCount: Number(e.target.value) })} /></label>
            <div className="form-row">
              <label>Contact name<input value={form.contactName} onChange={e => setForm({ ...form, contactName: e.target.value })} /></label>
              <label>Contact phone<input value={form.contactPhone} onChange={e => setForm({ ...form, contactPhone: e.target.value })} /></label>
            </div>
            <label>Special request<textarea rows={2} value={form.specialRequest} onChange={e => setForm({ ...form, specialRequest: e.target.value })} /></label>
            {formErr && <p className="error">{formErr}</p>}
            <button className="button primary full" disabled={saving || !formValid} onClick={saveForm}>
              {saving ? 'Saving…' : modal === 'new' ? 'Create reservation' : 'Save changes'}
            </button>
          </div>
        </Modal>
      )}

      {cancelTarget && (
        <Modal title={`Cancel ${cancelTarget.bookingReference}`} onClose={() => setCancelTarget(null)}>
          <div className="input-group">
            <p className="muted small">{cancelTarget.contactName} · {cancelTarget.table.tableNumber} · {cancelTarget.startTime.slice(0, 5)}. The customer will be notified.</p>
            <label>Reason (optional)<textarea rows={2} value={cancelReason} onChange={e => setCancelReason(e.target.value)} /></label>
            {formErr && <p className="error">{formErr}</p>}
            <button className="button primary full" disabled={saving} onClick={confirmCancel}>{saving ? 'Cancelling…' : 'Cancel reservation'}</button>
          </div>
        </Modal>
      )}
    </div>
  );
}
