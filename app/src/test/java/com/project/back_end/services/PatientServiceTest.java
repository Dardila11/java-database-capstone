package com.project.back_end.services;

import com.project.back_end.DTO.AppointmentDTO;
import com.project.back_end.enums.ServiceResult;
import com.project.back_end.models.Appointment;
import com.project.back_end.models.Doctor;
import com.project.back_end.models.Patient;
import com.project.back_end.repo.AppointmentRepository;
import com.project.back_end.repo.PatientRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("PatientServiceTest")
public class PatientServiceTest {

    private static final String PATIENT_EMAIL = "patient@example.com";
    private static final String DOCTOR_EMAIL = "doctor@example.com";

    @Mock private PatientRepository patientRepository;
    @Mock private AppointmentRepository appointmentRepository;
    @Mock private TokenService tokenService;
    @Mock private PasswordEncoder passwordEncoder;


    @InjectMocks
    private PatientService patientService;

    private Patient patient(long id){
        Patient p = new Patient();
        p.setId(id);
        p.setName("Test Patient");
        p.setEmail(PATIENT_EMAIL);
        p.setPassword("testPatient12345");
        return p;
    }

    private Doctor doctor(long id){
        Doctor d = new Doctor();
        d.setId(id);
        d.setName("Dr. Smith");
        d.setEmail(DOCTOR_EMAIL);
        d.setAvailableTimes(List.of("09:00", "10:00"));
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

    // createPatient
    @Nested
    @DisplayName("createPatient()")
    class CreatePatient {

        @Test
        @DisplayName(("returns SUCCESS when save succeeds"))
        void return1OnSucceeds(){
            Patient patient = patient(0L);
            when(patientRepository.save(any(Patient.class))).thenReturn(patient);

            assertThat(patientService.createPatient(patient)).isEqualTo(ServiceResult.SUCCESS);
        }

        @Test
        @DisplayName("returns CONFLICT when repository throws an exception")
        void return0OnRepositoryException(){
            Patient patient = patient(1L);
            when(patientRepository.save(any(Patient.class)))
                    .thenThrow(new RuntimeException("DB error"));
            assertThat(patientService.createPatient(patient)).isEqualTo(ServiceResult.CONFLICT);
        }

    }

    @Nested
    @DisplayName("getPatientAppointments")
    class GetPatientAppointments{
        @Test
        @DisplayName("returns a list of AppointmentDTOs for a given patient ID")
        void returnsAppointmentDTOList() {
            Patient pat = patient(1L);
            Doctor doc = doctor(1L);
            Appointment appt = scheduledAppointment(10L, doc, pat, LocalDateTime.now().plusDays(1));

            when(appointmentRepository.findByPatientId(1L)).thenReturn(List.of(appt));

            List<AppointmentDTO> result = patientService.getPatientAppointments(1L);

            assertThat(result).isNotNull();
            assertThat(result.size()).isEqualTo(1);
            assertThat(result.get(0).getId()).isEqualTo(10L);
            assertThat(result.get(0).getPatientId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("returns an empty list when no appointments are found")
        void returnsEmptyListWhenNoneFound() {
            when(appointmentRepository.findByPatientId(99L)).thenReturn(List.of());

            List<AppointmentDTO> result = patientService.getPatientAppointments(99L);

            assertThat(result).isNotNull();
            assertThat(result.isEmpty()).isTrue();
        }


    }


}
