import { useState, useEffect } from 'react';
import { ShoppingBagIcon, PlusIcon, MinusIcon, MagnifyingGlassIcon } from '@heroicons/react/24/outline';
import { SectionHeading, Empty, Modal } from '../components/UI';
import { menuApi, type MenuItemDto, errorMessage } from '../services/api';
import { money } from '../data';

export default function Menu() {
  const [items, setItems] = useState<MenuItemDto[]>([]);
  const [loading, setLoading] = useState(true);
  const [err, setErr] = useState('');
  const [query, setQuery] = useState('');
  const [cart, setCart] = useState<Record<number, number>>({});
  const [open, setOpen] = useState(false);

  useEffect(() => {
    menuApi.items()
      .then(setItems)
      .catch(e => setErr(errorMessage(e)))
      .finally(() => setLoading(false));
  }, []);

  const visible = items.filter(i => i.isAvailable && i.isActive).filter(i => {
    const matchQ = !query || i.name.toLowerCase().includes(query.toLowerCase()) || i.description.toLowerCase().includes(query.toLowerCase());
    return matchQ;
  });

  const quantity = (id: number, delta: number) => setCart(c => {
    const next = (c[id] || 0) + delta;
    if (next <= 0) { const { [id]: _, ...rest } = c; return rest; }
    return { ...c, [id]: next };
  });

  const count = Object.values(cart).reduce((a, b) => a + b, 0);
  const entries = items.filter(i => cart[i.id]);
  const total = entries.reduce((s, i) => s + i.price * cart[i.id], 0);

  return (
    <div className="page-container page-enter">
      <SectionHeading
        eyebrow="MADE WITH LOVE. SHARED WITH YOU."
        title="A little something delicious"
        description="Fresh ingredients. Familiar favorites. Something to bring everyone together."
        action={
          <button className="button primary" onClick={() => setOpen(true)}>
            <ShoppingBagIcon /> Your bag ({count})
          </button>
        }
      />

      {err && <p className="error">{err}</p>}

      <div className="menu-filters">
        <label className="inline-search">
          <MagnifyingGlassIcon />
          <input aria-label="Search menu" placeholder="What are you craving?" value={query} onChange={e => setQuery(e.target.value)} />
        </label>
      </div>

      {loading ? (
        <div className="menu-grid">{Array.from({ length: 6 }, (_, i) => <div className="skeleton" key={i} style={{ height: 260 }} />)}</div>
      ) : (
        <div className="menu-grid">
          {visible.map(item => (
            <article className="food-card" key={item.id}>
              <div className="food-photo">
                <img src={item.imageUrl || `https://source.unsplash.com/400x300/?food,${item.name}`} alt={item.name} onError={e => { (e.target as HTMLImageElement).src = 'https://images.unsplash.com/photo-1546069901-ba9599a7e63c?w=400&h=300&fit=crop'; }} />
              </div>
              <div className="food-info">
                <h3>{item.name}</h3>
                <p>{item.description}</p>
                <div className="row-between">
                  <b>{money(item.price)}</b>
                  <div className="quantity">
                    {cart[item.id] > 0 && (
                      <>
                        <button aria-label={`Remove one ${item.name}`} onClick={() => quantity(item.id, -1)}><MinusIcon /></button>
                        <span>{cart[item.id]}</span>
                      </>
                    )}
                    <button aria-label={`Add ${item.name}`} onClick={() => quantity(item.id, 1)}><PlusIcon /></button>
                  </div>
                </div>
              </div>
            </article>
          ))}
        </div>
      )}

      {!loading && !visible.length && (
        <Empty title="Nothing on the menu matches that search">
          <button className="text-button" onClick={() => setQuery('')}>Clear search</button>
        </Empty>
      )}

      {open && (
        <Modal title="Your delicious little lineup" onClose={() => setOpen(false)}>
          {entries.length ? (
            <>
              {entries.map(i => (
                <div className="cart-row" key={i.id}>
                  <div><b>{i.name}</b><p>{money(i.price)}</p></div>
                  <div className="quantity">
                    <button aria-label={`Remove one ${i.name}`} onClick={() => quantity(i.id, -1)}><MinusIcon /></button>
                    <span>{cart[i.id]}</span>
                    <button aria-label={`Add one ${i.name}`} onClick={() => quantity(i.id, 1)}><PlusIcon /></button>
                  </div>
                </div>
              ))}
              <div className="totals">
                <span>Subtotal</span><b>{money(total)}</b>
                <span>Service charge (10%)</span><b>{money(total * .1)}</b>
                <strong>Total</strong><strong>{money(total * 1.1)}</strong>
              </div>
              <p className="muted small" style={{ marginTop: 12 }}>Sign in and proceed to a table reservation to place your order with a waiter.</p>
            </>
          ) : <Empty title="Your bag is feeling a little empty"><p>Add something delicious from the menu.</p></Empty>}
        </Modal>
      )}
    </div>
  );
}
