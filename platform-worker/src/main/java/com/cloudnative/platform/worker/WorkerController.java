package com.cloudnative.platform.worker;

import java.util.Map;
import java.util.UUID;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class WorkerController {
    @GetMapping("/api/v1/status")
    public Map<String, String> status() {
        return Map.of("service", "platform-worker", "status", "READY");
    }

    @PostMapping("/api/v1/events/process")
    public ProcessResponse process(
            @RequestBody Map<String, Object> event,
            @RequestHeader(value = "X-Correlation-ID", required = false) String correlationId) {
        String correlation = correlationId == null || correlationId.isBlank()
                ? UUID.randomUUID().toString()
                : correlationId;
        return new ProcessResponse(UUID.randomUUID().toString(), correlation, "PROCESSED");
    }

    public record ProcessResponse(String eventId, String correlationId, String result) {
    }
}
