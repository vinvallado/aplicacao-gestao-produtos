-- Create user_roles enum type if it doesn't exist
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'user_role_enum') THEN
        CREATE TYPE user_role_enum AS ENUM ('ROLE_USER', 'ROLE_ADMIN');
    END IF;
END
$$;

-- Create users table
CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Create user_roles join table with explicit type casting
CREATE TABLE IF NOT EXISTS user_roles (
    user_id BIGINT NOT NULL,
    role VARCHAR(20) NOT NULL,
    PRIMARY KEY (user_id, role),
    CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

-- Create indexes for better query performance
CREATE INDEX IF NOT EXISTS idx_users_username ON users (username);
CREATE INDEX IF NOT EXISTS idx_users_email ON users (email);
CREATE INDEX IF NOT EXISTS idx_user_roles_user_id ON user_roles (user_id);

-- Insert a default admin user (password: admin123)
-- Note: In a real application, you should change this default password after first login
INSERT INTO users (username, email, password, full_name, enabled)
SELECT 'admin', 'admin@example.com', '$2a$10$XptfskLsT1SL/bOzZLkIf.1Ix9tWfEvWZkQqRqR2R3Fqlgekfcg9m', 'Administrator', true
WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'admin');

-- Assign admin role to the default admin user
INSERT INTO user_roles (user_id, role)
SELECT id, 'ROLE_ADMIN'
FROM users
WHERE username = 'admin'
AND NOT EXISTS (SELECT 1 FROM user_roles WHERE user_id = (SELECT id FROM users WHERE username = 'admin') AND role = 'ROLE_ADMIN');

-- Create test user for integration tests (password: password)
INSERT INTO users (username, email, password, full_name, enabled)
SELECT 'testuser', 'test@example.com', '$2a$10$XptfskLsT1SL/bOzZLkIf.1Ix9tWfEvWZkQqRqR2R3Fqlgekfcg9m', 'Test User', true
WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'testuser');

-- Assign user role to the test user
INSERT INTO user_roles (user_id, role)
SELECT id, 'ROLE_USER'
FROM users
WHERE username = 'testuser'
AND NOT EXISTS (SELECT 1 FROM user_roles WHERE user_id = (SELECT id FROM users WHERE username = 'testuser') AND role = 'ROLE_USER');
