
package br.com.boticario.project.pmp.infrastructure.batch.mapper;

import br.com.boticario.project.pmp.domain.Product;
import br.com.boticario.project.pmp.infrastructure.batch.dto.ProductJsonDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.math.BigDecimal;
import java.util.List;

@Mapper(componentModel = "spring")
public interface ProductJsonMapper {

    @Mapping(target = "id", ignore = true) // ID é gerado pelo banco, não vem do JSON
    @Mapping(source = "product", target = "product")
    @Mapping(source = "type", target = "type")
    @Mapping(source = "price", target = "price", qualifiedByName = "mapPrice")
    @Mapping(source = "quantity", target = "quantity")
    @Mapping(source = "industry", target = "industry")
    @Mapping(source = "origin", target = "origin")
    Product toDomain(ProductJsonDTO dto);

    List<Product> toDomainList(List<ProductJsonDTO> dtoList);

    @Named("mapPrice")
    default BigDecimal mapPrice(String price) {
        if (price == null || price.isEmpty()) {
            return null;
        }
        // Remove o símbolo de dólar e vírgulas, depois converte para BigDecimal
        String cleanedPrice = price.replace("$", "").replace(",", "");
        return new BigDecimal(cleanedPrice);
    }
}
