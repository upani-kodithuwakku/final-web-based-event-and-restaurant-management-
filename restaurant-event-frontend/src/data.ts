import { addDays, format } from 'date-fns';
import type { Table, MenuItem, Reservation } from './types';
export const photo = (id: string, width = 900) => `https://images.unsplash.com/${id}?auto=format&fit=crop&w=${width}&q=85`;
export const images = { hero: photo('photo-1517248135467-4c7edcad34c4', 1800), garden: '/images/occasions/birthday-garden.jpg', window: photo('photo-1552566626-52f8b828add9'), indoor: photo('photo-1517248135467-4c7edcad34c4'), terrace: photo('photo-1414235077428-338989a2e8c0'), event: photo('photo-1511795409834-ef04bbd61622'), private: '/images/occasions/birthday-garden.jpg',
  // Event-type specific photos
  gala:        photo('photo-1549451371-64aa98a6f660'),
  christmas:   photo('photo-1512389142860-9c449e58a543'),
  newYear:     photo('photo-1514525253161-7a46d19cd819'),
  reception:   photo('photo-1530103862676-de8c9debad1d'),
  graduation:  '/images/occasions/graduation.jpg',
  corporate:   photo('photo-1517048676732-d65bc937f952'),
  babyShower:  photo('photo-1519689680058-324335c77eba'),
  farewell:    photo('photo-1540575467063-178a50c2df87'),
  teamBuilding:photo('photo-1522202176988-66273c2fd55f'),
  pavilion:    photo('photo-1567620905732-2d1ec7ab7445'),
};

export const tomorrow = () => format(addDays(new Date(), 1), 'yyyy-MM-dd');
export const tables: Table[] = Array.from({ length: 10 }, (_, i) => ({ id: i + 1, tableNumber: `T${String(i + 1).padStart(2, '0')}`, capacity: [2, 4, 4, 6, 2, 8, 4, 6, 4, 12][i], location: ['GARDEN', 'WINDOW', 'INDOOR', 'OUTDOOR', 'WINDOW', 'PRIVATE', 'GARDEN', 'INDOOR', 'OUTDOOR', 'PRIVATE'][i], currentStatus: i === 8 ? 'OUT_OF_SERVICE' : 'AVAILABLE', isActive: true }));
export const tableImage = (location: string, table?: Table) => table?.imageUrl || ({ GARDEN: images.garden, WINDOW: images.window, INDOOR: images.indoor, OUTDOOR: images.terrace, PRIVATE: images.private }[location.toUpperCase()] || images.indoor);
export const tableTitle = (location: string, table?: Table) => table?.displayName || ({ GARDEN: 'The garden hideaway', WINDOW: 'A seat with a view', INDOOR: 'The dining room', OUTDOOR: 'Under the open sky', PRIVATE: 'Your own little space' }[location.toUpperCase()] || `${location} dining`);
export const seedReservations: Reservation[] = [{ id: 1, bookingReference: 'RES-DEMO-001', table: tables[1], reservationDate: tomorrow(), startTime: '19:00', guestCount: 2, status: 'CONFIRMED', contactName: 'Alex Morgan', contactPhone: '0771234567', specialRequest: 'A quiet table by the window, please.' }];
export const menu: MenuItem[] = [
{ id: 1, name: 'Burrata & heirloom tomatoes', description: 'Creamy burrata, garden basil, aged balsamic', category: 'Starters', price: 2400, image: photo('photo-1608897013039-887f21d8c804'), tag: 'Vegetarian' },
{ id: 2, name: 'Wood-fired margherita', description: 'San Marzano tomatoes, mozzarella, fresh basil', category: 'Mains', price: 3200, image: photo('photo-1579751626657-72bc17010498'), tag: 'Guest favorite' },
{ id: 3, name: 'Grilled salmon bowl', description: 'Atlantic salmon, seasonal greens, lemon dressing', category: 'Mains', price: 4800, image: photo('photo-1467003909585-2f8a72700288') },
{ id: 4, name: 'The Gather burger', description: 'Grilled beef, aged cheddar, house sauce, fries', category: 'Mains', price: 3600, image: photo('photo-1568901346375-23c9450c58cd'), tag: 'Chef’s pick' },
{ id: 5, name: 'Chocolate indulgence', description: 'Warm chocolate cake with vanilla ice cream', category: 'Desserts', price: 1800, image: photo('photo-1578985545062-69928b1d9587') },
{ id: 6, name: 'Garden citrus cooler', description: 'Fresh lime, mint, sparkling water', category: 'Drinks', price: 950, image: photo('photo-1513558161293-cdaf765edfd7'), tag: 'Refreshing' }
];
export const money = (amount: number) => `LKR ${amount.toLocaleString('en-LK')}`;
