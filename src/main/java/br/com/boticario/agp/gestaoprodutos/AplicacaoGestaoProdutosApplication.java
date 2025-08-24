package br.com.boticario.agp.gestaoprodutos;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "br.com.boticario.agp.gestaoprodutos")
public class AplicacaoGestaoProdutosApplication {

	public static void main(String[] args) {
		SpringApplication.run(AplicacaoGestaoProdutosApplication.class, args);
	}

}
