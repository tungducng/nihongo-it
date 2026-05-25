-- V8: audit `created_by` / `updated_by` columns for entities extending
-- AbstractAuditEntity. Hibernate's strict schema validator requires these to
-- exist when ddl-auto=validate.
ALTER TABLE categories
    ADD COLUMN IF NOT EXISTS created_by VARCHAR(64),
    ADD COLUMN IF NOT EXISTS updated_by VARCHAR(64);

ALTER TABLE topics
    ADD COLUMN IF NOT EXISTS created_by VARCHAR(64),
    ADD COLUMN IF NOT EXISTS updated_by VARCHAR(64);

ALTER TABLE conversations
    ADD COLUMN IF NOT EXISTS created_by VARCHAR(64),
    ADD COLUMN IF NOT EXISTS updated_by VARCHAR(64);

ALTER TABLE flashcards
    ADD COLUMN IF NOT EXISTS created_by VARCHAR(64),
    ADD COLUMN IF NOT EXISTS updated_by VARCHAR(64);
