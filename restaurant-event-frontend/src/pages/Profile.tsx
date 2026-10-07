import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { format } from 'date-fns';
import { useApp, useStored } from '../context/AppContext';
import { Empty, SectionHeading } from '../components/UI';
import { errorMessage, userApi, type UserProfileDto } from '../services/api';

export default function Profile() {
  const app = useApp();
  const [preferences, setPreferences] = useStored('gather-preferences', { diet: '', notes: '', updates: true });
  const [profile, setProfile] = useState<UserProfileDto | null>(null);
  const [loadErr, setLoadErr] = useState('');
  const [busy, setBusy] = useState(false);
  const [saved, setSaved] = useState(false);
  const [err, setErr] = useState('');
  const [pw, setPw] = useState({ current: '', next: '', confirm: '' });
  const [pwBusy, setPwBusy] = useState(false);
  const [pwMsg, setPwMsg] = useState('');
  const [pwErr, setPwErr] = useState('');

  // Load the latest details from the server rather than what was saved at login.
  useEffect(() => {
    if (!app.user) return;
    userApi.me().then(setProfile).catch(e => setLoadErr(errorMessage(e)));
  }, [app.user?.userId]);

  const saveProfile = async (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault(); setBusy(true); setErr(''); setSaved(false);
    const f = new FormData(e.currentTarget);
    try {
      const updated = await userApi.update({ fullName: String(f.get('name')), phone: String(f.get('phone') || '') });
      setProfile(updated);
      app.login({ ...app.user!, fullName: updated.fullName, phone: updated.phone });
      setSaved(true);
    } catch (error) { setErr(errorMessage(error)); }
    finally { setBusy(false); }
  };

  const changePassword = async (e: React.FormEvent) => {
    e.preventDefault(); setPwErr(''); setPwMsg('');
    if (pw.next.length < 8 || pw.next.length > 72) return setPwErr('New password must be between 8 and 72 characters.');
    if (pw.next !== pw.confirm) return setPwErr('The new passwords do not match.');
    setPwBusy(true);
    try {
      await userApi.changePassword({ currentPassword: pw.current, newPassword: pw.next });
      setPw({ current: '', next: '', confirm: '' });
      setPwMsg('Password changed. Use your new password next time you log in.');
    } catch (error) { setPwErr(errorMessage(error)); }
    finally { setPwBusy(false); }
  };

  const details = profile ?? (app.user ? { fullName: app.user.fullName, email: app.user.email, phone: app.user.phone ?? '' } : null);

  return (
    <div className="page-container narrow">
      <SectionHeading eyebrow="YOUR GATHER" title="A little about you" description="The details that help make every visit feel more like you." />
      {app.user && details ? (
        <>
          <form className="panel" onSubmit={saveProfile} key={profile ? 'loaded' : 'initial'}>
            <h3>Personal information</h3>
            {profile?.createdAt && <p className="small muted">Member since {format(new Date(profile.createdAt), 'MMMM yyyy')}</p>}
            {loadErr && <p className="error" role="alert">{loadErr}</p>}
            <label>Full name<input name="name" defaultValue={details.fullName} required maxLength={100} /></label>
            <label>Email address<input type="email" value={details.email} readOnly /></label>
            <label>Phone number<input name="phone" type="tel" pattern="[0-9]{10}|^$" inputMode="numeric" defaultValue={details.phone || ''} placeholder="0771234567" /></label>
            {err && <p className="error" role="alert">{err}</p>}
            {saved && <p className="muted small" style={{ color: 'var(--babu)' }}>Profile updated successfully.</p>}
            <button className="button primary" disabled={busy}>{busy ? 'Saving…' : 'Save profile'}</button>
          </form>
          <form className="panel" onSubmit={changePassword}>
            <h3>Change password</h3>
            <label>Current password<input type="password" autoComplete="current-password" required value={pw.current}
              onChange={e => setPw({ ...pw, current: e.target.value })} /></label>
            <label>New password<input type="password" autoComplete="new-password" required minLength={8} maxLength={72} placeholder="At least 8 characters"
              value={pw.next} onChange={e => setPw({ ...pw, next: e.target.value })} /></label>
            <label>Confirm new password<input type="password" autoComplete="new-password" required value={pw.confirm}
              onChange={e => setPw({ ...pw, confirm: e.target.value })} /></label>
            {pwErr && <p className="error" role="alert">{pwErr}</p>}
            {pwMsg && <p className="muted small" role="status" style={{ color: 'var(--babu)' }}>{pwMsg}</p>}
            <button className="button primary" disabled={pwBusy}>{pwBusy ? 'Saving…' : 'Change password'}</button>
          </form>
          <form className="panel" onSubmit={e => { e.preventDefault(); app.notify('Preferences saved on this device.'); }}>
            <h3>Make yourself at home</h3>
            <label>Dietary preference
              <select value={preferences.diet} onChange={e => setPreferences({ ...preferences, diet: e.target.value })}>
                <option value="">No preference</option>
                <option>Vegetarian</option>
                <option>Vegan</option>
                <option>Gluten-free</option>
              </select>
            </label>
            <label>Anything else?<textarea value={preferences.notes} onChange={e => setPreferences({ ...preferences, notes: e.target.value })} /></label>
            <p className="small muted">Stored on this device. Include dietary needs in your booking request so staff receive them.</p>
            <button className="button primary">Save preferences</button>
          </form>
        </>
      ) : (
        <Empty title="Let's get to know you">
          <Link className="button primary" to="/login">Sign in</Link>
        </Empty>
      )}
    </div>
  );
}
