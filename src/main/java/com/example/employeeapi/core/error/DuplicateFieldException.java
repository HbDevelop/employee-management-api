package com.example.employeeapi.core.error;

/** Levée quand une valeur qui doit être unique existe déjà (ex. email déjà utilisé). */
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
