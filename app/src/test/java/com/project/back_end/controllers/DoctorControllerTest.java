package com.project.back_end.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.back_end.DTO.AuthDTO;
import com.project.back_end.enums.ServiceResult;
import com.project.back_end.exceptions.InvalidCredentialsException;
import com.project.back_end.exceptions.InvalidTokenException;
import com.project.back_end.models.Doctor;
import com.project.back_end.services.DoctorService;
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

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DoctorController.class)
@TestPropertySource(properties = "api.path=/")
@DisplayName("DoctorController — /doctor")
class DoctorControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @MockitoBean DoctorService doctorService;
    @MockitoBean Service service;
    @MockitoBean ValidationService validationService;

    private static final String BEARER = "Bearer token123";
    private static final String TOKEN = "token123";

    private Doctor validDoctor() {
        Doctor d = new Doctor();
        d.setName("Jane Smith");
        d.setSpecialty("Cardiology");
        d.setEmail("jane@example.com");
        d.setPassword("secret123");
        d.setPhone("1234567890");
        d.setAvailableTimes(List.of("09:00-10:00"));
        return d;
    }

    @Nested
    @DisplayName("POST /doctor/login")
    class Login {

        @Test
        @DisplayName("valid credentials returns 200 with token")
        void validCredentials_returns200() throws Exception {
            AuthDTO.LoginRequest body = new AuthDTO.LoginRequest("jane@example.com", "secret123");
            when(validationService.validateDoctorLogin("jane@example.com", "secret123"))
                    .thenReturn("mocked.jwt.token");

            mockMvc.perform(post("/doctor/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(body)))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.token").value("mocked.jwt.token"));
        }

        @Test
        @DisplayName("invalid credentials returns 401 with error")
        void invalidCredentials_returns401() throws Exception {
            AuthDTO.LoginRequest body = new AuthDTO.LoginRequest("jane@example.com", "wrongpass");
            when(validationService.validateDoctorLogin("jane@example.com", "wrongpass"))
                    .thenThrow(new InvalidCredentialsException("Invalid email or password"));

            mockMvc.perform(post("/doctor/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(body)))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.error").value("Invalid email or password"));
        }

        @Test
        @DisplayName("missing body returns 400")
        void missingBody_returns400() throws Exception {
            mockMvc.perform(post("/doctor/login").contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").exists());
        }
    }

    @Nested
    @DisplayName("GET /doctor")
    class GetDoctors {

        @Test
        @DisplayName("returns 200 with doctors list")
        void returns200WithDoctors() throws Exception {
            when(doctorService.getDoctors()).thenReturn(List.of(validDoctor()));

            mockMvc.perform(get("/doctor"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.doctors").isArray())
                    .andExpect(jsonPath("$.doctors.length()").value(1));
        }

        @Test
        @DisplayName("empty list returns 200 with empty array")
        void emptyList_returns200() throws Exception {
            when(doctorService.getDoctors()).thenReturn(List.of());

            mockMvc.perform(get("/doctor"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.doctors").isArray())
                    .andExpect(jsonPath("$.doctors.length()").value(0));
        }
    }

    @Nested
    @DisplayName("GET /doctor/availability/{user}/{doctorId}/{date}")
    class GetAvailability {

        @Test
        @DisplayName("valid token returns 200 with availability slots")
        void validToken_returns200() throws Exception {
            LocalDateTime date = LocalDateTime.of(2024, 1, 15, 9, 0);
            when(service.extractToken(BEARER)).thenReturn(TOKEN);
            when(doctorService.getDoctorAvailability(1L, date))
                    .thenReturn(List.of("09:00-10:00", "10:00-11:00"));

            mockMvc.perform(get("/doctor/availability/patient/1/2024-01-15T09:00:00")
                            .header("Authorization", BEARER))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.availability").isArray())
                    .andExpect(jsonPath("$.availability.length()").value(2));
        }

        @Test
        @DisplayName("invalid token returns 401")
        void invalidToken_returns401() throws Exception {
            when(service.extractToken(BEARER)).thenReturn(TOKEN);
            doThrow(new InvalidTokenException("Invalid token"))
                    .when(validationService).validateToken(TOKEN, "patient");

            mockMvc.perform(get("/doctor/availability/patient/1/2024-01-15T09:00:00")
                            .header("Authorization", BEARER))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.error").value("Invalid token"));
        }
    }

    @Nested
    @DisplayName("POST /doctor/ (saveDoctor)")
    class SaveDoctor {

        @Test
        @DisplayName("success returns 200")
        void success_returns200() throws Exception {
            when(service.extractToken(BEARER)).thenReturn(TOKEN);
            when(doctorService.saveDoctor(any(Doctor.class))).thenReturn(ServiceResult.SUCCESS);

            mockMvc.perform(post("/doctor/")
                            .header("Authorization", BEARER)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validDoctor())))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value("doctor saved successfully"));
        }

        @Test
        @DisplayName("conflict returns 409 with error")
        void conflict_returns409() throws Exception {
            when(service.extractToken(BEARER)).thenReturn(TOKEN);
            when(doctorService.saveDoctor(any(Doctor.class))).thenReturn(ServiceResult.CONFLICT);

            mockMvc.perform(post("/doctor/")
                            .header("Authorization", BEARER)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validDoctor())))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.error").value("failed to save doctor"));
        }

        @Test
        @DisplayName("duplicate returns 409 with error")
        void duplicate_returns409() throws Exception {
            when(service.extractToken(BEARER)).thenReturn(TOKEN);
            when(doctorService.saveDoctor(any(Doctor.class))).thenReturn(ServiceResult.DUPLICATE);

            mockMvc.perform(post("/doctor/")
                            .header("Authorization", BEARER)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validDoctor())))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.error").value("doctor already exists"));
        }

        @Test
        @DisplayName("invalid token returns 401")
        void invalidToken_returns401() throws Exception {
            when(service.extractToken(BEARER)).thenReturn(TOKEN);
            doThrow(new InvalidTokenException("Invalid token"))
                    .when(validationService).validateToken(TOKEN, "admin");

            mockMvc.perform(post("/doctor/")
                            .header("Authorization", BEARER)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validDoctor())))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("PUT /doctor/ (updateDoctor)")
    class UpdateDoctor {

        @Test
        @DisplayName("success returns 200")
        void success_returns200() throws Exception {
            when(service.extractToken(BEARER)).thenReturn(TOKEN);
            when(doctorService.updateDoctor(any(Doctor.class))).thenReturn(ServiceResult.SUCCESS);

            mockMvc.perform(put("/doctor/")
                            .header("Authorization", BEARER)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validDoctor())))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value("doctor updated"));
        }

        @Test
        @DisplayName("not found returns 404")
        void notFound_returns404() throws Exception {
            when(service.extractToken(BEARER)).thenReturn(TOKEN);
            when(doctorService.updateDoctor(any(Doctor.class))).thenReturn(ServiceResult.NOT_FOUND);

            mockMvc.perform(put("/doctor/")
                            .header("Authorization", BEARER)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validDoctor())))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.error").value("doctor does not exist"));
        }

        @Test
        @DisplayName("conflict returns 409")
        void conflict_returns409() throws Exception {
            when(service.extractToken(BEARER)).thenReturn(TOKEN);
            when(doctorService.updateDoctor(any(Doctor.class))).thenReturn(ServiceResult.CONFLICT);

            mockMvc.perform(put("/doctor/")
                            .header("Authorization", BEARER)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validDoctor())))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.error").value("failed to update"));
        }

        @Test
        @DisplayName("invalid token returns 401")
        void invalidToken_returns401() throws Exception {
            when(service.extractToken(BEARER)).thenReturn(TOKEN);
            doThrow(new InvalidTokenException("Invalid token"))
                    .when(validationService).validateToken(TOKEN, "admin");

            mockMvc.perform(put("/doctor/")
                            .header("Authorization", BEARER)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validDoctor())))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("DELETE /doctor/{id}")
    class DeleteDoctor {

        @Test
        @DisplayName("success returns 200")
        void success_returns200() throws Exception {
            when(service.extractToken(BEARER)).thenReturn(TOKEN);
            when(doctorService.deleteDoctor(1L)).thenReturn(ServiceResult.SUCCESS);

            mockMvc.perform(delete("/doctor/1")
                            .header("Authorization", BEARER))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value("doctor deleted successfully"));
        }

        @Test
        @DisplayName("not found returns 404")
        void notFound_returns404() throws Exception {
            when(service.extractToken(BEARER)).thenReturn(TOKEN);
            when(doctorService.deleteDoctor(1L)).thenReturn(ServiceResult.NOT_FOUND);

            mockMvc.perform(delete("/doctor/1")
                            .header("Authorization", BEARER))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.error").value("doctor does not exist"));
        }

        @Test
        @DisplayName("unexpected service result returns 500")
        void unexpectedResult_returns500() throws Exception {
            when(service.extractToken(BEARER)).thenReturn(TOKEN);
            when(doctorService.deleteDoctor(1L)).thenReturn(ServiceResult.CONFLICT);

            mockMvc.perform(delete("/doctor/1")
                            .header("Authorization", BEARER))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.error").value("failed to delete doctor"));
        }

        @Test
        @DisplayName("invalid token returns 401")
        void invalidToken_returns401() throws Exception {
            when(service.extractToken(BEARER)).thenReturn(TOKEN);
            doThrow(new InvalidTokenException("Invalid token"))
                    .when(validationService).validateToken(TOKEN, "admin");

            mockMvc.perform(delete("/doctor/1")
                            .header("Authorization", BEARER))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("GET /doctor/filter/{name}/{time}/{speciality}")
    class Filter {

        @Test
        @DisplayName("returns 200 with matching doctors")
        void returns200WithMatchingDoctors() throws Exception {
            when(service.filterDoctor("Smith", "Cardiology", "09:00-10:00"))
                    .thenReturn(List.of(validDoctor()));

            mockMvc.perform(get("/doctor/filter/Smith/09:00-10:00/Cardiology"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.doctors").isArray())
                    .andExpect(jsonPath("$.doctors.length()").value(1));
        }

        @Test
        @DisplayName("no matches returns 200 with empty array")
        void noMatches_returnsEmptyList() throws Exception {
            when(service.filterDoctor("Unknown", "Surgery", "none"))
                    .thenReturn(List.of());

            mockMvc.perform(get("/doctor/filter/Unknown/none/Surgery"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.doctors").isArray())
                    .andExpect(jsonPath("$.doctors.length()").value(0));
        }
    }
}
