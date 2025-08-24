
package br.com.boticario.project.pmp.infrastructure.persistence.mapper;

import br.com.boticario.project.pmp.domain.Product;
import br.com.boticario.project.pmp.infrastructure.persistence.model.ProductEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    @Mapping(target = "id", source = "id")
    @Mapping(target = "product", source = "product")
    @Mapping(target = "type", source = "type")
    @Mapping(target = "price", source = "price")
    @Mapping(target = "quantity", source = "quantity")
    @Mapping(target = "industry", source = "industry")
    @Mapping(target = "origin", source = "origin")
    ProductEntity toEntity(Product product);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "product", source = "product")
    @Mapping(target = "type", source = "type")
    @Mapping(target = "price", source = "price")
    @Mapping(target = "quantity", source = "quantity")
    @Mapping(target = "industry", source = "industry")
    @Mapping(target = "origin", source = "origin")
    Product toDomain(ProductEntity productEntity);

    List<Product> toDomainList(List<ProductEntity> productEntities);

}
