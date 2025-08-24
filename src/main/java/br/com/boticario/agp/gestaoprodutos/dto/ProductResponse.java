package br.com.boticario.agp.gestaoprodutos.dto;

import br.com.boticario.agp.gestaoprodutos.model.Product;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO para retornar os dados de um produto na resposta da API.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponse {
    
    private Long id;
    private String name;
    private String type;
    private BigDecimal price;
    private Integer quantity;
    private String industry;
    private String origin;
    
    /**
     * Converte um objeto Product para ProductResponse.
     *
     * @param product O produto a ser convertido
     * @return Um novo objeto ProductResponse com os dados do produto
     */
    public static ProductResponse fromEntity(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .type(product.getType())
                .price(product.getPrice())
                .quantity(product.getQuantity())
                .industry(product.getIndustry())
                .origin(product.getOrigin())
                .build();
    }
}
