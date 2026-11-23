package com.medibook.medibook.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AvailabilityBreakResponse {

    @Schema(description = "Break unique identifier")
    private UUID breakId;

    @JsonFormat(pattern = "HH:mm")
    @Schema(description = "Break start time", example = "13:00", type = "string")
    private LocalTime startTime;

    @JsonFormat(pattern = "HH:mm")
    @Schema(description = "Break end time", example = "14:00", type = "string")
    private LocalTime endTime;

    @Schema(description = "Break reason", example = "Lunch break")
    private String reason;

    @Schema(description = "Break creation timestamp")
    private LocalDateTime createdAt;
}
