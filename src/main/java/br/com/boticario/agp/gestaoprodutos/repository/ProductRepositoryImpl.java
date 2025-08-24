package br.com.boticario.agp.gestaoprodutos.repository;

import br.com.boticario.agp.gestaoprodutos.model.Product;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Implementação do repositório personalizado para a entidade Product.
 */
@Repository
public class ProductRepositoryImpl implements ProductRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Page<Product> findBySearchCriteria(
            String name, 
            BigDecimal minPrice, 
            BigDecimal maxPrice, 
            Pageable pageable) {
        
        // Criação do CriteriaBuilder e CriteriaQuery
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Product> query = cb.createQuery(Product.class);
        Root<Product> product = query.from(Product.class);
        
        // Lista para armazenar as condições de busca
        List<Predicate> predicates = new ArrayList<>();
        
        // Adiciona condição para o nome (busca case-insensitive e parcial)
        if (StringUtils.hasText(name)) {
            predicates.add(cb.like(
                cb.lower(product.get("name")), 
                "%" + name.toLowerCase() + "%"
            ));
        }
        
        // Adiciona condição para o preço mínimo
        if (minPrice != null) {
            predicates.add(cb.greaterThanOrEqualTo(
                product.get("price"), 
                minPrice
            ));
        }
        
        // Adiciona condição para o preço máximo
        if (maxPrice != null) {
            predicates.add(cb.lessThanOrEqualTo(
                product.get("price"), 
                maxPrice
            ));
        }
        
        // Aplica as condições à consulta
        query.where(predicates.toArray(new Predicate[0]));
        
        // Aplica a ordenação
        if (pageable.getSort().isSorted()) {
            List<Order> orders = new ArrayList<>();
            pageable.getSort().forEach(order -> {
                if (order.isAscending()) {
                    orders.add(cb.asc(product.get(order.getProperty())));
                } else {
                    orders.add(cb.desc(product.get(order.getProperty())));
                }
            });
            query.orderBy(orders);
        }
        
        // Cria a consulta tipada
        TypedQuery<Product> typedQuery = entityManager.createQuery(query);
        
        // Aplica a paginação
        typedQuery.setFirstResult((int) pageable.getOffset());
        typedQuery.setMaxResults(pageable.getPageSize());
        
        // Executa a consulta para obter os resultados
        List<Product> resultList = typedQuery.getResultList();
        
        // Cria uma consulta para contar o total de resultados
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<Product> countRoot = countQuery.from(Product.class);
        countQuery.select(cb.count(countRoot));
        
        // Aplica as mesmas condições da consulta principal
        countQuery.where(predicates.toArray(new Predicate[0]));
        
        // Executa a contagem
        Long total = entityManager.createQuery(countQuery).getSingleResult();
        
        // Retorna uma página com os resultados e informações de paginação
        return new PageImpl<>(resultList, pageable, total);
    }
}
