package com.example.employeeapi.authentication;

/** Utilisateur connecté, reconstruit à partir du contenu du JWT. */
public record AuthUser(String id, String email, String firstName, String lastName) {
}
