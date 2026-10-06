import { useCallback, useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import {
  ArrowPathIcon, ArrowRightIcon, CalendarDaysIcon, CheckCircleIcon,
  CreditCardIcon, HeartIcon, SparklesIcon, UsersIcon,
  StarIcon,
} from '@heroicons/react/24/outline';
import BookingPayment, { BookingPaymentBadge, useBookingPayments } from '../components/BookingPayment';
import { images, money, tomorrow } from '../data';
import { useApp } from '../context/AppContext';
import { Badge, Modal, SectionHeading, Empty } from '../components/UI';
import { eventApi, type EventHallDto, type EventPackageDto, type EventBookingDto, errorMessage } from '../services/api';

const occasion = (type: string) =>
  type.toLowerCase().replaceAll('_', ' ').replace(/\b\w/g, c => c.toUpperCase());

const occasionPhoto = (name: string) => `/images/occasions/${name}.jpg`;
const birthdayPhotos: Record<string, string> = {
  'Birthday Table for Your Favourite People': 'birthday-cake',
  'Birthday Garden Party': 'birthday-balloons',
  'Classic Birthday Buffet': 'birthday-buffet',
  'Evening Birthday Reception': 'birthday-party',
  'Family Birthday Dinner': 'birthday-table',
  'Garden Birthday Celebration': 'birthday-outdoors',
  'Grand Birthday Banquet': 'birthday-banquet',
  'Terrace Birthday Party': 'birthday-cupcakes',
};
const packageImage = (pkg: EventPackageDto) => occasionPhoto(
  (pkg.eventType === 'BIRTHDAY' ? birthdayPhotos[pkg.name] : undefined) || ({
    BIRTHDAY: 'birthday-cake', ANNIVERSARY: 'anniversary', ENGAGEMENT: 'engagement',
    WEDDING: 'wedding', BABY_SHOWER: 'baby-shower', GRADUATION: 'graduation',
    FAMILY: 'family-feast', CORPORATE: 'corporate', RECEPTION: 'reception',
    FAREWELL: 'farewell', CHRISTMAS: 'christmas', NEW_YEAR: 'new-year',
    RETIREMENT: 'retirement', NAMING: 'naming', TEAM_BUILDING: 'team-building', GALA: 'gala',
  }[pkg.eventType] ?? 'birthday-garden')
);

const occasionEmoji = (type: string): string => ({
  BIRTHDAY: '🎂', ANNIVERSARY: '🥂', ENGAGEMENT: '💍', WEDDING: '💐',
  BABY_SHOWER: '🍼', GRADUATION: '🎓', FAMILY: '🏡', CORPORATE: '💼',
  RECEPTION: '🍾', FAREWELL: '✈️', CHRISTMAS: '🎄', NEW_YEAR: '🎆',
  RETIREMENT: '🏅', NAMING: '👶', TEAM_BUILDING: '🤝', GALA: '✨',
}[type] ?? '🎉');

const FEATURED = new Set([
  'A Beautiful Beginning', 'Grand Gala Dinner',
  "New Year's Eve Countdown", 'Christmas Gala Night', 'The Engagement Edit',
]);


export default function Events() {
  const { user } = useApp();
  const customer = !!user?.roles.includes('CUSTOMER');
  const [payFor, setPayFor] = useState<number>();
  const [paymentRefresh, setPaymentRefresh] = useState(0);
  const [halls, setHalls] = useState<EventHallDto[]>([]);
  const [packages, setPackages] = useState<EventPackageDto[]>([]);
  const [myBookings, setMyBookings] = useState<EventBookingDto[]>([]);
  const { summary, error: paymentError } = useBookingPayments(customer, `${paymentRefresh}-${myBookings.map(b => `${b.id}:${b.status}`).join(',')}`);
  const [loading, setLoading] = useState(true);
  const [catalogError, setCatalogError] = useState('');
  const [bookingError, setBookingError] = useState('');
  const [category, setCategory] = useState('');
  const [search, setSearch] = useState('');
  const [guestFilter, setGuestFilter] = useState('');
  const [sort, setSort] = useState('featured');
  const [selected, setSelected] = useState<EventPackageDto | null>(null);
  const [selectedHallId, setSelectedHallId] = useState<number | ''>('');
  const [formDate, setFormDate] = useState(tomorrow());
  const [formGuests, setFormGuests] = useState('');
  const [formNote, setFormNote] = useState('');
  const [formStart, setFormStart] = useState('18:00');
  const [formEnd, setFormEnd] = useState('22:00');
  const [busy, setBusy] = useState(false);
  const [err, setErr] = useState('');
  const [notice, setNotice] = useState('');

  const loadCatalog = useCallback(async () => {
    setLoading(true); setCatalogError('');
    try {
      const [h, p] = await Promise.all([eventApi.halls(), eventApi.packages()]);
      setHalls(h.filter(hall => hall.isActive));
      setPackages(p.filter(pkg => pkg.isActive));
    } catch (e) { setCatalogError(errorMessage(e)); }
    finally { setLoading(false); }
  }, []);

  useEffect(() => { void loadCatalog(); }, [loadCatalog]);

  useEffect(() => {
    if (!customer) { setMyBookings([]); return; }
    let active = true;
    const load = async () => {
      try { const rows = await eventApi.myBookings(); if (active) { setMyBookings(rows); setBookingError(''); } }
      catch (e) { if (active) setBookingError(errorMessage(e)); }
    };
    void load();
    const timer = window.setInterval(() => void load(), 20000);
    return () => { active = false; window.clearInterval(timer); };
  }, [customer, user?.userId, paymentRefresh]);

  const choose = (pkg: EventPackageDto) => {
    setErr(''); setSelected(pkg); setFormGuests(String(pkg.minimumGuests));
    setSelectedHallId(halls.find(h => h.capacity >= pkg.minimumGuests)?.id || '');
    setFormDate(tomorrow()); setFormStart('18:00'); setFormEnd('22:00'); setFormNote('');
  };

  const submit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!selected || !selectedHallId) { setErr('Please choose a hall that fits your guests.'); return; }
    if (formEnd <= formStart) { setErr('End time must be after start time.'); return; }
    setBusy(true); setErr('');
    try {
      const booking = await eventApi.book({
        hallId: selectedHallId, packageId: selected.id, eventDate: formDate,
        startTime: formStart, endTime: formEnd, guestCount: Number(formGuests),
        specialRequirements: formNote,
      });
      setMyBookings(all => [booking, ...all]); setSelected(null); setPaymentRefresh(n => n + 1);
      setNotice(`Enquiry ${booking.bookingReference} received. Your coordinator will confirm the details — you can then pay by card or at the outlet.`);
    } catch (e) { setErr(errorMessage(e)); }
    finally { setBusy(false); }
  };

  const cancel = async (id: number) => {
    setBusy(true); setBookingError('');
    try { const u = await eventApi.cancel(id); setMyBookings(all => all.map(b => b.id === id ? u : b)); setPaymentRefresh(n => n + 1); }
    catch (e) { setBookingError(errorMessage(e)); }
    finally { setBusy(false); }
  };

  const categories = [...new Set(packages.map(p => p.eventType))];
  const visible = packages.filter(p =>
    (!category || p.eventType === category)
    && `${p.name} ${p.description} ${occasion(p.eventType)}`.toLowerCase().includes(search.trim().toLowerCase())
    && (!guestFilter || (Number(guestFilter) >= p.minimumGuests && Number(guestFilter) <= p.maximumGuests))
  ).sort((a, b) => sort === 'price-low' ? a.basePrice - b.basePrice
    : sort === 'price-high' ? b.basePrice - a.basePrice
    : Number(FEATURED.has(b.name)) - Number(FEATURED.has(a.name)));
  const clearFilters = () => { setCategory(''); setSearch(''); setGuestFilter(''); setSort('featured'); };
  const fittingHalls = halls.filter(h => h.capacity >= Number(formGuests || selected?.minimumGuests || 1));

  return (
    <div className="page-container page-enter events-page">

      {/* ── Hero ──────────────────────────────────────────── */}
      <section className="event-hero celebrations-hero" style={{ backgroundImage: `url(${images.event})` }}>
        <span className="hero-kicker">YOUR PEOPLE. YOUR OCCASION.</span>
        <h1>Every reason<br />to celebrate.</h1>
        <p>From a birthday toast to your biggest yes.<br />Find a setting that feels like you.</p>
        <a className="button primary" href="#celebration-packages">
          Find your celebration <ArrowRightIcon />
        </a>
        {customer && <a className="celebrations-bookings-link" href="#my-celebrations">My bookings &amp; payments</a>}
      </section>

      {/* ── How it works ──────────────────────────────────── */}
      <div className="celebration-how" aria-label="How to book your celebration">
        <div><SparklesIcon /><span><b>Choose your occasion</b><small>Spaces and menus made for sharing</small></span></div>
        <div><CalendarDaysIcon /><span><b>Make it yours</b><small>Your coordinator confirms the details</small></span></div>
        <div><CreditCardIcon /><span><b>Pay your way</b><small>Card now or pay at the outlet after confirmation</small></span></div>
      </div>

      {notice && (
        <p className="celebration-notice" role="status">
          <CheckCircleIcon />{notice}
        </p>
      )}

      {/* ── Package catalogue ─────────────────────────────── */}
      <section id="celebration-packages" className="celebration-catalog">
        <SectionHeading
          eyebrow="CELEBRATE YOUR WAY"
          title="A little occasion. A lasting memory."
          description={`Explore birthdays, weddings, galas and everything worth getting together for. ${visible.length} ${visible.length === 1 ? 'occasion' : 'occasions'} available.`}
        />

        <div className="celebration-filters">
          <label>Find your celebration
            <input type="search" placeholder="Try birthday, wedding or team dinner" value={search} onChange={e => setSearch(e.target.value)} />
          </label>
          <label>Number of guests
            <input type="number" min="1" step="1" placeholder="Any group size" value={guestFilter} onChange={e => setGuestFilter(e.target.value)} />
          </label>
          <label>Sort packages
            <select value={sort} onChange={e => setSort(e.target.value)}>
              <option value="featured">Featured first</option>
              <option value="price-low">Price: low to high</option>
              <option value="price-high">Price: high to low</option>
            </select>
          </label>
        </div>

        {/* Category filter chips */}
        <div className="celebration-categories" role="group" aria-label="Filter by occasion">
          <button aria-pressed={!category} onClick={() => setCategory('')}>
            🎉 All occasions
          </button>
          {categories.map(type => (
            <button key={type} aria-pressed={category === type} onClick={() => setCategory(type)}>
              {occasionEmoji(type)} {occasion(type)}
            </button>
          ))}
        </div>

        {catalogError && (
          <div className="error" role="alert">
            {catalogError}{' '}
            <button className="text-button" onClick={() => void loadCatalog()}>Retry</button>
          </div>
        )}

        {loading ? (
          <div className="celebration-package-grid" aria-label="Loading">
            {[1, 2, 3, 4, 5, 6].map(n => <div className="skeleton" key={n} />)}
          </div>
        ) : !visible.length ? (
          <Empty title={packages.length ? 'No celebrations match your plans yet' : 'No packages available right now'}>
            <p>{packages.length ? 'Try another occasion or a different group size.' : 'Please try again shortly.'}</p>
            {packages.length > 0 && <button className="button" onClick={clearFilters}>Clear filters</button>}
          </Empty>
        ) : (
          <div className="celebration-package-grid">
            {visible.map(pkg => {
              const featured = FEATURED.has(pkg.name);
              return (
                <article className={`celebration-package${featured ? ' pkg-featured' : ''}`} key={pkg.id}>
                  {featured && <span className="pkg-ribbon"><StarIcon /> Featured</span>}
                  <div className="celebration-package-photo">
                    <img src={packageImage(pkg)} alt={`${occasion(pkg.eventType)} celebration setting`} loading="lazy" />
                    <span className="pkg-type-badge">
                      {occasionEmoji(pkg.eventType)} {occasion(pkg.eventType)}
                    </span>
                  </div>
                  <div className="celebration-package-copy">
                    <h3>{pkg.name}</h3>
                    <p>{pkg.description}</p>
                    <span className="celebration-guests">
                      <UsersIcon />{pkg.minimumGuests}–{pkg.maximumGuests} guests
                    </span>
                    <footer>
                      <div>
                        <small>Package price from</small>
                        <strong>{money(pkg.basePrice)}</strong>
                      </div>
                      <button className="button" onClick={() => choose(pkg)}>
                        Explore package <ArrowRightIcon />
                      </button>
                    </footer>
                  </div>
                </article>
              );
            })}
          </div>
        )}
      </section>

      {/* ── Personal touch banner ─────────────────────────── */}
      <section className="celebration-personal">
        <HeartIcon />
        <div>
          <h2>The little details make it yours.</h2>
          <p>A favourite menu, a family tradition, a surprise worth keeping. Tell your coordinator what matters to you when you enquire.</p>
        </div>
      </section>

      {/* ── My bookings ───────────────────────────────────── */}
      {customer && (
        <section id="my-celebrations" className="celebration-bookings">
          <SectionHeading
            eyebrow="YOUR NEXT GOOD MOMENT"
            title="My bookings &amp; payments"
            description="Confirmed celebrations can be paid by card or at the outlet."
            action={<button className="button" onClick={() => setPaymentRefresh(n => n + 1)}><ArrowPathIcon /> Refresh</button>}
          />
          {bookingError && <p className="error" role="alert">{bookingError}</p>}
          {paymentError && <p className="error" role="alert">Payment status unavailable: {paymentError}</p>}
          {!myBookings.length && !bookingError && (
            <Empty title="Your next celebration starts here">
              <p>Choose a package above to send your enquiry.</p>
            </Empty>
          )}
          {myBookings.map(booking => {
            const bill = summary?.eventBookings.find(l => l.targetId === booking.id);
            const canPay = booking.status === 'CONFIRMED' && bill?.payable && bill.paymentStatus !== 'PAID' && bill.paymentStatus !== 'REFUNDED';
            return (
              <article className="celebration-booking" key={booking.id}>
                <div>
                  <span className="eyebrow">{booking.bookingReference}</span>
                  <h3>{booking.packageName}</h3>
                  <p>{booking.hallName} · {booking.eventDate} · {booking.startTime?.slice(0, 5)} · {booking.guestCount} guests</p>
                  <Badge status={booking.status} />
                  {booking.status === 'REJECTED' && booking.rejectionReason && <p>{booking.rejectionReason}</p>}
                </div>
                <div className="celebration-booking-payment">
                  {booking.status === 'PENDING'
                    ? <>
                        <p className="muted small">Awaiting coordinator confirmation. Payment opens once confirmed.</p>
                        <button className="text-button" disabled={busy} onClick={() => void cancel(booking.id)}>Cancel enquiry</button>
                      </>
                    : <>
                        {bill && <><strong>{money(bill.total)}</strong><BookingPaymentBadge payment={bill} /></>}
                        {canPay && (
                          <button className="button primary" onClick={() => setPayFor(booking.id)}>
                            <CreditCardIcon /> Pay now
                          </button>
                        )}
                        {bill && (
                          <Link className="text-button" to={`/payments?event=${booking.id}`}>
                            View event bill <ArrowRightIcon className="inline-icon" />
                          </Link>
                        )}
                      </>
                  }
                </div>
              </article>
            );
          })}
        </section>
      )}

      {payFor && (
        <BookingPayment
          purpose="EVENT_BOOKING"
          targetId={payFor}
          onClose={() => setPayFor(undefined)}
          onDone={() => setPaymentRefresh(n => n + 1)}
        />
      )}

      {selected && (
        <Modal title="Let's make it memorable" onClose={() => { if (!busy) setSelected(null); }}>
          <h3>{selected.name}</h3>
          <img className="celebration-enquiry-photo" src={packageImage(selected)} alt={`${occasion(selected.eventType)} setting`} />
          <p className="muted">{selected.description}</p>
          <p className="muted">{money(selected.basePrice)} · {selected.minimumGuests}–{selected.maximumGuests} guests</p>
          <dl className="celebration-price-review">
            <div><dt>Package total</dt><dd>{money(selected.basePrice)}</dd></div>
            <div><dt>Due today</dt><dd>{money(0)}</dd></div>
          </dl>
          <p className="celebration-enquiry-note">
            Send an enquiry first. Once your coordinator confirms, choose card payment or pay at the outlet from My bookings &amp; payments.
          </p>
          {!customer
            ? <p>Please <Link className="text-button" to="/login">sign in with a customer account</Link> to submit your enquiry.</p>
            : <form onSubmit={submit}>
                <div className="form-grid">
                  <label>Event date
                    <input required type="date" value={formDate} min={tomorrow()} onChange={e => setFormDate(e.target.value)} />
                  </label>
                  <label>Guests
                    <input required type="number" min={selected.minimumGuests} max={selected.maximumGuests} value={formGuests}
                      onChange={e => {
                        setFormGuests(e.target.value);
                        if (halls.find(h => h.id === selectedHallId && h.capacity < Number(e.target.value))) setSelectedHallId('');
                      }} />
                  </label>
                </div>
                <label>Select a hall
                  <select required value={selectedHallId} onChange={e => setSelectedHallId(e.target.value ? Number(e.target.value) : '')}>
                    <option value="">Choose a hall…</option>
                    {fittingHalls.map(h => (
                      <option key={h.id} value={h.id}>{h.name} · up to {h.capacity} guests · {h.location}</option>
                    ))}
                  </select>
                </label>
                {!fittingHalls.length && <p className="error">No hall fits this guest count. Please choose fewer guests or another package.</p>}
                <div className="form-grid">
                  <label>Start time<input required type="time" value={formStart} onChange={e => setFormStart(e.target.value)} /></label>
                  <label>End time<input required type="time" value={formEnd} onChange={e => setFormEnd(e.target.value)} /></label>
                </div>
                <label>Tell us about your plans
                  <textarea maxLength={1000} value={formNote} onChange={e => setFormNote(e.target.value)}
                    placeholder="Your ideas, dietary needs, and special touches…" />
                </label>
                {err && <p role="alert" className="error">{err}</p>}
                <button className="button primary full" disabled={busy || !selectedHallId}>
                  {busy ? 'Submitting…' : 'Send celebration enquiry'}
                </button>
              </form>
          }
        </Modal>
      )}
    </div>
  );
}
