package com.medibook.medibook.service;

import com.medibook.medibook.dto.request.CreatePatientProfileRequest;
import com.medibook.medibook.dto.request.UpdatePatientProfileRequest;
import com.medibook.medibook.dto.response.PatientProfileStatusResponse;
import com.medibook.medibook.dto.response.PatientResponse;
import com.medibook.medibook.dto.response.PatientSummaryResponse;
import com.medibook.medibook.entity.Patient;
import com.medibook.medibook.entity.User;
import com.medibook.medibook.enums.UserRole;
import com.medibook.medibook.exception.BadRequestException;
import com.medibook.medibook.exception.ResourceAlreadyExistsException;
import com.medibook.medibook.exception.ResourceNotFoundException;
import com.medibook.medibook.mapper.PatientMapper;
import com.medibook.medibook.repository.PatientRepository;
import com.medibook.medibook.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
public class PatientService {

    private final PatientRepository patientRepository;
    private final UserRepository userRepository;
    private final PatientMapper patientMapper;

    public PatientService(
            PatientRepository patientRepository,
            UserRepository userRepository,
            PatientMapper patientMapper
    ) {
        this.patientRepository = patientRepository;
        this.userRepository = userRepository;
        this.patientMapper = patientMapper;
    }

    // ==========================================
    // PATIENT: PROFILE CREATION & SELF-MANAGEMENT
    // ==========================================
    @Transactional
    public PatientResponse createProfile(String email, CreatePatientProfileRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        if (user.getRole() != UserRole.PATIENT) {
            throw new BadRequestException("Only users with PATIENT role can create a patient profile");
        }

        if (patientRepository.existsByUserId(user.getId())) {
            throw new ResourceAlreadyExistsException("Patient profile already exists for this account");
        }

        Patient patient = Patient.builder()
                .user(user)
                .dateOfBirth(request.getDateOfBirth())
                .gender(request.getGender())
                .address(request.getAddress().trim())
                .city(request.getCity().trim())
                .state(request.getState().trim())
                .postalCode(request.getPostalCode().trim())
                .emergencyContactName(request.getEmergencyContactName().trim())
                .emergencyContactPhone(request.getEmergencyContactPhone().trim())
                .emergencyContactRelationship(request.getEmergencyContactRelationship().trim())
                .profileImageUrl(request.getProfileImageUrl())
                .build();

        Patient savedPatient = patientRepository.save(patient);
        return patientMapper.toPatientResponse(savedPatient);
    }

    @Transactional(readOnly = true)
    public PatientResponse getMyProfile(String email) {
        Patient patient = patientRepository.findByUserEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Patient profile not found. Please complete your profile."));
        return patientMapper.toPatientResponse(patient);
    }

    @Transactional
    public PatientResponse updateMyProfile(String email, UpdatePatientProfileRequest request) {
        Patient patient = patientRepository.findByUserEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Patient profile not found. Please create your profile first."));

        patient.setDateOfBirth(request.getDateOfBirth());
        patient.setGender(request.getGender());
        patient.setAddress(request.getAddress().trim());
        patient.setCity(request.getCity().trim());
        patient.setState(request.getState().trim());
        patient.setPostalCode(request.getPostalCode().trim());
        patient.setEmergencyContactName(request.getEmergencyContactName().trim());
        patient.setEmergencyContactPhone(request.getEmergencyContactPhone().trim());
        patient.setEmergencyContactRelationship(request.getEmergencyContactRelationship().trim());
        patient.setProfileImageUrl(request.getProfileImageUrl());

        Patient updatedPatient = patientRepository.save(patient);
        return patientMapper.toPatientResponse(updatedPatient);
    }

    @Transactional(readOnly = true)
    public PatientProfileStatusResponse getProfileStatus(String email) {
        Optional<Patient> patientOpt = patientRepository.findByUserEmail(email);
        return PatientProfileStatusResponse.builder()
                .profileCompleted(patientOpt.isPresent())
                .patientId(patientOpt.map(Patient::getId).orElse(null))
                .build();
    }

    // ==========================================
    // ADMIN: PATIENT MANAGEMENT
    // ==========================================
    @Transactional(readOnly = true)
    public Page<PatientSummaryResponse> getAllPatients(Pageable pageable) {
        return patientRepository.findAll(pageable)
                .map(patientMapper::toPatientSummaryResponse);
    }

    @Transactional(readOnly = true)
    public PatientResponse getPatientById(UUID patientId) {
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with ID: " + patientId));
        return patientMapper.toPatientResponse(patient);
    }

    @Transactional(readOnly = true)
    public Page<PatientSummaryResponse> searchPatients(
            String patientName,
            String email,
            String city,
            Pageable pageable
    ) {
        Specification<Patient> spec = (root, query, cb) -> cb.conjunction();

        if (patientName != null && !patientName.isBlank()) {
            String namePattern = "%" + patientName.trim().toLowerCase() + "%";
            spec = spec.and((root, query, cb) ->
                    cb.like(cb.lower(root.get("user").get("fullName")), namePattern));
        }

        if (email != null && !email.isBlank()) {
            String emailPattern = "%" + email.trim().toLowerCase() + "%";
            spec = spec.and((root, query, cb) ->
                    cb.like(cb.lower(root.get("user").get("email")), emailPattern));
        }

        if (city != null && !city.isBlank()) {
            String cityPattern = "%" + city.trim().toLowerCase() + "%";
            spec = spec.and((root, query, cb) ->
                    cb.like(cb.lower(root.get("city")), cityPattern));
        }

        return patientRepository.findAll(spec, pageable)
                .map(patientMapper::toPatientSummaryResponse);
    }

    @Transactional
    public PatientResponse activatePatient(UUID patientId) {
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with ID: " + patientId));

        User user = patient.getUser();
        user.setIsActive(true);
        userRepository.save(user);

        return patientMapper.toPatientResponse(patient);
    }

    @Transactional
    public PatientResponse deactivatePatient(UUID patientId) {
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with ID: " + patientId));

        User user = patient.getUser();
        user.setIsActive(false);
        userRepository.save(user);

        return patientMapper.toPatientResponse(patient);
    }
}
