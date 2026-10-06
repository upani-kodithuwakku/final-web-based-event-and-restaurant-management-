import { useEffect, useState } from 'react';
import { api, errorMessage, type EventHallDto, type EventPackageDto } from '../services/api';
import { normalizeEvent } from '../services/eventCatalog';
import { Modal } from './UI';
import { money } from '../data';
const blank = { name: '', description: '', location: '', capacity: 30, eventType: 'BIRTHDAY', basePrice: 12000, minimumGuests: 2, maximumGuests: 30 };
export default function EventCatalogManager() {
  const [halls, setHalls] = useState<EventHallDto[]>([]);
  const [packages, setPackages] = useState<EventPackageDto[]>([]);
  const [kind, setKind] = useState<'halls' | 'packages'>('packages');
  const [form, setForm] = useState(blank);
  const [editing, setEditing] = useState<number | null | undefined>();
  const [error, setError] = useState('');
  const [busy, setBusy] = useState(false);
  const load = async () => { try {
    const [h,p] = await Promise.all([api.get<EventHallDto[]>('/event-coordinator/catalog/halls'), api.get<EventPackageDto[]>('/event-coordinator/catalog/packages')]);
    setHalls(h.data.map(normalizeEvent)); setPackages(p.data.map(normalizeEvent));
  } catch(e) { setError(errorMessage(e)); } };
  useEffect(() => { void load(); }, []);
  const remove = async (id: number) => {
    if (!window.confirm(`Remove this ${kind === 'halls' ? 'hall' : 'package'} from the catalogue? Booking history will be kept.`)) return;
    setBusy(true); setError(''); try { await api.delete(`/event-coordinator/catalog/${kind}/${id}`); await load(); } catch(e) { setError(errorMessage(e)); } finally { setBusy(false); }
  };
  return <section className="catalog-management">
    <div className="row-between"><h2>Celebration catalogue</h2><button className="button primary" onClick={() => { setForm(blank);setEditing(null);setError(''); }}>Add {kind === 'halls' ? 'hall' : 'package'}</button></div>
    <div className="tabs"><button className={kind === 'packages' ? 'active' : ''} onClick={() => setKind('packages')}>Packages</button><button className={kind === 'halls' ? 'active' : ''} onClick={() => setKind('halls')}>Halls</button></div>
    {error && <p role="alert" className="error">{error}</p>}
    <div className="catalog-admin-list">{(kind === 'halls' ? halls : packages).map(row => <article key={row.id} className="catalog-admin-row"><div><strong>{row.name}</strong><p className="muted small">{'capacity' in row ? `${row.capacity} guests · ${row.location}` : `${row.minimumGuests}–${row.maximumGuests} guests · ${money(row.basePrice)}`}</p></div><div className="button-row"><button className="button" disabled={busy} onClick={() => {setForm({...blank,...row});setEditing(row.id);setError('');}}>Edit</button><button className="text-button" disabled={busy} onClick={() => void remove(row.id)}>Remove</button></div></article>)}</div>
    {editing !== undefined && <Modal title={`${editing === null ? 'Add' : 'Edit'} ${kind === 'halls' ? 'hall' : 'package'}`} onClose={() => { if(!busy) setEditing(undefined); }}>
      <form onSubmit={async e => {
        e.preventDefault(); if(kind === 'packages' && form.maximumGuests < form.minimumGuests) { setError('Maximum guests must be at least minimum guests.'); return; }
        setBusy(true);setError('');try { const path=`/event-coordinator/catalog/${kind}`; if(editing === null) await api.post(path,form); else await api.put(`${path}/${editing}`,form); await load();setEditing(undefined); } catch(e) { setError(errorMessage(e)); } finally {setBusy(false);}
      }}>
        <label>Name<input required maxLength={100} value={form.name} onChange={e => setForm({...form,name:e.target.value})} /></label>
        <label>Description<textarea maxLength={1000} value={form.description || ''} onChange={e => setForm({...form,description:e.target.value})} /></label>
        {kind === 'halls' ? <><label>Location<input required maxLength={100} value={form.location} onChange={e => setForm({...form,location:e.target.value})} /></label><label>Capacity<input required type="number" min={1} max={1000} value={form.capacity} onChange={e => setForm({...form,capacity:Number(e.target.value)})} /></label></> : <>
          <label>Occasion<select value={form.eventType} onChange={e => setForm({...form,eventType:e.target.value})}>{['BIRTHDAY','ANNIVERSARY','WEDDING','ENGAGEMENT','BABY_SHOWER','GRADUATION','FAMILY','CORPORATE','RECEPTION','FAREWELL','CHRISTMAS','NEW_YEAR','RETIREMENT','NAMING','TEAM_BUILDING','GALA'].map(t => <option key={t}>{t}</option>)}</select></label>
          <label>Package price (LKR)<input required type="number" min="0.01" step="0.01" value={form.basePrice} onChange={e => setForm({...form,basePrice:Number(e.target.value)})} /></label>
          <div className="form-grid"><label>Minimum guests<input required type="number" min={1} max={1000} value={form.minimumGuests} onChange={e => setForm({...form,minimumGuests:Number(e.target.value)})} /></label><label>Maximum guests<input required type="number" min={form.minimumGuests} max={1000} value={form.maximumGuests} onChange={e => setForm({...form,maximumGuests:Number(e.target.value)})} /></label></div>
        </>}
        {error && <p role="alert" className="error">{error}</p>}<button className="button primary full" disabled={busy}>{busy ? 'Saving…' : 'Save'}</button>
      </form>
    </Modal>}
  </section>;
}
