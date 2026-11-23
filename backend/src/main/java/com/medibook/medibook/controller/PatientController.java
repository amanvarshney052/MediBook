package com.medibook.medibook.controller;

import com.medibook.medibook.dto.request.CreatePatientProfileRequest;
import com.medibook.medibook.dto.request.UpdatePatientProfileRequest;
import com.medibook.medibook.dto.response.PatientProfileStatusResponse;
import com.medibook.medibook.dto.response.PatientResponse;
import com.medibook.medibook.service.PatientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/patients")
@Tag(name = "Patients", description = "Endpoints for patient profile self-management")
@PreAuthorize("hasRole('PATIENT')")
@SecurityRequirement(name = "BearerAuth")
public class PatientController {

    private final PatientService patientService;

    public PatientController(PatientService patientService) {
        this.patientService = patientService;
    }

    @PostMapping("/me/profile")
    @Operation(summary = "Create patient profile", description = "Allows an authenticated patient to complete their personal profile.")
    public ResponseEntity<PatientResponse> createProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody CreatePatientProfileRequest request
    ) {
        PatientResponse response = patientService.createProfile(userDetails.getUsername(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/me")
    @Operation(summary = "Get authenticated patient profile", description = "Returns the profile of the currently logged-in patient.")
    public ResponseEntity<PatientResponse> getMyProfile(@AuthenticationPrincipal UserDetails userDetails) {
        PatientResponse response = patientService.getMyProfile(userDetails.getUsername());
        return ResponseEntity.ok(response);
    }

    @PutMapping("/me")
    @Operation(summary = "Update authenticated patient profile", description = "Allows a patient to update permitted personal details.")
    public ResponseEntity<PatientResponse> updateMyProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody UpdatePatientProfileRequest request
    ) {
        PatientResponse response = patientService.updateMyProfile(userDetails.getUsername(), request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me/profile-status")
    @Operation(summary = "Check profile completion status", description = "Returns whether the authenticated patient has completed their profile.")
    public ResponseEntity<PatientProfileStatusResponse> getProfileStatus(@AuthenticationPrincipal UserDetails userDetails) {
        PatientProfileStatusResponse response = patientService.getProfileStatus(userDetails.getUsername());
        return ResponseEntity.ok(response);
    }
}
