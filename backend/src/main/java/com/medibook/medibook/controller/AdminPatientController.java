package com.medibook.medibook.controller;

import com.medibook.medibook.dto.response.PatientResponse;
import com.medibook.medibook.dto.response.PatientSummaryResponse;
import com.medibook.medibook.service.PatientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/admin/patients")
@Tag(name = "Admin Patient Management", description = "Administrative endpoints to view, search, activate, and deactivate patients")
@PreAuthorize("hasRole('ADMIN')")
@SecurityRequirement(name = "BearerAuth")
public class AdminPatientController {

    private final PatientService patientService;

    public AdminPatientController(PatientService patientService) {
        this.patientService = patientService;
    }

    @GetMapping
    @Operation(summary = "Get all registered patients", description = "Returns paginated list of registered patient summaries.")
    public ResponseEntity<Page<PatientSummaryResponse>> getAllPatients(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String direction
    ) {
        Sort sort = direction.equalsIgnoreCase("ASC") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<PatientSummaryResponse> response = patientService.getAllPatients(pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{patientId}")
    @Operation(summary = "Get patient by ID", description = "Returns complete patient profile information for administration.")
    public ResponseEntity<PatientResponse> getPatientById(@PathVariable UUID patientId) {
        PatientResponse response = patientService.getPatientById(patientId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/search")
    @Operation(summary = "Search patients with optional filters", description = "Filters patients by patientName, email, and city.")
    public ResponseEntity<Page<PatientSummaryResponse>> searchPatients(
            @RequestParam(required = false) String patientName,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String city,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String direction
    ) {
        Sort sort = direction.equalsIgnoreCase("ASC") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<PatientSummaryResponse> response = patientService.searchPatients(patientName, email, city, pageable);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{patientId}/activate")
    @Operation(summary = "Activate patient account", description = "Enables patient account.")
    public ResponseEntity<PatientResponse> activatePatient(@PathVariable UUID patientId) {
        PatientResponse response = patientService.activatePatient(patientId);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{patientId}/deactivate")
    @Operation(summary = "Deactivate patient account", description = "Disables patient account, preventing login.")
    public ResponseEntity<PatientResponse> deactivatePatient(@PathVariable UUID patientId) {
        PatientResponse response = patientService.deactivatePatient(patientId);
        return ResponseEntity.ok(response);
    }
}
