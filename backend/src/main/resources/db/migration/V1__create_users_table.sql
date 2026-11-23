-- =====================================================
-- Flyway Migration: V1__create_users_table.sql
-- Description: Create users table with constraints & indexes
-- =====================================================

CREATE EXTENSION IF NOT EXISTS "pgcrypto";

CREATE TABLE IF NOT EXISTS users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    full_name VARCHAR(150) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    phone_number VARCHAR(20),
    password VARCHAR(255) NOT NULL,
    role VARCHAR(30) NOT NULL CHECK (role IN ('ADMIN', 'DOCTOR', 'PATIENT')),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_users_role ON users(role);

-- Seed default initial admin (Password: Admin123!)
INSERT INTO users (id, full_name, email, phone_number, password, role, is_active)
VALUES (
    'a0000000-0000-0000-0000-000000000001',
    'System Administrator',
    'admin@medibook.com',
    '9999999999',
    '$2a$10$w3OvdN7r.n8rS/E14xY3vO8gN/J3m9w5/4V0w0vF3cQc6U1M4K2iq',
    'ADMIN',
    TRUE
)
ON CONFLICT (email) DO NOTHING;
