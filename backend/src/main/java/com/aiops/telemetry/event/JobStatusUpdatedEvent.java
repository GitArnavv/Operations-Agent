package com.aiops.telemetry.event;

import java.time.Instant;

public class JobStatusUpdatedEvent implements TelemetryEvent {
    private final String tenantId;
    private final String jobId;
    private final String jobName;
    private final String status;
    private final Instant timestamp;

    public JobStatusUpdatedEvent(String tenantId, String jobId, String jobName, String status) {
        this.tenantId = tenantId;
        this.jobId = jobId;
        this.jobName = jobName;
        this.status = status;
        this.timestamp = Instant.now();
    }

    @Override
    public String tenantId() {
        return tenantId;
    }

    @Override
    public String eventType() {
        return "JOB_STATUS_UPDATED";
    }

    @Override
    public Instant timestamp() {
        return timestamp;
    }

    public String getJobId() { return jobId; }
    public String getJobName() { return jobName; }
    public String getStatus() { return status; }
}
