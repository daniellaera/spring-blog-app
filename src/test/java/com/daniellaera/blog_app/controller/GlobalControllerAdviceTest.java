package com.daniellaera.blog_app.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {TestController.class, GlobalControllerAdvice.class})
class GlobalControllerAdviceTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void whenResponseStatusExceptionThrown_thenGlobalExceptionHandlerHandlesIt() throws Exception {
        // Simulate a GET request to a controller method that throws ResponseStatusException
        mockMvc.perform(get("/test/exception"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Resource not found"));
    }
}
