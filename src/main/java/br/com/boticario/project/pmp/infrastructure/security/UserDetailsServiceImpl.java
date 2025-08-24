package br.com.boticario.project.pmp.infrastructure.security;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    // Não há dependências injetadas via construtor aqui, então não precisamos de um construtor explícito
    // para injeção. O construtor padrão é suficiente.

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Para fins de PoC, um usuário hardcoded. Em uma aplicação real, buscaria do banco de dados.
        if ("admin".equals(username)) {
            // Senha "password" encodada com BCrypt
            return new User("admin", new BCryptPasswordEncoder().encode("password"), new ArrayList<>());
        }
        throw new UsernameNotFoundException("User not found with username: " + username);
    }
}