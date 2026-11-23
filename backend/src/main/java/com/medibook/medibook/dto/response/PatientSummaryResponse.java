package com.medibook.medibook.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PatientSummaryResponse {
    private UUID patientId;
    private String fullName;
    private String email;
    private String phoneNumber;
    private String city;
    private String profileImageUrl;
    private Boolean isActive;
    private LocalDateTime createdAt;
}
