package com.medibook.medibook.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AvailabilityExceptionResponse {

    @Schema(description = "Date exception unique identifier")
    private UUID exceptionId;

    @Schema(description = "Doctor unique identifier")
    private UUID doctorId;

    @Schema(description = "Date when doctor is unavailable", example = "2026-12-25")
    private LocalDate exceptionDate;

    @Schema(description = "Reason for being unavailable", example = "Personal leave")
    private String reason;

    @Schema(description = "Created timestamp")
    private LocalDateTime createdAt;
}
