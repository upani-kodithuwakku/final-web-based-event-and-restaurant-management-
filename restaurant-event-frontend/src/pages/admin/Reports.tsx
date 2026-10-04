import { useState, useEffect } from 'react';
import { format } from 'date-fns';
import { ArrowDownTrayIcon } from '@heroicons/react/24/outline';
import { SectionHeading } from '../../components/UI';
import { api, errorMessage } from '../../services/api';

type DashboardData = {
  date: string;
  todayReservations: number;
  totalTables: number;
  availableTables: number;
  occupiedTables: number;
};

export default function AdminReports() {
  const [dash, setDash] = useState<DashboardData | null>(null);
  const [loading, setLoading] = useState(true);
  const [err, setErr] = useState('');

  useEffect(() => {
    api.get<DashboardData>('/admin/reports/dashboard')
      .then(r => setDash(r.data))
      .catch(e => setErr(errorMessage(e)))
      .finally(() => setLoading(false));
  }, []);

  return (
    <div className="page-enter">
      <SectionHeading
        eyebrow="ANALYTICS"
        title="Reports & overview"
        description={`System snapshot as of ${format(new Date(), 'EEEE, d MMMM yyyy')}.`}
        action={<button className="button" disabled><ArrowDownTrayIcon style={{ width: 14, height: 14 }} /> Export (coming soon)</button>}
      />

      {err && <p className="error">{err}</p>}

      {loading ? <div className="skeleton" style={{ height: 200 }} /> : dash && (
        <div className="report-grid">
          <div className="report-card">
            <h3>Today's reservations</h3>
            <div className="report-row"><span>Date</span><b>{dash.date}</b></div>
            <div className="report-row"><span>Active today</span><b style={{ color: 'var(--babu)' }}>{dash.todayReservations}</b></div>
          </div>

          <div className="report-card">
            <h3>Table utilisation</h3>
            <div className="report-row"><span>Total tables</span><b>{dash.totalTables}</b></div>
            <div className="report-row"><span>Available now</span><b style={{ color: 'var(--babu)' }}>{dash.availableTables}</b></div>
            <div className="report-row"><span>Occupied now</span><b style={{ color: 'var(--rausch)' }}>{dash.occupiedTables}</b></div>
            <div className="report-row">
              <span>Utilisation rate</span>
              <b>{dash.totalTables ? Math.round((dash.occupiedTables / dash.totalTables) * 100) : 0}%</b>
            </div>
          </div>

          <div className="report-card" style={{ gridColumn: '1 / -1' }}>
            <h3>More reports coming soon</h3>
            <p className="muted small" style={{ marginTop: 8 }}>Revenue breakdown, reservation trends, inventory usage, and event analytics will appear here as more data is collected.</p>
          </div>
        </div>
      )}
    </div>
  );
}
