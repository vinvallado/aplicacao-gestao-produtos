
package br.com.boticario.project.pmp.domain;

/**
 * Exceção de negócio lançada quando se tenta criar um produto que já existe
 * com a mesma combinação de nome e tipo.
 */
public class ProductAlreadyExistsException extends RuntimeException {

    public ProductAlreadyExistsException(String product, String type) {
        super(String.format("Product with name '%s' and type '%s' already exists.", product, type));
    }
}
