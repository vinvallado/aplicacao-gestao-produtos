package br.com.boticario.aplicacaogestaoprodutos.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * Configuração para processamento assíncrono.
 * Define um pool de threads para execução paralela de tarefas.
 */
@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        // Número de threads disponíveis no pool
        executor.setCorePoolSize(Runtime.getRuntime().availableProcessors());
        // Número máximo de threads
        executor.setMaxPoolSize(Runtime.getRuntime().availableProcessors() * 2);
        // Capacidade da fila de tarefas
        executor.setQueueCapacity(500);
        // Prefixo para identificação das threads
        executor.setThreadNamePrefix("JsonProcessor-");
        // Inicializa o executor
        executor.initialize();
        return executor;
    }
}