import { describe, expect, it } from 'vitest';
import { normalizeEvent } from './eventCatalog';

describe('event catalogue API compatibility', () => {
  it('keeps active packages returned by the running API visible', () => {
    const rows = [{ id: 1, active: true }, { id: 2, active: false }];
    expect(rows.map(normalizeEvent).filter(row => row.isActive).map(row => row.id)).toEqual([1]);
  });
  it('also accepts the explicit isActive field without overriding false', () => {
    expect(normalizeEvent({ isActive: false, active: true }).isActive).toBe(false);
    expect(normalizeEvent({ isActive: true }).isActive).toBe(true);
  });
});
