package com.aiops.telemetry.controller;

import com.aiops.security.SecurityUtils;
import com.aiops.telemetry.service.TelemetryStreamService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/v1/telemetry")
public class TelemetryController {

    private final TelemetryStreamService telemetryStreamService;

    public TelemetryController(TelemetryStreamService telemetryStreamService) {
        this.telemetryStreamService = telemetryStreamService;
    }

    @GetMapping(path = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream() {
        String tenantId = SecurityUtils.getCurrentTenantId();
        return telemetryStreamService.subscribe(tenantId);
    }
}
