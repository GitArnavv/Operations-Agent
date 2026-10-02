import { describe, it, expect } from 'vitest';
import {
  formatInr,
  formatInrCompact,
  formatPercent,
  formatNumber,
  formatDays,
} from '../utils/formatters';

describe('Centralized Indian Business Formatters', () => {
  describe('formatInr', () => {
    it('formats currency with Indian grouping (Lakhs/Crores) and 2 decimal places', () => {
      // Handles standard Indian comma separation: 1,06,200.00
      const formatted = formatInr(106200);
      expect(formatted).toContain('1,06,200.00');
      expect(formatted).toMatch(/^₹|\u20B9/);
    });

    it('formats large amounts in Crores with Indian grouping', () => {
      const formatted = formatInr(23500000);
      expect(formatted).toContain('2,35,00,000.00');
    });

    it('handles zero, null, undefined and negative values gracefully', () => {
      expect(formatInr(0)).toContain('0.00');
      expect(formatInr(null)).toContain('0.00');
      expect(formatInr(undefined)).toContain('0.00');
      expect(formatInr(-50000)).toContain('50,000.00');
    });

    it('supports whole currency without decimals', () => {
      const formatted = formatInr(15000, { showDecimals: false });
      expect(formatted).not.toContain('.00');
      expect(formatted).toContain('15,000');
    });
  });

  describe('formatInrCompact', () => {
    it('formats amounts in Lakhs', () => {
      expect(formatInrCompact(240000)).toBe('₹2.40 Lakh');
      expect(formatInrCompact(185000)).toBe('₹1.85 Lakh');
      expect(formatInrCompact(240000, { shortSuffix: true })).toBe('₹2.40L');
    });

    it('formats amounts in Crores', () => {
      expect(formatInrCompact(23500000)).toBe('₹2.35 Cr');
      expect(formatInrCompact(10000000, { shortSuffix: true })).toBe('₹1.00Cr');
    });

    it('falls back to standard format for amounts below 1 Lakh', () => {
      const formatted = formatInrCompact(45000);
      expect(formatted).toContain('45,000.00');
    });
  });

  describe('formatPercent', () => {
    it('restricts defect rates and SLAs to exact 2-decimal percentages without float tails', () => {
      // 84.20000000004% must format as 84.20%
      expect(formatPercent(84.20000000004)).toBe('84.20%');
      // 3.149 should round to 3.15%
      expect(formatPercent(3.149)).toBe('3.15%');
      // 94 should pad to 94.00%
      expect(formatPercent(94)).toBe('94.00%');
      // 0 should pad to 0.00%
      expect(formatPercent(0)).toBe('0.00%');
    });

    it('handles null, undefined and strings', () => {
      expect(formatPercent(null)).toBe('0.00%');
      expect(formatPercent('18.5')).toBe('18.50%');
    });

    it('supports custom decimals and optional sign', () => {
      expect(formatPercent(12.3456, 1)).toBe('12.3%');
      expect(formatPercent(5.2, 2, { includeSign: true })).toBe('+5.20%');
    });
  });

  describe('formatNumber and formatDays', () => {
    it('formats quantities with Indian grouping', () => {
      expect(formatNumber(150000)).toBe('1,50,000');
      expect(formatNumber(500)).toBe('500');
    });

    it('formats days of stock cover and lead time', () => {
      expect(formatDays(3.4)).toBe('3.4 days');
      expect(formatDays(1)).toBe('1 day');
      expect(formatDays(7)).toBe('7 days');
    });
  });
});
