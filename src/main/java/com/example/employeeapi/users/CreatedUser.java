package com.example.employeeapi.users;

import com.fasterxml.jackson.annotation.JsonProperty;

/** Réponse de /auth/register : l'utilisateur créé, sans son mot de passe. */
public record CreatedUser(String email, String firstName, String lastName, @JsonProperty("_id") String id) {
}
