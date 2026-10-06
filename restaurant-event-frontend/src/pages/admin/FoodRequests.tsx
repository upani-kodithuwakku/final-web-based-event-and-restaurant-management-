import { useEffect, useState } from 'react';
import { formatDistanceToNow, isValid, parseISO } from 'date-fns';
import { CheckIcon, ChatBubbleLeftRightIcon, ClockIcon, MagnifyingGlassIcon } from '@heroicons/react/24/outline';
import { SectionHeading, Empty } from '../../components/UI';
import { foodRequestApi, errorMessage, type FoodRequestDto } from '../../services/api';
import { useApp } from '../../context/AppContext';

export default function FoodRequests() {
  const { user } = useApp();
  const allowed = user?.roles.some(r => ['ADMIN', 'MANAGER', 'WAITER', 'KITCHEN_STAFF'].includes(r));
  const [requests, setRequests] = useState<FoodRequestDto[]>([]);
  const [query, setQuery] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(true);
  const [busy, setBusy] = useState<number | null>(null);
  const load = async () => {
    try { setRequests(await foodRequestApi.queue()); setError(''); }
    catch (e) { setError(errorMessage(e)); }
    finally { setLoading(false); }
  };
  useEffect(() => {
    if (!allowed) return;
    void load();
    const timer = window.setInterval(() => void load(), 15000);
    return () => window.clearInterval(timer);
  }, [allowed]);
  const filtered = requests.filter(r => `${r.customerName} ${r.itemName || ''} ${r.message}`.toLowerCase().includes(query.trim().toLowerCase()));
  if (!allowed) return <p>You do not have access to customer food requests.</p>;
  return <div className="page-enter food-requests-page">
    <SectionHeading eyebrow="CUSTOMER SUPPORT" title="Food requests"
      description="A little care makes a great meal. Here’s what your guests need."
      action={<button className="button" onClick={load}>Refresh</button>} />
    <div className="fr-summary">
      <p aria-live="polite"><strong>{requests.length}</strong> open requests</p>
      <label className="fr-search"><MagnifyingGlassIcon aria-hidden="true" /><input type="search" aria-label="Search requests by customer, dish or message" placeholder="Search guests, dishes or requests…" value={query} onChange={e => setQuery(e.target.value)} /></label>
    </div>
    {error && <p className="error" role="alert">{error}</p>}
    {loading ? <p role="status">Loading requests…</p> : !requests.length && !error ?
      <div className="fr-empty"><ChatBubbleLeftRightIcon aria-hidden="true" /><Empty title="All caught up – no open food requests"><p>Every little request has been taken care of.</p></Empty></div> :
      !filtered.length && !error ? <Empty title="No matching requests"><button className="text-button" onClick={() => setQuery('')}>Clear search</button></Empty> :
      <div className="fr-grid">{filtered.map(request => {
        const created = parseISO(request.createdAt);
        const validDate = isValid(created);
        const waiting = validDate && Date.now() - created.getTime() > 15 * 60 * 1000;
        return <article className="fr-card" key={request.id}>
          <header><span className="fr-avatar" aria-hidden="true">{(request.customerName || 'Guest').trim().charAt(0).toUpperCase()}</span><div><span className="fr-kicker">Guest request</span><h3>{request.customerName || 'Guest'}</h3></div></header>
          <div className="fr-tags"><span className="fr-dish">{request.itemName || 'General request'}</span>{waiting && <span className="fr-wait"><ClockIcon aria-hidden="true" />Waiting a while</span>}</div>
          <blockquote>{request.message}</blockquote>
          <footer><time dateTime={validDate ? request.createdAt : undefined} title={validDate ? created.toLocaleString() : 'Time unavailable'}>{validDate ? formatDistanceToNow(created, { addSuffix: true }) : 'Time unavailable'}</time>
            <button className="button fr-resolve" disabled={busy !== null} aria-label={`Mark request from ${request.customerName || 'guest'} resolved`} onClick={async () => {
              setBusy(request.id);
              try { await foodRequestApi.resolve(request.id); setRequests(all => all.filter(r => r.id !== request.id)); setError(''); }
              catch (e) { setError(errorMessage(e)); }
              finally { setBusy(null); }
            }}><CheckIcon aria-hidden="true" />{busy === request.id ? 'Saving…' : 'Mark resolved'}</button>
          </footer>
        </article>;
      })}</div>}
  </div>;
}
