package com.medibook.medibook.repository;

import com.medibook.medibook.entity.AvailabilityBreak;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AvailabilityBreakRepository extends JpaRepository<AvailabilityBreak, UUID> {

    List<AvailabilityBreak> findByAvailabilityIdOrderByStartTimeAsc(UUID availabilityId);

    Optional<AvailabilityBreak> findByIdAndAvailabilityId(UUID id, UUID availabilityId);

    @Query("SELECT COUNT(b) > 0 FROM AvailabilityBreak b " +
           "WHERE b.availability.id = :availabilityId " +
           "AND b.startTime < :endTime " +
           "AND b.endTime > :startTime")
    boolean existsOverlappingBreak(
            @Param("availabilityId") UUID availabilityId,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime
    );

    @Query("SELECT COUNT(b) > 0 FROM AvailabilityBreak b " +
           "WHERE b.availability.id = :availabilityId " +
           "AND b.startTime < :endTime " +
           "AND b.endTime > :startTime " +
           "AND b.id != :excludeId")
    boolean existsOverlappingBreakExcludingId(
            @Param("availabilityId") UUID availabilityId,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime,
            @Param("excludeId") UUID excludeId
    );
}
