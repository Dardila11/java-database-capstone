package com.project.back_end.shared;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public final class AuthDTO {

    private AuthDTO() {}

    public record AdminLoginRequest(
            @NotBlank(message = "Username is required")
            String username,
            @NotBlank(message = "Password is required")
            String password)  {}

    public record LoginRequest(
            @NotBlank(message = "Email is required")
            @Email(message = "Invalid email format")
            String email,
            @NotBlank(message = "Password is required")
            String password)  {}

    public record LoginResponse(String token) { }

}