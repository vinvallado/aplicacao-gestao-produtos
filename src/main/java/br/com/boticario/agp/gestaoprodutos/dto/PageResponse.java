package br.com.boticario.agp.gestaoprodutos.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Classe genérica para representar uma resposta paginada da API.
 *
 * @param <T> O tipo dos itens na página
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageResponse<T> {
    
    private List<T> content;
    private int pageNumber;
    private int pageSize;
    private long totalElements;
    private int totalPages;
    private boolean last;
    
    /**
     * Cria uma resposta paginada a partir de um objeto Page do Spring Data.
     *
     * @param page O objeto Page do Spring Data
     * @param content A lista de itens já convertidos para o tipo de saída
     * @param <T> O tipo dos itens na página
     * @return Uma nova instância de PageResponse
     */
    public static <T> PageResponse<T> fromPage(Page<?> page, List<T> content) {
        return PageResponse.<T>builder()
                .content(content)
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .build();
    }
}
