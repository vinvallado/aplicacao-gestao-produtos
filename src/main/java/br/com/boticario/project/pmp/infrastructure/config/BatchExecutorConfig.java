
package br.com.boticario.project.pmp.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
public class BatchExecutorConfig {

    @Bean(name = "batchExecutorService")
    public ExecutorService batchExecutorService() {
        // Define um pool de threads fixo para o processamento em batch.
        // O número de threads pode ser ajustado conforme a necessidade e recursos do sistema.
        return Executors.newFixedThreadPool(4);
    }
}
