import EventCatalogManager from '../../components/EventCatalogManager';
import { BookingPaymentBadge } from '../../components/BookingPayment';
import { customerPaymentApi, type CustomerPaymentDto } from '../../services/api';
import { useState, useEffect } from 'react';
import { SparklesIcon, FunnelIcon } from '@heroicons/react/24/outline';
import { Badge, Empty, Modal, SectionHeading } from '../../components/UI';
import { eventCoordinatorApi, type EventBookingDto, errorMessage } from '../../services/api';
import { money } from '../../data';

export default function AdminEvents() {
  const [events, setEvents] = useState<EventBookingDto[]>([]);
  const [payments, setPayments] = useState<CustomerPaymentDto[]>([]);
  const [paymentError, setPaymentError] = useState('');
  useEffect(() => { let active = true; const load = () => customerPaymentApi.events().then(data => { if (active) { setPayments(data); setPaymentError(''); } }).catch(e => { if (active) setPaymentError(errorMessage(e)); }); void load(); const timer = window.setInterval(() => void load(), 20000); return () => { active = false; window.clearInterval(timer); }; }, []);
  const [loading, setLoading] = useState(true);
  const [filter, setFilter] = useState('');
  const [selected, setSelected] = useState<EventBookingDto | null>(null);
  const [rejectNote, setRejectNote] = useState('');
  const [busy, setBusy] = useState(false);
  const [err, setErr] = useState('');

  const load = async () => {
    setLoading(true);
    try { setEvents(await eventCoordinatorApi.list()); }
    catch { setErr('Failed to load event bookings.'); }
    finally { setLoading(false); }
  };

  useEffect(() => { void load(); }, []);

  const approve = async (id: number) => {
    setBusy(true); setErr('');
    try {
      const updated = await eventCoordinatorApi.approve(id);
      setEvents(all => all.map(e => e.id === id ? updated : e));
      setSelected(null);
    } catch (e) { setErr(errorMessage(e)); }
    finally { setBusy(false); }
  };

  const reject = async (id: number) => {
    if (!rejectNote.trim()) { setErr("Please enter a rejection reason."); return; }
    setBusy(true); setErr('');
    try {
      const updated = await eventCoordinatorApi.reject(id, rejectNote);
      setEvents(all => all.map(e => e.id === id ? updated : e));
      setSelected(null); setRejectNote('');
    } catch (e) { setErr(errorMessage(e)); }
    finally { setBusy(false); }
  };

  const filtered = events.filter(e => !filter || e.status === filter);

  return (
    <div className="page-enter">
      <SectionHeading
        eyebrow="EVENT COORDINATOR"
        title="Event bookings"
        description="Review enquiries, approve celebrations, and manage your event calendar."
        action={
          <div style={{ display: 'flex', gap: 8, alignItems: 'center' }}>
            <FunnelIcon style={{ width: 16, height: 16, color: 'var(--foggy)' }} />
            <select value={filter} onChange={e => setFilter(e.target.value)} style={{ padding: '8px 14px', border: '1.5px solid var(--gray-200)', borderRadius: 'var(--radius-sm)', fontSize: 14, background: 'var(--white)', cursor: 'pointer' }}>
              <option value="">All statuses</option>
              {['PENDING', 'CONFIRMED', 'REJECTED', 'CANCELLED'].map(s => <option key={s} value={s}>{s.charAt(0) + s.slice(1).toLowerCase()}</option>)}
            </select>
          </div>
        }
      />

      {err && <p className="error">{err}</p>}

      {loading ? <div className="skeleton" style={{ height: 200 }} /> : filtered.length === 0 ? (
        <Empty title="No event bookings">
          <p>Event enquiries from customers will appear here for review.</p>
        </Empty>
      ) : (
        <div className="event-booking-list">
          {filtered.map(e => (
            <div key={e.id} className="event-booking-row">
              <div className="eb-info">
                <h3>{e.hallName}</h3>
                <p>
                  {e.packageName} · {String(e.eventDate)} · {e.guestCount} guests ·{' '}
                  <span style={{ fontSize: 11, color: 'var(--gray-300)' }}>{e.bookingReference}</span>
                  {e.specialRequirements && <span style={{ marginLeft: 8, fontStyle: 'italic' }}>— "{e.specialRequirements.slice(0, 60)}{e.specialRequirements.length > 60 ? '…' : ''}"</span>}
                </p>
              </div>
              {paymentError ? <span className="muted small">Payment status unavailable</span> : <BookingPaymentBadge payment={payments.find(p => p.eventBookingId === e.id)} />}
              <Badge status={e.status} />
              <div className="eb-actions">
                {e.status === 'PENDING' && (
                  <>
                    <button className="button primary" style={{ padding: '7px 14px', fontSize: 13 }} disabled={busy} onClick={() => approve(e.id)}>Approve</button>
                    <button className="button" style={{ padding: '7px 14px', fontSize: 13 }} onClick={() => { setSelected(e); setRejectNote(''); }}>Reject</button>
                  </>
                )}
                {e.status !== 'PENDING' && (
                  <button className="text-button" style={{ fontSize: 13 }} onClick={() => setSelected(e)}>View details</button>
                )}
              </div>
            </div>
          ))}
        </div>
      )}

      <EventCatalogManager />

      {selected && (
        <Modal title="Event booking details" onClose={() => setSelected(null)}>
          <div style={{ display: 'flex', flexDirection: 'column', gap: 8 }}>
            <div className="report-row"><span>Reference</span><b>{selected.bookingReference}</b></div>
            <div className="report-row"><span>Hall</span><b>{selected.hallName}</b></div>
            <div className="report-row"><span>Package</span><b>{selected.packageName}</b></div>
            <div className="report-row"><span>Date</span><b>{String(selected.eventDate)}</b></div>
            <div className="report-row"><span>Time</span><b>{String(selected.startTime).slice(0, 5)} – {String(selected.endTime).slice(0, 5)}</b></div>
            <div className="report-row"><span>Guests</span><b>{selected.guestCount}</b></div>
            <div className="report-row"><span>Deposit</span><b>{money(selected.depositAmount)}</b></div>
            <div className="report-row"><span>Status</span><Badge status={selected.status} /></div>
            {selected.specialRequirements && (
              <div style={{ background: 'var(--gray-50)', borderRadius: 'var(--radius-sm)', padding: '12px 16px', fontSize: 14 }}>
                <p className="eyebrow" style={{ marginBottom: 4 }}>Special requirements</p>
                <p>{selected.specialRequirements}</p>
              </div>
            )}
            {selected.rejectionReason && (
              <div style={{ background: 'var(--rausch-light)', borderRadius: 'var(--radius-sm)', padding: '12px 16px', fontSize: 14 }}>
                <p className="eyebrow" style={{ marginBottom: 4 }}>Rejection reason</p>
                <p>{selected.rejectionReason}</p>
              </div>
            )}
            {selected.status === 'PENDING' && (
              <>
                <div className="divider" />
                <label>Rejection reason<textarea maxLength={500} value={rejectNote} onChange={e => setRejectNote(e.target.value)} placeholder="Let the customer know why…" /></label>
                {err && <p className="error">{err}</p>}
                <div className="button-row">
                  <button className="button primary" disabled={busy} onClick={() => approve(selected.id)}>Approve booking</button>
                  <button className="button" disabled={busy} onClick={() => reject(selected.id)}>Reject</button>
                </div>
              </>
            )}
          </div>
        </Modal>
      )}
    </div>
  );
}
