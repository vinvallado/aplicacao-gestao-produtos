package br.com.boticario.agp.gestaoprodutos.exception;

/**
 * Exceção lançada quando um arquivo JSON não está no formato esperado.
 */
public class InvalidJsonFormatException extends RuntimeException {

    public InvalidJsonFormatException(String message) {
        super(message);
    }

    public InvalidJsonFormatException(String message, Throwable cause) {
        super(message, cause);
    }
}
