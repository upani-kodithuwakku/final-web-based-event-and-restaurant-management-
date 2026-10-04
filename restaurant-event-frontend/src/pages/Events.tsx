import { useState, useEffect } from 'react';
import { images, money, tomorrow } from '../data';
import { useApp } from '../context/AppContext';
import { Badge, Modal, SectionHeading, Empty } from '../components/UI';
import { eventApi, type EventHallDto, type EventPackageDto, type EventBookingDto, errorMessage } from '../services/api';

const FALLBACK_PACKAGES = [
  { name: 'The intimate gathering', type: 'BIRTHDAYS & GET-TOGETHERS', description: 'Your favorite people, a beautiful table, and a menu made for sharing.', min: 10, max: 30, price: 45000, image: images.private },
  { name: 'A day to remember', type: 'WEDDINGS & ENGAGEMENTS', description: 'From the first toast to the last dance, let us make it extraordinary.', min: 30, max: 120, price: 180000, image: images.event },
  { name: 'Beyond the boardroom', type: 'TEAMS & CORPORATE EVENTS', description: 'Fresh perspectives, great food, and space to connect with your team.', min: 10, max: 60, price: 85000, image: images.indoor },
];

export default function Events() {
  const { user } = useApp();
  const [halls, setHalls] = useState<EventHallDto[]>([]);
  const [packages, setPackages] = useState<EventPackageDto[]>([]);
  const [myBookings, setMyBookings] = useState<EventBookingDto[]>([]);
  const [loading, setLoading] = useState(true);
  const [selected, setSelected] = useState<EventPackageDto | null>(null);
  const [selectedHallId, setSelectedHallId] = useState<number | ''>('');
  const [formDate, setFormDate] = useState(tomorrow());
  const [formGuests, setFormGuests] = useState('');
  const [formNote, setFormNote] = useState('');
  const [formStart, setFormStart] = useState('18:00');
  const [formEnd, setFormEnd] = useState('22:00');
  const [busy, setBusy] = useState(false);
  const [err, setErr] = useState('');

  useEffect(() => {
    Promise.all([
      eventApi.halls().catch(() => [] as EventHallDto[]),
      eventApi.packages().catch(() => [] as EventPackageDto[]),
    ]).then(([h, p]) => {
      setHalls(h);
      setPackages(p);
    }).finally(() => setLoading(false));
  }, []);

  useEffect(() => {
    if (!user) return;
    eventApi.myBookings().then(setMyBookings).catch(() => {});
  }, [user]);

  const displayPackages = packages.length > 0 ? packages : FALLBACK_PACKAGES.map((p, i) => ({
    id: i + 1, name: p.name, eventType: p.type, description: p.description,
    basePrice: p.price, minimumGuests: p.min, maximumGuests: p.max, isActive: true,
  } as EventPackageDto));

  const submit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!selected || !selectedHallId) { setErr('Please select a hall for your event.'); return; }
    setBusy(true); setErr('');
    try {
      const booking = await eventApi.book({
        hallId: selectedHallId,
        packageId: selected.id,
        eventDate: formDate,
        startTime: formStart,
        endTime: formEnd,
        guestCount: Number(formGuests) || selected.minimumGuests,
        specialRequirements: formNote,
      });
      setMyBookings(all => [booking, ...all]);
      setSelected(null);
    } catch (e) { setErr(errorMessage(e)); }
    finally { setBusy(false); }
  };

  const cancel = async (id: number) => {
    try {
      const updated = await eventApi.cancel(id);
      setMyBookings(all => all.map(b => b.id === id ? updated : b));
    } catch (e) { setErr(errorMessage(e)); }
  };

  const pkgImage = (name: string) => FALLBACK_PACKAGES.find(p => name.toLowerCase().includes(p.name.toLowerCase().split(' ')[1]))?.image || images.event;

  return (
    <div className="page-container page-enter">
      <section className="event-hero" style={{ backgroundImage: `linear-gradient(90deg,rgba(0,0,0,.6),rgba(0,0,0,.12)),url(${images.event})` }}>
        <span className="hero-kicker">YOUR PEOPLE. YOUR OCCASION.</span>
        <h1>Big moments.<br />Beautiful beginnings.</h1>
        <p>Bring your story. We'll set the scene.</p>
      </section>

      <SectionHeading
        eyebrow="CELEBRATE YOUR WAY"
        title="For whatever brings you together"
        description="Thoughtfully planned spaces and packages for a day that feels like you."
      />

      {err && <p className="error">{err}</p>}

      <div className="menu-grid">
        {displayPackages.filter(p => p.isActive).map(p => (
          <article className="food-card" key={p.id}>
            <img className="package-image" src={pkgImage(p.name)} alt={p.name} />
            <div className="food-info">
              <span className="eyebrow">{p.eventType}</span>
              <h3>{p.name}</h3>
              <p>{p.description}</p>
              <p>{p.minimumGuests}–{p.maximumGuests} guests · From <b>{money(p.basePrice)}</b></p>
              <button className="button full" onClick={() => {
                setErr(''); setSelected(p);
                setFormGuests(String(p.minimumGuests));
                setSelectedHallId(halls[0]?.id || '');
                setFormDate(tomorrow()); setFormStart('18:00'); setFormEnd('22:00'); setFormNote('');
              }}>
                Explore & enquire ↗
              </button>
            </div>
          </article>
        ))}
      </div>

      {user && myBookings.length > 0 && (
        <section className="section-block">
          <SectionHeading title="Your celebrations" />
          {myBookings.map(b => (
            <div className="order-card" key={b.id}>
              <div className="row-between">
                <h3>{b.hallName}</h3>
                <Badge status={b.status} />
              </div>
              <p>{b.packageName} · {b.eventDate} · {b.guestCount} guests · {b.bookingReference}</p>
              {b.status === 'PENDING' && (
                <button className="text-button" onClick={() => cancel(b.id)}>Cancel request</button>
              )}
            </div>
          ))}
        </section>
      )}

      {selected && (
        <Modal title="Let's make it memorable" onClose={() => setSelected(null)}>
          <h3>{selected.name}</h3>
          <p className="muted">From {money(selected.basePrice)} · {selected.minimumGuests}–{selected.maximumGuests} guests</p>
          {!user ? (
            <p className="muted" style={{ marginTop: 16 }}>Please <a href="/auth">sign in</a> to submit an event enquiry.</p>
          ) : (
            <form onSubmit={submit}>
              <div className="input-group">
                {halls.length > 0 && (
                  <label>
                    Select a hall
                    <select value={selectedHallId} onChange={e => setSelectedHallId(Number(e.target.value))} required>
                      <option value="">Choose a hall…</option>
                      {halls.filter(h => h.isActive && h.capacity >= selected.minimumGuests).map(h => (
                        <option key={h.id} value={h.id}>{h.name} (capacity {h.capacity}, {h.location})</option>
                      ))}
                    </select>
                  </label>
                )}
                <div className="form-grid">
                  <label>Event date<input required type="date" value={formDate} min={tomorrow()} onChange={e => setFormDate(e.target.value)} /></label>
                  <label>Guests<input required type="number" min={selected.minimumGuests} max={selected.maximumGuests} value={formGuests} onChange={e => setFormGuests(e.target.value)} /></label>
                </div>
                <div className="form-grid">
                  <label>Start time<input required type="time" value={formStart} onChange={e => setFormStart(e.target.value)} /></label>
                  <label>End time<input required type="time" value={formEnd} onChange={e => setFormEnd(e.target.value)} /></label>
                </div>
                <label>Tell us about your plans<textarea value={formNote} onChange={e => setFormNote(e.target.value)} placeholder="Your ideas, dietary needs, and special touches…" /></label>
                {err && <p role="alert" className="error">{err}</p>}
                <button className="button primary full" disabled={busy}>{busy ? 'Submitting…' : 'Submit enquiry'}</button>
              </div>
            </form>
          )}
        </Modal>
      )}
    </div>
  );
}
