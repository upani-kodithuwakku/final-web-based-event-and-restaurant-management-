import { useState, useEffect, useRef } from 'react';
import { ArrowPathIcon, FireIcon, CheckIcon } from '@heroicons/react/24/outline';
import { SectionHeading } from '../../components/UI';
import { kitchenApi, type OrderDto, errorMessage } from '../../services/api';
import { useApp } from '../../context/AppContext';

const STATUS_FLOW: Record<string, string | null> = {
  PENDING: 'PREPARING',
  PREPARING: 'READY',
  READY: null,
};
const lanes = [
  { status: 'PENDING', title: 'New orders', action: 'Start preparing' },
  { status: 'PREPARING', title: 'Preparing', action: 'Mark ready' },
  { status: 'READY', title: 'Ready to serve', action: '' },
];
const ageMinutes = (createdAt: string) => Math.max(0, Math.floor((Date.now() - new Date(createdAt).getTime()) / 60000));

export default function KitchenOrders() {
  const app = useApp();
  const [orders, setOrders] = useState<OrderDto[]>([]);
  const [loading, setLoading] = useState(true);
  const [busy, setBusy] = useState<number | null>(null);
  const [err, setErr] = useState('');
  const [updatedAt, setUpdatedAt] = useState<Date>();
  const [arrivals, setArrivals] = useState<number[]>([]);
  const knownIds = useRef<Set<number> | null>(null);
  const requestRunning = useRef(false);
  const load = async () => {
    if (requestRunning.current) return;
    requestRunning.current = true;
    setLoading(true);
    try {
      const next = await kitchenApi.queue();
      setArrivals(knownIds.current ? next.filter(order => !knownIds.current!.has(order.id)).map(order => order.id) : []);
      knownIds.current = new Set(next.map(order => order.id));
      setOrders(next); setErr(''); setUpdatedAt(new Date());
    } catch (e) { setErr(errorMessage(e)); }
    finally { setLoading(false); requestRunning.current = false; }
  };
  useEffect(() => {
    void load();
    const timer = window.setInterval(() => void load(), 15000);
    return () => window.clearInterval(timer);
  }, []);
  const advance = async (order: OrderDto) => {
    const next = STATUS_FLOW[order.status];
    if (!next) return;
    setBusy(order.id); setErr('');
    try {
      const updated = await kitchenApi.updateStatus(order.id, next);
      setOrders(all => all.map(o => o.id === order.id ? updated : o).filter(o => o.status !== 'SERVED'));
    } catch (e) { setErr(errorMessage(e)); }
    finally { setBusy(null); }
  };
  const openOrders = orders.filter(order => lanes.some(lane => lane.status === order.status));
  const waiting = openOrders.filter(order => order.status === 'PENDING');
  const oldest = waiting.length ? Math.max(...waiting.map(order => ageMinutes(order.createdAt))) : 0;
  return (
    <div className="page-enter kitchen-board-page">
      <SectionHeading eyebrow="KITCHEN" title="Orders queue" description="A little focus for every stage. The queue refreshes every 15 seconds." />
      <div className="kitchen-summary">
        <div><span>Total open orders</span><strong>{openOrders.length}</strong></div>
        <div><span>Oldest waiting</span><strong>{oldest} min</strong></div>
        <div className="kitchen-refresh"><button className="button" onClick={() => void load()} disabled={loading}><ArrowPathIcon aria-hidden="true" />{loading ? 'Refreshing…' : 'Refresh'}</button><small aria-live="polite">{updatedAt ? `Updated ${updatedAt.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}` : 'Loading queue…'}</small></div>
      </div>
      {err && <p className="error" role="alert">{err}</p>}
      <div className="kitchen-kanban" aria-busy={loading}>
        {lanes.map(lane => {
          const tickets = openOrders.filter(order => order.status === lane.status);
          return <section key={lane.status} className={`kitchen-lane lane-${lane.status.toLowerCase()}`} aria-labelledby={`lane-${lane.status}`}>
            <header><h2 id={`lane-${lane.status}`}>{lane.title}</h2><span aria-label={`${tickets.length} orders`}>{tickets.length}</span></header>
            <div className="kitchen-tickets" tabIndex={0} aria-label={`${lane.title} orders`}>
              {!tickets.length && <p className="muted small kitchen-lane-empty">{loading && !updatedAt ? 'Loading orders…' : 'Nothing here right now.'}</p>}
              {tickets.map(order => <article key={order.id} className={`kitchen-ticket ${arrivals.includes(order.id) ? 'ticket-arrived' : ''}`}>
                <div className="kitchen-ticket-heading"><strong>#{order.orderReference}</strong>{ageMinutes(order.createdAt) > 20 && <span className="kitchen-late">Late</span>}</div>
                <div className="kitchen-ticket-meta"><span>{order.tableId ? `Table ${app.tables.find(table => table.id === order.tableId)?.tableNumber ?? order.tableId}` : 'Takeaway'}</span><time dateTime={order.createdAt}>{ageMinutes(order.createdAt)} min ago</time></div>
                <ul className="kitchen-ticket-items">{order.items.map(item => <li key={item.id}><span className="kitchen-quantity">{item.quantity}</span><div><strong>{item.itemNameSnapshot}</strong>{item.specialNote && <small>{item.specialNote}</small>}</div></li>)}</ul>
                {order.specialNote && <p className="kitchen-customer-note"><strong>Customer note</strong>{order.specialNote}</p>}
                <footer>{STATUS_FLOW[order.status] ? <button className="button primary" disabled={busy !== null || loading} onClick={() => void advance(order)}>{order.status === 'PENDING' ? <FireIcon aria-hidden="true" /> : <CheckIcon aria-hidden="true" />}{busy === order.id ? 'Updating…' : lane.action}</button> : <span className="kitchen-ready-label"><CheckIcon aria-hidden="true" />Ready for the serving team</span>}</footer>
              </article>)}
            </div>
          </section>;
        })}
      </div>
    </div>
  );
}
