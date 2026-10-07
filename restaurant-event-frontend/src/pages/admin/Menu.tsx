import { useEffect, useState } from 'react';
import toast from 'react-hot-toast';
import { useApp } from '../../context/AppContext';
import { Modal, SectionHeading } from '../../components/UI';
import MenuPhoto from '../../components/MenuPhoto';
import { adminMenuApi, menuApi, errorMessage, type MenuCategoryDto, type MenuItemDto, type MenuItemInput } from '../../services/api';

const blank: MenuItemInput = { categoryId: 0, name: '', description: '', price: 0, imageUrl: '', preparationMinutes: 15, isAvailable: true };
export default function AdminMenu() {
  const { user } = useApp();
  const roles = user?.roles ?? [];
  const canManage = roles.some(r => ['ADMIN', 'MANAGER'].includes(r));
  const canView = canManage || roles.some(r => ['WAITER', 'KITCHEN_STAFF'].includes(r));
  const [items, setItems] = useState<MenuItemDto[]>([]);
  const [categories, setCategories] = useState<MenuCategoryDto[]>([]);
  const [search, setSearch] = useState('');
  const [category, setCategory] = useState('');
  const [editor, setEditor] = useState<number | 'new' | null>(null);
  const [form, setForm] = useState<MenuItemInput>(blank);
  const [remove, setRemove] = useState<MenuItemDto | null>(null);
  const [categoryOpen, setCategoryOpen] = useState(false);
  const [editingCategory, setEditingCategory] = useState<MenuCategoryDto | null>(null);
  const [categoryName, setCategoryName] = useState('');
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(true);
  const load = async () => {
    setError(''); setLoading(true);
    try { const [foods, groups] = await Promise.all([adminMenuApi.list(), menuApi.categories()]); setItems(foods); setCategories(groups); }
    catch (e) { setError(errorMessage(e)); }
    finally { setLoading(false); }
  };
  useEffect(() => { if (canView) void load(); }, [canView]);
  const run = async (action: () => Promise<void>) => {
    setBusy(true); setError('');
    try { await action(); } catch (e) { setError(errorMessage(e)); toast.error(errorMessage(e)); }
    finally { setBusy(false); }
  };
  if (!canView) return <p role="alert">You do not have permission to manage the menu.</p>;
  const visible = items.filter(i => (!category || i.categoryId === Number(category)) && `${i.name} ${i.description}`.toLowerCase().includes(search.toLowerCase()));
  const save = () => run(async () => {
    const payload = { ...form, name: form.name.trim(), imageUrl: (form.imageUrl ?? '').trim() };
    if (!payload.name || payload.name.length > 100 || !payload.categoryId || !Number.isFinite(payload.price) || payload.price <= 0 || payload.price > 9999999999.99 || Math.abs(payload.price * 100 - Math.round(payload.price * 100)) > 0.0001 || !Number.isInteger(payload.preparationMinutes) || payload.preparationMinutes < 1 || payload.preparationMinutes > 240) throw new Error('Enter a dish name, category, positive price with up to two decimals and preparation time between 1 and 240 minutes.');
    if (payload.imageUrl && !/^(https?:\/\/|\/)/i.test(payload.imageUrl)) throw new Error('Use an http(s) image URL or a local image path.');
    const saved = editor === 'new' ? await adminMenuApi.create(payload) : await adminMenuApi.update(editor as number, payload);
    setItems(all => editor === 'new' ? [...all, saved] : all.map(i => i.id === saved.id ? saved : i)); setEditor(null); toast.success('Menu item saved');
  });
  return <div className="page-enter admin-menu-page">
    <SectionHeading eyebrow="FRESH FROM OUR KITCHEN" title="Our menu" description={canManage ? 'Create dishes, keep prices current and manage what guests can order.' : 'View dishes and keep availability current for our guests.'} action={<div style={{display:'flex', gap:8, flexWrap:'wrap'}}><button className="button" onClick={load} disabled={loading || busy}>Refresh</button>{canManage && <><button className="button" onClick={() => {setEditingCategory(null);setCategoryName('');setCategoryOpen(true); setError('');}}>Add category</button><button className="button primary" onClick={() => {setForm({...blank, categoryId:categories[0]?.id ?? 0}); setEditor('new'); setError('');}}>Add food</button></>}</div>} />
    {error && <p className="error" role="alert">{error}</p>}
    {canManage && <section aria-label="Manage categories"><h2>Categories</h2><div className="button-row">{categories.map(c => <div key={c.id}><strong>{c.name}</strong> <button className="text-button" disabled={busy} onClick={() => {setEditingCategory(c);setCategoryName(c.name);setCategoryOpen(true);}}>Edit category</button> <button className="text-button" disabled={busy} onClick={() => {if(window.confirm(`Remove category ${c.name}? Move or remove its dishes first.`)) void run(async () => {await adminMenuApi.removeCategory(c.id);await load();});}}>Remove category</button></div>)}</div></section>}
    <div style={{display:'flex', gap:16, marginBottom:24, flexWrap:'wrap'}}><input className="input" aria-label="Search menu" placeholder="Search dishes…" value={search} onChange={e => setSearch(e.target.value)} /><select className="input" aria-label="Filter category" value={category} onChange={e => setCategory(e.target.value)}><option value="">All categories</option>{categories.map(c => <option key={c.id} value={c.id}>{c.name}</option>)}</select></div>
    <div className="menu-stats"><div><strong>{items.length}</strong><span>Total dishes</span></div><div><strong>{items.filter(i => i.isAvailable).length}</strong><span>Available to order</span></div><div><strong>{items.filter(i => !i.isAvailable).length}</strong><span>Unavailable</span></div></div>
    {loading ? <p role="status">Loading menu…</p> : visible.length === 0 ? <p>No dishes found. {canManage ? 'Add a food item to get started.' : 'Try a different search.'}</p> : <div style={{display:'grid',gridTemplateColumns:'repeat(auto-fill, minmax(260px, 1fr))',gap:24}}>{visible.map(i => <article className="admin-food-card" key={i.id} style={{background:'white',border:'1px solid var(--border)',borderRadius:20,overflow:'hidden'}}>
      <div style={{height:190,position:'relative',overflow:'hidden'}}><MenuPhoto name={i.name} imageUrl={i.imageUrl} /></div><div className="admin-food-card-body"><small>{categories.find(c => c.id === i.categoryId)?.name}</small><h3>{i.name}</h3><p className="admin-food-description">{i.description}</p><strong>LKR {i.price.toLocaleString()}</strong><p>{i.preparationMinutes} min preparation</p><div className="admin-food-actions"><span className={`supplier-status ${i.isAvailable ? 'active' : ''}`}>{i.isAvailable ? 'Available' : 'Unavailable'}</span><button className="button" disabled={busy} onClick={() => run(async () => {const updated = await adminMenuApi.availability(i.id, !i.isAvailable); setItems(all => all.map(x => x.id === i.id ? updated : x)); toast.success(updated.isAvailable ? 'Dish available' : 'Dish unavailable');})}>{i.isAvailable ? 'Mark unavailable' : 'Mark available'}</button>{canManage && <div style={{display:'flex',gap:8,marginTop:12}}><button className="button" disabled={busy} onClick={() => {setForm({...i}); setEditor(i.id); setError('');}}>Edit</button><button className="button" disabled={busy} onClick={() => {setRemove(i); setError('');}}>Remove</button></div>}</div></div>
    </article>)}</div>}
    {editor !== null && <Modal title={editor === 'new' ? 'Add food' : 'Edit food'} onClose={() => {if (!busy) setEditor(null);}}><form className="admin-menu-form" onSubmit={e => {e.preventDefault(); void save();}} style={{display:'grid',gap:16}}>
      <label>Dish name<input className="input" required maxLength={100} value={form.name} onChange={e => setForm({...form,name:e.target.value})} /></label>
      <label>Category<select className="input" required value={form.categoryId || ''} onChange={e => setForm({...form,categoryId:Number(e.target.value)})}><option value="">Choose category</option>{categories.map(c => <option key={c.id} value={c.id}>{c.name}</option>)}</select></label>
      <label>Description<textarea className="input" maxLength={1000} value={form.description ?? ''} onChange={e => setForm({...form,description:e.target.value})} /></label>
      <label>Price (LKR)<input className="input" type="number" required min="0.01" max="9999999999.99" step="0.01" value={form.price} onChange={e => setForm({...form,price:Number(e.target.value)})} /></label>
      <label>Preparation time (minutes)<input className="input" type="number" required min="1" max="240" step="1" value={form.preparationMinutes} onChange={e => setForm({...form,preparationMinutes:Number(e.target.value)})} /></label>
      <label>Photo URL<input className="input" maxLength={500} placeholder="https://… or /images/…" value={form.imageUrl ?? ''} onChange={e => setForm({...form,imageUrl:e.target.value})} /></label>
      <label><input type="checkbox" checked={form.isAvailable} onChange={e => setForm({...form,isAvailable:e.target.checked})} /> Available to order</label>
      {error && <p className="error" role="alert">{error}</p>}<button className="button primary" disabled={busy}>{busy ? 'Saving…' : 'Save food'}</button>
    </form></Modal>}
    {remove && <Modal title="Remove food?" onClose={() => {if (!busy) setRemove(null);}}><p>Remove {remove.name} from the menu? Existing order records are kept.</p><button className="button primary" disabled={busy} onClick={() => run(async () => {await adminMenuApi.remove(remove.id); setItems(all => all.filter(i => i.id !== remove.id)); setRemove(null); toast.success('Food removed');})}>Remove food</button></Modal>}
    {categoryOpen && <Modal title={editingCategory ? "Edit category" : "Add category"} onClose={() => {if (!busy) setCategoryOpen(false);}}><form className="admin-menu-form" onSubmit={e => {e.preventDefault(); void run(async () => {if(editingCategory) {
 const updated = await adminMenuApi.updateCategory(editingCategory.id,{name:categoryName.trim(),description:editingCategory.description ?? '',displayOrder:editingCategory.displayOrder});
 setCategories(all => all.map(c => c.id===updated.id?updated:c));
 } else {const created=await adminMenuApi.createCategory(categoryName.trim());setCategories(all => [...all,created]);} setCategoryName(''); setCategoryOpen(false); toast.success('Category saved');});}}><label>Category name<input className="input" required maxLength={100} value={categoryName} onChange={e => setCategoryName(e.target.value)} /></label><button className="button primary" disabled={busy || !categoryName.trim()}>Save category</button></form></Modal>}
  </div>;
}
