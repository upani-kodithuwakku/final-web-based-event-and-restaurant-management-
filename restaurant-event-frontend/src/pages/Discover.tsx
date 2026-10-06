import { useEffect, useRef, useState } from 'react';
import { Link } from 'react-router-dom';
import { CalendarDaysIcon, ClockIcon, UsersIcon, MagnifyingGlassIcon, AdjustmentsHorizontalIcon, HeartIcon, StarIcon, ArrowRightIcon, MapPinIcon, SunIcon, HomeIcon, SparklesIcon, Squares2X2Icon, BuildingStorefrontIcon, ChevronRightIcon, CheckBadgeIcon, CheckIcon, ChatBubbleLeftRightIcon } from '@heroicons/react/24/outline';
import { format, parseISO } from 'date-fns';
import { useApp } from '../context/AppContext';
import { images, menu, money, tableImage, tableTitle, tomorrow } from '../data';
import { errorMessage, reservationApi, menuApi } from '../services/api';
import { Badge, Empty, Modal, SectionHeading } from '../components/UI';
import BookingModal from '../components/BookingModal';
import HeroCarousel from '../components/HeroCarousel';
import MenuPhoto from '../components/MenuPhoto';
import type { Table } from '../types';
import { filterSavedSpaces } from '../services/savedSpaces';

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
  const momentsRef = useRef<HTMLDivElement>(null);
  const [popular, setPopular] = useState(menu.slice(0, 4));
  useEffect(() => {
    if (savedOnly) return;
    let active = true;
    menuApi.items().then(items => {
      const available = items.filter(item => item.isAvailable && item.isActive !== false).slice(0, 4);
      if (active && available.length) setPopular(available.map(item => ({
        id: item.id, name: item.name, description: item.description || 'Freshly made. Best shared.',
        price: item.price, image: item.imageUrl, category: '', tag: '',
      })));
    }).catch(() => { /* The existing menu collection remains available offline. */ });
    return () => { active = false; };
  }, [savedOnly]);
  useEffect(() => {
    const root = momentsRef.current;
    if (!root) return;
    const preference = window.matchMedia('(prefers-reduced-motion: reduce)');
    let observer: IntersectionObserver | undefined;
    const reset = () => {
      observer?.disconnect();
      root.querySelectorAll('.gather-reveal').forEach(node => node.classList.remove('reveal-pending'));
      if (preference.matches || !('IntersectionObserver' in window)) return;
      observer = new IntersectionObserver(entries => entries.forEach(entry => {
        if (entry.isIntersecting) { entry.target.classList.remove('reveal-pending'); observer?.unobserve(entry.target); }
      }), { threshold: 0.08 });
      root.querySelectorAll('.gather-reveal').forEach(node => { node.classList.add('reveal-pending'); observer?.observe(node); });
    };
    reset(); preference.addEventListener('change', reset);
    return () => { observer?.disconnect(); preference.removeEventListener('change', reset); };
  }, [savedOnly]);
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
      document.getElementById('spaces')?.scrollIntoView({behavior: 'smooth', block: 'start'});
    } catch (e) { setError(errorMessage(e)); }
    finally { setBusy(false); }
  };

  const savedCount = app.tables.filter(table => app.saved.includes(table.id)).length;
  const clearSavedFilters = () => { setCategory(''); setGuests(1); };
  const visible = savedOnly
    ? filterSavedSpaces(app.tables, app.saved, { category, guests })
    : (searched ? results : app.tables)
      .filter(t => t.currentStatus !== 'OUT_OF_SERVICE' && (!category || t.location.toUpperCase() === category) && t.capacity >= capacity)
      ;

  return (
    <div className={`discover page-enter ${savedOnly ? 'saved-spaces-page' : ''}`}>
      {!savedOnly && (
        <>
          <HeroCarousel />

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
                <p>{tableTitle(upcoming.table.location, upcoming.table)} <span>·</span> {format(parseISO(`2000-01-01T${upcoming.startTime}`), 'h:mm a')} <span>·</span> {upcoming.guestCount} guests <span>·</span> {upcoming.table.tableNumber}</p>
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
        {savedOnly && <div className="saved-summary-strip"><HeartIcon aria-hidden="true" /><div><strong>{savedCount} saved {savedCount === 1 ? 'space' : 'spaces'}</strong><p>A collection of corners for your next gathering.</p></div></div>}
        <div className="filter-bar">
          <div className="categories">
            {categories.map(c => <button key={c.name} className={category === c.value ? 'active' : ''} onClick={() => { setCategory(c.value); if (!savedOnly) void search(); }}><c.icon />{c.name}</button>)}
          </div>
          {savedOnly ? <div className="saved-guest-stepper" role="group" aria-label="Filter by guest count"><button aria-label="Fewer guests" disabled={guests <= 1} onClick={() => setGuests(n => n - 1)}>−</button><span aria-live="polite">{guests} {guests === 1 ? 'guest' : 'guests'}</span><button aria-label="More guests" disabled={guests >= 100} onClick={() => setGuests(n => n + 1)}>+</button></div> : <button className={`button filter-button ${capacity ? 'selected' : ''}`} onClick={() => setFilterOpen(true)}><AdjustmentsHorizontalIcon /> Filters{capacity ? ' · 1' : ''}</button>}
        </div>
        {(error || (savedOnly && app.loadError)) && <p role="alert" className="error">{error || app.loadError}{savedOnly && app.loadError && <button className="text-button" onClick={() => void app.refresh()}>Retry loading spaces</button>}</p>}
        {savedOnly && <div className="results-line" aria-live="polite"><span>{savedCount} saved spaces · {visible.length} match {guests} {guests === 1 ? 'guest' : 'guests'}</span><button className="text-button" onClick={clearSavedFilters}>Clear filters</button></div>}
        {searched && (
          <div className="results-line">
            <span>{visible.length} spaces for {guests} guests · {format(parseISO(date), 'MMM d')} at {time}</span>
            <button className="text-button" onClick={() => { setSearched(false); setError(''); }}>Clear search</button>
          </div>
        )}
        <div className="space-grid">
          {(busy || (savedOnly && app.loading)) ? Array.from({ length: 6 }, (_, i) => <div className="skeleton" key={i} />) : visible.map((table, i) => (
            <article className="space-card" key={table.id}>
              <div className="space-photo">
                <button className="image-button" aria-label={`Reserve ${tableTitle(table.location, table)} ${table.tableNumber}`} onClick={() => setSelected(table)}>
                  <img src={tableImage(table.location, table)} alt={`${tableTitle(table.location, table)} at Gather`} loading="lazy" onError={e => { e.currentTarget.onerror = null; e.currentTarget.src = images.garden; }} />
                </button>
                {i < 2 && <span className="photo-tag">{i === 0 ? 'Guest favorite' : 'A little extra special'}</span>}
                <button className={`heart-button ${app.saved.includes(table.id) ? 'is-saved' : ''}`} aria-label={app.saved.includes(table.id) ? `Unsave ${table.tableNumber}` : `Save ${table.tableNumber}`} aria-pressed={app.saved.includes(table.id)} onClick={() => app.setSaved(all => all.includes(table.id) ? all.filter(id => id !== table.id) : [...all, table.id])}>
                  <HeartIcon />
                </button>
                <div className="photo-dots"><i /><i /><i /></div>
              </div>
              <div className="card-title">
                <h3><button onClick={() => setSelected(table)}>{tableTitle(table.location, table)}</button></h3>
                <span><StarIcon /> {i % 2 ? '4.98' : '4.95'}</span>
              </div>
              <p className="space-description">{table.description || 'A welcoming setting for your next gathering.'}</p>
              <p>{table.location.charAt(0) + table.location.slice(1).toLowerCase()} · Up to {table.capacity} guests</p>
              <p className="table-meta"><span>{table.tableNumber}</span>{!savedOnly && <><span className="availability-dot" />{searched ? 'Available for your visit' : 'Check availability to reserve'}</>}</p>
              <button className="reserve-link" onClick={() => setSelected(table)}>Reserve this table <ArrowRightIcon /></button>
            </article>
          ))}
        </div>
        {!busy && !(savedOnly && (app.loading || app.loadError)) && !visible.length && (
          <Empty title={savedOnly ? savedCount ? `None of your saved spaces fit ${guests} guests${category ? ' in this area' : ''}` : 'Save a space for later' : searched ? 'A different time, perhaps?' : 'Find a table for your visit'}>
            <p>{savedOnly ? savedCount ? 'Try fewer guests or another area. Out-of-service spaces are hidden.' : 'Tap the heart on any table to keep it here.' : 'Try another date, fewer guests, or a different seating preference.'}</p>
            {alternatives.map(t => <button className="button alternative" key={t} onClick={() => { setTime(t.slice(0, 5)); void search(t.slice(0, 5)); }}>{t.slice(0, 5)}</button>)}
            {savedOnly && (savedCount ? <button className="button" onClick={clearSavedFilters}>Clear filters</button> : <Link className="button primary" to="/">Discover spaces</Link>)}
            {!savedOnly && <><button className="button primary" disabled={busy} onClick={() => { setCategory(''); setCapacity(0); void search(); }}>Check all available tables</button><p className="small muted">This checks your selected date, time and guest count across all seating areas.</p></>}
          </Empty>
        )}
      </section>

      {!savedOnly && (
        <>
          <div className="gather-home-additions" ref={momentsRef}>
          <section className="popular-section gather-reveal" aria-labelledby="popular-title">
            <div className="home-section-heading"><div><span className="eyebrow">FROM OUR KITCHEN</span><h2 id="popular-title">Our popular dishes</h2><p>A few favourites to bring everyone to the table.</p></div><Link to="/menu" className="home-text-link">View full menu <ArrowRightIcon aria-hidden="true" /></Link></div>
            <div className="popular-grid">{popular.map(dish => <article className="food-card" key={dish.id}>
              <div className="food-photo"><MenuPhoto name={dish.name} imageUrl={dish.image} />{dish.tag && <span className="photo-tag">{dish.tag}</span>}</div>
              <div className="food-info"><h3>{dish.name}</h3><p>{dish.description}</p><b>{money(dish.price)}</b></div>
            </article>)}</div>
          </section>
          <section className="celebration-banner celebration-refresh gather-reveal" aria-labelledby="celebration-title">
            <div className="celebration-copy">
              <span className="eyebrow">LIFE'S BIG AND LITTLE OCCASIONS</span>
              <h2 id="celebration-title">Some moments deserve a little more Gather.</h2>
              <p>Birthdays, “I do”s, and just-because get-togethers. You bring the people. We'll make it special.</p>
              <ul className="celebration-checklist">{['Private halls for 10–120 guests', 'Curated menus & packages', 'A dedicated event coordinator', 'Decor and cake on request'].map(line => <li key={line}><CheckIcon aria-hidden="true" />{line}</li>)}</ul>
              <div className="celebration-actions"><Link className="button primary" to="/events">Let's plan something <ArrowRightIcon aria-hidden="true" /></Link><Link className="button" to="/events">View packages</Link></div>
            </div>
            <div className="celebration-visual">
              <img className="celebration-main-photo" src={images.event} alt="Elegant celebration table with flowers and a warm evening setting" loading="lazy" />
              <span className="celebration-sticker"><span>make<em>memories.</em></span></span>
              <div className="celebration-trust"><span><HeartIcon aria-hidden="true" /><strong>Your occasion</strong><small>Made personal</small></span><span><CheckBadgeIcon aria-hidden="true" /><strong>Thoughtful details</strong><small>From start to finish</small></span></div>
            </div>
          </section>
          <section className="gather-values values-refresh gather-reveal" aria-label="The Gather way">
            {[{ icon: SparklesIcon, number: '01', label: 'Fresh & thoughtful', title: 'Good things on every plate.', text: 'Seasonal ingredients, local flavours, and a reason to stay a little longer.' },
              { icon: HeartIcon, number: '02', label: 'Always welcoming', title: 'Come as you are.', text: 'A first date or your usual Tuesday. There’s always a place for you around our table.' },
              { icon: ClockIcon, number: '03', label: 'Effortless moments', title: 'Less planning. More living.', text: 'A few taps to your next great meal. We’ll take care of the little details.' }].map(value => <article className="gather-value-card" key={value.number}><div className="value-card-top"><value.icon aria-hidden="true" /><span>{value.number}</span></div><span className="eyebrow">{value.label}</span><h3>{value.title}</h3><p>{value.text}</p></article>)}
          </section>
          <section className="guest-section gather-reveal" aria-labelledby="guests-title">
            <div className="home-section-heading"><div><span className="eyebrow">GOOD COMPANY. GREAT MEMORIES.</span><h2 id="guests-title">What our guests say</h2><p>Little moments that stay with you.</p></div><ChatBubbleLeftRightIcon aria-hidden="true" /></div>
            <div className="guest-review-row" role="region" aria-label="Guest reviews; scroll for more" tabIndex={0}>
              {[{ name: 'Kavindi S.', initials: 'KS', occasion: 'Birthday dinner', quote: 'The cake arrived at just the right moment. Such a lovely evening with our favourite people.' },
                { name: 'Amal & Dilini', initials: 'AD', occasion: 'Anniversary', quote: 'A quiet corner, beautiful food, and time to ourselves. We’re already planning our next visit.' },
                { name: 'Tharindu R.', initials: 'TR', occasion: 'Team lunch', quote: 'Easy to organise, plenty for everyone to enjoy, and a warm welcome from the whole team.' }].map(review => <figure className="guest-review" key={review.name}><span className="review-stars" aria-label="5 out of 5 stars">★★★★★</span><blockquote>“{review.quote}”</blockquote><figcaption><span className="review-avatar" aria-hidden="true">{review.initials}</span><span><strong>{review.name}</strong><small>{review.occasion}</small></span></figcaption></figure>)}
            </div>
          </section>
          </div>
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
          <button className="button primary full" onClick={() => { setFilterOpen(false); void search(); }}>Show spaces</button>
          <button className="text-button" onClick={() => { setCapacity(0); setCategory(''); }}>Reset filters</button>
        </Modal>
      )}
    </div>
  );
}
