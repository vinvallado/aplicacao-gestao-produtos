package br.com.boticario.agp.gestaoprodutos.service;

import br.com.boticario.agp.gestaoprodutos.dto.PageResponse;
import br.com.boticario.agp.gestaoprodutos.dto.ProductRequest;
import br.com.boticario.agp.gestaoprodutos.dto.ProductSearchRequest;
import br.com.boticario.agp.gestaoprodutos.exception.ResourceNotFoundException;
import br.com.boticario.agp.gestaoprodutos.model.Product;
import br.com.boticario.agp.gestaoprodutos.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductServiceImpl productService;

    private Product product;
    private ProductRequest productRequest;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        product = Product.builder()
                .id(1L)
                .name("Test Product")
                .type("Test Type")
                .price(new BigDecimal("10.00"))
                .quantity(100)
                .industry("Test Industry")
                .origin("Test Origin")
                .build();

        productRequest = new ProductRequest();
        productRequest.setName("New Product");
        productRequest.setType("New Type");
        productRequest.setPrice(new BigDecimal("20.00"));
        productRequest.setQuantity(200);
        productRequest.setIndustry("New Industry");
        productRequest.setOrigin("New Origin");

        pageable = PageRequest.of(0, 10);
    }

    @Test
    void searchProducts_shouldReturnProducts_whenNameProvided() {
        ProductSearchRequest searchRequest = ProductSearchRequest.builder().name("Test").build();
        Page<Product> productPage = new PageImpl<>(Collections.singletonList(product));
        when(productRepository.findBySearchCriteria(any(), any(), any(), any(Pageable.class))).thenReturn(productPage);

        PageResponse<?> result = productService.searchProducts(searchRequest, pageable);

        assertNotNull(result);
        assertFalse(result.getContent().isEmpty());
        assertEquals(1, result.getContent().size());
        verify(productRepository, times(1)).findBySearchCriteria(any(), any(), any(), any(Pageable.class));
    }

    @Test
    void searchProducts_shouldReturnProducts_whenPriceRangeProvided() {
        ProductSearchRequest searchRequest = ProductSearchRequest.builder().minPrice(new BigDecimal("5.00")).maxPrice(new BigDecimal("15.00")).build();
        Page<Product> productPage = new PageImpl<>(Collections.singletonList(product));
        when(productRepository.findBySearchCriteria(any(), any(), any(), any(Pageable.class))).thenReturn(productPage);

        PageResponse<?> result = productService.searchProducts(searchRequest, pageable);

        assertNotNull(result);
        assertFalse(result.getContent().isEmpty());
        assertEquals(1, result.getContent().size());
        verify(productRepository, times(1)).findBySearchCriteria(any(), any(), any(), any(Pageable.class));
    }

    @Test
    void searchProducts_shouldThrowException_whenNoCriteriaProvided() {
        ProductSearchRequest searchRequest = ProductSearchRequest.builder().build();

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            productService.searchProducts(searchRequest, pageable);
        });

        assertEquals("Pelo menos um critério de busca deve ser informado (nome ou faixa de preço)", exception.getMessage());
        verify(productRepository, never()).findBySearchCriteria(any(), any(), any(), any(Pageable.class));
    }

    @Test
    void findById_shouldReturnProduct_whenProductExists() {
        when(productRepository.findById(anyLong())).thenReturn(Optional.of(product));

        var result = productService.findById(1L);

        assertNotNull(result);
        assertEquals(product.getName(), result.getName());
        verify(productRepository, times(1)).findById(anyLong());
    }

    @Test
    void findById_shouldThrowException_whenProductDoesNotExist() {
        when(productRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            productService.findById(1L);
        });
        verify(productRepository, times(1)).findById(anyLong());
    }

    @Test
    void createProduct_shouldCreateProduct_whenProductDoesNotExist() {
        when(productRepository.existsByNameAndType(anyString(), anyString())).thenReturn(false);
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> {
            Product savedProduct = invocation.getArgument(0);
            savedProduct.setId(1L); // Simulate ID being set by JPA
            return savedProduct;
        });

        var result = productService.createProduct(productRequest);

        assertNotNull(result);
        assertEquals(productRequest.getName(), result.getName());
        verify(productRepository, times(1)).existsByNameAndType(anyString(), anyString());
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    void createProduct_shouldThrowException_whenProductAlreadyExists() {
        when(productRepository.existsByNameAndType(anyString(), anyString())).thenReturn(true);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            productService.createProduct(productRequest);
        });

        assertEquals("Já existe um produto com o mesmo nome e tipo", exception.getMessage());
        verify(productRepository, times(1)).existsByNameAndType(anyString(), anyString());
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void updateProduct_shouldUpdateProduct_whenProductExistsAndNoDuplicate() {
        when(productRepository.findById(anyLong())).thenReturn(Optional.of(product));
        when(productRepository.existsByNameAndTypeAndIdNot(anyString(), anyString(), anyLong())).thenReturn(false);
        when(productRepository.save(any(Product.class))).thenReturn(product);

        var result = productService.updateProduct(1L, productRequest);

        assertNotNull(result);
        assertEquals(productRequest.getName(), result.getName());
        verify(productRepository, times(1)).findById(anyLong());
        verify(productRepository, times(1)).existsByNameAndTypeAndIdNot(anyString(), anyString(), anyLong());
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    void updateProduct_shouldThrowResourceNotFoundException_whenProductDoesNotExist() {
        when(productRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            productService.updateProduct(1L, productRequest);
        });
        verify(productRepository, times(1)).findById(anyLong());
        verify(productRepository, never()).existsByNameAndTypeAndIdNot(anyString(), anyString(), anyLong());
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void updateProduct_shouldThrowIllegalArgumentException_whenDuplicateExists() {
        when(productRepository.findById(anyLong())).thenReturn(Optional.of(product));
        when(productRepository.existsByNameAndTypeAndIdNot(anyString(), anyString(), anyLong())).thenReturn(true);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            productService.updateProduct(1L, productRequest);
        });

        assertEquals("Já existe outro produto com o mesmo nome e tipo", exception.getMessage());
        verify(productRepository, times(1)).findById(anyLong());
        verify(productRepository, times(1)).existsByNameAndTypeAndIdNot(anyString(), anyString(), anyLong());
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void deleteProduct_shouldDeleteProduct_whenProductExists() {
        when(productRepository.existsById(anyLong())).thenReturn(true);
        doNothing().when(productRepository).deleteById(anyLong());

        productService.deleteProduct(1L);

        verify(productRepository, times(1)).existsById(anyLong());
        verify(productRepository, times(1)).deleteById(anyLong());
    }

    @Test
    void deleteProduct_shouldThrowResourceNotFoundException_whenProductDoesNotExist() {
        when(productRepository.existsById(anyLong())).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> {
            productService.deleteProduct(1L);
        });
        verify(productRepository, times(1)).existsById(anyLong());
        verify(productRepository, never()).deleteById(anyLong());
    }

    @Test
    void existsByNameAndType_shouldReturnTrue_whenProductExists() {
        when(productRepository.existsByNameAndType(anyString(), anyString())).thenReturn(true);

        boolean exists = productService.existsByNameAndType("Name", "Type");

        assertTrue(exists);
        verify(productRepository, times(1)).existsByNameAndType(anyString(), anyString());
    }

    @Test
    void existsByNameAndType_shouldReturnFalse_whenProductDoesNotExist() {
        when(productRepository.existsByNameAndType(anyString(), anyString())).thenReturn(false);

        boolean exists = productService.existsByNameAndType("Name", "Type");

        assertFalse(exists);
        verify(productRepository, times(1)).existsByNameAndType(anyString(), anyString());
    }

    @Test
    void existsByNameAndTypeAndIdNot_shouldReturnTrue_whenProductExists() {
        when(productRepository.existsByNameAndTypeAndIdNot(anyString(), anyString(), anyLong())).thenReturn(true);

        boolean exists = productService.existsByNameAndTypeAndIdNot("Name", "Type", 2L);

        assertTrue(exists);
        verify(productRepository, times(1)).existsByNameAndTypeAndIdNot(anyString(), anyString(), anyLong());
    }

    @Test
    void existsByNameAndTypeAndIdNot_shouldReturnFalse_whenProductDoesNotExist() {
        when(productRepository.existsByNameAndTypeAndIdNot(anyString(), anyString(), anyLong())).thenReturn(false);

        boolean exists = productService.existsByNameAndTypeAndIdNot("Name", "Type", 2L);

        assertFalse(exists);
        verify(productRepository, times(1)).existsByNameAndTypeAndIdNot(anyString(), anyString(), anyLong());
    }
}
