
package br.com.boticario.project.pmp.infrastructure.batch.importer;

import br.com.boticario.project.pmp.application.ports.out.ProductRepositoryPort;
import br.com.boticario.project.pmp.domain.Product;
import br.com.boticario.project.pmp.domain.ProductAlreadyExistsException;
import br.com.boticario.project.pmp.infrastructure.batch.dto.ProductJsonDTO;
import br.com.boticario.project.pmp.infrastructure.batch.mapper.ProductJsonMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationListener;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

@Component
public class ProductBatchImporter implements ApplicationListener<ApplicationReadyEvent> {

    private static final Logger log = LoggerFactory.getLogger(ProductBatchImporter.class);

    private final ProductRepositoryPort productRepositoryPort;
    private final ProductJsonMapper productJsonMapper;
    private final ObjectMapper objectMapper;
    private final ResourcePatternResolver resourcePatternResolver;
    private final ExecutorService batchExecutorService;

    public ProductBatchImporter(
            ProductRepositoryPort productRepositoryPort,
            ProductJsonMapper productJsonMapper,
            ObjectMapper objectMapper,
            ResourcePatternResolver resourcePatternResolver,
            @Qualifier("batchExecutorService") ExecutorService batchExecutorService) {
        this.productRepositoryPort = productRepositoryPort;
        this.productJsonMapper = productJsonMapper;
        this.objectMapper = objectMapper;
        this.resourcePatternResolver = resourcePatternResolver;
        this.batchExecutorService = batchExecutorService;
    }

    // Lista de arquivos JSON a serem importados
    private static final String[] JSON_FILES = {
            "classpath:data_1.json",
            "classpath:data_2.json",
            "classpath:data_3.json",
            "classpath:data_4.json"
    };

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        log.info("Iniciando importação de produtos em batch...");

        List<CompletableFuture<Void>> futures = Arrays.stream(JSON_FILES)
                .map(this::processFileAsync)
                .collect(Collectors.toList());

        // Espera que todas as importações assíncronas sejam concluídas
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .whenComplete((result, throwable) -> {
                    if (throwable != null) {
                        log.error("Importação de produtos concluída com erros: {}", throwable.getMessage(), throwable);
                    } else {
                        log.info("Importação de produtos em batch concluída com sucesso.");
                    }
                }).join(); // Bloqueia até que todas as futures sejam concluídas
    }

    private CompletableFuture<Void> processFileAsync(String filePath) {
        return CompletableFuture.runAsync(() -> {
            log.info("Processando arquivo: {}", filePath);
            try {
                Resource[] resources = resourcePatternResolver.getResources(filePath);
                if (resources.length == 0) {
                    log.warn("Arquivo não encontrado: {}", filePath);
                    return;
                }
                Resource resource = resources[0];

                // Lê o conteúdo do arquivo JSON
                List<ProductJsonDTO> productJsonDTOs = objectMapper.readValue(
                        resource.getInputStream(),
                        objectMapper.getTypeFactory().constructCollectionType(List.class, ProductJsonDTO.class)
                );

                List<Product> productsToSave = productJsonDTOs.stream()
                        .map(productJsonMapper::toDomain)
                        .collect(Collectors.toList());

                // Salva os produtos, tratando duplicatas
                for (Product product : productsToSave) {
                    try {
                        productRepositoryPort.save(product);
                    } catch (ProductAlreadyExistsException e) {
                        log.warn("Produto duplicado encontrado e ignorado: {}", e.getMessage());
                    } catch (Exception e) {
                        log.error("Erro ao salvar produto {}: {}", product.getProduct(), e.getMessage(), e);
                    }
                }
                log.info("Arquivo {} processado com sucesso. Total de {} produtos.", filePath, productsToSave.size());

            } catch (IOException e) {
                log.error("Erro de I/O ao ler arquivo {}: {}", filePath, e.getMessage(), e);
            } catch (Exception e) {
                log.error("Erro inesperado ao processar arquivo {}: {}", filePath, e.getMessage(), e);
            }
        }, batchExecutorService);
    }
}
