package com.example.employeeapi.authentication;

import com.example.employeeapi.core.validation.Patterns;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/** Données attendues pour créer un compte. */
public record RegisterRequest(
        @NotBlank(message = "The user email is required")
        @Pattern(regexp = Patterns.EMAIL, message = "Invalid email address")
        String email,

        @NotBlank(message = "The user password is required")
        @Pattern(regexp = Patterns.PASSWORD,
                message = "The password must contains lowercase uppercase digit and special character and it is 8 characters minimum")
        String password,

        @NotBlank(message = "The user firstName is required")
        String firstName,

        @NotBlank(message = "The user lastName is required")
        String lastName) {

    public static RegisterRequest empty() {
        return new RegisterRequest(null, null, null, null);
    }
}
