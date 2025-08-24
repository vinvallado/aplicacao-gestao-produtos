package br.com.boticario.agp.gestaoprodutos.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Entidade que representa um produto no sistema.
 * A combinação de 'name' e 'type' deve ser única no sistema.
 */

/**
 * Entidade que representa um produto no sistema.
 */
@Entity
@Table(name = "products",
        uniqueConstraints = @UniqueConstraint(columnNames = {"name", "type"}))
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 50)
    private String type;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(nullable = false)
    private Integer quantity;
    
    @Column(length = 100)
    private String industry;
    
    @Column(length = 50)
    private String origin;
}
