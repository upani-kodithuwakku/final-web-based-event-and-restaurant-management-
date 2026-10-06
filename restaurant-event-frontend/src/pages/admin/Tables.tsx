import { useEffect, useState } from 'react';
import { PlusIcon, PencilSquareIcon, UsersIcon, SunIcon, BuildingStorefrontIcon, HomeIcon, SparklesIcon, CloudIcon, TableCellsIcon } from '@heroicons/react/24/outline';
import { useApp } from '../../context/AppContext';
import { Empty, Modal, SectionHeading } from '../../components/UI';
import { errorMessage, reservationApi } from '../../services/api';
import type { Table } from '../../types';

const LOCATIONS = ['GARDEN', 'WINDOW', 'INDOOR', 'OUTDOOR', 'PRIVATE'];
const STATUSES  = ['AVAILABLE', 'RESERVED', 'OCCUPIED', 'OUT_OF_SERVICE'];
// Icon per seating area for the table cards.
const AREA_ICONS: Record<string, React.ElementType> = {
  GARDEN: SunIcon, WINDOW: BuildingStorefrontIcon, INDOOR: HomeIcon, OUTDOOR: CloudIcon, PRIVATE: SparklesIcon,
};
const label = (value: string) => value.charAt(0) + value.slice(1).toLowerCase().replace(/_/g, ' ');

type FormValues = { tableNumber: string; capacity: number; location: string; displayName?: string; description?: string; imageUrl?: string };
const EMPTY: FormValues = { tableNumber: '', capacity: 4, location: 'INDOOR' };

export default function AdminTables() {
  const { tables, setTables } = useApp();
  const [modal, setModal] = useState<Table | null | 'new'>(null);
  const [form, setForm] = useState<FormValues>(EMPTY);
  const [busy, setBusy] = useState(false);
  const [err, setErr] = useState('');
  const [statusBusy, setStatusBusy] = useState<number | null>(null);
  const [filter, setFilter] = useState('');

  useEffect(() => {
    reservationApi.tables().then(setTables).catch(() => {});
  }, []);

  const openNew = () => { setForm(EMPTY); setModal('new'); setErr(''); };
  const openEdit = (t: Table) => { setForm({ tableNumber: t.tableNumber, capacity: t.capacity, location: t.location, displayName:t.displayName, description:t.description, imageUrl:t.imageUrl }); setModal(t); setErr(''); };

  const save = async () => {
    if(!form.tableNumber.trim() || !Number.isInteger(form.capacity) || form.capacity < 1 || form.capacity > 200) {setErr('Enter a table number and a whole-number capacity between 1 and 200.');return;}
    setBusy(true); setErr('');
    try {
      if (modal === 'new') {
        const created = await reservationApi.createTable(form);
        setTables(all => [created, ...all]);
      } else if (modal) {
        const updated = await reservationApi.updateTable((modal as Table).id, form);
        setTables(all => all.map(t => t.id === updated.id ? updated : t));
      }
      setModal(null);
    } catch (e) { setErr(errorMessage(e)); }
    finally { setBusy(false); }
  };

  const changeStatus = async (t: Table, status: string) => {
    setStatusBusy(t.id); setErr('');
    try {
      const updated = await reservationApi.tableStatus(t.id, status);
      setTables(all => all.map(x => x.id === t.id ? updated : x));
    } catch (e) { setErr(errorMessage(e)); }
    finally { setStatusBusy(null); }
  };

  const sorted = [...tables].sort((a, b) => a.tableNumber.localeCompare(b.tableNumber, undefined, { numeric: true }));
  const visible = filter ? sorted.filter(t => t.currentStatus === filter) : sorted;
  const seats = tables.reduce((sum, t) => sum + t.capacity, 0);

  return (
    <div className="page-enter">
      <SectionHeading
        eyebrow="TABLE MANAGEMENT"
        title="Restaurant tables"
        description="Manage table configuration, capacity, and live status."
        action={<button className="button primary" onClick={openNew}><PlusIcon style={{ width: 16, height: 16 }} /> Add table</button>}
      />

      {err && <p className="error">{err}</p>}

      <div className="table-stats">
        <button className={`table-stat total${filter === '' ? ' selected' : ''}`} onClick={() => setFilter('')}>
          <span>All tables</span><strong>{tables.length}</strong><small>{seats} seats</small>
        </button>
        {STATUSES.map(st => (
          <button key={st} className={`table-stat ${st.toLowerCase().replace(/_/g, '-')}${filter === st ? ' selected' : ''}`}
            onClick={() => setFilter(filter === st ? '' : st)} aria-pressed={filter === st}>
            <span><i />{label(st)}</span><strong>{tables.filter(t => t.currentStatus === st).length}</strong>
          </button>
        ))}
      </div>

      {!visible.length ? <Empty title={filter ? `No ${label(filter).toLowerCase()} tables` : 'No tables yet'}>
        {filter ? <button className="text-button" onClick={() => setFilter('')}>Show all tables</button> : <p>Add your first table to start taking reservations.</p>}
      </Empty> : (
        <div className="table-grid">
          {visible.map(t => {
            const Icon = AREA_ICONS[t.location.toUpperCase()] ?? TableCellsIcon;
            const statusClass = t.currentStatus.toLowerCase().replace(/_/g, '-');
            return (
              <article key={t.id} className={`table-card ${statusClass}${t.isActive ? '' : ' inactive'}`}>
                <header>
                  <span className={`table-area ${t.location.toLowerCase()}`}><Icon /></span>
                  <div>
                    <h3>{t.tableNumber}</h3>
                    <p>{label(t.location)}{t.isActive ? '' : ' · Inactive'}</p>
                  </div>
                  <button className="icon-button" onClick={() => openEdit(t)} title={`Edit ${t.tableNumber}`} aria-label={`Edit ${t.tableNumber}`}>
                    <PencilSquareIcon style={{ width: 18, height: 18 }} />
                  </button>
                </header>
                <div className="table-seats" aria-label={`${t.capacity} seats`}>
                  <UsersIcon /><b>{t.capacity}</b> {t.capacity === 1 ? 'guest' : 'guests'}
                  <span className="seat-dots" aria-hidden="true">{Array.from({ length: Math.min(t.capacity, 12) }, (_, i) => <i key={i} />)}</span>
                </div>
                {t.isActive && <button className="text-button" onClick={async () => {if(!window.confirm(`Remove table ${t.tableNumber}? Tables with upcoming reservations cannot be removed.`)) return;try {await reservationApi.deleteTable(t.id);setTables(all => all.map(x => x.id === t.id ? {...x,isActive:false} : x));} catch(e) {setErr(errorMessage(e));}}}>Remove table</button>}
                <label className={`status-select ${statusClass}`}>
                  <span className="sr-only">Status of {t.tableNumber}</span>
                  <select value={t.currentStatus} disabled={statusBusy === t.id} onChange={e => changeStatus(t, e.target.value)}>
                    {STATUSES.map(st => <option key={st} value={st}>{label(st)}</option>)}
                  </select>
                </label>
              </article>
            );
          })}
        </div>
      )}

      {modal !== null && (
        <Modal title={modal === 'new' ? 'Add a new table' : `Edit ${(modal as Table).tableNumber}`} onClose={() => setModal(null)}>
          <div className="input-group">
            <label>Space name<input maxLength={100} value={form.displayName || ''} onChange={e => setForm({...form,displayName:e.target.value})} placeholder="e.g. The Jasmine Courtyard" /></label>
            <label>Description<input maxLength={255} value={form.description || ''} onChange={e => setForm({...form,description:e.target.value})} /></label>
            <label>Photo URL<input type="url" maxLength={255} value={form.imageUrl || ''} onChange={e => setForm({...form,imageUrl:e.target.value})} placeholder="https://…" /></label>
            <label>Table number<input value={form.tableNumber} onChange={e => setForm({ ...form, tableNumber: e.target.value })} placeholder="e.g. T11" /></label>
            <div className="form-row">
              <label>Capacity (guests)<input type="number" min={1} max={30} value={form.capacity} onChange={e => setForm({ ...form, capacity: Number(e.target.value) })} /></label>
              <label>Location / seating area
                <select value={form.location} onChange={e => setForm({ ...form, location: e.target.value })}>
                  {LOCATIONS.map(l => <option key={l} value={l}>{l.charAt(0) + l.slice(1).toLowerCase()}</option>)}
                </select>
              </label>
            </div>
            {err && <p className="error">{err}</p>}
            <button className="button primary full" disabled={busy || !form.tableNumber} onClick={save}>
              {busy ? 'Saving…' : modal === 'new' ? 'Add table' : 'Save changes'}
            </button>
          </div>
        </Modal>
      )}
    </div>
  );
}
