-- Adiciona as colunas industry e origin à tabela products
ALTER TABLE products
    ADD COLUMN industry VARCHAR(100);
ALTER TABLE products
    ADD COLUMN origin VARCHAR(50);
