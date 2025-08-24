package br.com.boticario.agp.gestaoprodutos.service;

import br.com.boticario.agp.gestaoprodutos.dto.PageResponse;
import br.com.boticario.agp.gestaoprodutos.dto.ProductRequest;
import br.com.boticario.agp.gestaoprodutos.dto.ProductResponse;
import br.com.boticario.agp.gestaoprodutos.dto.ProductSearchRequest;
import br.com.boticario.agp.gestaoprodutos.exception.ResourceNotFoundException;
import br.com.boticario.agp.gestaoprodutos.model.Product;
import br.com.boticario.agp.gestaoprodutos.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Implementação do serviço de produtos.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ProductResponse> searchProducts(ProductSearchRequest searchRequest, Pageable pageable) {
        log.debug("Buscando produtos com critérios: {}", searchRequest);
        
        // Valida se pelo menos um critério de busca foi informado
        if (!searchRequest.hasSearchCriteria()) {
            throw new IllegalArgumentException("Pelo menos um critério de busca deve ser informado (nome ou faixa de preço)");
        }
        
        // Configura a ordenação padrão por nome
        Pageable sortedPageable = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                Sort.by("name").ascending()
        );
        
        // Executa a busca com os critérios fornecidos
        Page<Product> productsPage = productRepository.findBySearchCriteria(
                searchRequest.getName(),
                searchRequest.getMinPrice(),
                searchRequest.getMaxPrice(),
                sortedPageable
        );
        
        // Converte os resultados para DTOs
        List<ProductResponse> content = productsPage.getContent().stream()
                .map(ProductResponse::fromEntity)
                .collect(Collectors.toList());
        
        log.debug("Encontrados {} produtos na página {}/{}", 
                content.size(), 
                productsPage.getNumber() + 1, 
                productsPage.getTotalPages());
        
        // Retorna a resposta paginada
        return PageResponse.fromPage(productsPage, content);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponse findById(Long id) {
        log.debug("Buscando produto com ID: {}", id);
        
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Produto", "id", id));
        
        return ProductResponse.fromEntity(product);
    }

    @Override
    @Transactional
    public ProductResponse createProduct(ProductRequest productRequest) {
        log.debug("Criando novo produto: {}", productRequest);
        
        // Verifica se já existe um produto com o mesmo nome e tipo
        if (existsByNameAndType(productRequest.getName(), productRequest.getType())) {
            throw new IllegalArgumentException("Já existe um produto com o mesmo nome e tipo");
        }
        
        // Converte o DTO para entidade
        Product product = Product.builder()
                .name(productRequest.getName())
                .type(productRequest.getType())
                .price(productRequest.getPrice())
                .quantity(productRequest.getQuantity())
                .industry(productRequest.getIndustry())
                .origin(productRequest.getOrigin())
                .build();
        
        // Salva o produto
        Product savedProduct = productRepository.save(product);
        log.info("Produto criado com sucesso: ID={}", savedProduct.getId());
        
        return ProductResponse.fromEntity(savedProduct);
    }

    @Override
    @Transactional
    public ProductResponse updateProduct(Long id, ProductRequest productRequest) {
        log.debug("Atualizando produto ID {}: {}", id, productRequest);
        
        // Busca o produto existente
        Product existingProduct = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Produto", "id", id));
        
        // Verifica se já existe outro produto com o mesmo nome e tipo (excluindo o atual)
        if (existsByNameAndTypeAndIdNot(
                productRequest.getName(), 
                productRequest.getType(), 
                id)) {
            
            throw new IllegalArgumentException("Já existe outro produto com o mesmo nome e tipo");
        }
        
        // Atualiza os dados do produto
        existingProduct.setName(productRequest.getName());
        existingProduct.setType(productRequest.getType());
        existingProduct.setPrice(productRequest.getPrice());
        existingProduct.setQuantity(productRequest.getQuantity());
        existingProduct.setIndustry(productRequest.getIndustry());
        existingProduct.setOrigin(productRequest.getOrigin());
        
        // Salva as alterações
        Product updatedProduct = productRepository.save(existingProduct);
        log.info("Produto atualizado com sucesso: ID={}", id);
        
        return ProductResponse.fromEntity(updatedProduct);
    }

    @Override
    @Transactional
    public void deleteProduct(Long id) {
        log.debug("Excluindo produto com ID: {}", id);
        
        // Verifica se o produto existe
        if (!productRepository.existsById(id)) {
            throw new ResourceNotFoundException("Produto", "id", id);
        }
        
        // Exclui o produto
        productRepository.deleteById(id);
        log.info("Produto excluído com sucesso: ID={}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByNameAndType(String name, String type) {
        return productRepository.existsByNameAndType(name, type);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByNameAndTypeAndIdNot(String name, String type, Long id) {
        return productRepository.existsByNameAndTypeAndIdNot(name, type, id);
    }
}
