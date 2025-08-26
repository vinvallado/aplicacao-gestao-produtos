# Product Management PoC - Boticário

Prova de Conceito desenvolvida para o processo seletivo de Pessoa Desenvolvedora Backend Java III na Boticário.

## Core Concepts & Architecture

Este projeto foi desenvolvido utilizando **Java 17** e **Spring Boot 3**. A arquitetura escolhida foi a **Arquitetura Limpa (Hexagonal)**.

**Justificativa Arquitetural:** A escolha pela Arquitetura Limpa visa isolar o núcleo de regras de negócio da aplicação de detalhes de infraestrutura (frameworks, bancos de dados). Isso resulta em um sistema:
- **Altamente Testável:** A lógica de negócio pode ser testada de forma unitária, sem a necessidade de subir o contexto da aplicação.
- **Manutenível e Evoluível:** A troca de um componente de infraestrutura (ex: trocar a API REST por um consumidor de filas) tem impacto mínimo no núcleo do sistema.
- **Independente de Frameworks:** O coração da aplicação não depende do Spring, apenas o utiliza como um "plugin" na camada de infraestrutura.

## Tech Stack

- **Java 17** & **Spring Boot 3**
- **Maven:** Gerenciador de dependências
- **PostgreSQL:** Banco de dados relacional
- **Docker** & **Docker Compose:** Orquestração de contêineres
- **Spring Security & JWT:** Segurança da API
- **Spring Data JPA:** Persistência de dados
- **Testcontainers:** Testes de integração
- **JUnit 5, Mockito, AssertJ:** Testes unitários e de integração

## Como Executar a Aplicação

**A maneira mais simples e recomendada de executar a aplicação é usando Docker.**

**Pré-requisitos:**
- Docker e Docker Compose

**Instruções:**

1.  Clone o repositório para a sua máquina local.
2.  Navegue até o diretório raiz do projeto.
3.  Execute o seguinte comando:

    ```bash
    docker-compose up --build
    ```

A aplicação será construída e iniciada, juntamente com uma instância do banco de dados PostgreSQL. A API estará disponível em `http://localhost:8080`.

**Observação sobre o Banco de Dados:** A configuração do `docker-compose` foi ajustada para não utilizar um volume persistente para o banco de dados. Isso garante que, a cada vez que o comando `docker-compose up` for executado, um banco de dados limpo seja criado e as migrações do Flyway sejam aplicadas corretamente, evitando erros de checksum.

## API Endpoints

Todos os endpoints, exceto `/api/auth/login`, são protegidos e requerem um token JWT no header `Authorization: Bearer <token>`.

- **`POST /api/auth/register`**: Registra um novo usuário no sistema.
- **`POST /api/auth/login`**: Endpoint de autenticação para obter um token. Para esta PoC, ele aceita qualquer usuário e senha e retorna um token válido.
- **`POST /api/v1/products`**: Insere um novo produto manualmente.
- **`GET /api/v1/products`**: Consulta produtos com filtros e paginação.
  - **Parâmetros:**
    - `name` (opcional): Filtra por nome do produto (case-insensitive).
    - `minPrice` (opcional): Preço mínimo.
    - `maxPrice` (opcional): Preço máximo.
    - `page` (default: 0): Número da página.
    - `size` (default: 10): Tamanho da página.
- **`GET /api/v1/products/{id}`**: Busca um produto específico pelo seu ID.
- **`PUT /api/v1/products/{id}`**: Atualiza os dados de um produto existente.
- **`DELETE /api/v1/products/{id}`**: Remove um produto do sistema.

## Testes

O projeto possui uma suíte abrangente de testes unitários e de integração para garantir a qualidade e o funcionamento correto da aplicação.

### Como Executar os Testes

Para executar todos os testes (unitários e de integração), utilize o seguinte comando Maven:

```bash
mvn clean test
```

**Observação:** Os testes de integração utilizam [Testcontainers](https://www.testcontainers.org/) para provisionar um banco de dados PostgreSQL em um contêiner Docker, garantindo um ambiente de teste isolado e consistente. Certifique-se de que o Docker esteja em execução.

## Coleção Postman

Uma coleção Postman foi criada para facilitar o teste manual dos endpoints da API.

### Como Importar a Coleção Postman

1.  Abra o Postman.
2.  Clique em "Import" no canto superior esquerdo.
3.  Selecione a aba "File" e escolha o arquivo `SGT-Product-Management.postman_collection.json` localizado na raiz do projeto.
4.  Após a importação, você encontrará a coleção "SGT - Product Management" na sua lista de coleções.

### Como Usar a Coleção Postman

1.  **Defina a `baseUrl`:** A coleção possui uma variável de ambiente `baseUrl`. Por padrão, ela está configurada para `http://localhost:8080`. Se sua aplicação estiver rodando em uma porta diferente, atualize esta variável.
2.  **Obtenha um Token JWT:**
    *   Primeiro, execute a requisição `Register User` para criar um novo usuário (se ainda não tiver um).
    *   Em seguida, execute a requisição `Login User`. Esta requisição possui um script de pós-execução que automaticamente extrai o `access_token` da resposta e o armazena na variável de ambiente `jwtToken`.
3.  **Use o Token JWT:** Todas as requisições de gerenciamento de produtos (Create, Get All, Get by ID, Update, Delete) estão configuradas para usar a variável `jwtToken` no cabeçalho `Authorization: Bearer {{jwtToken}}`. Após fazer login, as requisições subsequentes usarão o token automaticamente.

## Cobertura de Testes

O objetivo é alcançar uma alta cobertura de testes, garantindo que a maioria das linhas de código e branches sejam exercidas pelos testes. Ferramentas como JaCoCo podem ser integradas ao Maven para gerar relatórios de cobertura.

Para gerar um relatório de cobertura de testes (requer o plugin JaCoCo configurado no `pom.xml`):

```bash
mvn clean verify
```

Após a execução, o relatório estará disponível em `target/site/jacoco/index.html`.

## Conclusão

O projeto foi corrigido e aprimorado para atender aos requisitos do desafio. As seguintes ações foram tomadas:

- **Correção de Erros de Compilação:** A classe principal da aplicação foi criada, resolvendo os erros de compilação nos testes.
- **Correção de Warnings:** Warnings relacionados ao MapStruct, dependências duplicadas e Lombok foram corrigidos.
- **Correção do Flyway:** O problema de checksum do Flyway foi resolvido ajustando a configuração do Docker Compose para garantir um banco de dados limpo a cada execução.
- **Melhora da Documentação:** O `README.md` foi atualizado com instruções claras e simplificadas para executar a aplicação.

O projeto está agora em um estado estável, com testes passando e pronto para ser avaliado.