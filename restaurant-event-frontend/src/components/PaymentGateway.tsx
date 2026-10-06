import toast from 'react-hot-toast';
import { useState } from 'react';
import { CheckIcon, CreditCardIcon, CalendarDaysIcon, LockClosedIcon, UserIcon, ShieldCheckIcon, ArrowRightIcon } from '@heroicons/react/24/outline';
import { Modal } from './UI';
import { errorMessage, type CardDetails, type CustomerPaymentDto } from '../services/api';
import { cardBrand, cardNumberIsValid, expiryIsValid, formatCardNumber, formatExpiry } from '../services/card';
import { money } from '../data';

type Props = {
  /** Amount shown before paying; the success screen uses the amount the server charged. */
  amount: number;
  reference?: string;
  /** Checkout steps shown at the top; the last one is the current step. */
  steps: string[];
  doneLabel: string;
  pay: (card: CardDetails) => Promise<CustomerPaymentDto>;
  onClose: () => void;
  onDone: (payment: CustomerPaymentDto) => void;
};

// Keeps the "processing" state on screen long enough to read.
const MIN_PROCESSING_MS = 1200;

export default function PaymentGateway({ amount, reference, steps, doneLabel, pay, onClose, onDone }: Props) {
  const [card, setCard] = useState<CardDetails>({ holderName: '', number: '', expiry: '', cvv: '' });
  const [error, setError] = useState('');
  const [stage, setStage] = useState<'form' | 'processing' | 'done'>('form');
  const [paid, setPaid] = useState<CustomerPaymentDto | null>(null);
  const set = (field: keyof CardDetails, value: string) => { setCard(c => ({ ...c, [field]: value })); setError(''); };
  const brand = cardBrand(card.number);

  const submit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!card.holderName.trim()) return setError('Please enter the cardholder name.');
    if (!cardNumberIsValid(card.number)) return setError('Card number must have exactly 12 digits.');
    if (!expiryIsValid(card.expiry)) return setError('Please enter a valid expiry date (MM/YY) that has not passed.');
    if (!/^\d{3,4}$/.test(card.cvv)) return setError('CVC must be 3 or 4 digits.');
    setStage('processing'); setError('');
    const started = Date.now();
    try {
      const payment = await pay(card);
      if (payment.status !== 'PAID') throw new Error('Payment was not completed. Please refresh your bill before trying again.');
      await new Promise(r => setTimeout(r, Math.max(0, MIN_PROCESSING_MS - (Date.now() - started))));
      setCard({ holderName: '', number: '', expiry: '', cvv: '' });
      setPaid(payment); setStage('done');
    } catch (err) { setError(errorMessage(err)); setStage('form'); }
  };

  const finish = (payment: CustomerPaymentDto) => {
    onDone(payment);
    toast.success(<div><strong>From: Gather</strong><p>{payment.confirmationMessage || `Payment successful! ${money(payment.amount)} received (ref ${payment.paymentReference}). Thank you - gather.com`}</p></div>, { duration: 8000, style: { maxWidth: 480 } });
  };
  const close = () => { if (stage === 'done' && paid) finish(paid); else if (stage === 'form') onClose(); };

  return (
    <Modal title="Card checkout" onClose={close}>
      <div className="gateway">
        <ol className="gw-steps" aria-label="Checkout progress">
          {steps.map((step, i) => {
            const current = i === steps.length - 1 && stage !== 'done';
            return <li key={step} className={current ? 'current' : 'done'} aria-current={current ? 'step' : undefined}>
              <span>{current ? i + 1 : <CheckIcon />}</span>{step}
            </li>;
          })}
        </ol>

        {stage === 'done' && paid ? (
          <div className="gw-success" role="status">
            <div className="gw-check"><CheckIcon /></div>
            <h3>Payment complete</h3>
            <p><strong>{money(paid.amount)}</strong> · No real charge</p>
            <p className="muted">{paid.cardBrand && paid.cardBrand !== 'CARD' ? paid.cardBrand : 'Card'} •••• {paid.cardLast4} · Ref {paid.paymentReference}</p>
            <button className="button primary full" onClick={() => finish(paid)}>{doneLabel} <ArrowRightIcon /></button>
          </div>
        ) : (
          <form onSubmit={submit} noValidate>
            <div className="gw-head">
              <h3>Payment details</h3>
              <p className="muted">Use test card details to try the checkout. No real money is charged.</p>
            </div>
            <div className="gw-test-note">Test card: <b>4242 4242 4242</b><br />Use any name, a future expiry date and CVC <b>123</b>.</div>
            <div className="gw-amount"><span>{reference ?? 'Amount due'}</span><strong>{money(amount)}</strong></div>

            <fieldset className="gw-fields" disabled={stage === 'processing'}>
              <label>Cardholder name
                <span className="gw-input"><UserIcon /><input autoComplete="cc-name" placeholder="Name on card" maxLength={100}
                  value={card.holderName} onChange={e => set('holderName', e.target.value)} /></span>
              </label>
              <label>Card number
                <span className="gw-input"><CreditCardIcon /><input inputMode="numeric" autoComplete="cc-number" placeholder="1234 5678 9012"
                  maxLength={14} value={card.number} onChange={e => set('number', formatCardNumber(e.target.value))} />
                  {brand && <em className="gw-brand">{brand}</em>}</span>
              </label>
              <div className="gw-row">
                <label>Expiry date
                  <span className="gw-input"><CalendarDaysIcon /><input inputMode="numeric" autoComplete="cc-exp" placeholder="MM / YY"
                    value={card.expiry} onChange={e => set('expiry', formatExpiry(e.target.value))} /></span>
                </label>
                <label><span className="gw-label">CVC <small>back of card</small></span>
                  <span className="gw-input"><LockClosedIcon /><input type="password" inputMode="numeric" autoComplete="cc-csc" placeholder="•••" maxLength={4}
                    value={card.cvv} onChange={e => set('cvv', e.target.value.replace(/\D/g, ''))} /></span>
                </label>
              </div>
            </fieldset>

            {error && <p className="error" role="alert">{error}</p>}
            <button className="button primary full gw-pay" disabled={stage === 'processing'}>
              {stage === 'processing'
                ? <><span className="gw-spinner" aria-hidden="true" /> Processing payment…</>
                : <><LockClosedIcon /> Pay {money(amount)} <ArrowRightIcon /></>}
            </button>
            <button type="button" className="text-button" disabled={stage === 'processing'} onClick={onClose}>Back to payment options · Pay at the outlet</button>
            <div className="gw-trust">
              <span><ShieldCheckIcon /> Simulated gateway, no real charge</span>
              <span><LockClosedIcon /> Only the last 4 digits are saved</span>
            </div>
          </form>
        )}
      </div>
    </Modal>
  );
}
