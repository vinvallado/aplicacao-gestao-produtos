package br.com.boticario.aplicacaogestaoprodutos.repository;

import br.com.boticario.aplicacaogestaoprodutos.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositório para operações de banco de dados relacionadas a produtos.
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    
    /**
     * Verifica se existe um produto com o nome e tipo fornecidos.
     *
     * @param name Nome do produto
     * @param type Tipo do produto
     * @return true se existir um produto com o nome e tipo fornecidos, false caso contrário
     */
    boolean existsByNameAndType(String name, String type);
    
    /**
     * Busca um produto pelo nome e tipo.
     *
     * @param name Nome do produto
     * @param type Tipo do produto
     * @return Optional contendo o produto encontrado, se existir
     */
    Optional<Product> findByNameAndType(String name, String type);
}
