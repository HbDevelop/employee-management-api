package com.example.employeeapi.users;

import com.fasterxml.jackson.annotation.JsonProperty;

/** Un utilisateur tel qu'on le renvoie au client : sans son mot de passe. */
public record PublicUser(String email, String firstName, String lastName, @JsonProperty("_id") String id) {

    public static PublicUser from(User user) {
        return new PublicUser(user.getEmail(), user.getFirstName(), user.getLastName(), user.getId());
    }
}
