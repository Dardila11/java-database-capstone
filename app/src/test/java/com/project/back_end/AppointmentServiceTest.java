package com.project.back_end;

import com.project.back_end.DTO.AppointmentDTO;
import com.project.back_end.models.Appointment;
import com.project.back_end.models.Doctor;
import com.project.back_end.models.Patient;
import com.project.back_end.repo.AppointmentRepository;
import com.project.back_end.repo.DoctorRepository;
import com.project.back_end.repo.PatientRepository;
import com.project.back_end.services.AppointmentService;
import com.project.back_end.services.TokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AppointmentService")
class AppointmentServiceTest {

    private static final String TOKEN = "some.jwt.token";
    private static final String PATIENT_EMAIL = "patient@example.com";
    private static final String DOCTOR_EMAIL = "doctor@example.com";

    @Mock private AppointmentRepository appointmentRepository;
    @Mock private PatientRepository patientRepository;
    @Mock private DoctorRepository doctorRepository;
    @Mock private TokenService tokenService;

    @InjectMocks
    private AppointmentService appointmentService;

    private Patient patient(long id) {
        Patient p = new Patient();
        p.setId(id);
        p.setName("Test Patient");
        p.setEmail(PATIENT_EMAIL);
        return p;
    }

    private Doctor doctor(long id) {
        Doctor d = new Doctor();
        d.setId(id);
        d.setName("Dr. Smith");
        d.setEmail(DOCTOR_EMAIL);
        d.setAvailableTimes(List.of("09:00", "10:00", "14:00"));
        return d;
    }

    private Appointment scheduledAppointment(long id, Doctor doc, Patient pat, LocalDateTime time) {
        Appointment a = new Appointment();
        a.setId(id);
        a.setDoctor(doc);
        a.setPatient(pat);
        a.setAppointmentTime(time);
        a.setStatus(0);
        return a;
    }

    @Nested
    @DisplayName("bookAppointment()")
    class BookAppointment {

        @Test
        @DisplayName("returns 1 when save succeeds")
        void returns1OnSuccess() {
            Appointment appt = scheduledAppointment(0L, doctor(1L), patient(1L),
                    LocalDateTime.now().plusDays(1));
            when(appointmentRepository.save(any(Appointment.class))).thenReturn(appt);

            assertThat(appointmentService.bookAppointment(appt)).isEqualTo(1);
        }

        @Test
        @DisplayName("returns 0 when repository throws an exception")
        void returns0OnRepositoryException() {
            Appointment appt = scheduledAppointment(0L, doctor(1L), patient(1L),
                    LocalDateTime.now().plusDays(1));
            when(appointmentRepository.save(any(Appointment.class)))
                    .thenThrow(new RuntimeException("DB error"));

            assertThat(appointmentService.bookAppointment(appt)).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("cancelAppointment()")
    class CancelAppointment {

        @Test
        @DisplayName("returns -1 when appointment does not exist")
        void returnsMinusOneWhenNotFound() {
            when(appointmentRepository.findById(99L)).thenReturn(Optional.empty());

            assertThat(appointmentService.cancelAppointment(99L, TOKEN)).isEqualTo(-1);
        }

        @Test
        @DisplayName("returns -2 when token belongs to a different patient")
        void returnsMinusTwoForWrongPatient() {
            Patient owner = patient(1L);
            Patient requester = patient(2L);
            Appointment appt = scheduledAppointment(10L, doctor(1L), owner,
                    LocalDateTime.now().plusDays(1));

            when(appointmentRepository.findById(10L)).thenReturn(Optional.of(appt));
            when(tokenService.extractEmail(TOKEN)).thenReturn(PATIENT_EMAIL);
            when(patientRepository.findByEmail(PATIENT_EMAIL)).thenReturn(Optional.of(requester));

            assertThat(appointmentService.cancelAppointment(10L, TOKEN)).isEqualTo(-2);
        }

        @Test
        @DisplayName("returns -2 when patient is not found in the database")
        void returnsMinusTwoWhenPatientMissing() {
            Appointment appt = scheduledAppointment(10L, doctor(1L), patient(1L),
                    LocalDateTime.now().plusDays(1));

            when(appointmentRepository.findById(10L)).thenReturn(Optional.of(appt));
            when(tokenService.extractEmail(TOKEN)).thenReturn(PATIENT_EMAIL);
            when(patientRepository.findByEmail(PATIENT_EMAIL)).thenReturn(Optional.empty());

            assertThat(appointmentService.cancelAppointment(10L, TOKEN)).isEqualTo(-2);
        }

        @Test
        @DisplayName("returns 1 and deletes the record on success")
        void returns1AndDeletesOnSuccess() {
            Patient owner = patient(1L);
            Appointment appt = scheduledAppointment(10L, doctor(1L), owner,
                    LocalDateTime.now().plusDays(1));

            when(appointmentRepository.findById(10L)).thenReturn(Optional.of(appt));
            when(tokenService.extractEmail(TOKEN)).thenReturn(PATIENT_EMAIL);
            when(patientRepository.findByEmail(PATIENT_EMAIL)).thenReturn(Optional.of(owner));

            int result = appointmentService.cancelAppointment(10L, TOKEN);

            assertThat(result).isEqualTo(1);
            verify(appointmentRepository).deleteById(10L);
        }
    }

    @Nested
    @DisplayName("updateAppointment()")
    class UpdateAppointment {

        @Test
        @DisplayName("returns -1 when appointment does not exist")
        void returnsMinusOneWhenNotFound() {
            Appointment request = new Appointment();
            request.setId(99L);
            when(appointmentRepository.findById(99L)).thenReturn(Optional.empty());

            assertThat(appointmentService.updateAppointment(request, TOKEN)).isEqualTo(-1);
        }

        @Test
        @DisplayName("returns -2 when token belongs to a different patient")
        void returnsMinusTwoForWrongPatient() {
            Patient owner = patient(1L);
            Patient requester = patient(2L);
            Appointment existing = scheduledAppointment(10L, doctor(1L), owner,
                    LocalDateTime.now().plusDays(1));
            Appointment request = scheduledAppointment(10L, null, null,
                    LocalDateTime.now().plusDays(1).withHour(9).withMinute(0));

            when(appointmentRepository.findById(10L)).thenReturn(Optional.of(existing));
            when(tokenService.extractEmail(TOKEN)).thenReturn(PATIENT_EMAIL);
            when(patientRepository.findByEmail(PATIENT_EMAIL)).thenReturn(Optional.of(requester));

            assertThat(appointmentService.updateAppointment(request, TOKEN)).isEqualTo(-2);
        }

        @Test
        @DisplayName("returns 0 when appointment is already completed (status != 0)")
        void returns0WhenAlreadyCompleted() {
            Patient owner = patient(1L);
            Appointment existing = scheduledAppointment(10L, doctor(1L), owner,
                    LocalDateTime.now().plusDays(1));
            existing.setStatus(1);
            Appointment request = scheduledAppointment(10L, null, null,
                    LocalDateTime.now().plusDays(1).withHour(9).withMinute(0));

            when(appointmentRepository.findById(10L)).thenReturn(Optional.of(existing));
            when(tokenService.extractEmail(TOKEN)).thenReturn(PATIENT_EMAIL);
            when(patientRepository.findByEmail(PATIENT_EMAIL)).thenReturn(Optional.of(owner));

            assertThat(appointmentService.updateAppointment(request, TOKEN)).isEqualTo(0);
        }

        @Test
        @DisplayName("returns 0 when requested time is not in doctor's available slots")
        void returns0WhenTimeUnavailable() {
            Patient owner = patient(1L);
            Doctor doc = doctor(1L);
            // availableTimes = ["09:00", "10:00", "14:00"] — 11:30 is not available
            Appointment existing = scheduledAppointment(10L, doc, owner,
                    LocalDateTime.now().plusDays(1).withHour(9).withMinute(0));
            LocalDateTime unavailableTime = LocalDate.now().plusDays(1)
                    .atTime(LocalTime.of(11, 30));
            Appointment request = scheduledAppointment(10L, null, null, unavailableTime);

            when(appointmentRepository.findById(10L)).thenReturn(Optional.of(existing));
            when(tokenService.extractEmail(TOKEN)).thenReturn(PATIENT_EMAIL);
            when(patientRepository.findByEmail(PATIENT_EMAIL)).thenReturn(Optional.of(owner));

            assertThat(appointmentService.updateAppointment(request, TOKEN)).isEqualTo(0);
        }

        @Test
        @DisplayName("returns 1 and saves when patient matches, status is 0, and time is available")
        void returns1AndSavesOnSuccess() {
            Patient owner = patient(1L);
            Doctor doc = doctor(1L);
            Appointment existing = scheduledAppointment(10L, doc, owner,
                    LocalDateTime.now().plusDays(1).withHour(9).withMinute(0));
            LocalDateTime newTime = LocalDate.now().plusDays(1).atTime(LocalTime.of(10, 0));
            Appointment request = scheduledAppointment(10L, null, null, newTime);

            when(appointmentRepository.findById(10L)).thenReturn(Optional.of(existing));
            when(tokenService.extractEmail(TOKEN)).thenReturn(PATIENT_EMAIL);
            when(patientRepository.findByEmail(PATIENT_EMAIL)).thenReturn(Optional.of(owner));
            when(appointmentRepository.save(existing)).thenReturn(existing);

            int result = appointmentService.updateAppointment(request, TOKEN);

            assertThat(result).isEqualTo(1);
            assertThat(existing.getAppointmentTime()).isEqualTo(newTime);
            verify(appointmentRepository).save(existing);
        }
    }


    @Nested
    @DisplayName("getAppointments()")
    class GetAppointments {

        @BeforeEach
        void stubDoctorLookup() {
            when(tokenService.extractEmail(TOKEN)).thenReturn(DOCTOR_EMAIL);
            when(doctorRepository.findByEmail(DOCTOR_EMAIL)).thenReturn(doctor(1L));
        }

        @Test
        @DisplayName("returns all appointments for a date when patientName is 'null'")
        void returnsAllForDateWhenNoPatientFilter() {
            Doctor doc = doctor(1L);
            Patient pat = patient(1L);
            Appointment appt = scheduledAppointment(1L, doc, pat,
                    LocalDate.now().atTime(LocalTime.of(9, 0)));

            when(appointmentRepository.findByDoctorIdAndAppointmentTimeBetween(
                    eq(1L), any(), any()))
                    .thenReturn(List.of(appt));

            List<AppointmentDTO> result = appointmentService.getAppointments(
                    LocalDate.now().toString(), "null", TOKEN);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getDoctorId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("returns filtered appointments when patientName is provided")
        void returnsFilteredByPatientName() {
            Doctor doc = doctor(1L);
            Patient pat = patient(1L);
            Appointment appt = scheduledAppointment(1L, doc, pat,
                    LocalDate.now().atTime(LocalTime.of(9, 0)));

            when(appointmentRepository
                    .findByDoctorIdAndPatient_NameContainingIgnoreCaseAndAppointmentTimeBetween(
                            eq(1L), eq("Test Patient"), any(), any()))
                    .thenReturn(List.of(appt));

            List<AppointmentDTO> result = appointmentService.getAppointments(
                    LocalDate.now().toString(), "Test Patient", TOKEN);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getPatientName()).isEqualTo("Test Patient");
        }

        @Test
        @DisplayName("returns empty list when no appointments match")
        void returnsEmptyListWhenNoneFound() {
            when(appointmentRepository.findByDoctorIdAndAppointmentTimeBetween(
                    eq(1L), any(), any()))
                    .thenReturn(List.of());

            List<AppointmentDTO> result = appointmentService.getAppointments(
                    LocalDate.now().toString(), "null", TOKEN);

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("changeStatus()")
    class ChangeStatus {

        @Test
        @DisplayName("delegates to repository with the given status and id")
        void delegatesToRepository() {
            appointmentService.changeStatus(1, 42L);
            verify(appointmentRepository).updateStatus(1, 42L);
        }
    }
}
