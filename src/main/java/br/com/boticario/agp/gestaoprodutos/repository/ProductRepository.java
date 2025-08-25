package br.com.boticario.agp.gestaoprodutos.repository;

import br.com.boticario.agp.gestaoprodutos.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Repositório para operações de banco de dados relacionadas a produtos.
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, Long>, ProductRepositoryCustom {
    
    /**
     * Busca produtos por nome e tipo.
     *
     * @param name Nome do produto (case-insensitive, busca parcial)
     * @param type Tipo do produto (case-insensitive)
     * @return Lista de produtos que correspondem aos critérios
     */
    @Query("SELECT p FROM Product p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%')) AND LOWER(p.type) = LOWER(:type)")
    List<Product> findByNameContainingIgnoreCaseAndTypeIgnoreCase(@Param("name") String name, @Param("type") String type);
    
    /**
     * Verifica se existe um produto com o nome e tipo fornecidos.
     *
     * @param name Nome do produto
     * @param type Tipo do produto
     * @return true se existir um produto com o nome e tipo fornecidos, false caso contrário
     */
    boolean existsByNameAndType(String name, String type);
    
    /**
     * Verifica se existe um produto com o nome e tipo fornecidos, excluindo um ID específico.
     *
     * @param name Nome do produto
     * @param type Tipo do produto
     * @param id ID do produto a ser excluído da verificação
     * @return true se existir um produto com o mesmo nome e tipo, false caso contrário
     */
    boolean existsByNameAndTypeAndIdNot(String name, String type, Long id);
    
    /**
     * Busca produtos por tipo, com paginação.
     *
     * @param type Tipo do produto (case-insensitive)
     * @param pageable Configuração de paginação
     * @return Página de produtos do tipo especificado
     */
    Page<Product> findByTypeIgnoreCase(String type, Pageable pageable);
    
    /**
     * Busca produtos por intervalo de preço, com paginação.
     *
     * @param minPrice Preço mínimo (inclusive)
     * @param maxPrice Preço máximo (inclusive)
     * @param pageable Configuração de paginação
     * @return Página de produtos dentro do intervalo de preço especificado
     */
    Page<Product> findByPriceBetween(BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable);
    
    /**
     * Busca produtos por nome (case-insensitive, busca parcial), com paginação.
     *
     * @param name Parte do nome do produto
     * @param pageable Configuração de paginação
     * @return Página de produtos cujo nome contém a string fornecida
     */
    Page<Product> findByNameContainingIgnoreCase(String name, Pageable pageable);
    
    /**
     * Busca produtos pelo nome e tipo.
     *
     * @param names Lista de nomes de produtos
     * @param types Lista de tipos de produtos
     * @return Lista de produtos encontrados
     */
    List<Product> findByNameInAndTypeIn(List<String> names, List<String> types);
    
    /**
     * Busca um produto pelo nome e tipo.
     *
     * @param name Nome do produto
     * @param type Tipo do produto
     * @return Optional contendo o produto, se encontrado
     */
    Optional<Product> findByNameAndType(String name, String type);
}
