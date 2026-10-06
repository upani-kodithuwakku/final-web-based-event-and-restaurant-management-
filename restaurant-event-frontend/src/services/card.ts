// Input helpers for the simulated card gateway. The backend re-validates everything.
export const digitsOnly = (value: string) => value.replace(/\D/g, '');

/** "4242424242424242" -> "4242 4242 4242 4242" (max 12 digits). */
export function formatCardNumber(value: string): string {
  return digitsOnly(value).slice(0, 12).replace(/(\d{4})(?=\d)/g, '$1 ');
}

/** "1229" -> "12/29"; keeps a partly typed value usable. */
export function formatExpiry(value: string): string {
  const d = digitsOnly(value).slice(0, 4);
  return d.length > 2 ? `${d.slice(0, 2)}/${d.slice(2)}` : d;
}

/** Simulated gateway for the project: any number with exactly 12 digits is accepted. */
export function cardNumberIsValid(number: string): boolean {
  return /^\d{12}$/.test(digitsOnly(number));
}

/** A brand label for the number field (text only, no card artwork). */
export function cardBrand(number: string): string {
  const d = digitsOnly(number);
  if (/^4/.test(d)) return 'VISA';
  if (/^(5[1-5]|2[2-7])/.test(d)) return 'Mastercard';
  if (/^3[47]/.test(d)) return 'AMEX';
  return '';
}

/** True when MM/YY is a real month that has not passed yet. */
export function expiryIsValid(value: string, now = new Date()): boolean {
  const m = /^(0[1-9]|1[0-2])\/(\d{2})$/.exec(value);
  if (!m) return false;
  const year = 2000 + Number(m[2]), month = Number(m[1]);
  return year > now.getFullYear() || (year === now.getFullYear() && month >= now.getMonth() + 1);
}

/** Optional phone: empty is fine, otherwise 9-15 digits (e.g. 0771234567 or +94771234567). */
export function phoneIsValid(phone: string): boolean {
  if (!phone.trim()) return true;
  return /^\+?[0-9 ()-]{9,20}$/.test(phone.trim()) && digitsOnly(phone).length >= 9 && digitsOnly(phone).length <= 15;
}
