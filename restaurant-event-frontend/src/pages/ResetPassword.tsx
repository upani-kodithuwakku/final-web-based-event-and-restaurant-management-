import { useState } from 'react';
import { Link, useNavigate, useSearchParams } from 'react-router-dom';
import { errorMessage, passwordResetApi } from '../services/api';
import { images } from '../data';

export default function ResetPassword() {
  const [params] = useSearchParams();
  const token = params.get('token') ?? '';
  const navigate = useNavigate();
  const [password, setPassword] = useState('');
  const [confirm, setConfirm] = useState('');
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState('');
  const [done, setDone] = useState(false);

  const submit = async (e: React.FormEvent) => {
    e.preventDefault(); setError('');
    if (password.length < 8 || password.length > 72) return setError('Password must be between 8 and 72 characters.');
    if (password !== confirm) return setError('The two passwords do not match.');
    setBusy(true);
    try { await passwordResetApi.reset(token, password); setDone(true); }
    catch (err) { setError(errorMessage(err)); }
    finally { setBusy(false); }
  };

  return (
    <div className="auth-page">
      <div className="auth-image" style={{ backgroundImage: `linear-gradient(0deg,rgba(0,0,0,.6),transparent),url(${images.garden})` }}>
        <h1>A fresh start.<br />Same favourite table.</h1>
      </div>
      <div className="auth-form">
        <span className="eyebrow">NEW PASSWORD</span>
        <h1>Choose a new password</h1>
        {!token ? (
          <p className="error" role="alert">This reset link is incomplete. <Link className="text-button" to="/forgot-password">Request a new one</Link>.</p>
        ) : done ? (
          <div className="reset-sent" role="status">
            <p>Your password has been updated.</p>
            <button className="button primary full" onClick={() => navigate('/login')}>Log in</button>
          </div>
        ) : (
          <form onSubmit={submit}>
            <label>New password<input required type="password" autoComplete="new-password" minLength={8} maxLength={72} placeholder="At least 8 characters"
              value={password} onChange={e => setPassword(e.target.value)} /></label>
            <label>Confirm new password<input required type="password" autoComplete="new-password" placeholder="Type it again"
              value={confirm} onChange={e => setConfirm(e.target.value)} /></label>
            {error && <p className="error" role="alert">{error}{error.includes('expired') && <> <Link className="text-button" to="/forgot-password">Request a new link</Link></>}</p>}
            <button className="button primary full" disabled={busy}>{busy ? 'Saving…' : 'Update password'}</button>
          </form>
        )}
        <p className="center small"><Link className="text-button" to="/login">Back to log in</Link></p>
      </div>
    </div>
  );
}
