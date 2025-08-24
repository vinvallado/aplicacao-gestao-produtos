
package br.com.boticario.project.pmp.application.usecases;

import br.com.boticario.project.pmp.application.ports.in.QueryProductUseCasePort;
import br.com.boticario.project.pmp.application.ports.out.ProductRepositoryPort;
import br.com.boticario.project.pmp.domain.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class QueryProductUseCase implements QueryProductUseCasePort {

    private final ProductRepositoryPort productRepositoryPort;

    public QueryProductUseCase(ProductRepositoryPort productRepositoryPort) {
        this.productRepositoryPort = productRepositoryPort;
    }

    @Override
    public Page<Product> findByCriteria(String name, BigDecimal minPrice, BigDecimal maxPrice, String industry, String origin, Pageable pageable) {
        // A lógica aqui é simples: apenas delegar para a porta de persistência.
        // Em casos de uso mais complexos, poderíamos ter mais orquestração,
        // como combinar dados de diferentes fontes, por exemplo.
        return productRepositoryPort.findByCriteria(name, minPrice, maxPrice, industry, origin, pageable);
    }
}
