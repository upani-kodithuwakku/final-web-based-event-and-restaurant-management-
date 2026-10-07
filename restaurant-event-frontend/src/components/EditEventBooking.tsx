import { useState } from "react";
import { Modal } from "./UI";
import {
  eventApi,
  errorMessage,
  type EventBookingDto,
  type EventHallDto,
  type EventPackageDto,
} from "../services/api";
import { localToday } from "../services/validation";
export default function EditEventBooking({
  booking,
  halls,
  packages,
  onClose,
  onSave,
}: {
  booking: EventBookingDto;
  halls: EventHallDto[];
  packages: EventPackageDto[];
  onClose: () => void;
  onSave: (b: EventBookingDto) => void;
}) {
  const [form, setForm] = useState({
    hallId: booking.hallId,
    packageId: booking.packageId,
    eventDate: booking.eventDate,
    startTime: booking.startTime.slice(0, 5),
    endTime: booking.endTime.slice(0, 5),
    guestCount: booking.guestCount,
    specialRequirements: booking.specialRequirements ?? "",
  });
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState("");
  const save = async () => {
    setError("");
    if (
      form.eventDate <= localToday() ||
      form.endTime <= form.startTime ||
      !Number.isInteger(form.guestCount)
    ) {
      setError(
        "Choose a future date, valid guest count and end time after start.",
      );
      return;
    }
    setBusy(true);
    try {
      onSave(await eventApi.update(booking.id, form));
    } catch (e) {
      setError(errorMessage(e));
    } finally {
      setBusy(false);
    }
  };
  return (
    <Modal
      title={`Edit ${booking.bookingReference}`}
      onClose={() => {
        if (!busy) onClose();
      }}
    >
      <form
        onSubmit={(e) => {
          e.preventDefault();
          void save();
        }}
      >
        <p className="muted">
          Changes need staff confirmation again. Bookings with a payment or
          invoice must be changed through staff.
        </p>
        <label>
          Package
          <select
            required
            value={form.packageId}
            onChange={(e) =>
              setForm({ ...form, packageId: Number(e.target.value) })
            }
          >
            {packages.map((p) => (
              <option key={p.id} value={p.id}>
                {p.name}
              </option>
            ))}
          </select>
        </label>
        <label>
          Hall
          <select
            required
            value={form.hallId}
            onChange={(e) =>
              setForm({ ...form, hallId: Number(e.target.value) })
            }
          >
            {halls.map((h) => (
              <option key={h.id} value={h.id}>
                {h.name} ({h.capacity} guests)
              </option>
            ))}
          </select>
        </label>
        <label>
          Event date
          <input
            required
            type="date"
            min={localToday()}
            value={form.eventDate}
            onChange={(e) => setForm({ ...form, eventDate: e.target.value })}
          />
        </label>
        <label>
          Start
          <input
            required
            type="time"
            value={form.startTime}
            onChange={(e) => setForm({ ...form, startTime: e.target.value })}
          />
        </label>
        <label>
          End
          <input
            required
            type="time"
            value={form.endTime}
            onChange={(e) => setForm({ ...form, endTime: e.target.value })}
          />
        </label>
        <label>
          Guests
          <input
            required
            type="number"
            min={1}
            max={1000}
            value={form.guestCount}
            onChange={(e) =>
              setForm({ ...form, guestCount: Number(e.target.value) })
            }
          />
        </label>
        <label>
          Special requirements
          <textarea
            maxLength={1000}
            value={form.specialRequirements}
            onChange={(e) =>
              setForm({ ...form, specialRequirements: e.target.value })
            }
          />
        </label>
        {error && (
          <p role="alert" className="error">
            {error}
          </p>
        )}
        <button className="button primary" disabled={busy}>
          {busy ? "Saving…" : "Save enquiry"}
        </button>
      </form>
    </Modal>
  );
}
