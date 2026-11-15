package com.example.employeeapi.users;

import com.fasterxml.jackson.annotation.JsonProperty;

/** L'utilisateur renvoyé après une inscription, sans son mot de passe. */
public record CreatedUser(String email, String firstName, String lastName, @JsonProperty("_id") String id) {
}
