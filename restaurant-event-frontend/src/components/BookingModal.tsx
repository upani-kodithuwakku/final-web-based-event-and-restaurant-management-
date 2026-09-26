import { useState } from 'react';
import { Link } from 'react-router-dom';
import { CheckCircleIcon, CalendarDaysIcon, UsersIcon } from '@heroicons/react/24/outline';
import { Modal } from './UI';
import { useApp } from '../context/AppContext';
import { errorMessage } from '../services/api';
import { tableImage, tableTitle } from '../data';
import type { Reservation, Table } from '../types';

export default function BookingModal({ table, date, time, guests, existing, onClose }: { table: Table; date: string; time: string; guests: number; existing?: Reservation; onClose: () => void }) {
  const { user, saveBooking } = useApp();
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState('');
  const [result, setResult] = useState<Reservation>();

  return (
    <Modal title={result ? "You're all set!" : existing ? 'Update your reservation' : 'Make a little time for good times'} onClose={onClose}>
      {result ? (
        <div className="booking-success">
          <CheckCircleIcon />
          <h2>Your table is waiting.</h2>
          <p>{result.reservationDate} at {result.startTime.slice(0, 5)} · {result.guestCount} guests</p>
          <p className="reference">{result.bookingReference}</p>
          <Link className="button primary" to="/reservations" onClick={onClose}>View my reservations</Link>
        </div>
      ) : (
        <>
          <div className="booking-preview">
            <img src={tableImage(table.location)} alt={tableTitle(table.location)} />
            <div>
              <span className="eyebrow">{table.tableNumber} · {table.location}</span>
              <h3>{tableTitle(table.location)}</h3>
              <p><UsersIcon />{table.capacity} guests maximum</p>
            </div>
          </div>
          {!user ? (
            <div className="empty">
              <p>Sign in to reserve your table.</p>
              <Link className="button primary" to="/login">Log in</Link>
            </div>
          ) : (
            <form onSubmit={async e => {
              e.preventDefault(); setBusy(true); setError('');
              const f = new FormData(e.currentTarget);
              try {
                setResult(await saveBooking({
                  tableId: table.id,
                  reservationDate: String(f.get('date')),
                  startTime: String(f.get('time')),
                  guestCount: Number(f.get('guests')),
                  contactName: String(f.get('name')),
                  contactPhone: String(f.get('phone')),
                  specialRequest: String(f.get('request')),
                  seatingPreference: table.location,
                }, existing?.id));
              } catch (err) { setError(errorMessage(err)); }
              finally { setBusy(false); }
            }}>
              <div className="form-grid">
                <label>Date<input required type="date" name="date" defaultValue={date} min={new Date().toLocaleDateString('en-CA')} /></label>
                <label>Time<input required type="time" name="time" defaultValue={time.slice(0, 5)} min="11:00" max="21:00" /></label>
                <label>Guests<input required type="number" name="guests" min="1" max={table.capacity} defaultValue={guests} /></label>
                <label>Contact name<input required name="name" autoComplete="name" defaultValue={existing?.contactName || user?.fullName || ''} placeholder="Your full name" /></label>
              </div>
              <label>Phone number<input required type="tel" name="phone" autoComplete="tel" pattern="[+0-9 ()-]{7,20}" defaultValue={existing?.contactPhone || ''} placeholder="+94 77 123 4567" /></label>
              <label>Anything we should know?<textarea name="request" maxLength={500} defaultValue={existing?.specialRequest} placeholder="A birthday, dietary needs, or a favorite seat…" /></label>
              <p className="muted small"><CalendarDaysIcon className="inline-icon" /> Your table is reserved for 2 hours. No booking fee.</p>
              {error && <p role="alert" className="error">{error}</p>}
              <button className="button primary full" disabled={busy}>{busy ? 'Saving reservation…' : existing ? 'Save changes' : 'Confirm reservation'}</button>
            </form>
          )}
        </>
      )}
    </Modal>
  );
}
