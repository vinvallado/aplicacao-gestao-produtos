-- Criação da tabela de produtos
CREATE TABLE IF NOT EXISTS products (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    type VARCHAR(50) NOT NULL,
    price NUMERIC(10, 2) NOT NULL,
    quantity INTEGER NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT products_name_type_key UNIQUE (name, type)
);

-- Comentários para documentação
COMMENT ON TABLE products IS 'Tabela de produtos do sistema';
COMMENT ON COLUMN products.id IS 'Identificador único do produto';
COMMENT ON COLUMN products.name IS 'Nome do produto';
COMMENT ON COLUMN products.type IS 'Tipo/categoria do produto';
COMMENT ON COLUMN products.price IS 'Preço do produto';
COMMENT ON COLUMN products.quantity IS 'Quantidade em estoque';
COMMENT ON COLUMN products.created_at IS 'Data de criação do registro';
COMMENT ON COLUMN products.updated_at IS 'Data da última atualização do registro';
