import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { CalendarDaysIcon, ShoppingBagIcon, SparklesIcon, DocumentTextIcon, BellIcon, UserCircleIcon } from '@heroicons/react/24/outline';
import { useApp } from '../context/AppContext';
import { Badge, Empty } from '../components/UI';
import { billingApi, eventApi, type InvoiceDto, type EventBookingDto, errorMessage } from '../services/api';
import { money } from '../data';

export default function CustomerDashboard() {
  const { user, reservations, loading, loadError, refresh, notifications } = useApp();
  const [invoices, setInvoices] = useState<InvoiceDto[]>([]);
  const [events, setEvents] = useState<EventBookingDto[]>([]);

  useEffect(() => { void refresh(); }, []);

  useEffect(() => {
    billingApi.myInvoices().then(setInvoices).catch(() => {});
    eventApi.myBookings().then(setEvents).catch(() => {});
  }, []);

  const upcoming = reservations.filter(r => ['CONFIRMED', 'PENDING', 'CHECKED_IN'].includes(r.status));
  const activeEvents = events.filter(e => ['PENDING', 'APPROVED', 'CONFIRMED'].includes(e.status));
  const unpaidInvoices = invoices.filter(i => i.status === 'ISSUED');

  if (!user) {
    return (
      <div className="page-container page-enter" style={{ textAlign: 'center', paddingTop: 80 }}>
        <h2>Please sign in to view your dashboard</h2>
        <Link to="/login" className="button primary" style={{ marginTop: 16 }}>Sign in</Link>
      </div>
    );
  }

  return (
    <div className="page-container page-enter">
      <div style={{ marginBottom: 32 }}>
        <p className="eyebrow">WELCOME BACK</p>
        <h1 style={{ fontSize: 32, fontWeight: 800 }}>{user.fullName?.split(' ')[0] ?? 'Hello'} 👋</h1>
        <p className="muted">Here's everything at a glance.</p>
      </div>

      {loadError && <p className="error">{loadError}</p>}

      {/* Quick actions */}
      <section style={{ marginBottom: 40 }}>
        <h2 style={{ fontSize: 18, fontWeight: 700, marginBottom: 16 }}>Quick actions</h2>
        <div style={{ display: 'flex', gap: 12, flexWrap: 'wrap' }}>
          {[
            { to: '/', label: 'Find a table', icon: CalendarDaysIcon },
            { to: '/menu', label: 'Browse menu', icon: ShoppingBagIcon },
            { to: '/events', label: 'Book an event', icon: SparklesIcon },
            { to: '/reservations', label: 'My reservations', icon: CalendarDaysIcon },
            { to: '/profile', label: 'Edit profile', icon: UserCircleIcon },
          ].map(({ to, label, icon: Icon }) => (
            <Link key={to + label} to={to} className="button" style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
              <Icon style={{ width: 16, height: 16 }} /> {label}
            </Link>
          ))}
        </div>
      </section>

      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(320px, 1fr))', gap: 24 }}>
        {/* Upcoming reservations */}
        <section className="report-card">
          <h2 style={{ fontSize: 16, fontWeight: 700, marginBottom: 14, display: 'flex', alignItems: 'center', gap: 8 }}>
            <CalendarDaysIcon style={{ width: 18, height: 18, color: 'var(--babu)' }} /> Upcoming reservations
          </h2>
          {loading ? <div className="skeleton" style={{ height: 80 }} /> : upcoming.length === 0 ? (
            <Empty title="No upcoming reservations">
              <Link to="/" className="text-button">Reserve a table →</Link>
            </Empty>
          ) : upcoming.slice(0, 3).map(r => (
            <div key={r.id} className="report-row" style={{ marginBottom: 8 }}>
              <div>
                <b>{r.reservationDate}</b> at {r.startTime.slice(0, 5)}
                <p className="small muted">{r.guestCount} guests · Table {r.table?.tableNumber}</p>
              </div>
              <Badge status={r.status} />
            </div>
          ))}
          {upcoming.length > 3 && <Link to="/reservations" className="text-button" style={{ fontSize: 13 }}>View all ({upcoming.length}) →</Link>}
        </section>

        {/* Active event bookings */}
        <section className="report-card">
          <h2 style={{ fontSize: 16, fontWeight: 700, marginBottom: 14, display: 'flex', alignItems: 'center', gap: 8 }}>
            <SparklesIcon style={{ width: 18, height: 18, color: 'var(--babu)' }} /> Event bookings
          </h2>
          {activeEvents.length === 0 ? (
            <Empty title="No active event bookings">
              <Link to="/events" className="text-button">Browse events →</Link>
            </Empty>
          ) : activeEvents.slice(0, 3).map(e => (
            <div key={e.id} className="report-row" style={{ marginBottom: 8 }}>
              <div>
                <b>{e.hallName}</b>
                <p className="small muted">{e.packageName} · {e.eventDate}</p>
              </div>
              <Badge status={e.status} />
            </div>
          ))}
        </section>

        {/* Invoices */}
        <section className="report-card">
          <h2 style={{ fontSize: 16, fontWeight: 700, marginBottom: 14, display: 'flex', alignItems: 'center', gap: 8 }}>
            <DocumentTextIcon style={{ width: 18, height: 18, color: 'var(--babu)' }} /> My invoices
          </h2>
          {invoices.length === 0 ? (
            <p className="muted small">No invoices yet.</p>
          ) : invoices.slice(0, 4).map(inv => (
            <div key={inv.id} className="report-row" style={{ marginBottom: 8 }}>
              <div>
                <b>{inv.invoiceNumber}</b>
                <p className="small muted">{money(inv.totalAmount)}</p>
              </div>
              <Badge status={inv.status} />
            </div>
          ))}
          {unpaidInvoices.length > 0 && (
            <p className="small" style={{ color: 'var(--arches)', fontWeight: 600, marginTop: 8 }}>
              {unpaidInvoices.length} unpaid invoice{unpaidInvoices.length > 1 ? 's' : ''} — contact the cashier to pay.
            </p>
          )}
        </section>

        {/* Notifications */}
        <section className="report-card">
          <h2 style={{ fontSize: 16, fontWeight: 700, marginBottom: 14, display: 'flex', alignItems: 'center', gap: 8 }}>
            <BellIcon style={{ width: 18, height: 18, color: 'var(--babu)' }} /> Notifications
          </h2>
          {notifications.length === 0 ? (
            <p className="muted small">No notifications.</p>
          ) : notifications.slice(0, 5).map((n, i) => (
            <p key={i} className="small" style={{ marginBottom: 8, paddingBottom: 8, borderBottom: i < 4 ? '1px solid var(--gray-100)' : 'none' }}>{n}</p>
          ))}
        </section>
      </div>
    </div>
  );
}
