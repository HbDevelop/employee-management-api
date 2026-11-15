package com.example.employeeapi.authentication;

/** L'utilisateur connecté, tel que décrit dans son jeton. */
public record AuthUser(String id, String email, String firstName, String lastName) {
}
