import { useEffect, useState } from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import { format, parseISO } from 'date-fns';
import { CreditCardIcon, BuildingStorefrontIcon, CheckCircleIcon, ClockIcon, ArrowPathIcon, ArrowLeftIcon } from '@heroicons/react/24/outline';
import { SectionHeading, Empty } from '../components/UI';
import PaymentGateway from '../components/PaymentGateway';
import { customerPaymentApi, errorMessage, type BillLineDto, type PaymentSummaryDto } from '../services/api';
import { useApp } from '../context/AppContext';
import { money } from '../data';


export default function Payments() {
  const { user } = useApp();
  const isCustomer = !!user?.roles.includes('CUSTOMER');
  const [bill, setBill] = useState<PaymentSummaryDto | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [notice, setNotice] = useState('');
  const [busyId, setBusyId] = useState<string | null>(null);
  const [cardFor, setCardFor] = useState<BillLineDto | null>(null);
  // ?order=<id> or ?event=<id> opens that one bill instead of the whole summary.
  const [params] = useSearchParams();
  const focusOrder = Number(params.get('order')) || null;
  const focusReservation = Number(params.get('reservation')) || null;
  const focusEvent = Number(params.get('event')) || null;

  const load = async () => {
    try { setBill(await customerPaymentApi.summary()); setError(''); }
    catch (e) { setError(errorMessage(e)); }
    finally { setLoading(false); }
  };
  useEffect(() => {
    if (!isCustomer) return;
    void load();
    // Staff confirm orders while the customer waits, so refresh now and then.
    const timer = window.setInterval(() => void load(), 20000);
    return () => window.clearInterval(timer);
  }, [isCustomer]);

  if (!isCustomer) return <div className="page-container"><Empty title="Payments are for customer accounts">
    <p>Sign in with a customer account to see your bill.</p></Empty></div>;

  const key = (line: BillLineDto) => `${line.purpose}-${line.targetId}`;

  const payAtOutlet = async (line: BillLineDto) => {
    setBusyId(key(line)); setError(''); setNotice('');
    try {
      if (line.paymentId) await customerPaymentApi.changeMethod(line.paymentId, { method: 'PAY_AT_OUTLET' });
      else await customerPaymentApi.create({ ...target(line), method: 'PAY_AT_OUTLET' });
      setNotice(`${line.reference}: we'll collect ${money(line.total)} when you visit us.`);
      await load();
    } catch (e) { setError(errorMessage(e)); }
    finally { setBusyId(null); }
  };

  const focused = !focusOrder && !focusEvent && !focusReservation ? null
    : (focusOrder ? bill?.foodOrders.find(l => l.targetId === focusOrder) : focusEvent ? bill?.eventBookings.find(l => l.targetId === focusEvent) : bill?.tableReservations.find(l => l.targetId === focusReservation)) ?? null;
  const hasFood = !!bill?.foodOrders.length;
  const hasEvents = !!bill?.eventBookings.length;
  const hasReservations = !!bill?.tableReservations.length;
  const showGrand = [hasFood, hasEvents, hasReservations].filter(Boolean).length >= 2;

  return (
    <div className="page-container page-enter payments-page">
      <SectionHeading eyebrow="YOUR BILL" title="Payments"
        description="Pay by card now, or choose to pay when you visit us. Your totals are always shown here."
        action={<button className="button" onClick={() => { setLoading(true); void load(); }}><ArrowPathIcon /> Refresh</button>} />

      {notice && <p className="menu-notice success" role="status"><CheckCircleIcon />{notice}</p>}
      {error && <p className="error" role="alert">{error}</p>}

      {(focusOrder || focusEvent || focusReservation) && bill ? (
        focused ? <SingleBill line={focused} busy={busyId === key(focused)} onCard={() => setCardFor(focused)} onOutlet={() => void payAtOutlet(focused)} />
          : <Empty title="We couldn't find that bill"><Link className="text-button" to="/payments">See all your payments</Link></Empty>
      ) : loading && !bill ? <div className="skeleton" style={{ height: 260 }} /> : !hasFood && !hasEvents && !hasReservations ? (
        <Empty title="Nothing to pay yet">
          <p>Once you reserve a table, order food or book a celebration, your bill will appear here.</p>
          <p style={{ display: 'flex', gap: 16, justifyContent: 'center', marginTop: 12 }}>
            <Link className="text-button" to="/menu">Browse the menu</Link>
            <Link className="text-button" to="/events">Plan a celebration</Link>
          </p>
        </Empty>
      ) : bill && (
        <div className="bill-layout">
          <div className="bill-sections">
            {hasFood && <BillSection title="Food orders" lines={bill.foodOrders} total={bill.foodTotal} totalLabel="Food total"
              busyId={busyId} lineKey={key} onCard={setCardFor} onOutlet={payAtOutlet} />}
            {hasEvents && <BillSection title="Events & celebrations" lines={bill.eventBookings} total={bill.eventTotal} totalLabel="Events total"
              busyId={busyId} lineKey={key} onCard={setCardFor} onOutlet={payAtOutlet} />}
            {hasReservations && <BillSection title="Table reservations" lines={bill.tableReservations} total={bill.reservationTotal} totalLabel="Reservation deposits"
              busyId={busyId} lineKey={key} onCard={setCardFor} onOutlet={payAtOutlet} />}
          </div>

          <aside className="bill-summary" aria-label="Bill summary">
            <h3>Summary</h3>
            {bill.foodTotal != null && <div className="report-row"><span>Food total</span><b>{money(bill.foodTotal)}</b></div>}
            {bill.eventTotal != null && <div className="report-row"><span>Events total</span><b>{money(bill.eventTotal)}</b></div>}
            {bill.reservationTotal != null && <div className="report-row"><span>Reservation deposits</span><b>{money(bill.reservationTotal)}</b></div>}
            {showGrand && <div className="report-row bill-grand"><strong>Full total</strong><strong>{money(bill.grandTotal ?? 0)}</strong></div>}
            {bill.grandTotal == null
              ? <p className="muted small">Totals appear once our staff confirm your order or booking.</p>
              : <div className="bill-progress">
                  <div className="report-row"><span>Paid so far</span><b className="paid-text">{money(bill.amountPaid)}</b></div>
                  <div className="report-row"><span>Still to pay</span><b>{money(bill.amountDue)}</b></div>
                  <p className="muted small">Amounts you chose to pay at the outlet are included in “Still to pay”.</p>
                </div>}
          </aside>
        </div>
      )}

      {cardFor && <PaymentGateway amount={cardFor.total} reference={cardFor.reference} doneLabel="Back to payments"
        steps={['Your bill', 'Card payment']}
        pay={(card) => cardFor.paymentId
          ? customerPaymentApi.changeMethod(cardFor.paymentId, { method: 'CARD', card })
          : customerPaymentApi.create({ ...target(cardFor), method: 'CARD', card })}
        onClose={() => setCardFor(null)}
        onDone={async payment => {
          setCardFor(null); setNotice(`Payment ${payment.paymentReference} received: ${money(payment.amount)} for ${cardFor.reference}.`);
          await load();
        }} />}
    </div>
  );
}

const target = (line: BillLineDto) =>
  line.purpose === 'FOOD_ORDER' ? { foodOrderId: line.targetId } : line.purpose === 'EVENT_BOOKING' ? { eventBookingId: line.targetId } : { tableReservationId: line.targetId };

/** One order's or booking's bill: itemised, with its own total and payment options. */
function SingleBill({ line, busy, onCard, onOutlet }: { line: BillLineDto; busy: boolean; onCard: () => void; onOutlet: () => void }) {
  const isFood = line.purpose === 'FOOD_ORDER';
  return (
    <div className="single-bill">
      <Link className="text-button pay-back" to="/payments"><ArrowLeftIcon /> All payments</Link>
      <article className="bill-section receipt">
        <header className="receipt-head">
          <div>
            <span className="eyebrow">{isFood ? 'FOOD ORDER' : line.purpose === 'TABLE_RESERVATION' ? 'TABLE RESERVATION' : 'EVENT BOOKING'}</span>
            <h2>{line.reference}</h2>
            <p className="muted small">
              {line.eventDate ? `Event on ${format(parseISO(line.eventDate), 'd MMM yyyy')}` : `Ordered ${format(new Date(line.createdAt), 'd MMM yyyy, h:mm a')}`}
              {' · '}{line.targetStatus.charAt(0) + line.targetStatus.slice(1).toLowerCase().replace(/_/g, ' ')}
            </p>
          </div>
        </header>
        <table className="receipt-items">
          <thead><tr><th>Item</th><th>Qty</th><th>Price</th><th>Amount</th></tr></thead>
          <tbody>{line.items.map((it, i) => (
            <tr key={`${it.name}-${i}`}><td>{it.name}</td><td>{it.quantity}</td><td>{money(it.unitPrice)}</td><td>{money(it.lineTotal)}</td></tr>
          ))}</tbody>
        </table>
        <div className="receipt-totals">
          <div className="report-row"><span>Subtotal</span><b>{money(line.subtotal)}</b></div>
          {line.serviceCharge > 0 && <div className="report-row"><span>Service charge (10%)</span><b>{money(line.serviceCharge)}</b></div>}
          <div className="report-row bill-grand"><strong>Total</strong><strong>{money(line.total)}</strong></div>
        </div>
        <PaymentState line={line} busy={busy} onCard={onCard} onOutlet={onOutlet} />
      </article>
    </div>
  );
}

function BillSection({ title, lines, total, totalLabel, busyId, lineKey, onCard, onOutlet }: {
  title: string; lines: BillLineDto[]; total: number | null; totalLabel: string; busyId: string | null;
  lineKey: (l: BillLineDto) => string; onCard: (l: BillLineDto) => void; onOutlet: (l: BillLineDto) => void;
}) {
  return (
    <section className="bill-section">
      <h2>{title}</h2>
      {lines.map(line => (
        <article className={`bill-line${line.payable ? '' : ' waiting'}`} key={lineKey(line)}>
          <div className="bill-line-head">
            <div>
              <b>{line.reference}</b>
              <p>{line.description}</p>
              <span className="muted small">
                {line.eventDate ? `Event on ${format(parseISO(line.eventDate), 'd MMM yyyy')}` : `Ordered ${format(new Date(line.createdAt), 'd MMM yyyy, h:mm a')}`}
              </span>
            </div>
            <div className="bill-amount">
              <strong>{money(line.total)}</strong>
              {line.serviceCharge > 0 && <span className="muted small">{money(line.subtotal)} + {money(line.serviceCharge)} service</span>}
            </div>
          </div>
          <PaymentState line={line} busy={busyId === lineKey(line)} onCard={() => onCard(line)} onOutlet={() => onOutlet(line)} />
        </article>
      ))}
      <div className="bill-section-total"><span>{totalLabel}</span><strong>{total != null ? money(total) : '—'}</strong></div>
    </section>
  );
}

function PaymentState({ line, busy, onCard, onOutlet }: { line: BillLineDto; busy: boolean; onCard: () => void; onOutlet: () => void }) {
  if (!line.payable && !line.paymentStatus)
    return <p className="pay-chip waiting"><ClockIcon />{line.purpose === 'TABLE_RESERVATION' ? 'This reservation is no longer payable.' : "Waiting for our staff to confirm. You can pay once it's confirmed."}</p>;
  if (line.paymentStatus === 'PAID')
    return <p className="pay-chip paid"><CheckCircleIcon />
      {line.paymentMethod === 'CARD' ? `Paid by card${line.cardLast4 ? ` •••• ${line.cardLast4}` : ''}` : 'Paid at the outlet'}</p>;
  if (line.paymentStatus === 'REFUNDED') return <p className="pay-chip">Refunded</p>;

  if (!line.payable) return <p className="pay-chip waiting">This booking is no longer payable.</p>;
  const outletChosen = line.paymentStatus === 'PENDING' && line.paymentMethod === 'PAY_AT_OUTLET';
  return (
    <div className="pay-actions">
      {outletChosen && <p className="pay-chip outlet"><BuildingStorefrontIcon /> You'll pay {money(line.total)} at the outlet</p>}
      {line.paymentStatus === 'FAILED' && <p className="pay-chip failed">The last payment attempt failed. Please try again.</p>}
      <div className="pay-buttons">
        <button className="button primary" disabled={busy} onClick={onCard}><CreditCardIcon />
          {outletChosen ? 'Pay by card instead' : 'Pay with credit card'}</button>
        {!outletChosen && <button className="button" disabled={busy} onClick={onOutlet}><BuildingStorefrontIcon />
          {busy ? 'Saving…' : 'Pay at the outlet'}</button>}
      </div>
    </div>
  );
}
