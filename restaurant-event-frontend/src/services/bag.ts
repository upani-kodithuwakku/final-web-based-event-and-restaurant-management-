export type Bag = Record<number, number>;
export function changeQuantity(bag: Bag, id: number, delta: number): Bag {
  const next = { ...bag };
  const quantity = Math.max(0, Math.min(99, (next[id] || 0) + delta));
  if (quantity) next[id] = quantity;
  else delete next[id];
  return next;
}
export function readBag(key: string): Bag {
  try {
    const value: unknown = JSON.parse(sessionStorage.getItem(key) || '{}');
    if (!value || typeof value !== 'object' || Array.isArray(value)) return {};
    return Object.fromEntries(Object.entries(value).filter(([id, n]) =>
      Number.isInteger(Number(id)) && Number(id) > 0 && typeof n === 'number' && Number.isInteger(n) && n > 0 && n <= 99));
  } catch { return {}; }
}
