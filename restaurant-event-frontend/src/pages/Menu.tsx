import MenuPhoto from '../components/MenuPhoto';
import { useState, useEffect, useRef } from 'react';
import { Link } from 'react-router-dom';
import { ShoppingBagIcon, PlusIcon, MinusIcon, MagnifyingGlassIcon, InformationCircleIcon, CheckCircleIcon, CreditCardIcon, BuildingStorefrontIcon, ChevronRightIcon, ArrowLeftIcon } from '@heroicons/react/24/outline';
import { SectionHeading, Empty, Modal } from '../components/UI';
import { menuApi, orderApi, foodRequestApi, customerPaymentApi, type MenuItemDto, type OrderDto, type FoodRequestDto, errorMessage } from '../services/api';
import PaymentGateway from '../components/PaymentGateway';
import { changeQuantity, readBag, type Bag } from '../services/bag';
import { useApp } from '../context/AppContext';
import { money } from '../data';

export default function Menu() {
  const { user } = useApp();
  // Keep each customer's bag separate when the account changes.
  return <MenuContent key={user?.userId ?? 'guest'} />;
}

function MenuContent() {
  const { user } = useApp();
  const canOrder = !!user?.roles.includes('CUSTOMER');
  const bagKey = `gather-bag-${user?.userId}`;
  const [items, setItems] = useState<MenuItemDto[]>([]);
  const [loading, setLoading] = useState(true);
  const [err, setErr] = useState('');
  const [query, setQuery] = useState('');
  const [cart, setCart] = useState<Bag>(() => canOrder ? readBag(bagKey) : {});
  const [undo, setUndo] = useState<Bag | null>(null);
  const [open, setOpen] = useState(false);
  const [requestOpen, setRequestOpen] = useState(false);
  const [message, setMessage] = useState('');
  const [requestItem, setRequestItem] = useState('');
  const [note, setNote] = useState('');
  const [orderType, setOrderType] = useState('DINE_IN');
  const [busy, setBusy] = useState(false);
  const submitting = useRef(false);
  const [actionError, setActionError] = useState('');
  const [success, setSuccess] = useState('');
  const [orders, setOrders] = useState<OrderDto[]>([]);
  const [requests, setRequests] = useState<FoodRequestDto[]>([]);
  const [historyError, setHistoryError] = useState('');
  // Checkout: the bag asks how to pay, then card payments open the gateway.
  const [step, setStep] = useState<'bag' | 'choose'>('bag');
  const [gatewayAmount, setGatewayAmount] = useState<number | null>(null);
  const placedOrder = useRef<OrderDto | null>(null);

  const loadMenu = async () => {
    setLoading(true); setErr('');
    try { setItems(await menuApi.items()); }
    catch (e) { setErr(errorMessage(e)); }
    finally { setLoading(false); }
  };
  const loadHistory = async () => {
    if (!canOrder) return;
    try {
      const [nextOrders, nextRequests] = await Promise.all([orderApi.mine(), foodRequestApi.mine()]);
      setOrders(nextOrders); setRequests(nextRequests); setHistoryError('');
    } catch (e) { setHistoryError(errorMessage(e)); }
  };
  useEffect(() => { void loadMenu(); }, []);
  useEffect(() => {
    void loadHistory();
    if (!canOrder) return;
    const timer = window.setInterval(() => void loadHistory(), 20000);
    return () => window.clearInterval(timer);
  }, []);
  useEffect(() => {
    if (canOrder) { try { sessionStorage.setItem(bagKey, JSON.stringify(cart)); } catch { /* Keep using the in-memory bag. */ } }
  }, [cart, canOrder, bagKey]);

  const visible = items.filter(i => i.isActive).filter(i =>
    !query || `${i.name} ${i.description ?? ''}`.toLowerCase().includes(query.trim().toLowerCase()));
  const quantity = (id: number, delta: number) => {
    if (!canOrder || busy) return;
    setUndo(cart); setCart(changeQuantity(cart, id, delta));
  };
  const entries = Object.entries(cart).map(([id, count]) => ({ id: Number(id), count, item: items.find(i => i.id === Number(id)) }));
  const count = Object.values(cart).reduce((a, b) => a + b, 0);
  const total = entries.reduce((s, entry) => s + (entry.item?.price ?? 0) * entry.count, 0);
  const invalidBag = entries.some(e => !e.item?.isAvailable || !e.item?.isActive);

  const estimatedTotal = Math.round(total * 1.1 * 100) / 100;
  const createOrder = async () => {
    try {
      const order = await orderApi.create({ orderType, specialNote: note.trim(),
        items: entries.map(e => ({ menuItemId: e.id, quantity: e.count })) });
      placedOrder.current = order;
      setOrders(all => [order, ...all]); setCart({}); setUndo(null); setNote('');
      return order;
    } catch (e) { void loadMenu(); throw e; }
  };
  const closeBag = () => { setOpen(false); setStep('bag'); };
  const payAtOutlet = async () => {
    if (!canOrder || submitting.current || !count || invalidBag) return;
    submitting.current = true; setBusy(true); setActionError('');
    try {
      const order = await createOrder();
      try {
        const payment = await customerPaymentApi.create({ foodOrderId: order.id, method: 'PAY_AT_OUTLET' });
        setSuccess(`Order ${order.orderReference} sent to the kitchen. Please pay ${money(payment.amount)} at the outlet.`);
      } catch {
        setSuccess(`Order ${order.orderReference} sent to the kitchen. Choose how to pay in Payments.`);
      }
      placedOrder.current = null; closeBag();
    } catch (e) { setActionError(errorMessage(e)); }
    finally { submitting.current = false; setBusy(false); }
  };
  const startCardPayment = () => {
    if (!canOrder || !count || invalidBag) return;
    setGatewayAmount(estimatedTotal); closeBag();
  };
  const sendRequest = async () => {
    if (!canOrder || submitting.current || !message.trim()) return;
    submitting.current = true; setBusy(true); setActionError('');
    try {
      const request = await foodRequestApi.create({ menuItemId: requestItem ? Number(requestItem) : undefined, message: message.trim() });
      setRequests(all => [request, ...all]); setMessage(''); setRequestItem(''); setRequestOpen(false);
      setSuccess('Your food request was sent to our staff.');
    } catch (e) { setActionError(errorMessage(e)); }
    finally { submitting.current = false; setBusy(false); }
  };

  return (
    <div className="page-container page-enter menu-page">
      <div className="menu-top">
      <SectionHeading eyebrow="MADE WITH LOVE. SHARED WITH YOU." title="A little something delicious"
        description="Fresh ingredients. Familiar favorites. Something to bring everyone together."
        action={canOrder ? <button className="button primary" onClick={() => { setOpen(true); setActionError(''); }}>
          <ShoppingBagIcon /> Your bag ({count})</button> :
          !user ? <Link className="button primary" to="/login" state={{ from: '/menu' }}>Sign in to order</Link> : user?.roles.some(r => ['ADMIN', 'MANAGER', 'WAITER', 'KITCHEN_STAFF'].includes(r)) ? <Link className="button primary" to="/admin/menu">Manage menu</Link> : undefined} />
      {success && <p className="menu-notice success" role="status"><CheckCircleIcon />{success}</p>}
      {!user && <p className="menu-notice"><InformationCircleIcon />
        <span>Explore our menu freely. <Link to="/login" state={{ from: '/menu' }}>Sign in</Link> to add food to your bag and place an order.</span></p>}
      {err && <p className="error" role="alert">{err} <button className="text-button" onClick={loadMenu}>Retry</button></p>}
      <div className="menu-filters">
        <label className="inline-search"><MagnifyingGlassIcon />
          <input aria-label="Search menu" placeholder="What are you craving?" value={query} onChange={e => setQuery(e.target.value)} />
        </label>
        {canOrder && <div className="menu-actions">
          {undo && <button className="text-button" disabled={busy} onClick={() => { setCart(undo); setUndo(null); }}>Undo bag change</button>}
          <button className="button" onClick={() => { setRequestOpen(true); setActionError(''); }}>Request food / ask staff</button>
        </div>}
      </div>
      </div>
      {loading ? <div className="menu-grid">{Array.from({ length: 6 }, (_, i) => <div className="skeleton" key={i} style={{ height: 260 }} />)}</div> :
        <div className="menu-grid">{visible.map(item => (
          <article className="food-card" key={item.id}>
            <div className="food-photo"><MenuPhoto name={item.name} imageUrl={item.imageUrl} /></div>
            <div className="food-info"><h3>{item.name}</h3><p>{item.description}</p>
              <div className="row-between"><b>{money(item.price)}</b>
                {!item.isAvailable ? <span className="muted">Currently unavailable</span> : canOrder ?
                  <div className="quantity">
                    {cart[item.id] > 0 && <><button disabled={busy} aria-label={`Remove one ${item.name}`} onClick={() => quantity(item.id, -1)}><MinusIcon /></button><span>{cart[item.id]}</span></>}
                    <button disabled={busy || cart[item.id] >= 99} aria-label={`Add ${item.name}`} onClick={() => quantity(item.id, 1)}><PlusIcon /></button>
                  </div> : !user ? <Link className="card-signin" to="/login" state={{ from: '/menu' }}>Sign in to order</Link> : null}
              </div>
            </div>
          </article>
        ))}</div>}
      {!loading && !err && !visible.length && <Empty title={query ? 'Nothing on the menu matches that search' : 'The menu is being prepared'}>
        {query ? <button className="text-button" onClick={() => setQuery('')}>Clear search</button> : <p>Please check back shortly.</p>}
      </Empty>}
      {open && canOrder && <Modal title={step === 'choose' ? 'How would you like to pay?' : 'Your delicious little lineup'} onClose={() => { if (!busy) closeBag(); }}>
        {actionError && <p className="error" role="alert">{actionError}</p>}
        {step === 'choose' && entries.length ? <div className="pay-choice">
          <div className="card-form-amount"><span>Order total</span><strong>{money(estimatedTotal)}</strong></div>
          <button className="pay-option" disabled={busy} onClick={startCardPayment}>
            <span className="pay-option-icon"><CreditCardIcon /></span>
            <span><b>Pay with card</b><small>Pay now with your credit or debit card.</small></span><ChevronRightIcon />
          </button>
          <button className="pay-option" disabled={busy} onClick={payAtOutlet}>
            <span className="pay-option-icon outlet"><BuildingStorefrontIcon /></span>
            <span><b>{busy ? 'Placing your order…' : 'Pay at the outlet'}</b><small>Pay when you dine with us or collect your order.</small></span><ChevronRightIcon />
          </button>
          <button className="text-button pay-back" disabled={busy} onClick={() => setStep('bag')}><ArrowLeftIcon /> Back to your bag</button>
        </div> : entries.length ? <>
          {entries.map(({ id, count: n, item }) => <div className="cart-row" key={id}>
            <div><b>{item?.name ?? 'Item no longer on the menu'}</b><p>{item ? money(item.price) : 'Remove this item to continue'}</p></div>
            <div className="quantity">
              <button disabled={busy} aria-label={`Remove one ${item?.name ?? 'item'}`} onClick={() => quantity(id, -1)}><MinusIcon /></button>
              <span>{n}</span><button disabled={busy || n >= 99 || !item?.isAvailable} aria-label={`Add one ${item?.name ?? 'item'}`} onClick={() => quantity(id, 1)}><PlusIcon /></button>
            </div>
            <button className="text-button" disabled={busy} onClick={() => { setUndo(cart); const next = { ...cart }; delete next[id]; setCart(next); }}>Remove</button>
          </div>)}
          <div className="totals"><span>Subtotal</span><b>{money(total)}</b>
            <span>Service charge estimate (10%)</span><b>{money(total * .1)}</b>
            <strong>Estimated total</strong><strong>{money(total * 1.1)}</strong></div>
          <p className="muted small">The final bill confirms the service charge.</p>
          {invalidBag && <p className="error">Remove unavailable items before ordering.</p>}
          <label>Order type<select value={orderType} disabled={busy} onChange={e => setOrderType(e.target.value)}><option value="DINE_IN">Dine in</option><option value="TAKEAWAY">Takeaway</option></select></label>
          <label>Notes for staff (optional)<textarea value={note} disabled={busy} maxLength={500} placeholder="Table number, food preferences, or other details" onChange={e => setNote(e.target.value)} /></label>
          <button className="button primary full" disabled={busy || invalidBag} onClick={() => { setActionError(''); setStep('choose'); }}>Continue to payment</button>
          <button className="text-button" disabled={busy} onClick={() => { setUndo(cart); setCart({}); }}>Clear bag</button>
        </> : <Empty title="Your bag is feeling a little empty"><p>Add something delicious from the menu.</p></Empty>}
        {undo && <button className="text-button" disabled={busy} onClick={() => { setCart(undo); setUndo(null); }}>Undo bag change</button>}
      </Modal>}
      {gatewayAmount !== null && <PaymentGateway amount={gatewayAmount} reference="Your food order" doneLabel="Back to the menu"
        steps={['Your bag', 'Payment method', 'Card payment']}
        pay={async (card) => {
          const order = placedOrder.current ?? await createOrder();
          return customerPaymentApi.create({ foodOrderId: order.id, method: 'CARD', card });
        }}
        onClose={() => {
          // Closed before paying: if the order was already placed, it can be paid later in Payments.
          if (placedOrder.current) setSuccess(`Order ${placedOrder.current.orderReference} sent to the kitchen. You can pay for it any time in Payments.`);
          placedOrder.current = null; setGatewayAmount(null);
        }}
        onDone={payment => {
          setSuccess(`Paid ${money(payment.amount)} for order ${placedOrder.current?.orderReference ?? ''}. It's on its way to the kitchen.`);
          placedOrder.current = null; setGatewayAmount(null);
        }} />}
      {requestOpen && canOrder && <Modal title="Request food or ask our staff" onClose={() => { if (!busy) setRequestOpen(false); }}>
        <form onSubmit={e => { e.preventDefault(); void sendRequest(); }}>
          <p className="muted">Ask about a dish, dietary needs, or food you cannot find. This sends a request, not an order.</p>
          <label>Dish (optional)<select value={requestItem} disabled={busy} onChange={e => setRequestItem(e.target.value)}>
            <option value="">General food request</option>{items.map(i => <option key={i.id} value={i.id}>{i.name}</option>)}
          </select></label>
          <label>Your request<textarea required maxLength={500} value={message} disabled={busy} placeholder="Tell us what you need and where you are seated." onChange={e => setMessage(e.target.value)} /></label>
          {actionError && <p className="error" role="alert">{actionError}</p>}
          <button className="button primary full" disabled={busy || !message.trim()}>{busy ? 'Sending…' : 'Send to staff'}</button>
        </form>
      </Modal>}
      {canOrder && <section style={{ marginTop: 32 }} aria-label="My orders and requests">
        <div className="row-between"><h2>Your orders and requests</h2><button className="text-button" onClick={loadHistory}>Refresh</button></div>
        {historyError && <p className="error" role="alert">{historyError}</p>}
        {!orders.length && !requests.length && <p className="muted">Your orders and food requests will appear here.</p>}
        {orders.map(o => <div className="cart-row" key={`order-${o.id}`}><div><b>{o.orderReference}</b><p>{o.items.map(i => `${i.quantity} × ${i.itemNameSnapshot}`).join(', ')}</p></div><span>{o.status}</span><b>{money(o.subtotal)}</b>{o.status !== 'CANCELLED' && <Link className="card-signin" to={`/payments?order=${o.id}`}>View bill</Link>}</div>)}
        {requests.map(r => <div className="cart-row" key={`request-${r.id}`}><div><b>{r.itemName || 'Food request'}</b><p>{r.message}</p></div><span>{r.status === 'RESOLVED' ? 'Resolved by staff' : 'Waiting for staff'}</span></div>)}
      </section>}
    </div>
  );
}
