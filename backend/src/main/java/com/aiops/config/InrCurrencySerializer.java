package com.aiops.config;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * Custom Jackson serializer for Indian Rupee (INR) currency display formatting.
 * Formats values with the Indian numbering system (Lakhs/Crores) and 2 decimal places:
 * e.g., ₹1,06,200.00
 */
public class InrCurrencySerializer extends JsonSerializer<BigDecimal> {

    @Override
    public void serialize(BigDecimal value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        if (value == null) {
            gen.writeNull();
            return;
        }

        BigDecimal scaled = value.setScale(2, RoundingMode.HALF_UP);
        String plain = scaled.toPlainString();
        String[] parts = plain.split("\\.");
        String integerPart = parts[0];
        String fractionPart = parts.length > 1 ? parts[1] : "00";

        boolean negative = integerPart.startsWith("-");
        if (negative) {
            integerPart = integerPart.substring(1);
        }

        StringBuilder sb = new StringBuilder();
        int len = integerPart.length();
        if (len <= 3) {
            sb.append(integerPart);
        } else {
            String lastThree = integerPart.substring(len - 3);
            String remaining = integerPart.substring(0, len - 3);
            StringBuilder remSb = new StringBuilder();
            int remLen = remaining.length();
            int count = 0;
            for (int i = remLen - 1; i >= 0; i--) {
                remSb.append(remaining.charAt(i));
                count++;
                if (count % 2 == 0 && i > 0) {
                    remSb.append(",");
                }
            }
            sb.append(remSb.reverse());
            sb.append(",");
            sb.append(lastThree);
        }

        String formatted = (negative ? "-₹" : "₹") + sb.toString() + "." + fractionPart;
        gen.writeString(formatted);
    }
}
