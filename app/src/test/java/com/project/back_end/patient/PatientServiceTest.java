package com.project.back_end.patient;

import com.project.back_end.enums.ServiceResult;
import com.project.back_end.patient.internal.PatientRepository;
import com.project.back_end.patient.internal.PatientService;
import com.project.back_end.shared.TokenService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("PatientServiceTest")
public class PatientServiceTest {

    @Mock private PatientRepository patientRepository;
    @Mock private TokenService tokenService;
    @Mock private PasswordEncoder passwordEncoder;

    @InjectMocks
    private PatientService patientService;

    private Patient patient(long id) {
        Patient p = new Patient();
        p.setId(id);
        p.setName("Test Patient");
        p.setEmail("patient@example.com");
        p.setPassword("testPatient12345");
        return p;
    }

    @Nested
    @DisplayName("createPatient()")
    class CreatePatient {

        @Test
        @DisplayName("returns SUCCESS when save succeeds")
        void return1OnSucceeds() {
            Patient patient = patient(0L);
            when(patientRepository.save(any(Patient.class))).thenReturn(patient);

            assertThat(patientService.createPatient(patient)).isEqualTo(ServiceResult.SUCCESS);
        }

        @Test
        @DisplayName("returns CONFLICT when repository throws an exception")
        void return0OnRepositoryException() {
            Patient patient = patient(1L);
            when(patientRepository.save(any(Patient.class)))
                    .thenThrow(new RuntimeException("DB error"));
            assertThat(patientService.createPatient(patient)).isEqualTo(ServiceResult.CONFLICT);
        }
    }
}
