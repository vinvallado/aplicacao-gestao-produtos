package br.com.boticario.agp.gestaoprodutos;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import br.com.boticario.agp.gestaoprodutos.config.JwtProperties;

@SpringBootApplication(scanBasePackages = "br.com.boticario.agp.gestaoprodutos")
@EnableConfigurationProperties(JwtProperties.class)
public class AplicacaoGestaoProdutosApplication {

	public static void main(String[] args) {
		SpringApplication.run(AplicacaoGestaoProdutosApplication.class, args);
	}

}
