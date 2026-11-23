package com.medibook.medibook.controller;

import com.medibook.medibook.dto.request.LoginRequest;
import com.medibook.medibook.dto.request.RegisterPatientRequest;
import com.medibook.medibook.dto.response.AuthResponse;
import com.medibook.medibook.dto.response.UserResponse;
import com.medibook.medibook.security.JwtAuthenticationFilter;
import com.medibook.medibook.service.AuthenticationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Endpoints for patient registration, login, profile retrieval, and logout")
public class AuthenticationController {

    private final AuthenticationService authenticationService;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public AuthenticationController(
            AuthenticationService authenticationService,
            JwtAuthenticationFilter jwtAuthenticationFilter
    ) {
        this.authenticationService = authenticationService;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @PostMapping("/register/patient")
    @Operation(summary = "Register a new patient account", description = "Public endpoint to register as a patient with PATIENT role automatically assigned.")
    public ResponseEntity<AuthResponse> registerPatient(@Valid @RequestBody RegisterPatientRequest request) {
        AuthResponse response = authenticationService.registerPatient(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    @Operation(summary = "User login", description = "Authenticate credentials and obtain JWT access token.")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authenticationService.login(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    @SecurityRequirement(name = "BearerAuth")
    @Operation(summary = "Get current authenticated user profile", description = "Returns safe profile information for the authenticated user.")
    public ResponseEntity<UserResponse> getCurrentUser(@AuthenticationPrincipal UserDetails userDetails) {
        UserResponse response = authenticationService.getCurrentUser(userDetails.getUsername());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    @SecurityRequirement(name = "BearerAuth")
    @Operation(summary = "Logout user", description = "Invalidates the current JWT access token on the server side.")
    public ResponseEntity<Map<String, String>> logout(HttpServletRequest request) {
        String token = jwtAuthenticationFilter.extractJwtFromRequest(request);
        authenticationService.logout(token);
        return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
    }
}
