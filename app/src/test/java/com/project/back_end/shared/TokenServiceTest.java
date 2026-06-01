package com.project.back_end.shared;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("TokenService")
class TokenServiceTest {

    private static final String TEST_SECRET = "test-secret-key-for-unit-tests-32b";
    private static final String EMAIL = "user@example.com";

    @Mock private RoleValidator adminValidator;
    @Mock private RoleValidator doctorValidator;
    @Mock private RoleValidator patientValidator;

    private TokenService tokenService;

    @BeforeEach
    void setUp() {
        when(adminValidator.role()).thenReturn("admin");
        when(doctorValidator.role()).thenReturn("doctor");
        when(patientValidator.role()).thenReturn("patient");
        tokenService = new TokenService(List.of(adminValidator, doctorValidator, patientValidator));
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
            assertThat(tokenService.generateToken(EMAIL)).isNotBlank();
        }

        @Test
        @DisplayName("token has three dot-separated JWT segments")
        void hasThreeSegments() {
            assertThat(tokenService.generateToken(EMAIL).split("\\.")).hasSize(3);
        }

        @Test
        @DisplayName("different subjects produce different tokens")
        void differentSubjectsProduceDifferentTokens() {
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
        @DisplayName("round-trips the subject exactly")
        void roundTripsEmail() {
            String token = tokenService.generateToken(EMAIL);
            assertThat(tokenService.extractEmail(token)).isEqualTo(EMAIL);
        }

        @Test
        @DisplayName("round-trips a subject with special characters")
        void roundTripsEmailWithSpecialCharacters() {
            String unusual = "user+tag@sub.domain.org";
            assertThat(tokenService.extractEmail(tokenService.generateToken(unusual))).isEqualTo(unusual);
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
        @DisplayName("returns true for 'admin' role when validator confirms subject")
        void trueForAdminRoleWhenAdminExists() {
            when(adminValidator.isValidSubject(EMAIL)).thenReturn(true);
            assertThat(tokenService.validateToken(validToken, "admin")).isTrue();
        }

        @Test
        @DisplayName("returns false for 'admin' role when validator rejects subject")
        void falseForAdminRoleWhenAdminNotFound() {
            when(adminValidator.isValidSubject(EMAIL)).thenReturn(false);
            assertThat(tokenService.validateToken(validToken, "admin")).isFalse();
        }

        @Test
        @DisplayName("returns true for 'doctor' role when validator confirms subject")
        void trueForDoctorRoleWhenDoctorExists() {
            when(doctorValidator.isValidSubject(EMAIL)).thenReturn(true);
            assertThat(tokenService.validateToken(validToken, "doctor")).isTrue();
        }

        @Test
        @DisplayName("returns false for 'doctor' role when validator rejects subject")
        void falseForDoctorRoleWhenDoctorNotFound() {
            when(doctorValidator.isValidSubject(EMAIL)).thenReturn(false);
            assertThat(tokenService.validateToken(validToken, "doctor")).isFalse();
        }

        @Test
        @DisplayName("returns true for 'patient' role when validator confirms subject")
        void trueForPatientRoleWhenPatientExists() {
            when(patientValidator.isValidSubject(EMAIL)).thenReturn(true);
            assertThat(tokenService.validateToken(validToken, "patient")).isTrue();
        }

        @Test
        @DisplayName("returns false for 'patient' role when validator rejects subject")
        void falseForPatientRoleWhenPatientNotFound() {
            when(patientValidator.isValidSubject(EMAIL)).thenReturn(false);
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
            assertThat(tokenService.validateToken(validToken + "tampered", "admin")).isFalse();
        }

        @Test
        @DisplayName("returns false for a token signed with a different secret")
        void falseForTokenSignedWithWrongSecret() {
            ReflectionTestUtils.setField(tokenService, "jwtSecret", "wrong-secret-key-for-unit-tests-32b");
            String foreignToken = tokenService.generateToken(EMAIL);
            ReflectionTestUtils.setField(tokenService, "jwtSecret", TEST_SECRET);
            assertThat(tokenService.validateToken(foreignToken, "admin")).isFalse();
        }

        @Test
        @DisplayName("returns false for a blank token string")
        void falseForBlankToken() {
            assertThat(tokenService.validateToken("", "admin")).isFalse();
        }
    }
}
