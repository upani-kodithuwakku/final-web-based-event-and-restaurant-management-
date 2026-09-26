import { createContext, useContext, useEffect, useState, type ReactNode } from 'react';
import { reservationApi } from '../services/api';
import type { BookingInput, EventBooking, Order, Reservation, Table, User } from '../types';
import toast from 'react-hot-toast';

function read<T>(key: string, fallback: T): T { try { return JSON.parse(localStorage.getItem(key) || 'null') ?? fallback; } catch { return fallback; } }
export function useStored<T>(key: string, fallback: T) { const [value, setValue] = useState<T>(() => read(key, fallback)); useEffect(() => { localStorage.setItem(key, JSON.stringify(value)); }, [key, value]); return [value, setValue] as const; }

const STAFF_ROLES = ['ADMIN', 'MANAGER', 'WAITER', 'KITCHEN_STAFF', 'EVENT_COORDINATOR', 'CASHIER', 'INVENTORY_MANAGER'];

function useAppState() {
  const [user, setUser] = useState<User | null>(() => { try { return JSON.parse(sessionStorage.getItem('gather-user') || 'null'); } catch { return null; } });
  const [reservations, setReservations] = useState<Reservation[]>([]);
  const [tables, setTables] = useState<Table[]>([]);
  const [orders, setOrders] = useState<Order[]>([]);
  const [events, setEvents] = useState<EventBooking[]>([]);
  const [saved, setSaved] = useStored<number[]>('gather-saved', []);
  const [notifications, setNotifications] = useStored<string[]>('gather-notifications', ['Welcome to Gather. Your next good moment starts here.']);
  const [loading, setLoading] = useState(false);
  const [loadError, setLoadError] = useState('');

  const login = (next: User) => {
    setUser(next);
    sessionStorage.setItem('gather-user', JSON.stringify(next));
    if (next.token) sessionStorage.setItem('gather-token', next.token);
  };

  const logout = () => {
    setUser(null);
    setReservations([]);
    setTables([]);
    sessionStorage.removeItem('gather-user');
    sessionStorage.removeItem('gather-token');
  };

  const notify = (message: string) => { setNotifications(n => [message, ...n]); toast.success(message); };

  const refresh = async () => {
    if (!user) return;
    setLoading(true); setLoadError('');
    try {
      if (user.roles.some(r => STAFF_ROLES.includes(r))) {
        setTables(await reservationApi.tables());
      } else {
        setReservations(await reservationApi.mine());
      }
    } catch { setLoadError('Could not load your data. Please retry.'); }
    finally { setLoading(false); }
  };

  useEffect(() => { void refresh(); }, [user?.userId]);

  const saveBooking = async (input: BookingInput, id?: number) => {
    const result = await reservationApi.save(input, id);
    setReservations(all => id ? all.map(r => r.id === id ? result : r) : [result, ...all]);
    notify(id ? 'Your reservation has been updated.' : 'Your table is reserved. See you at Gather!');
    return result;
  };

  const cancelBooking = async (id: number, reason: string) => {
    const result = await reservationApi.cancel(id, reason);
    setReservations(all => all.map(r => r.id === id ? result : r));
    notify('Reservation cancelled. We hope to see you soon.');
  };

  return { user, login, logout, reservations, setReservations, tables, setTables, orders, setOrders, events, setEvents, saved, setSaved, notifications, setNotifications, notify, saveBooking, cancelBooking, loading, loadError, refresh };
}

const AppContext = createContext<ReturnType<typeof useAppState> | null>(null);
export const AppProvider = ({ children }: { children: ReactNode }) => <AppContext.Provider value={useAppState()}>{children}</AppContext.Provider>;
export const useApp = () => { const app = useContext(AppContext); if (!app) throw new Error('AppProvider missing'); return app; };
