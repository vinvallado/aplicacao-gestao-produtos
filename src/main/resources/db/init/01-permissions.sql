-- Script de inicialização para configurar permissões do usuário manager

-- Garantir que o banco de dados 'boticario_products' existe
SELECT 'CREATE DATABASE boticario_products'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'boticario_products')\gexec

-- Conectar ao banco de dados 'boticario_products'
\c boticario_products

-- Garantir que o usuário 'manager' existe e tem a senha correta
DO $$
BEGIN
    IF NOT EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = 'manager') THEN
        CREATE ROLE manager WITH LOGIN PASSWORD 'root';
    ELSE
        ALTER ROLE manager WITH PASSWORD 'root';
    END IF;
END
$$;

-- Conceder todas as permissões no banco de dados 'boticario_products' para o usuário 'manager'
GRANT ALL PRIVILEGES ON DATABASE boticario_products TO manager;

-- Garantir que o usuário 'manager' tem permissão para criar bancos de dados (opcional)
ALTER USER manager CREATEDB;

-- Conceder privilégios no esquema public
GRANT ALL PRIVILEGES ON SCHEMA public TO manager;

-- Conceder todos os privilégios em todos os objetos existentes
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO manager;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO manager;
GRANT ALL PRIVILEGES ON ALL FUNCTIONS IN SCHEMA public TO manager;

-- Garantir que o usuário manager tem privilégios em objetos futuros
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO manager;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON SEQUENCES TO manager;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON FUNCTIONS TO manager;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TYPES TO manager;
