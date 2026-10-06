import axios from 'axios';
import { normalizeEvent } from './eventCatalog';
import type { AdminBookingInput, AdminUpdateInput, BookingInput, Reservation, Table, User } from '../types';
export const api = axios.create({ baseURL: import.meta.env.VITE_API_BASE_URL || '/api', timeout: 12000 });
api.interceptors.request.use(config => { const token = sessionStorage.getItem('gather-token'); if (token) config.headers.Authorization = `Bearer ${token}`; return config; });
export const errorMessage = (e: unknown) => axios.isAxiosError(e) ? e.response?.data?.message || (e.response?.status === 401 ? 'Please sign in to continue.' : 'Unable to reach the server. Please try again.') : e instanceof Error ? e.message : 'Something went wrong.';
export type ReservationHistory = {id:number; actorId?:number; action:string; previousStatus?:string; status:string; createdAt:string};
export const reservationApi = {
 history: async (id:number) => (await api.get<{data: ReservationHistory[]}>(`/admin/reservations/${id}/history`)).data.data,
 deleteTable: async (id: number) => { await api.delete(`/admin/tables/${id}`); },
 spaces: async () => (await api.get<{data: Table[]}>('/reservations/tables')).data.data,
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
 table: async (id: number) => (await api.get<{data: Table}>(`/admin/tables/${id}`)).data.data,
 adminGet: async (id: number) => (await api.get<{data: Reservation}>(`/admin/reservations/${id}`)).data.data,
 adminCreate: async (input: AdminBookingInput) => (await api.post<{data: Reservation}>('/admin/reservations', input)).data.data,
 adminUpdate: async (id: number, input: AdminUpdateInput) => (await api.put<{data: Reservation}>(`/admin/reservations/${id}`, input)).data.data,
 adminCancel: async (id: number, reason: string) => (await api.patch<{data: Reservation}>(`/admin/reservations/${id}/cancel`, {reason})).data.data,
};
// ── Customer management (profile, password reset, notifications, reports) ──
export type UserProfileDto = { id: number; fullName: string; email: string; phone: string; roles: string[]; createdAt?: string };
export const userApi = {
 me: async () => (await api.get<{data: UserProfileDto}>('/users/me')).data.data,
 update: async (body: {fullName: string; phone?: string}) => (await api.put<{data: UserProfileDto}>('/users/me', body)).data.data,
 changePassword: async (body: {currentPassword: string; newPassword: string}) => { await api.put('/users/me/password', body); },
};
export const passwordResetApi = {
 /** Returns the API message, plus the reset link when the backend runs with EXPOSE_RESET_LINK=true. */
 request: async (email: string) => {
   const body = (await api.post<{message: string; data: {message: string; resetLink?: string}}>('/auth/forgot-password', { email })).data;
   return { message: body.message, resetLink: body.data?.resetLink };
 },
 reset: async (token: string, newPassword: string) => { await api.post('/auth/reset-password', { token, newPassword }); },
};
export type NotificationDto = { id: number; title: string; message: string; type: string; isRead: boolean; createdAt: string };
export const notificationApi = {
 list: async () => (await api.get<NotificationDto[]>('/notifications')).data,
 unreadCount: async () => (await api.get<{count: number}>('/notifications/unread-count')).data.count,
 markRead: async (id: number) => (await api.patch<NotificationDto>(`/notifications/${id}/read`)).data,
 markAllRead: async () => { await api.patch('/notifications/read-all'); },
};
export type DashboardReportDto = {
 date: string; todayReservations: number; totalTables: number; availableTables: number; occupiedTables: number;
 reservedTables: number; outOfServiceTables: number; totalCustomers: number; activeFoodOrders: number;
 pendingEventBookings: number; lowStockItems: number;
};
export type ReservationReportDto = { from: string; to: string; totalReservations: number; totalGuests: number; byStatus: Record<string, number>; noShowRate: number; busiestDay?: string; busiestDayReservations: number };
export type SalesReportDto = { from: string; to: string; orderCount: number; cancelledOrders: number; foodRevenue: number; averageOrderValue: number; topItems: { name: string; quantity: number; revenue: number }[] };
export type InventoryReportDto = { activeItems: number; lowStockCount: number; lowStockItems: { id: number; name: string; unit: string; currentQuantity: number; reorderLevel: number }[] };
export type EventReportDto = { from: string; to: string; totalBookings: number; byStatus: Record<string, number>; confirmedGuests: number; confirmedValue: number };
type Range = { from?: string; to?: string };
export const reportApi = {
 dashboard: async () => (await api.get<DashboardReportDto>('/admin/reports/dashboard')).data,
 reservations: async (range: Range = {}) => (await api.get<ReservationReportDto>('/admin/reports/reservations', { params: range })).data,
 sales: async (range: Range = {}) => (await api.get<SalesReportDto>('/admin/reports/sales', { params: range })).data,
 inventory: async () => (await api.get<InventoryReportDto>('/admin/reports/inventory')).data,
 events: async (range: Range = {}) => (await api.get<EventReportDto>('/admin/reports/events', { params: range })).data,
};

export type AdminUserDto = { id: number; fullName: string; email: string; phone: string; roles: string[]; isActive: boolean };
export const adminUserApi = {
 update: async (id: number, body: {fullName: string; phone: string}) => (await api.put<AdminUserDto>(`/admin/users/${id}`,body)).data,
 list: async () => (await api.get<AdminUserDto[]>('/admin/users')).data,
 suspend: async (id: number, active: boolean) => (await api.patch<AdminUserDto>(`/admin/users/${id}/suspend`, { active })).data,
 resetPassword: async (id: number, password: string) => api.post(`/admin/users/${id}/reset-password`, { password }),
 deactivate: async (id: number) => api.delete(`/admin/users/${id}`),
};

// Inventory — returns plain arrays (no data wrapper)
export type InventoryItemDto = { id: number; name: string; unit: string; currentQuantity: number; reorderLevel: number; lowStock: boolean; isActive: boolean };
export const inventoryApi = {
 remove: async (id: number) => { await api.delete(`/inventory/items/${id}`); },
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
 halls: async () => (await api.get<(EventHallDto & { active?: boolean })[]>('/events/halls')).data.map(normalizeEvent),
 packages: async () => (await api.get<(EventPackageDto & { active?: boolean })[]>('/events/packages')).data.map(normalizeEvent),
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
 list: async () => (await api.get<StaffDto[]>('/admin/staff')).data.map(normalizeEvent),
 update: async (id: number, body: object) => (await api.put<StaffDto>(`/admin/staff/${id}`, body)).data,
 remove: async (id: number) => { await api.delete(`/admin/staff/${id}`); },
 updateShift: async (id: number, body: object) => (await api.put<ShiftDto>(`/admin/staff/shifts/${id}`,body)).data,
 deleteShift: async (id: number) => { await api.delete(`/admin/staff/shifts/${id}`); },
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

// Customer payments for confirmed food orders and event bookings
export type CardDetails = { holderName: string; number: string; expiry: string; cvv: string };
export type PaymentChoice = 'CARD' | 'PAY_AT_OUTLET';
export type CustomerPaymentDto = {
  id: number; paymentReference: string; customerId: number; purpose: 'FOOD_ORDER' | 'EVENT_BOOKING' | 'TABLE_RESERVATION';
  foodOrderId?: number; eventBookingId?: number; tableReservationId?: number; targetReference?: string; amount: number;
  method: PaymentChoice; status: 'PENDING' | 'PAID' | 'FAILED' | 'REFUNDED';
  cardHolderName?: string; cardLast4?: string; cardBrand?: string; gatewayReference?: string;
  confirmationMessage?: string;
  paidAt?: string; createdAt: string; updatedAt: string;
};
export type BillLineDto = {
  purpose: 'FOOD_ORDER' | 'EVENT_BOOKING' | 'TABLE_RESERVATION'; targetId: number; reference: string; description: string;
  eventDate?: string; createdAt: string; targetStatus: string; confirmed: boolean; payable: boolean;
  subtotal: number; serviceCharge: number; total: number;
  paymentId?: number; paymentReference?: string; paymentMethod?: PaymentChoice;
  paymentStatus?: CustomerPaymentDto['status']; cardLast4?: string;
  items: { name: string; quantity: number; unitPrice: number; lineTotal: number }[];
};
export type PaymentSummaryDto = {
  foodOrders: BillLineDto[]; eventBookings: BillLineDto[]; tableReservations: BillLineDto[]; depositPerGuest: number; reservationTotal: number | null;
  foodTotal: number | null; eventTotal: number | null; grandTotal: number | null;
  amountPaid: number; amountDue: number;
};
export const customerPaymentApi = {
 summary: async () => {
   const summary = (await api.get<PaymentSummaryDto>('/payments/summary')).data;
   return { ...summary, tableReservations: summary.tableReservations ?? [], reservationTotal: summary.reservationTotal ?? null };
 },
 mine: async () => (await api.get<CustomerPaymentDto[]>('/payments/my')).data,
 create: async (body: { foodOrderId?: number; eventBookingId?: number; tableReservationId?: number; method: PaymentChoice; card?: CardDetails }) =>
   (await api.post<CustomerPaymentDto>('/payments', body)).data,
 changeMethod: async (id: number, body: { method: PaymentChoice; card?: CardDetails }) =>
   (await api.put<CustomerPaymentDto>(`/payments/${id}`, body)).data,
 reservations: async () => (await api.get<CustomerPaymentDto[]>('/payments/reservations')).data,
 events: async () => (await api.get<CustomerPaymentDto[]>('/payments/events')).data,
 all: async (status?: string) => (await api.get<CustomerPaymentDto[]>('/payments', { params: status ? { status } : {} })).data,
 updateStatus: async (id: number, status: string) => (await api.patch<CustomerPaymentDto>(`/payments/${id}/status`, { status })).data,
 remove: async (id: number) => { await api.delete(`/payments/${id}`); },
};

// Kitchen (kitchen staff/admin)
export type OrderDto = { id: number; orderReference: string; customerId: number; tableId?: number; specialNote?: string; status: string; subtotal: number; items: {id: number; menuItemId: number; itemNameSnapshot: string; unitPriceSnapshot: number; quantity: number; lineTotal: number; specialNote?: string}[]; createdAt: string };
export const orderApi = {
 create: async (body: { orderType: string; specialNote: string; items: { menuItemId: number; quantity: number }[] }) => (await api.post<OrderDto>('/orders', body)).data,
 mine: async () => (await api.get<OrderDto[]>('/orders/my')).data,
};
export type FoodRequestDto = { id: number; customerName: string; itemName?: string; message: string; status: string; createdAt: string };
export const foodRequestApi = {
 create: async (body: { menuItemId?: number; message: string }) => (await api.post<FoodRequestDto>('/food-requests', body)).data,
 mine: async () => (await api.get<FoodRequestDto[]>('/food-requests/my')).data,
 queue: async () => (await api.get<FoodRequestDto[]>('/food-requests')).data,
 resolve: async (id: number) => (await api.patch<FoodRequestDto>(`/food-requests/${id}/resolve`)).data,
};
export const kitchenApi = {
 queue: async () => (await api.get<OrderDto[]>('/kitchen/orders')).data,
 updateStatus: async (id: number, status: string) => (await api.patch<OrderDto>(`/kitchen/orders/${id}/status`, { status })).data,
};

export const authenticate = async (register: boolean, values: Record<string, string>) => (await api.post<{data: User}>(`/auth/${register ? 'register' : 'login'}`, values)).data.data;

export type MenuItemInput = Omit<MenuItemDto, 'id' | 'isActive'>;
export const adminMenuApi = {
 list: async () => (await api.get<MenuItemDto[]>('/admin/menu/items')).data,
 create: async (body: MenuItemInput) => (await api.post<MenuItemDto>('/admin/menu/items', body)).data,
 update: async (id: number, body: MenuItemInput) => (await api.put<MenuItemDto>(`/admin/menu/items/${id}`, body)).data,
 remove: async (id: number) => { await api.delete(`/admin/menu/items/${id}`); },
 availability: async (id: number, available: boolean) => (await api.patch<MenuItemDto>(`/admin/menu/items/${id}/availability`, {available})).data,
 createCategory: async (name: string) => (await api.post<MenuCategoryDto>('/admin/menu/categories', {name, displayOrder: 0})).data,
};

export type SupplierDto = { id: number; name: string; contactPerson: string; phone: string; email: string; address: string; suppliedProducts: string; joinedDate: string | null; active: boolean; createdAt: string; updatedAt: string };
export type SupplierInput = Omit<SupplierDto, 'id' | 'createdAt' | 'updatedAt'>;
export const supplierApi = {
 list: async () => (await api.get<SupplierDto[]>('/suppliers')).data,
 create: async (body: SupplierInput) => (await api.post<SupplierDto>('/suppliers', body)).data,
 update: async (id: number, body: SupplierInput) => (await api.put<SupplierDto>(`/suppliers/${id}`, body)).data,
 remove: async (id: number) => { await api.delete(`/suppliers/${id}`); },
};
