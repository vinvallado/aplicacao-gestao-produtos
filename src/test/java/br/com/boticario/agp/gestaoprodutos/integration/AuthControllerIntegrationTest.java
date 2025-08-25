package br.com.boticario.agp.gestaoprodutos.integration;

import br.com.boticario.agp.gestaoprodutos.AplicacaoGestaoProdutosApplication;
import br.com.boticario.agp.gestaoprodutos.TestcontainersConfiguration;
import br.com.boticario.agp.gestaoprodutos.dto.request.LoginRequest;
import br.com.boticario.agp.gestaoprodutos.dto.request.RegisterRequest;
import br.com.boticario.agp.gestaoprodutos.model.User;
import br.com.boticario.agp.gestaoprodutos.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;

import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
@SpringBootTest(classes = AplicacaoGestaoProdutosApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@ContextConfiguration(classes = TestcontainersConfiguration.class)
@Transactional
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        log.info("Limpando o banco de dados antes do teste...");
        userRepository.deleteAll();
    }

    @Test
    void registerUser_shouldCreateUserAndReturnToken_whenValid() throws Exception {
        log.info("Testando registro de usuário com dados válidos...");
        
        RegisterRequest registerRequest = RegisterRequest.builder()
                .username("testuser")
                .fullName("Test User")
                .email("test@example.com")
                .password("Password123!") // Senha mais forte
                .build();

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andDo(result -> log.info("Resposta do registro: {}", result.getResponse().getContentAsString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.access_token", notNullValue()))
                .andExpect(jsonPath("$.username", is("testuser")));

        assertTrue(userRepository.findByUsername("testuser").isPresent(), 
                "O usuário deveria ter sido salvo no banco de dados");
        
        log.info("Teste de registro de usuário concluído com sucesso!");
    }

    @Test
    void registerUser_shouldReturnBadRequest_whenInvalidData() throws Exception {
        log.info("Testando registro com dados inválidos...");
        
        RegisterRequest registerRequest = RegisterRequest.builder()
                .username("us") // Muito curto
                .fullName("")
                .email("invalid-email")
                .password("123") // Muito curto
                .build();

        String requestBody = objectMapper.writeValueAsString(registerRequest);
        log.info("Request body: {}", requestBody);

        MvcResult result = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andDo(mvcResult -> {
                    log.info("Status: {}", mvcResult.getResponse().getStatus());
                    log.info("Response content: {}", mvcResult.getResponse().getContentAsString());
                    log.info("Response headers: {}", mvcResult.getResponse().getHeaderNames().stream()
                            .map(name -> name + "=" + mvcResult.getResponse().getHeader(name))
                            .collect(Collectors.joining(", ")));
                })
                .andExpect(status().isBadRequest())
                .andReturn();
                
        // Log the full response for debugging
        String responseContent = result.getResponse().getContentAsString();
        log.info("Full response content: {}", responseContent);
        
        // Parse the JSON to see its structure
        try {
            Object json = objectMapper.readValue(responseContent, Object.class);
            log.info("Parsed JSON: {}", objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(json));
        } catch (Exception e) {
            log.error("Failed to parse JSON response", e);
        }
        
        // Check the response structure
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").exists())
                .andExpect(jsonPath("$.errors").isMap())
                .andExpect(jsonPath("$.errors.username").exists())
                .andExpect(jsonPath("$.errors.email").exists())
                .andExpect(jsonPath("$.errors.password").exists());
                
        log.info("Teste de validação de dados inválidos concluído com sucesso!");
    }

    @Test
    void registerUser_shouldReturnConflict_whenUsernameAlreadyExists() throws Exception {
        log.info("Testando registro com nome de usuário já existente...");
        
        // Cria um usuário existente
        userRepository.save(User.builder()
                .username("existinguser")
                .fullName("Existing User")
                .email("existing@example.com")
                .password(passwordEncoder.encode("Password123!"))
                .build());

        RegisterRequest registerRequest = RegisterRequest.builder()
                .username("existinguser") // Nome de usuário já existe
                .fullName("Another User")
                .email("another@example.com")
                .password("Password123!")
                .build();

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andDo(result -> log.info("Resposta de conflito: {}", result.getResponse().getContentAsString()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message", is("Nome de usuário já está em uso")));
                
        log.info("Teste de conflito de nome de usuário concluído com sucesso!");
    }

    @Test
    void registerUser_shouldReturnConflict_whenEmailAlreadyExists() throws Exception {
        log.info("Testando registro com email já existente...");
        
        // Cria um usuário existente
        userRepository.save(User.builder()
                .username("anotheruser")
                .fullName("Another User")
                .email("existing@example.com")
                .password(passwordEncoder.encode("Password123!"))
                .build());

        RegisterRequest registerRequest = RegisterRequest.builder()
                .username("newuser")
                .fullName("New User")
                .email("existing@example.com") // Email já existe
                .password("Password123!")
                .build();

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andDo(result -> log.info("Resposta de conflito: {}", result.getResponse().getContentAsString()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message", is("Email já está em uso")));
                
        log.info("Teste de conflito de email concluído com sucesso!");
    }

    @Test
    void authenticateUser_shouldReturnToken_whenValidCredentials() throws Exception {
        log.info("Testando autenticação com credenciais válidas...");
        
        // Cria um usuário para teste
        userRepository.save(User.builder()
                .username("authuser")
                .fullName("Auth User")
                .email("auth@example.com")
                .password(passwordEncoder.encode("Password123!"))
                .build());

        LoginRequest loginRequest = LoginRequest.builder()
                .username("authuser")
                .password("Password123!")
                .build();

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andDo(result -> log.info("Resposta de login: {}", result.getResponse().getContentAsString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.access_token", notNullValue()))
                .andExpect(jsonPath("$.username", is("authuser")));
                
        log.info("Teste de autenticação bem-sucedida concluído!");
    }

    @Test
    void authenticateUser_shouldReturnUnauthorized_whenInvalidCredentials() throws Exception {
        log.info("Testando autenticação com credenciais inválidas...");
        
        // Cria um usuário para teste
        userRepository.save(User.builder()
                .username("authuser")
                .fullName("Auth User")
                .email("auth@example.com")
                .password(passwordEncoder.encode("Password123!"))
                .build());

        LoginRequest loginRequest = LoginRequest.builder()
                .username("authuser")
                .password("wrongpassword") // Senha incorreta
                .build();

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andDo(result -> log.info("Resposta de não autorizado: {}", result.getResponse().getContentAsString()))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message", is("Credenciais inválidas")));
                
        log.info("Teste de autenticação com credenciais inválidas concluído!");
    }
}
