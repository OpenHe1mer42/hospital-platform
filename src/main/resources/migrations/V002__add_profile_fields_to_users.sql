ALTER TABLE users
    ADD COLUMN IF NOT EXISTS name VARCHAR(100) NULL AFTER id,
    ADD COLUMN IF NOT EXISTS surname VARCHAR(100) NULL AFTER name,
    ADD COLUMN IF NOT EXISTS salted_password TEXT NULL AFTER password_hash,
    ADD COLUMN IF NOT EXISTS phone VARCHAR(50) NULL AFTER salted_password,
    ADD COLUMN IF NOT EXISTS gender VARCHAR(20) NULL AFTER phone,
    ADD COLUMN IF NOT EXISTS last_login_at DATETIME NULL AFTER is_active,
    ADD COLUMN IF NOT EXISTS date_of_birth DATE NULL AFTER last_login_at,
    ADD COLUMN IF NOT EXISTS address_id INT NULL AFTER date_of_birth,
    ADD COLUMN IF NOT EXISTS role_id INT NULL AFTER address_id;

CREATE INDEX IF NOT EXISTS idx_users_address_id ON users (address_id);
CREATE INDEX IF NOT EXISTS idx_users_role_id ON users (role_id);
