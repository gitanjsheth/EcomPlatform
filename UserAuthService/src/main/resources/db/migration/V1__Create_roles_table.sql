-- Create roles table
CREATE TABLE roles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    role_name VARCHAR(50) NOT NULL UNIQUE,
    role_description VARCHAR(255),
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    created_by VARCHAR(100),
    updated_by VARCHAR(100)
);

-- Insert default role
INSERT INTO roles (role_name, role_description, status, created_at, updated_at, created_by) 
VALUES ('USER', 'Default user role', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'SYSTEM'); 