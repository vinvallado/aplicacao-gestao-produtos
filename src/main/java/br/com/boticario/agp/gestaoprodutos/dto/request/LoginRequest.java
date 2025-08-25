package br.com.boticario.agp.gestaoprodutos.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LoginRequest {
    @NotBlank(message = "O nome de usuário é obrigatório")
    private String username;
    
    @NotBlank(message = "A senha é obrigatória")
    private String password;
}
