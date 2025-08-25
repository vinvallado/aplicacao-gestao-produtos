package br.com.boticario.agp.gestaoprodutos.service;

import br.com.boticario.agp.gestaoprodutos.dto.ProductImportDto;
import br.com.boticario.agp.gestaoprodutos.exception.InvalidJsonFormatException;
import br.com.boticario.agp.gestaoprodutos.model.Product;
import br.com.boticario.agp.gestaoprodutos.repository.ProductRepository;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
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
import java.math.BigDecimal;
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
        log.info("Iniciando processamento assíncrono de arquivos JSON do diretório: {}", DATA_FILES_PATTERN);
        processJsonFiles().thenAccept(processedCount -> {
            log.info("Processamento concluído. Total de novos produtos salvos: {}", processedCount);
        }).exceptionally(ex -> {
            log.error("Erro durante o processamento dos arquivos JSON: {}", ex.getMessage(), ex);
            return null; // Retorna null para o thenAccept
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
            if (resources.length == 0) {
                log.warn("Nenhum arquivo encontrado no padrão: {}", DATA_FILES_PATTERN);
                return CompletableFuture.completedFuture(0);
            }
            log.info("Iniciando processamento de {} arquivos encontrados no diretório: {}", 
                    resources.length, Arrays.stream(resources).map(Resource::getFilename).collect(Collectors.toList()));

            // Processa os arquivos em paralelo
            List<CompletableFuture<List<Product>>> futures = Arrays.stream(resources)
                    .map(this::processFile)
                    .collect(Collectors.toList());

            // Combina os resultados de todas as tarefas assíncronas
            return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                    .thenApply(v -> {
                        List<Product> allProducts = futures.stream()
                                .map(CompletableFuture::join)
                                .flatMap(List::stream)
                                .collect(Collectors.toList());

                        // Filtra produtos duplicados e salva no banco de dados
                        return saveUniqueProducts(allProducts);
                    });
        } catch (Exception e) {
            log.error("Erro ao processar arquivos JSON: {}", e.getMessage(), e);
            return CompletableFuture.completedFuture(0);
        }
    }

    /**
     * Processa um único arquivo JSON de forma assíncrona.
     * Método package-private para permitir testes unitários.
     */
    @Async
    CompletableFuture<List<Product>> processFile(Resource resource) {
        String filename = resource.getFilename();
        log.debug("Iniciando processamento do arquivo: {}", filename);

        try (InputStream inputStream = resource.getInputStream()) {
            // Lê o JSON como um nó para verificar a estrutura
            com.fasterxml.jackson.databind.JsonNode rootNode = objectMapper.readTree(inputStream);
            
            // Verifica se o JSON tem a estrutura esperada (com campo 'data')
            com.fasterxml.jackson.databind.JsonNode dataNode = rootNode.path("data");
            List<ProductImportDto> productDtos;
            
            if (dataNode.isArray()) {
                // Se o JSON tem um campo 'data' com um array, usa esse array
                productDtos = objectMapper.convertValue(dataNode, new TypeReference<List<ProductImportDto>>() {});
            } else if (rootNode.isArray()) {
                // Se o JSON é diretamente um array, usa o rootNode
                productDtos = objectMapper.convertValue(rootNode, new TypeReference<List<ProductImportDto>>() {});
            } else {
                throw new InvalidJsonFormatException("Formato JSON inválido no arquivo " + filename + ": esperado um array ou um objeto com campo 'data' contendo um array");
            }
            
            // Converte DTOs para entidades, filtrando os inválidos
            List<Product> products = new ArrayList<>();
            for (int i = 0; i < productDtos.size(); i++) {
                ProductImportDto dto = productDtos.get(i);
                try {
                    validateProductDto(dto, filename, i + 1); // Valida DTO individualmente
                    products.add(convertToEntity(dto));
                } catch (InvalidJsonFormatException e) {
                    log.warn("Produto inválido no arquivo {}: {}", filename, e.getMessage());
                    // Continua processando os outros produtos
                }
            }

            log.info("Arquivo {} processado com sucesso. {} produtos válidos encontrados.", filename, products.size());
            return CompletableFuture.completedFuture(products);
        } catch (JsonParseException | JsonMappingException e) {
            String errorMsg = String.format("Formato JSON inválido no arquivo %s: %s", filename, e.getMessage());
            log.error(errorMsg, e);
            return CompletableFuture.completedFuture(Collections.emptyList()); // Retorna lista vazia para arquivo inválido
        } catch (IOException e) {
            String errorMsg = String.format("Erro ao ler o arquivo %s: %s", filename, e.getMessage());
            log.error(errorMsg, e);
            return CompletableFuture.completedFuture(Collections.emptyList()); // Retorna lista vazia para erro de leitura
        }
    }

    /**
     * Valida um único DTO de produto.
     * 
     * @param dto O DTO a ser validado
     * @param filename Nome do arquivo para mensagens de erro
     * @param index Índice do produto no arquivo (para mensagens de erro)
     * @throws InvalidJsonFormatException Se a validação falhar
     */
    private void validateProductDto(ProductImportDto dto, String filename, int index) {
        // Validação do campo product (nome do produto)
        if (dto.getProduct() == null || dto.getProduct().trim().isEmpty()) {
            throw new InvalidJsonFormatException(
                String.format("Erro no arquivo %s, produto #%d: O campo 'product' é obrigatório.", filename, index));
        }
        
        // Validação do campo type
        if (dto.getType() == null || dto.getType().trim().isEmpty()) {
            throw new InvalidJsonFormatException(
                String.format("Erro no arquivo %s, produto '%s': O campo 'type' é obrigatório.", filename, dto.getProduct()));
        }
        
        // Validação do campo price
        if (dto.getPrice() == null || dto.getPrice().trim().isEmpty()) {
            throw new InvalidJsonFormatException(
                String.format("Erro no arquivo %s, produto '%s': O campo 'price' é obrigatório.", filename, dto.getProduct()));
        }
        
        try {
            // Tenta converter o preço para validar o formato
            BigDecimal price = new BigDecimal(dto.getPrice().replace("$", ""));
            if (price.compareTo(BigDecimal.ZERO) <= 0) {
                throw new InvalidJsonFormatException(
                    String.format("Erro no arquivo %s, produto '%s': O campo 'price' deve ser maior que zero.", filename, dto.getProduct()));
            }
        } catch (NumberFormatException e) {
            throw new InvalidJsonFormatException(
                String.format("Erro no arquivo %s, produto '%s': Formato de preço inválido. Use o formato '$0.00'.", filename, dto.getProduct()));
        }
        
        // Validação do campo quantity
        if (dto.getQuantity() == null) {
            throw new InvalidJsonFormatException(
                String.format("Erro no arquivo %s, produto '%s': O campo 'quantity' é obrigatório.", filename, dto.getProduct()));
        }
        if (dto.getQuantity() < 0) {
            throw new InvalidJsonFormatException(
                String.format("Erro no arquivo %s, produto '%s': O campo 'quantity' não pode ser negativo.", filename, dto.getProduct()));
        }
        
        // Validação do campo industry (opcional)
        if (dto.getIndustry() == null) {
            dto.setIndustry(""); // Define como string vazia se for nulo
        }
        
        // Validação do campo origin (opcional)
        if (dto.getOrigin() == null) {
            dto.setOrigin(""); // Define como string vazia se for nulo
        }
    }

    /**
     * Salva apenas os produtos que ainda não existem no banco de dados.
     * 
     * @param products Lista de produtos a serem salvos
     * @return Número total de produtos salvos
     */
    @Transactional
    public int saveUniqueProducts(List<Product> products) {
        if (products == null || products.isEmpty()) {
            log.debug("Nenhum produto para salvar.");
            return 0;
        }

        log.debug("Verificando produtos duplicados entre {} produtos fornecidos", products.size());
        
        // Filtra para obter apenas produtos únicos na lista fornecida (com base no nome e tipo)
        List<Product> uniqueProducts = products.stream()
                .filter(distinctByKeys(Product::getName, Product::getType))
                .collect(Collectors.toList());

        // Busca produtos existentes no banco de dados
        Set<String> existingProducts = productRepository.findByNameInAndTypeIn(
                uniqueProducts.stream().map(Product::getName).collect(Collectors.toList()),
                uniqueProducts.stream().map(Product::getType).collect(Collectors.toList()))
                .stream()
                .map(p -> p.getName() + "|" + p.getType())
                .collect(Collectors.toSet());

        // Filtra para manter apenas produtos que não existem no banco de dados
        List<Product> newProducts = uniqueProducts.stream()
                .filter(p -> !existingProducts.contains(p.getName() + "|" + p.getType()))
                .collect(Collectors.toList());

        if (newProducts.isEmpty()) {
            log.info("Todos os produtos já existem no banco de dados. Nenhum novo produto para salvar.");
            return 0;
        }

        log.info("Ignorando {} produtos que já existem no banco de dados", 
                uniqueProducts.size() - newProducts.size());

        // Tamanho do lote para inserção em lotes
        final int batchSize = 100;
        int totalSaved = 0;

        log.info("Salvando {} novos produtos em {} lotes de até {} produtos cada", 
                newProducts.size(), (int) Math.ceil((double) newProducts.size() / batchSize), batchSize);

        // Salva em lotes para melhor performance
        for (int i = 0; i < newProducts.size(); i += batchSize) {
            int end = Math.min(newProducts.size(), i + batchSize);
            List<Product> batch = newProducts.subList(i, end);
            
            List<Product> savedBatch = productRepository.saveAll(batch);
            totalSaved += savedBatch.size();
            
            log.debug("Lote {}/{}: {} produtos salvos com sucesso", 
                    (i / batchSize) + 1, 
                    (int) Math.ceil((double) newProducts.size() / batchSize),
                    savedBatch.size());
        }

        log.info("Processo de salvamento concluído. Total de {} novos produtos salvos.", totalSaved);
        return totalSaved;
    }

    /**
     * Converte um DTO de importação para a entidade Product.
     * Método package-private para permitir testes unitários.
     */
    Product convertToEntity(ProductImportDto dto) {
        // Converte o preço de string (ex: "$1.99") para BigDecimal
        BigDecimal price = dto.getPrice() != null && !dto.getPrice().trim().isEmpty() 
                ? new BigDecimal(dto.getPrice().replace("$", "")) 
                : BigDecimal.ZERO;
                
        return Product.builder()
                .name(dto.getProduct())  // Usa getProduct() em vez de getName()
                .type(dto.getType())
                .price(price)
                .quantity(dto.getQuantity())
                .industry(dto.getIndustry())
                .origin(dto.getOrigin())
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
