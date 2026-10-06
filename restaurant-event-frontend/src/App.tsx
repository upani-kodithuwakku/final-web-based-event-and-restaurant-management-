import { type ReactNode } from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { Toaster } from 'react-hot-toast';
import { AppProvider, useApp } from './context/AppContext';
import Layout from './components/Layout';
import AdminLayout from './components/AdminLayout';
import Discover from './pages/Discover';
import Auth from './pages/Auth';
import ForgotPassword from './pages/ForgotPassword';
import ResetPassword from './pages/ResetPassword';
import Reservations from './pages/Reservations';
import Menu from './pages/Menu';
import Events from './pages/Events';
import Profile from './pages/Profile';
import CustomerDashboard from './pages/CustomerDashboard';
import NotFound from './pages/shared/NotFound';
import AdminDashboard from './pages/admin/Dashboard';
import Suppliers from './pages/admin/Suppliers';
import AdminMenu from './pages/admin/Menu';
import AdminTables from './pages/admin/Tables';
import AdminReservations from './pages/admin/AdminReservations';
import AdminEvents from './pages/admin/AdminEvents';
import AdminInventory from './pages/admin/Inventory';
import AdminStaff from './pages/admin/Staff';
import AdminReports from './pages/admin/Reports';
import KitchenOrders from './pages/admin/Kitchen';
import CashierDashboard from './pages/admin/Cashier';
import AdminUsers from './pages/admin/Users';
import FoodRequests from './pages/admin/FoodRequests';
import Payments from './pages/Payments';
import CustomerPayments from './pages/admin/CustomerPayments';

const STAFF_ROLES = ['ADMIN', 'MANAGER', 'WAITER', 'KITCHEN_STAFF', 'EVENT_COORDINATOR', 'CASHIER', 'INVENTORY_MANAGER'];

function AdminGuard({ children }: { children: ReactNode }) {
  const { user } = useApp();
  if (!user || !user.roles.some(r => STAFF_ROLES.includes(r))) {
    return <Navigate to="/login" replace />;
  }
  return <>{children}</>;
}

function CustomerGuard({ children }: { children: ReactNode }) {
  const { user } = useApp();
  if (!user) return <Navigate to="/login" replace />;
  return <>{children}</>;
}

function AppRoutes() {
  return (
    <Routes>
      <Route element={<Layout />}>
        <Route index element={<Discover />} />
        <Route path="saved" element={<Discover savedOnly />} />
        <Route path="menu" element={<Menu />} />
        <Route path="events" element={<Events />} />
        <Route path="reservations" element={<Reservations />} />
        <Route path="profile" element={<Profile />} />
        <Route path="dashboard" element={<CustomerGuard><CustomerDashboard /></CustomerGuard>} />
        <Route path="payments" element={<CustomerGuard><Payments /></CustomerGuard>} />
        <Route path="login" element={<Auth />} />
        <Route path="register" element={<Auth register />} />
        <Route path="forgot-password" element={<ForgotPassword />} />
        <Route path="reset-password" element={<ResetPassword />} />
        <Route path="*" element={<NotFound />} />
      </Route>
      <Route
        path="admin"
        element={
          <AdminGuard>
            <AdminLayout />
          </AdminGuard>
        }
      >
        <Route index element={<AdminDashboard />} />
        <Route path="menu" element={<AdminMenu />} />
        <Route path="suppliers" element={<Suppliers />} />
        <Route path="tables" element={<AdminTables />} />
        <Route path="reservations" element={<AdminReservations />} />
        <Route path="events" element={<AdminEvents />} />
        <Route path="inventory" element={<AdminInventory />} />
        <Route path="staff" element={<AdminStaff />} />
        <Route path="reports" element={<AdminReports />} />
        <Route path="kitchen" element={<KitchenOrders />} />
        <Route path="cashier" element={<CashierDashboard />} />
        <Route path="users" element={<AdminUsers />} />
        <Route path="food-requests" element={<FoodRequests />} />
        <Route path="payments" element={<CustomerPayments />} />
      </Route>
    </Routes>
  );
}

export default function App() {
  return (
    <AppProvider>
      <BrowserRouter>
        <Toaster
          position="top-center"
          toastOptions={{
            style: { borderRadius: 12, fontFamily: 'Inter, sans-serif', fontSize: 14 },
            success: { iconTheme: { primary: '#00A699', secondary: '#fff' } },
          }}
        />
        <AppRoutes />
      </BrowserRouter>
    </AppProvider>
  );
}
