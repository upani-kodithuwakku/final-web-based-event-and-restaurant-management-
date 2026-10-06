import { useState } from 'react';
import { Link } from 'react-router-dom';
import { useApp, useStored } from '../context/AppContext';
import { Empty, SectionHeading } from '../components/UI';
import { errorMessage, userApi } from '../services/api';

export default function Profile() {
  const app = useApp();
  const [preferences, setPreferences] = useStored('gather-preferences', { diet: '', notes: '', updates: true });
  const [busy, setBusy] = useState(false);
  const [saved, setSaved] = useState(false);
  const [err, setErr] = useState('');

  const saveProfile = async (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault(); setBusy(true); setErr(''); setSaved(false);
    const f = new FormData(e.currentTarget);
    try {
      const updated = await userApi.update({ fullName: String(f.get('name')), phone: String(f.get('phone') || '') });
      app.login({ ...app.user!, fullName: updated.fullName });
      setSaved(true);
    } catch (error) { setErr(errorMessage(error)); }
    finally { setBusy(false); }
  };

  return (
    <div className="page-container narrow">
      <SectionHeading eyebrow="YOUR GATHER" title="A little about you" description="The details that help make every visit feel more like you." />
      {app.user ? (
        <>
          <form className="panel" onSubmit={saveProfile}>
            <h3>Personal information</h3>
            <label>Full name<input name="name" defaultValue={app.user.fullName} required /></label>
            <label>Email address<input type="email" value={app.user.email} readOnly /></label>
            <label>Phone number<input name="phone" type="tel" defaultValue={app.user.phone || ''} placeholder="+94 77 123 4567" /></label>
            {err && <p className="error" role="alert">{err}</p>}
            {saved && <p className="muted small" style={{ color: 'var(--babu)' }}>Profile updated successfully.</p>}
            <button className="button primary" disabled={busy}>{busy ? 'Saving…' : 'Save profile'}</button>
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
