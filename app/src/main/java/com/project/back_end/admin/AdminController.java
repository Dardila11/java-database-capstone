
package com.project.back_end.admin;

import com.project.back_end.shared.AuthDTO;
import com.project.back_end.shared.ValidationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("${api.path}admin")
@Tag(name = "Admin Controller for Authentication")
public class AdminController {
    private final ValidationService validationService;

    public AdminController(ValidationService validationService) {
        this.validationService = validationService;
    }

    @PostMapping("/login")
    @Operation(
            summary = "Login Admin User",
            description = "Authenticate an admin user and return a authentication token",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Authentication request with username and password",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AuthDTO.AdminLoginRequest.class)
                    )
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Successful authentication",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(example = "{\"token\": \"some.jwt.token\"}")
                            )
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Invalid username or password",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(example = "{\"error\": \"Invalid credentials\"}")
                            )
                    )
            }

    )
    public ResponseEntity<Map<String, String>> adminLogin(@Valid @RequestBody AuthDTO.AdminLoginRequest adminLoginRequest) {
        String token = validationService.validateAdminLogin(adminLoginRequest.username(), adminLoginRequest.password());
        return ResponseEntity.ok(Map.of("token", token));
    }
}

