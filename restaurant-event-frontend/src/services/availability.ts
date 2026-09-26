import type { Reservation, Table } from '../types';
export const activeStatuses = ['PENDING', 'CONFIRMED', 'CHECKED_IN'];
export function availableTables(tables: Table[], reservations: Reservation[], date: string, time: string, guests: number, excludeId?: number) {
 const start = new Date(`${date}T${time}`).getTime();
 if (!Number.isFinite(start) || start <= Date.now() || guests < 1) return [];
 return tables.filter(t => t.isActive && t.capacity >= guests && t.currentStatus !== 'OUT_OF_SERVICE' && !reservations.some(r => r.id !== excludeId && r.table.id === t.id && activeStatuses.includes(r.status) && Math.abs(new Date(`${r.reservationDate}T${r.startTime}`).getTime() - start) < 120 * 60000));
}
