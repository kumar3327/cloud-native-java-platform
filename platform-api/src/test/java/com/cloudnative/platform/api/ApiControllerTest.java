package com.cloudnative.platform.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestTemplate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ApiController.class)
class ApiControllerTest {
    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    RestTemplate restTemplate;

    @Test
    void statusIsReady() throws Exception {
        mockMvc.perform(get("/api/v1/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("READY"));
    }

    @Test
    void createsJobAndForwardsCorrelationId() throws Exception {
        when(restTemplate.postForObject(
                eq("http://localhost:8081/api/v1/events/process"),
                any(), eq(ApiController.WorkerResponse.class)))
                .thenReturn(new ApiController.WorkerResponse("event-1", "test-correlation", "PROCESSED"));

        mockMvc.perform(post("/api/v1/jobs")
                        .header("X-Correlation-ID", "test-correlation")
                        .contentType("application/json")
                        .content("{\"name\":\"build-image\",\"retries\":2}"))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.correlationId").value("test-correlation"))
                .andExpect(jsonPath("$.result").value("PROCESSED"));
    }
}
