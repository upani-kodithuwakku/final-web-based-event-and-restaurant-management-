import { type ReactNode } from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { Toaster } from 'react-hot-toast';
import { AppProvider, useApp } from './context/AppContext';
import Layout from './components/Layout';
import AdminLayout from './components/AdminLayout';
import Discover from './pages/Discover';
import Reservations from './pages/Reservations';
import AdminTables from './pages/admin/Tables';
import AdminReservations from './pages/admin/AdminReservations';

const STAFF_ROLES = ['ADMIN', 'MANAGER', 'WAITER', 'KITCHEN_STAFF', 'EVENT_COORDINATOR', 'CASHIER', 'INVENTORY_MANAGER'];

function AdminGuard({ children }: { children: ReactNode }) {
  const { user } = useApp();
  if (!user || !user.roles.some(r => STAFF_ROLES.includes(r))) {
    return <Navigate to="/" replace />;
  }
  return <>{children}</>;
}

function AppRoutes() {
  return (
    <Routes>
      <Route element={<Layout />}>
        <Route index element={<Discover />} />
        <Route path="saved" element={<Discover savedOnly />} />
        <Route path="reservations" element={<Reservations />} />
        <Route path="*" element={<Navigate to="/" replace />} />
      </Route>
      <Route
        path="admin"
        element={
          <AdminGuard>
            <AdminLayout />
          </AdminGuard>
        }
      >
        <Route index element={<Navigate to="reservations" replace />} />
        <Route path="tables" element={<AdminTables />} />
        <Route path="reservations" element={<AdminReservations />} />
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
