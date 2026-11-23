package com.medibook.medibook.repository;

import com.medibook.medibook.entity.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PatientRepository extends JpaRepository<Patient, UUID>, JpaSpecificationExecutor<Patient> {

    Optional<Patient> findByUserId(UUID userId);

    Optional<Patient> findByUserEmail(String email);

    boolean existsByUserId(UUID userId);
}
