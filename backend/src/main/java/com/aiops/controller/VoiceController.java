package com.aiops.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/voice")
public class VoiceController {

    @PostMapping("/synthesize")
    public ResponseEntity<?> synthesize(@RequestBody Map<String, String> request) {
        String text = request.get("text");
        String lang = request.getOrDefault("language", "en-IN");

        // Returns audio metadata and Web Speech API configuration
        return ResponseEntity.ok(Map.of(
                "status", "READY",
                "language", lang,
                "text", text,
                "provider", "WEB_SPEECH_AND_CLOUD_FALLBACK",
                "pitch", 1.0,
                "rate", 1.05
        ));
    }
}
