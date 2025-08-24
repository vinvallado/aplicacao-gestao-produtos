
package br.com.boticario.project.pmp.infrastructure.web.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponseDTO {

    private Long id;
    private String product;
    private String type;
    private String price;  // Formatted with $ symbol
    private Integer quantity;
    private String industry;
    private String origin;
    
    // Helper method to get price as BigDecimal (without $ symbol)
    public BigDecimal getPriceAsBigDecimal() {
        if (price == null || price.trim().isEmpty()) {
            return null;
        }
        String priceValue = price.replaceAll("[^\\d.]", "");
        return new BigDecimal(priceValue);
    }
}
