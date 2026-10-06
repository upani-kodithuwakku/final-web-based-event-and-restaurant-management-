import { describe, expect, it } from 'vitest';
import { cardBrand, cardNumberIsValid, expiryIsValid, formatCardNumber, formatExpiry, phoneIsValid } from './card';

describe('card gateway helpers', () => {
  it('groups card digits and drops other characters', () => {
    expect(formatCardNumber('4242-4242 4242x4242')).toBe('4242 4242 4242');
    expect(formatCardNumber('4'.repeat(25)).replace(/ /g, '')).toHaveLength(12);
  });
  it('formats expiry as MM/YY while typing', () => {
    expect(formatExpiry('1')).toBe('1');
    expect(formatExpiry('122')).toBe('12/2');
    expect(formatExpiry('12/29')).toBe('12/29');
  });
  it('accepts any card number with exactly 12 digits', () => {
    expect(cardNumberIsValid('1234 5678 9012')).toBe(true);
    expect(cardNumberIsValid('4242 4242 4242 4241')).toBe(false);
    expect(cardNumberIsValid('1234 5678 901')).toBe(false);
  });
  it('labels common card brands', () => {
    expect(cardBrand('4242')).toBe('VISA');
    expect(cardBrand('5555')).toBe('Mastercard');
    expect(cardBrand('9999')).toBe('');
  });
  it('rejects expired or impossible dates', () => {
    const now = new Date(2026, 9, 4); // October 2026
    expect(expiryIsValid('10/26', now)).toBe(true);
    expect(expiryIsValid('09/26', now)).toBe(false);
    expect(expiryIsValid('13/30', now)).toBe(false);
  });
  it('treats the phone as optional but checks it when given', () => {
    expect(phoneIsValid('')).toBe(true);
    expect(phoneIsValid('077 123 4567')).toBe(true);
    expect(phoneIsValid('+94771234567')).toBe(true);
    expect(phoneIsValid('call me')).toBe(false);
  });
});
