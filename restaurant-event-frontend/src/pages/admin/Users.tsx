import { useState, useEffect } from 'react';
import { KeyIcon, NoSymbolIcon, CheckCircleIcon } from '@heroicons/react/24/outline';
import { Modal, SectionHeading, Badge, Empty } from '../../components/UI';
import { adminUserApi, type AdminUserDto, errorMessage } from '../../services/api';

const ROLE_ORDER = ['ADMIN', 'MANAGER', 'EVENT_COORDINATOR', 'CASHIER', 'KITCHEN_STAFF', 'WAITER', 'INVENTORY_MANAGER', 'CUSTOMER'];

export default function AdminUsers() {
  const [users, setUsers] = useState<AdminUserDto[]>([]);
  const [loading, setLoading] = useState(true);
  const [filter, setFilter] = useState<'all' | 'staff' | 'customer'>('all');
  const [roleFilter, setRoleFilter] = useState('all');
  const [resetTarget, setResetTarget] = useState<AdminUserDto | null>(null);
  const [resetPw, setResetPw] = useState('');
  const [busy, setBusy] = useState(false);
  const [err, setErr] = useState('');

  const load = async () => {
    setLoading(true);
    try { setUsers(await adminUserApi.list()); }
    catch (e) { setErr(errorMessage(e)); }
    finally { setLoading(false); }
  };

  useEffect(() => { void load(); }, []);

  const toggleSuspend = async (u: AdminUserDto) => {
    setBusy(true);
    try {
      const updated = await adminUserApi.suspend(u.id, !u.isActive);
      setUsers(all => all.map(x => x.id === u.id ? { ...x, isActive: updated.isActive } : x));
    } catch (e) { setErr(errorMessage(e)); }
    finally { setBusy(false); }
  };

  const doResetPw = async () => {
    if (!resetTarget || resetPw.length < 8) { setErr('Password must be at least 8 characters.'); return; }
    setBusy(true); setErr('');
    try {
      await adminUserApi.resetPassword(resetTarget.id, resetPw);
      setResetTarget(null); setResetPw('');
    } catch (e) { setErr(errorMessage(e)); }
    finally { setBusy(false); }
  };

  const allRoles = Array.from(new Set(users.flatMap(u => u.roles)))
    .sort((a, b) => ROLE_ORDER.indexOf(a) - ROLE_ORDER.indexOf(b));

  const filtered = users.filter(u => {
    const isCustomer = u.roles.includes('CUSTOMER') && u.roles.length === 1;
    if (filter === 'staff' && isCustomer) return false;
    if (filter === 'customer' && !isCustomer) return false;
    if (roleFilter !== 'all' && !u.roles.includes(roleFilter)) return false;
    return true;
  });

  const totalStaff = users.filter(u => !(u.roles.includes('CUSTOMER') && u.roles.length === 1)).length;
  const totalCustomers = users.filter(u => u.roles.includes('CUSTOMER') && u.roles.length === 1).length;
  const suspended = users.filter(u => !u.isActive).length;

  return (
    <div className="page-enter">
      <SectionHeading
        eyebrow="USER MANAGEMENT"
        title="All users"
        description="View and manage all registered users — staff and customers."
      />

      {/* Summary cards */}
      <div style={{ display: 'flex', gap: 16, marginBottom: 28, flexWrap: 'wrap' }}>
        {[
          { label: 'Total users', value: users.length },
          { label: 'Staff', value: totalStaff },
          { label: 'Customers', value: totalCustomers },
          { label: 'Suspended', value: suspended, warn: suspended > 0 },
        ].map(({ label, value, warn }) => (
          <div key={label} className="report-card" style={{ minWidth: 140, flex: 1, textAlign: 'center', padding: '16px 12px' }}>
            <p style={{ fontSize: 28, fontWeight: 800, color: warn ? 'var(--arches)' : 'var(--hof)', margin: 0 }}>{value}</p>
            <p className="small muted" style={{ margin: 0 }}>{label}</p>
          </div>
        ))}
      </div>

      {/* Filters */}
      <div style={{ display: 'flex', gap: 12, marginBottom: 20, flexWrap: 'wrap', alignItems: 'center' }}>
        <div className="tabs" style={{ marginBottom: 0 }}>
          {(['all', 'staff', 'customer'] as const).map(t => (
            <button key={t} className={filter === t ? 'active' : ''} onClick={() => setFilter(t)}>
              {t.charAt(0).toUpperCase() + t.slice(1)}
            </button>
          ))}
        </div>
        <select
          value={roleFilter}
          onChange={e => setRoleFilter(e.target.value)}
          style={{ border: '1.5px solid var(--gray-200)', borderRadius: 'var(--radius-sm)', padding: '7px 12px', fontSize: 13, background: 'var(--white)' }}
        >
          <option value="all">All roles</option>
          {allRoles.map(r => <option key={r} value={r}>{r.replace(/_/g, ' ')}</option>)}
        </select>
      </div>

      {err && <p className="error">{err}</p>}

      {loading ? (
        <div className="skeleton" style={{ height: 200 }} />
      ) : filtered.length === 0 ? (
        <Empty title="No users found" />
      ) : (
        <div className="admin-table-wrap">
          <table className="admin-table">
            <thead>
              <tr>
                <th>Name</th>
                <th>Email</th>
                <th>Phone</th>
                <th>Roles</th>
                <th>Status</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {filtered.map(u => (
                <tr key={u.id} style={{ opacity: u.isActive ? 1 : 0.55 }}>
                  <td style={{ fontWeight: 600 }}>{u.fullName || '—'}</td>
                  <td className="small">{u.email}</td>
                  <td className="small muted">{u.phone || '—'}</td>
                  <td>
                    <div style={{ display: 'flex', flexWrap: 'wrap', gap: 4 }}>
                      {u.roles.map(r => <Badge key={r} status={r} />)}
                    </div>
                  </td>
                  <td><Badge status={u.isActive ? 'ACTIVE' : 'INACTIVE'} /></td>
                  <td>
                    <div style={{ display: 'flex', gap: 6 }}>
                      <button
                        className="button"
                        style={{ padding: '5px 10px', fontSize: 12 }}
                        disabled={busy}
                        onClick={() => toggleSuspend(u)}
                        title={u.isActive ? 'Suspend user' : 'Activate user'}
                      >
                        {u.isActive
                          ? <><NoSymbolIcon style={{ width: 13, height: 13, display: 'inline', marginRight: 4 }} />Suspend</>
                          : <><CheckCircleIcon style={{ width: 13, height: 13, display: 'inline', marginRight: 4 }} />Activate</>
                        }
                      </button>
                      <button
                        className="button"
                        style={{ padding: '5px 10px', fontSize: 12 }}
                        onClick={() => { setResetTarget(u); setResetPw(''); setErr(''); }}
                      >
                        <KeyIcon style={{ width: 13, height: 13, display: 'inline', marginRight: 4 }} />Reset PW
                      </button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {resetTarget && (
        <Modal title={`Reset password — ${resetTarget.fullName || resetTarget.email}`} onClose={() => setResetTarget(null)}>
          <div className="input-group">
            <p className="muted small">Set a new password for this user. They must change it after their next login.</p>
            <label>New password<input type="password" value={resetPw} onChange={e => setResetPw(e.target.value)} placeholder="Min. 8 characters" /></label>
            {err && <p className="error">{err}</p>}
            <button className="button primary full" disabled={busy || resetPw.length < 8} onClick={doResetPw}>
              {busy ? 'Saving…' : 'Reset password'}
            </button>
          </div>
        </Modal>
      )}
    </div>
  );
}
