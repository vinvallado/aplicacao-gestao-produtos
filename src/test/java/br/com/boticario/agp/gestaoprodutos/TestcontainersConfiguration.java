package br.com.boticario.agp.gestaoprodutos;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.utility.DockerImageName;

@Slf4j
@TestConfiguration(proxyBeanMethods = false)
public class TestcontainersConfiguration {

    private static final String POSTGRES_IMAGE = "postgres:15.3";
    private static PostgreSQLContainer<?> postgresContainer;

    @Bean
    @ServiceConnection
    public PostgreSQLContainer<?> postgresContainer() {
        if (postgresContainer == null) {
            postgresContainer = new PostgreSQLContainer<>(DockerImageName.parse(POSTGRES_IMAGE))
                    .withDatabaseName("testdb")
                    .withUsername("testuser")
                    .withPassword("testpass")
                    .withReuse(true);
            
            // Habilita logs do container
            postgresContainer.withLogConsumer(new Slf4jLogConsumer(log));
            
            log.info("Iniciando container PostgreSQL: {}", POSTGRES_IMAGE);
            postgresContainer.start();
        }
        return postgresContainer;
    }
}
