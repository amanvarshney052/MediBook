package com.medibook.medibook.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminUpdateDoctorRequest {

    @NotBlank(message = "Full name is required")
    @Size(max = 150, message = "Full name cannot exceed 150 characters")
    private String fullName;

    @Size(max = 20, message = "Phone number cannot exceed 20 characters")
    private String phoneNumber;

    @NotBlank(message = "Specialization is required")
    @Size(max = 100, message = "Specialization cannot exceed 100 characters")
    private String specialization;

    @NotBlank(message = "Qualification is required")
    @Size(max = 150, message = "Qualification cannot exceed 150 characters")
    private String qualification;

    @NotNull(message = "Experience years is required")
    @Min(value = 0, message = "Experience years cannot be negative")
    private Integer experienceYears;

    @NotNull(message = "Consultation fee is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Consultation fee cannot be negative")
    private BigDecimal consultationFee;

    @NotBlank(message = "Clinic name is required")
    @Size(max = 150, message = "Clinic name cannot exceed 150 characters")
    private String clinicName;

    @NotBlank(message = "Clinic address is required")
    private String clinicAddress;

    private String bio;

    @Size(max = 255, message = "Profile image URL cannot exceed 255 characters")
    private String profileImageUrl;
}
