package br.com.boticario.agp.gestaoprodutos.integration;

import br.com.boticario.agp.gestaoprodutos.AplicacaoGestaoProdutosApplication;
import br.com.boticario.agp.gestaoprodutos.model.Product;
import br.com.boticario.agp.gestaoprodutos.repository.ProductRepository;
import br.com.boticario.agp.gestaoprodutos.service.JsonFileProcessorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Teste de integração para o JsonFileProcessorService.
 * Verifica o carregamento e processamento dos arquivos JSON.
 */
@SpringBootTest(classes = AplicacaoGestaoProdutosApplication.class)
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application-test.properties")
class JsonFileProcessorServiceIntegrationTest {

    @Autowired
    private JsonFileProcessorService jsonFileProcessorService;

    @Autowired
    private ProductRepository productRepository;

    @BeforeEach
    void setUp() {
        // Limpa o banco de dados antes de cada teste
        productRepository.deleteAll();
    }

    @Test
    void testProcessJsonFiles_ShouldLoadAndSaveProducts() throws ExecutionException, InterruptedException, TimeoutException {
        // Act
        jsonFileProcessorService.processJsonFiles()
                .get(10, TimeUnit.SECONDS); // Timeout de 10 segundos

        // Aguarda até que os produtos sejam processados
        await().atMost(15, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    // Assert
                    List<Product> products = productRepository.findAll();
                    assertFalse(products.isEmpty(), "Deveria ter carregado produtos dos arquivos JSON");
                    
                    // Verifica se os produtos foram carregados corretamente
                    Product product1 = productRepository.findByNameAndType("Produto Teste 1", "Tipo A")
                            .orElse(null);
                    assertNotNull(product1, "Produto 1 não foi encontrado");
                    assertEquals(0, new BigDecimal("19.99").compareTo(product1.getPrice()));
                    assertEquals(100, product1.getQuantity());
                    
                    Product product2 = productRepository.findByNameAndType("Produto Teste 2", "Tipo B")
                            .orElse(null);
                    assertNotNull(product2, "Produto 2 não foi encontrado");
                    assertEquals(0, new BigDecimal("29.99").compareTo(product2.getPrice()));
                    assertEquals(50, product2.getQuantity());
                });
    }

    @Test
    void testProcessJsonFiles_ShouldNotDuplicateProducts() throws ExecutionException, InterruptedException, TimeoutException {
        // Primeira execução
        jsonFileProcessorService.processJsonFiles().get(10, TimeUnit.SECONDS);
        
        // Aguarda o processamento
        await().atMost(15, TimeUnit.SECONDS).until(() -> !productRepository.findAll().isEmpty());
        
        // Conta os produtos após a primeira execução
        long firstCount = productRepository.count();
        
        // Segunda execução - não deve adicionar produtos duplicados
        jsonFileProcessorService.processJsonFiles().get(10, TimeUnit.SECONDS);
        
        // Aguarda um pouco para garantir que o processamento foi concluído
        Thread.sleep(1000);
        
        // Verifica se a contagem permanece a mesma
        long secondCount = productRepository.count();
        assertEquals(firstCount, secondCount, "Não deveria adicionar produtos duplicados");
    }
}
