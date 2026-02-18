-- Insert default permissions
INSERT INTO permissions (id, name, description, resource, action, created_at, updated_at) VALUES
    (gen_random_uuid(), 'USER_READ', 'Read user information', 'USER', 'READ', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (gen_random_uuid(), 'USER_CREATE', 'Create new users', 'USER', 'CREATE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (gen_random_uuid(), 'USER_UPDATE', 'Update user information', 'USER', 'UPDATE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (gen_random_uuid(), 'USER_DELETE', 'Delete users', 'USER', 'DELETE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (gen_random_uuid(), 'ROLE_READ', 'Read role information', 'ROLE', 'READ', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (gen_random_uuid(), 'ROLE_CREATE', 'Create new roles', 'ROLE', 'CREATE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (gen_random_uuid(), 'ROLE_UPDATE', 'Update role information', 'ROLE', 'UPDATE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (gen_random_uuid(), 'ROLE_DELETE', 'Delete roles', 'ROLE', 'DELETE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Insert default roles
INSERT INTO roles (id, name, description, created_at, updated_at) VALUES
    (gen_random_uuid(), 'ROLE_ADMIN', 'Administrator with full access', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (gen_random_uuid(), 'ROLE_USER', 'Standard user with basic access', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (gen_random_uuid(), 'ROLE_MODERATOR', 'Moderator with elevated privileges', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Assign all permissions to ROLE_ADMIN
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
CROSS JOIN permissions p
WHERE r.name = 'ROLE_ADMIN';

-- Assign read permissions to ROLE_USER
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
CROSS JOIN permissions p
WHERE r.name = 'ROLE_USER'
AND p.action = 'READ';

-- Assign read and update user permissions to ROLE_MODERATOR
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
CROSS JOIN permissions p
WHERE r.name = 'ROLE_MODERATOR'
AND (p.action = 'READ' OR (p.resource = 'USER' AND p.action = 'UPDATE'));
