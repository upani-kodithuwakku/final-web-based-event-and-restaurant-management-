import axios from 'axios';
import type { BookingInput, Reservation, Table, User } from '../types';
export const api = axios.create({ baseURL: import.meta.env.VITE_API_BASE_URL || '/api', timeout: 12000 });
api.interceptors.request.use(config => { const token = sessionStorage.getItem('gather-token'); if (token) config.headers.Authorization = `Bearer ${token}`; return config; });
export const errorMessage = (e: unknown) => axios.isAxiosError(e) ? e.response?.data?.message || (e.response?.status === 401 ? 'Please sign in to continue.' : 'Unable to reach the server. Please try again.') : e instanceof Error ? e.message : 'Something went wrong.';
export const reservationApi = {
 availability: async (date: string, time: string, guests: number, preference?: string) => (await api.get<{data: {availableTables: Table[]; alternativeTimes: string[]}}>('/reservations/availability', { params: {date, time, guests, preference: preference || undefined} })).data.data,
 mine: async () => (await api.get<{data: Reservation[]}>('/reservations/my')).data.data,
 save: async (input: BookingInput, id?: number) => (await (id ? api.put(`/reservations/${id}`, input) : api.post('/reservations', input))).data.data as Reservation,
 cancel: async (id: number, reason: string) => (await api.patch(`/reservations/${id}/cancel`, {reason})).data.data as Reservation,
 tables: async () => (await api.get<{data: Table[]}>('/admin/tables')).data.data,
 createTable: async (body: {tableNumber: string; capacity: number; location: string}) => (await api.post<{data: Table}>('/admin/tables', body)).data.data,
 updateTable: async (id: number, body: {tableNumber: string; capacity: number; location: string}) => (await api.put<{data: Table}>(`/admin/tables/${id}`, body)).data.data,
 daily: async (date: string) => (await api.get<{data: Reservation[]}>('/admin/reservations', {params: {date}})).data.data,
 action: async (id: number, action: string) => (await api.patch(`/admin/reservations/${id}/${action}`)).data.data as Reservation,
 tableStatus: async (id: number, status: string) => (await api.patch(`/admin/tables/${id}/status`, {status})).data.data as Table,
};
export const userApi = {
 me: async () => (await api.get<{data: User}>('/users/me')).data.data,
 update: async (body: {fullName: string; phone?: string}) => (await api.put<{data: User}>('/users/me', body)).data.data,
};

export type AdminUserDto = { id: number; fullName: string; email: string; phone: string; roles: string[]; isActive: boolean };
export const adminUserApi = {
 list: async () => (await api.get<AdminUserDto[]>('/admin/users')).data,
 suspend: async (id: number, active: boolean) => (await api.patch<AdminUserDto>(`/admin/users/${id}/suspend`, { active })).data,
 resetPassword: async (id: number, password: string) => api.post(`/admin/users/${id}/reset-password`, { password }),
 deactivate: async (id: number) => api.delete(`/admin/users/${id}`),
};

// Inventory — returns plain arrays (no data wrapper)
export type InventoryItemDto = { id: number; name: string; unit: string; currentQuantity: number; reorderLevel: number; lowStock: boolean; isActive: boolean };
export const inventoryApi = {
 list: async () => (await api.get<InventoryItemDto[]>('/inventory/items')).data,
 lowStock: async () => (await api.get<InventoryItemDto[]>('/inventory/items/low-stock')).data,
 create: async (body: {name: string; unit: string; currentQuantity: number; reorderLevel: number}) => (await api.post<InventoryItemDto>('/inventory/items', body)).data,
 update: async (id: number, body: {name: string; unit: string; currentQuantity: number; reorderLevel: number}) => (await api.put<InventoryItemDto>(`/inventory/items/${id}`, body)).data,
 adjust: async (id: number, delta: number, note: string) => (await api.patch<InventoryItemDto>(`/inventory/items/${id}/adjust`, {delta, note})).data,
};

// Menu — returns plain arrays
export type MenuCategoryDto = { id: number; name: string; description: string; displayOrder: number; isActive: boolean };
export type MenuItemDto = { id: number; categoryId: number; name: string; description: string; price: number; imageUrl: string; preparationMinutes: number; isAvailable: boolean; isActive: boolean };
export const menuApi = {
 categories: async () => (await api.get<MenuCategoryDto[]>('/menu/categories')).data,
 items: async (categoryId?: number) => (await api.get<MenuItemDto[]>('/menu/items', { params: categoryId ? {categoryId} : {} })).data,
};

// Events (customer) — returns plain arrays
export type EventHallDto = { id: number; name: string; capacity: number; location: string; description: string; isActive: boolean };
export type EventPackageDto = { id: number; name: string; eventType: string; description: string; basePrice: number; minimumGuests: number; maximumGuests: number; isActive: boolean };
export type EventBookingDto = { id: number; bookingReference: string; hallName: string; packageName: string; eventDate: string; startTime: string; endTime: string; guestCount: number; status: string; depositAmount: number; specialRequirements?: string; rejectionReason?: string };
export const eventApi = {
 halls: async () => (await api.get<EventHallDto[]>('/events/halls')).data,
 packages: async () => (await api.get<EventPackageDto[]>('/events/packages')).data,
 book: async (body: object) => (await api.post<EventBookingDto>('/events/bookings', body)).data,
 myBookings: async () => (await api.get<EventBookingDto[]>('/events/bookings/my')).data,
 cancel: async (id: number) => (await api.patch<EventBookingDto>(`/events/bookings/${id}/cancel`)).data,
};

// Event coordinator (admin)
export const eventCoordinatorApi = {
 list: async () => (await api.get<EventBookingDto[]>('/event-coordinator/bookings')).data,
 approve: async (id: number) => (await api.patch<EventBookingDto>(`/event-coordinator/bookings/${id}/approve`)).data,
 reject: async (id: number, reason?: string) => (await api.patch<EventBookingDto>(`/event-coordinator/bookings/${id}/reject`, {reason})).data,
};

// Staff (admin)
export type StaffDto = {
  id: number; userId: number; employeeCode: string;
  fullName?: string; email?: string; phone?: string;
  jobTitle: string; employmentStatus: string; isActive: boolean; roles?: string[];
};
export type ShiftDto = { id: number; shiftDate: string; startTime: string; endTime: string; roleRequired: string; requiredStaffCount: number; status: string; assignments: {id: number; staffId: number; assignedRole: string; status: string}[] };
export const staffApi = {
 list: async () => (await api.get<StaffDto[]>('/admin/staff')).data,
 getOne: async (id: number) => (await api.get<StaffDto>(`/admin/staff/${id}`)).data,
 createUser: async (body: { fullName: string; email: string; password: string; phone?: string; roles: string[]; jobTitle?: string; employmentStatus?: string }) =>
   (await api.post<StaffDto>('/admin/staff/users', body)).data,
 updateRoles: async (id: number, roles: string[]) => (await api.patch<StaffDto>(`/admin/staff/${id}/roles`, { roles })).data,
 resetPassword: async (id: number, password: string) => (await api.post(`/admin/staff/${id}/reset-password`, { password })).data,
 toggleStatus: async (id: number, active: boolean) => (await api.patch(`/admin/staff/${id}/status`, { active })).data,
 shifts: async (date?: string) => (await api.get<ShiftDto[]>('/admin/staff/shifts', {params: date ? {date} : {}})).data,
 createShift: async (body: object) => (await api.post<ShiftDto>('/admin/staff/shifts', body)).data,
 assign: async (shiftId: number, staffId: number) => (await api.post(`/admin/staff/shifts/${shiftId}/assign`, {staffId})).data,
 unassign: async (shiftId: number, staffId: number) => (await api.delete(`/admin/staff/shifts/${shiftId}/assignments/${staffId}`)).data,
};

// Billing (cashier/admin)
export type InvoiceDto = { id: number; invoiceNumber: string; customerId: number; invoiceType: string; subtotal: number; serviceCharge: number; taxAmount: number; discountAmount: number; totalAmount: number; status: string; issuedAt: string };
export type PaymentDto = { id: number; paymentReference: string; invoiceId: number; amount: number; method: string; status: string; paidAt?: string };
export const billingApi = {
 invoices: async () => (await api.get<InvoiceDto[]>('/billing/invoices')).data,
 myInvoices: async () => (await api.get<InvoiceDto[]>('/billing/invoices/my')).data,
 createInvoice: async (body: object) => (await api.post<InvoiceDto>('/billing/invoices', body)).data,
 payments: async (invoiceId: number) => (await api.get<PaymentDto[]>(`/billing/payments/${invoiceId}`)).data,
 pay: async (body: object) => (await api.post<PaymentDto>('/billing/payments', body)).data,
};

// Kitchen (kitchen staff/admin)
export type OrderDto = { id: number; orderReference: string; customerId: number; tableId?: number; status: string; subtotal: number; items: {id: number; menuItemId: number; itemNameSnapshot: string; unitPriceSnapshot: number; quantity: number; lineTotal: number; specialNote?: string}[]; createdAt: string };
export const kitchenApi = {
 queue: async () => (await api.get<OrderDto[]>('/kitchen/orders')).data,
 updateStatus: async (id: number, status: string) => (await api.patch<OrderDto>(`/kitchen/orders/${id}/status`, { status })).data,
};

export const authenticate = async (register: boolean, values: Record<string, string>) => (await api.post<{data: User}>(`/auth/${register ? 'register' : 'login'}`, values)).data.data;
