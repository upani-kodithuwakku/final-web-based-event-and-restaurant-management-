import toast from 'react-hot-toast';
import { localToday, reservationError } from '../services/validation';
import { useState } from 'react';
import { Link } from 'react-router-dom';
import { CheckCircleIcon, CalendarDaysIcon, UsersIcon } from '@heroicons/react/24/outline';
import { Modal } from './UI';
import { useApp } from '../context/AppContext';
import { errorMessage, reservationApi } from '../services/api';
import { tableImage, tableTitle } from '../data';
import BookingPayment from './BookingPayment';
import type { Reservation, Table } from '../types';

export default function BookingModal({ table, date, time, guests, existing, onClose, admin = false, onCreated }: { table: Table; date: string; time: string; guests: number; existing?: Reservation; onClose: () => void; admin?: boolean; onCreated?: (r: Reservation) => void }) {
  const { user, saveBooking } = useApp();
  const [paymentDone, setPaymentDone] = useState(false);
  const [checkout, setCheckout] = useState(false);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState('');
  const [result, setResult] = useState<Reservation>();

  if (checkout && result) return <BookingPayment purpose="TABLE_RESERVATION" targetId={result.id} onClose={() => setCheckout(false)} onDone={() => setPaymentDone(true)} />;

  return (
    <Modal title={result ? "You're all set!" : existing ? 'Update your reservation' : 'Make a little time for good times'} onClose={onClose}>
      {result ? (
        <div className="booking-success">
          <CheckCircleIcon />
          <h2>Your table is waiting.</h2>
          <p>{result.reservationDate} at {result.startTime.slice(0, 5)} · {result.guestCount} guests</p>
          <p className="reference">{result.bookingReference}</p>
          {!existing && !admin && !paymentDone && <div className="booking-payment-options"><button className="button primary" onClick={() => setCheckout(true)}>Pay reservation deposit</button><button className="text-button" onClick={onClose}>Skip for now</button></div>}
          <p className="muted">{paymentDone ? 'Your payment choice has been saved.' : 'Your reservation is confirmed.'}</p>{admin ? <button className="button primary" onClick={onClose}>Back to calendar</button> : <Link className="button primary" to="/reservations" onClick={onClose}>View my reservations</Link>}
        </div>
      ) : (
        <>
          <div className="booking-preview">
            <img src={tableImage(table.location, table)} alt={tableTitle(table.location, table)} />
            <div>
              <span className="eyebrow">{table.tableNumber} · {table.location}</span>
              <h3>{tableTitle(table.location, table)}</h3>
              <p><UsersIcon />{table.capacity} guests maximum</p>
            </div>
          </div>
          {!user ? (
            <div className="empty">
              <p>Sign in to reserve your table.</p>
              <Link className="button primary" to="/login">Log in</Link>
            </div>
          ) : (
            <form noValidate onSubmit={async e => {
              e.preventDefault(); setError('');
              const f = new FormData(e.currentTarget);
              const validation = reservationError(String(f.get('date')), String(f.get('time')), Number(f.get('guests')), table.capacity, String(f.get('phone')), String(f.get('name')));
              if (validation) { setError(validation); toast.error(validation); return; }
              setBusy(true);
              try {
                const input = {
                  tableId: table.id,
                  reservationDate: String(f.get('date')),
                  startTime: String(f.get('time')),
                  guestCount: Number(f.get('guests')),
                  contactName: String(f.get('name')),
                  contactPhone: String(f.get('phone')),
                  specialRequest: String(f.get('request')),
                  seatingPreference: table.location,
                };
                const booked = admin ? await reservationApi.adminCreate({ ...input, customerEmail: String(f.get('customerEmail')) }) : await saveBooking(input, existing?.id);
                setResult(booked); onCreated?.(booked);
                if (!existing && !admin) setCheckout(true);
              } catch (err) { const message = errorMessage(err); setError(message); toast.error(message); }
              finally { setBusy(false); }
            }}>
              {admin && <label>Customer account email<input required type="email" name="customerEmail" placeholder="Customer’s registered email" /></label>}
              <div className="form-grid">
                <label>Date<input required type="date" name="date" defaultValue={date} min={localToday()} /></label>
                <label>Time<input required type="time" name="time" defaultValue={time.slice(0, 5)} min="11:00" max="21:00" /></label>
                <label>Guests<input required type="number" name="guests" min="1" max={table.capacity} defaultValue={guests} /></label>
                <label>Contact name<input required name="name" maxLength={100} autoComplete="name" defaultValue={existing?.contactName || (admin ? '' : user?.fullName) || ''} placeholder="Your full name" /></label>
              </div>
              <label>Phone number<input required type="tel" name="phone" autoComplete="tel" inputMode="numeric" pattern="[0-9]{10}" defaultValue={existing?.contactPhone || ''} placeholder="0771234567" /></label>
              <label>Anything we should know?<textarea name="request" maxLength={500} defaultValue={existing?.specialRequest} placeholder="A birthday, dietary needs, or a favorite seat…" /></label>
              <p className="muted small"><CalendarDaysIcon className="inline-icon" /> Your table is reserved for 2 hours. A per-guest reservation deposit is payable after booking.</p>
              {error && <p role="alert" className="error">{error}</p>}
              <button className="button primary full" disabled={busy}>{busy ? 'Saving reservation…' : existing ? 'Save changes' : 'Confirm reservation'}</button>
            </form>
          )}
        </>
      )}
    </Modal>
  );
}
