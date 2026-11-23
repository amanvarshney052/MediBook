package com.medibook.medibook.mapper;

import com.medibook.medibook.dto.response.PatientResponse;
import com.medibook.medibook.dto.response.PatientSummaryResponse;
import com.medibook.medibook.entity.Patient;
import org.springframework.stereotype.Component;

@Component
public class PatientMapper {

    public PatientResponse toPatientResponse(Patient patient) {
        if (patient == null) {
            return null;
        }

        return PatientResponse.builder()
                .patientId(patient.getId())
                .userId(patient.getUser() != null ? patient.getUser().getId() : null)
                .fullName(patient.getUser() != null ? patient.getUser().getFullName() : null)
                .email(patient.getUser() != null ? patient.getUser().getEmail() : null)
                .phoneNumber(patient.getUser() != null ? patient.getUser().getPhoneNumber() : null)
                .dateOfBirth(patient.getDateOfBirth())
                .gender(patient.getGender())
                .address(patient.getAddress())
                .city(patient.getCity())
                .state(patient.getState())
                .postalCode(patient.getPostalCode())
                .emergencyContactName(patient.getEmergencyContactName())
                .emergencyContactPhone(patient.getEmergencyContactPhone())
                .emergencyContactRelationship(patient.getEmergencyContactRelationship())
                .profileImageUrl(patient.getProfileImageUrl())
                .isActive(patient.getUser() != null ? patient.getUser().getIsActive() : null)
                .createdAt(patient.getCreatedAt())
                .updatedAt(patient.getUpdatedAt())
                .build();
    }

    public PatientSummaryResponse toPatientSummaryResponse(Patient patient) {
        if (patient == null) {
            return null;
        }

        return PatientSummaryResponse.builder()
                .patientId(patient.getId())
                .fullName(patient.getUser() != null ? patient.getUser().getFullName() : null)
                .email(patient.getUser() != null ? patient.getUser().getEmail() : null)
                .phoneNumber(patient.getUser() != null ? patient.getUser().getPhoneNumber() : null)
                .city(patient.getCity())
                .profileImageUrl(patient.getProfileImageUrl())
                .isActive(patient.getUser() != null ? patient.getUser().getIsActive() : null)
                .createdAt(patient.getCreatedAt())
                .build();
    }
}
