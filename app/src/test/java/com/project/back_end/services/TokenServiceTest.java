package com.project.back_end.services;

import com.project.back_end.admin.Admin;
import com.project.back_end.admin.AdminRepository;
import com.project.back_end.doctor.Doctor;
import com.project.back_end.doctor.DoctorRepository;
import com.project.back_end.patient.Patient;
import com.project.back_end.patient.PatientRepository;
import com.project.back_end.shared.TokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("TokenService")
class TokenServiceTest {

    // 32-byte secret satisfies HMAC-SHA256 minimum key length requirement
    private static final String TEST_SECRET = "test-secret-key-for-unit-tests-32b";
    private static final String EMAIL = "user@example.com";

    @Mock
    private AdminRepository adminRepository;

    @Mock
    private DoctorRepository doctorRepository;

    @Mock
    private PatientRepository patientRepository;

    @InjectMocks
    private TokenService tokenService;

    @BeforeEach
    void injectSecret() {
        ReflectionTestUtils.setField(tokenService, "jwtSecret", TEST_SECRET);
    }

    // -----------------------------------------------------------------------
    // generateToken
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("generateToken()")
    class GenerateToken {

        @Test
        @DisplayName("returns a non-null, non-blank JWT string")
        void returnsNonBlankToken() {
            String token = tokenService.generateToken(EMAIL);
            assertThat(token).isNotBlank();
        }

        @Test
        @DisplayName("token has three dot-separated JWT segments")
        void hasThreeSegments() {
            String token = tokenService.generateToken(EMAIL);
            assertThat(token.split("\\.")).hasSize(3);
        }

        @Test
        @DisplayName("different emails produce different tokens")
        void differentEmailsProduceDifferentTokens() {
            String t1 = tokenService.generateToken("a@example.com");
            String t2 = tokenService.generateToken("b@example.com");
            assertThat(t1).isNotEqualTo(t2);
        }
    }

    // -----------------------------------------------------------------------
    // extractEmail
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("extractEmail()")
    class ExtractEmail {

        @Test
        @DisplayName("round-trips the subject email exactly")
        void roundTripsEmail() {
            String token = tokenService.generateToken(EMAIL);
            assertThat(tokenService.extractEmail(token)).isEqualTo(EMAIL);
        }

        @Test
        @DisplayName("round-trips an email with special characters")
        void roundTripsEmailWithSpecialCharacters() {
            String unusual = "user+tag@sub.domain.org";
            String token = tokenService.generateToken(unusual);
            assertThat(tokenService.extractEmail(token)).isEqualTo(unusual);
        }
    }

    // -----------------------------------------------------------------------
    // validateToken
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("validateToken()")
    class ValidateToken {

        private String validToken;

        @BeforeEach
        void buildToken() {
            validToken = tokenService.generateToken(EMAIL);
        }

        @Test
        @DisplayName("returns true for 'admin' role when admin exists")
        void trueForAdminRoleWhenAdminExists() {
            when(adminRepository.findByUsername(EMAIL)).thenReturn(new Admin());
            assertThat(tokenService.validateToken(validToken, "admin")).isTrue();
        }

        @Test
        @DisplayName("returns false for 'admin' role when admin not found")
        void falseForAdminRoleWhenAdminNotFound() {
            when(adminRepository.findByUsername(EMAIL)).thenReturn(null);
            assertThat(tokenService.validateToken(validToken, "admin")).isFalse();
        }

        @Test
        @DisplayName("returns true for 'doctor' role when doctor exists")
        void trueForDoctorRoleWhenDoctorExists() {
            when(doctorRepository.findByEmail(EMAIL)).thenReturn(new Doctor());
            assertThat(tokenService.validateToken(validToken, "doctor")).isTrue();
        }

        @Test
        @DisplayName("returns false for 'doctor' role when doctor not found")
        void falseForDoctorRoleWhenDoctorNotFound() {
            when(doctorRepository.findByEmail(EMAIL)).thenReturn(null);
            assertThat(tokenService.validateToken(validToken, "doctor")).isFalse();
        }

        @Test
        @DisplayName("returns true for 'patient' role when patient exists")
        void trueForPatientRoleWhenPatientExists() {
            when(patientRepository.findByEmail(EMAIL)).thenReturn(Optional.of(new Patient()));
            assertThat(tokenService.validateToken(validToken, "patient")).isTrue();
        }

        @Test
        @DisplayName("returns false for 'patient' role when patient not found")
        void falseForPatientRoleWhenPatientNotFound() {
            when(patientRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());
            assertThat(tokenService.validateToken(validToken, "patient")).isFalse();
        }

        @Test
        @DisplayName("returns false for an unknown role")
        void falseForUnknownRole() {
            assertThat(tokenService.validateToken(validToken, "nurse")).isFalse();
        }

        @Test
        @DisplayName("returns false for a structurally tampered token")
        void falseForTamperedToken() {
            String tampered = validToken + "tampered";
            assertThat(tokenService.validateToken(tampered, "admin")).isFalse();
        }

        @Test
        @DisplayName("returns false for a token signed with a different secret")
        void falseForTokenSignedWithWrongSecret() {
            ReflectionTestUtils.setField(tokenService, "jwtSecret", "wrong-secret-key-for-unit-tests-32b");
            String foreignToken = tokenService.generateToken(EMAIL);

            // restore original secret so verification uses the correct key
            ReflectionTestUtils.setField(tokenService, "jwtSecret", TEST_SECRET);

            assertThat(tokenService.validateToken(foreignToken, "admin")).isFalse();
        }

        @Test
        @DisplayName("returns false for a blank / empty token string")
        void falseForBlankToken() {
            assertThat(tokenService.validateToken("", "admin")).isFalse();
        }
    }
}
