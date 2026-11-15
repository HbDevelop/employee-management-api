package com.example.employeeapi.core.error;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/** Transforme les erreurs de l'application en réponses HTTP claires. */
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ValidationFailedException.class)
    public ResponseEntity<List<FieldError>> handleValidation(ValidationFailedException ex) {
        return ResponseEntity.badRequest().body(ex.getErrors());
    }

    @ExceptionHandler(DuplicateFieldException.class)
    public ResponseEntity<List<Map<String, Object>>> handleDuplicate(DuplicateFieldException ex) {
        String message = "Duplicate constraint violation. The value '" + ex.getValue() + "' is already used!!";
        return ResponseEntity.badRequest()
                .body(List.of(Map.of("field", ex.getField(), "errors", List.of(message))));
    }

    /** Toute erreur imprévue : on la note dans les logs et on répond 500. */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleUnexpected(Exception ex) {
        log.error("Unexpected error", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Unexpected Error");
    }
}
