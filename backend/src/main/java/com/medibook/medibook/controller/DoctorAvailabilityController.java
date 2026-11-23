package com.medibook.medibook.controller;

import com.medibook.medibook.dto.request.*;
import com.medibook.medibook.dto.response.*;
import com.medibook.medibook.service.DoctorAvailabilityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/doctors")
@Tag(name = "Doctor Availability & Scheduling", description = "Endpoints for doctor weekly schedule, breaks, date exceptions, and available slot generation")
@SecurityRequirement(name = "BearerAuth")
public class DoctorAvailabilityController {

    private final DoctorAvailabilityService availabilityService;

    public DoctorAvailabilityController(DoctorAvailabilityService availabilityService) {
        this.availabilityService = availabilityService;
    }

    // ==========================================
    // 1. AVAILABILITY MANAGEMENT (DOCTOR SELF)
    // ==========================================

    @PostMapping("/me/availability")
    @PreAuthorize("hasRole('DOCTOR')")
    @Operation(summary = "Create availability schedule", description = "Configures weekly working hours and slot duration for authenticated doctor.")
    public ResponseEntity<AvailabilityResponse> createAvailability(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody CreateAvailabilityRequest request
    ) {
        AvailabilityResponse response = availabilityService.createAvailability(userDetails.getUsername(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/me/availability")
    @PreAuthorize("hasRole('DOCTOR')")
    @Operation(summary = "Get own weekly availability", description = "Retrieves all availability configurations and breaks for authenticated doctor.")
    public ResponseEntity<List<AvailabilityResponse>> getMyAvailability(@AuthenticationPrincipal UserDetails userDetails) {
        List<AvailabilityResponse> response = availabilityService.getMyAvailabilities(userDetails.getUsername());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me/availability/{availabilityId}")
    @PreAuthorize("hasRole('DOCTOR')")
    @Operation(summary = "Get availability by ID", description = "Retrieves a specific availability configuration for authenticated doctor.")
    public ResponseEntity<AvailabilityResponse> getMyAvailabilityById(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID availabilityId
    ) {
        AvailabilityResponse response = availabilityService.getMyAvailabilityById(userDetails.getUsername(), availabilityId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/me/availability/{availabilityId}")
    @PreAuthorize("hasRole('DOCTOR')")
    @Operation(summary = "Update availability schedule", description = "Updates working hours, slot duration, and active status for an existing schedule.")
    public ResponseEntity<AvailabilityResponse> updateAvailability(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID availabilityId,
            @Valid @RequestBody UpdateAvailabilityRequest request
    ) {
        AvailabilityResponse response = availabilityService.updateAvailability(userDetails.getUsername(), availabilityId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/me/availability/{availabilityId}")
    @PreAuthorize("hasRole('DOCTOR')")
    @Operation(summary = "Delete availability schedule", description = "Deletes an availability schedule and its associated breaks.")
    public ResponseEntity<Void> deleteAvailability(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID availabilityId
    ) {
        availabilityService.deleteAvailability(userDetails.getUsername(), availabilityId);
        return ResponseEntity.noContent().build();
    }

    // ==========================================
    // 2. BREAK MANAGEMENT (DOCTOR SELF)
    // ==========================================

    @PostMapping("/me/availability/{availabilityId}/breaks")
    @PreAuthorize("hasRole('DOCTOR')")
    @Operation(summary = "Add a break to schedule", description = "Adds a break period within working hours for the specified availability.")
    public ResponseEntity<AvailabilityBreakResponse> addBreak(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID availabilityId,
            @Valid @RequestBody CreateAvailabilityBreakRequest request
    ) {
        AvailabilityBreakResponse response = availabilityService.addBreak(userDetails.getUsername(), availabilityId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/me/availability/{availabilityId}/breaks")
    @PreAuthorize("hasRole('DOCTOR')")
    @Operation(summary = "Get breaks for schedule", description = "Retrieves all breaks defined for the specified availability.")
    public ResponseEntity<List<AvailabilityBreakResponse>> getBreaks(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID availabilityId
    ) {
        List<AvailabilityBreakResponse> response = availabilityService.getBreaks(userDetails.getUsername(), availabilityId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/me/availability/{availabilityId}/breaks/{breakId}")
    @PreAuthorize("hasRole('DOCTOR')")
    @Operation(summary = "Update a break", description = "Updates time range or reason for an existing break.")
    public ResponseEntity<AvailabilityBreakResponse> updateBreak(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID availabilityId,
            @PathVariable UUID breakId,
            @Valid @RequestBody UpdateAvailabilityBreakRequest request
    ) {
        AvailabilityBreakResponse response = availabilityService.updateBreak(userDetails.getUsername(), availabilityId, breakId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/me/availability/{availabilityId}/breaks/{breakId}")
    @PreAuthorize("hasRole('DOCTOR')")
    @Operation(summary = "Delete a break", description = "Removes a break period from the specified availability schedule.")
    public ResponseEntity<Void> deleteBreak(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID availabilityId,
            @PathVariable UUID breakId
    ) {
        availabilityService.deleteBreak(userDetails.getUsername(), availabilityId, breakId);
        return ResponseEntity.noContent().build();
    }

    // ==========================================
    // 3. DATE EXCEPTION MANAGEMENT (DOCTOR SELF)
    // ==========================================

    @PostMapping("/me/availability/exceptions")
    @PreAuthorize("hasRole('DOCTOR')")
    @Operation(summary = "Add unavailable date", description = "Blocks a specific date when the doctor is not available for appointments.")
    public ResponseEntity<AvailabilityExceptionResponse> addException(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody CreateAvailabilityExceptionRequest request
    ) {
        AvailabilityExceptionResponse response = availabilityService.addException(userDetails.getUsername(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/me/availability/exceptions")
    @PreAuthorize("hasRole('DOCTOR')")
    @Operation(summary = "Get unavailable dates", description = "Lists all dates blocked by the authenticated doctor.")
    public ResponseEntity<List<AvailabilityExceptionResponse>> getMyExceptions(@AuthenticationPrincipal UserDetails userDetails) {
        List<AvailabilityExceptionResponse> response = availabilityService.getMyExceptions(userDetails.getUsername());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/me/availability/exceptions/{exceptionId}")
    @PreAuthorize("hasRole('DOCTOR')")
    @Operation(summary = "Delete unavailable date", description = "Unblocks a previously blocked date.")
    public ResponseEntity<Void> deleteException(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID exceptionId
    ) {
        availabilityService.deleteException(userDetails.getUsername(), exceptionId);
        return ResponseEntity.noContent().build();
    }

    // ==========================================
    // 4. PUBLIC & PATIENT AVAILABILITY VIEWS
    // ==========================================

    @GetMapping("/{doctorId}/availability")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get doctor weekly availability", description = "Returns active weekly schedule and breaks for an active, verified doctor.")
    public ResponseEntity<List<AvailabilityResponse>> getDoctorAvailability(@PathVariable UUID doctorId) {
        List<AvailabilityResponse> response = availabilityService.getPublicDoctorAvailability(doctorId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{doctorId}/available-slots")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get available appointment slots", description = "Generates available bookable slots for a doctor on a specific date, excluding breaks and exceptions.")
    public ResponseEntity<List<AvailableSlotResponse>> getAvailableSlots(
            @PathVariable UUID doctorId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        List<AvailableSlotResponse> response = availabilityService.getAvailableSlots(doctorId, date);
        return ResponseEntity.ok(response);
    }
}
