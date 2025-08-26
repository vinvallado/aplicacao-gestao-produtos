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
            CriteriaBuilder cb = entityManager.getCriteriaBuilder();

            // 1. Query for the actual results
            CriteriaQuery<Product> query = cb.createQuery(Product.class);
            Root<Product> root = query.from(Product.class);

            // Build predicates
            List<Predicate> predicates = new ArrayList<>();
            if (name != null && !name.trim().isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%"));
            }
            if (minPrice != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("price"), minPrice));
            }
            if (maxPrice != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("price"), maxPrice));
            }
            query.where(cb.and(predicates.toArray(new Predicate[0])));

            // Apply sorting
            if (pageable.getSort().isSorted()) {
                List<Order> orders = new ArrayList<>();
                for (Sort.Order order : pageable.getSort()) {
                    orders.add(order.isAscending() ? cb.asc(root.get(order.getProperty())) : cb.desc(root.get(order.getProperty())));
                }
                query.orderBy(orders);
            }

            // Execute query for results
            List<Product> result = entityManager.createQuery(query)
                    .setFirstResult((int) pageable.getOffset())
                    .setMaxResults(pageable.getPageSize())
                    .getResultList();

            // 2. Query for the total count
            CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
            Root<Product> countRoot = countQuery.from(Product.class);
            
            // Apply the same predicates to the count query
            if (!predicates.isEmpty()) {
                // Important: Re-create predicates on the new root if they are complex,
                // but for simple cases, applying the same logic is fine.
                // For safety, we rebuild the predicate list on the countRoot.
                List<Predicate> countPredicates = new ArrayList<>();
                 if (name != null && !name.trim().isEmpty()) {
                    countPredicates.add(cb.like(cb.lower(countRoot.get("name")), "%" + name.toLowerCase() + "%"));
                }
                if (minPrice != null) {
                    countPredicates.add(cb.greaterThanOrEqualTo(countRoot.get("price"), minPrice));
                }
                if (maxPrice != null) {
                    countPredicates.add(cb.lessThanOrEqualTo(countRoot.get("price"), maxPrice));
                }
                countQuery.where(cb.and(countPredicates.toArray(new Predicate[0])));
            }
            
            countQuery.select(cb.count(countRoot));

            // Execute count query
            Long total = entityManager.createQuery(countQuery).getSingleResult();

            log.info("Returning page {}/{} with {} items. Total items: {}",
                    pageable.getPageNumber() + 1, (total / pageable.getPageSize()) + 1, result.size(), total);

            return new PageImpl<>(result, pageable, total);

        } catch (Exception e) {
            log.error("Error in findBySearchCriteria: {}", e.getMessage(), e);
            throw e;
        }
    }
}
