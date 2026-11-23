package com.medibook.medibook.mapper;

import com.medibook.medibook.dto.response.AvailabilityBreakResponse;
import com.medibook.medibook.dto.response.AvailabilityExceptionResponse;
import com.medibook.medibook.dto.response.AvailabilityResponse;
import com.medibook.medibook.entity.AvailabilityBreak;
import com.medibook.medibook.entity.DoctorAvailability;
import com.medibook.medibook.entity.DoctorAvailabilityException;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class AvailabilityMapper {

    public AvailabilityResponse toAvailabilityResponse(DoctorAvailability availability) {
        if (availability == null) {
            return null;
        }

        List<AvailabilityBreakResponse> breakResponses = Collections.emptyList();
        if (availability.getBreaks() != null && !availability.getBreaks().isEmpty()) {
            breakResponses = availability.getBreaks().stream()
                    .map(this::toAvailabilityBreakResponse)
                    .collect(Collectors.toList());
        }

        return AvailabilityResponse.builder()
                .availabilityId(availability.getId())
                .doctorId(availability.getDoctor() != null ? availability.getDoctor().getId() : null)
                .dayOfWeek(availability.getDayOfWeek())
                .startTime(availability.getStartTime())
                .endTime(availability.getEndTime())
                .slotDurationMinutes(availability.getSlotDurationMinutes())
                .isAvailable(availability.getIsAvailable())
                .breaks(breakResponses)
                .createdAt(availability.getCreatedAt())
                .updatedAt(availability.getUpdatedAt())
                .build();
    }

    public AvailabilityBreakResponse toAvailabilityBreakResponse(AvailabilityBreak b) {
        if (b == null) {
            return null;
        }

        return AvailabilityBreakResponse.builder()
                .breakId(b.getId())
                .startTime(b.getStartTime())
                .endTime(b.getEndTime())
                .reason(b.getReason())
                .createdAt(b.getCreatedAt())
                .build();
    }

    public AvailabilityExceptionResponse toAvailabilityExceptionResponse(DoctorAvailabilityException exception) {
        if (exception == null) {
            return null;
        }

        return AvailabilityExceptionResponse.builder()
                .exceptionId(exception.getId())
                .doctorId(exception.getDoctor() != null ? exception.getDoctor().getId() : null)
                .exceptionDate(exception.getExceptionDate())
                .reason(exception.getReason())
                .createdAt(exception.getCreatedAt())
                .build();
    }
}
