package com.example.employeeapi.core.error;

import java.util.List;

/** Levée quand les données envoyées par le client sont incorrectes. */
public class ValidationFailedException extends RuntimeException {

    private final transient List<FieldError> errors;

    public ValidationFailedException(List<FieldError> errors) {
        super("Validation failed");
        this.errors = errors;
    }

    public List<FieldError> getErrors() {
        return errors;
    }
}
