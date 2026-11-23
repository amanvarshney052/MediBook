-- =====================================================
-- Flyway Migration: V4__create_doctor_availability_tables.sql
-- Description: Create doctor availability, breaks, and exceptions tables
-- =====================================================

-- Table 1: doctor_availabilities
CREATE TABLE IF NOT EXISTS doctor_availabilities (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    doctor_id UUID NOT NULL REFERENCES doctors(id) ON DELETE CASCADE,
    day_of_week VARCHAR(20) NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    slot_duration_minutes INTEGER NOT NULL,
    is_available BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_doctor_availabilities_time CHECK (end_time > start_time),
    CONSTRAINT chk_doctor_availabilities_slot_duration CHECK (slot_duration_minutes > 0),
    CONSTRAINT chk_doctor_availabilities_day CHECK (day_of_week IN ('MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY', 'SUNDAY'))
);

CREATE INDEX IF NOT EXISTS idx_doctor_availabilities_doctor_id ON doctor_availabilities(doctor_id);
CREATE INDEX IF NOT EXISTS idx_doctor_availabilities_day_of_week ON doctor_availabilities(day_of_week);

-- Table 2: availability_breaks
CREATE TABLE IF NOT EXISTS availability_breaks (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    availability_id UUID NOT NULL REFERENCES doctor_availabilities(id) ON DELETE CASCADE,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    reason VARCHAR(255),
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_availability_breaks_time CHECK (end_time > start_time)
);

CREATE INDEX IF NOT EXISTS idx_availability_breaks_availability_id ON availability_breaks(availability_id);

-- Table 3: doctor_availability_exceptions
CREATE TABLE IF NOT EXISTS doctor_availability_exceptions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    doctor_id UUID NOT NULL REFERENCES doctors(id) ON DELETE CASCADE,
    exception_date DATE NOT NULL,
    reason VARCHAR(255),
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_doctor_availability_exceptions UNIQUE (doctor_id, exception_date)
);

CREATE INDEX IF NOT EXISTS idx_doctor_availability_exceptions_doctor_id ON doctor_availability_exceptions(doctor_id);
CREATE INDEX IF NOT EXISTS idx_doctor_availability_exceptions_date ON doctor_availability_exceptions(exception_date);
