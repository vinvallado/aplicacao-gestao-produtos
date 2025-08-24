
package br.com.boticario.project.pmp.infrastructure.web.mapper;

import br.com.boticario.project.pmp.domain.Product;
import br.com.boticario.project.pmp.infrastructure.web.dto.ProductRequestDTO;
import br.com.boticario.project.pmp.infrastructure.web.dto.ProductResponseDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.data.domain.Page;

import java.math.BigDecimal;

@Mapper(componentModel = "spring")
public interface ProductWebMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "product", source = "dto.product")
    @Mapping(target = "type", source = "dto.type")
    @Mapping(target = "price", source = "dto", qualifiedByName = "mapPriceToBigDecimal")
    @Mapping(target = "quantity", source = "dto.quantity")
    @Mapping(target = "industry", source = "dto.industry")
    @Mapping(target = "origin", source = "dto.origin")
    Product toDomain(ProductRequestDTO dto);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "product", source = "product")
    @Mapping(target = "type", source = "type")
    @Mapping(target = "price", source = "price", qualifiedByName = "formatPriceWithDollar")
    @Mapping(target = "quantity", source = "quantity")
    @Mapping(target = "industry", source = "industry")
    @Mapping(target = "origin", source = "origin")
    ProductResponseDTO toResponseDTO(Product product);

    // Mapeia uma Page de domínio para uma Page de DTOs de resposta
    default Page<ProductResponseDTO> toResponsePage(Page<Product> productPage) {
        return productPage.map(this::toResponseDTO);
    }

    @Named("mapPriceToBigDecimal")
    default BigDecimal mapPriceToBigDecimal(ProductRequestDTO dto) {
        return dto.getPriceAsBigDecimal();
    }

    @Named("formatPriceWithDollar")
    default String formatPriceWithDollar(BigDecimal price) {
        if (price == null) {
            return null;
        }
        return String.format("$%.2f", price);
    }
}
