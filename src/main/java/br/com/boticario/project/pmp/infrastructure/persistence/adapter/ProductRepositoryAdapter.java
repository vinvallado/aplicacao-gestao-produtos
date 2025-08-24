
package br.com.boticario.project.pmp.infrastructure.persistence.adapter;

import br.com.boticario.project.pmp.application.ports.out.ProductRepositoryPort;
import br.com.boticario.project.pmp.domain.Product;
import br.com.boticario.project.pmp.infrastructure.persistence.mapper.ProductMapper;
import br.com.boticario.project.pmp.infrastructure.persistence.model.ProductEntity;
import br.com.boticario.project.pmp.infrastructure.persistence.repository.SpringProductRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ProductRepositoryAdapter implements ProductRepositoryPort {

    private final SpringProductRepository springProductRepository;
    private final ProductMapper productMapper;

    public ProductRepositoryAdapter(SpringProductRepository springProductRepository, ProductMapper productMapper) {
        this.springProductRepository = springProductRepository;
        this.productMapper = productMapper;
    }

    @Override
    public Product save(Product product) {
        ProductEntity productEntity = productMapper.toEntity(product);
        ProductEntity savedEntity = springProductRepository.save(productEntity);
        return productMapper.toDomain(savedEntity);
    }

    @Override
    public void saveAll(List<Product> products) {
        List<ProductEntity> entities = products.stream().map(productMapper::toEntity).collect(Collectors.toList());
        springProductRepository.saveAll(entities);
    }

    @Override
    public boolean existsByProductAndType(String product, String type) {
        return springProductRepository.existsByProductAndType(product, type);
    }

    @Override
    public Page<Product> findByCriteria(String name, BigDecimal minPrice, BigDecimal maxPrice, String industry, String origin, Pageable pageable) {
        // A mágica da consulta dinâmica acontece aqui com a Specification
        Specification<ProductEntity> spec = createSpecification(name, minPrice, maxPrice, industry, origin);
        Page<ProductEntity> entityPage = springProductRepository.findAll(spec, pageable);
        // Mapeia a página de entidades para uma página de objetos de domínio
        return entityPage.map(productMapper::toDomain);
    }

    private Specification<ProductEntity> createSpecification(String name, BigDecimal minPrice, BigDecimal maxPrice, String industry, String origin) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (name != null && !name.isBlank()) {
                predicates.add(criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("product")), 
                    "%" + name.toLowerCase() + "%"
                ));
            }

            if (minPrice != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("price"), minPrice));
            }

            if (maxPrice != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("price"), maxPrice));
            }

            if (industry != null && !industry.isBlank()) {
                predicates.add(criteriaBuilder.equal(
                    criteriaBuilder.lower(root.get("industry")), 
                    industry.toLowerCase()
                ));
            }

            if (origin != null && !origin.isBlank()) {
                predicates.add(criteriaBuilder.equal(
                    criteriaBuilder.lower(root.get("origin")), 
                    origin.toLowerCase()
                ));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
