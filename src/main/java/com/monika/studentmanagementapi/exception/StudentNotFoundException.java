package com.monika.studentmanagementapi.exception;

/** Thrown when a student id does not exist. Mapped to HTTP 404 by GlobalExceptionHandler. */
public class StudentNotFoundException extends RuntimeException {

    public StudentNotFoundException(Long id) {
        super("Student not found with id " + id);
    }
}
