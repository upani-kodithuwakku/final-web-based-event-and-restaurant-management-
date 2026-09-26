import { NavLink, Outlet, Link } from 'react-router-dom';
import {
  Squares2X2Icon,
  TableCellsIcon,
  CalendarDaysIcon,
  SparklesIcon,
  ArchiveBoxIcon,
  UserGroupIcon,
  UsersIcon,
  ChartBarIcon,
  FireIcon,
  BanknotesIcon,
  ArrowLeftIcon,
  ArrowRightStartOnRectangleIcon,
} from '@heroicons/react/24/outline';
import { Logo } from './Layout';
import { useApp } from '../context/AppContext';

type NavItem = { label: string; to: string; icon: React.ElementType; end?: boolean; roles: string[] };

const ALL_NAV: NavItem[] = [
  { label: 'Dashboard',    to: '/admin',             icon: Squares2X2Icon,  end: true, roles: ['ADMIN', 'MANAGER'] },
  { label: 'Reservations', to: '/admin/reservations', icon: CalendarDaysIcon,            roles: ['ADMIN', 'MANAGER', 'WAITER'] },
  { label: 'Tables',       to: '/admin/tables',       icon: TableCellsIcon,              roles: ['ADMIN', 'MANAGER', 'WAITER'] },
  { label: 'Events',       to: '/admin/events',       icon: SparklesIcon,                roles: ['ADMIN', 'MANAGER', 'EVENT_COORDINATOR'] },
  { label: 'Kitchen',      to: '/admin/kitchen',      icon: FireIcon,                    roles: ['ADMIN', 'MANAGER', 'KITCHEN_STAFF'] },
  { label: 'Cashier',      to: '/admin/cashier',      icon: BanknotesIcon,               roles: ['ADMIN', 'MANAGER', 'CASHIER'] },
  { label: 'Inventory',    to: '/admin/inventory',    icon: ArchiveBoxIcon,              roles: ['ADMIN', 'MANAGER', 'INVENTORY_MANAGER'] },
  { label: 'Staff',        to: '/admin/staff',        icon: UserGroupIcon,               roles: ['ADMIN', 'MANAGER'] },
  { label: 'Users',        to: '/admin/users',        icon: UsersIcon,                   roles: ['ADMIN', 'MANAGER'] },
  { label: 'Reports',      to: '/admin/reports',      icon: ChartBarIcon,                roles: ['ADMIN', 'MANAGER'] },
];

export default function AdminLayout() {
  const { user, logout } = useApp();
  const userRoles: string[] = user?.roles ?? [];

  const nav = ALL_NAV.filter(item => item.roles.some(r => userRoles.includes(r)));

  const roleLabel = userRoles.includes('ADMIN') ? 'Admin'
    : userRoles.includes('MANAGER') ? 'Manager'
    : userRoles.includes('EVENT_COORDINATOR') ? 'Event Coordinator'
    : userRoles.includes('INVENTORY_MANAGER') ? 'Inventory Manager'
    : userRoles.includes('CASHIER') ? 'Cashier'
    : userRoles.includes('KITCHEN_STAFF') ? 'Kitchen Staff'
    : userRoles.includes('WAITER') ? 'Waiter'
    : 'Staff';

  return (
    <>
      <header className="site-header">
        <div className="nav-wrap">
          <Logo />
          <span style={{ fontSize: 12, fontWeight: 700, letterSpacing: '.06em', textTransform: 'uppercase', color: 'var(--foggy)', flex: 1 }}>
            {roleLabel} Workspace
          </span>
          <div className="nav-actions">
            <span style={{ fontSize: 14, color: 'var(--foggy)' }}>{user?.fullName ?? 'Staff'}</span>
            <Link to="/" className="button" style={{ padding: '8px 16px', fontSize: 13 }}>
              <ArrowLeftIcon style={{ width: 14, height: 14 }} /> Back to site
            </Link>
            <button className="button" style={{ padding: '8px 16px', fontSize: 13 }} onClick={logout}>
              <ArrowRightStartOnRectangleIcon style={{ width: 14, height: 14 }} /> Log out
            </button>
          </div>
        </div>
      </header>
      <div className="admin-wrap">
        <aside className="admin-sidebar">
          <p className="sidebar-section">Menu</p>
          {nav.map(({ label, to, icon: Icon, end }) => (
            <NavLink
              key={to}
              to={to}
              end={end}
              className={({ isActive }) => `sidebar-link${isActive ? ' active' : ''}`}
            >
              <Icon />
              <span>{label}</span>
            </NavLink>
          ))}
        </aside>
        <main className="admin-main">
          <Outlet />
        </main>
      </div>
    </>
  );
}
