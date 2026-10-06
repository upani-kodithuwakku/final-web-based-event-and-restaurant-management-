import { describe, expect, it, vi, afterEach } from 'vitest';
import { changeQuantity, readBag } from './bag';
afterEach(() => vi.unstubAllGlobals());
describe('customer bag', () => {
  it('adds, removes the final unit, and leaves the previous bag available for undo', () => {
    const original = { 7: 1 };
    expect(changeQuantity(original, 7, 1)).toEqual({ 7: 2 });
    expect(changeQuantity(original, 7, -1)).toEqual({});
    expect(original).toEqual({ 7: 1 });
    expect(changeQuantity({}, 7, -1)).toEqual({});
    expect(changeQuantity({ 7: 99 }, 7, 1)).toEqual({ 7: 99 });
  });
  it('isolates saved bags by account and discards invalid stored quantities', () => {
    vi.stubGlobal('sessionStorage', { getItem: (key: string) => key === 'customer-1' ? '{"7":2,"8":-1,"9":100,"10":"2"}' : null });
    expect(readBag('customer-1')).toEqual({ 7: 2 });
    expect(readBag('customer-2')).toEqual({});
  });
  it('handles corrupt storage', () => {
    vi.stubGlobal('sessionStorage', { getItem: () => '{' });
    expect(readBag('customer')).toEqual({});
  });
});
