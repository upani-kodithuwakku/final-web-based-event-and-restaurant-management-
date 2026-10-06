import { useState, useEffect } from 'react';
import { KeyIcon, NoSymbolIcon, CheckCircleIcon } from '@heroicons/react/24/outline';
import { Modal, SectionHeading, Empty } from '../../components/UI';
import { adminUserApi, type AdminUserDto, errorMessage } from '../../services/api';

const ROLE_ORDER = ['ADMIN', 'MANAGER', 'EVENT_COORDINATOR', 'CASHIER', 'KITCHEN_STAFF', 'WAITER', 'INVENTORY_MANAGER', 'CUSTOMER'];

export default function AdminUsers() {
  const [query, setQuery] = useState('');
  const [statusFilter, setStatusFilter] = useState('all');
  const [users, setUsers] = useState<AdminUserDto[]>([]);
  const [loading, setLoading] = useState(true);
  const [filter, setFilter] = useState<'all' | 'staff' | 'customer'>('all');
  const [roleFilter, setRoleFilter] = useState('all');
  const [editUser, setEditUser] = useState<AdminUserDto>();
  const [editName, setEditName] = useState('');
  const [editPhone, setEditPhone] = useState('');
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
    if (!`${u.fullName} ${u.email} ${u.phone}`.toLowerCase().includes(query.toLowerCase().trim())) return false;
    if (statusFilter === 'active' && !u.isActive) return false;
    if (statusFilter === 'suspended' && u.isActive) return false;
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
    <div className="page-enter users-page">
      <SectionHeading
        eyebrow="USER MANAGEMENT"
        title="People at Gather"
        description="A little more personal. Manage your team and guests in one place."
      />

      {/* Summary cards */}
      <div className="users-summary">
        {[
          { label: 'Total users', value: users.length },
          { label: 'Staff', value: totalStaff },
          { label: 'Customers', value: totalCustomers },
          { label: 'Suspended', value: suspended, warn: suspended > 0 },
        ].map(({ label, value, warn }) => (
          <div key={label} className={`users-stat${warn ? ' users-stat-warning' : ''}`}>
            <p style={{ fontSize: 28, fontWeight: 800, color: warn ? 'var(--arches)' : 'var(--hof)', margin: 0 }}>{value}</p>
            <p className="small muted" style={{ margin: 0 }}>{label}</p>
          </div>
        ))}
      </div>

      {/* Filters */}
      <div className="users-toolbar">
        <label className="users-search"><span>Search people</span><input aria-label="Search by name, email or phone" placeholder="Search name, email or phone…" value={query} onChange={e => setQuery(e.target.value)} /></label>
        <div className="tabs" style={{ marginBottom: 0 }}>
          {(['all', 'staff', 'customer'] as const).map(t => (
            <button key={t} className={filter === t ? 'active' : ''} onClick={() => setFilter(t)}>
              {t.charAt(0).toUpperCase() + t.slice(1)}
            </button>
          ))}
        </div>
        <select
          aria-label="Filter by role"
          value={roleFilter}
          onChange={e => setRoleFilter(e.target.value)}
          style={{ border: '1.5px solid var(--gray-200)', borderRadius: 'var(--radius-sm)', padding: '7px 12px', fontSize: 13, background: 'var(--white)' }}
        >
          <option value="all">All roles</option>
          {allRoles.map(r => <option key={r} value={r}>{r.replace(/_/g, ' ')}</option>)}
        </select>
        <select aria-label="Filter by status" value={statusFilter} onChange={e => setStatusFilter(e.target.value)}><option value="all">All statuses</option><option value="active">Active</option><option value="suspended">Suspended</option></select>
      </div>

      {err && <p className="error">{err}</p>}

      {loading ? (
        <div className="skeleton" style={{ height: 200 }} />
      ) : filtered.length === 0 ? (
        <Empty title="No users found" />
      ) : (
        <div className="users-directory">
          <div className="users-directory-heading"><div><h3>User directory</h3><p>Manage access and account details</p></div><span>{filtered.length} people</span></div>
          <div className="users-table-scroll" role="region" aria-label="User directory" tabIndex={0}>
          <table className="admin-table">
            <thead>
              <tr>
                <th scope="col">Person</th>
                <th>Phone</th>
                <th>Roles</th>
                <th>Status</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {filtered.map(u => (
                <tr key={u.id}>
                  <td><div className="users-person"><span aria-hidden="true" className={`users-avatar tone-${u.id % 4}`}>{(u.fullName || u.email).split(/\s+/).slice(0, 2).map(n => n[0]).join('').toUpperCase()}</span><div><strong>{u.fullName || 'Unnamed user'}</strong><span className="users-email">{u.email}</span></div></div></td>
                  <td className="small muted">{u.phone || '—'}</td>
                  <td>
                    <div style={{ display: 'flex', flexWrap: 'wrap', gap: 4 }}>
                      {u.roles.map(r => <span key={r} className={`users-role ${r === 'CUSTOMER' ? 'guest' : 'team'}`}>{r.toLowerCase().replace(/_/g, ' ')}</span>)}
                    </div>
                  </td>
                  <td><span className={`users-status ${u.isActive ? 'is-active' : 'is-suspended'}`}><i />{u.isActive ? 'Active' : 'Suspended'}</span></td>
                  <td>
                    <div style={{ display: 'flex', gap: 6 }}><button className="button" onClick={() => {setEditUser(u);setEditName(u.fullName);setEditPhone(u.phone || '');setErr('');}}>Edit</button>
                      <button
                        className="button"
                        style={{ padding: '5px 10px', fontSize: 12 }}
                        disabled={busy}
                        onClick={() => toggleSuspend(u)}
                        aria-label={`${u.isActive ? 'Suspend' : 'Activate'} ${u.fullName}`}
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
                        <KeyIcon style={{ width: 13, height: 13, display: 'inline', marginRight: 4 }} />Reset password
                      </button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
          </div>
          <div className="users-directory-footer">Showing {filtered.length} of {users.length} users<span>Account access is managed securely</span></div>
        </div>
      )}

      {editUser && <Modal title="Edit user details" onClose={() => setEditUser(undefined)}><form onSubmit={async e => {e.preventDefault();setBusy(true);try {const updated=await adminUserApi.update(editUser.id,{fullName:editName,phone:editPhone});setUsers(all => all.map(x => x.id === updated.id ? updated : x));setEditUser(undefined);} catch(e) {setErr(errorMessage(e));} finally {setBusy(false);}}}><label>Full name<input required maxLength={100} value={editName} onChange={e => setEditName(e.target.value)} /></label><label>Phone<input type="tel" pattern="[0-9]{10}|^$" value={editPhone} onChange={e => setEditPhone(e.target.value)} placeholder="0771234567" /></label>{err && <p className="error" role="alert">{err}</p>}<button className="button primary" disabled={busy}>Save details</button></form></Modal>}
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
