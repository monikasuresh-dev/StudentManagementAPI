package com.monika.studentmanagementapi.exception;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.List;

/** The single JSON shape used for every error response. */
@Schema(description = "Error response returned for 400 and 404 cases")
public record ErrorResponse(
        @Schema(example = "2026-10-11T10:15:30Z") Instant timestamp,
        @Schema(example = "400") int status,
        @Schema(example = "Bad Request") String error,
        @Schema(example = "Validation failed") String message,
        @Schema(description = "Field-level details (empty when not applicable)") List<String> details) {
}
