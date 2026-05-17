INSERT INTO users (full_name, email, password_hash, role, is_active)
SELECT 'Carely Admin',
       'admin@carely.local',
       '$2a$12$ppKcWXwyTSh7zR674Dhts./AjsmL6rDKhjr2FT9qU7OFinGPKWDby',
       'ADMIN',
       TRUE
WHERE NOT EXISTS (
    SELECT 1 FROM users WHERE LOWER(email) = LOWER('admin@carely.local')
);
