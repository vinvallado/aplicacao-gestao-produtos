package br.com.boticario.agp.gestaoprodutos.service;

import br.com.boticario.agp.gestaoprodutos.dto.ProductImportDto;
import br.com.boticario.agp.gestaoprodutos.exception.InvalidJsonFormatException;
import br.com.boticario.agp.gestaoprodutos.model.Product;
import br.com.boticario.agp.gestaoprodutos.repository.ProductRepository;
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

    private static final String SAMPLE_JSON = "[{\"product\":\"Produto Teste\",\"type\":\"Tipo Teste\",\"price\":10.50,\"quantity\":100}]";

    @BeforeEach
    void setUp() {
        // Configuração comum para os testes, se necessário
    }

    @Test
    void testProcessFile_Success() throws Exception {
        // Arrange
        Resource resource = mock(Resource.class);
        when(resource.getInputStream())
                .thenReturn(new ByteArrayInputStream(SAMPLE_JSON.getBytes(StandardCharsets.UTF_8)));
        when(resource.getFilename()).thenReturn("test.json");

        // Act
        CompletableFuture<List<Product>> result = jsonFileProcessorService.processFile(resource);
        List<Product> products = result.get();

        // Assert
        assertNotNull(products);
        assertEquals(1, products.size());
        Product product = products.get(0);
        assertEquals("Produto Teste", product.getName());
        assertEquals("Tipo Teste", product.getType());
        assertEquals(0, new BigDecimal("10.50").compareTo(product.getPrice()));
        assertEquals(100, product.getQuantity());
    }

    @Test
    void testSaveUniqueProducts_WithNewProducts() {
        // Arrange
        Product newProduct = Product.builder()
                .name("Novo Produto")
                .type("Novo Tipo")
                .price(new BigDecimal("99.99"))
                .quantity(10)
                .build();

        when(productRepository.saveAll(anyList())).thenReturn(Collections.singletonList(newProduct));

        // Act
        int savedCount = jsonFileProcessorService.saveUniqueProducts(Collections.singletonList(newProduct));

        // Assert
        assertEquals(1, savedCount);
        verify(productRepository, times(1)).saveAll(anyList());
    }


    @Test
    void testValidateProductDto_InvalidPrice() {
        // Arrange
        ProductImportDto dto = new ProductImportDto();
        dto.setProduct("Test Product");
        dto.setType("Test Type");
        dto.setQuantity(10);
        dto.setIndustry("Test Industry");
        dto.setOrigin("Test Origin");

        // Test case 1: Null price
        dto.setPrice(null);
        InvalidJsonFormatException exception1 = assertThrows(InvalidJsonFormatException.class, () -> {
            jsonFileProcessorService.validateProductDto(dto, "test.json", 1);
        });
        assertTrue(exception1.getMessage().contains("O campo 'price' é obrigatório"));

        // Test case 2: Empty price
        dto.setPrice("");
        InvalidJsonFormatException exception2 = assertThrows(InvalidJsonFormatException.class, () -> {
            jsonFileProcessorService.validateProductDto(dto, "test.json", 1);
        });
        assertTrue(exception2.getMessage().contains("O campo 'price' é obrigatório"));

        // Test case 3: Zero price
        dto.setPrice("$0.00");
        InvalidJsonFormatException exception3 = assertThrows(InvalidJsonFormatException.class, () -> {
            jsonFileProcessorService.validateProductDto(dto, "test.json", 1);
        });
        assertTrue(exception3.getMessage().contains("O campo 'price' deve ser maior que zero"));

        // Test case 4: Negative price
        dto.setPrice("-$10.50");
        InvalidJsonFormatException exception4 = assertThrows(InvalidJsonFormatException.class, () -> {
            jsonFileProcessorService.validateProductDto(dto, "test.json", 1);
        });
        assertTrue(exception4.getMessage().contains("O campo 'price' deve ser maior que zero"));

        // Test case 5: Invalid price format
        dto.setPrice("abc");
        InvalidJsonFormatException exception5 = assertThrows(InvalidJsonFormatException.class, () -> {
            jsonFileProcessorService.validateProductDto(dto, "test.json", 1);
        });
        assertTrue(exception5.getMessage().contains("Formato de preço inválido"));

        // Test case 6: Invalid price format with R$
        dto.setPrice("R$abc");
        InvalidJsonFormatException exception6 = assertThrows(InvalidJsonFormatException.class, () -> {
            jsonFileProcessorService.validateProductDto(dto, "test.json", 1);
        });
        assertTrue(exception6.getMessage().contains("Formato de preço inválido"));
    }

    @Test
    void testValidateProductDto_ValidPrice() {
        // Arrange
        ProductImportDto dto = new ProductImportDto();
        dto.setProduct("Test Product");
        dto.setType("Test Type");
        dto.setQuantity(10);
        dto.setIndustry("Test Industry");
        dto.setOrigin("Test Origin");

        // Test case 1: Valid price with $ and dot
        dto.setPrice("$10.50");
        assertDoesNotThrow(() -> jsonFileProcessorService.validateProductDto(dto, "test.json", 1));

        // Test case 2: Valid price with R$ and comma
        dto.setPrice("R$10,50");
        assertDoesNotThrow(() -> jsonFileProcessorService.validateProductDto(dto, "test.json", 1));

        // Test case 3: Valid price without currency symbol, with dot
        dto.setPrice("25.75");
        assertDoesNotThrow(() -> jsonFileProcessorService.validateProductDto(dto, "test.json", 1));

        // Test case 4: Valid price without currency symbol, with comma
        dto.setPrice("30,00");
        assertDoesNotThrow(() -> jsonFileProcessorService.validateProductDto(dto, "test.json", 1));
    }

    @Test
    void testConvertToEntity() {
        // Arrange
        ProductImportDto dto = new ProductImportDto();
        dto.setProduct("Produto Teste");
        dto.setType("Tipo Teste");
        dto.setPrice("$19.99");
        dto.setQuantity(50);
        dto.setIndustry("Cosméticos");
        dto.setOrigin("SP");

        // Act
        Product product = jsonFileProcessorService.convertToEntity(dto);

        // Assert
        assertNotNull(product);
        assertEquals("Produto Teste", product.getName());
        assertEquals("Tipo Teste", product.getType());
        assertEquals(0, new BigDecimal("19.99").compareTo(product.getPrice()));
        assertEquals(50, product.getQuantity().intValue());
        assertEquals("Cosméticos", product.getIndustry());
        assertEquals("SP", product.getOrigin());
    }
}
