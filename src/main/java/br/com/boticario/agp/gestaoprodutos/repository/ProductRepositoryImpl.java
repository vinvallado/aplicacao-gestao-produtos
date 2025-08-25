package br.com.boticario.agp.gestaoprodutos.repository;

import br.com.boticario.agp.gestaoprodutos.model.Product;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementação do repositório personalizado para a entidade Product.
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class ProductRepositoryImpl implements ProductRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Page<Product> findBySearchCriteria(
            String name, 
            BigDecimal minPrice, 
            BigDecimal maxPrice, 
            Pageable pageable) {
        
        log.info("=== Starting product search with criteria ===");
        log.info("Search parameters - name: '{}', minPrice: {}, maxPrice: {}", name, minPrice, maxPrice);
        log.info("Pagination - page: {}, size: {}, sort: {}", 
                pageable.getPageNumber(), pageable.getPageSize(), pageable.getSort());
        
        try {
            // Criação do CriteriaBuilder e CriteriaQuery
            CriteriaBuilder cb = entityManager.getCriteriaBuilder();
            
            // Query para os resultados
            CriteriaQuery<Product> query = cb.createQuery(Product.class);
            Root<Product> product = query.from(Product.class);
            query.select(product);
            
            // Lista para armazenar as condições de busca
            List<Predicate> predicates = new ArrayList<>();
            
            // Adiciona condições de busca se os parâmetros forem fornecidos
            if (name != null && !name.trim().isEmpty()) {
                String searchName = "%" + name.toLowerCase() + "%";
                log.info("Adding name filter: {}", searchName);
                predicates.add(cb.like(cb.lower(product.get("name")), searchName));
            }
            
            if (minPrice != null) {
                log.info("Adding minPrice filter: {}", minPrice);
                predicates.add(cb.greaterThanOrEqualTo(product.get("price"), minPrice));
            }
            
            if (maxPrice != null) {
                log.info("Adding maxPrice filter: {}", maxPrice);
                predicates.add(cb.lessThanOrEqualTo(product.get("price"), maxPrice));
            }
            
            // Aplica os predicados à consulta
            if (!predicates.isEmpty()) {
                log.info("Applying {} predicates to query", predicates.size());
                query.where(cb.and(predicates.toArray(new Predicate[0])));
            } else {
                log.info("No filters applied, will return all products");
            }
            
            // Aplica a ordenação
            List<Order> orders = new ArrayList<>();
            if (pageable.getSort().isSorted()) {
                log.info("Applying sorting: {}", pageable.getSort());
                for (Sort.Order order : pageable.getSort()) {
                    String property = order.getProperty();
                    if (order.getDirection().isAscending()) {
                        orders.add(cb.asc(product.get(property)));
                        log.info("Added ASC sort for property: {}", property);
                    } else {
                        orders.add(cb.desc(product.get(property)));
                        log.info("Added DESC sort for property: {}", property);
                    }
                }
                query.orderBy(orders);
            } else {
                log.info("No sorting specified, using default");
            }
            
            // Executa a consulta para obter os resultados
            log.info("Executing paginated query with offset: {}, max results: {}", 
                    pageable.getOffset(), pageable.getPageSize());
            
            List<Product> result = entityManager.createQuery(query)
                    .setFirstResult((int) pageable.getOffset())
                    .setMaxResults(pageable.getPageSize())
                    .getResultList();
            
            log.info("Found {} products in current page", result.size());
            
            // Cria uma consulta para contar o total de resultados
            log.info("Executing count query");
            CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
            // Reutiliza o mesmo Root da query principal para a contagem
            countQuery.select(cb.count(product));
            // Reutiliza a mesma cláusula 'where' da query principal
            if (query.getRestriction() != null) {
                countQuery.where(query.getRestriction());
            }

            // Executa a consulta de contagem
            Long total = entityManager.createQuery(countQuery).getSingleResult();
            log.info("Total products matching criteria: {}", total);
            
            // Retorna uma página com os resultados e informações de paginação
            Page<Product> page = new PageImpl<>(result, pageable, total);
            log.info("Returning page {}/{} with {} items", 
                    page.getNumber() + 1, page.getTotalPages(), page.getNumberOfElements());
            
            return page;
            
        } catch (Exception e) {
            log.error("Error in findBySearchCriteria: {}", e.getMessage(), e);
            throw e;
        }
    }
}
