import { useState } from "react";
import { Modal } from "./UI";
import { orderApi, errorMessage, type OrderDto } from "../services/api";
export default function EditOrder({
  order,
  onSave,
  onClose,
}: {
  order: OrderDto;
  onSave: (o: OrderDto) => void;
  onClose: () => void;
}) {
  const [lines, setLines] = useState(
    order.items.map((i) => ({
      menuItemId: i.menuItemId,
      quantity: i.quantity,
      specialNote: i.specialNote ?? "",
      name: i.itemNameSnapshot,
    })),
  );
  const [note, setNote] = useState(order.specialNote ?? "");
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState("");
  return (
    <Modal
      title={`Edit ${order.orderReference}`}
      onClose={() => {
        if (!busy) onClose();
      }}
    >
      <form
        onSubmit={async (e) => {
          e.preventDefault();
          setBusy(true);
          setError("");
          try {
            onSave(
              await orderApi.update(order.id, {
                orderType: order.orderType,
                tableId: order.tableId,
                reservationId: order.reservationId,
                specialNote: note,
                items: lines.map(({ menuItemId, quantity, specialNote }) => ({
                  menuItemId,
                  quantity,
                  specialNote,
                })),
              }),
            );
          } catch (err) {
            setError(errorMessage(err));
          } finally {
            setBusy(false);
          }
        }}
      >
        <p className="muted">
          Only pending orders without a payment or invoice can be changed.
        </p>
        {lines.map((line, index) => (
          <div key={line.menuItemId}>
            <label>
              {line.name}
              <input
                required
                type="number"
                min={1}
                max={99}
                value={line.quantity}
                onChange={(e) =>
                  setLines((all) =>
                    all.map((x, i) =>
                      i === index
                        ? { ...x, quantity: Number(e.target.value) }
                        : x,
                    ),
                  )
                }
              />
            </label>
            <button
              type="button"
              className="text-button"
              disabled={busy || lines.length === 1}
              onClick={() =>
                setLines((all) => all.filter((_, i) => i !== index))
              }
            >
              Remove item
            </button>
          </div>
        ))}
        <label>
          Notes
          <textarea
            maxLength={500}
            value={note}
            onChange={(e) => setNote(e.target.value)}
          />
        </label>
        {error && (
          <p className="error" role="alert">
            {error}
          </p>
        )}
        <button className="button primary" disabled={busy}>
          {busy ? "Saving…" : "Save order"}
        </button>
      </form>
    </Modal>
  );
}
