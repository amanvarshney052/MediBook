package com.medibook.medibook.controller;

import com.medibook.medibook.dto.response.AvailabilityResponse;
import com.medibook.medibook.service.DoctorAvailabilityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/doctors")
@Tag(name = "Admin Doctor Availability", description = "Administrative endpoints to inspect doctor availability schedules")
@PreAuthorize("hasRole('ADMIN')")
@SecurityRequirement(name = "BearerAuth")
public class AdminAvailabilityController {

    private final DoctorAvailabilityService availabilityService;

    public AdminAvailabilityController(DoctorAvailabilityService availabilityService) {
        this.availabilityService = availabilityService;
    }

    @GetMapping("/{doctorId}/availability")
    @Operation(summary = "Get doctor availability (Admin view)", description = "Returns complete availability and breaks for any doctor.")
    public ResponseEntity<List<AvailabilityResponse>> getDoctorAvailability(@PathVariable UUID doctorId) {
        List<AvailabilityResponse> response = availabilityService.getDoctorAvailabilityForAdmin(doctorId);
        return ResponseEntity.ok(response);
    }
}
