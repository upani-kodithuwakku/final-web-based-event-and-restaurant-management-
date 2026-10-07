import { useCallback, useEffect, useState } from 'react';
import { formatDistanceToNow } from 'date-fns';
import { BellIcon, CheckIcon } from '@heroicons/react/24/outline';
import { useApp } from '../context/AppContext';
import { Modal } from './UI';
import { errorMessage, notificationApi, type NotificationDto } from '../services/api';

// How often to check for new notifications while the site is open.
const POLL_MS = 30000;

/** Header bell: the signed-in user's notifications from the backend, plus messages saved on this device. */
export default function NotificationBell() {
  const { user, notifications: deviceNotes, setNotifications: setDeviceNotes } = useApp();
  const [open, setOpen] = useState(false);
  const [items, setItems] = useState<NotificationDto[]>([]);
  const [unread, setUnread] = useState(0);
  const [error, setError] = useState('');

  const refreshCount = useCallback(async () => {
    if (!user) return;
    try { setUnread(await notificationApi.unreadCount()); } catch { /* keep the last count */ }
  }, [user]);

  const load = useCallback(async () => {
    if (!user) return;
    try {
      const list = await notificationApi.list();
      setItems(list); setUnread(list.filter(n => !n.isRead).length); setError('');
    } catch (e) { setError(errorMessage(e)); }
  }, [user]);

  useEffect(() => {
    setItems([]); setUnread(0);
    if (!user) return;
    void refreshCount();
    const timer = window.setInterval(() => void refreshCount(), POLL_MS);
    return () => window.clearInterval(timer);
  }, [user, refreshCount]);

  useEffect(() => { if (open) void load(); }, [open, load]);

  const markRead = async (n: NotificationDto) => {
    if (n.isRead) return;
    try {
      const updated = await notificationApi.markRead(n.id);
      setItems(all => all.map(x => x.id === n.id ? updated : x));
      setUnread(c => Math.max(0, c - 1));
    } catch (e) { setError(errorMessage(e)); }
  };
  const markAllRead = async () => {
    try {
      await notificationApi.markAllRead();
      setItems(all => all.map(x => ({ ...x, isRead: true }))); setUnread(0);
    } catch (e) { setError(errorMessage(e)); }
  };

  const hasDot = unread > 0 || deviceNotes.length > 0;
  return (
    <>
      <button className="icon-button notification-button" aria-label={unread ? `Notifications, ${unread} unread` : 'Notifications'} onClick={() => setOpen(true)}>
        <BellIcon />{hasDot && <i />}
        {unread > 0 && <span className="notification-count" aria-hidden="true">{unread > 9 ? '9+' : unread}</span>}
      </button>
      {open && (
        <Modal title="Your notifications" onClose={() => setOpen(false)}>
          {error && <p className="error" role="alert">{error}</p>}
          {user && items.length > 0 && (
            <div className="notice-head">
              <span className="small muted">{unread ? `${unread} unread` : 'All read'}</span>
              {unread > 0 && <button className="text-button" onClick={markAllRead}>Mark all as read</button>}
            </div>
          )}
          {items.length > 0 && <button className="text-button" onClick={async () => {if(!window.confirm('Delete all your notifications?')) return;try {await notificationApi.clear();setItems([]);setUnread(0);} catch(e) {setError(errorMessage(e));}}}>Delete all notifications</button>}
          {items.map(n => (
            <div key={n.id}><button key={n.id} className={`notice server${n.isRead ? '' : ' unread'}`} onClick={() => void markRead(n)}>
              <BellIcon />
              <span>
                <b>{n.title}</b>
                <p>{n.message}</p>
                <small>{formatDistanceToNow(new Date(n.createdAt), { addSuffix: true })}</small>
              </span>
              {n.isRead ? <CheckIcon className="notice-read" aria-label="Read" /> : <i className="notice-dot" aria-label="Unread" />}
            </button><button className="text-button" aria-label={`Delete ${n.title}`} onClick={async () => {try {await notificationApi.remove(n.id);setItems(all => all.filter(x => x.id !== n.id));await refreshCount();}catch(e){setError(errorMessage(e));}}}>Delete</button></div>
          ))}
          {deviceNotes.length > 0 && <>
            <div className="notice-head">
              <span className="small muted">On this device</span>
              <button className="text-button" onClick={() => setDeviceNotes([])}>Clear</button>
            </div>
            {deviceNotes.map((n, i) => <div className="notice" key={`${n}-${i}`}><BellIcon /><p>{n}</p></div>)}
          </>}
          {!items.length && !deviceNotes.length && !error && <p>You're all caught up.</p>}
        </Modal>
      )}
    </>
  );
}
