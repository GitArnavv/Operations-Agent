package com.aiops.telemetry.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * Production Micrometer Metrics Collector for AI Operations & Agent Workloads.
 * 
 * Captures:
 * 1. Gemini Flash call latency (Timer with percentiles/SLAs)
 * 2. Token consumption per request (DistributionSummary for prompt, completion, total)
 * 3. Agent tool invocation counts and failure rates (Counters)
 */
@Component
public class AiOpsMetrics {

    private static final Logger log = LoggerFactory.getLogger(AiOpsMetrics.class);

    private final MeterRegistry meterRegistry;

    // Cache metrics to avoid recreating Meter instances repeatedly
    private final ConcurrentMap<String, Timer> latencyTimers = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, DistributionSummary> tokenSummaries = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Counter> toolCounters = new ConcurrentHashMap<>();

    public AiOpsMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        log.info("Initialized AiOpsMetrics with MeterRegistry [{}]", meterRegistry.getClass().getSimpleName());
    }

    // ==========================================
    // 1. GEMINI FLASH CALL LATENCY (Timer)
    // ==========================================

    /**
     * Starts a timer sample to measure Gemini Flash execution duration.
     */
    public Timer.Sample startGeminiTimer() {
        return Timer.start(meterRegistry);
    }

    /**
     * Stops a timer sample and records latency tagged with model, status, and tenant.
     */
    public void stopGeminiTimer(Timer.Sample sample, String model, String status, String tenantId) {
        if (sample == null) {
            return;
        }
        Timer timer = getOrCreateLatencyTimer(model, status, tenantId);
        sample.stop(timer);
    }

    /**
     * Directly records latency in milliseconds for a Gemini Flash call.
     */
    public void recordGeminiLatency(long durationMs, String model, String status, String tenantId) {
        Timer timer = getOrCreateLatencyTimer(model, status, tenantId);
        timer.record(durationMs, TimeUnit.MILLISECONDS);
    }

    /**
     * Executes a supplier while recording its execution latency in Micrometer.
     */
    public <T> T recordGeminiExecution(String model, String tenantId, Supplier<T> operation) {
        Timer.Sample sample = startGeminiTimer();
        String status = "SUCCESS";
        try {
            return operation.get();
        } catch (Exception e) {
            status = "FAILURE";
            throw e;
        } finally {
            stopGeminiTimer(sample, model, status, tenantId);
        }
    }

    private Timer getOrCreateLatencyTimer(String model, String status, String tenantId) {
        String safeModel = model != null ? model : "gemini-1.5-flash";
        String safeStatus = status != null ? status : "SUCCESS";
        String safeTenant = tenantId != null ? tenantId : "unknown";
        String key = safeModel + ":" + safeStatus + ":" + safeTenant;

        return latencyTimers.computeIfAbsent(key, k -> Timer.builder("aiops.gemini.flash.latency")
                .description("Latency of Gemini Flash LLM calls in milliseconds")
                .tag("model", safeModel)
                .tag("status", safeStatus)
                .tag("tenant", safeTenant)
                .publishPercentileHistogram()
                .minimumExpectedValue(Duration.ofMillis(50))
                .maximumExpectedValue(Duration.ofSeconds(30))
                .register(meterRegistry));
    }

    // ==========================================
    // 2. TOKEN CONSUMPTION (DistributionSummary)
    // ==========================================

    /**
     * Records prompt, completion, and total token consumption for a request.
     */
    public void recordTokens(int promptTokens, int completionTokens, String model, String tenantId) {
        String safeModel = model != null ? model : "gemini-1.5-flash";
        String safeTenant = tenantId != null ? tenantId : "unknown";

        if (promptTokens > 0) {
            getOrCreateTokenSummary("prompt", safeModel, safeTenant).record(promptTokens);
        }
        if (completionTokens > 0) {
            getOrCreateTokenSummary("completion", safeModel, safeTenant).record(completionTokens);
        }
        int total = promptTokens + completionTokens;
        if (total > 0) {
            getOrCreateTokenSummary("total", safeModel, safeTenant).record(total);
        }
    }

    private DistributionSummary getOrCreateTokenSummary(String type, String model, String tenantId) {
        String key = type + ":" + model + ":" + tenantId;
        return tokenSummaries.computeIfAbsent(key, k -> DistributionSummary.builder("aiops.gemini.token.consumption")
                .description("Distribution of tokens consumed per Gemini request")
                .baseUnit("tokens")
                .tag("type", type)
                .tag("model", model)
                .tag("tenant", tenantId)
                .minimumExpectedValue(1.0)
                .maximumExpectedValue(100000.0)
                .register(meterRegistry));
    }

    // ==========================================
    // 3. AGENT TOOL INVOCATIONS & FAILURES (Counters)
    // ==========================================

    /**
     * Records a successful tool invocation.
     */
    public void recordToolSuccess(String toolName, String tenantId) {
        recordToolInvocation(toolName, "SUCCESS", tenantId, "none");
    }

    /**
     * Records a failed tool invocation with an error classification tag.
     */
    public void recordToolFailure(String toolName, String tenantId, String errorType) {
        String safeError = errorType != null && !errorType.isBlank() ? errorType : "UNHANDLED_EXCEPTION";
        recordToolInvocation(toolName, "FAILURE", tenantId, safeError);
    }

    private void recordToolInvocation(String toolName, String status, String tenantId, String errorType) {
        String safeTool = toolName != null ? toolName : "unknown_tool";
        String safeStatus = status != null ? status : "SUCCESS";
        String safeTenant = tenantId != null ? tenantId : "unknown";
        String safeError = errorType != null ? errorType : "none";

        String key = safeTool + ":" + safeStatus + ":" + safeTenant + ":" + safeError;
        Counter counter = toolCounters.computeIfAbsent(key, k -> Counter.builder("aiops.agent.tool.invocations")
                .description("Counts total tool invocations and outcome failure rates by tool and tenant")
                .tag("tool", safeTool)
                .tag("status", safeStatus)
                .tag("tenant", safeTenant)
                .tag("error_type", safeError)
                .register(meterRegistry));

        counter.increment();
    }

    public MeterRegistry getMeterRegistry() {
        return meterRegistry;
    }
}
