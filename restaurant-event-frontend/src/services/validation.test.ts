import { describe, expect, it } from 'vitest';
import { reservationError } from './validation';
const now = Date.parse('2026-10-06T10:00:00+05:30');
const check = (date = '2026-10-07', time = '18:00', guests = 3, phone = '0771234567') => reservationError(date, time, guests, 4, phone, 'Upani', now);
describe('reservation validation', () => {
  it('accepts a future reservation with exactly ten phone digits', () => expect(check()).toBe(''));
  it('rejects short, long, international and nonnumeric phone numbers', () => {
    for (const phone of ['077123456', '07712345678', '+94771234567', '077 1234567', 'abcdefghij']) expect(check(undefined, undefined, undefined, phone)).toContain('10 digits');
  });
  it('rejects impossible dates and past times', () => {
    expect(check('2026-02-30')).toContain('valid reservation date');
    expect(check('2026-10-06','09:00')).toContain('11:00');
    expect(reservationError('2026-10-06','11:00',3,4,'0771234567','Upani',Date.parse('2026-10-06T12:00:00+05:30'))).toContain('future');
  });
  it('rejects times outside service hours and guest counts beyond capacity', () => {
    expect(check(undefined,'21:30')).toContain('21:00');
    expect(check(undefined,undefined,5)).toContain('1 and 4');
    expect(check(undefined,undefined,1.5)).toContain('1 and 4');
  });
});
