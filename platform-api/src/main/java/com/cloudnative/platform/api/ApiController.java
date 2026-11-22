package com.cloudnative.platform.api;

import java.util.Map;
import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
@Validated
public class ApiController {
    private final RestTemplate restTemplate;
    private final String workerUrl;

    public ApiController(RestTemplate restTemplate,
                         @Value("${PLATFORM_WORKER_URL:${platform.worker.url:http://localhost:8081}}") String workerUrl) {
        this.restTemplate = restTemplate;
        this.workerUrl = workerUrl;
    }

    @GetMapping("/api/v1/status")
    public Map<String, String> status() {
        return Map.of("service", "platform-api", "status", "READY");
    }

    @PostMapping("/api/v1/jobs")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public JobResponse createJob(
            @Valid @RequestBody JobRequest request,
            @RequestHeader(value = "X-Correlation-ID", required = false) String correlationId) {
        String correlation = correlationId == null || correlationId.isBlank()
                ? UUID.randomUUID().toString()
                : correlationId;

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Correlation-ID", correlation);
        org.springframework.http.HttpEntity<Map<String, Object>> entity =
                new org.springframework.http.HttpEntity<>(Map.of("name", request.name(), "attempt", 1), headers);

        WorkerResponse worker = restTemplate.postForObject(
                workerUrl + "/api/v1/events/process", entity, WorkerResponse.class);

        return new JobResponse(UUID.randomUUID().toString(), correlation,
                "ACCEPTED", worker == null ? "UNKNOWN" : worker.result());
    }

    public record JobRequest(
            @NotBlank String name,
            @Positive Integer retries) {
    }

    public record JobResponse(String jobId, String correlationId, String status, String result) {
    }

    public record WorkerResponse(String eventId, String correlationId, String result) {
    }
}
