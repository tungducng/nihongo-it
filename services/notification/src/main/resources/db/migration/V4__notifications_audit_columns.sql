-- V4: Add audit columns to notifications so the entity (which extends
-- AbstractAuditEntity) passes Hibernate strict schema validation.
ALTER TABLE notifications
    ADD COLUMN IF NOT EXISTS created_at TIMESTAMP NOT NULL DEFAULT now(),
    ADD COLUMN IF NOT EXISTS created_by VARCHAR(64),
    ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP NOT NULL DEFAULT now(),
    ADD COLUMN IF NOT EXISTS updated_by VARCHAR(64);
