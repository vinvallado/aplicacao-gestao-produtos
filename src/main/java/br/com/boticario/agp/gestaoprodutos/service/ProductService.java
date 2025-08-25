package br.com.boticario.agp.gestaoprodutos.service;

import br.com.boticario.agp.gestaoprodutos.dto.PageResponse;
import br.com.boticario.agp.gestaoprodutos.dto.ProductRequest;
import br.com.boticario.agp.gestaoprodutos.dto.ProductResponse;
import br.com.boticario.agp.gestaoprodutos.dto.ProductSearchRequest;
import br.com.boticario.agp.gestaoprodutos.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Interface de serviço para operações relacionadas a produtos.
 */
public interface ProductService {
    
    /**
     * Busca produtos com base nos critérios fornecidos.
     * 
     * @param searchRequest Os critérios de busca
     * @param pageable As informações de paginação
     * @return Uma página de produtos que correspondem aos critérios
     */
    PageResponse<ProductResponse> searchProducts(ProductSearchRequest searchRequest, Pageable pageable);
    
    /**
     * Busca um produto pelo ID.
     * 
     * @param id O ID do produto a ser buscado
     * @return O produto encontrado
     * @throws br.com.boticario.agp.gestaoprodutos.exception.ResourceNotFoundException Se o produto não for encontrado
     */
    ProductResponse findById(Long id);
    
    /**
     * Cria um novo produto.
     * 
     * @param productRequest Os dados do produto a ser criado
     * @return O produto criado
     */
    ProductResponse createProduct(ProductRequest productRequest);
    
    /**
     * Atualiza um produto existente.
     * 
     * @param id O ID do produto a ser atualizado
     * @param productRequest Os novos dados do produto
     * @return O produto atualizado
     * @throws br.com.boticario.agp.gestaoprodutos.exception.ResourceNotFoundException Se o produto não for encontrado
     */
    ProductResponse updateProduct(Long id, ProductRequest productRequest);
    
    /**
     * Exclui um produto pelo ID.
     * 
     * @param id O ID do produto a ser excluído
     * @throws br.com.boticario.agp.gestaoprodutos.exception.ResourceNotFoundException Se o produto não for encontrado
     */
    void deleteProduct(Long id);
    
    /**
     * Verifica se já existe um produto com o mesmo nome e tipo.
     * 
     * @param name O nome do produto
     * @param type O tipo do produto
     * @return true se já existir um produto com o mesmo nome e tipo, false caso contrário
     */
    boolean existsByNameAndType(String name, String type);
    
    /**
     * Verifica se já existe um produto com o mesmo nome e tipo, excluindo um ID específico.
     * Útil para atualizações, onde não queremos considerar o próprio registro.
     * 
     * @param name O nome do produto
     * @param type O tipo do produto
     * @param id O ID do produto a ser excluído da verificação
     * @return true se já existir outro produto com o mesmo nome e tipo, false caso contrário
     */
    boolean existsByNameAndTypeAndIdNot(String name, String type, Long id);
}
