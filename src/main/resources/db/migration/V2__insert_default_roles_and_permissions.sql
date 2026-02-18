-- Insert default roles
INSERT INTO roles (id, name, description, created_at) VALUES
    (gen_random_uuid(), 'ROLE_USER', 'Standard user role with basic permissions', NOW()),
    (gen_random_uuid(), 'ROLE_ADMIN', 'Administrator role with full permissions', NOW()),
    (gen_random_uuid(), 'ROLE_MODERATOR', 'Moderator role with elevated permissions', NOW()),
    (gen_random_uuid(), 'ROLE_SUPER_ADMIN', 'Super administrator with unrestricted access', NOW());

-- Insert default permissions
INSERT INTO permissions (id, name, description, resource, action, created_at) VALUES
    -- User permissions
    (gen_random_uuid(), 'USER_READ', 'Read user information', 'user', 'read', NOW()),
    (gen_random_uuid(), 'USER_WRITE', 'Create and update user information', 'user', 'write', NOW()),
    (gen_random_uuid(), 'USER_DELETE', 'Delete user accounts', 'user', 'delete', NOW()),
    (gen_random_uuid(), 'USER_MANAGE', 'Full user management capabilities', 'user', 'manage', NOW()),

    -- Role permissions
    (gen_random_uuid(), 'ROLE_READ', 'Read role information', 'role', 'read', NOW()),
    (gen_random_uuid(), 'ROLE_WRITE', 'Create and update roles', 'role', 'write', NOW()),
    (gen_random_uuid(), 'ROLE_DELETE', 'Delete roles', 'role', 'delete', NOW()),

    -- Permission permissions
    (gen_random_uuid(), 'PERMISSION_READ', 'Read permission information', 'permission', 'read', NOW()),
    (gen_random_uuid(), 'PERMISSION_WRITE', 'Create and update permissions', 'permission', 'write', NOW()),
    (gen_random_uuid(), 'PERMISSION_DELETE', 'Delete permissions', 'permission', 'delete', NOW());

-- Assign permissions to roles
-- ROLE_USER gets basic read permission
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.name = 'ROLE_USER' AND p.name = 'USER_READ';

-- ROLE_MODERATOR gets user read and write permissions
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.name = 'ROLE_MODERATOR' AND p.name IN ('USER_READ', 'USER_WRITE');

-- ROLE_ADMIN gets all user and role permissions
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.name = 'ROLE_ADMIN' AND p.name IN (
    'USER_READ', 'USER_WRITE', 'USER_DELETE', 'USER_MANAGE',
    'ROLE_READ', 'ROLE_WRITE', 'ROLE_DELETE'
);

-- ROLE_SUPER_ADMIN gets all permissions
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.name = 'ROLE_SUPER_ADMIN';
