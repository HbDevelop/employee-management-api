package com.example.employeeapi.authentication;

import com.example.employeeapi.core.validation.Patterns;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record LoginRequest(
        @NotBlank(message = "The user email is required")
        @Pattern(regexp = Patterns.EMAIL, message = "Invalid email address")
        String email,

        @NotBlank(message = "The user password is required")
        String password) {

    public static LoginRequest empty() {
        return new LoginRequest(null, null);
    }
}
