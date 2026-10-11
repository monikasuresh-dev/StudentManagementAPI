package com.monika.studentmanagementapi.exception;

import jakarta.validation.ConstraintViolationException;
import java.time.Instant;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/**
 * Catches exceptions from all controllers and turns them into clean JSON errors.
 * No stack traces or internal class names are sent to the client.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** Missing student -> 404. */
    @ExceptionHandler(StudentNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(StudentNotFoundException ex) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), List.of());
    }

    /** @Valid failed on the request body -> 400 with one line per bad field. */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        List<String> details = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .sorted()
                .toList();
        return build(HttpStatus.BAD_REQUEST, "Validation failed", details);
    }

    /** Path variable failed a constraint such as @Positive (for example id = 0) -> 400. */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException ex) {
        List<String> details = ex.getConstraintViolations().stream()
                .map(violation -> violation.getMessage())
                .sorted()
                .toList();
        return build(HttpStatus.BAD_REQUEST, "Invalid request parameter", details);
    }

    /** Id is not a number, e.g. /api/students/abc -> 400. */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        return build(HttpStatus.BAD_REQUEST,
                "Invalid value for '" + ex.getName() + "': it must be a valid number", List.of());
    }

    /** Body is missing or is not valid JSON -> 400. */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleUnreadableBody(HttpMessageNotReadableException ex) {
        return build(HttpStatus.BAD_REQUEST,
                "Request body is missing or is not valid JSON", List.of());
    }

    private ResponseEntity<ErrorResponse> build(HttpStatus status, String message, List<String> details) {
        ErrorResponse body = new ErrorResponse(
                Instant.now(), status.value(), status.getReasonPhrase(), message, details);
        return ResponseEntity.status(status).body(body);
    }
}
