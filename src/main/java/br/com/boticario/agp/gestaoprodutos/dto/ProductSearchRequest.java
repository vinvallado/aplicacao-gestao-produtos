package br.com.boticario.agp.gestaoprodutos.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO para receber os parâmetros de busca de produtos.
 * Pelo menos um dos campos deve ser preenchido.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductSearchRequest {
    
    private String name;
    
    @DecimalMin(value = "0.0", message = "O preço mínimo não pode ser negativo")
    @Digits(integer = 10, fraction = 2, message = "O preço deve ter no máximo 2 casas decimais")
    private BigDecimal minPrice;
    
    @DecimalMin(value = "0.0", message = "O preço máximo não pode ser negativo")
    @Digits(integer = 10, fraction = 2, message = "O preço deve ter no máximo 2 casas decimais")
    private BigDecimal maxPrice;
    
    /**
     * Checks if there are any search criteria specified.
     * 
     * @return true if any search criteria is specified, false otherwise
     */
    public boolean hasSearchCriteria() {
        return (name != null && !name.trim().isEmpty()) || 
               minPrice != null || 
               maxPrice != null;
    }
    
    /**
     * Creates a ProductSearchRequest with no criteria, which will return all products
     * @return a new ProductSearchRequest with no criteria
     */
    public static ProductSearchRequest allProducts() {
        return ProductSearchRequest.builder().build();
    }
}
