
package br.com.boticario.project.pmp.infrastructure.web.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductRequestDTO {

    @NotBlank(message = "Product name cannot be empty")
    private String product;

    @NotBlank(message = "Product type cannot be empty")
    private String type;

    @NotBlank(message = "Price cannot be empty")
    @Pattern(regexp = "^\\$?\\.?\\d+\\.?\\d*$", message = "Invalid price format. Expected format: $0.00")
    private String price;

    @NotNull(message = "Quantity cannot be null")
    private Integer quantity;

    private String industry;
    
    private String origin;
    
    // Helper method to get price as BigDecimal (strips $ and converts)
    public BigDecimal getPriceAsBigDecimal() {
        if (price == null || price.trim().isEmpty()) {
            return null;
        }
        String priceValue = price.replaceAll("[^\\d.]", "");
        return new BigDecimal(priceValue);
    }
}
