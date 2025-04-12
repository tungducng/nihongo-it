-- Insert default roles if they don't exist
INSERT INTO roles (role_id, role_name)
VALUES (1, 'ROLE_ADMIN')
ON CONFLICT (role_id) DO NOTHING;

INSERT INTO roles (role_id, role_name)
VALUES (2, 'ROLE_USER')
ON CONFLICT (role_id) DO NOTHING; 