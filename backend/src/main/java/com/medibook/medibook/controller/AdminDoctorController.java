package com.medibook.medibook.controller;

import com.medibook.medibook.dto.request.AdminUpdateDoctorRequest;
import com.medibook.medibook.dto.request.CreateDoctorRequest;
import com.medibook.medibook.dto.response.DoctorResponse;
import com.medibook.medibook.service.DoctorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/admin/doctors")
@Tag(name = "Admin Doctor Management", description = "Administrative endpoints to create, verify, activate, and manage doctors")
@PreAuthorize("hasRole('ADMIN')")
@SecurityRequirement(name = "BearerAuth")
public class AdminDoctorController {

    private final DoctorService doctorService;

    public AdminDoctorController(DoctorService doctorService) {
        this.doctorService = doctorService;
    }

    @PostMapping
    @Operation(summary = "Create doctor account", description = "Creates a doctor account with DOCTOR role and an unverified Doctor profile.")
    public ResponseEntity<DoctorResponse> createDoctor(@Valid @RequestBody CreateDoctorRequest request) {
        DoctorResponse response = doctorService.createDoctor(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "Get all doctors (Admin view)", description = "Returns all doctors, including inactive and unverified profiles.")
    public ResponseEntity<Page<DoctorResponse>> getAllDoctors(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String direction
    ) {
        Sort sort = direction.equalsIgnoreCase("ASC") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<DoctorResponse> response = doctorService.getAllDoctorsForAdmin(pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{doctorId}")
    @Operation(summary = "Get doctor by ID (Admin view)", description = "Returns complete doctor profile for administrative review.")
    public ResponseEntity<DoctorResponse> getDoctorById(@PathVariable UUID doctorId) {
        DoctorResponse response = doctorService.getDoctorByIdForAdmin(doctorId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{doctorId}")
    @Operation(summary = "Update doctor (Admin view)", description = "Allows administrators to update doctor information.")
    public ResponseEntity<DoctorResponse> updateDoctor(
            @PathVariable UUID doctorId,
            @Valid @RequestBody AdminUpdateDoctorRequest request
    ) {
        DoctorResponse response = doctorService.updateDoctorByAdmin(doctorId, request);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{doctorId}/verify")
    @Operation(summary = "Verify doctor", description = "Marks a doctor profile as verified.")
    public ResponseEntity<DoctorResponse> verifyDoctor(@PathVariable UUID doctorId) {
        DoctorResponse response = doctorService.verifyDoctor(doctorId);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{doctorId}/activate")
    @Operation(summary = "Activate doctor", description = "Sets doctor account status to active.")
    public ResponseEntity<DoctorResponse> activateDoctor(@PathVariable UUID doctorId) {
        DoctorResponse response = doctorService.activateDoctor(doctorId);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{doctorId}/deactivate")
    @Operation(summary = "Deactivate doctor", description = "Deactivates doctor account preventing login and public visibility.")
    public ResponseEntity<DoctorResponse> deactivateDoctor(@PathVariable UUID doctorId) {
        DoctorResponse response = doctorService.deactivateDoctor(doctorId);
        return ResponseEntity.ok(response);
    }
}
