package com.medibook.medibook.repository;

import com.medibook.medibook.entity.Doctor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface DoctorRepository extends JpaRepository<Doctor, UUID>, JpaSpecificationExecutor<Doctor> {

    Optional<Doctor> findByUserId(UUID userId);

    Optional<Doctor> findByUserEmail(String email);

    boolean existsByLicenseNumber(String licenseNumber);

    Page<Doctor> findByIsVerifiedTrueAndUserIsActiveTrue(Pageable pageable);
}
