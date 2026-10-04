import { useState, useEffect } from 'react';
import { format } from 'date-fns';
import { PlusIcon, UserGroupIcon, KeyIcon } from '@heroicons/react/24/outline';
import { Modal, SectionHeading, Badge, Empty } from '../../components/UI';
import { staffApi, type StaffDto, type ShiftDto, errorMessage } from '../../services/api';

const TODAY = format(new Date(), 'yyyy-MM-dd');
const STAFF_ROLES = ['ADMIN', 'MANAGER', 'WAITER', 'KITCHEN_STAFF', 'EVENT_COORDINATOR', 'CASHIER', 'INVENTORY_MANAGER'];
const EMP_STATUSES = ['FULL_TIME', 'PART_TIME', 'CONTRACT'];

const BLANK_USER = { fullName: '', email: '', password: '', phone: '', roles: [] as string[], jobTitle: '', employmentStatus: 'FULL_TIME' };

export default function AdminStaff() {
  const [staff, setStaff] = useState<StaffDto[]>([]);
  const [shifts, setShifts] = useState<ShiftDto[]>([]);
  const [tab, setTab] = useState<'staff' | 'shifts'>('staff');
  const [date, setDate] = useState(TODAY);
  const [loading, setLoading] = useState(true);
  const [staffLoading, setStaffLoading] = useState(true);
  const [staffError, setStaffError] = useState('');
  const [modal, setModal] = useState<'shift' | 'add-staff' | 'reset-pw' | null>(null);
  const [shiftForm, setShiftForm] = useState({ shiftDate: TODAY, startTime: '10:00', endTime: '18:00', roleRequired: 'WAITER', requiredStaffCount: 2 });
  const [staffForm, setStaffForm] = useState(BLANK_USER);
  const [resetTarget, setResetTarget] = useState<StaffDto | null>(null);
  const [resetPw, setResetPw] = useState('');
  const [busy, setBusy] = useState(false);
  const [err, setErr] = useState('');

  const loadStaff = async () => {
    setStaffLoading(true);
    setStaffError('');
    try { setStaff(await staffApi.list()); }
    catch (e) { setStaffError(errorMessage(e)); }
    finally { setStaffLoading(false); }
  };

  const loadShifts = async () => {
    setLoading(true);
    try { setShifts(await staffApi.shifts(date)); }
    catch { setErr('Failed to load shifts.'); }
    finally { setLoading(false); }
  };

  useEffect(() => { void loadStaff(); }, []);
  useEffect(() => { void loadShifts(); }, [date]);

  const createStaffUser = async () => {
    if (!staffForm.fullName || !staffForm.email || !staffForm.password) { setErr('Full name, email, and password are required.'); return; }
    if (staffForm.password.length < 8) { setErr('Password must be at least 8 characters.'); return; }
    if (staffForm.roles.length === 0) { setErr('Select at least one role.'); return; }
    setBusy(true); setErr('');
    try {
      const created = await staffApi.createUser(staffForm);
      setStaff(all => [created, ...all]);
      setModal(null); setStaffForm(BLANK_USER);
    } catch (e) { setErr(errorMessage(e)); }
    finally { setBusy(false); }
  };

  const createShift = async () => {
    setBusy(true); setErr('');
    try {
      const created = await staffApi.createShift(shiftForm);
      setShifts(all => [created, ...all]);
      setModal(null);
    } catch (e) { setErr(errorMessage(e)); }
    finally { setBusy(false); }
  };

  const doResetPw = async () => {
    if (!resetTarget || resetPw.length < 8) { setErr('Password must be at least 8 characters.'); return; }
    setBusy(true); setErr('');
    try {
      await staffApi.resetPassword(resetTarget.id, resetPw);
      setModal(null); setResetPw(''); setResetTarget(null);
    } catch (e) { setErr(errorMessage(e)); }
    finally { setBusy(false); }
  };

  const toggleAssign = async (shift: ShiftDto, staffId: number) => {
    const already = shift.assignments.some(a => a.staffId === staffId);
    try {
      if (already) {
        await staffApi.unassign(shift.id, staffId);
        setShifts(all => all.map(s => s.id === shift.id ? { ...s, assignments: s.assignments.filter(a => a.staffId !== staffId) } : s));
      } else {
        await staffApi.assign(shift.id, staffId);
        await loadShifts();
      }
    } catch (e) { setErr(errorMessage(e)); }
  };

  const toggleRole = (role: string) => {
    setStaffForm(f => ({
      ...f,
      roles: f.roles.includes(role) ? f.roles.filter(r => r !== role) : [...f.roles, role],
    }));
  };

  return (
    <div className="page-enter">
      <SectionHeading
        eyebrow="STAFF MANAGEMENT"
        title="Team & scheduling"
        description="Create staff accounts, assign shifts, and manage your team."
        action={
          <div style={{ display: 'flex', gap: 8 }}>
            {tab === 'staff' && (
              <button className="button primary" onClick={() => { setStaffForm(BLANK_USER); setErr(''); setModal('add-staff'); }}>
                <PlusIcon style={{ width: 14, height: 14 }} /> Add Staff Member
              </button>
            )}
            {tab === 'shifts' && (
              <button className="button primary" onClick={() => { setShiftForm({ shiftDate: date, startTime: '10:00', endTime: '18:00', roleRequired: 'WAITER', requiredStaffCount: 2 }); setModal('shift'); }}>
                <PlusIcon style={{ width: 14, height: 14 }} /> Add Shift
              </button>
            )}
          </div>
        }
      />

      <div className="tabs">
        <button className={tab === 'staff' ? 'active' : ''} onClick={() => setTab('staff')}>
          <UserGroupIcon style={{ width: 14, height: 14, display: 'inline', marginRight: 6 }} />
          Staff ({staff.filter(s => s.isActive).length})
        </button>
        <button className={tab === 'shifts' ? 'active' : ''} onClick={() => setTab('shifts')}>Shift schedule</button>
      </div>

      {err && <p className="error">{err}</p>}

      {tab === 'staff' && (
        <div className="staff-grid" style={{ marginTop: 24 }}>
          {staffLoading && <div className="skeleton" style={{ height: 120 }} />}
          {staffError && (
            <div role="alert">
              <p className="error">Failed to load staff: {staffError}</p>
              <button className="button" disabled={staffLoading} onClick={() => void loadStaff()}>Retry</button>
            </div>
          )}
          {staff.filter(s => s.isActive).map(s => (
            <div key={s.id} className="staff-card">
              <div className="staff-avatar">{(s.fullName || s.employeeCode).slice(0, 2).toUpperCase()}</div>
              <h3>{s.fullName || s.employeeCode}</h3>
              <p className="small">{s.jobTitle || '—'}</p>
              <p className="small muted">{s.email}</p>
              {s.roles && s.roles.length > 0 && (
                <div style={{ display: 'flex', flexWrap: 'wrap', gap: 4, marginTop: 4 }}>
                  {s.roles.map(r => <Badge key={r} status={r} />)}
                </div>
              )}
              <Badge status={s.isActive ? 'ACTIVE' : 'INACTIVE'} />
              <button className="text-button" style={{ fontSize: 12, marginTop: 8 }} onClick={() => { setResetTarget(s); setResetPw(''); setErr(''); setModal('reset-pw'); }}>
                <KeyIcon style={{ width: 12, height: 12, display: 'inline', marginRight: 4 }} />Reset password
              </button>
            </div>
          ))}
          {!staffLoading && !staffError && staff.filter(s => s.isActive).length === 0 && (
            <Empty title="No active staff members">
              <p>Click <b>Add Staff Member</b> to create the first account.</p>
            </Empty>
          )}
        </div>
      )}

      {tab === 'shifts' && (
        <div style={{ marginTop: 24 }}>
          <div style={{ display: 'flex', gap: 12, marginBottom: 20, alignItems: 'center' }}>
            <label style={{ flexDirection: 'row', alignItems: 'center', gap: 8, textTransform: 'none', letterSpacing: 0, fontSize: 14, fontWeight: 600 }}>
              Date:
              <input type="date" value={date} onChange={e => setDate(e.target.value)}
                style={{ border: '1.5px solid var(--gray-200)', borderRadius: 'var(--radius-sm)', padding: '7px 12px', fontSize: 14, background: 'var(--white)' }} />
            </label>
          </div>
          {loading ? <div className="skeleton" style={{ height: 120 }} /> : shifts.length === 0 ? (
            <Empty title={`No shifts for ${date}`}><p>Create a shift to start scheduling.</p></Empty>
          ) : (
            <div className="shift-list">
              {shifts.map(shift => (
                <div key={shift.id} className="shift-row">
                  <span className="shift-time">{String(shift.startTime).slice(0, 5)} – {String(shift.endTime).slice(0, 5)}</span>
                  <div className="shift-body">
                    <p>{shift.roleRequired.replace('_', ' ').toLowerCase()} · {shift.requiredStaffCount} needed</p>
                    <span>{shift.assignments.length > 0 ? `${shift.assignments.length} assigned` : 'Unassigned'}</span>
                  </div>
                  <Badge status={shift.status} />
                  <div style={{ display: 'flex', gap: 6, flexWrap: 'wrap' }}>
                    {staff.filter(s => s.isActive).map(s => (
                      <button
                        key={s.id}
                        onClick={() => toggleAssign(shift, s.id)}
                        title={s.fullName || s.employeeCode}
                        style={{
                          padding: '4px 10px', borderRadius: 9999, fontSize: 12, fontWeight: 600, border: '1.5px solid',
                          borderColor: shift.assignments.some(a => a.staffId === s.id) ? 'var(--babu)' : 'var(--gray-200)',
                          background: shift.assignments.some(a => a.staffId === s.id) ? 'var(--babu-light)' : 'var(--white)',
                          color: shift.assignments.some(a => a.staffId === s.id) ? 'var(--babu)' : 'var(--hof)',
                        }}
                      >
                        {(s.fullName || s.employeeCode).slice(0, 6)}
                      </button>
                    ))}
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      )}

      {/* Add Staff Member Modal */}
      {modal === 'add-staff' && (
        <Modal title="Add staff member" onClose={() => setModal(null)}>
          <div className="input-group">
            <div className="form-row">
              <label>Full name *<input value={staffForm.fullName} onChange={e => setStaffForm({ ...staffForm, fullName: e.target.value })} placeholder="Jane Smith" /></label>
              <label>Email address *<input type="email" value={staffForm.email} onChange={e => setStaffForm({ ...staffForm, email: e.target.value })} placeholder="jane@gather.com" /></label>
            </div>
            <div className="form-row">
              <label>Temporary password *<input type="password" value={staffForm.password} onChange={e => setStaffForm({ ...staffForm, password: e.target.value })} placeholder="Min. 8 characters" /></label>
              <label>Phone<input type="tel" value={staffForm.phone} onChange={e => setStaffForm({ ...staffForm, phone: e.target.value })} placeholder="+94 77 …" /></label>
            </div>
            <div className="form-row">
              <label>Job title<input value={staffForm.jobTitle} onChange={e => setStaffForm({ ...staffForm, jobTitle: e.target.value })} placeholder="e.g. Senior Waiter" /></label>
              <label>Employment status
                <select value={staffForm.employmentStatus} onChange={e => setStaffForm({ ...staffForm, employmentStatus: e.target.value })}>
                  {EMP_STATUSES.map(s => <option key={s} value={s}>{s.replace('_', ' ')}</option>)}
                </select>
              </label>
            </div>
            <div>
              <label style={{ display: 'block', marginBottom: 8 }}>Roles * (select all that apply)</label>
              <div style={{ display: 'flex', flexWrap: 'wrap', gap: 8 }}>
                {STAFF_ROLES.map(r => (
                  <button
                    key={r}
                    type="button"
                    onClick={() => toggleRole(r)}
                    style={{
                      padding: '6px 14px', borderRadius: 9999, fontSize: 13, fontWeight: 600, border: '1.5px solid', cursor: 'pointer',
                      borderColor: staffForm.roles.includes(r) ? 'var(--babu)' : 'var(--gray-200)',
                      background: staffForm.roles.includes(r) ? 'var(--babu-light)' : 'var(--white)',
                      color: staffForm.roles.includes(r) ? 'var(--babu)' : 'var(--hof)',
                    }}
                  >
                    {r.replace(/_/g, ' ')}
                  </button>
                ))}
              </div>
            </div>
            {err && <p className="error">{err}</p>}
            <button className="button primary full" disabled={busy} onClick={createStaffUser}>
              {busy ? 'Creating account…' : 'Create staff account'}
            </button>
          </div>
        </Modal>
      )}

      {/* Create Shift Modal */}
      {modal === 'shift' && (
        <Modal title="Schedule a shift" onClose={() => setModal(null)}>
          <div className="input-group">
            <label>Date<input type="date" value={shiftForm.shiftDate} onChange={e => setShiftForm({ ...shiftForm, shiftDate: e.target.value })} /></label>
            <div className="form-row">
              <label>Start time<input type="time" value={shiftForm.startTime} onChange={e => setShiftForm({ ...shiftForm, startTime: e.target.value })} /></label>
              <label>End time<input type="time" value={shiftForm.endTime} onChange={e => setShiftForm({ ...shiftForm, endTime: e.target.value })} /></label>
            </div>
            <div className="form-row">
              <label>Role required
                <select value={shiftForm.roleRequired} onChange={e => setShiftForm({ ...shiftForm, roleRequired: e.target.value })}>
                  {STAFF_ROLES.map(r => <option key={r} value={r}>{r.replace(/_/g, ' ')}</option>)}
                </select>
              </label>
              <label>Staff needed<input type="number" min={1} max={20} value={shiftForm.requiredStaffCount} onChange={e => setShiftForm({ ...shiftForm, requiredStaffCount: Number(e.target.value) })} /></label>
            </div>
            {err && <p className="error">{err}</p>}
            <button className="button primary full" disabled={busy} onClick={createShift}>{busy ? 'Creating…' : 'Create shift'}</button>
          </div>
        </Modal>
      )}

      {/* Reset Password Modal */}
      {modal === 'reset-pw' && resetTarget && (
        <Modal title={`Reset password — ${resetTarget.fullName || resetTarget.employeeCode}`} onClose={() => setModal(null)}>
          <div className="input-group">
            <p className="muted small">Set a new temporary password for this staff member. They should change it after next login.</p>
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
