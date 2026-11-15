package com.example.employeeapi.core.error;

/** Violation d'une contrainte d'unicité (ex. email déjà utilisé). */
public class DuplicateFieldException extends RuntimeException {

    private final String field;
    private final String value;

    public DuplicateFieldException(String field, String value, Throwable cause) {
        super("Duplicate value for field " + field, cause);
        this.field = field;
        this.value = value;
    }

    public String getField() {
        return field;
    }

    public String getValue() {
        return value;
    }
}
