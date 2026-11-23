package com.medibook.medibook.repository;

import com.medibook.medibook.entity.DoctorAvailabilityException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DoctorAvailabilityExceptionRepository extends JpaRepository<DoctorAvailabilityException, UUID> {

    List<DoctorAvailabilityException> findByDoctorIdOrderByExceptionDateAsc(UUID doctorId);

    Optional<DoctorAvailabilityException> findByIdAndDoctorId(UUID id, UUID doctorId);

    boolean existsByDoctorIdAndExceptionDate(UUID doctorId, LocalDate exceptionDate);

    Optional<DoctorAvailabilityException> findByDoctorIdAndExceptionDate(UUID doctorId, LocalDate exceptionDate);
}
