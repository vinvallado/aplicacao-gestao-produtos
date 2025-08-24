
package br.com.boticario.project.pmp.application.usecases;

import br.com.boticario.project.pmp.application.ports.in.CreateProductUseCasePort;
import br.com.boticario.project.pmp.application.ports.out.ProductRepositoryPort;
import br.com.boticario.project.pmp.domain.Product;
import br.com.boticario.project.pmp.domain.ProductAlreadyExistsException;
import org.springframework.stereotype.Service;

@Service
public class CreateProductUseCase implements CreateProductUseCasePort {

    private final ProductRepositoryPort productRepositoryPort;

    public CreateProductUseCase(ProductRepositoryPort productRepositoryPort) {
        this.productRepositoryPort = productRepositoryPort;
    }

    @Override
    public Product createProduct(Product product) {
        // Regra de negócio: não permitir produtos com a mesma combinação de nome e tipo
        if (productRepositoryPort.existsByProductAndType(product.getProduct(), product.getType())) {
            throw new ProductAlreadyExistsException(product.getProduct(), product.getType());
        }
        return productRepositoryPort.save(product);
    }
}
