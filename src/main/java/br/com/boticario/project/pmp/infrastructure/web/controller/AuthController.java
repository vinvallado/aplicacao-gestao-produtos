
package br.com.boticario.project.pmp.infrastructure.web.controller;

import br.com.boticario.project.pmp.infrastructure.security.JwtService;
import br.com.boticario.project.pmp.infrastructure.web.dto.LoginRequestDTO;
import br.com.boticario.project.pmp.infrastructure.web.dto.LoginResponseDTO;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    public AuthController(AuthenticationManager authenticationManager, JwtService jwtService, UserDetailsService userDetailsService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@RequestBody @Valid LoginRequestDTO request) {
        // Autentica o usuário
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        // Se a autenticação for bem-sucedida, carrega os detalhes do usuário e gera o token
        UserDetails userDetails = userDetailsService.loadUserByUsername(request.getUsername());
        String jwtToken = jwtService.generateToken(userDetails);

        return ResponseEntity.ok(new LoginResponseDTO(jwtToken));
    }
}
