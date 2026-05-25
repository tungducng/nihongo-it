-- V9: audit `created_by` / `updated_by` columns for entities extending
-- AbstractAuditEntity. Hibernate's strict schema validator requires these to
-- exist when ddl-auto=validate.
ALTER TABLE users
    ADD COLUMN IF NOT EXISTS created_by VARCHAR(64),
    ADD COLUMN IF NOT EXISTS updated_by VARCHAR(64);
