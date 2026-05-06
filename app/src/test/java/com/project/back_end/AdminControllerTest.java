package com.project.back_end;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.back_end.DTO.AuthDTO;
import com.project.back_end.controllers.AdminController;
import com.project.back_end.exceptions.InvalidCredentialsException;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminController.class)
@TestPropertySource(properties = "api.path=/")
@DisplayName("AdminController — POST /admin/login")
class AdminControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @MockitoBean
    ValidationService validationService;

    @Nested
    @DisplayName("valid credentials")
    class ValidCredentials {

        @Test
        @DisplayName("returns 200 with token in body")
        void returns200AndToken() throws Exception {
            AuthDTO.AdminLoginRequest body = new AuthDTO.AdminLoginRequest("admin", "secret");
            when(validationService.validateAdminLogin("admin", "secret"))
                    .thenReturn("mocked.jwt.token");

            mockMvc.perform(post("/admin/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(body)))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.token").value("mocked.jwt.token"));
        }
    }

    @Nested
    @DisplayName("bad credentials")
    class BadCredentials {

        @Test
        @DisplayName("wrong password returns 401 with error message")
        void wrongPassword_returns401() throws Exception {
            AuthDTO.AdminLoginRequest body = new AuthDTO.AdminLoginRequest("admin", "wrong");
            when(validationService.validateAdminLogin("admin", "wrong"))
                    .thenThrow(new InvalidCredentialsException("Invalid username or password"));

            mockMvc.perform(post("/admin/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(body)))
                    .andExpect(status().isUnauthorized())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.error").value("Invalid username or password"));
        }

        @Test
        @DisplayName("unknown username returns 401 with error message")
        void unknownUsername_returns401() throws Exception {
            AuthDTO.AdminLoginRequest body = new AuthDTO.AdminLoginRequest("nobody", "pass");
            when(validationService.validateAdminLogin("nobody", "pass"))
                    .thenThrow(new InvalidCredentialsException("Invalid username or password"));

            mockMvc.perform(post("/admin/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(body)))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.error").value("Invalid username or password"));
        }
    }
}
