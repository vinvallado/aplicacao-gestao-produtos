package br.com.boticario.agp.gestaoprodutos.repository;

import br.com.boticario.agp.gestaoprodutos.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;

/**
 * Interface para métodos de repositório personalizados para a entidade Product.
 */
public interface ProductRepositoryCustom {
    
    /**
     * Busca produtos com base em critérios de pesquisa.
     * 
     * @param name Nome do produto (opcional, busca parcial case-insensitive)
     * @param minPrice Preço mínimo (opcional)
     * @param maxPrice Preço máximo (opcional)
     * @param pageable Configuração de paginação e ordenação
     * @return Página de produtos que atendem aos critérios
     */
    Page<Product> findBySearchCriteria(
            String name, 
            BigDecimal minPrice, 
            BigDecimal maxPrice, 
            Pageable pageable);
}
