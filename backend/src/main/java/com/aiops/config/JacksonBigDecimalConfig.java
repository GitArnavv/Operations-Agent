package com.aiops.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;

/**
 * Jackson configuration module for {@link BigDecimal}.
 * Configures global serialization to apply HALF_UP rounding and exactly 2 decimal places,
 * avoiding IEEE 754 floating-point inaccuracies for financial, tax, inventory, and rate fields.
 */
@Configuration
public class JacksonBigDecimalConfig {

    @Bean
    public SimpleModule bigDecimalPrecisionModule() {
        SimpleModule module = new SimpleModule("BigDecimalPrecisionModule");
        module.addSerializer(BigDecimal.class, new BigDecimalPrecisionSerializer());
        return module;
    }

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jacksonBigDecimalCustomizer() {
        return builder -> {
            // Enable deserializing floating point inputs directly as BigDecimal to eliminate float conversion error
            builder.featuresToEnable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS);
            // Ensure BigDecimals are not written in plain floating-point scientific notation
            builder.featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
            builder.serializerByType(BigDecimal.class, new BigDecimalPrecisionSerializer());
        };
    }
}
