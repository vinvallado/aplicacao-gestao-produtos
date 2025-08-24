package br.com.boticario.agp.gestaoprodutos.exception;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Manipulador global de exceções para padronizar as respostas de erro da API.
 */
@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Object> handleResourceNotFoundException(ResourceNotFoundException ex, WebRequest request) {
        log.error("Recurso não encontrado: {}", ex.getMessage());
        return buildErrorResponse(ex, HttpStatus.NOT_FOUND, request);
    }

    @ExceptionHandler(InvalidJsonFormatException.class)
    public ResponseEntity<Object> handleInvalidJsonFormatException(InvalidJsonFormatException ex, WebRequest request) {
        log.error("Formato JSON inválido: {}", ex.getMessage());
        return buildErrorResponse(ex, HttpStatus.BAD_REQUEST, request);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Object> handleDataIntegrityViolation(DataIntegrityViolationException ex, WebRequest request) {
        log.error("Violação de integridade de dados: {}", ex.getMessage());
        String errorMessage = "Erro de integridade de dados. Verifique os dados fornecidos.";
        
        if (ex.getCause() != null && ex.getCause().getMessage() != null) {
            if (ex.getCause().getMessage().contains("duplicate key")) {
                errorMessage = "Já existe um produto com o mesmo nome e tipo.";
            }
        }
        
        return buildErrorResponse(new RuntimeException(errorMessage), HttpStatus.CONFLICT, request);
    }

    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpHeaders headers, 
            HttpStatus status, WebRequest request) {
        
        Map<String, String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        fieldError -> fieldError.getDefaultMessage() != null ? 
                                fieldError.getDefaultMessage() : "Erro de validação"
                ));
        
        log.error("Erro de validação: {}", errors);
        
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", status.value());
        body.put("error", "Erro de validação");
        body.put("message", "Erro ao validar os campos da requisição");
        body.put("errors", errors);
        
        return new ResponseEntity<>(body, headers, status);
    }

    protected ResponseEntity<Object> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex, HttpHeaders headers, 
            HttpStatus status, WebRequest request) {
        
        log.error("Erro ao ler a requisição: {}", ex.getMessage());
        String error = "Requisição JSON malformada";
        return buildErrorResponse(new RuntimeException(error), status, request);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Object> handleConstraintViolation(
            ConstraintViolationException ex, WebRequest request) {
        
        log.error("Violação de restrição: {}", ex.getMessage());
        String error = "Erro de validação nos parâmetros da requisição";
        return buildErrorResponse(new RuntimeException(error), HttpStatus.BAD_REQUEST, request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleAllUncaughtException(
            Exception ex, WebRequest request) {
        
        log.error("Erro não tratado: {}", ex.getMessage(), ex);
        String error = "Ocorreu um erro inesperado no servidor";
        return buildErrorResponse(new RuntimeException(error), 
                HttpStatus.INTERNAL_SERVER_ERROR, request);
    }

    private ResponseEntity<Object> buildErrorResponse(
            Exception exception, HttpStatus status, WebRequest request) {
        
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", exception.getMessage());
        body.put("path", request.getDescription(false).replace("uri=", ""));
        
        return new ResponseEntity<>(body, status);
    }
}
