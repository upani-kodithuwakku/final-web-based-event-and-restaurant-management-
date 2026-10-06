import type { Table } from '../types';

export function filterSavedSpaces(tables: Table[], savedIds: readonly number[], filters: { category: string; guests: number }): Table[] {
  const saved = new Set(savedIds);
  return tables.filter(table => saved.has(table.id)
    && table.currentStatus !== 'OUT_OF_SERVICE'
    && table.isActive !== false
    && table.capacity >= filters.guests
    && (!filters.category || table.location.toUpperCase() === filters.category.toUpperCase()));
}
