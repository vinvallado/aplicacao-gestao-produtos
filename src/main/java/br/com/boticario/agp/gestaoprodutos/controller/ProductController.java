package br.com.boticario.agp.gestaoprodutos.controller;

import br.com.boticario.agp.gestaoprodutos.dto.PageResponse;
import br.com.boticario.agp.gestaoprodutos.dto.ProductRequest;
import br.com.boticario.agp.gestaoprodutos.dto.ProductResponse;
import br.com.boticario.agp.gestaoprodutos.dto.ProductSearchRequest;
import br.com.boticario.agp.gestaoprodutos.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST para operações relacionadas a produtos.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@Tag(name = "Produtos", description = "API para gerenciamento de produtos")
public class ProductController {

    private final ProductService productService;

    @Operation(summary = "Buscar produtos", description = "Busca produtos com base em critérios de pesquisa")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Produtos encontrados com sucesso", 
                     content = @Content(schema = @Schema(implementation = PageResponse.class))),
        @ApiResponse(responseCode = "400", description = "Parâmetros de busca inválidos"),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    @GetMapping
    public ResponseEntity<PageResponse<ProductResponse>> searchProducts(
            @Parameter(description = "Nome do produto para busca") @RequestParam(required = false) String name,
            @Parameter(description = "Preço mínimo para busca") @RequestParam(required = false) Double minPrice,
            @Parameter(description = "Preço máximo para busca") @RequestParam(required = false) Double maxPrice,
            @Parameter(description = "Número da página (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamanho da página") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Campo para ordenação (padrão: 'name')") @RequestParam(defaultValue = "name") String sort) {
        
        log.info("Recebida requisição para buscar produtos - Nome: {}, Preço Mín: {}, Preço Máx: {}, Página: {}, Tamanho: {}", 
                name, minPrice, maxPrice, page, size);
        
        // Cria o objeto de paginação
        Pageable pageable = PageRequest.of(page, size, Sort.by(sort));
        
        // Se nenhum critério for fornecido, retorna todos os produtos
        
        // Cria o objeto de busca
        ProductSearchRequest searchRequest = ProductSearchRequest.builder()
                .name(name)
                .minPrice(minPrice != null ? BigDecimal.valueOf(minPrice) : null)
                .maxPrice(maxPrice != null ? BigDecimal.valueOf(maxPrice) : null)
                .build();
                
        // Executa a busca
        PageResponse<ProductResponse> response = productService.searchProducts(searchRequest, pageable);
        
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Buscar produto por ID", description = "Busca um produto específico pelo seu ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Produto encontrado com sucesso", 
                     content = @Content(schema = @Schema(implementation = ProductResponse.class))),
        @ApiResponse(responseCode = "404", description = "Produto não encontrado"),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProductById(
            @Parameter(description = "ID do produto a ser buscado", required = true) @PathVariable Long id) {
        
        log.info("Recebida requisição para buscar produto com ID: {}", id);
        
        ProductResponse product = productService.findById(id);
        
        return ResponseEntity.ok(product);
    }

    @Operation(summary = "Criar novo produto", description = "Cria um novo produto no sistema")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Produto criado com sucesso", 
                     content = @Content(schema = @Schema(implementation = ProductResponse.class))),
        @ApiResponse(responseCode = "400", description = "Dados do produto inválidos"),
        @ApiResponse(responseCode = "409", description = "Já existe um produto com o mesmo nome e tipo"),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    @PostMapping
    public ResponseEntity<ProductResponse> createProduct(
            @Parameter(description = "Dados do produto a ser criado", required = true)
            @Valid @RequestBody ProductRequest productRequest) {
        
        log.info("Recebida requisição para criar novo produto: {}", productRequest);
        
        ProductResponse createdProduct = productService.createProduct(productRequest);
        
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(createdProduct);
    }

    @Operation(summary = "Atualizar produto existente", description = "Atualiza os dados de um produto existente")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Produto atualizado com sucesso", 
                     content = @Content(schema = @Schema(implementation = ProductResponse.class))),
        @ApiResponse(responseCode = "400", description = "Dados do produto inválidos"),
        @ApiResponse(responseCode = "404", description = "Produto não encontrado"),
        @ApiResponse(responseCode = "409", description = "Já existe outro produto com o mesmo nome e tipo"),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    @PutMapping("/{id}")
    public ResponseEntity<ProductResponse> updateProduct(
            @Parameter(description = "ID do produto a ser atualizado", required = true) @PathVariable Long id,
            @Parameter(description = "Novos dados do produto", required = true)
            @Valid @RequestBody ProductRequest productRequest) {
        
        log.info("Recebida requisição para atualizar produto ID {}: {}", id, productRequest);
        
        ProductResponse updatedProduct = productService.updateProduct(id, productRequest);
        
        return ResponseEntity.ok(updatedProduct);
    }

    @Operation(summary = "Excluir produto", description = "Remove um produto do sistema")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Produto excluído com sucesso"),
        @ApiResponse(responseCode = "404", description = "Produto não encontrado"),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(
            @Parameter(description = "ID do produto a ser excluído", required = true) @PathVariable Long id) {
        
        log.info("Recebida requisição para excluir produto com ID: {}", id);
        
        productService.deleteProduct(id);
        
        return ResponseEntity.noContent().build();
    }
}
