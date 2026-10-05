import { useState, useEffect } from 'react';
import { PlusIcon, ExclamationTriangleIcon } from '@heroicons/react/24/outline';
import { Modal, SectionHeading } from '../../components/UI';
import { inventoryApi, type InventoryItemDto, errorMessage } from '../../services/api';

export default function AdminInventory() {
  const [items, setItems] = useState<InventoryItemDto[]>([]);
  const [loading, setLoading] = useState(true);
  const [modal, setModal] = useState<InventoryItemDto | 'new' | null>(null);
  const [form, setForm] = useState({ name: '', unit: 'kg', currentQuantity: 0, reorderLevel: 5 });
  const [adjModal, setAdjModal] = useState<InventoryItemDto | null>(null);
  const [adjDelta, setAdjDelta] = useState('');
  const [adjNote, setAdjNote] = useState('');
  const [busy, setBusy] = useState(false);
  const [err, setErr] = useState('');

  const load = async () => {
    setLoading(true);
    try { setItems(await inventoryApi.list()); }
    catch { setErr('Failed to load inventory.'); }
    finally { setLoading(false); }
  };

  useEffect(() => { void load(); }, []);

  const lowStock = items.filter(i => i.isActive && i.lowStock);

  const save = async () => {
    setBusy(true); setErr('');
    try {
      if (modal === 'new') {
        const created = await inventoryApi.create(form);
        setItems(all => [created, ...all]);
      } else if (modal) {
        const updated = await inventoryApi.update((modal as InventoryItemDto).id, form);
        setItems(all => all.map(i => i.id === updated.id ? updated : i));
      }
      setModal(null);
    } catch (e) { setErr(errorMessage(e)); }
    finally { setBusy(false); }
  };

  const applyAdj = async () => {
    if (!adjModal) return;
    setBusy(true); setErr('');
    try {
      const updated = await inventoryApi.adjust(adjModal.id, parseFloat(adjDelta), adjNote || 'Manual adjustment');
      setItems(all => all.map(i => i.id === updated.id ? updated : i));
      setAdjModal(null); setAdjDelta(''); setAdjNote('');
    } catch (e) { setErr(errorMessage(e)); }
    finally { setBusy(false); }
  };

  const pct = (item: InventoryItemDto) => Math.min(100, Math.round((Number(item.currentQuantity) / (Number(item.reorderLevel) * 3)) * 100));

  return (
    <div className="page-enter">
      <SectionHeading
        eyebrow="INVENTORY"
        title="Stock management"
        description="Monitor stock levels, set reorder thresholds, and track movements."
        action={
          <button className="button primary" onClick={() => { setForm({ name: '', unit: 'kg', currentQuantity: 0, reorderLevel: 5 }); setModal('new'); setErr(''); }}>
            <PlusIcon style={{ width: 16, height: 16 }} /> Add item
          </button>
        }
      />

      {lowStock.length > 0 && (
        <div className="alert-banner">
          <ExclamationTriangleIcon />
          {lowStock.length} item{lowStock.length > 1 ? 's' : ''} at or below reorder level:{' '}
          {lowStock.map(i => i.name).join(', ')}
        </div>
      )}

      {err && <p className="error">{err}</p>}

      {loading ? (
        <div className="skeleton" style={{ height: 200 }} />
      ) : (
        <div className="inventory-grid">
          {items.filter(i => i.isActive).map(item => (
            <div key={item.id} className={`inventory-card${item.lowStock ? ' low-stock' : ''}`}>
              <div className="row-between">
                <h3>{item.name}</h3>
                {item.lowStock && <ExclamationTriangleIcon style={{ width: 16, height: 16, color: 'var(--arches)' }} />}
              </div>
              <p className="small muted">{item.unit} · reorder at {item.reorderLevel}</p>
              <div className="stock-bar">
                <div className="stock-fill" style={{ width: `${pct(item)}%` }} />
              </div>
              <div className="row-between">
                <b style={{ fontSize: 20, fontWeight: 800 }}>{Number(item.currentQuantity).toFixed(2)}</b>
                <span className="small muted">{item.unit}</span>
              </div>
              <div className="row-between" style={{ marginTop: 4 }}>
                <button
                  className="button" style={{ padding: '5px 12px', fontSize: 13 }}
                  onClick={() => { setForm({ name: item.name, unit: item.unit, currentQuantity: Number(item.currentQuantity), reorderLevel: Number(item.reorderLevel) }); setModal(item); setErr(''); }}
                >
                  Edit
                </button>
                <button
                  className="button primary" style={{ padding: '5px 14px', fontSize: 13 }}
                  onClick={() => { setAdjModal(item); setAdjDelta(''); setAdjNote(''); }}
                >
                  Adjust stock
                </button>
              </div>
            </div>
          ))}
        </div>
      )}

      {modal !== null && (
        <Modal title={modal === 'new' ? 'Add inventory item' : `Edit ${(modal as InventoryItemDto).name}`} onClose={() => setModal(null)}>
          <div className="input-group">
            <label>Item name<input value={form.name} onChange={e => setForm({ ...form, name: e.target.value })} placeholder="e.g. Olive oil" /></label>
            <div className="form-row">
              <label>Unit
                <select value={form.unit} onChange={e => setForm({ ...form, unit: e.target.value })}>
                  {['kg', 'g', 'litre', 'ml', 'bottle', 'bunch', 'unit', 'pack', 'dozen'].map(u => <option key={u}>{u}</option>)}
                </select>
              </label>
              <label>Current quantity<input type="number" min={0} step="0.001" value={form.currentQuantity} onChange={e => setForm({ ...form, currentQuantity: Number(e.target.value) })} /></label>
              <label>Reorder level<input type="number" min={0} step="0.001" value={form.reorderLevel} onChange={e => setForm({ ...form, reorderLevel: Number(e.target.value) })} /></label>
            </div>
            {err && <p className="error">{err}</p>}
            <button className="button primary full" disabled={busy || !form.name} onClick={save}>
              {busy ? 'Saving…' : modal === 'new' ? 'Add item' : 'Save changes'}
            </button>
          </div>
        </Modal>
      )}

      {adjModal && (
        <Modal title={`Adjust stock — ${adjModal.name}`} onClose={() => setAdjModal(null)}>
          <div className="input-group">
            <p className="muted small">Current: <b>{Number(adjModal.currentQuantity).toFixed(2)} {adjModal.unit}</b></p>
            <label>
              Change (positive to add, negative to remove)
              <input type="number" step="0.001" value={adjDelta} onChange={e => setAdjDelta(e.target.value)} placeholder="e.g. 10 or -2.5" />
            </label>
            <label>Note (optional)<input value={adjNote} onChange={e => setAdjNote(e.target.value)} placeholder="e.g. Weekly delivery received" /></label>
            {err && <p className="error">{err}</p>}
            <button className="button primary full" disabled={busy || !adjDelta} onClick={applyAdj}>
              {busy ? 'Saving…' : 'Apply adjustment'}
            </button>
          </div>
        </Modal>
      )}
    </div>
  );
}
