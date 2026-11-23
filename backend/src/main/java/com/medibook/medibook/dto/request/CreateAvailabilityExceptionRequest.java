package com.medibook.medibook.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateAvailabilityExceptionRequest {

    @NotNull(message = "Exception date is required")
    @Schema(description = "Date when doctor is unavailable", example = "2026-12-25")
    private LocalDate exceptionDate;

    @Size(max = 255, message = "Reason cannot exceed 255 characters")
    @Schema(description = "Reason for date exception", example = "Personal leave")
    private String reason;
}
