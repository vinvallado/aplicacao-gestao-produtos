package br.com.boticario.agp.gestaoprodutos.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

/**
 * DTO para receber os dados de um novo produto na requisição.
 */
@Data
public class ProductRequest {
    
    @NotBlank(message = "O nome do produto é obrigatório")
    @Size(max = 100, message = "O nome do produto deve ter no máximo 100 caracteres")
    private String name;
    
    @NotBlank(message = "O tipo do produto é obrigatório")
    @Size(max = 50, message = "O tipo do produto deve ter no máximo 50 caracteres")
    private String type;
    
    @NotNull(message = "O preço é obrigatório")
    @DecimalMin(value = "0.0", inclusive = false, message = "O preço deve ser maior que zero")
    private BigDecimal price;
    
    @NotNull(message = "A quantidade é obrigatória")
    @PositiveOrZero(message = "A quantidade não pode ser negativa")
    private Integer quantity;
    
    @Size(max = 100, message = "A indústria deve ter no máximo 100 caracteres")
    private String industry;
    
    @Size(max = 50, message = "A origem deve ter no máximo 50 caracteres")
    private String origin;
    
    /**
     * Retorna a indústria ou uma string vazia se for nula.
     */
    public String getIndustry() {
        return industry != null ? industry : "";
    }
    
    /**
     * Retorna a origem ou uma string vazia se for nula.
     */
    public String getOrigin() {
        return origin != null ? origin : "";
    }
}
