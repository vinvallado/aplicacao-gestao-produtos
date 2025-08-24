package br.com.boticario.aplicacaogestaoprodutos.service;

import br.com.boticario.aplicacaogestaoprodutos.dto.ProductImportDto;
import br.com.boticario.aplicacaogestaoprodutos.model.Product;
import br.com.boticario.aplicacaogestaoprodutos.repository.ProductRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.io.ResourceLoader;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JsonFileProcessorServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ResourcePatternResolver resourcePatternResolver;

    @InjectMocks
    private JsonFileProcessorService jsonFileProcessorService;

    private static final String SAMPLE_JSON = "[{\"name\":\"Produto Teste\",\"type\":\"Tipo Teste\",\"price\":10.50,\"quantity\":100}]";

    @BeforeEach
    void setUp() {
        // Configure default mocks here if needed
    }

    @Test
    void testProcessFile_Success() throws Exception {
        // Arrange
        Resource mockResource = mock(Resource.class);
        String sampleJson = "[{\"name\":\"Produto Teste\",\"type\":\"Tipo Teste\",\"price\":10.50,\"quantity\":100}]";
        InputStream inputStream = new ByteArrayInputStream(sampleJson.getBytes(StandardCharsets.UTF_8));
        when(mockResource.getInputStream()).thenReturn(inputStream);
        when(mockResource.getFilename()).thenReturn("test.json");

        // Act
        CompletableFuture<List<Product>> future = jsonFileProcessorService.processFile(mockResource);
        List<Product> result = future.get();

        // Assert
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals("Produto Teste", result.get(0).getName());
        assertEquals("Tipo Teste", result.get(0).getType());
        assertEquals(new BigDecimal("10.50"), result.get(0).getPrice());
        assertEquals(100, result.get(0).getQuantity());
    }

    @Test
    void testSaveUniqueProducts_WithNewProducts() {
        // Arrange
        Product existingProduct = Product.builder()
                .name("Existente")
                .type("Tipo 1")
                .price(BigDecimal.TEN)
                .quantity(5)
                .build();

        Product newProduct = Product.builder()
                .name("Novo")
                .type("Tipo 2")
                .price(BigDecimal.ONE)
                .quantity(10)
                .build();

        when(productRepository.findAll()).thenReturn(Collections.singletonList(existingProduct));
        when(productRepository.saveAll(anyList())).thenReturn(Collections.singletonList(newProduct));

        // Act
        int savedCount = jsonFileProcessorService.saveUniqueProducts(Arrays.asList(existingProduct, newProduct));

        // Assert
        assertEquals(1, savedCount);
        verify(productRepository, times(1)).saveAll(anyList());
    }
    
    @Test
    void testConvertToEntity() {
        // Arrange
        ProductImportDto dto = new ProductImportDto();
        dto.setName("Teste");
        dto.setType("Tipo Teste");
        dto.setPrice(BigDecimal.TEN);
        dto.setQuantity(5);

        // Act
        Product product = jsonFileProcessorService.convertToEntity(dto);

        // Assert
        assertNotNull(product);
        assertEquals("Teste", product.getName());
        assertEquals("Tipo Teste", product.getType());
        assertEquals(BigDecimal.TEN, product.getPrice());
        assertEquals(5, product.getQuantity());
    }
}
