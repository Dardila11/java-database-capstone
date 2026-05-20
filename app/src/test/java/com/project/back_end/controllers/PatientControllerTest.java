package com.project.back_end.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.back_end.DTO.AuthDTO;
import com.project.back_end.enums.ServiceResult;
import com.project.back_end.exceptions.InvalidCredentialsException;
import com.project.back_end.exceptions.InvalidTokenException;
import com.project.back_end.models.Patient;
import com.project.back_end.services.PatientService;
import com.project.back_end.services.Service;
import com.project.back_end.services.ValidationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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
    @DisplayName("POST /patient/login")
    class Login {

        @Nested
        @DisplayName("valid credentials")
        class ValidCredentials {

            @Test
            @DisplayName("returns 200 with token in body")
            void login_validCredentials_returns200() throws Exception {
                AuthDTO.LoginRequest body = new AuthDTO.LoginRequest("patient@gmail.com", "secret123");
                when(validationService.validatePatientLogin("patient@gmail.com", "secret123"))
                        .thenReturn("mocked.jwt.token");

                mockMvc.perform(post("/patient/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(body)))
                        .andExpect(status().isOk())
                        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                        .andExpect(jsonPath("$.token").value("mocked.jwt.token"));
            }
        }

        @Nested
        @DisplayName("invalid credentials")
        class InvalidCredentials {

            @Test
            @DisplayName("returns 401 when credentials are wrong")
            void login_wrongCredentials_returns401() throws Exception {
                AuthDTO.LoginRequest body = new AuthDTO.LoginRequest("wrong@gmail.com", "wrongpass");
                when(validationService.validatePatientLogin("wrong@gmail.com", "wrongpass"))
                        .thenThrow(new InvalidCredentialsException("Invalid email or password"));

                mockMvc.perform(post("/patient/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(body)))
                        .andExpect(status().isUnauthorized())
                        .andExpect(jsonPath("$.error").value("Invalid email or password"));
            }
        }

        @Nested
        @DisplayName("invalid request body")
        class InvalidBody {

            @Test
            @DisplayName("returns 400 when email is blank")
            void login_blankEmail_returns400() throws Exception {
                mockMvc.perform(post("/patient/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"email\":\"\",\"password\":\"secret123\"}"))
                        .andExpect(status().isBadRequest());
            }

            @Test
            @DisplayName("returns 400 when email format is invalid")
            void login_malformedEmail_returns400() throws Exception {
                mockMvc.perform(post("/patient/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"email\":\"not-an-email\",\"password\":\"secret123\"}"))
                        .andExpect(status().isBadRequest());
            }

            @Test
            @DisplayName("returns 400 when password is blank")
            void login_blankPassword_returns400() throws Exception {
                mockMvc.perform(post("/patient/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"email\":\"patient@gmail.com\",\"password\":\"\"}"))
                        .andExpect(status().isBadRequest());
            }

            @Test
            @DisplayName("returns 400 when body is missing")
            void login_missingBody_returns400() throws Exception {
                mockMvc.perform(post("/patient/login")
                                .contentType(MediaType.APPLICATION_JSON))
                        .andExpect(status().isBadRequest());
            }
        }
    }

    // ── POST /patient/create ─────────────────────────────────────────────────

    @Nested
    @DisplayName("POST /patient/create")
    class CreatePatient {

        // Patient.password is @JsonProperty WRITE_ONLY so objectMapper.writeValueAsString(Patient)
        // omits it — use a Map to keep the password in the request body.
        private String validPatientJson() throws Exception {
            return objectMapper.writeValueAsString(Map.of(
                    "name", "John Doe",
                    "email", "john@gmail.com",
                    "password", "secret123",
                    "phone", "1234567890",
                    "address", "123 Main St"
            ));
        }

        @Test
        @DisplayName("returns 200 when patient is created successfully")
        void create_validPatient_returns200() throws Exception {
            when(service.validatePatient("john@gmail.com", "1234567890")).thenReturn(true);
            when(patientService.createPatient(any(Patient.class))).thenReturn(ServiceResult.SUCCESS);

            mockMvc.perform(post("/patient/create")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(validPatientJson()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Patient created successfully"));
        }

        @Test
        @DisplayName("returns 400 when email or phone already exists")
        void create_duplicatePatient_returns400() throws Exception {
            when(service.validatePatient("john@gmail.com", "1234567890")).thenReturn(false);

            mockMvc.perform(post("/patient/create")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(validPatientJson()))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("Patient with this email or phone already exists"));
        }

        @Test
        @DisplayName("returns 500 when service fails to persist")
        void create_persistenceFails_returns500() throws Exception {
            when(service.validatePatient("john@gmail.com", "1234567890")).thenReturn(true);
            when(patientService.createPatient(any(Patient.class))).thenReturn(ServiceResult.CONFLICT);

            mockMvc.perform(post("/patient/create")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(validPatientJson()))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.error").value("Failed to create patient"));
        }

        @Test
        @DisplayName("returns 400 when required fields are missing")
        void create_missingFields_returns400() throws Exception {
            mockMvc.perform(post("/patient/create")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isBadRequest());
        }
    }

    // ── GET /patient/ ────────────────────────────────────────────────────────

    @Nested
    @DisplayName("GET /patient/")
    class GetPatient {

        @Test
        @DisplayName("returns 200 with patient details")
        void getPatient_validToken_returns200() throws Exception {
            Patient p = new Patient();
            p.setName("John Doe");
            p.setEmail("john@gmail.com");

            when(service.extractToken("Bearer mocked.jwt.token")).thenReturn("mocked.jwt.token");
            when(patientService.getPatientDetails("mocked.jwt.token")).thenReturn(p);

            mockMvc.perform(get("/patient/")
                            .header("Authorization", "Bearer mocked.jwt.token"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.patient.name").value("John Doe"))
                    .andExpect(jsonPath("$.patient.email").value("john@gmail.com"));
        }

        @Test
        @DisplayName("returns 401 when token is invalid")
        void getPatient_invalidToken_returns401() throws Exception {
            when(service.extractToken("Bearer bad.token")).thenReturn("bad.token");
            doThrow(new InvalidTokenException("Invalid token"))
                    .when(validationService).validateToken("bad.token", "patient");

            mockMvc.perform(get("/patient/")
                            .header("Authorization", "Bearer bad.token"))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.error").value("Invalid token"));
        }
    }

    // ── GET /patient/{id} ────────────────────────────────────────────────────

    @Nested
    @DisplayName("GET /patient/{id}")
    class GetPatientAppointments {

        @Test
        @DisplayName("returns 200 with appointments list")
        void getAppointments_validToken_returns200() throws Exception {
            when(service.extractToken("Bearer mocked.jwt.token")).thenReturn("mocked.jwt.token");
            when(patientService.getPatientAppointments(1L)).thenReturn(List.of());

            mockMvc.perform(get("/patient/1")
                            .header("Authorization", "Bearer mocked.jwt.token"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.appointments").isArray());
        }

        @Test
        @DisplayName("returns 401 when token is invalid")
        void getAppointments_invalidToken_returns401() throws Exception {
            when(service.extractToken("Bearer bad.token")).thenReturn("bad.token");
            doThrow(new InvalidTokenException("Invalid token"))
                    .when(validationService).validateToken("bad.token", "patient");

            mockMvc.perform(get("/patient/1")
                            .header("Authorization", "Bearer bad.token"))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.error").value("Invalid token"));
        }
    }

    // ── GET /patient/filter/{condition}/{name} ───────────────────────────────

    @Nested
    @DisplayName("GET /patient/filter/{condition}/{name}")
    class FilterPatientAppointments {

        @Test
        @DisplayName("returns 200 with filtered appointments")
        void filter_validToken_returns200() throws Exception {
            when(service.extractToken("Bearer mocked.jwt.token")).thenReturn("mocked.jwt.token");
            when(service.filterPatient("mocked.jwt.token", "upcoming", "John"))
                    .thenReturn(List.of());

            mockMvc.perform(get("/patient/filter/upcoming/John")
                            .header("Authorization", "Bearer mocked.jwt.token"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.appointments").isArray());
        }

        @Test
        @DisplayName("returns 401 when token is invalid")
        void filter_invalidToken_returns401() throws Exception {
            when(service.extractToken("Bearer bad.token")).thenReturn("bad.token");
            doThrow(new InvalidTokenException("Invalid token"))
                    .when(validationService).validateToken("bad.token", "patient");

            mockMvc.perform(get("/patient/filter/upcoming/John")
                            .header("Authorization", "Bearer bad.token"))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.error").value("Invalid token"));
        }
    }
}
