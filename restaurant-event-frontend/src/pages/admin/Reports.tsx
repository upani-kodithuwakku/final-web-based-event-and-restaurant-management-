import { useEffect, useState } from 'react';
import { format, parseISO, subDays } from 'date-fns';
import { ArrowDownTrayIcon, CalendarDaysIcon, TableCellsIcon, UsersIcon, FireIcon, SparklesIcon, ArchiveBoxIcon } from '@heroicons/react/24/outline';
import { SectionHeading } from '../../components/UI';
import {
  errorMessage, reportApi,
  type DashboardReportDto, type EventReportDto, type InventoryReportDto, type ReservationReportDto, type SalesReportDto,
} from '../../services/api';
import { money } from '../../data';

const iso = (d: Date) => format(d, 'yyyy-MM-dd');
const PRESETS = [{ label: 'Today', days: 1 }, { label: '7 days', days: 7 }, { label: '30 days', days: 30 }];
const nice = (s: string) => s.charAt(0) + s.slice(1).toLowerCase().replace(/_/g, ' ');
// Status bar colours, matching the badges used elsewhere.
const TONE: Record<string, string> = {
  PENDING: 'var(--arches)', CONFIRMED: 'var(--babu)', CHECKED_IN: '#0066FF', COMPLETED: 'var(--hof)',
  CANCELLED: 'var(--rausch)', NO_SHOW: 'var(--gray-300)', REJECTED: 'var(--rausch)',
};

export default function AdminReports() {
  const today = new Date();
  const [range, setRange] = useState({ from: iso(subDays(today, 29)), to: iso(today) });
  const [dash, setDash] = useState<DashboardReportDto | null>(null);
  const [inventory, setInventory] = useState<InventoryReportDto | null>(null);
  const [res, setRes] = useState<ReservationReportDto | null>(null);
  const [sales, setSales] = useState<SalesReportDto | null>(null);
  const [events, setEvents] = useState<EventReportDto | null>(null);
  const [loading, setLoading] = useState(true);
  const [err, setErr] = useState('');

  useEffect(() => {
    Promise.all([reportApi.dashboard(), reportApi.inventory()])
      .then(([d, i]) => { setDash(d); setInventory(i); })
      .catch(e => setErr(errorMessage(e)));
  }, []);

  useEffect(() => {
    if (range.from > range.to) { setErr("The 'from' date must be on or before the 'to' date."); return; }
    setLoading(true); setErr('');
    Promise.all([reportApi.reservations(range), reportApi.sales(range), reportApi.events(range)])
      .then(([r, s, e]) => { setRes(r); setSales(s); setEvents(e); })
      .catch(e => setErr(errorMessage(e)))
      .finally(() => setLoading(false));
  }, [range.from, range.to]);

  const preset = (days: number) => setRange({ from: iso(subDays(today, days - 1)), to: iso(today) });
  const activePreset = PRESETS.find(p => range.to === iso(today) && range.from === iso(subDays(today, p.days - 1)));

  const exportCsv = () => {
    const rows: (string | number)[][] = [['Report', 'Metric', 'Value'], ['Range', 'From', range.from], ['Range', 'To', range.to]];
    if (dash) Object.entries(dash).forEach(([k, v]) => rows.push(['Dashboard', k, v]));
    if (res) {
      rows.push(['Reservations', 'total', res.totalReservations], ['Reservations', 'guests', res.totalGuests], ['Reservations', 'noShowRate%', res.noShowRate]);
      Object.entries(res.byStatus).forEach(([k, v]) => rows.push(['Reservations', k, v]));
    }
    if (sales) {
      rows.push(['Sales', 'orders', sales.orderCount], ['Sales', 'cancelled', sales.cancelledOrders], ['Sales', 'revenue', sales.foodRevenue], ['Sales', 'averageOrder', sales.averageOrderValue]);
      sales.topItems.forEach(t => rows.push(['Top items', t.name, t.quantity]));
    }
    if (events) {
      rows.push(['Events', 'total', events.totalBookings], ['Events', 'confirmedGuests', events.confirmedGuests], ['Events', 'confirmedValue', events.confirmedValue]);
      Object.entries(events.byStatus).forEach(([k, v]) => rows.push(['Events', k, v]));
    }
    inventory?.lowStockItems.forEach(i => rows.push(['Low stock', i.name, `${i.currentQuantity} ${i.unit} (reorder at ${i.reorderLevel})`]));
    const csv = rows.map(r => r.map(c => `"${String(c).replace(/"/g, '""')}"`).join(',')).join('\n');
    const url = URL.createObjectURL(new Blob([csv], { type: 'text/csv' }));
    const a = Object.assign(document.createElement('a'), { href: url, download: `gather-report-${range.from}-to-${range.to}.csv` });
    a.click(); URL.revokeObjectURL(url);
  };

  const kpis = dash ? [
    { icon: CalendarDaysIcon, label: "Today's reservations", value: dash.todayReservations },
    { icon: TableCellsIcon, label: 'Tables available', value: `${dash.availableTables} / ${dash.totalTables}` },
    { icon: UsersIcon, label: 'Customers', value: dash.totalCustomers },
    { icon: FireIcon, label: 'Active food orders', value: dash.activeFoodOrders },
    { icon: SparklesIcon, label: 'Events awaiting approval', value: dash.pendingEventBookings },
    { icon: ArchiveBoxIcon, label: 'Low-stock items', value: dash.lowStockItems, warn: dash.lowStockItems > 0 },
  ] : [];

  return (
    <div className="page-enter reports-page">
      <SectionHeading eyebrow="ANALYTICS" title="Reports & overview"
        description={`System snapshot as of ${format(today, 'EEEE, d MMMM yyyy')}.`}
        action={<button className="button" onClick={exportCsv} disabled={!dash && !res}><ArrowDownTrayIcon style={{ width: 14, height: 14 }} /> Export CSV</button>} />

      {err && <p className="error" role="alert">{err}</p>}

      <div className="kpi-grid">
        {dash ? kpis.map(k => (
          <div className={`kpi-card${k.warn ? ' warn' : ''}`} key={k.label}>
            <k.icon /><span>{k.label}</span><strong>{k.value}</strong>
          </div>
        )) : Array.from({ length: 6 }, (_, i) => <div className="skeleton" key={i} style={{ height: 104 }} />)}
      </div>

      <div className="report-range">
        <div className="pills">
          {PRESETS.map(p => <button key={p.label} className={activePreset === p ? 'active' : ''} onClick={() => preset(p.days)}>{p.label}</button>)}
        </div>
        <label>From<input type="date" value={range.from} max={range.to} onChange={e => setRange({ ...range, from: e.target.value })} /></label>
        <label>To<input type="date" value={range.to} min={range.from} onChange={e => setRange({ ...range, to: e.target.value })} /></label>
      </div>

      {loading && !res ? <div className="skeleton" style={{ height: 260 }} /> : (
        <div className="report-grid">
          {res && <div className="report-card">
            <h3>Reservations</h3>
            <div className="report-row"><span>Reservations</span><b>{res.totalReservations}</b></div>
            <div className="report-row"><span>Guests</span><b>{res.totalGuests}</b></div>
            <StatusBars counts={res.byStatus} />
            <div className="report-row"><span>No-show rate</span><b style={{ color: res.noShowRate > 15 ? 'var(--rausch)' : undefined }}>{res.noShowRate}%</b></div>
            <div className="report-row"><span>Busiest day</span><b>{res.busiestDay ? `${format(parseISO(res.busiestDay), 'd MMM')} (${res.busiestDayReservations})` : '—'}</b></div>
          </div>}

          {sales && <div className="report-card">
            <h3>Food sales</h3>
            <div className="report-row"><span>Revenue</span><b style={{ color: 'var(--babu)', fontSize: 18 }}>{money(sales.foodRevenue)}</b></div>
            <div className="report-row"><span>Orders</span><b>{sales.orderCount}</b></div>
            <div className="report-row"><span>Average order</span><b>{money(sales.averageOrderValue)}</b></div>
            <div className="report-row"><span>Cancelled orders</span><b>{sales.cancelledOrders}</b></div>
            <h4 className="report-sub">Top dishes</h4>
            {sales.topItems.length ? sales.topItems.map((t, i) => (
              <div className="report-row" key={t.name}><span>{i + 1}. {t.name}</span><b>{t.quantity} sold · {money(t.revenue)}</b></div>
            )) : <p className="muted small">No orders in this period.</p>}
          </div>}

          {events && <div className="report-card">
            <h3>Events &amp; celebrations</h3><p className="muted small">Bookings received during the selected dates, including future celebrations.</p>
            <div className="report-row"><span>Bookings</span><b>{events.totalBookings}</b></div>
            <StatusBars counts={events.byStatus} />
            <div className="report-row"><span>Confirmed guests</span><b>{events.confirmedGuests}</b></div>
            <div className="report-row"><span>Confirmed value</span><b style={{ color: 'var(--babu)' }}>{money(events.confirmedValue)}</b></div>
          </div>}

          {inventory && <div className="report-card">
            <h3>Inventory</h3>
            <div className="report-row"><span>Active items</span><b>{inventory.activeItems}</b></div>
            <div className="report-row"><span>Low stock</span><b style={{ color: inventory.lowStockCount ? 'var(--rausch)' : 'var(--babu)' }}>{inventory.lowStockCount}</b></div>
            {inventory.lowStockItems.length ? inventory.lowStockItems.map(i => (
              <div className="report-row" key={i.id}><span>{i.name}</span><b>{i.currentQuantity} {i.unit} <small className="muted">/ reorder at {i.reorderLevel}</small></b></div>
            )) : <p className="muted small">Everything is above its reorder level.</p>}
          </div>}
        </div>
      )}
    </div>
  );
}

function StatusBars({ counts }: { counts: Record<string, number> }) {
  const total = Object.values(counts).reduce((a, b) => a + b, 0);
  return (
    <div className="status-bars">
      {Object.entries(counts).map(([status, n]) => (
        <div className="status-bar" key={status}>
          <span>{nice(status)}</span>
          <div><i style={{ width: total ? `${(n / total) * 100}%` : 0, background: TONE[status] ?? 'var(--gray-300)' }} /></div>
          <b>{n}</b>
        </div>
      ))}
    </div>
  );
}
