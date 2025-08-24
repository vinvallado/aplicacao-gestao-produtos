
package br.com.boticario.project.pmp.infrastructure.web.controller;

import br.com.boticario.project.pmp.application.ports.in.CreateProductUseCasePort;
import br.com.boticario.project.pmp.application.ports.in.QueryProductUseCasePort;
import br.com.boticario.project.pmp.domain.Product;
import br.com.boticario.project.pmp.domain.ProductAlreadyExistsException;
import br.com.boticario.project.pmp.infrastructure.web.dto.ProductRequestDTO;
import br.com.boticario.project.pmp.infrastructure.web.dto.ProductResponseDTO;
import br.com.boticario.project.pmp.infrastructure.web.mapper.ProductWebMapper;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final CreateProductUseCasePort createProductUseCasePort;
    private final QueryProductUseCasePort queryProductUseCasePort;
    private final ProductWebMapper productWebMapper;

    public ProductController(CreateProductUseCasePort createProductUseCasePort, 
                           QueryProductUseCasePort queryProductUseCasePort, 
                           ProductWebMapper productWebMapper) {
        this.createProductUseCasePort = createProductUseCasePort;
        this.queryProductUseCasePort = queryProductUseCasePort;
        this.productWebMapper = productWebMapper;
    }

    @PostMapping
    public ResponseEntity<ProductResponseDTO> createProduct(@RequestBody @Valid ProductRequestDTO requestDTO) {
        Product product = productWebMapper.toDomain(requestDTO);
        Product createdProduct = createProductUseCasePort.createProduct(product);
        return ResponseEntity.status(HttpStatus.CREATED).body(productWebMapper.toResponseDTO(createdProduct));
    }

    @GetMapping
    public ResponseEntity<Page<ProductResponseDTO>> getProducts(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) String industry,
            @RequestParam(required = false) String origin,
            Pageable pageable) {

        if (name == null && minPrice == null && maxPrice == null && industry == null && origin == null) {
            return ResponseEntity.badRequest().build();
        }

        Page<Product> productsPage = queryProductUseCasePort.findByCriteria(
            name, 
            minPrice, 
            maxPrice, 
            industry, 
            origin,
            pageable
        );
        
        return ResponseEntity.ok(productWebMapper.toResponsePage(productsPage));
    }

    @ExceptionHandler(ProductAlreadyExistsException.class)
    public ResponseEntity<String> handleProductAlreadyExistsException(ProductAlreadyExistsException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
    }
}
