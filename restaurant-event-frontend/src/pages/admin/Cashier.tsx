import { useState, useEffect } from 'react';
import { SectionHeading, Badge, Modal, Empty } from '../../components/UI';
import { api, customerPaymentApi, type CustomerPaymentDto, billingApi, type InvoiceDto, type PaymentDto, errorMessage } from '../../services/api';
import { money } from '../../data';
import { format } from 'date-fns';

type CashierSummary = { todayRevenue: number; todayCardRevenue: number; todayOutletRevenue: number; unpaidInvoices: { count: number; total: number }; awaitingCollection: { count: number; total: number }; recentPayments: { reference: string; type: string; amount: number; method: string; paidAt: string }[] };
const typeLabel = (type: string) => type.toLowerCase().replaceAll('_', ' ');

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
  const [search, setSearch] = useState('');
  const [tab, setTab] = useState<'collect' | 'invoices'>('collect');
  const [summary, setSummary] = useState<CashierSummary | null>(null);
  const [outlet, setOutlet] = useState<CustomerPaymentDto[]>([]);
  const [receipts, setReceipts] = useState<CustomerPaymentDto[]>([]);
  const [collecting, setCollecting] = useState<number | null>(null);

  const load = async () => {
    setLoading(true);
    try {
      const [allInvoices, totals, pending] = await Promise.all([billingApi.invoices(), api.get<CashierSummary>('/cashier/summary'), customerPaymentApi.all('PENDING')]);
      setInvoices(allInvoices); setSummary(totals.data); setOutlet(pending.filter(p => p.method === 'PAY_AT_OUTLET'));
    }
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
      await load();
    } catch (e) { setErr(errorMessage(e)); }
    finally { setBusy(false); }
  };

  const collect = async (payment: CustomerPaymentDto) => {
    setCollecting(payment.id); setErr('');
    try {
      const receipt = await customerPaymentApi.updateStatus(payment.id, 'PAID');
      setReceipts(previous => [receipt, ...previous]);
      await load();
    } catch (e) { setErr(errorMessage(e)); }
    finally { setCollecting(null); }
  };
  const visible = invoices.filter(i => (!filter || i.status === filter) && i.invoiceNumber.toLowerCase().includes(search.toLowerCase()));

  return (
    <div className="page-enter cashier-page">
      <SectionHeading eyebrow="CASHIER" title="Invoices & payments" description="Keep collections moving and see what came in today." action={<button className="button secondary" onClick={() => void load()} disabled={loading}>Refresh</button>} />
      {err && <p className="error" role="alert">{err}</p>}
      <div className="cashier-stats" aria-label="Payment overview">
        <div className="cashier-stat cashier-revenue"><span>Today's revenue</span><strong>{summary ? money(summary.todayRevenue) : '—'}</strong><small>Collected today · Asia/Colombo</small></div>
        <div className="cashier-stat"><span>Card vs outlet</span><strong>{summary ? money(summary.todayCardRevenue) : '—'}</strong><small>Card / online · outlet {summary ? money(summary.todayOutletRevenue) : '—'}</small></div>
        <div className="cashier-stat"><span>Unpaid invoices</span><strong>{summary?.unpaidInvoices.count ?? '—'}</strong><small>{summary ? money(summary.unpaidInvoices.total) : '—'} · draft & issued</small></div>
        <div className="cashier-stat cashier-collection"><span>To collect at outlet</span><strong>{summary?.awaitingCollection.count ?? '—'}</strong><small>{summary ? money(summary.awaitingCollection.total) : '—'}</small></div>
      </div>
      <div className="cashier-layout">
        <section className="cashier-workspace">
          <div className="cashier-tabs" role="tablist" aria-label="Billing views">
            <button id="cashier-collect-tab" role="tab" aria-selected={tab === 'collect'} aria-controls="cashier-panel" onClick={() => setTab('collect')}>To collect ({outlet.length})</button>
            <button id="cashier-invoices-tab" role="tab" aria-selected={tab === 'invoices'} aria-controls="cashier-panel" onClick={() => setTab('invoices')}>Invoices</button>
          </div>
          <div id="cashier-panel" role="tabpanel" aria-labelledby={`cashier-${tab}-tab`}>
            {loading ? <div className="skeleton" style={{ height: 160 }} /> : tab === 'collect' ? <>
              {receipts.map(p => <div className="cashier-receipt" key={p.id} role="status">{p.paymentReference} · Payment collected</div>)}
              {outlet.length === 0 ? <Empty title="No outlet payments awaiting collection" /> : outlet.map(p => <article className="cashier-outlet-row" key={p.id}>
                <div><strong>{p.targetReference || p.paymentReference}</strong><p>{typeLabel(p.purpose)} · {p.paymentReference}</p><small>{format(new Date(p.createdAt), 'd MMM yyyy, HH:mm')}</small></div>
                <div className="cashier-outlet-action"><strong>{money(p.amount)}</strong><button className="button primary" disabled={collecting !== null} onClick={() => void collect(p)}>{collecting === p.id ? 'Collecting…' : 'Mark as paid'}</button></div>
              </article>)}
            </> : <>
              <div className="cashier-filters"><label>Search invoices<input type="search" placeholder="Invoice number" value={search} onChange={e => setSearch(e.target.value)} /></label><label>Status<select value={filter} onChange={e => setFilter(e.target.value)}><option value="">All statuses</option>{['DRAFT', 'ISSUED', 'PAID', 'VOID'].map(status => <option key={status}>{status}</option>)}</select></label></div>
              {visible.length === 0 ? <Empty title="No invoices found" /> : <div className="cashier-table-wrap"><table className="cashier-table"><thead><tr><th>Invoice</th><th>Type</th><th>Amount</th><th>Date</th><th>Status</th></tr></thead><tbody>{visible.map(inv => <tr key={inv.id}><td><button className="cashier-invoice-link" onClick={() => void openInvoice(inv)}>{inv.invoiceNumber}</button></td><td>{typeLabel(inv.invoiceType)}</td><td>{money(inv.totalAmount)}</td><td>{inv.issuedAt ? format(new Date(inv.issuedAt), 'd MMM yyyy') : 'Draft'}</td><td><Badge status={inv.status} /><small className="cashier-paid-label">{inv.status === 'PAID' ? 'Paid' : ['DRAFT', 'ISSUED'].includes(inv.status) ? 'Unpaid' : ''}</small></td></tr>)}</tbody></table></div>}
            </>}
          </div>
        </section>
        <aside className="cashier-recent"><h3>Recent payments</h3><p>Latest collections from all payment types.</p>{summary?.recentPayments.length ? summary.recentPayments.map((p, index) => <article key={`${p.reference}-${index}`}><div><strong>{p.reference}</strong><b>{money(p.amount)}</b></div><small>{typeLabel(p.type)} · {typeLabel(p.method)}</small><small>{format(new Date(p.paidAt), 'd MMM, HH:mm')}</small></article>) : <p>No payments collected yet.</p>}</aside>
      </div>

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
