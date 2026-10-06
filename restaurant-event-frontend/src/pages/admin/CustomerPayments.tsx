import { useEffect, useState } from 'react';
import { format } from 'date-fns';
import { SectionHeading, Empty } from '../../components/UI';
import { customerPaymentApi, errorMessage, type CustomerPaymentDto } from '../../services/api';
import { useApp } from '../../context/AppContext';
import { money } from '../../data';

const FILTERS = ['', 'PENDING', 'PAID', 'FAILED', 'REFUNDED'];
// Status changes staff may make; mirrors CustomerPaymentService.updateStatus.
const ACTIONS: Record<string, { label: string; status: string }[]> = {
  PENDING: [{ label: 'Mark as paid', status: 'PAID' }, { label: 'Mark failed', status: 'FAILED' }],
  PAID: [{ label: 'Refund', status: 'REFUNDED' }],
  FAILED: [{ label: 'Reopen', status: 'PENDING' }],
  REFUNDED: [],
};

export default function CustomerPayments() {
  const { user } = useApp();
  const isAdmin = !!user?.roles.includes('ADMIN');
  const [payments, setPayments] = useState<CustomerPaymentDto[]>([]);
  const [filter, setFilter] = useState('');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [busy, setBusy] = useState<number | null>(null);

  const load = async (status = filter) => {
    setLoading(true);
    try { setPayments(await customerPaymentApi.all(status || undefined)); setError(''); }
    catch (e) { setError(errorMessage(e)); }
    finally { setLoading(false); }
  };
  useEffect(() => { void load(filter); }, [filter]);

  const run = async (id: number, action: () => Promise<unknown>) => {
    setBusy(id); setError('');
    try { await action(); await load(); }
    catch (e) { setError(errorMessage(e)); }
    finally { setBusy(null); }
  };

  const pendingTotal = payments.filter(p => p.status === 'PENDING').reduce((s, p) => s + p.amount, 0);

  return (
    <div className="page-enter">
      <SectionHeading eyebrow="BILLING" title="Customer payments"
        description="Card payments settle instantly. Mark pay-at-outlet payments as paid when you collect them."
        action={<button className="button" onClick={() => void load()}>Refresh</button>} />
      <div className="pills" style={{ marginBottom: 16 }}>
        {FILTERS.map(f => <button key={f || 'all'} className={filter === f ? 'active' : ''} onClick={() => setFilter(f)}>
          {f ? f.charAt(0) + f.slice(1).toLowerCase() : 'All'}</button>)}
      </div>
      {!filter && payments.length > 0 && <p className="muted" style={{ marginBottom: 12 }}>Awaiting collection: <b>{money(pendingTotal)}</b></p>}
      {error && <p className="error" role="alert">{error}</p>}
      {loading ? <div className="skeleton" style={{ height: 200 }} /> : !payments.length ? <Empty title="No payments found" /> : (
        <div className="admin-payments">
          {payments.map(p => (
            <article className="admin-payment-row" key={p.id}>
              <div>
                <b>{p.paymentReference}</b>
                <p>{p.purpose === 'FOOD_ORDER' ? 'Food order' : 'Event booking'} {p.targetReference} · Customer #{p.customerId}</p>
                <span className="muted small">
                  {p.method === 'CARD' ? `Card${p.cardLast4 ? ` •••• ${p.cardLast4}` : ''}` : 'Pay at outlet'}
                  {' · '}{format(new Date(p.createdAt), 'd MMM yyyy, h:mm a')}
                </span>
              </div>
              <strong>{money(p.amount)}</strong>
              <span className={`pay-status ${p.status.toLowerCase()}`}>{p.status.toLowerCase()}</span>
              <div className="admin-payment-actions">
                {ACTIONS[p.status].map(a => (
                  <button key={a.status} className="button" disabled={busy !== null}
                    onClick={() => void run(p.id, () => customerPaymentApi.updateStatus(p.id, a.status))}>{a.label}</button>
                ))}
                {isAdmin && <button className="text-button danger" disabled={busy !== null}
                  onClick={() => { if (window.confirm(`Delete payment ${p.paymentReference}? This cannot be undone.`)) void run(p.id, () => customerPaymentApi.remove(p.id)); }}>
                  Delete</button>}
              </div>
            </article>
          ))}
        </div>
      )}
    </div>
  );
}
