
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

### Pré-requisitos
- Java 17+
- Maven 3.8+
- Docker e Docker Compose

### Opção 1: Usando Docker (Recomendado)

1.  **Construa a imagem Docker do projeto:**
    ```bash
    mvn clean package -DskipTests
    ```
2.  **Suba a aplicação e o banco de dados com Docker Compose:**
    ```bash
    docker-compose up --build
    ```
A aplicação estará disponível em `http://localhost:8080`.

### Opção 2: Localmente (Sem Docker)

1.  Inicie uma instância do PostgreSQL localmente.
2.  Crie um banco de dados chamado `boticario_products`.
3.  Configure as variáveis de ambiente ou altere o `application.properties` com suas credenciais.
4.  Execute a aplicação:
    ```bash
    mvn spring-boot:run
    ```

## API Endpoints

Todos os endpoints, exceto `/api/auth/login`, são protegidos e requerem um token JWT no header `Authorization: Bearer <token>`.

- **`POST /api/auth/login`**: Endpoint de autenticação para obter um token. Para esta PoC, ele aceita qualquer usuário e senha e retorna um token válido.
- **`POST /api/products`**: Insere um novo produto manualmente.
- **`GET /api/products`**: Consulta produtos com filtros e paginação.
  - **Parâmetros:**
    - `name` (opcional): Filtra por nome do produto (case-insensitive).
    - `minPrice` (opcional): Preço mínimo.
    - `maxPrice` (opcional): Preço máximo.
    - `page` (default: 0): Número da página.
    - `size` (default: 10): Tamanho da página.

