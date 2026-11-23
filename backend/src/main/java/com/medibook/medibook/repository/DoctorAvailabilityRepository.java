package com.medibook.medibook.repository;

import com.medibook.medibook.entity.DoctorAvailability;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DoctorAvailabilityRepository extends JpaRepository<DoctorAvailability, UUID> {

    List<DoctorAvailability> findByDoctorIdOrderByDayOfWeekAscStartTimeAsc(UUID doctorId);

    List<DoctorAvailability> findByDoctorIdAndDayOfWeekOrderByStartTimeAsc(UUID doctorId, DayOfWeek dayOfWeek);

    List<DoctorAvailability> findByDoctorIdAndIsAvailableTrueOrderByDayOfWeekAscStartTimeAsc(UUID doctorId);

    List<DoctorAvailability> findByDoctorIdAndDayOfWeekAndIsAvailableTrueOrderByStartTimeAsc(UUID doctorId, DayOfWeek dayOfWeek);

    Optional<DoctorAvailability> findByIdAndDoctorId(UUID id, UUID doctorId);

    @Query("SELECT COUNT(a) > 0 FROM DoctorAvailability a " +
           "WHERE a.doctor.id = :doctorId " +
           "AND a.dayOfWeek = :dayOfWeek " +
           "AND a.startTime < :endTime " +
           "AND a.endTime > :startTime")
    boolean existsOverlappingAvailability(
            @Param("doctorId") UUID doctorId,
            @Param("dayOfWeek") DayOfWeek dayOfWeek,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime
    );

    @Query("SELECT COUNT(a) > 0 FROM DoctorAvailability a " +
           "WHERE a.doctor.id = :doctorId " +
           "AND a.dayOfWeek = :dayOfWeek " +
           "AND a.startTime < :endTime " +
           "AND a.endTime > :startTime " +
           "AND a.id != :excludeId")
    boolean existsOverlappingAvailabilityExcludingId(
            @Param("doctorId") UUID doctorId,
            @Param("dayOfWeek") DayOfWeek dayOfWeek,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime,
            @Param("excludeId") UUID excludeId
    );
}
