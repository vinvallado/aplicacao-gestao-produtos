package br.com.boticario.aplicacaogestaoprodutos.service;

import br.com.boticario.aplicacaogestaoprodutos.dto.ProductImportDto;
import br.com.boticario.aplicacaogestaoprodutos.model.Product;
import br.com.boticario.aplicacaogestaoprodutos.repository.ProductRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Serviço responsável por processar arquivos JSON de produtos de forma assíncrona e paralela.
 * Implementa estratégias para alta performance e prevenção de duplicatas.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class JsonFileProcessorService {

    private static final String DATA_FILES_PATTERN = "classpath:data/data_*.json";
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private final ProductRepository productRepository;
    private final ResourcePatternResolver resourcePatternResolver;

    /**
     * Inicializa o processamento dos arquivos JSON durante a inicialização da aplicação.
     * O processamento é feito de forma assíncrona para não bloquear a inicialização.
     */
    @PostConstruct
    public void init() {
        log.info("Iniciando processamento assíncrono de arquivos JSON...");
        processJsonFiles().thenAccept(processedCount -> {
            log.info("Processamento concluído. Total de produtos processados: {}", processedCount);
        }).exceptionally(ex -> {
            log.error("Erro durante o processamento dos arquivos JSON", ex);
            return null;
        });
    }

    /**
     * Processa os arquivos JSON de forma paralela e assíncrona.
     *
     * @return CompletableFuture contendo o número total de produtos processados
     */
    @Async
    @Transactional
    public CompletableFuture<Integer> processJsonFiles() {
        try {
            Resource[] resources = resourcePatternResolver.getResources(DATA_FILES_PATTERN);
            log.info("Encontrados {} arquivos para processamento", resources.length);

            // Processa os arquivos em paralelo
            List<CompletableFuture<List<Product>>> futures = Arrays.stream(resources)
                    .parallel()
                    .map(this::processFile)
                    .collect(Collectors.toList());

            // Aguarda a conclusão de todos os processamentos
            return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                    .thenApply(v -> {
                        // Combina todos os resultados
                        List<Product> allProducts = futures.stream()
                                .map(CompletableFuture::join)
                                .flatMap(List::stream)
                                .collect(Collectors.toList());

                        // Remove duplicatas baseado em nome e tipo
                        List<Product> uniqueProducts = allProducts.stream()
                                .filter(distinctByKeys(Product::getName, Product::getType))
                                .collect(Collectors.toList());

                        // Salva os produtos únicos
                        return saveUniqueProducts(uniqueProducts);
                    });

        } catch (Exception e) {
            log.error("Falha no processamento dos arquivos JSON", e);
            return CompletableFuture.completedFuture(0);
        }
    }

    /**
     * Processa um único arquivo JSON de forma assíncrona.
     * Método package-private para permitir testes unitários.
     */
    CompletableFuture<List<Product>> processFile(Resource resource) {
        return CompletableFuture.supplyAsync(() -> {
            try (InputStream inputStream = resource.getInputStream()) {
                log.debug("Processando arquivo: {}", resource.getFilename());

                // Lê a lista de produtos do arquivo
                List<ProductImportDto> productDtos = objectMapper.readValue(
                        inputStream,
                        new TypeReference<List<ProductImportDto>>() {}
                );

                log.info("Arquivo {} processado com sucesso. {} produtos encontrados.",
                        resource.getFilename(), productDtos.size());

                // Converte DTOs para entidades
                return productDtos.stream()
                        .map(this::convertToEntity)
                        .collect(Collectors.toList());

            } catch (IOException e) {
                log.error("Erro ao processar o arquivo: " + resource.getFilename(), e);
                return Collections.emptyList();
            }
        });
    }

    /**
     * Salva apenas os produtos que ainda não existem no banco de dados.
     * Utiliza processamento em lotes para melhor performance.
     */
    @Transactional
    protected int saveUniqueProducts(List<Product> products) {
        if (products == null || products.isEmpty()) {
            return 0;
        }

        // Filtra produtos que já existem no banco
        List<Product> existingProducts = productRepository.findAll();
        Set<String> existingProductKeys = existingProducts.stream()
                .map(p -> p.getName() + "|" + p.getType())
                .collect(Collectors.toSet());

        List<Product> newProducts = products.parallelStream()
                .filter(p -> !existingProductKeys.contains(p.getName() + "|" + p.getType()))
                .collect(Collectors.toList());

        if (!newProducts.isEmpty()) {
            // Processa em lotes para melhor performance
            int batchSize = 100;
            for (int i = 0; i < newProducts.size(); i += batchSize) {
                int end = Math.min(newProducts.size(), i + batchSize);
                List<Product> batch = newProducts.subList(i, end);
                productRepository.saveAll(batch);
                log.debug("Lote de {} produtos salvo ({} de {})",
                        batch.size(), end, newProducts.size());
            }
            log.info("Total de {} novos produtos salvos no banco de dados", newProducts.size());
        } else {
            log.info("Nenhum novo produto para salvar");
        }

        return newProducts.size();
    }

    /**
     * Converte um DTO de importação para a entidade Product.
     * Método package-private para permitir testes unitários.
     */
    Product convertToEntity(ProductImportDto dto) {
        return Product.builder()
                .name(dto.getName())
                .type(dto.getType())
                .price(dto.getPrice())
                .quantity(dto.getQuantity())
                .build();
    }

    /**
     * Método auxiliar para filtrar elementos distintos por múltiplas chaves.
     */
    @SafeVarargs
    private static <T> Predicate<T> distinctByKeys(Function<? super T, ?>... keyExtractors) {
        final Map<List<?>, Boolean> seen = new ConcurrentHashMap<>();
        return t -> {
            List<?> keys = Arrays.stream(keyExtractors)
                    .map(ke -> ke.apply(t))
                    .collect(Collectors.toList());
            return seen.putIfAbsent(keys, Boolean.TRUE) == null;
        };
    }
}