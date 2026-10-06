import { useState, useEffect } from 'react';
import { SectionHeading, Badge, Modal, Empty } from '../../components/UI';
import { billingApi, type InvoiceDto, type PaymentDto, errorMessage } from '../../services/api';
import { money } from '../../data';
import { format } from 'date-fns';

const PAYMENT_METHODS = ['CASH', 'CARD', 'ONLINE'];

export default function CashierDashboard() {
  const [invoices, setInvoices] = useState<InvoiceDto[]>([]);
  const [loading, setLoading] = useState(true);
  const [selected, setSelected] = useState<InvoiceDto | null>(null);
  const [payments, setPayments] = useState<PaymentDto[]>([]);
  const [payForm, setPayForm] = useState({ method: 'CASH', amount: '' });
  const [busy, setBusy] = useState(false);
  const [err, setErr] = useState('');
  const [filter, setFilter] = useState('');

  const load = async () => {
    setLoading(true);
    try { setInvoices(await billingApi.invoices()); }
    catch (e) { setErr(errorMessage(e)); }
    finally { setLoading(false); }
  };

  useEffect(() => { void load(); }, []);

  const openInvoice = async (inv: InvoiceDto) => {
    setSelected(inv); setErr('');
    setPayForm({ method: 'CASH', amount: String(inv.totalAmount) });
    try { setPayments(await billingApi.payments(inv.id)); }
    catch { setPayments([]); }
  };

  const processPayment = async () => {
    if (!selected) return;
    setBusy(true); setErr('');
    try {
      const p = await billingApi.pay({ invoiceId: selected.id, method: payForm.method, amount: parseFloat(payForm.amount) });
      setPayments(prev => [p, ...prev]);
      const updated = { ...selected, status: p.status === 'PAID' ? 'PAID' : selected.status };
      setInvoices(all => all.map(i => i.id === selected.id ? updated as InvoiceDto : i));
      setSelected(updated as InvoiceDto);
    } catch (e) { setErr(errorMessage(e)); }
    finally { setBusy(false); }
  };

  const visible = invoices.filter(i => !filter || i.status === filter);
  const unpaid = invoices.filter(i => i.status === 'ISSUED').length;
  const todayTotal = invoices
    .filter(i => i.status === 'PAID' && i.issuedAt?.startsWith(format(new Date(), 'yyyy-MM-dd')))
    .reduce((s, i) => s + i.totalAmount, 0);

  return (
    <div className="page-enter">
      <SectionHeading
        eyebrow="CASHIER"
        title="Invoices & payments"
        description="Process payments, record transactions, and manage billing."
        action={
          <select value={filter} onChange={e => setFilter(e.target.value)} style={{ padding: '8px 14px', border: '1.5px solid var(--gray-200)', borderRadius: 'var(--radius-sm)', fontSize: 14, background: 'var(--white)' }}>
            <option value="">All statuses</option>
            {['DRAFT', 'ISSUED', 'PAID', 'VOID'].map(s => <option key={s}>{s}</option>)}
          </select>
        }
      />

      <div className="report-grid" style={{ marginBottom: 24 }}>
        <div className="report-card">
          <h3>Unpaid invoices</h3>
          <div className="report-row"><span>Awaiting payment</span><b style={{ color: 'var(--arches)', fontSize: 24 }}>{unpaid}</b></div>
        </div>
        <div className="report-card">
          <h3>Today's revenue</h3>
          <div className="report-row"><span>Total collected today</span><b style={{ color: 'var(--babu)', fontSize: 18 }}>{money(todayTotal)}</b></div>
        </div>
      </div>

      {err && <p className="error">{err}</p>}

      {loading ? <div className="skeleton" style={{ height: 200 }} /> : visible.length === 0 ? (
        <Empty title="No invoices found" />
      ) : (
        <div className="invoice-list">
          {visible.map(inv => (
            <div key={inv.id} className="invoice-row" onClick={() => openInvoice(inv)} style={{ cursor: 'pointer' }}>
              <div className="eb-info">
                <h3>{inv.invoiceNumber}</h3>
                <p>{inv.invoiceType} · {money(inv.totalAmount)} · {inv.issuedAt ? format(new Date(inv.issuedAt), 'd MMM yyyy') : '—'}</p>
              </div>
              <Badge status={inv.status} />
            </div>
          ))}
        </div>
      )}

      {selected && (
        <Modal title={`Invoice — ${selected.invoiceNumber}`} onClose={() => setSelected(null)}>
          <div style={{ display: 'flex', flexDirection: 'column', gap: 8 }}>
            <div className="report-row"><span>Type</span><b>{selected.invoiceType}</b></div>
            <div className="report-row"><span>Subtotal</span><b>{money(selected.subtotal)}</b></div>
            <div className="report-row"><span>Service charge</span><b>{money(selected.serviceCharge)}</b></div>
            <div className="report-row"><span>Tax</span><b>{money(selected.taxAmount)}</b></div>
            <div className="report-row"><span>Discount</span><b>{money(selected.discountAmount)}</b></div>
            <div className="report-row"><strong>Total</strong><strong style={{ fontSize: 18 }}>{money(selected.totalAmount)}</strong></div>
            <Badge status={selected.status} />

            {payments.length > 0 && (
              <div style={{ marginTop: 8 }}>
                <p className="eyebrow" style={{ marginBottom: 6 }}>Payment history</p>
                {payments.map(p => (
                  <div key={p.id} className="report-row" style={{ fontSize: 13 }}>
                    <span>{p.method} · {p.paidAt ? format(new Date(p.paidAt), 'd MMM HH:mm') : '—'}</span>
                    <b>{money(p.amount)} <Badge status={p.status} /></b>
                  </div>
                ))}
              </div>
            )}

            {selected.status === 'ISSUED' && (
              <>
                <div className="divider" />
                <p className="eyebrow">Record payment</p>
                <div className="form-row">
                  <label>Method
                    <select value={payForm.method} onChange={e => setPayForm({ ...payForm, method: e.target.value })}>
                      {PAYMENT_METHODS.map(m => <option key={m}>{m}</option>)}
                    </select>
                  </label>
                  <label>Amount (LKR)<input type="number" step="0.01" value={payForm.amount} onChange={e => setPayForm({ ...payForm, amount: e.target.value })} /></label>
                </div>
                {err && <p className="error">{err}</p>}
                <button className="button primary full" disabled={busy} onClick={processPayment}>
                  {busy ? 'Processing…' : 'Record payment'}
                </button>
              </>
            )}
          </div>
        </Modal>
      )}
    </div>
  );
}
