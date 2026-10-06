import { useState } from 'react';
import { Link } from 'react-router-dom';
import { EnvelopeIcon } from '@heroicons/react/24/outline';
import { errorMessage, passwordResetApi } from '../services/api';
import { images } from '../data';

export default function ForgotPassword() {
  const [email, setEmail] = useState('');
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState('');
  const [sent, setSent] = useState<{ message: string; resetLink?: string } | null>(null);

  const submit = async (e: React.FormEvent) => {
    e.preventDefault(); setBusy(true); setError('');
    try { setSent(await passwordResetApi.request(email.trim())); }
    catch (err) { setError(errorMessage(err)); }
    finally { setBusy(false); }
  };

  // The backend has no email server: it logs the link, and returns it here only in local demo mode.
  const resetPath = sent?.resetLink ? sent.resetLink.replace(/^https?:\/\/[^/]+/, '') : null;

  return (
    <div className="auth-page">
      <div className="auth-image" style={{ backgroundImage: `linear-gradient(0deg,rgba(0,0,0,.6),transparent),url(${images.garden})` }}>
        <h1>It happens to all of us.<br />Let's get you back in.</h1>
      </div>
      <div className="auth-form">
        <span className="eyebrow">FORGOT PASSWORD</span>
        <h1>Reset your password</h1>
        {sent ? (
          <div className="reset-sent" role="status">
            <EnvelopeIcon />
            <p>{sent.message}</p>
            {resetPath && <p className="small">Demo mode (no email server): <Link className="text-button" to={resetPath}>open your reset link</Link></p>}
            {!resetPath && <p className="small muted">The link is valid for 30 minutes. Check your inbox, or ask an admin to reset your password.</p>}
          </div>
        ) : (
          <>
            <p>Enter the email you signed up with and we'll send you a link to choose a new password.</p>
            <form onSubmit={submit}>
              <label>Email address<input required type="email" autoComplete="email" placeholder="you@example.com"
                value={email} onChange={e => setEmail(e.target.value)} /></label>
              {error && <p className="error" role="alert">{error}</p>}
              <button className="button primary full" disabled={busy}>{busy ? 'Sending…' : 'Send reset link'}</button>
            </form>
          </>
        )}
        <p className="center small"><Link className="text-button" to="/login">Back to log in</Link></p>
      </div>
    </div>
  );
}
