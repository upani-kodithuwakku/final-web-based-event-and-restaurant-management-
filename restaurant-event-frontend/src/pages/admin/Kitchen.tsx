import { useState, useEffect } from 'react';
import { ArrowPathIcon, FireIcon } from '@heroicons/react/24/outline';
import { SectionHeading, Badge, Empty } from '../../components/UI';
import { kitchenApi, type OrderDto, errorMessage } from '../../services/api';
import { formatDistanceToNow } from 'date-fns';
import { money } from '../../data';

const STATUS_FLOW: Record<string, string | null> = {
  PENDING: 'PREPARING',
  PREPARING: 'READY',
  READY: null,
};

const STATUS_LABEL: Record<string, string> = {
  PENDING: 'Start preparing',
  PREPARING: 'Mark ready',
};

export default function KitchenOrders() {
  const [orders, setOrders] = useState<OrderDto[]>([]);
  const [loading, setLoading] = useState(true);
  const [busy, setBusy] = useState<number | null>(null);
  const [err, setErr] = useState('');

  const load = async () => {
    setLoading(true);
    try { setOrders(await kitchenApi.queue()); }
    catch (e) { setErr(errorMessage(e)); }
    finally { setLoading(false); }
  };

  useEffect(() => { void load(); }, []);

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

  const byStatus = (s: string) => orders.filter(o => o.status === s);

  const col = (label: string, status: string, accent: string) => (
    <div className="kitchen-col">
      <h3 style={{ fontSize: 13, fontWeight: 700, letterSpacing: '.06em', textTransform: 'uppercase', color: accent, marginBottom: 12 }}>
        {label} <span style={{ fontWeight: 400, color: 'var(--foggy)' }}>({byStatus(status).length})</span>
      </h3>
      {byStatus(status).length === 0 ? (
        <p className="muted small">Nothing here right now.</p>
      ) : byStatus(status).map(order => (
        <div key={order.id} className="kitchen-card">
          <div className="row-between" style={{ marginBottom: 8 }}>
            <b style={{ fontSize: 15 }}>#{order.orderReference.slice(-6)}</b>
            <span className="small muted">{formatDistanceToNow(new Date(order.createdAt), { addSuffix: true })}</span>
          </div>
          {order.tableId && <p className="small muted" style={{ marginBottom: 8 }}>Table {order.tableId}</p>}
          <ul style={{ listStyle: 'none', padding: 0, margin: '0 0 12px', display: 'flex', flexDirection: 'column', gap: 4 }}>
            {order.items.map(item => (
              <li key={item.id} style={{ display: 'flex', justifyContent: 'space-between', fontSize: 14 }}>
                <span>{item.quantity}× {item.itemNameSnapshot}</span>
                {item.specialNote && <span className="muted small" style={{ fontStyle: 'italic' }}>{item.specialNote}</span>}
              </li>
            ))}
          </ul>
          {STATUS_FLOW[order.status] && (
            <button
              className="button primary full"
              style={{ fontSize: 13 }}
              disabled={busy === order.id}
              onClick={() => advance(order)}
            >
              {busy === order.id ? 'Updating…' : STATUS_LABEL[order.status]}
            </button>
          )}
        </div>
      ))}
    </div>
  );

  return (
    <div className="page-enter">
      <SectionHeading
        eyebrow="KITCHEN"
        title="Orders queue"
        description="Live kitchen view. Update order status as you prepare and complete each ticket."
        action={
          <button className="button" onClick={load} disabled={loading}>
            <ArrowPathIcon style={{ width: 14, height: 14 }} /> {loading ? 'Refreshing…' : 'Refresh'}
          </button>
        }
      />

      {err && <p className="error">{err}</p>}

      {loading ? (
        <div className="kitchen-grid">{[0, 1, 2].map(i => <div key={i} className="skeleton" style={{ height: 200 }} />)}</div>
      ) : orders.length === 0 ? (
        <Empty title="Kitchen is quiet">
          <p>No active orders right now. New orders will appear here automatically.</p>
        </Empty>
      ) : (
        <div className="kitchen-grid">
          {col('New orders', 'PENDING', 'var(--arches)')}
          {col('Preparing', 'PREPARING', 'var(--babu)')}
          {col('Ready to serve', 'READY', '#16a34a')}
        </div>
      )}
    </div>
  );
}
