package com.aiops.interceptor;

import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * Filter ensuring OpenTelemetry / Micrometer Tracing correlation IDs ('traceId', 'spanId')
 * are extracted, populated into SLF4J MDC, and propagated onto HTTP response headers.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 5)
public class TraceContextFilter extends OncePerRequestFilter {

    public static final String TRACE_ID_HEADER = "X-Trace-Id";
    public static final String SPAN_ID_HEADER = "X-Span-Id";
    public static final String TRACE_ID_MDC_KEY = "traceId";
    public static final String SPAN_ID_MDC_KEY = "spanId";
    public static final String TENANT_ID_MDC_KEY = "tenantId";

    private final ObjectProvider<Tracer> tracerProvider;

    public TraceContextFilter(ObjectProvider<Tracer> tracerProvider) {
        this.tracerProvider = tracerProvider;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String traceId = null;
        String spanId = null;

        // 1. Try to obtain active trace and span IDs from Micrometer / OpenTelemetry Tracer
        Tracer tracer = tracerProvider.getIfAvailable();
        if (tracer != null) {
            Span currentSpan = tracer.currentSpan();
            if (currentSpan != null && currentSpan.context() != null) {
                traceId = currentSpan.context().traceId();
                spanId = currentSpan.context().spanId();
            }
        }

        // 2. Fallback to inbound headers or generate a clean hex correlation ID
        if (!StringUtils.hasText(traceId)) {
            traceId = request.getHeader(TRACE_ID_HEADER);
            if (!StringUtils.hasText(traceId)) {
                String traceparent = request.getHeader("traceparent");
                if (StringUtils.hasText(traceparent) && traceparent.startsWith("00-")) {
                    String[] parts = traceparent.split("-");
                    if (parts.length >= 3) {
                        traceId = parts[1];
                        spanId = parts[2];
                    }
                }
            }
            if (!StringUtils.hasText(traceId)) {
                traceId = UUID.randomUUID().toString().replace("-", "");
            }
        }

        if (!StringUtils.hasText(spanId)) {
            spanId = request.getHeader(SPAN_ID_HEADER);
            if (!StringUtils.hasText(spanId)) {
                spanId = UUID.randomUUID().toString().replace("-", "").substring(0, 16);
            }
        }

        // 3. Populate MDC for structured application logging
        MDC.put(TRACE_ID_MDC_KEY, traceId);
        MDC.put(SPAN_ID_MDC_KEY, spanId);

        // 4. Inject correlation IDs into downstream response headers
        response.setHeader(TRACE_ID_HEADER, traceId);
        response.setHeader(SPAN_ID_HEADER, spanId);

        try {
            filterChain.doFilter(request, response);
        } finally {
            MDC.remove(TRACE_ID_MDC_KEY);
            MDC.remove(SPAN_ID_MDC_KEY);
            MDC.remove(TENANT_ID_MDC_KEY);
        }
    }
}
