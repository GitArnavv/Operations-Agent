/**
 * Centralized formatting utility for Indian business operations.
 * Uses Intl.NumberFormat("en-IN") to ensure standard Indian numbering system
 * grouping (Lakhs and Crores) and exact 2-decimal precision without IEEE 754 float tails.
 */

// Cache standard formatters for high performance across large data tables
const inrCurrencyFormatter = new Intl.NumberFormat('en-IN', {
  style: 'currency',
  currency: 'INR',
  minimumFractionDigits: 2,
  maximumFractionDigits: 2,
});

const inrWholeCurrencyFormatter = new Intl.NumberFormat('en-IN', {
  style: 'currency',
  currency: 'INR',
  minimumFractionDigits: 0,
  maximumFractionDigits: 0,
});

const indianNumberFormatter = new Intl.NumberFormat('en-IN', {
  minimumFractionDigits: 0,
  maximumFractionDigits: 2,
});

/**
 * Parses numeric inputs safely (handles numbers, strings, null, undefined).
 */
export function parseNumeric(value: number | string | null | undefined): number {
  if (value === null || value === undefined || value === '') return 0;
  if (typeof value === 'number') {
    return Number.isFinite(value) ? value : 0;
  }
  const cleaned = String(value).replace(/[^0-9.-]+/g, '');
  const parsed = parseFloat(cleaned);
  return Number.isFinite(parsed) ? parsed : 0;
}

/**
 * Formats a value as standard INR currency (e.g., ₹1,06,200.00).
 * Handles Indian grouping (thousands, then lakhs, then crores).
 */
export function formatInr(
  value: number | string | null | undefined,
  options: { showDecimals?: boolean } = { showDecimals: true }
): string {
  const num = parseNumeric(value);
  if (options.showDecimals === false) {
    return inrWholeCurrencyFormatter.format(num);
  }
  return inrCurrencyFormatter.format(num);
}

/**
 * Formats large Indian currency amounts in human-readable Lakhs and Crores:
 * - >= 1,00,00,000 (1 Crore): e.g., ₹2.35 Cr
 * - >= 1,00,000 (1 Lakh): e.g., ₹2.40 Lakh (or ₹2.40L with shortSuffix)
 * - < 1,00,000: Standard ₹X,XXX.XX
 */
export function formatInrCompact(
  value: number | string | null | undefined,
  options: { shortSuffix?: boolean; decimals?: number } = {}
): string {
  const num = parseNumeric(value);
  const { shortSuffix = false, decimals = 2 } = options;
  const isNegative = num < 0;
  const abs = Math.abs(num);

  const prefix = isNegative ? '-₹' : '₹';

  if (abs >= 10000000) {
    // 1 Crore = 10,000,000
    const val = abs / 10000000;
    const formatted = val.toFixed(decimals);
    return `${prefix}${formatted}${shortSuffix ? 'Cr' : ' Cr'}`;
  }

  if (abs >= 100000) {
    // 1 Lakh = 100,000
    const val = abs / 100000;
    const formatted = val.toFixed(decimals);
    return `${prefix}${formatted}${shortSuffix ? 'L' : ' Lakh'}`;
  }

  return formatInr(num, { showDecimals: decimals > 0 });
}

/**
 * Formats percentages, defect rates, OTIF SLAs, and scores to exact decimal places.
 * Eliminates floating-point tails like 84.20000000004% -> 84.20%.
 */
export function formatPercent(
  value: number | string | null | undefined,
  decimals = 2,
  options: { includeSign?: boolean; suffix?: string } = {}
): string {
  const num = parseNumeric(value);
  const { includeSign = false, suffix = '%' } = options;

  const formatter = new Intl.NumberFormat('en-IN', {
    minimumFractionDigits: decimals,
    maximumFractionDigits: decimals,
  });

  const formatted = formatter.format(num);
  const sign = includeSign && num > 0 ? '+' : '';
  return `${sign}${formatted}${suffix}`;
}

/**
 * Formats general numeric metrics (quantities, counts, catalog items) using Indian grouping.
 */
export function formatNumber(
  value: number | string | null | undefined,
  decimals = 0
): string {
  const num = parseNumeric(value);
  if (decimals === 0) {
    return new Intl.NumberFormat('en-IN', {
      maximumFractionDigits: 0,
    }).format(num);
  }
  return new Intl.NumberFormat('en-IN', {
    minimumFractionDigits: decimals,
    maximumFractionDigits: decimals,
  }).format(num);
}

/**
 * Formats days of inventory cover (DoC) or lead time days.
 */
export function formatDays(value: number | string | null | undefined, decimals = 1): string {
  const num = parseNumeric(value);
  const formatted = num % 1 === 0 ? num.toFixed(0) : num.toFixed(decimals);
  return `${formatted} ${num === 1 ? 'day' : 'days'}`;
}
