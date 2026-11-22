package com.cloudnative.platform.worker;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(WorkerController.class)
class WorkerControllerTest {
    @Autowired
    MockMvc mockMvc;

    @Test
    void statusIsReady() throws Exception {
        mockMvc.perform(get("/api/v1/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("READY"));
    }

    @Test
    void processesEventWithCorrelationId() throws Exception {
        mockMvc.perform(post("/api/v1/events/process")
                        .header("X-Correlation-ID", "test-correlation")
                        .contentType("application/json")
                        .content("{\"name\":\"build-image\",\"attempt\":1}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.correlationId").value("test-correlation"))
                .andExpect(jsonPath("$.result").value("PROCESSED"));
    }
}
