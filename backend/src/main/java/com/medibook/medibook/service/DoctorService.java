package com.medibook.medibook.service;

import com.medibook.medibook.dto.request.AdminUpdateDoctorRequest;
import com.medibook.medibook.dto.request.CreateDoctorRequest;
import com.medibook.medibook.dto.request.UpdateDoctorProfileRequest;
import com.medibook.medibook.dto.response.DoctorResponse;
import com.medibook.medibook.dto.response.DoctorSummaryResponse;
import com.medibook.medibook.entity.Doctor;
import com.medibook.medibook.entity.User;
import com.medibook.medibook.enums.UserRole;
import com.medibook.medibook.exception.ResourceAlreadyExistsException;
import com.medibook.medibook.exception.ResourceNotFoundException;
import com.medibook.medibook.mapper.DoctorMapper;
import com.medibook.medibook.repository.DoctorRepository;
import com.medibook.medibook.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class DoctorService {

    private final DoctorRepository doctorRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final DoctorMapper doctorMapper;

    public DoctorService(
            DoctorRepository doctorRepository,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            DoctorMapper doctorMapper
    ) {
        this.doctorRepository = doctorRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.doctorMapper = doctorMapper;
    }

    // ==========================================
    // ADMIN: CREATE DOCTOR ACCOUNT
    // ==========================================
    @Transactional
    public DoctorResponse createDoctor(CreateDoctorRequest request) {
        String normalizedEmail = request.getEmail().trim().toLowerCase();

        // 1. Check if email already exists
        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new ResourceAlreadyExistsException("Email " + normalizedEmail + " is already registered");
        }

        // 2. Check if license number already exists
        if (doctorRepository.existsByLicenseNumber(request.getLicenseNumber().trim())) {
            throw new ResourceAlreadyExistsException("License number " + request.getLicenseNumber().trim() + " is already registered");
        }

        // 3. Create User account with DOCTOR role
        User user = User.builder()
                .fullName(request.getFullName().trim())
                .email(normalizedEmail)
                .phoneNumber(request.getPhoneNumber() != null ? request.getPhoneNumber().trim() : null)
                .password(passwordEncoder.encode(request.getPassword()))
                .role(UserRole.DOCTOR)
                .isActive(true)
                .build();

        User savedUser = userRepository.save(user);

        // 4. Create Doctor profile (initially unverified)
        Doctor doctor = Doctor.builder()
                .user(savedUser)
                .specialization(request.getSpecialization().trim())
                .qualification(request.getQualification().trim())
                .experienceYears(request.getExperienceYears())
                .consultationFee(request.getConsultationFee())
                .clinicName(request.getClinicName().trim())
                .clinicAddress(request.getClinicAddress().trim())
                .bio(request.getBio())
                .licenseNumber(request.getLicenseNumber().trim())
                .profileImageUrl(request.getProfileImageUrl())
                .isVerified(false)
                .build();

        Doctor savedDoctor = doctorRepository.save(doctor);
        return doctorMapper.toDoctorResponse(savedDoctor);
    }

    // ==========================================
    // DOCTOR: PROFILE SELF-MANAGEMENT
    // ==========================================
    @Transactional(readOnly = true)
    public DoctorResponse getMyProfile(String email) {
        Doctor doctor = doctorRepository.findByUserEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor profile not found for user: " + email));
        return doctorMapper.toDoctorResponse(doctor);
    }

    @Transactional
    public DoctorResponse updateMyProfile(String email, UpdateDoctorProfileRequest request) {
        Doctor doctor = doctorRepository.findByUserEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor profile not found for user: " + email));

        // Update permitted fields only
        doctor.setSpecialization(request.getSpecialization().trim());
        doctor.setQualification(request.getQualification().trim());
        doctor.setExperienceYears(request.getExperienceYears());
        doctor.setConsultationFee(request.getConsultationFee());
        doctor.setClinicName(request.getClinicName().trim());
        doctor.setClinicAddress(request.getClinicAddress().trim());
        doctor.setBio(request.getBio());
        doctor.setProfileImageUrl(request.getProfileImageUrl());

        Doctor updatedDoctor = doctorRepository.save(doctor);
        return doctorMapper.toDoctorResponse(updatedDoctor);
    }

    // ==========================================
    // PUBLIC / AUTHENTICATED: SEARCH & BROWSE
    // ==========================================
    @Transactional(readOnly = true)
    public DoctorResponse getDoctorById(UUID doctorId) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found with ID: " + doctorId));

        // Ensure doctor is active and verified for public display
        if (!Boolean.TRUE.equals(doctor.getIsVerified()) || !Boolean.TRUE.equals(doctor.getUser().getIsActive())) {
            throw new ResourceNotFoundException("Doctor is currently unavailable");
        }

        return doctorMapper.toPublicDoctorResponse(doctor);
    }

    @Transactional(readOnly = true)
    public Page<DoctorSummaryResponse> getAllDoctors(Pageable pageable) {
        return doctorRepository.findByIsVerifiedTrueAndUserIsActiveTrue(pageable)
                .map(doctorMapper::toDoctorSummaryResponse);
    }

    @Transactional(readOnly = true)
    public Page<DoctorSummaryResponse> searchDoctors(
            String specialization,
            String doctorName,
            String clinicName,
            Integer minExperience,
            BigDecimal maxFee,
            Pageable pageable
    ) {
        Specification<Doctor> spec = (root, query, cb) -> cb.and(
                cb.isTrue(root.get("isVerified")),
                cb.isTrue(root.get("user").get("isActive"))
        );

        if (specialization != null && !specialization.isBlank()) {
            String specPattern = "%" + specialization.trim().toLowerCase() + "%";
            spec = spec.and((root, query, cb) ->
                    cb.like(cb.lower(root.get("specialization")), specPattern));
        }

        if (doctorName != null && !doctorName.isBlank()) {
            String namePattern = "%" + doctorName.trim().toLowerCase() + "%";
            spec = spec.and((root, query, cb) ->
                    cb.like(cb.lower(root.get("user").get("fullName")), namePattern));
        }

        if (clinicName != null && !clinicName.isBlank()) {
            String clinicPattern = "%" + clinicName.trim().toLowerCase() + "%";
            spec = spec.and((root, query, cb) ->
                    cb.like(cb.lower(root.get("clinicName")), clinicPattern));
        }

        if (minExperience != null) {
            spec = spec.and((root, query, cb) ->
                    cb.greaterThanOrEqualTo(root.get("experienceYears"), minExperience));
        }

        if (maxFee != null) {
            spec = spec.and((root, query, cb) ->
                    cb.lessThanOrEqualTo(root.get("consultationFee"), maxFee));
        }

        return doctorRepository.findAll(spec, pageable).map(doctorMapper::toDoctorSummaryResponse);
    }

    // ==========================================
    // ADMIN: DOCTOR MANAGEMENT
    // ==========================================
    @Transactional(readOnly = true)
    public Page<DoctorResponse> getAllDoctorsForAdmin(Pageable pageable) {
        return doctorRepository.findAll(pageable)
                .map(doctorMapper::toDoctorResponse);
    }

    @Transactional(readOnly = true)
    public DoctorResponse getDoctorByIdForAdmin(UUID doctorId) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found with ID: " + doctorId));
        return doctorMapper.toDoctorResponse(doctor);
    }

    @Transactional
    public DoctorResponse updateDoctorByAdmin(UUID doctorId, AdminUpdateDoctorRequest request) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found with ID: " + doctorId));

        // Update User info
        User user = doctor.getUser();
        user.setFullName(request.getFullName().trim());
        if (request.getPhoneNumber() != null) {
            user.setPhoneNumber(request.getPhoneNumber().trim());
        }
        userRepository.save(user);

        // Update Doctor professional info
        doctor.setSpecialization(request.getSpecialization().trim());
        doctor.setQualification(request.getQualification().trim());
        doctor.setExperienceYears(request.getExperienceYears());
        doctor.setConsultationFee(request.getConsultationFee());
        doctor.setClinicName(request.getClinicName().trim());
        doctor.setClinicAddress(request.getClinicAddress().trim());
        doctor.setBio(request.getBio());
        doctor.setProfileImageUrl(request.getProfileImageUrl());

        Doctor updatedDoctor = doctorRepository.save(doctor);
        return doctorMapper.toDoctorResponse(updatedDoctor);
    }

    @Transactional
    public DoctorResponse verifyDoctor(UUID doctorId) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found with ID: " + doctorId));

        doctor.setIsVerified(true);
        Doctor updatedDoctor = doctorRepository.save(doctor);
        return doctorMapper.toDoctorResponse(updatedDoctor);
    }

    @Transactional
    public DoctorResponse activateDoctor(UUID doctorId) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found with ID: " + doctorId));

        User user = doctor.getUser();
        user.setIsActive(true);
        userRepository.save(user);

        return doctorMapper.toDoctorResponse(doctor);
    }

    @Transactional
    public DoctorResponse deactivateDoctor(UUID doctorId) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found with ID: " + doctorId));

        User user = doctor.getUser();
        user.setIsActive(false);
        userRepository.save(user);

        return doctorMapper.toDoctorResponse(doctor);
    }
}
