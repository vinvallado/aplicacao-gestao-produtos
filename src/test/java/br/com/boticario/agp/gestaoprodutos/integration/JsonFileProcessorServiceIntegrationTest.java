package br.com.boticario.agp.gestaoprodutos.integration;

import br.com.boticario.agp.gestaoprodutos.AplicacaoGestaoProdutosApplication;
import br.com.boticario.agp.gestaoprodutos.model.Product;
import br.com.boticario.agp.gestaoprodutos.repository.ProductRepository;
import br.com.boticario.agp.gestaoprodutos.service.JsonFileProcessorService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Teste de integração para o JsonFileProcessorService.
 * Verifica o carregamento e processamento dos arquivos JSON.
 */
@Slf4j
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
        log.info("Limpando o banco de dados antes do teste...");
        productRepository.deleteAll();
    }

    @Test
    void testProcessJsonFiles_ShouldLoadAndSaveProducts() throws Exception {
        log.info("Iniciando teste de carregamento de produtos...");
        
        // Act
        jsonFileProcessorService.processJsonFiles()
                .get(10, TimeUnit.SECONDS); // Timeout de 10 segundos

        // Aguarda até que os produtos sejam processados
        await().atMost(30, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    // Assert
                    List<Product> products = productRepository.findAll();
                    log.info("Produtos encontrados no banco: {}", products.size());
                    
                    assertFalse(products.isEmpty(), "Deveria ter carregado produtos dos arquivos JSON");
                    
                    // Verifica se os produtos foram carregados corretamente
                    Optional<Product> product1Opt = productRepository.findByNameAndType("Produto Teste 1", "Tipo A");
                    assertTrue(product1Opt.isPresent(), "Produto 1 não foi encontrado");
                    Product product1 = product1Opt.get();
                    log.info("Produto 1 encontrado: {}", product1);
                    
                    assertEquals(0, new BigDecimal("19.99").compareTo(product1.getPrice()),
                            "Preço do produto 1 não corresponde ao esperado");
                    assertEquals(100, product1.getQuantity(),
                            "Quantidade do produto 1 não corresponde ao esperado");
                    
                    Optional<Product> product2Opt = productRepository.findByNameAndType("Produto Teste 2", "Tipo B");
                    assertTrue(product2Opt.isPresent(), "Produto 2 não foi encontrado");
                    Product product2 = product2Opt.get();
                    log.info("Produto 2 encontrado: {}", product2);
                    
                    assertEquals(0, new BigDecimal("29.99").compareTo(product2.getPrice()),
                            "Preço do produto 2 não corresponde ao esperado");
                    assertEquals(50, product2.getQuantity(),
                            "Quantidade do produto 2 não corresponde ao esperado");
                });
        
        log.info("Teste de carregamento de produtos concluído com sucesso!");
    }

    @Test
    void testProcessJsonFiles_ShouldNotDuplicateProducts() throws Exception {
        log.info("Iniciando teste de não duplicação de produtos...");
        
        // Primeira execução
        jsonFileProcessorService.processJsonFiles().get(10, TimeUnit.SECONDS);
        
        // Aguarda o processamento
        await().atMost(30, TimeUnit.SECONDS).until(() -> {
            List<Product> products = productRepository.findAll();
            log.info("Produtos após primeira execução: {}", products.size());
            return !products.isEmpty();
        });
        
        // Conta os produtos após a primeira execução
        long firstCount = productRepository.count();
        log.info("Total de produtos após primeira execução: {}", firstCount);
        
        // Segunda execução - não deve adicionar produtos duplicados
        jsonFileProcessorService.processJsonFiles().get(10, TimeUnit.SECONDS);
        
        // Aguarda um pouco para garantir que o processamento foi concluído
        Thread.sleep(2000);
        
        // Verifica se a contagem permanece a mesma
        long secondCount = productRepository.count();
        log.info("Total de produtos após segunda execução: {}", secondCount);
        
        assertEquals(firstCount, secondCount, "Não deveria adicionar produtos duplicados");
        
        log.info("Teste de não duplicação de produtos concluído com sucesso!");
    }
}
