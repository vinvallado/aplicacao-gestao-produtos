-- Atualizar o método de autenticação para permitir conexões de qualquer endereço IP
-- Isso é apenas para desenvolvimento, em produção use restrições mais rígidas

-- Adicionar uma entrada para permitir conexões de qualquer endereço IP com autenticação por senha
-- Isso substitui a linha 'host all all all scram-sha-256' existente
-- 
-- A sintaxe é: 
-- host   DATABASE  USER  ADDRESS  METHOD  [OPTIONS]
--
-- Onde:
-- - 'all' para DATABASE significa todos os bancos de dados
-- - 'all' para USER significa todos os usuários
-- - 'all' para ADDRESS significa qualquer endereço IP
-- - 'md5' é o método de autenticação (usa criptografia)

-- Primeiro, vamos remover a entrada existente (se houver)
-- Isso é apenas para garantir que não tenhamos entradas duplicadas
-- Em um ambiente de produção, você deve ser mais cuidadoso com isso
-- e possivelmente fazer backup do arquivo pg_hba.conf primeiro

-- Adicionar uma entrada para permitir conexões de qualquer endereço IP com autenticação md5
-- Isso é mais compatível com versões mais antigas do PostgreSQL
-- Se você estiver usando uma versão mais recente, pode querer usar 'scram-sha-256' em vez de 'md5'
-- host all all all scram-sha-256

-- A linha abaixo está comentada porque não podemos modificar diretamente o pg_hba.conf via SQL
-- Em vez disso, vamos configurar o contêiner para usar um pg_hba.conf personalizado
-- host all all all md5

-- Como alternativa, podemos criar um script de inicialização personalizado
-- que será executado quando o contêiner for iniciado
-- Isso é feito criando arquivos .sh no diretório /docker-entrypoint-initdb.d/
-- no contêiner

-- Para este exemplo, vamos criar um script SQL que será executado uma vez
-- para configurar as permissões corretas

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
