import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { authenticate, errorMessage } from '../services/api';
import { useApp } from '../context/AppContext';
import { images } from '../data';

const ROLE_REDIRECT: [string, string][] = [
  ['ADMIN',               '/admin'],
  ['MANAGER',             '/admin'],
  ['EVENT_COORDINATOR',   '/admin/events'],
  ['INVENTORY_MANAGER',   '/admin/inventory'],
  ['CASHIER',             '/admin/cashier'],
  ['KITCHEN_STAFF',       '/admin/kitchen'],
  ['WAITER',              '/admin/tables'],
];

function redirectForRoles(roles: string[]): string {
  for (const [role, path] of ROLE_REDIRECT) {
    if (roles.includes(role)) return path;
  }
  return '/dashboard';
}

export default function Auth({ register = false }: { register?: boolean }) {
  const app = useApp();
  const navigate = useNavigate();
  const [error, setError] = useState('');
  const [busy, setBusy] = useState(false);

  return (
    <div className="auth-page">
      <div className="auth-image" style={{ backgroundImage: `linear-gradient(0deg,rgba(0,0,0,.6),transparent),url(${images.garden})` }}>
        <h1>A place to belong.<br />A table to come back to.</h1>
      </div>
      <div className="auth-form">
        <span className="eyebrow">WELCOME TO GATHER</span>
        <h1>{register ? 'Your seat is waiting.' : 'Good to see you again.'}</h1>
        <p>{register ? 'Create an account for more moments around the table.' : 'Log in to your next good moment.'}</p>
        <form onSubmit={async e => {
          e.preventDefault(); setBusy(true); setError('');
          const values = Object.fromEntries(new FormData(e.currentTarget)) as Record<string, string>;
          try {
            const user = await authenticate(register, values);
            app.login(user);
            navigate(redirectForRoles(user.roles));
          } catch (err) { setError(errorMessage(err)); }
          finally { setBusy(false); }
        }}>
          {register && <label>Full name<input required name="fullName" autoComplete="name" placeholder="Your full name" /></label>}
          <label>Email address<input required name="email" autoComplete="email" type="email" placeholder="you@example.com" /></label>
          <label>Password<input required name="password" type="password" autoComplete={register ? 'new-password' : 'current-password'} minLength={register ? 8 : 1} placeholder={register ? 'At least 8 characters' : 'Your password'} /></label>
          {register && <label>Phone number<input name="phone" type="tel" autoComplete="tel" placeholder="+94 77 123 4567" /></label>}
          {error && <p className="error" role="alert">{error}</p>}
          <button className="button primary full" disabled={busy}>{busy ? 'One moment…' : register ? 'Create account' : 'Log in'}</button>
        </form>
        <p className="center small">
          {register ? 'Already part of the table? ' : 'New around here? '}
          <Link className="text-button" to={register ? '/login' : '/register'}>{register ? 'Log in' : 'Create an account'}</Link>
        </p>
      </div>
    </div>
  );
}
