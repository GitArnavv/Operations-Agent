package com.aiops.telemetry.event;

import java.time.Instant;

/**
 * Base interface for all tenant-scoped telemetry and domain events.
 */
public interface TelemetryEvent {
    String tenantId();
    String eventType();
    Instant timestamp();
}
