import { useEffect, useState } from 'react';
import toast from 'react-hot-toast';
import { useApp } from '../../context/AppContext';
import { Modal, SectionHeading } from '../../components/UI';
import { supplierApi, errorMessage, type SupplierDto, type SupplierInput } from '../../services/api';
const today = () => new Intl.DateTimeFormat('en-CA', {timeZone:'Asia/Colombo',year:'numeric',month:'2-digit',day:'2-digit'}).format(new Date());
const empty = (): SupplierInput => ({name:'',contactPerson:'',phone:'',email:'',address:'',suppliedProducts:'',joinedDate:today(),active:true});
export default function Suppliers() {
  const {user} = useApp();
  const canManage = user?.roles.some(r => ['ADMIN','MANAGER'].includes(r));
  const [suppliers,setSuppliers] = useState<SupplierDto[]>([]);
  const [search,setSearch] = useState('');
  const [status,setStatus] = useState('active');
  const [loading,setLoading] = useState(true);
  const [busy,setBusy] = useState(false);
  const [error,setError] = useState('');
  const [editing,setEditing] = useState<SupplierDto | 'new' | null>(null);
  const [removing,setRemoving] = useState<SupplierDto | null>(null);
  const [form,setForm] = useState<SupplierInput>(empty());
  const load = async () => {setLoading(true);setError('');try {setSuppliers(await supplierApi.list());} catch(e) {setError(errorMessage(e));}finally {setLoading(false);}};
  useEffect(() => {void load();},[]);
  const visible = suppliers.filter(s => (status === 'all' || s.active === (status === 'active')) && `${s.name} ${s.contactPerson} ${s.suppliedProducts ?? ''} ${s.email}`.toLowerCase().includes(search.toLowerCase()));
  const save = async () => {
    if(busy) return;
    if(!form.name.trim() || !form.contactPerson.trim() || !form.address.trim() || !form.suppliedProducts.trim() || !/^\d{10}$/.test(form.phone) || !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.email) || !form.joinedDate || form.joinedDate > today()) {setError('Complete all fields. Phone must have 10 digits; email and joined date must be valid.');return;}
    setBusy(true);setError('');
    try {const saved = editing === 'new' ? await supplierApi.create(form) : await supplierApi.update((editing as SupplierDto).id,form);setSuppliers(all => editing === 'new' ? [...all,saved] : all.map(s => s.id === saved.id ? saved : s));setEditing(null);toast.success('Supplier saved');}
    catch(e) {setError(errorMessage(e));toast.error(errorMessage(e));}finally {setBusy(false);}
  };
  return <div className="page-enter suppliers-page">
    <SectionHeading eyebrow="OUR SUPPLY PARTNERS" title="Suppliers" description="The people and products that keep our kitchen stocked." action={<div className="supplier-toolbar"><button className="button" onClick={load} disabled={loading || busy}>Refresh</button>{canManage && <button className="button primary" onClick={() => {setForm(empty());setEditing('new');setError('');}}>Add supplier</button>}</div>} />
    <div className="menu-stats"><div><strong>{suppliers.filter(s => s.active).length}</strong><span>Active suppliers</span></div><div><strong>{suppliers.filter(s => !s.active).length}</strong><span>Inactive suppliers</span></div></div>
    <div className="supplier-toolbar supplier-filters"><input aria-label="Search suppliers" placeholder="Search company, contact or products…" value={search} onChange={e => setSearch(e.target.value)} /><select aria-label="Supplier status" value={status} onChange={e => setStatus(e.target.value)}><option value="active">Active suppliers</option><option value="inactive">Inactive suppliers</option><option value="all">All suppliers</option></select></div>
    {error && <p className="error" role="alert">{error}</p>}
    {loading ? <p role="status">Loading suppliers…</p> : !visible.length ? <p>No suppliers match your search.</p> : <div className="supplier-grid">{visible.map(s => <article key={s.id} className="supplier-card"><div className="supplier-card-top"><span className="supplier-avatar">{s.name.slice(0,1)}</span><div><h3>{s.name}</h3><span className={`supplier-status ${s.active ? 'active' : ''}`}>{s.active ? 'Active' : 'Inactive'}</span></div></div><dl><dt>Supplies</dt><dd>{s.suppliedProducts || 'Not recorded'}</dd><dt>Contact person</dt><dd>{s.contactPerson || 'Not recorded'}</dd><dt>Phone</dt><dd>{s.phone ? <a href={`tel:${s.phone}`}>{s.phone}</a> : 'Not recorded'}</dd><dt>Email</dt><dd>{s.email ? <a href={`mailto:${s.email}`}>{s.email}</a> : 'Not recorded'}</dd><dt>Address</dt><dd>{s.address || 'Not recorded'}</dd><dt>Joined date</dt><dd>{s.joinedDate || 'Not recorded'}</dd><dt>Supplier ID</dt><dd>#{s.id}</dd><dt>Created</dt><dd>{new Date(s.createdAt).toLocaleString()}</dd><dt>Last updated</dt><dd>{new Date(s.updatedAt).toLocaleString()}</dd></dl>{canManage && <div className="supplier-card-actions"><button className="button" disabled={busy} onClick={() => {setForm({...s,suppliedProducts:s.suppliedProducts ?? '',joinedDate:s.joinedDate ?? ''});setEditing(s);setError('');}}>Edit</button>{s.active && <button className="button" disabled={busy} onClick={() => {setRemoving(s);setError('');}}>Remove</button>}</div>}</article>)}</div>}
    {editing && <Modal title={editing === 'new' ? 'Add supplier' : 'Edit supplier'} onClose={() => {if(!busy)setEditing(null);}}><form className="supplier-form" onSubmit={e => {e.preventDefault();void save();}}>
      <label>Company / supplier name<input required maxLength={150} value={form.name} onChange={e => setForm({...form,name:e.target.value})} /></label>
      <label>Contact person<input required maxLength={100} value={form.contactPerson} onChange={e => setForm({...form,contactPerson:e.target.value})} /></label>
      <label>Products supplied<textarea required maxLength={500} placeholder="Vegetables, flour, dairy products…" value={form.suppliedProducts} onChange={e => setForm({...form,suppliedProducts:e.target.value})} /></label>
      <label>Phone number<input required inputMode="numeric" pattern="[0-9]{10}" maxLength={10} placeholder="0771234567" value={form.phone} onChange={e => setForm({...form,phone:e.target.value})} /></label>
      <label>Email<input required type="email" maxLength={150} value={form.email} onChange={e => setForm({...form,email:e.target.value})} /></label>
      <label>Address<textarea required maxLength={500} value={form.address} onChange={e => setForm({...form,address:e.target.value})} /></label>
      <label>Joined date<input required type="date" max={today()} value={form.joinedDate ?? ''} onChange={e => setForm({...form,joinedDate:e.target.value})} /></label>
      <label>Status<select value={String(form.active)} onChange={e => setForm({...form,active:e.target.value === 'true'})}><option value="true">Active</option><option value="false">Inactive</option></select></label>
      <div className="supplier-record-meta"><p>Supplier ID: {editing === 'new' ? 'Assigned automatically' : editing.id}</p><p>Created: {editing === 'new' ? 'Set automatically when saved' : new Date(editing.createdAt).toLocaleString()}</p><p>Updated: Set automatically when saved</p></div>
      {error && <p className="error" role="alert">{error}</p>}<button className="button primary" disabled={busy}>{busy ? 'Saving…' : 'Save supplier'}</button>
    </form></Modal>}
    {removing && <Modal title="Remove supplier?" onClose={() => {if(!busy)setRemoving(null);}}><p>{removing.name} will be marked inactive. Their saved details will remain available under inactive suppliers.</p><button className="button primary" disabled={busy} onClick={async () => {setBusy(true);try {await supplierApi.remove(removing.id);setSuppliers(all => all.map(s => s.id === removing.id ? {...s,active:false} : s));setRemoving(null);toast.success('Supplier removed');}catch(e){setError(errorMessage(e));toast.error(errorMessage(e));}finally {setBusy(false);}}}>Remove supplier</button></Modal>}
  </div>;
}
