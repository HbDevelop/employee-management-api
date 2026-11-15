package com.example.employeeapi.core.validation;

import java.lang.reflect.RecordComponent;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.example.employeeapi.core.error.FieldError;
import com.example.employeeapi.core.error.ValidationFailedException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import jakarta.validation.constraints.NotBlank;
import org.springframework.stereotype.Component;

/** Vérifie les données envoyées par le client et signale une erreur par champ incorrect. */
@Component
public class RequestValidator {

    private final Validator validator;

    public RequestValidator(Validator validator) {
        this.validator = validator;
    }

    public <T extends Record> void validate(T body) {
        Map<String, ConstraintViolation<T>> violationByField = new HashMap<>();
        for (ConstraintViolation<T> violation : validator.validate(body)) {
            violationByField.merge(violation.getPropertyPath().toString(), violation, RequestValidator::mostImportant);
        }
        if (violationByField.isEmpty()) {
            return;
        }
        List<FieldError> errors = Arrays.stream(body.getClass().getRecordComponents())
                .map(RecordComponent::getName)
                .filter(violationByField::containsKey)
                .map(field -> new FieldError(field, violationByField.get(field).getMessage()))
                .toList();
        throw new ValidationFailedException(errors);
    }

    /** Si un champ a plusieurs erreurs, on garde "requis" plutôt que "invalide". */
    private static <T> ConstraintViolation<T> mostImportant(ConstraintViolation<T> a, ConstraintViolation<T> b) {
        if (isRequiredViolation(a)) {
            return a;
        }
        if (isRequiredViolation(b)) {
            return b;
        }
        return a.getMessage().compareTo(b.getMessage()) <= 0 ? a : b;
    }

    private static boolean isRequiredViolation(ConstraintViolation<?> violation) {
        return violation.getConstraintDescriptor().getAnnotation() instanceof NotBlank;
    }
}
