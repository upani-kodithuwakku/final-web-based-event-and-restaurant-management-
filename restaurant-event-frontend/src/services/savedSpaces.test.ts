import { describe, expect, it } from 'vitest';
import { filterSavedSpaces } from './savedSpaces';
import type { Table } from '../types';
const tables: Table[] = [
  { id: 1, tableNumber: 'T1', capacity: 2, location: 'WINDOW', currentStatus: 'AVAILABLE', isActive: true },
  { id: 2, tableNumber: 'T2', capacity: 6, location: 'GARDEN', currentStatus: 'AVAILABLE', isActive: true },
  { id: 3, tableNumber: 'T3', capacity: 6, location: 'WINDOW', currentStatus: 'OUT_OF_SERVICE', isActive: true },
  { id: 4, tableNumber: 'T4', capacity: 6, location: 'WINDOW', currentStatus: 'AVAILABLE', isActive: true },
];
describe('saved spaces filters', () => {
  it('uses all saved tables without an availability search', () => {
    expect(filterSavedSpaces(tables, [1, 2, 3], { category: '', guests: 2 }).map(t => t.id)).toEqual([1, 2]);
  });
  it('filters capacity', () => {
    expect(filterSavedSpaces(tables, [1, 2], { category: '', guests: 4 }).map(t => t.id)).toEqual([2]);
  });
  it('filters category', () => {
    expect(filterSavedSpaces(tables, [1, 2], { category: 'window', guests: 2 }).map(t => t.id)).toEqual([1]);
  });
  it('excludes out-of-service and not-saved tables', () => {
    expect(filterSavedSpaces(tables, [3], { category: '', guests: 1 })).toEqual([]);
  });
  it('excludes inactive tables', () => {
    expect(filterSavedSpaces([{ ...tables[0], isActive: false }], [1], { category: '', guests: 1 })).toEqual([]);
  });
});
