-- 1. Insertar o actualizar cuentas IAM con el hash BCrypt de 'SafeSanctuary2025#'
INSERT INTO iam.accounts (email, password_hash, role, status, biometric_enabled, failed_login_attempts, created_at, updated_at)
VALUES 
  ('carlos.paciente@safediary.com', '$2a$10$o7ANnGA81a4Wccklaqd4xuz3gicxrvjIy3qrgJCYO9BXlPcDPgfeq', 'PATIENT', 'ACTIVE', false, 0, NOW(), NOW()),
  ('elena.paciente@safediary.com', '$2a$10$o7ANnGA81a4Wccklaqd4xuz3gicxrvjIy3qrgJCYO9BXlPcDPgfeq', 'PATIENT', 'ACTIVE', false, 0, NOW(), NOW()),
  ('dra.laura@psicosalud.org', '$2a$10$o7ANnGA81a4Wccklaqd4xuz3gicxrvjIy3qrgJCYO9BXlPcDPgfeq', 'PSYCHOLOGIST', 'ACTIVE', false, 0, NOW(), NOW())
ON CONFLICT (email) DO UPDATE 
SET password_hash = EXCLUDED.password_hash,
    failed_login_attempts = 0,
    locked_until = NULL,
    status = 'ACTIVE';

-- 2. Asegurar suscripciones en Payments (la columna correcta es active_until)
INSERT INTO payments.subscriptions (patient_account_id, plan, status, active_until, created_at, updated_at)
SELECT a.id, 'FREE', 'ACTIVE', NULL, NOW(), NOW()
FROM iam.accounts a
WHERE a.email = 'carlos.paciente@safediary.com'
ON CONFLICT (patient_account_id) DO UPDATE
SET plan = EXCLUDED.plan,
    status = EXCLUDED.status;

INSERT INTO payments.subscriptions (patient_account_id, plan, status, active_until, created_at, updated_at)
SELECT a.id, 'TERRA', 'ACTIVE', NOW() + INTERVAL '1 month', NOW(), NOW()
FROM iam.accounts a
WHERE a.email = 'elena.paciente@safediary.com'
ON CONFLICT (patient_account_id) DO UPDATE
SET plan = EXCLUDED.plan,
    status = EXCLUDED.status,
    active_until = EXCLUDED.active_until;

-- 3. Asegurar perfiles en Profiles
INSERT INTO profiles.patients (first_name, last_name, email, password_hash, emergency_contact_name, emergency_contact_relationship, emergency_contact_phone, created_at, updated_at)
VALUES 
  ('Carlos', 'Mendoza', 'carlos.paciente@safediary.com', '$2a$10$o7ANnGA81a4Wccklaqd4xuz3gicxrvjIy3qrgJCYO9BXlPcDPgfeq', 'Maria Mendoza', 'PARENT', '+51987654321', NOW(), NOW()),
  ('Elena', 'Rojas', 'elena.paciente@safediary.com', '$2a$10$o7ANnGA81a4Wccklaqd4xuz3gicxrvjIy3qrgJCYO9BXlPcDPgfeq', 'Juan Rojas', 'SIBLING', '+51912345678', NOW(), NOW())
ON CONFLICT (email) DO UPDATE
SET first_name = EXCLUDED.first_name,
    last_name = EXCLUDED.last_name,
    emergency_contact_name = EXCLUDED.emergency_contact_name,
    emergency_contact_relationship = EXCLUDED.emergency_contact_relationship,
    emergency_contact_phone = EXCLUDED.emergency_contact_phone;
