package com.aiops.telemetry.service;

import com.aiops.telemetry.event.TelemetryEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class TelemetryStreamService {
    private static final Logger log = LoggerFactory.getLogger(TelemetryStreamService.class);
    private final Map<String, List<SseEmitter>> tenantEmitters = new ConcurrentHashMap<>();

    public SseEmitter subscribe(String tenantId) {
        SseEmitter emitter = new SseEmitter(60 * 60 * 1000L); // 1 hour timeout
        
        tenantEmitters.computeIfAbsent(tenantId, k -> new CopyOnWriteArrayList<>()).add(emitter);
        
        emitter.onCompletion(() -> removeEmitter(tenantId, emitter));
        emitter.onTimeout(() -> removeEmitter(tenantId, emitter));
        emitter.onError((e) -> removeEmitter(tenantId, emitter));
        
        try {
            emitter.send(SseEmitter.event().name("CONNECT").data("Connected to telemetry stream for tenant " + tenantId));
        } catch (IOException e) {
            removeEmitter(tenantId, emitter);
        }
        
        return emitter;
    }

    private void removeEmitter(String tenantId, SseEmitter emitter) {
        List<SseEmitter> emitters = tenantEmitters.get(tenantId);
        if (emitters != null) {
            emitters.remove(emitter);
            if (emitters.isEmpty()) {
                tenantEmitters.remove(tenantId);
            }
        }
    }

    @EventListener
    public void onTelemetryEvent(TelemetryEvent event) {
        String tenantId = event.tenantId();
        List<SseEmitter> emitters = tenantEmitters.get(tenantId);
        
        if (emitters != null && !emitters.isEmpty()) {
            List<SseEmitter> deadEmitters = new CopyOnWriteArrayList<>();
            for (SseEmitter emitter : emitters) {
                try {
                    emitter.send(SseEmitter.event()
                            .name(event.eventType())
                            .data(event));
                } catch (IOException e) {
                    deadEmitters.add(emitter);
                }
            }
            emitters.removeAll(deadEmitters);
        }
    }
}
