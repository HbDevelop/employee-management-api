package com.example.employeeapi.core.error;

/** Une erreur sur un champ envoyé par le client. */
public record FieldError(String field, String error) {
}
