package com.project.back_end.controllers;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.back_end.DTO.AuthDTO;
import com.project.back_end.services.PatientService;
import com.project.back_end.services.Service;
import com.project.back_end.services.ValidationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@WebMvcTest(PatientController.class)
@TestPropertySource(properties = "api.path=/")
@DisplayName("PatientController — /patient")
public class PatientControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @MockitoBean ValidationService validationService;
    @MockitoBean PatientService patientService;
    @MockitoBean Service service;

    @Nested
    @DisplayName("valid credentials")
    class ValidCredentials {

        @Test
        @DisplayName("returns 200 with token in body")
        void loggedIn_returns200() throws Exception{
            AuthDTO.LoginRequest body = new AuthDTO.LoginRequest("doctor@gmail.com", "doctor12345");
            when(validationService.validatePatientLogin("doctor@gmail.com", "doctor12345"))
                    .thenReturn("mocked.jwt.token");

            mockMvc.perform(post("/patient/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(body)))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.token").value("mocked.jwt.token"));
        }
    }

}
