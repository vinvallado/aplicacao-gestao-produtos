package br.com.boticario.agp.gestaoprodutos.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableJpaRepositories(basePackages = "br.com.boticario.agp.gestaoprodutos.repository")
@EnableTransactionManagement
public class JpaConfig {
    // Configuração adicional do JPA pode ser adicionada aqui, se necessário
}
