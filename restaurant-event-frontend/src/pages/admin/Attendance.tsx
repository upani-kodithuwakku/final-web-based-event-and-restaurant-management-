import { useEffect, useState } from "react";
import { useApp } from "../../context/AppContext";
import { Modal, SectionHeading } from "../../components/UI";
import {
  attendanceApi,
  staffApi,
  errorMessage,
  type AttendanceDto,
  type AttendanceInput,
  type StaffDto,
  type ShiftDto,
} from "../../services/api";
const blank = (): AttendanceInput => ({
  assignmentId: 0,
  status: "PRESENT",
  checkInAt: null,
  checkOutAt: null,
});
export default function Attendance() {
  const { user } = useApp();
  const allowed = user?.roles.some((r) => ["ADMIN", "MANAGER"].includes(r));
  const [records, setRecords] = useState<AttendanceDto[]>([]);
  const [staff, setStaff] = useState<StaffDto[]>([]);
  const [shifts, setShifts] = useState<ShiftDto[]>([]);
  const [editing, setEditing] = useState<AttendanceDto | "new" | null>(null);
  const [form, setForm] = useState(blank);
  const [error, setError] = useState("");
  const [busy, setBusy] = useState(false);
  const [loading, setLoading] = useState(true);
  const load = async () => {
    setLoading(true);
    try {
      const [a, s, h] = await Promise.all([
        attendanceApi.list(),
        staffApi.list(),
        staffApi.shifts(),
      ]);
      setRecords(a);
      setStaff(s);
      setShifts(h);
      setError("");
    } catch (e) {
      setError(errorMessage(e));
    } finally {
      setLoading(false);
    }
  };
  useEffect(() => {
    if (allowed) void load();
  }, [allowed]);
  const assignments = shifts
    .filter((s) => s.status !== "CANCELLED")
    .flatMap((s) =>
      s.assignments.map((a) => ({
        ...a,
        shift: s,
        name: staff.find((x) => x.id === a.staffId)?.fullName ?? "Staff",
      })),
    );
  if (!allowed)
    return <p role="alert">You do not have permission to manage attendance.</p>;
  return (
    <div className="page-enter">
      <SectionHeading
        eyebrow="STAFF"
        title="Attendance"
        description="Record attendance after an assigned shift starts. Times use Sri Lanka time."
        action={
          <button
            className="button primary"
            disabled={busy}
            onClick={() => {
              setForm(blank());
              setEditing("new");
              setError("");
            }}
          >
            Record attendance
          </button>
        }
      />
      {error && (
        <p className="error" role="alert">
          {error}
        </p>
      )}
      {loading ? (
        <p>Loading attendance…</p>
      ) : !records.length ? (
        <p>No attendance recorded. Create a shift and assign staff first.</p>
      ) : (
        <table>
          <thead>
            <tr>
              <th>Staff</th>
              <th>Shift date</th>
              <th>Status</th>
              <th>Check-in</th>
              <th>Check-out</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            {records.map((row) => (
              <tr key={row.id}>
                <td>{row.staffName}</td>
                <td>{row.shiftDate}</td>
                <td>{row.status}</td>
                <td>{row.checkInAt?.replace("T", " ") ?? "—"}</td>
                <td>{row.checkOutAt?.replace("T", " ") ?? "—"}</td>
                <td>
                  <button
                    className="text-button"
                    disabled={busy}
                    onClick={() => {
                      setForm({
                        assignmentId: row.assignmentId,
                        status: row.status,
                        checkInAt: row.checkInAt ?? null,
                        checkOutAt: row.checkOutAt ?? null,
                      });
                      setEditing(row);
                      setError("");
                    }}
                  >
                    Edit
                  </button>
                  <button
                    className="text-button"
                    disabled={busy}
                    onClick={async () => {
                      if (!window.confirm("Delete this attendance record?"))
                        return;
                      setBusy(true);
                      try {
                        await attendanceApi.remove(row.id);
                        await load();
                      } catch (e) {
                        setError(errorMessage(e));
                      } finally {
                        setBusy(false);
                      }
                    }}
                  >
                    Delete
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
      {editing && (
        <Modal
          title={editing === "new" ? "Record attendance" : "Edit attendance"}
          onClose={() => {
            if (!busy) setEditing(null);
          }}
        >
          <form
            onSubmit={async (e) => {
              e.preventDefault();
              setBusy(true);
              setError("");
              try {
                if (editing === "new") await attendanceApi.create(form);
                else await attendanceApi.update(editing.id, form);
                setEditing(null);
                await load();
              } catch (err) {
                setError(errorMessage(err));
              } finally {
                setBusy(false);
              }
            }}
          >
            <label>
              Shift assignment
              <select
                required
                disabled={editing !== "new"}
                value={form.assignmentId || ""}
                onChange={(e) =>
                  setForm({ ...form, assignmentId: Number(e.target.value) })
                }
              >
                <option value="">Choose an assignment</option>
                {assignments.map((a) => (
                  <option key={a.id} value={a.id}>
                    {a.name} · {a.shift.shiftDate} ·{" "}
                    {a.shift.startTime.slice(0, 5)}–
                    {a.shift.endTime.slice(0, 5)}
                  </option>
                ))}
              </select>
            </label>
            <label>
              Status
              <select
                value={form.status}
                onChange={(e) =>
                  setForm({
                    ...form,
                    status: e.target.value,
                    ...(e.target.value === "ABSENT"
                      ? { checkInAt: null, checkOutAt: null }
                      : {}),
                  })
                }
              >
                {["PRESENT", "ABSENT", "LATE", "HALF_DAY"].map((s) => (
                  <option key={s}>{s}</option>
                ))}
              </select>
            </label>
            {form.status !== "ABSENT" && (
              <>
                <label>
                  Check-in (Sri Lanka time)
                  <input
                    required
                    type="datetime-local"
                    value={form.checkInAt ?? ""}
                    onChange={(e) =>
                      setForm({ ...form, checkInAt: e.target.value || null })
                    }
                  />
                </label>
                <label>
                  Check-out (optional)
                  <input
                    type="datetime-local"
                    value={form.checkOutAt ?? ""}
                    onChange={(e) =>
                      setForm({ ...form, checkOutAt: e.target.value || null })
                    }
                  />
                </label>
              </>
            )}
            {error && (
              <p className="error" role="alert">
                {error}
              </p>
            )}
            <button className="button primary" disabled={busy}>
              {busy ? "Saving…" : "Save attendance"}
            </button>
          </form>
        </Modal>
      )}
    </div>
  );
}
