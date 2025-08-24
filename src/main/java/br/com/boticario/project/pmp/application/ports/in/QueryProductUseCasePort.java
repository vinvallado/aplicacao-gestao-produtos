
package br.com.boticario.project.pmp.application.ports.in;

import br.com.boticario.project.pmp.domain.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;

/**
 * Porta de Entrada para o caso de uso de consulta de produtos.
 */
public interface QueryProductUseCasePort {

    Page<Product> findByCriteria(String name, BigDecimal minPrice, BigDecimal maxPrice, String industry, String origin, Pageable pageable);

}
