package br.com.boticario.agp.gestaoprodutos.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO para importação de produtos a partir de arquivos JSON.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductImportDto {
    private String product;  // Nome do produto
    private Integer quantity;
    private String price;    // String no formato "$X.XX"
    private String type;     // Tamanho/tipo (ex: XS, S, M, L, XL, 2XL, 3XL)
    private String industry; // Indústria/setor
    private String origin;   // Estado de origem (ex: SP, RJ, etc.)
}
