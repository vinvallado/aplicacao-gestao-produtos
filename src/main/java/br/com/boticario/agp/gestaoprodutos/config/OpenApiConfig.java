package br.com.boticario.agp.gestaoprodutos.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuração do SpringDoc OpenAPI para documentação da API.
 */
@Configuration
public class OpenApiConfig {

    /**
     * Configuração principal do OpenAPI.
     *
     * @return Configuração do OpenAPI
     */
    @Bean
    public OpenAPI customOpenAPI() {
        final String securitySchemeName = "bearerAuth";
        
        return new OpenAPI()
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName,
                                new SecurityScheme()
                                        .name(securitySchemeName)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")))
                .info(new Info()
                        .title("API de Gestão de Produtos")
                        .description("""
                                API REST para gerenciamento de produtos.
                                
                                ## Visão Geral
                                Esta API permite gerenciar produtos, incluindo operações de:
                                - Cadastro de novos produtos
                                - Consulta de produtos por diversos critérios
                                - Atualização de produtos existentes
                                - Exclusão de produtos
                                
                                ## Autenticação
                                A API utiliza autenticação JWT (JSON Web Token).
                                Para obter um token, faça login no endpoint `/api/auth/login`.
                                
                                ## Documentação
                                - **Swagger UI**: [/swagger-ui.html](http://localhost:8080/swagger-ui.html)
                                - **OpenAPI**: [/v3/api-docs](http://localhost:8080/v3/api-docs)
                                """)
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Equipe de Desenvolvimento")
                                .email("suporte@boticario.com.br"))
                        .license(new License()
                                .name("Licença Boticário")
                                .url("https://www.boticario.com.br/")));
    }

    /**
     * Configuração do grupo de documentação para a API v1.
     *
     * @return Configuração do grupo de documentação
     */
    @Bean
    public GroupedOpenApi v1Api() {
        return GroupedOpenApi.builder()
                .group("v1")
                .pathsToMatch("/api/v1/**")
                .build();
    }

    /**
     * Configuração do grupo de documentação para a API de autenticação.
     *
     * @return Configuração do grupo de documentação
     */
    @Bean
    public GroupedOpenApi authApi() {
        return GroupedOpenApi.builder()
                .group("autenticacao")
                .pathsToMatch("/api/auth/**")
                .build();
    }
}
