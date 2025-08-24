package br.com.boticario.agp.gestaoprodutos;

import org.springframework.boot.SpringApplication;

public class TestAplicacaoGestaoProdutosApplication {

	public static void main(String[] args) {
		SpringApplication.from(AplicacaoGestaoProdutosApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
