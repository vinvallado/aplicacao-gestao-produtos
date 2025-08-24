
package br.com.boticario.project.pmp.application.ports.out;

import br.com.boticario.project.pmp.domain.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Porta de saída que define o contrato para operações de persistência com a entidade Product.
 * A implementação concreta (Adapter) estará na camada de infraestrutura.
 */
public interface ProductRepositoryPort {

    /**
     * Salva um único produto.
     *
     * @param product o produto a ser salvo.
     * @return o produto salvo (com ID preenchido).
     */
    Product save(Product product);

    /**
     * Salva uma lista de produtos de forma otimizada.
     *
     * @param products a lista de produtos a ser salva.
     */
    void saveAll(List<Product> products);

    /**
     * Verifica se um produto com a combinação de nome e tipo já existe.
     *
     * @param product nome do produto.
     * @param type tipo do produto.
     * @return true se existir, false caso contrário.
     */
    boolean existsByProductAndType(String product, String type);

    /**
     * Busca produtos com base em critérios de filtro e paginação.
     *
     * @param name nome do produto (pode ser nulo).
     * @param minPrice preço mínimo (pode ser nulo).
     * @param maxPrice preço máximo (pode ser nulo).
     * @param industry setor/indústria do produto (pode ser nulo).
     * @param origin origem/estado do produto (pode ser nulo).
     * @param pageable objeto de paginação.
     * @return uma página (Page) de produtos que atendem aos critérios.
     */
    Page<Product> findByCriteria(String name, BigDecimal minPrice, BigDecimal maxPrice, String industry, String origin, Pageable pageable);

}
