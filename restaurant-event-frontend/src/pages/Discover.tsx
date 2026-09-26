import { useState } from 'react';
import { Link } from 'react-router-dom';
import { CalendarDaysIcon, ClockIcon, UsersIcon, MagnifyingGlassIcon, AdjustmentsHorizontalIcon, HeartIcon, StarIcon, ArrowRightIcon, MapPinIcon, SunIcon, HomeIcon, SparklesIcon, Squares2X2Icon, BuildingStorefrontIcon, ChevronRightIcon, CheckBadgeIcon } from '@heroicons/react/24/outline';
import { format, parseISO } from 'date-fns';
import { useApp } from '../context/AppContext';
import { images, tableImage, tableTitle, tomorrow } from '../data';
import { errorMessage, reservationApi } from '../services/api';
import { Badge, Empty, Modal, SectionHeading } from '../components/UI';
import BookingModal from '../components/BookingModal';
import type { Table } from '../types';

const categories = [
  { name: 'All spaces', value: '', icon: Squares2X2Icon },
  { name: 'Garden dining', value: 'GARDEN', icon: SunIcon },
  { name: 'By the window', value: 'WINDOW', icon: BuildingStorefrontIcon },
  { name: 'Indoor comfort', value: 'INDOOR', icon: HomeIcon },
  { name: 'Al fresco', value: 'OUTDOOR', icon: SunIcon },
  { name: 'Private dining', value: 'PRIVATE', icon: SparklesIcon },
];

export default function Discover({ savedOnly = false }: { savedOnly?: boolean }) {
  const app = useApp();
  const [date, setDate] = useState(tomorrow());
  const [time, setTime] = useState('19:00');
  const [guests, setGuests] = useState(2);
  const [category, setCategory] = useState('');
  const [selected, setSelected] = useState<Table>();
  const [searched, setSearched] = useState(false);
  const [results, setResults] = useState<Table[]>([]);
  const [alternatives, setAlternatives] = useState<string[]>([]);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState('');
  const [filterOpen, setFilterOpen] = useState(false);
  const [capacity, setCapacity] = useState(0);

  const upcoming = app.reservations.filter(r => ['CONFIRMED', 'PENDING'].includes(r.status) && new Date(`${r.reservationDate}T${r.startTime}`) > new Date())[0];

  const search = async (newTime = time) => {
    setBusy(true); setError(''); setAlternatives([]);
    try {
      if (new Date(`${date}T${newTime}`) <= new Date()) throw new Error('Choose a date and time in the future.');
      const response = await reservationApi.availability(date, newTime, guests);
      setResults(response.availableTables);
      setAlternatives(response.alternativeTimes || []);
      setSearched(true);
    } catch (e) { setError(errorMessage(e)); }
    finally { setBusy(false); }
  };

  const visible = (searched ? results : [])
    .filter(t => t.currentStatus !== 'OUT_OF_SERVICE' && (!category || t.location.toUpperCase() === category) && t.capacity >= capacity && (!savedOnly || app.saved.includes(t.id)))
    .slice(0, searched || savedOnly ? 20 : 6);

  return (
    <div className="discover page-enter">
      {!savedOnly && (
        <>
          <section className="hero" style={{ backgroundImage: `linear-gradient(90deg,rgba(17,29,24,.67),rgba(17,29,24,.06)),url(${images.hero})` }}>
            <div className="hero-content">
              <span className="hero-kicker"><span /> A LITTLE ESCAPE, RIGHT HERE IN COLOMBO</span>
              <h1>Good food.<br />Great company.<br /><em>Your kind of place.</em></h1>
              <p>Find your favorite corner. Share something delicious.<br />Make an ordinary day a little more memorable.</p>
              <a href="#spaces" className="hero-link">Find your table <ArrowRightIcon /></a>
            </div>
            <div className="hero-location"><MapPinIcon /> Park Street, Colombo <span>·</span> Est. 2026</div>
          </section>

          <form className="search-bar" onSubmit={e => { e.preventDefault(); void search(); }}>
            <label><CalendarDaysIcon /><span><b>When</b><input aria-label="Reservation date" required type="date" value={date} min={format(new Date(), 'yyyy-MM-dd')} onChange={e => { setDate(e.target.value); setSearched(false); }} /></span></label>
            <label><ClockIcon /><span><b>What time</b><select aria-label="Reservation time" value={time} onChange={e => { setTime(e.target.value); setSearched(false); }}>{['11:00', '12:00', '13:00', '14:00', '15:00', '17:00', '18:00', '19:00', '20:00', '21:00'].map(t => <option key={t} value={t}>{format(parseISO(`2026-01-01T${t}`), 'h:mm a')}</option>)}</select></span></label>
            <label><UsersIcon /><span><b>Who's coming</b><select aria-label="Guest count" value={guests} onChange={e => { setGuests(Number(e.target.value)); setSearched(false); }}>{Array.from({ length: 12 }, (_, i) => <option key={i} value={i + 1}>{i + 1} {i ? 'guests' : 'guest'}</option>)}</select></span></label>
            <button className="button primary" disabled={busy}><MagnifyingGlassIcon />{busy ? 'Searching…' : 'Find a table'}</button>
          </form>

          <div className="trust-row">
            <span><CheckBadgeIcon /> A seat for every occasion</span>
            <span><CalendarDaysIcon /> Easy, flexible reservations</span>
            <span><HeartIcon /> Made for moments together</span>
          </div>

          {upcoming && (
            <div className="upcoming-banner">
              <div className="date-tile">
                <span>{format(parseISO(upcoming.reservationDate), 'MMM')}</span>
                <b>{format(parseISO(upcoming.reservationDate), 'dd')}</b>
              </div>
              <div>
                <div className="upcoming-title"><h3>Something to look forward to</h3><Badge status={upcoming.status} /></div>
                <p>{tableTitle(upcoming.table.location)} <span>·</span> {format(parseISO(`2000-01-01T${upcoming.startTime}`), 'h:mm a')} <span>·</span> {upcoming.guestCount} guests <span>·</span> {upcoming.table.tableNumber}</p>
              </div>
              <Link to="/reservations">View reservation <ChevronRightIcon /></Link>
            </div>
          )}
        </>
      )}

      <section id="spaces" className="spaces-section">
        <SectionHeading
          eyebrow={savedOnly ? 'YOUR LITTLE COLLECTION' : 'FIND YOUR HAPPY PLACE'}
          title={savedOnly ? 'Spaces you love' : "There's a seat with your name on it"}
          description={savedOnly ? 'Your favorite corners, ready for your next visit.' : 'A sunny spot, a cozy corner, or a table for the whole gang. Make it yours.'}
          action={<span className="location-note"><MapPinIcon /> One place. So many possibilities.</span>}
        />
        <div className="filter-bar">
          <div className="categories">
            {categories.map(c => <button key={c.name} className={category === c.value ? 'active' : ''} onClick={() => setCategory(c.value)}><c.icon />{c.name}</button>)}
          </div>
          <button className={`button filter-button ${capacity ? 'selected' : ''}`} onClick={() => setFilterOpen(true)}><AdjustmentsHorizontalIcon /> Filters{capacity ? ' · 1' : ''}</button>
        </div>
        {error && <p role="alert" className="error">{error}</p>}
        {searched && (
          <div className="results-line">
            <span>{visible.length} spaces for {guests} guests · {format(parseISO(date), 'MMM d')} at {time}</span>
            <button className="text-button" onClick={() => { setSearched(false); setError(''); }}>Clear search</button>
          </div>
        )}
        <div className="space-grid">
          {busy ? Array.from({ length: 6 }, (_, i) => <div className="skeleton" key={i} />) : visible.map((table, i) => (
            <article className="space-card" key={table.id}>
              <div className="space-photo">
                <button className="image-button" aria-label={`Reserve ${tableTitle(table.location)} ${table.tableNumber}`} onClick={() => setSelected(table)}>
                  <img src={tableImage(table.location)} alt={`${tableTitle(table.location)} at Gather`} loading="lazy" />
                </button>
                {i < 2 && <span className="photo-tag">{i === 0 ? 'Guest favorite' : 'A little extra special'}</span>}
                <button className={`heart-button ${app.saved.includes(table.id) ? 'is-saved' : ''}`} aria-label={app.saved.includes(table.id) ? `Unsave ${table.tableNumber}` : `Save ${table.tableNumber}`} aria-pressed={app.saved.includes(table.id)} onClick={() => app.setSaved(all => all.includes(table.id) ? all.filter(id => id !== table.id) : [...all, table.id])}>
                  <HeartIcon />
                </button>
                <div className="photo-dots"><i /><i /><i /></div>
              </div>
              <div className="card-title">
                <h3><button onClick={() => setSelected(table)}>{tableTitle(table.location)}</button></h3>
                <span><StarIcon /> {i % 2 ? '4.98' : '4.95'}</span>
              </div>
              <p>{table.location.charAt(0) + table.location.slice(1).toLowerCase()} · Up to {table.capacity} guests</p>
              <p className="table-meta"><span>{table.tableNumber}</span><span className="availability-dot" />Available for your visit</p>
              <button className="reserve-link" onClick={() => setSelected(table)}>Reserve this table <ArrowRightIcon /></button>
            </article>
          ))}
        </div>
        {!busy && !visible.length && (
          <Empty title={savedOnly ? 'Save a space for later' : searched ? 'A different time, perhaps?' : 'Find a table for your visit'}>
            <p>{savedOnly ? 'Tap the heart on any table to keep it here.' : 'Try another date, fewer guests, or a different seating preference.'}</p>
            {alternatives.map(t => <button className="button alternative" key={t} onClick={() => { setTime(t.slice(0, 5)); void search(t.slice(0, 5)); }}>{t.slice(0, 5)}</button>)}
            {!searched && <button className="button primary" onClick={() => void search()}>Check availability</button>}
          </Empty>
        )}
      </section>

      {!savedOnly && (
        <>
          <section className="celebration-banner">
            <div>
              <span className="eyebrow">LIFE'S BIG AND LITTLE OCCASIONS</span>
              <h2>Some moments deserve<br />a little more Gather.</h2>
              <p>Birthdays, "I do"s, and just-because get-togethers.<br />You bring the people. We'll make it special.</p>
              <Link className="button dark" to="/events">Let's plan something <ArrowRightIcon /></Link>
            </div>
            <img src={images.event} alt="A beautifully set celebration table" loading="lazy" />
            <span className="celebration-sticker">make<br /><em>memories.</em></span>
          </section>
          <section className="gather-values">
            <div><span>01 / FRESH & THOUGHTFUL</span><h3>Good things on every plate.</h3><p>Seasonal ingredients, local flavors, and food<br />that gives you a reason to stay a little longer.</p></div>
            <div><span>02 / ALWAYS WELCOMING</span><h3>Come as you are.</h3><p>A first date or your usual Tuesday. There's<br />always a place for you around our table.</p></div>
            <div><span>03 / EFFORTLESS MOMENTS</span><h3>Less planning. More living.</h3><p>A few taps to your next great meal.<br />We'll take care of the little details.</p></div>
          </section>
        </>
      )}

      {selected && <BookingModal table={selected} date={date} time={time} guests={Math.min(guests, selected.capacity)} onClose={() => setSelected(undefined)} />}
      {filterOpen && (
        <Modal title="Find your kind of space" onClose={() => setFilterOpen(false)}>
          <label>Minimum seating capacity
            <select value={capacity} onChange={e => setCapacity(Number(e.target.value))}>
              <option value="0">Any size</option>
              {[2, 4, 6, 8, 12].map(n => <option key={n} value={n}>{n} guests</option>)}
            </select>
          </label>
          <button className="button primary full" onClick={() => setFilterOpen(false)}>Show spaces</button>
          <button className="text-button" onClick={() => { setCapacity(0); setCategory(''); }}>Reset filters</button>
        </Modal>
      )}
    </div>
  );
}
