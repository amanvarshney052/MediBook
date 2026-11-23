package com.medibook.medibook.controller;

import com.medibook.medibook.dto.request.UpdateDoctorProfileRequest;
import com.medibook.medibook.dto.response.DoctorResponse;
import com.medibook.medibook.dto.response.DoctorSummaryResponse;
import com.medibook.medibook.service.DoctorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.UUID;

@RestController
@RequestMapping("/api/doctors")
@Tag(name = "Doctors", description = "Endpoints for doctor profile management, browsing, and searching")
@SecurityRequirement(name = "BearerAuth")
public class DoctorController {

    private final DoctorService doctorService;

    public DoctorController(DoctorService doctorService) {
        this.doctorService = doctorService;
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('DOCTOR')")
    @Operation(summary = "Get authenticated doctor's profile", description = "Allows a logged-in doctor to view their complete profile.")
    public ResponseEntity<DoctorResponse> getMyProfile(@AuthenticationPrincipal UserDetails userDetails) {
        DoctorResponse response = doctorService.getMyProfile(userDetails.getUsername());
        return ResponseEntity.ok(response);
    }

    @PutMapping("/me")
    @PreAuthorize("hasRole('DOCTOR')")
    @Operation(summary = "Update authenticated doctor's profile", description = "Allows a doctor to update permitted professional information.")
    public ResponseEntity<DoctorResponse> updateMyProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody UpdateDoctorProfileRequest request
    ) {
        DoctorResponse response = doctorService.updateMyProfile(userDetails.getUsername(), request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{doctorId}")
    @Operation(summary = "Get doctor by ID", description = "Returns public profile of an active and verified doctor.")
    public ResponseEntity<DoctorResponse> getDoctorById(@PathVariable UUID doctorId) {
        DoctorResponse response = doctorService.getDoctorById(doctorId);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "Get all active & verified doctors", description = "Returns paginated and sorted list of active and verified doctors.")
    public ResponseEntity<Page<DoctorSummaryResponse>> getAllDoctors(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "experienceYears") String sortBy,
            @RequestParam(defaultValue = "DESC") String direction
    ) {
        Sort sort = direction.equalsIgnoreCase("ASC") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<DoctorSummaryResponse> response = doctorService.getAllDoctors(pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/search")
    @Operation(summary = "Search doctors with optional filters", description = "Filters active and verified doctors by specialization, doctorName, clinicName, minExperience, and maxFee.")
    public ResponseEntity<Page<DoctorSummaryResponse>> searchDoctors(
            @RequestParam(required = false) String specialization,
            @RequestParam(required = false) String doctorName,
            @RequestParam(required = false) String clinicName,
            @RequestParam(required = false) Integer minExperience,
            @RequestParam(required = false) BigDecimal maxFee,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "experienceYears") String sortBy,
            @RequestParam(defaultValue = "DESC") String direction
    ) {
        Sort sort = direction.equalsIgnoreCase("ASC") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<DoctorSummaryResponse> response = doctorService.searchDoctors(
                specialization,
                doctorName,
                clinicName,
                minExperience,
                maxFee,
                pageable
        );
        return ResponseEntity.ok(response);
    }
}
