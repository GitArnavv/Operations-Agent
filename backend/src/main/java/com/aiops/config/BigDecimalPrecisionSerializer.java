package com.aiops.config;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Custom Jackson serializer for {@link BigDecimal} ensuring:
 * 1. Consistent rounding using {@link RoundingMode#HALF_UP}.
 * 2. Restricts rates, percentages, and financial values to 2 decimal places.
 * 3. Enforces standard 2-decimal-place formatting for INR without floating-point artifacts or scientific notation.
 */
public class BigDecimalPrecisionSerializer extends JsonSerializer<BigDecimal> {

    public static final int DEFAULT_SCALE = 2;
    public static final RoundingMode DEFAULT_ROUNDING_MODE = RoundingMode.HALF_UP;

    @Override
    public void serialize(BigDecimal value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        if (value == null) {
            gen.writeNull();
            return;
        }

        BigDecimal scaled = value.setScale(DEFAULT_SCALE, DEFAULT_ROUNDING_MODE);
        // Write raw number string to guarantee exact decimal representation in JSON
        gen.writeNumber(scaled.toPlainString());
    }
}
