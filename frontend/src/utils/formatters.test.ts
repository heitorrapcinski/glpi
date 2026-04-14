import { describe, it, expect } from 'vitest';
import { formatDate, formatNumber } from './formatters';

describe('formatDate', () => {
  it('formats a Date object with default options', () => {
    const date = new Date('2025-06-15T10:30:00Z');
    const result = formatDate(date, 'en-US');
    // Should contain the date parts formatted by Intl
    const expected = new Intl.DateTimeFormat('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    }).format(date);
    expect(result).toBe(expected);
  });

  it('formats an ISO string', () => {
    const iso = '2025-01-20T14:00:00Z';
    const date = new Date(iso);
    const result = formatDate(iso, 'en-US');
    const expected = new Intl.DateTimeFormat('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    }).format(date);
    expect(result).toBe(expected);
  });

  it('returns empty string for invalid date', () => {
    expect(formatDate('not-a-date', 'en-US')).toBe('');
  });

  it('accepts custom Intl options', () => {
    const date = new Date('2025-06-15T10:30:00Z');
    const options: Intl.DateTimeFormatOptions = { year: 'numeric', month: 'long' };
    const result = formatDate(date, 'en-US', options);
    const expected = new Intl.DateTimeFormat('en-US', options).format(date);
    expect(result).toBe(expected);
  });

  it('defaults to en-US locale', () => {
    const date = new Date('2025-06-15T10:30:00Z');
    const result = formatDate(date);
    const expected = new Intl.DateTimeFormat('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    }).format(date);
    expect(result).toBe(expected);
  });
});

describe('formatNumber', () => {
  it('formats an integer with default locale', () => {
    const result = formatNumber(1234567);
    const expected = new Intl.NumberFormat('en-US').format(1234567);
    expect(result).toBe(expected);
  });

  it('formats a decimal number', () => {
    const result = formatNumber(1234.56, 'en-US');
    const expected = new Intl.NumberFormat('en-US').format(1234.56);
    expect(result).toBe(expected);
  });

  it('formats zero', () => {
    expect(formatNumber(0, 'en-US')).toBe('0');
  });

  it('formats negative numbers', () => {
    const result = formatNumber(-42, 'en-US');
    const expected = new Intl.NumberFormat('en-US').format(-42);
    expect(result).toBe(expected);
  });

  it('accepts custom Intl options', () => {
    const options: Intl.NumberFormatOptions = { style: 'percent' };
    const result = formatNumber(0.75, 'en-US', options);
    const expected = new Intl.NumberFormat('en-US', options).format(0.75);
    expect(result).toBe(expected);
  });
});
