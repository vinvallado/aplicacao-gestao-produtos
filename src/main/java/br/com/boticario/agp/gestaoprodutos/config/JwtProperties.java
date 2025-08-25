package br.com.boticario.agp.gestaoprodutos.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "jwt")
public record JwtProperties(
    String secret,
    long expirationMs,
    String issuer,
    String audience
) {}
