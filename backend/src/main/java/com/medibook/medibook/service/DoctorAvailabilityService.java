package com.medibook.medibook.service;

import com.medibook.medibook.dto.request.*;
import com.medibook.medibook.dto.response.*;
import com.medibook.medibook.entity.AvailabilityBreak;
import com.medibook.medibook.entity.Doctor;
import com.medibook.medibook.entity.DoctorAvailability;
import com.medibook.medibook.entity.DoctorAvailabilityException;
import com.medibook.medibook.exception.BadRequestException;
import com.medibook.medibook.exception.ResourceAlreadyExistsException;
import com.medibook.medibook.exception.ResourceNotFoundException;
import com.medibook.medibook.mapper.AvailabilityMapper;
import com.medibook.medibook.repository.AvailabilityBreakRepository;
import com.medibook.medibook.repository.DoctorAvailabilityExceptionRepository;
import com.medibook.medibook.repository.DoctorAvailabilityRepository;
import com.medibook.medibook.repository.DoctorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class DoctorAvailabilityService {

    private final DoctorAvailabilityRepository availabilityRepository;
    private final AvailabilityBreakRepository breakRepository;
    private final DoctorAvailabilityExceptionRepository exceptionRepository;
    private final DoctorRepository doctorRepository;
    private final AvailabilityMapper availabilityMapper;

    public DoctorAvailabilityService(
            DoctorAvailabilityRepository availabilityRepository,
            AvailabilityBreakRepository breakRepository,
            DoctorAvailabilityExceptionRepository exceptionRepository,
            DoctorRepository doctorRepository,
            AvailabilityMapper availabilityMapper
    ) {
        this.availabilityRepository = availabilityRepository;
        this.breakRepository = breakRepository;
        this.exceptionRepository = exceptionRepository;
        this.doctorRepository = doctorRepository;
        this.availabilityMapper = availabilityMapper;
    }

    // ==========================================
    // 1. DOCTOR AVAILABILITY CRUD (DOCTOR SELF)
    // ==========================================

    @Transactional
    public AvailabilityResponse createAvailability(String email, CreateAvailabilityRequest request) {
        Doctor doctor = getDoctorByEmail(email);
        validateWorkingHours(request.getStartTime(), request.getEndTime(), request.getSlotDurationMinutes());

        boolean overlaps = availabilityRepository.existsOverlappingAvailability(
                doctor.getId(),
                request.getDayOfWeek(),
                request.getStartTime(),
                request.getEndTime()
        );
        if (overlaps) {
            throw new BadRequestException("Schedule overlaps with an existing availability for " + request.getDayOfWeek());
        }

        DoctorAvailability availability = DoctorAvailability.builder()
                .doctor(doctor)
                .dayOfWeek(request.getDayOfWeek())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .slotDurationMinutes(request.getSlotDurationMinutes())
                .isAvailable(request.getIsAvailable() != null ? request.getIsAvailable() : true)
                .build();

        DoctorAvailability saved = availabilityRepository.save(availability);
        return availabilityMapper.toAvailabilityResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<AvailabilityResponse> getMyAvailabilities(String email) {
        Doctor doctor = getDoctorByEmail(email);
        List<DoctorAvailability> availabilities = availabilityRepository
                .findByDoctorIdOrderByDayOfWeekAscStartTimeAsc(doctor.getId());
        return availabilities.stream()
                .map(availabilityMapper::toAvailabilityResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public AvailabilityResponse getMyAvailabilityById(String email, UUID availabilityId) {
        Doctor doctor = getDoctorByEmail(email);
        DoctorAvailability availability = availabilityRepository.findByIdAndDoctorId(availabilityId, doctor.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Availability not found with id: " + availabilityId));
        return availabilityMapper.toAvailabilityResponse(availability);
    }

    @Transactional
    public AvailabilityResponse updateAvailability(String email, UUID availabilityId, UpdateAvailabilityRequest request) {
        Doctor doctor = getDoctorByEmail(email);
        DoctorAvailability availability = availabilityRepository.findByIdAndDoctorId(availabilityId, doctor.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Availability not found with id: " + availabilityId));

        validateWorkingHours(request.getStartTime(), request.getEndTime(), request.getSlotDurationMinutes());

        boolean overlaps = availabilityRepository.existsOverlappingAvailabilityExcludingId(
                doctor.getId(),
                request.getDayOfWeek(),
                request.getStartTime(),
                request.getEndTime(),
                availabilityId
        );
        if (overlaps) {
            throw new BadRequestException("Schedule overlaps with an existing availability for " + request.getDayOfWeek());
        }

        // Verify existing breaks still fit inside the new working hours
        if (availability.getBreaks() != null) {
            for (AvailabilityBreak b : availability.getBreaks()) {
                if (b.getStartTime().isBefore(request.getStartTime()) || b.getEndTime().isAfter(request.getEndTime())) {
                    throw new BadRequestException("Existing break (" + b.getStartTime() + " - " + b.getEndTime() +
                            ") falls outside the updated working hours");
                }
            }
        }

        availability.setDayOfWeek(request.getDayOfWeek());
        availability.setStartTime(request.getStartTime());
        availability.setEndTime(request.getEndTime());
        availability.setSlotDurationMinutes(request.getSlotDurationMinutes());
        availability.setIsAvailable(request.getIsAvailable());

        DoctorAvailability updated = availabilityRepository.save(availability);
        return availabilityMapper.toAvailabilityResponse(updated);
    }

    @Transactional
    public void deleteAvailability(String email, UUID availabilityId) {
        Doctor doctor = getDoctorByEmail(email);
        DoctorAvailability availability = availabilityRepository.findByIdAndDoctorId(availabilityId, doctor.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Availability not found with id: " + availabilityId));
        availabilityRepository.delete(availability);
    }

    // ==========================================
    // 2. BREAK MANAGEMENT (DOCTOR SELF)
    // ==========================================

    @Transactional
    public AvailabilityBreakResponse addBreak(String email, UUID availabilityId, CreateAvailabilityBreakRequest request) {
        Doctor doctor = getDoctorByEmail(email);
        DoctorAvailability availability = availabilityRepository.findByIdAndDoctorId(availabilityId, doctor.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Availability not found with id: " + availabilityId));

        validateBreakTimes(request.getStartTime(), request.getEndTime(), availability);

        boolean overlaps = breakRepository.existsOverlappingBreak(
                availabilityId,
                request.getStartTime(),
                request.getEndTime()
        );
        if (overlaps) {
            throw new BadRequestException("Break overlaps with an existing break for this schedule");
        }

        AvailabilityBreak availabilityBreak = AvailabilityBreak.builder()
                .availability(availability)
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .reason(request.getReason() != null ? request.getReason().trim() : null)
                .build();

        AvailabilityBreak saved = breakRepository.save(availabilityBreak);
        return availabilityMapper.toAvailabilityBreakResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<AvailabilityBreakResponse> getBreaks(String email, UUID availabilityId) {
        Doctor doctor = getDoctorByEmail(email);
        // Verify ownership
        DoctorAvailability availability = availabilityRepository.findByIdAndDoctorId(availabilityId, doctor.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Availability not found with id: " + availabilityId));

        List<AvailabilityBreak> breaks = breakRepository.findByAvailabilityIdOrderByStartTimeAsc(availability.getId());
        return breaks.stream()
                .map(availabilityMapper::toAvailabilityBreakResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public AvailabilityBreakResponse updateBreak(
            String email,
            UUID availabilityId,
            UUID breakId,
            UpdateAvailabilityBreakRequest request
    ) {
        Doctor doctor = getDoctorByEmail(email);
        DoctorAvailability availability = availabilityRepository.findByIdAndDoctorId(availabilityId, doctor.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Availability not found with id: " + availabilityId));

        AvailabilityBreak availabilityBreak = breakRepository.findByIdAndAvailabilityId(breakId, availabilityId)
                .orElseThrow(() -> new ResourceNotFoundException("Break not found with id: " + breakId));

        validateBreakTimes(request.getStartTime(), request.getEndTime(), availability);

        boolean overlaps = breakRepository.existsOverlappingBreakExcludingId(
                availabilityId,
                request.getStartTime(),
                request.getEndTime(),
                breakId
        );
        if (overlaps) {
            throw new BadRequestException("Break overlaps with another existing break for this schedule");
        }

        availabilityBreak.setStartTime(request.getStartTime());
        availabilityBreak.setEndTime(request.getEndTime());
        availabilityBreak.setReason(request.getReason() != null ? request.getReason().trim() : null);

        AvailabilityBreak updated = breakRepository.save(availabilityBreak);
        return availabilityMapper.toAvailabilityBreakResponse(updated);
    }

    @Transactional
    public void deleteBreak(String email, UUID availabilityId, UUID breakId) {
        Doctor doctor = getDoctorByEmail(email);
        DoctorAvailability availability = availabilityRepository.findByIdAndDoctorId(availabilityId, doctor.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Availability not found with id: " + availabilityId));

        AvailabilityBreak availabilityBreak = breakRepository.findByIdAndAvailabilityId(breakId, availability.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Break not found with id: " + breakId));

        breakRepository.delete(availabilityBreak);
    }

    // ==========================================
    // 3. DATE EXCEPTION MANAGEMENT (DOCTOR SELF)
    // ==========================================

    @Transactional
    public AvailabilityExceptionResponse addException(String email, CreateAvailabilityExceptionRequest request) {
        Doctor doctor = getDoctorByEmail(email);

        if (request.getExceptionDate() == null) {
            throw new BadRequestException("Exception date is required");
        }

        boolean exists = exceptionRepository.existsByDoctorIdAndExceptionDate(doctor.getId(), request.getExceptionDate());
        if (exists) {
            throw new ResourceAlreadyExistsException("A date exception already exists for date: " + request.getExceptionDate());
        }

        DoctorAvailabilityException exception = DoctorAvailabilityException.builder()
                .doctor(doctor)
                .exceptionDate(request.getExceptionDate())
                .reason(request.getReason() != null ? request.getReason().trim() : null)
                .build();

        DoctorAvailabilityException saved = exceptionRepository.save(exception);
        return availabilityMapper.toAvailabilityExceptionResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<AvailabilityExceptionResponse> getMyExceptions(String email) {
        Doctor doctor = getDoctorByEmail(email);
        List<DoctorAvailabilityException> exceptions = exceptionRepository
                .findByDoctorIdOrderByExceptionDateAsc(doctor.getId());
        return exceptions.stream()
                .map(availabilityMapper::toAvailabilityExceptionResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteException(String email, UUID exceptionId) {
        Doctor doctor = getDoctorByEmail(email);
        DoctorAvailabilityException exception = exceptionRepository.findByIdAndDoctorId(exceptionId, doctor.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Date exception not found with id: " + exceptionId));
        exceptionRepository.delete(exception);
    }

    // ==========================================
    // 4. PUBLIC AVAILABILITY & AVAILABLE SLOTS
    // ==========================================

    @Transactional(readOnly = true)
    public List<AvailabilityResponse> getPublicDoctorAvailability(UUID doctorId) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found with id: " + doctorId));

        verifyDoctorActiveAndVerified(doctor);

        List<DoctorAvailability> availabilities = availabilityRepository
                .findByDoctorIdAndIsAvailableTrueOrderByDayOfWeekAscStartTimeAsc(doctorId);

        return availabilities.stream()
                .map(availabilityMapper::toAvailabilityResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AvailableSlotResponse> getAvailableSlots(UUID doctorId, LocalDate date) {
        if (date == null) {
            throw new BadRequestException("Date parameter is required");
        }

        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found with id: " + doctorId));

        verifyDoctorActiveAndVerified(doctor);

        // Check if date is blocked by date exception
        boolean isDateBlocked = exceptionRepository.existsByDoctorIdAndExceptionDate(doctorId, date);
        if (isDateBlocked) {
            return Collections.emptyList();
        }

        DayOfWeek dayOfWeek = date.getDayOfWeek();
        List<DoctorAvailability> availabilities = availabilityRepository
                .findByDoctorIdAndDayOfWeekAndIsAvailableTrueOrderByStartTimeAsc(doctorId, dayOfWeek);

        if (availabilities.isEmpty()) {
            return Collections.emptyList();
        }

        List<AvailableSlotResponse> availableSlots = new ArrayList<>();

        for (DoctorAvailability availability : availabilities) {
            int duration = availability.getSlotDurationMinutes();
            LocalTime current = availability.getStartTime();
            LocalTime end = availability.getEndTime();
            List<AvailabilityBreak> breaks = availability.getBreaks();

            while (!current.plusMinutes(duration).isAfter(end)) {
                LocalTime slotStart = current;
                LocalTime slotEnd = current.plusMinutes(duration);

                // Exclude slots that overlap with any break
                boolean overlapsBreak = false;
                if (breaks != null) {
                    for (AvailabilityBreak b : breaks) {
                        if (slotStart.isBefore(b.getEndTime()) && slotEnd.isAfter(b.getStartTime())) {
                            overlapsBreak = true;
                            break;
                        }
                    }
                }

                if (!overlapsBreak) {
                    availableSlots.add(AvailableSlotResponse.builder()
                            .startTime(slotStart)
                            .endTime(slotEnd)
                            .status("AVAILABLE")
                            .build());
                }

                current = current.plusMinutes(duration);
            }
        }

        return availableSlots;
    }

    // ==========================================
    // 5. ADMIN AVAILABILITY VIEW
    // ==========================================

    @Transactional(readOnly = true)
    public List<AvailabilityResponse> getDoctorAvailabilityForAdmin(UUID doctorId) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found with id: " + doctorId));

        List<DoctorAvailability> availabilities = availabilityRepository
                .findByDoctorIdOrderByDayOfWeekAscStartTimeAsc(doctor.getId());

        return availabilities.stream()
                .map(availabilityMapper::toAvailabilityResponse)
                .collect(Collectors.toList());
    }

    // ==========================================
    // HELPER & VALIDATION METHODS
    // ==========================================

    private Doctor getDoctorByEmail(String email) {
        Doctor doctor = doctorRepository.findByUserEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor profile not found for authenticated user"));

        if (!Boolean.TRUE.equals(doctor.getUser().getIsActive())) {
            throw new BadRequestException("Doctor account is deactivated");
        }
        return doctor;
    }

    private void verifyDoctorActiveAndVerified(Doctor doctor) {
        if (!Boolean.TRUE.equals(doctor.getUser().getIsActive())) {
            throw new BadRequestException("Doctor account is inactive");
        }
        if (!Boolean.TRUE.equals(doctor.getIsVerified())) {
            throw new BadRequestException("Doctor is not verified to expose availability");
        }
    }

    private void validateWorkingHours(LocalTime startTime, LocalTime endTime, Integer slotDurationMinutes) {
        if (!endTime.isAfter(startTime)) {
            throw new BadRequestException("End time must be after start time");
        }
        if (slotDurationMinutes == null || slotDurationMinutes <= 0) {
            throw new BadRequestException("Slot duration must be greater than zero");
        }
        long totalWorkingMinutes = Duration.between(startTime, endTime).toMinutes();
        if (slotDurationMinutes > totalWorkingMinutes) {
            throw new BadRequestException("Slot duration cannot exceed total working duration (" + totalWorkingMinutes + " minutes)");
        }
    }

    private void validateBreakTimes(LocalTime startTime, LocalTime endTime, DoctorAvailability availability) {
        if (!endTime.isAfter(startTime)) {
            throw new BadRequestException("Break end time must be after start time");
        }
        if (startTime.isBefore(availability.getStartTime()) || endTime.isAfter(availability.getEndTime())) {
            throw new BadRequestException("Break must fall within availability working hours (" +
                    availability.getStartTime() + " - " + availability.getEndTime() + ")");
        }
    }
}
