package br.com.boticario.aplicacaogestaoprodutos.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO para representar os dados de um produto durante a importação de arquivos JSON.
 * Utilizado para desserialização dos arquivos de dados.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductImportDto {
    private String name;
    private String type;
    private BigDecimal price;
    private Integer quantity;
}