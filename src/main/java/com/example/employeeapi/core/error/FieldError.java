package com.example.employeeapi.core.error;

/** Une erreur de validation, au même format que l'API Express : {"field": ..., "error": ...}. */
public record FieldError(String field, String error) {
}
