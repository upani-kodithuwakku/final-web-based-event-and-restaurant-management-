import { useState } from 'react';
import { Link, NavLink, Outlet } from 'react-router-dom';
import { BellIcon, Bars3Icon, UserCircleIcon, GlobeAltIcon, HeartIcon, ArrowUpRightIcon } from '@heroicons/react/24/outline';
import { useApp } from '../context/AppContext';
import { Modal } from './UI';

const STAFF_ROLES = ['ADMIN', 'MANAGER', 'WAITER', 'KITCHEN_STAFF', 'EVENT_COORDINATOR', 'CASHIER', 'INVENTORY_MANAGER'];

export function Logo() {
  return <Link to="/" className="logo" aria-label="Gather home"><svg viewBox="0 0 44 44" fill="none"><path d="M10 29c0-13 12-23 12-23s12 10 12 23c0 11-12 11-12 1s-12-10-12-1c0 11 12 11 12 1" stroke="currentColor" strokeWidth="2.8" strokeLinecap="round" /></svg>gather<span>®</span></Link>;
}

export default function Layout() {
  const { user, logout, notifications, setNotifications } = useApp();
  const [menu, setMenu] = useState(false);
  const [notices, setNotices] = useState(false);
  const isStaff = user?.roles.some(r => STAFF_ROLES.includes(r));

  return (
    <>
      <header className="site-header">
        <div className="nav-wrap">
          <Logo />
          <nav aria-label="Main navigation">
            <NavLink to="/" end>Discover</NavLink>
            <NavLink to="/menu">Our menu</NavLink>
            <NavLink to="/events">Events & celebrations</NavLink>
            <NavLink to="/reservations">My reservations</NavLink>
          </nav>
          <div className="nav-actions">
            <Link className="saved-link" to="/saved" aria-label="Saved tables"><HeartIcon /></Link>
            <button className="icon-button notification-button" aria-label="Notifications" onClick={() => setNotices(true)}>
              <BellIcon />{notifications.length > 0 && <i />}
            </button>
            <div className="profile-menu">
              <button className="profile-button" aria-label="Account menu" aria-expanded={menu} onClick={() => setMenu(!menu)}>
                <Bars3Icon /><span className="avatar">{user ? user.fullName.charAt(0) : <UserCircleIcon />}</span>
              </button>
              {menu && (
                <div className="dropdown" onClick={() => setMenu(false)}>
                  {user ? (
                    <>
                      <Link to="/profile">My profile</Link>
                      <button onClick={logout}>Sign out</button>
                    </>
                  ) : (
                    <>
                      <Link to="/login">Log in</Link>
                      <Link to="/register">Create an account</Link>
                    </>
                  )}
                  <Link to="/saved">Saved spaces</Link>
                  {isStaff && <Link to="/admin">Staff workspace <ArrowUpRightIcon /></Link>}
                </div>
              )}
            </div>
          </div>
        </div>
      </header>
      <main><Outlet /></main>
      <footer className="site-footer">
        <div className="footer-top">
          <div><Logo /><p>Good food. Great company.<br />A place for every moment.</p></div>
          <div><h4>Come together</h4><Link to="/">Find your table</Link><Link to="/menu">Explore our menu</Link><Link to="/events">Plan a celebration</Link></div>
          <div><h4>Your Gather</h4><Link to="/reservations">My reservations</Link><Link to="/saved">Saved spaces</Link><Link to="/profile">My account</Link></div>
          <div><h4>Visit us</h4><p>42, Park Street, Colombo 02<br />Sri Lanka</p><p>Every day · 11:00 AM – 11:00 PM</p><a href="mailto:hello@gather.example">hello@gather.example ↗</a></div>
        </div>
        <div className="footer-bottom">
          <span>© {new Date().getFullYear()} Gather · Group 06</span>
          <span>Connected to restaurant API</span>
          <span><GlobeAltIcon /> English (UK) <b>LKR</b></span>
        </div>
      </footer>
      {notices && (
        <Modal title="Your notifications" onClose={() => setNotices(false)}>
          {notifications.length ? (
            <>
              <button className="text-button" onClick={() => setNotifications([])}>Clear all</button>
              {notifications.map((n, i) => <div className="notice" key={`${n}-${i}`}><BellIcon /><p>{n}</p></div>)}
            </>
          ) : <p>You're all caught up.</p>}
        </Modal>
      )}
    </>
  );
}
