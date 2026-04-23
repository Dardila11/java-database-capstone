package com.project.back_end.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AdminLoginRequest(
        @NotBlank(message = "Username is required")
        String username,
        @NotNull(message = "Password is required")
        String password)  {}
