package com.medibook.medibook.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AvailabilityResponse {

    @Schema(description = "Availability unique identifier")
    private UUID availabilityId;

    @Schema(description = "Doctor unique identifier")
    private UUID doctorId;

    @Schema(description = "Day of the week", example = "MONDAY")
    private DayOfWeek dayOfWeek;

    @JsonFormat(pattern = "HH:mm")
    @Schema(description = "Working start time", example = "09:00", type = "string")
    private LocalTime startTime;

    @JsonFormat(pattern = "HH:mm")
    @Schema(description = "Working end time", example = "17:00", type = "string")
    private LocalTime endTime;

    @Schema(description = "Slot duration in minutes", example = "30")
    private Integer slotDurationMinutes;

    @Schema(description = "Whether availability is active", example = "true")
    private Boolean isAvailable;

    @Schema(description = "Associated breaks within this schedule")
    private List<AvailabilityBreakResponse> breaks;

    @Schema(description = "Created timestamp")
    private LocalDateTime createdAt;

    @Schema(description = "Last updated timestamp")
    private LocalDateTime updatedAt;
}
