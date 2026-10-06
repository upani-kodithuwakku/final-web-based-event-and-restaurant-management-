export const localToday = () => new Intl.DateTimeFormat('en-CA', { timeZone: 'Asia/Colombo', year: 'numeric', month: '2-digit', day: '2-digit' }).format(new Date());
export function reservationError(date: string, time: string, guests: number, capacity: number, phone: string, name: string, now = Date.now()): string {
  if (!/^[0-9]{10}$/.test(phone)) return 'Phone number must contain exactly 10 digits (for example, 0771234567).';
  if (!name.trim() || name.trim().length > 100) return 'Please enter a contact name of up to 100 characters.';
  if (!/^\d{4}-\d{2}-\d{2}$/.test(date) || Number.isNaN(Date.parse(date)) || new Date(`${date}T12:00:00Z`).toISOString().slice(0, 10) !== date) return 'Please choose a valid reservation date.';
  if (!/^([01]\d|2[0-3]):[0-5]\d$/.test(time) || time < '11:00' || time > '21:00') return 'Choose a start time between 11:00 and 21:00.';
  if (Date.parse(`${date}T${time}:00+05:30`) <= now) return 'Reservation date and time must be in the future.';
  if (!Number.isInteger(guests) || guests < 1 || guests > capacity) return `Choose between 1 and ${capacity} guests.`;
  return '';
}
