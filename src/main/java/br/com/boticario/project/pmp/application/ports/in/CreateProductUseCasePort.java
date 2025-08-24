
package br.com.boticario.project.pmp.application.ports.in;

import br.com.boticario.project.pmp.domain.Product;

/**
 * Porta de Entrada para o caso de uso de criação de produtos.
 */
public interface CreateProductUseCasePort {

    Product createProduct(Product product);

}
