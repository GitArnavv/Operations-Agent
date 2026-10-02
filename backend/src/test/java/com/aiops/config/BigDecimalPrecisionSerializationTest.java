package com.aiops.config;

import com.aiops.dto.FinancialCalculationDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("dev")
class BigDecimalPrecisionSerializationTest {

    @Autowired
    private ObjectMapper objectMapper;

    static class SampleRateAndFinancialModel {
        public BigDecimal amount;
        public BigDecimal gstRate;
        public BigDecimal discountRate;
        public BigDecimal precisionEdgeValue;

        public SampleRateAndFinancialModel(BigDecimal amount, BigDecimal gstRate, BigDecimal discountRate, BigDecimal precisionEdgeValue) {
            this.amount = amount;
            this.gstRate = gstRate;
            this.discountRate = discountRate;
            this.precisionEdgeValue = precisionEdgeValue;
        }
    }

    @Test
    @DisplayName("Serializes BigDecimals to exactly 2 decimal places using HALF_UP rounding")
    void testHalfUpRoundingSerialization() throws Exception {
        SampleRateAndFinancialModel model = new SampleRateAndFinancialModel(
                new BigDecimal("12500.555"), // Should round UP to 12500.56
                new BigDecimal("18.454"),    // Should round DOWN to 18.45
                new BigDecimal("5.0"),       // Should pad to 5.00
                new BigDecimal("0.005")      // Should round UP to 0.01
        );

        String json = objectMapper.writeValueAsString(model);

        assertTrue(json.contains("\"amount\":12500.56"), "Should round 12500.555 up to 12500.56");
        assertTrue(json.contains("\"gstRate\":18.45"), "Should round 18.454 down to 18.45");
        assertTrue(json.contains("\"discountRate\":5.00"), "Should format 5.0 with exactly 2 decimal places: 5.00");
        assertTrue(json.contains("\"precisionEdgeValue\":0.01"), "Should round 0.005 up to 0.01");
    }

    @Test
    @DisplayName("Enforces standard 2-decimal-place INR without IEEE 754 floating-point artifacts")
    void testNoFloatingPointArtifacts() throws Exception {
        // In standard double, 0.1 + 0.2 = 0.30000000000000004
        BigDecimal value1 = new BigDecimal("0.10");
        BigDecimal value2 = new BigDecimal("0.20");
        BigDecimal sum = value1.add(value2);

        SampleRateAndFinancialModel model = new SampleRateAndFinancialModel(
                sum,
                new BigDecimal("18.00"),
                BigDecimal.ZERO,
                new BigDecimal("10000000.00") // Large INR 1 Crore amount without scientific notation
        );

        String json = objectMapper.writeValueAsString(model);

        assertTrue(json.contains("\"amount\":0.30"), "Exact sum 0.30 without float artifacts");
        assertFalse(json.contains("0.30000000000000004"), "Must not contain float artifact");
        assertFalse(json.contains("1E+7") || json.contains("1E7"), "Must not use scientific notation");
        assertTrue(json.contains("\"precisionEdgeValue\":10000000.00"), "Must output plain string format for 1 Crore");
    }

    @Test
    @DisplayName("Verifies FinancialCalculationDto GST math and INR currency serialization")
    void testFinancialCalculationDto() throws Exception {
        FinancialCalculationDto dto = FinancialCalculationDto.calculate(
                "INV-2026-TEST",
                "27AABCS1429B1Z2",
                true, // Intra-state (Maharashtra to Maharashtra) -> CGST 9% + SGST 9%
                new BigDecimal("100000.00"), // Subtotal: ₹1,00,000
                new BigDecimal("10.00"),     // 10% discount -> Taxable: ₹90,000
                new BigDecimal("18.00"),     // 18% GST -> 9% CGST (₹8,100) + 9% SGST (₹8,100)
                new BigDecimal("12.50"),
                new BigDecimal("94.80")
        );

        assertEquals(new BigDecimal("10000.00"), dto.getDiscountAmount());
        assertEquals(new BigDecimal("90000.00"), dto.getTaxableAmount());
        assertEquals(new BigDecimal("8100.00"), dto.getCgstAmount());
        assertEquals(new BigDecimal("8100.00"), dto.getSgstAmount());
        assertEquals(new BigDecimal("0.00"), dto.getIgstAmount());
        assertEquals(new BigDecimal("16200.00"), dto.getTotalTaxAmount());
        assertEquals(new BigDecimal("106200.00"), dto.getTotalPayableInr());

        String json = objectMapper.writeValueAsString(dto);

        assertTrue(json.contains("\"totalPayableInr\":106200.00"));
        assertTrue(json.contains("\"cgstAmount\":8100.00"));
        assertTrue(json.contains("\"sgstAmount\":8100.00"));
        assertTrue(json.contains("\"formattedPayableInr\":\"₹1,06,200.00\""));
    }
}
