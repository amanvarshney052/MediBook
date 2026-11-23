package com.medibook.medibook.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateAvailabilityBreakRequest {

    @NotNull(message = "Break start time is required")
    @JsonFormat(pattern = "HH:mm")
    @Schema(description = "Break start time in HH:mm", example = "13:00", type = "string")
    private LocalTime startTime;

    @NotNull(message = "Break end time is required")
    @JsonFormat(pattern = "HH:mm")
    @Schema(description = "Break end time in HH:mm", example = "14:00", type = "string")
    private LocalTime endTime;

    @Size(max = 255, message = "Reason cannot exceed 255 characters")
    @Schema(description = "Reason for break", example = "Lunch break")
    private String reason;
}
