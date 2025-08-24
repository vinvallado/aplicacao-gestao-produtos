-- Adiciona as colunas industry e origin à tabela products
ALTER TABLE products
    ADD COLUMN IF NOT EXISTS industry VARCHAR(100),
    ADD COLUMN IF NOT EXISTS origin VARCHAR(50);
