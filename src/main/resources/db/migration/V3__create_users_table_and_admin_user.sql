-- Criação da tabela de usuários
CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    enabled BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Tabela para armazenar as funções (roles) dos usuários
CREATE TABLE IF NOT EXISTS user_roles (
    user_id BIGINT NOT NULL,
    role VARCHAR(20) NOT NULL,
    PRIMARY KEY (user_id, role),
    CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

-- Índice para melhorar consultas por username
CREATE INDEX IF NOT EXISTS idx_users_username ON users (username);

-- Inserir usuário administrador padrão (senha: admin123)
INSERT INTO users (username, password, full_name, email, enabled)
VALUES (
    'admin',
    '$2a$10$XptfskLsT1l/bRTLRiiCgegjHjOagcSkuDTB4zBv9J5UxE2q1lU8e', -- bcrypt hash para 'admin123'
    'Administrador do Sistema',
    'admin@boticario.com',
    true
) ON CONFLICT (username) DO NOTHING;

-- Atribuir função de administrador ao usuário admin
INSERT INTO user_roles (user_id, role)
SELECT id, 'ADMIN' FROM users WHERE username = 'admin'
ON CONFLICT (user_id, role) DO NOTHING;

-- Adicionar função de usuário regular ao admin também (opcional)
INSERT INTO user_roles (user_id, role)
SELECT id, 'USER' FROM users WHERE username = 'admin'
ON CONFLICT (user_id, role) DO NOTHING;
