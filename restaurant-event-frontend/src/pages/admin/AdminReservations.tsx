import { BookingPaymentBadge } from '../../components/BookingPayment';
import { customerPaymentApi, type CustomerPaymentDto } from '../../services/api';
import { useState, useEffect } from 'react';
import { CalendarDaysIcon, ClockIcon, UsersIcon, ArrowPathIcon, PhoneIcon, TableCellsIcon } from '@heroicons/react/24/outline';
import { Badge, SectionHeading } from '../../components/UI';
import { errorMessage, reservationApi, type ReservationHistory } from '../../services/api';
import { tableTitle, tableImage } from '../../data';
import { Modal } from '../../components/UI';
import { useApp } from '../../context/AppContext';
import { localToday, reservationError } from '../../services/validation';
import type { Reservation } from '../../types';

const ACTIONS: Record<string, { label: string; next: string }[]> = {
  CONFIRMED:  [{ label: 'Check in', next: 'check-in' }, { label: 'No-show', next: 'no-show' }],
  CHECKED_IN: [{ label: 'Complete', next: 'complete' }],
  PENDING:    [{ label: 'Confirm', next: 'confirm' }],
};

const EDITABLE = ['PENDING', 'CONFIRMED'];
const todayStr = () => localToday();

type ReservationForm = {
  customerEmail: string; tableId: number; reservationDate: string; startTime: string;
  guestCount: number; contactName: string; contactPhone: string; specialRequest: string;
};
const EMPTY_FORM: ReservationForm = {
  customerEmail: '', tableId: 0, reservationDate: todayStr(), startTime: '19:00',
  guestCount: 2, contactName: '', contactPhone: '', specialRequest: '',
};

export default function AdminReservations() {
  const app = useApp();
  const { tables, setTables } = app;
  const [modal, setModal] = useState<Reservation | 'new' | null>(null);
  const [form, setForm] = useState<ReservationForm>(EMPTY_FORM);
  const [formErr, setFormErr] = useState('');
  const [saving, setSaving] = useState(false);
  const [cancelTarget, setCancelTarget] = useState<Reservation | null>(null);
  const [cancelReason, setCancelReason] = useState('');

  const [historyFor, setHistoryFor] = useState<Reservation>();
  const [history, setHistory] = useState<ReservationHistory[]>([]);
  const [historyLoading, setHistoryLoading] = useState(false);
  const [historyError, setHistoryError] = useState('');
  const [date, setDate] = useState(localToday());
  const [statusFilter, setStatusFilter] = useState('');
  const [rows, setRows] = useState<Reservation[]>([]);
  const [payments, setPayments] = useState<CustomerPaymentDto[]>([]);
  const [paymentError, setPaymentError] = useState('');
  useEffect(() => { let active = true; const load = () => customerPaymentApi.reservations().then(data => { if (active) { setPayments(data); setPaymentError(''); } }).catch(e => { if (active) setPaymentError(errorMessage(e)); }); void load(); const timer = window.setInterval(() => void load(), 20000); return () => { active = false; window.clearInterval(timer); }; }, []);
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
    const table = tables.find(t => t.id === form.tableId);
    const validation = reservationError(form.reservationDate, form.startTime, form.guestCount, table?.capacity ?? 0, form.contactPhone, form.contactName);
    if (validation) { setFormErr(validation); return; }
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
    <div className="page-enter reservation-workspace">
      <SectionHeading eyebrow="STAFF VIEW" title="Reservation calendar" description="A clear view of every arrival, every table and every good moment." action={<div className="booking-payment-options">{app.user?.roles.some(r => ['ADMIN','MANAGER','WAITER'].includes(r)) && <button className="button primary" onClick={openNew}>Add reservation</button>}<button className="button" disabled={loading} onClick={() => void load()}><ArrowPathIcon /> Refresh</button></div>} />

      {historyFor && <Modal title={`Booking history · ${historyFor.bookingReference}`} onClose={() => setHistoryFor(undefined)}>
        {historyLoading ? <p role="status">Loading history…</p> : historyError ? <p className="error" role="alert">{historyError}</p> : history.length ? <ol className="reservation-history">{history.map(h => <li key={h.id}><strong>{h.action.replace('RESERVATION_', '').replaceAll('_',' ')}</strong><p>{h.previousStatus ? `${h.previousStatus.replaceAll('_',' ')} → ` : ''}{h.status.replaceAll('_',' ')}</p><small>{new Date(h.createdAt + '+05:30').toLocaleString('en-LK', {timeZone:'Asia/Colombo'})}{h.actorId ? ` · User #${h.actorId}` : ''}</small></li>)}</ol> : <p>No recorded changes yet. History starts with changes made after this update.</p>}
      </Modal>}
      <div className="reservation-overview">
        {[
          { label: 'Reservations', value: rows.length, icon: CalendarDaysIcon },
          { label: 'Guests expected', value: rows.filter(r => !['CANCELLED','NO_SHOW'].includes(r.status)).reduce((n,r) => n+r.guestCount,0), icon: UsersIcon },
          { label: 'Awaiting arrival', value: rows.filter(r => ['CONFIRMED','PENDING'].includes(r.status)).length, icon: ClockIcon },
          { label: 'Dining now', value: rows.filter(r => r.status === 'CHECKED_IN').length, icon: TableCellsIcon },
        ].map(k => <article key={k.label}><k.icon /><div><strong>{k.value}</strong><span>{k.label}</span></div></article>)}
      </div>
      <div className="reservation-toolbar">
        <label>Service date<input required type="date" value={date} onChange={e => { if(e.target.value) setDate(e.target.value); }} /></label>
        <label>Status<select value={statusFilter} onChange={e => setStatusFilter(e.target.value)}>
          <option value="">All statuses</option>
          {['PENDING','CONFIRMED','CHECKED_IN','COMPLETED','CANCELLED','NO_SHOW'].map(s => <option key={s} value={s}>{s.replaceAll('_',' ')}</option>)}
        </select></label>
        <label>Find a reservation<input type="search" placeholder="Guest name or booking reference" value={query} onChange={e => setQuery(e.target.value)} /></label>
      </div>

      {err && <p className="error">{err}</p>}

      {loading ? (
        <div className="skeleton" style={{ height: 200 }} />
      ) : filtered.length === 0 ? (
        <p className="muted small">No reservations found for {date}.</p>
      ) : (
        <div className="timeline">
          {filtered.sort((a, b) => a.startTime.localeCompare(b.startTime)).map(r => (
            <div key={r.id} className="timeline-item reservation-arrival">
              <img className="reservation-arrival-photo" src={tableImage(r.table.location, r.table)} alt={tableTitle(r.table.location, r.table)} />
              <span className="tl-time">{r.startTime.slice(0, 5)}</span>
              <div className="tl-body">
                <b>{r.contactName}</b>
                <p>
                  {tableTitle(r.table.location, r.table)} · {r.table.tableNumber} ·
                  <UsersIcon style={{ width: 12, height: 12, display: 'inline', marginLeft: 4, marginRight: 2 }} />
                  {r.guestCount} ·
                  <ClockIcon style={{ width: 12, height: 12, display: 'inline', marginLeft: 6, marginRight: 2 }} />
                  {r.startTime.slice(0, 5)}
                  <span style={{ fontSize: 11, letterSpacing: '.04em', color: 'var(--gray-300)', marginLeft: 8 }}>{r.bookingReference}</span>
                </p>
                <p className="reservation-contact"><PhoneIcon />{r.contactPhone}</p>
                {r.specialRequest && <p style={{ fontStyle: 'italic', color: 'var(--foggy)', fontSize: 12 }}>"{r.specialRequest}"</p>}
              </div>
              {paymentError ? <span className="muted small">Payment status unavailable</span> : <BookingPaymentBadge payment={payments.find(p => p.tableReservationId === r.id)} deposit />}
              <Badge status={r.status} />
              <div className="tl-actions">
                {EDITABLE.includes(r.status) && <button disabled={busy} onClick={() => openEdit(r)}>Edit</button>}
                {EDITABLE.includes(r.status) && <button disabled={busy} onClick={() => { setCancelTarget(r); setCancelReason(''); setFormErr(''); }}>Cancel</button>}

                <button onClick={async () => {setHistoryFor(r);setHistory([]);setHistoryError('');setHistoryLoading(true);try {setHistory(await reservationApi.history(r.id));} catch(e) {setHistoryError(errorMessage(e));} finally {setHistoryLoading(false);}}}>History</button>
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
              <label>Time<input type="time" min="11:00" max="21:00" value={form.startTime} onChange={e => setForm({ ...form, startTime: e.target.value })} /></label>
            </div>
            <label>Guests<input type="number" min={1} max={200} value={form.guestCount} onChange={e => setForm({ ...form, guestCount: Number(e.target.value) })} /></label>
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
