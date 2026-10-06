import { useEffect, useState } from 'react';
import { customerPaymentApi, errorMessage, type BillLineDto, type CustomerPaymentDto, type PaymentSummaryDto } from '../services/api';
import { money } from '../data';
import { Modal } from './UI';
import PaymentGateway from './PaymentGateway';

export function useBookingPayments(enabled: boolean, refreshKey?: unknown) {
  const [summary, setSummary] = useState<PaymentSummaryDto | null>(null);
  const [error, setError] = useState('');
  useEffect(() => {
    if (!enabled) { setSummary(null); return; }
    let active = true;
    const load = () => customerPaymentApi.summary().then(data => { if (active) { setSummary(data); setError(''); } }).catch(e => { if (active) setError(errorMessage(e)); });
    void load();
    const timer = window.setInterval(() => void load(), 20000);
    return () => { active = false; window.clearInterval(timer); };
  }, [enabled, refreshKey]);
  return { summary, error };
}

export function BookingPaymentBadge({ payment, deposit = false }: { payment?: CustomerPaymentDto | BillLineDto; deposit?: boolean }) {
  const status = payment && ('status' in payment ? payment.status : payment.paymentStatus);
  const method = payment && ('method' in payment ? payment.method : payment.paymentMethod);
  const amount = payment && ('amount' in payment ? payment.amount : payment.total);
  const label = deposit ? 'Deposit' : 'Payment';
  const text = status === 'PAID' ? `${label} paid${method === 'CARD' ? ` · card${payment?.cardLast4 ? ` •••• ${payment.cardLast4}` : ''}` : ' · outlet'}`
    : status === 'PENDING' && method === 'PAY_AT_OUTLET' ? `Pay ${money(amount || 0)} at the outlet`
    : status === 'REFUNDED' ? `${label} refunded` : `${label} not paid`;
  return <p className={`pay-chip ${status === 'PAID' ? 'paid' : method === 'PAY_AT_OUTLET' ? 'outlet' : 'waiting'}`}>{text}</p>;
}

export default function BookingPayment({ purpose, targetId, onClose, onDone }: {
  purpose: 'TABLE_RESERVATION' | 'EVENT_BOOKING'; targetId: number; onClose: () => void; onDone: () => void;
}) {
  const [line, setLine] = useState<BillLineDto>();
  const [error, setError] = useState('');
  const [card, setCard] = useState(false);
  const [busy, setBusy] = useState(false);
  useEffect(() => { let active = true; customerPaymentApi.summary().then(s => {
    if (!active) return;
    const found = (purpose === 'TABLE_RESERVATION' ? s.tableReservations : s.eventBookings).find(l => l.targetId === targetId);
    if (found) setLine(found); else setError('This bill is not available. Please refresh your bookings.');
  }).catch(e => { if (active) setError(errorMessage(e)); }); return () => { active = false; }; }, [purpose, targetId]);
  const target = purpose === 'TABLE_RESERVATION' ? { tableReservationId: targetId } : { eventBookingId: targetId };
  const done = () => { onDone(); onClose(); };
  if (card && line) return <PaymentGateway amount={line.total} reference={line.reference} steps={['Booking', 'Card payment']} doneLabel="Back to bookings"
    pay={(details) => line.paymentId ? customerPaymentApi.changeMethod(line.paymentId, { method: 'CARD', card: details }) : customerPaymentApi.create({ ...target, method: 'CARD', card: details })}
    onClose={() => setCard(false)} onDone={done} />;
  return <Modal title={purpose === 'TABLE_RESERVATION' ? 'Your reservation deposit' : 'Pay for your celebration'} onClose={onClose}>
    {error && <p className="error" role="alert">{error}</p>}
    {!line && !error && <p role="status">Loading your bill…</p>}
    {line && <><div className="booking-bill-summary">
      <span className="eyebrow">{line.reference}</span>
      <h3>{line.description}</h3>
      {line.eventDate && <p className="muted">{line.eventDate}</p>}
      <dl className="celebration-price-review">
        <div><dt>{purpose === 'TABLE_RESERVATION' ? 'Reservation deposit' : 'Celebration package'}</dt><dd>{money(line.subtotal)}</dd></div>
        <div><dt>Service charge</dt><dd>{money(line.serviceCharge)}</dd></div>
        <div><dt>Total due</dt><dd>{money(line.total)}</dd></div>
      </dl>
    </div><BookingPaymentBadge payment={line} deposit={purpose === 'TABLE_RESERVATION'} />
      {!line.payable && <p className="celebration-enquiry-note">Payment is available after your booking is confirmed.</p>}
      {line.payable && line.paymentStatus !== 'PAID' && line.paymentStatus !== 'REFUNDED' && <div className="booking-payment-options">
        <button className="button primary" disabled={busy} onClick={() => setCard(true)}>{purpose === 'TABLE_RESERVATION' ? 'Pay deposit with card' : 'Pay with card'}</button>
        <button className="button" disabled={busy} onClick={async () => { setBusy(true); setError(''); try {
          if (line.paymentId) await customerPaymentApi.changeMethod(line.paymentId, { method: 'PAY_AT_OUTLET' });
          else await customerPaymentApi.create({ ...target, method: 'PAY_AT_OUTLET' });
          done();
        } catch (e) { setError(errorMessage(e)); } finally { setBusy(false); } }}>{busy ? 'Saving…' : 'Pay at the outlet'}</button>
      </div>}
      <p className="gw-hint">Card checkout is a simulation. No money will be charged. Outlet payments stay pending until collected by staff.</p>
    </>}
    <button className="text-button" onClick={onClose} disabled={busy}>Skip for now</button>
  </Modal>;
}
