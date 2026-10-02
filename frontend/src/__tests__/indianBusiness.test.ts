import { describe, it, expect } from 'vitest';

describe('Indian Business Intelligence & Tax Computations', () => {
  it('formats currency correctly in Indian Lakh and Crore notation', () => {
    const formatInrLakh = (amount: number) => `₹${(amount / 100000).toFixed(2)} Lakh`;
    const formatInrCrore = (amount: number) => `₹${(amount / 10000000).toFixed(2)} Cr`;

    expect(formatInrLakh(240000)).toBe('₹2.40 Lakh');
    expect(formatInrLakh(185000)).toBe('₹1.85 Lakh');
    expect(formatInrCrore(23500000)).toBe('₹2.35 Cr');
  });

  it('correctly splits intra-state (CGST + SGST) vs inter-state (IGST) tax', () => {
    const calculateGst = (taxableAmount: number, buyerStateCode: string, sellerStateCode: string, ratePercent: number) => {
      const taxAmount = (taxableAmount * ratePercent) / 100;
      if (buyerStateCode === sellerStateCode) {
        return {
          type: 'INTRA_STATE',
          cgst: taxAmount / 2,
          sgst: taxAmount / 2,
          igst: 0,
          total: taxableAmount + taxAmount,
        };
      }
      return {
        type: 'INTER_STATE',
        cgst: 0,
        sgst: 0,
        igst: taxAmount,
        total: taxableAmount + taxAmount,
      };
    };

    // Maharashtra to Maharashtra (State code 27)
    const intra = calculateGst(100000, '27', '27', 18);
    expect(intra.type).toBe('INTRA_STATE');
    expect(intra.cgst).toBe(9000);
    expect(intra.sgst).toBe(9000);
    expect(intra.igst).toBe(0);
    expect(intra.total).toBe(118000);

    // Maharashtra to Gujarat (State code 24)
    const inter = calculateGst(100000, '24', '27', 18);
    expect(inter.type).toBe('INTER_STATE');
    expect(inter.cgst).toBe(0);
    expect(inter.sgst).toBe(0);
    expect(inter.igst).toBe(18000);
    expect(inter.total).toBe(118000);
  });

  it('calculates Days of Cover (DoC) and impending stockout risk accurately', () => {
    const computeDaysOfCover = (availableStock: number, dailyDemandRate: number) => {
      if (dailyDemandRate <= 0) return 999;
      return Number((availableStock / dailyDemandRate).toFixed(1));
    };

    const isStockoutCritical = (daysOfCover: number, leadTimeDays: number) => {
      return daysOfCover <= leadTimeDays;
    };

    const daysOfCover = computeDaysOfCover(12, 3.5);
    expect(daysOfCover).toBe(3.4);

    // Supplier lead time is 7 days, cover is 3.4 days -> Critical Risk
    expect(isStockoutCritical(daysOfCover, 7)).toBe(true);

    // Healthy item: 150 units, 5/day demand = 30 days cover, lead time 5 days
    const healthyCover = computeDaysOfCover(150, 5);
    expect(healthyCover).toBe(30);
    expect(isStockoutCritical(healthyCover, 5)).toBe(false);
  });
});
