package br.com.boticario.agp.gestaoprodutos.service;

import br.com.boticario.agp.gestaoprodutos.dto.request.LoginRequest;
import br.com.boticario.agp.gestaoprodutos.dto.request.RegisterRequest;
import br.com.boticario.agp.gestaoprodutos.dto.response.AuthResponse;
import br.com.boticario.agp.gestaoprodutos.exception.AuthenticationException;
import br.com.boticario.agp.gestaoprodutos.exception.ResourceAlreadyExistsException;
import br.com.boticario.agp.gestaoprodutos.model.User;
import br.com.boticario.agp.gestaoprodutos.repository.UserRepository;
import br.com.boticario.agp.gestaoprodutos.security.jwt.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Transactional
    public AuthResponse authenticate(LoginRequest request) {
        try {
            log.info("Tentativa de autenticação para o usuário: {}", request.getUsername());
            
            // Log para verificar se o usuário existe no banco de dados
            userRepository.findByUsername(request.getUsername())
                .ifPresentOrElse(
                    user -> log.info("Usuário encontrado no banco de dados: {}", user.getUsername()),
                    () -> log.warn("Usuário não encontrado no banco de dados: {}", request.getUsername())
                );
            
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(),
                            request.getPassword()
                    )
            );
            
            log.info("Autenticação bem-sucedida para o usuário: {}", request.getUsername());
            SecurityContextHolder.getContext().setAuthentication(authentication);
            
            User user = userRepository.findByUsername(request.getUsername())
                    .orElseThrow(() -> {
                        log.error("Usuário autenticado não encontrado no banco de dados: {}", request.getUsername());
                        return new AuthenticationException("Usuário não encontrado");
                    });
            
            log.info("Gerando token JWT para o usuário: {}", user.getUsername());
            String jwt = jwtService.generateToken(user);
            
            log.info("Autenticação concluída com sucesso para o usuário: {}", user.getUsername());
            return AuthResponse.builder()
                    .accessToken(jwt)
                    .expiresIn(jwtService.getExpirationTime() / 1000)
                    .username(user.getUsername())
                    .email(user.getEmail())
                    .roles(user.getRoles().stream().toList())
                    .build();
                    
        } catch (org.springframework.security.core.AuthenticationException e) {
            log.error("Falha na autenticação para o usuário: {}", request.getUsername(), e);
            throw new AuthenticationException("Credenciais inválidas");
        }
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        // Verificar se o usuário já existe
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new ResourceAlreadyExistsException("Nome de usuário já está em uso");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ResourceAlreadyExistsException("Email já está em uso");
        }

        // Criar novo usuário
        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .email(request.getEmail())
                .enabled(true)
                .build();

        // Definir roles padrão se não for especificado
        if (request.getRoles() == null || request.getRoles().isEmpty()) {
            user.addRole("USER");
        } else {
            request.getRoles().forEach(user::addRole);
        }

        // Salvar usuário
        user = userRepository.save(user);

        // Autenticar o usuário
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        
        // Gerar token JWT
        String jwt = jwtService.generateToken(user);

        return AuthResponse.builder()
                .accessToken(jwt)
                .expiresIn(jwtService.getExpirationTime() / 1000)
                .username(user.getUsername())
                .email(user.getEmail())
                .roles(user.getRoles().stream().toList())
                .build();
    }
}
