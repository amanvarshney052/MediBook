package com.medibook.medibook.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AvailableSlotResponse {

    @JsonFormat(pattern = "HH:mm")
    @Schema(description = "Slot start time", example = "09:00", type = "string")
    private LocalTime startTime;

    @JsonFormat(pattern = "HH:mm")
    @Schema(description = "Slot end time", example = "09:30", type = "string")
    private LocalTime endTime;

    @Schema(description = "Slot availability status", example = "AVAILABLE")
    private String status;
}
