package com.medibook.medibook.mapper;

import com.medibook.medibook.dto.response.DoctorResponse;
import com.medibook.medibook.dto.response.DoctorSummaryResponse;
import com.medibook.medibook.entity.Doctor;
import org.springframework.stereotype.Component;

@Component
public class DoctorMapper {

    public DoctorResponse toDoctorResponse(Doctor doctor) {
        if (doctor == null) {
            return null;
        }

        return DoctorResponse.builder()
                .doctorId(doctor.getId())
                .userId(doctor.getUser() != null ? doctor.getUser().getId() : null)
                .fullName(doctor.getUser() != null ? doctor.getUser().getFullName() : null)
                .email(doctor.getUser() != null ? doctor.getUser().getEmail() : null)
                .phoneNumber(doctor.getUser() != null ? doctor.getUser().getPhoneNumber() : null)
                .specialization(doctor.getSpecialization())
                .qualification(doctor.getQualification())
                .experienceYears(doctor.getExperienceYears())
                .consultationFee(doctor.getConsultationFee())
                .clinicName(doctor.getClinicName())
                .clinicAddress(doctor.getClinicAddress())
                .bio(doctor.getBio())
                .licenseNumber(doctor.getLicenseNumber())
                .profileImageUrl(doctor.getProfileImageUrl())
                .isVerified(doctor.getIsVerified())
                .isActive(doctor.getUser() != null ? doctor.getUser().getIsActive() : null)
                .createdAt(doctor.getCreatedAt())
                .updatedAt(doctor.getUpdatedAt())
                .build();
    }

    public DoctorResponse toPublicDoctorResponse(Doctor doctor) {
        if (doctor == null) {
            return null;
        }

        return DoctorResponse.builder()
                .doctorId(doctor.getId())
                .userId(doctor.getUser() != null ? doctor.getUser().getId() : null)
                .fullName(doctor.getUser() != null ? doctor.getUser().getFullName() : null)
                .email(doctor.getUser() != null ? doctor.getUser().getEmail() : null)
                .phoneNumber(doctor.getUser() != null ? doctor.getUser().getPhoneNumber() : null)
                .specialization(doctor.getSpecialization())
                .qualification(doctor.getQualification())
                .experienceYears(doctor.getExperienceYears())
                .consultationFee(doctor.getConsultationFee())
                .clinicName(doctor.getClinicName())
                .clinicAddress(doctor.getClinicAddress())
                .bio(doctor.getBio())
                .licenseNumber(null) // Mask private license number for public view
                .profileImageUrl(doctor.getProfileImageUrl())
                .isVerified(doctor.getIsVerified())
                .isActive(doctor.getUser() != null ? doctor.getUser().getIsActive() : null)
                .createdAt(doctor.getCreatedAt())
                .updatedAt(doctor.getUpdatedAt())
                .build();
    }

    public DoctorSummaryResponse toDoctorSummaryResponse(Doctor doctor) {
        if (doctor == null) {
            return null;
        }

        return DoctorSummaryResponse.builder()
                .doctorId(doctor.getId())
                .fullName(doctor.getUser() != null ? doctor.getUser().getFullName() : null)
                .specialization(doctor.getSpecialization())
                .qualification(doctor.getQualification())
                .experienceYears(doctor.getExperienceYears())
                .consultationFee(doctor.getConsultationFee())
                .clinicName(doctor.getClinicName())
                .clinicAddress(doctor.getClinicAddress())
                .profileImageUrl(doctor.getProfileImageUrl())
                .build();
    }
}
