package com.project.back_end.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.back_end.appointment.Appointment;
import com.project.back_end.appointment.AppointmentController;
import com.project.back_end.appointment.AppointmentDTO;
import com.project.back_end.appointment.AppointmentService;
import com.project.back_end.enums.ServiceResult;
import com.project.back_end.exceptions.InvalidTokenException;
import com.project.back_end.doctor.Doctor;
import com.project.back_end.patient.Patient;
import com.project.back_end.shared.Service;
import com.project.back_end.shared.ValidationService;
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
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AppointmentController.class)
@TestPropertySource(properties = "api.path=/")
@DisplayName("AppointmentController — /appointments")
class AppointmentControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @MockitoBean AppointmentService appointmentService;
    @MockitoBean Service service;
    @MockitoBean ValidationService validationService;

    private static final String BEARER = "Bearer token123";
    private static final String TOKEN  = "token123";

    // ── Factories ──────────────────────────────────────────────────────────────

    private Doctor validDoctor() {
        Doctor d = new Doctor();
        d.setId(1L);
        d.setName("Dr. Smith");
        d.setSpecialty("Cardiology");
        d.setEmail("doctor@example.com");
        d.setPassword("password123");
        d.setPhone("1234567890");
        return d;
    }

    private Patient validPatient() {
        Patient p = new Patient();
        p.setId(1L);
        p.setName("John Doe");
        p.setEmail("patient@example.com");
        p.setPassword("pass123");
        p.setPhone("0987654321");
        p.setAddress("123 Main St");
        return p;
    }

    private Appointment validAppointment() {
        Appointment a = new Appointment();
        a.setDoctor(validDoctor());
        a.setPatient(validPatient());
        a.setAppointmentTime(LocalDateTime.of(2027, 6, 15, 10, 0));
        a.setStatus(0);
        return a;
    }

    private AppointmentDTO validAppointmentDTO() {
        return new AppointmentDTO(
                1L, 1L, "Dr. Smith",
                1L, "John Doe", "patient@example.com", "0987654321", "123 Main St",
                LocalDateTime.of(2027, 6, 15, 10, 0), 0);
    }

    // ── GET /appointments/{date}/{patientName}/ ────────────────────────────────

    @Nested
    @DisplayName("GET /appointments/{date}/{patientName}/")
    class GetAppointments {

        @Test
        @DisplayName("valid doctor token returns 200 with appointments array")
        void validToken_returns200WithAppointments() throws Exception {
            when(service.extractToken(BEARER)).thenReturn(TOKEN);
            when(appointmentService.getAppointments("2027-06-15", "John", TOKEN))
                    .thenReturn(List.of(validAppointmentDTO()));

            mockMvc.perform(get("/appointments/2027-06-15/John/")
                            .header("Authorization", BEARER))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.appointments").isArray())
                    .andExpect(jsonPath("$.appointments.length()").value(1));
        }

        @Test
        @DisplayName("no patient filter ('null') returns 200 with empty array")
        void nullPatientFilter_returns200WithEmptyArray() throws Exception {
            when(service.extractToken(BEARER)).thenReturn(TOKEN);
            when(appointmentService.getAppointments("2027-06-15", "null", TOKEN))
                    .thenReturn(List.of());

            mockMvc.perform(get("/appointments/2027-06-15/null/")
                            .header("Authorization", BEARER))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.appointments").isArray())
                    .andExpect(jsonPath("$.appointments.length()").value(0));
        }

        @Test
        @DisplayName("invalid/expired token returns 401")
        void invalidToken_returns401() throws Exception {
            when(service.extractToken(BEARER)).thenReturn(TOKEN);
            doThrow(new InvalidTokenException("Invalid token"))
                    .when(validationService).validateToken(TOKEN, "doctor");

            mockMvc.perform(get("/appointments/2027-06-15/John/")
                            .header("Authorization", BEARER))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.error").value("Invalid token"));
        }
    }

    // ── POST /appointments/ ────────────────────────────────────────────────────

    @Nested
    @DisplayName("POST /appointments/")
    class BookAppointment {

        @Test
        @DisplayName("valid request returns 200 with success message")
        void validRequest_returns200() throws Exception {
            when(service.extractToken(BEARER)).thenReturn(TOKEN);
            when(service.validateAppointment(anyLong(), any(LocalDateTime.class)))
                    .thenReturn(ServiceResult.SUCCESS);
            when(appointmentService.bookAppointment(any(Appointment.class)))
                    .thenReturn(ServiceResult.SUCCESS);

            mockMvc.perform(post("/appointments/")
                            .header("Authorization", BEARER)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validAppointment())))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Appointment booked successfully"));
        }

        @Test
        @DisplayName("invalid/expired token returns 401")
        void invalidToken_returns401() throws Exception {
            when(service.extractToken(BEARER)).thenReturn(TOKEN);
            doThrow(new InvalidTokenException("Invalid token"))
                    .when(validationService).validateToken(TOKEN, "patient");

            mockMvc.perform(post("/appointments/")
                            .header("Authorization", BEARER)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validAppointment())))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.error").value("Invalid token"));
        }

        @Test
        @DisplayName("missing request body returns 400")
        void missingBody_returns400() throws Exception {
            when(service.extractToken(BEARER)).thenReturn(TOKEN);

            mockMvc.perform(post("/appointments/")
                            .header("Authorization", BEARER)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").exists());
        }

        @Test
        @DisplayName("null doctor field returns 400 with explicit error message")
        void nullDoctor_returns400() throws Exception {
            when(service.extractToken(BEARER)).thenReturn(TOKEN);

            Appointment appt = new Appointment();
            appt.setDoctor(null);
            appt.setAppointmentTime(LocalDateTime.of(2027, 6, 15, 10, 0));
            appt.setStatus(0);

            mockMvc.perform(post("/appointments/")
                            .header("Authorization", BEARER)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(appt)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("Doctor and appointment time are required"));
        }

        @Test
        @DisplayName("null appointmentTime returns 400 with explicit error message")
        void nullAppointmentTime_returns400() throws Exception {
            when(service.extractToken(BEARER)).thenReturn(TOKEN);

            Appointment appt = new Appointment();
            appt.setDoctor(validDoctor());
            appt.setAppointmentTime(null);
            appt.setStatus(0);

            mockMvc.perform(post("/appointments/")
                            .header("Authorization", BEARER)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(appt)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("Doctor and appointment time are required"));
        }

        @Test
        @DisplayName("doctor NOT_FOUND from validateAppointment returns 400")
        void doctorNotFound_returns400() throws Exception {
            when(service.extractToken(BEARER)).thenReturn(TOKEN);
            when(service.validateAppointment(anyLong(), any(LocalDateTime.class)))
                    .thenReturn(ServiceResult.NOT_FOUND);

            mockMvc.perform(post("/appointments/")
                            .header("Authorization", BEARER)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validAppointment())))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("Doctor ID is invalid"));
        }

        @Test
        @DisplayName("slot CONFLICT from validateAppointment returns 409")
        void slotConflict_returns409() throws Exception {
            when(service.extractToken(BEARER)).thenReturn(TOKEN);
            when(service.validateAppointment(anyLong(), any(LocalDateTime.class)))
                    .thenReturn(ServiceResult.CONFLICT);

            mockMvc.perform(post("/appointments/")
                            .header("Authorization", BEARER)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validAppointment())))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.error").value("The slot is already taken"));
        }

        @Test
        @DisplayName("bookAppointment returns non-SUCCESS → 500")
        void bookingFails_returns500() throws Exception {
            when(service.extractToken(BEARER)).thenReturn(TOKEN);
            when(service.validateAppointment(anyLong(), any(LocalDateTime.class)))
                    .thenReturn(ServiceResult.SUCCESS);
            when(appointmentService.bookAppointment(any(Appointment.class)))
                    .thenReturn(ServiceResult.CONFLICT);

            mockMvc.perform(post("/appointments/")
                            .header("Authorization", BEARER)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validAppointment())))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.error").value("Failed to book appointment"));
        }
    }

    // ── PUT /appointments/ ─────────────────────────────────────────────────────

    @Nested
    @DisplayName("PUT /appointments/")
    class UpdateAppointment {

        @Test
        @DisplayName("SUCCESS returns 200 with success message")
        void success_returns200() throws Exception {
            when(service.extractToken(BEARER)).thenReturn(TOKEN);
            when(appointmentService.updateAppointment(any(Appointment.class), eq(TOKEN)))
                    .thenReturn(ServiceResult.SUCCESS);

            mockMvc.perform(put("/appointments/")
                            .header("Authorization", BEARER)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validAppointment())))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value("Appointment updated"));
        }

        @Test
        @DisplayName("invalid/expired token returns 401")
        void invalidToken_returns401() throws Exception {
            when(service.extractToken(BEARER)).thenReturn(TOKEN);
            doThrow(new InvalidTokenException("Invalid token"))
                    .when(validationService).validateToken(TOKEN, "patient");

            mockMvc.perform(put("/appointments/")
                            .header("Authorization", BEARER)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validAppointment())))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.error").value("Invalid token"));
        }

        @Test
        @DisplayName("missing body returns 400")
        void missingBody_returns400() throws Exception {
            when(service.extractToken(BEARER)).thenReturn(TOKEN);

            mockMvc.perform(put("/appointments/")
                            .header("Authorization", BEARER)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").exists());
        }

        @Test
        @DisplayName("NOT_FOUND returns 404 with error message")
        void notFound_returns404() throws Exception {
            when(service.extractToken(BEARER)).thenReturn(TOKEN);
            when(appointmentService.updateAppointment(any(Appointment.class), eq(TOKEN)))
                    .thenReturn(ServiceResult.NOT_FOUND);

            mockMvc.perform(put("/appointments/")
                            .header("Authorization", BEARER)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validAppointment())))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.error").value("The appointment does not exist"));
        }

        @Test
        @DisplayName("CONFLICT (doctor unavailable) returns 409")
        void conflict_doctorUnavailable_returns409() throws Exception {
            when(service.extractToken(BEARER)).thenReturn(TOKEN);
            when(appointmentService.updateAppointment(any(Appointment.class), eq(TOKEN)))
                    .thenReturn(ServiceResult.CONFLICT);

            mockMvc.perform(put("/appointments/")
                            .header("Authorization", BEARER)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validAppointment())))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.error").value("The doctor is unavailable at this time"));
        }

        @Test
        @DisplayName("DUPLICATE (slot taken) returns 409")
        void duplicate_slotTaken_returns409() throws Exception {
            when(service.extractToken(BEARER)).thenReturn(TOKEN);
            when(appointmentService.updateAppointment(any(Appointment.class), eq(TOKEN)))
                    .thenReturn(ServiceResult.DUPLICATE);

            mockMvc.perform(put("/appointments/")
                            .header("Authorization", BEARER)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validAppointment())))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.error").value("The slot is already taken"));
        }

        @Test
        @DisplayName("UNAUTHORIZED returns 401 with error message")
        void unauthorized_returns401() throws Exception {
            when(service.extractToken(BEARER)).thenReturn(TOKEN);
            when(appointmentService.updateAppointment(any(Appointment.class), eq(TOKEN)))
                    .thenReturn(ServiceResult.UNAUTHORIZED);

            mockMvc.perform(put("/appointments/")
                            .header("Authorization", BEARER)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validAppointment())))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.error").value("Unauthorized access to this appointment"));
        }
    }

    // ── DELETE /appointments/{id} ──────────────────────────────────────────────

    @Nested
    @DisplayName("DELETE /appointments/{id}")
    class CancelAppointment {

        @Test
        @DisplayName("SUCCESS returns 200 with success message")
        void success_returns200() throws Exception {
            when(service.extractToken(BEARER)).thenReturn(TOKEN);
            when(appointmentService.cancelAppointment(1L, TOKEN))
                    .thenReturn(ServiceResult.SUCCESS);

            mockMvc.perform(delete("/appointments/1")
                            .header("Authorization", BEARER))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value("Appointment deleted successfully"));
        }

        @Test
        @DisplayName("invalid/expired token returns 401")
        void invalidToken_returns401() throws Exception {
            when(service.extractToken(BEARER)).thenReturn(TOKEN);
            doThrow(new InvalidTokenException("Invalid token"))
                    .when(validationService).validateToken(TOKEN, "patient");

            mockMvc.perform(delete("/appointments/1")
                            .header("Authorization", BEARER))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.error").value("Invalid token"));
        }

        @Test
        @DisplayName("NOT_FOUND returns 404 with error message")
        void notFound_returns404() throws Exception {
            when(service.extractToken(BEARER)).thenReturn(TOKEN);
            when(appointmentService.cancelAppointment(1L, TOKEN))
                    .thenReturn(ServiceResult.NOT_FOUND);

            mockMvc.perform(delete("/appointments/1")
                            .header("Authorization", BEARER))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.error").value("The appointment does not exist"));
        }

        @Test
        @DisplayName("CONFLICT (delete failed) returns 409")
        void conflict_deleteFailed_returns409() throws Exception {
            when(service.extractToken(BEARER)).thenReturn(TOKEN);
            when(appointmentService.cancelAppointment(1L, TOKEN))
                    .thenReturn(ServiceResult.CONFLICT);

            mockMvc.perform(delete("/appointments/1")
                            .header("Authorization", BEARER))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.error").value("Failed to delete appointment"));
        }

        @Test
        @DisplayName("DUPLICATE returns 409 with slot-taken message")
        void duplicate_returns409() throws Exception {
            when(service.extractToken(BEARER)).thenReturn(TOKEN);
            when(appointmentService.cancelAppointment(1L, TOKEN))
                    .thenReturn(ServiceResult.DUPLICATE);

            mockMvc.perform(delete("/appointments/1")
                            .header("Authorization", BEARER))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.error").value("The slot is already taken"));
        }

        @Test
        @DisplayName("UNAUTHORIZED returns 401 with error message")
        void unauthorized_returns401() throws Exception {
            when(service.extractToken(BEARER)).thenReturn(TOKEN);
            when(appointmentService.cancelAppointment(1L, TOKEN))
                    .thenReturn(ServiceResult.UNAUTHORIZED);

            mockMvc.perform(delete("/appointments/1")
                            .header("Authorization", BEARER))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.error").value("Unauthorized access to this appointment"));
        }
    }
}
