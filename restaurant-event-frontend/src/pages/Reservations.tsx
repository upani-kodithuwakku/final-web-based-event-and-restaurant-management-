import { useState } from 'react';
import { Link } from 'react-router-dom';
import { CalendarDaysIcon, ClockIcon, UsersIcon } from '@heroicons/react/24/outline';
import { useApp } from '../context/AppContext';
import { Badge, Empty, Modal, SectionHeading } from '../components/UI';
import BookingPayment, { BookingPaymentBadge, useBookingPayments } from '../components/BookingPayment';
import BookingModal from '../components/BookingModal';
import { tableImage, tableTitle, images } from '../data';
import { errorMessage } from '../services/api';
import type { Reservation } from '../types';

export default function Reservations() {
  const app = useApp();
  const [payFor, setPayFor] = useState<number>();
  const [paymentRefresh, setPaymentRefresh] = useState(0);
  const { summary, error: paymentError } = useBookingPayments(!!app.user?.roles.includes('CUSTOMER'), `${paymentRefresh}-${app.reservations.map(r => `${r.id}:${r.status}`).join(',')}`);
  const [tab, setTab] = useState('Upcoming');
  const [edit, setEdit] = useState<Reservation>();
  const [cancel, setCancel] = useState<Reservation>();
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState('');

  const rows = app.reservations.filter(r =>
    tab === 'Upcoming'
      ? ['PENDING', 'CONFIRMED', 'CHECKED_IN'].includes(r.status) && new Date(`${r.reservationDate}T${r.startTime}`) > new Date()
      : !['PENDING', 'CONFIRMED', 'CHECKED_IN'].includes(r.status) || new Date(`${r.reservationDate}T${r.startTime}`) <= new Date()
  );

  if (!app.user) {
    return (
      <div className="page-container page-enter reservations-page">
        <SectionHeading eyebrow="GOOD TIMES AHEAD" title="My reservations" description="A little less planning. A little more looking forward." action={<Link to="/" className="button primary">Find a table</Link>} />
        <Empty title="Your tables, all in one place"><Link className="button primary" to="/login">Sign in to view reservations</Link></Empty>
      </div>
    );
  }

  return (
    <div className="page-container page-enter reservations-page">
      <SectionHeading eyebrow="GOOD TIMES AHEAD" title="My reservations" description="A little less planning. A little more looking forward." action={<Link to="/" className="button primary">Find a table</Link>} />
      <section className="reservation-welcome" style={{ backgroundImage: `linear-gradient(90deg,rgba(25,35,32,.88),rgba(25,35,32,.15)),url(${images.window})` }}>
        <span className="eyebrow">A SEAT WITH YOUR NAME ON IT</span><h2>Good company.<br />A table to remember.</h2>
        <p>Plan your visit, personalise your table, and leave the rest to us.</p>
      </section>
      {paymentError && <p className="error" role="alert">Payment status unavailable: {paymentError}</p>}
      <div className="tabs">
        {['Upcoming', 'Past & cancelled'].map(t => <button key={t} className={t === tab ? 'active' : ''} onClick={() => setTab(t)}>{t}</button>)}
      </div>
      {app.loading ? (
        <div className="skeleton" />
      ) : app.loadError ? (
        <div className="error" role="alert">{app.loadError}<button className="text-button" onClick={app.refresh}>Retry</button></div>
      ) : rows.length ? (
        <div className="reservation-list">
          {rows.map(r => (
            <article className="reservation-card" key={r.id}>
              <img src={tableImage(r.table.location, r.table)} alt={tableTitle(r.table.location, r.table)} />
              <div>
                <div className="row-between"><span className="eyebrow">{r.bookingReference}</span><Badge status={r.status} /></div>
                <h2>{tableTitle(r.table.location, r.table)}</h2>
                <p>{r.table.tableNumber} · {r.table.location.toLowerCase()} seating</p>
                <div className="reservation-details">
                  <span><CalendarDaysIcon />{r.reservationDate}</span>
                  <span><ClockIcon />{r.startTime.slice(0, 5)}</span>
                  <span><UsersIcon />{r.guestCount} guests</span>
                </div>
                {summary && <BookingPaymentBadge payment={summary.tableReservations.find(l => l.targetId === r.id)} deposit />}
                {summary?.tableReservations.some(l => l.targetId === r.id && l.payable && l.paymentStatus !== 'PAID' && l.paymentStatus !== 'REFUNDED') && <button className="button primary" onClick={() => setPayFor(r.id)}>Pay deposit</button>}
                {r.specialRequest && <p className="request-note">"{r.specialRequest}"</p>}
                {['PENDING', 'CONFIRMED'].includes(r.status) && new Date(`${r.reservationDate}T${r.startTime}`) > new Date() && (
                  <div className="button-row">
                    <button className="button" onClick={() => setEdit(r)}>Modify reservation</button>
                    <button className="text-button" onClick={() => { setError(''); setCancel(r); }}>Cancel reservation</button>
                  </div>
                )}
              </div>
            </article>
          ))}
        </div>
      ) : (
        <Empty title={tab === 'Upcoming' ? 'Your next good moment is waiting' : 'No past reservations yet'}>
          <p>Find a space you love and we'll save you a seat.</p>
          <Link className="button primary" to="/">Explore our tables</Link>
        </Empty>
      )}
      {payFor && <BookingPayment purpose="TABLE_RESERVATION" targetId={payFor} onClose={() => setPayFor(undefined)} onDone={() => setPaymentRefresh(v => v + 1)} />}
      {edit && <BookingModal table={edit.table} date={edit.reservationDate} time={edit.startTime} guests={edit.guestCount} existing={edit} onClose={() => setEdit(undefined)} />}
      {cancel && (
        <Modal title="Cancel your reservation?" onClose={() => setCancel(undefined)}>
          <p>We'll release {cancel.table.tableNumber} on {cancel.reservationDate}. You can always reserve another time.</p>
          <form onSubmit={async e => {
            e.preventDefault(); setBusy(true);
            try { await app.cancelBooking(cancel.id, String(new FormData(e.currentTarget).get('reason'))); setCancel(undefined); }
            catch (err) { setError(errorMessage(err)); }
            finally { setBusy(false); }
          }}>
            <label>Reason (optional)<textarea name="reason" placeholder="Let us know what changed" /></label>
            {error && <p className="error" role="alert">{error}</p>}
            <button className="button primary full" disabled={busy}>{busy ? 'Cancelling…' : 'Yes, cancel reservation'}</button>
          </form>
        </Modal>
      )}
    </div>
  );
}
