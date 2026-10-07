import { useEffect, useState } from "react";
import { useApp } from "../../context/AppContext";
import { Modal, SectionHeading } from "../../components/UI";
import {
  purchaseOrderApi,
  inventoryApi,
  supplierApi,
  errorMessage,
  type PurchaseOrderDto,
  type PurchaseOrderInput,
  type SupplierDto,
  type InventoryItemDto,
} from "../../services/api";
import { money } from "../../data";
const blank = (): PurchaseOrderInput => ({
  supplierId: 0,
  notes: "",
  items: [{ inventoryItemId: 0, quantity: 1, unitCost: 1 }],
});
export default function PurchaseOrders() {
  const { user } = useApp();
  const allowed = user?.roles.some((r) =>
    ["ADMIN", "MANAGER", "INVENTORY_MANAGER"].includes(r),
  );
  const [orders, setOrders] = useState<PurchaseOrderDto[]>([]);
  const [suppliers, setSuppliers] = useState<SupplierDto[]>([]);
  const [stock, setStock] = useState<InventoryItemDto[]>([]);
  const [editing, setEditing] = useState<PurchaseOrderDto | "new" | null>(null);
  const [form, setForm] = useState(blank);
  const [error, setError] = useState("");
  const [busy, setBusy] = useState(false);
  const [loading, setLoading] = useState(true);
  const load = async () => {
    setLoading(true);
    try {
      const [p, s, i] = await Promise.all([
        purchaseOrderApi.list(),
        supplierApi.list(),
        inventoryApi.list(),
      ]);
      setOrders(p);
      setSuppliers(s.filter((x) => x.active));
      setStock(i);
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
  const action = async (work: () => Promise<unknown>) => {
    setBusy(true);
    setError("");
    try {
      await work();
      await load();
    } catch (e) {
      setError(errorMessage(e));
    } finally {
      setBusy(false);
    }
  };
  if (!allowed)
    return <p role="alert">You do not have permission to manage purchases.</p>;
  return (
    <div className="page-enter">
      <SectionHeading
        eyebrow="SUPPLY"
        title="Purchase orders"
        description="Order stock from a supplier, then receive the delivery once to update stock."
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
            Add purchase order
          </button>
        }
      />
      {error && (
        <p className="error" role="alert">
          {error}
        </p>
      )}
      {loading ? (
        <p>Loading purchase orders…</p>
      ) : !orders.length ? (
        <p>No purchase orders yet. Add a supplier and inventory items first.</p>
      ) : (
        orders.map((order) => (
          <article
            className="report-card"
            key={order.id}
            style={{ marginBottom: 16 }}
          >
            <h2>{order.poNumber}</h2>
            <p>
              {order.supplierName} · {order.status}
            </p>
            <p className="small muted">
              Ordered {order.orderedAt.replace("T", " ")}
              {order.receivedAt
                ? ` · Received ${order.receivedAt.replace("T", " ")}`
                : ""}
            </p>
            <table>
              <thead>
                <tr>
                  <th>Item</th>
                  <th>Ordered</th>
                  <th>Received</th>
                  <th>Unit cost</th>
                </tr>
              </thead>
              <tbody>
                {order.items.map((line) => (
                  <tr key={line.inventoryItemId}>
                    <td>{line.itemName}</td>
                    <td>{line.quantityOrdered}</td>
                    <td>{line.quantityReceived}</td>
                    <td>{money(line.unitCost)}</td>
                  </tr>
                ))}
              </tbody>
            </table>
            <p>{order.notes}</p>
            {order.status === "PENDING" && (
              <div className="button-row">
                <button
                  className="button"
                  disabled={busy}
                  onClick={() => {
                    setForm({
                      supplierId: order.supplierId,
                      notes: order.notes ?? "",
                      items: order.items.map((i) => ({
                        inventoryItemId: i.inventoryItemId,
                        quantity: i.quantityOrdered,
                        unitCost: i.unitCost,
                      })),
                    });
                    setEditing(order);
                  }}
                >
                  Edit
                </button>
                <button
                  className="button primary"
                  disabled={busy}
                  onClick={() => {
                    if (
                      window.confirm(
                        "Receive the complete delivery? Stock will be increased for every item.",
                      )
                    )
                      void action(() => purchaseOrderApi.receive(order.id));
                  }}
                >
                  Receive delivery
                </button>
                <button
                  className="text-button"
                  disabled={busy}
                  onClick={() => {
                    if (window.confirm("Cancel this purchase order?"))
                      void action(() => purchaseOrderApi.cancel(order.id));
                  }}
                >
                  Cancel
                </button>
              </div>
            )}
          </article>
        ))
      )}
      {editing && (
        <Modal
          title={
            editing === "new" ? "Add purchase order" : "Edit purchase order"
          }
          onClose={() => {
            if (!busy) setEditing(null);
          }}
        >
          <form
            onSubmit={(e) => {
              e.preventDefault();
              void action(async () => {
                if (
                  !form.supplierId ||
                  form.items.some(
                    (i) =>
                      !i.inventoryItemId || i.quantity <= 0 || i.unitCost <= 0,
                  )
                )
                  throw new Error(
                    "Choose supplier, stock items and positive quantities/costs.",
                  );
                if (editing === "new") await purchaseOrderApi.create(form);
                else await purchaseOrderApi.update(editing.id, form);
                setEditing(null);
              });
            }}
          >
            <label>
              Supplier
              <select
                required
                value={form.supplierId || ""}
                onChange={(e) =>
                  setForm({ ...form, supplierId: Number(e.target.value) })
                }
              >
                <option value="">Choose supplier</option>
                {suppliers.map((s) => (
                  <option key={s.id} value={s.id}>
                    {s.name}
                  </option>
                ))}
              </select>
            </label>
            {form.items.map((line, index) => (
              <fieldset key={index}>
                <legend>Stock item {index + 1}</legend>
                <label>
                  Item
                  <select
                    required
                    value={line.inventoryItemId || ""}
                    onChange={(e) =>
                      setForm({
                        ...form,
                        items: form.items.map((i, n) =>
                          n === index
                            ? { ...i, inventoryItemId: Number(e.target.value) }
                            : i,
                        ),
                      })
                    }
                  >
                    <option value="">Choose stock item</option>
                    {stock.map((i) => (
                      <option key={i.id} value={i.id}>
                        {i.name} ({i.unit})
                      </option>
                    ))}
                  </select>
                </label>
                <label>
                  Quantity
                  <input
                    required
                    type="number"
                    min="0.001"
                    max="999999999.999"
                    step="0.001"
                    value={line.quantity}
                    onChange={(e) =>
                      setForm({
                        ...form,
                        items: form.items.map((i, n) =>
                          n === index
                            ? { ...i, quantity: Number(e.target.value) }
                            : i,
                        ),
                      })
                    }
                  />
                </label>
                <label>
                  Unit cost (LKR)
                  <input
                    required
                    type="number"
                    min="0.01"
                    max="9999999999.99"
                    step="0.01"
                    value={line.unitCost}
                    onChange={(e) =>
                      setForm({
                        ...form,
                        items: form.items.map((i, n) =>
                          n === index
                            ? { ...i, unitCost: Number(e.target.value) }
                            : i,
                        ),
                      })
                    }
                  />
                </label>
                <button
                  type="button"
                  className="text-button"
                  disabled={form.items.length === 1 || busy}
                  onClick={() =>
                    setForm({
                      ...form,
                      items: form.items.filter((_, n) => n !== index),
                    })
                  }
                >
                  Remove line
                </button>
              </fieldset>
            ))}
            <button
              type="button"
              className="button"
              disabled={busy || form.items.length >= 100}
              onClick={() =>
                setForm({
                  ...form,
                  items: [
                    ...form.items,
                    { inventoryItemId: 0, quantity: 1, unitCost: 1 },
                  ],
                })
              }
            >
              Add another item
            </button>
            <label>
              Notes
              <textarea
                maxLength={500}
                value={form.notes}
                onChange={(e) => setForm({ ...form, notes: e.target.value })}
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
      )}
    </div>
  );
}
