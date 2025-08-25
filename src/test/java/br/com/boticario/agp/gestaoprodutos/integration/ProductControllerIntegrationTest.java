import br.com.boticario.agp.gestaoprodutos.AplicacaoGestaoProdutosApplication;
import br.com.boticario.agp.gestaoprodutos.TestcontainersConfiguration;
import br.com.boticario.agp.gestaoprodutos.dto.ProductRequest;
import br.com.boticario.agp.gestaoprodutos.dto.request.LoginRequest;
import br.com.boticario.agp.gestaoprodutos.dto.request.RegisterRequest;
import br.com.boticario.agp.gestaoprodutos.model.Product;
import br.com.boticario.agp.gestaoprodutos.model.User;
import br.com.boticario.agp.gestaoprodutos.repository.ProductRepository;
import br.com.boticario.agp.gestaoprodutos.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = AplicacaoGestaoProdutosApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@ContextConfiguration(classes = TestcontainersConfiguration.class)
@Transactional // Garante que cada teste seja transacional e faça rollback
class ProductControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Product product1;
    private Product product2;
    private String jwtToken;

    @BeforeEach
    void setUp() throws Exception {
        productRepository.deleteAll(); // Limpa o banco antes de cada teste
        userRepository.deleteAll(); // Limpa usuários antes de cada teste

        // 1. Register a user
        RegisterRequest registerRequest = RegisterRequest.builder()
                .username("testuser")
                .fullName("Test User")
                .email("test@example.com")
                .password("password123")
                .build();

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk());

        // 2. Login to get JWT token
        LoginRequest loginRequest = LoginRequest.builder()
                .username("testuser")
                .password("password123")
                .build();

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        String responseString = result.getResponse().getContentAsString();
        this.jwtToken = objectMapper.readTree(responseString).get("access_token").asText();

        // Populate products after login setup
        product1 = Product.builder()
                .name("Product A")
                .type("Type X")
                .price(new BigDecimal("10.00"))
                .quantity(10)
                .industry("Industry 1")
                .origin("Origin 1")
                .build();

        product2 = Product.builder()
                .name("Product B")
                .type("Type Y")
                .price(new BigDecimal("20.00"))
                .quantity(20)
                .industry("Industry 2")
                .origin("Origin 2")
                .build();

        productRepository.saveAll(java.util.Arrays.asList(product1, product2));
    }

    // Testes para GET /api/v1/products
    @Test
    void searchProducts_shouldReturnAllProducts_whenNoParams() throws Exception {
        mockMvc.perform(get("/api/v1/products")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.totalElements", is(2)));
    }

    @Test
    void searchProducts_shouldReturnFilteredProducts_whenNameProvided() throws Exception {
        mockMvc.perform(get("/api/v1/products")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtToken)
                        .param("name", "Product A")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].name", is("Product A")));
    }

    @Test
    void searchProducts_shouldReturnFilteredProducts_whenPriceRangeProvided() throws Exception {
        mockMvc.perform(get("/api/v1/products")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtToken)
                        .param("minPrice", "15.00")
                        .param("maxPrice", "25.00")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].name", is("Product B")));
    }

    @Test
    void searchProducts_shouldReturnBadRequest_whenNoCriteriaProvided() throws Exception {
        mockMvc.perform(get("/api/v1/products")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtToken)
                        .param("name", "") // Empty name
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is("Pelo menos um critério de busca deve ser informado (nome ou faixa de preço)")));
    }

    @Test
    void searchProducts_shouldReturnPaginatedResults() throws Exception {
        // Adiciona mais produtos para testar paginação
        productRepository.save(Product.builder().name("Product C").type("Type Z").price(new BigDecimal("30.00")).quantity(30).build());
        productRepository.save(Product.builder().name("Product D").type("Type W").price(new BigDecimal("40.00")).quantity(40).build());

        mockMvc.perform(get("/api/v1/products")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtToken)
                        .param("page", "0")
                        .param("size", "2")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.totalElements", is(4)))
                .andExpect(jsonPath("$.totalPages", is(2)))
                .andExpect(jsonPath("$.pageNumber", is(0)));
    }

    // Testes para GET /api/v1/products/{id}
    @Test
    void getProductById_shouldReturnProduct_whenExists() throws Exception {
        mockMvc.perform(get("/api/v1/products/{id}", product1.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is(product1.getName())));
    }

    @Test
    void getProductById_shouldReturnNotFound_whenNotExists() throws Exception {
        mockMvc.perform(get("/api/v1/products/{id}", 999L)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", is("Produto não encontrado com id: '999'")));
    }

    // Testes para POST /api/v1/products
    @Test
    void createProduct_shouldCreateProduct_whenValid() throws Exception {
        ProductRequest newProductRequest = new ProductRequest();
        newProductRequest.setName("New Product");
        newProductRequest.setType("New Type");
        newProductRequest.setPrice(new BigDecimal("50.00"));
        newProductRequest.setQuantity(50);

        mockMvc.perform(post("/api/v1/products")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newProductRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name", is("New Product")));

        Optional<Product> createdProduct = productRepository.findByNameAndType("New Product", "New Type");
        assertTrue(createdProduct.isPresent());
    }

    @Test
    void createProduct_shouldReturnBadRequest_whenInvalidData() throws Exception {
        ProductRequest invalidProductRequest = new ProductRequest(); // Missing name, type, price

        mockMvc.perform(post("/api/v1/products")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidProductRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.name", is("O nome do produto é obrigatório")));
    }

    @Test
    void createProduct_shouldReturnConflict_whenDuplicate() throws Exception {
        ProductRequest duplicateProductRequest = new ProductRequest();
        duplicateProductRequest.setName(product1.getName());
        duplicateProductRequest.setType(product1.getType());
        duplicateProductRequest.setPrice(new BigDecimal("100.00"));
        duplicateProductRequest.setQuantity(1);

        mockMvc.perform(post("/api/v1/products")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicateProductRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message", is("Já existe um produto com o mesmo nome e tipo")));
    }

    // Testes para PUT /api/v1/products/{id}
    @Test
    void updateProduct_shouldUpdateProduct_whenValid() throws Exception {
        ProductRequest updatedProductRequest = new ProductRequest();
        updatedProductRequest.setName("Updated Product A");
        updatedProductRequest.setType("Updated Type X");
        updatedProductRequest.setPrice(new BigDecimal("15.00"));
        updatedProductRequest.setQuantity(15);

        mockMvc.perform(put("/api/v1/products/{id}", product1.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedProductRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Updated Product A")));

        Optional<Product> updatedProduct = productRepository.findById(product1.getId());
        assertTrue(updatedProduct.isPresent());
        assertEquals("Updated Product A", updatedProduct.get().getName());
    }

    @Test
    void updateProduct_shouldReturnNotFound_whenNotExists() throws Exception {
        ProductRequest updatedProductRequest = new ProductRequest();
        updatedProductRequest.setName("Non Existent");
        updatedProductRequest.setType("Type");
        updatedProductRequest.setPrice(new BigDecimal("1.00"));
        updatedProductRequest.setQuantity(1);

        mockMvc.perform(put("/api/v1/products/{id}", 999L)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedProductRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", is("Produto não encontrado com id: '999'")));
    }

    @Test
    void updateProduct_shouldReturnConflict_whenDuplicateExists() throws Exception {
        ProductRequest updatedProductRequest = new ProductRequest();
        updatedProductRequest.setName(product2.getName()); // Try to update product1 to be like product2
        updatedProductRequest.setType(product2.getType());
        updatedProductRequest.setPrice(new BigDecimal("1.00"));
        updatedProductRequest.setQuantity(1);

        mockMvc.perform(put("/api/v1/products/{id}", product1.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedProductRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message", is("Já existe outro produto com o mesmo nome e tipo")));
    }

    // Testes para DELETE /api/v1/products/{id}
    @Test
    void deleteProduct_shouldDeleteProduct_whenExists() throws Exception {
        mockMvc.perform(delete("/api/v1/products/{id}", product1.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        Optional<Product> deletedProduct = productRepository.findById(product1.getId());
        assertFalse(deletedProduct.isPresent());
    }

    @Test
    void deleteProduct_shouldReturnNotFound_whenNotExists() throws Exception {
        mockMvc.perform(delete("/api/v1/products/{id}", 999L)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", is("Produto não encontrado com id: '999'")));
    }
}
