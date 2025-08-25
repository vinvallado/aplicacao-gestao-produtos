package br.com.boticario.agp.gestaoprodutos.service;

import br.com.boticario.agp.gestaoprodutos.dto.request.LoginRequest;
import br.com.boticario.agp.gestaoprodutos.dto.request.RegisterRequest;
import br.com.boticario.agp.gestaoprodutos.dto.response.AuthResponse;
import br.com.boticario.agp.gestaoprodutos.exception.AuthenticationException;
import br.com.boticario.agp.gestaoprodutos.model.User;
import br.com.boticario.agp.gestaoprodutos.repository.UserRepository;
import br.com.boticario.agp.gestaoprodutos.security.jwt.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    private User user;
    private LoginRequest loginRequest;
    private RegisterRequest registerRequest;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .password("encodedPassword")
                .fullName("Test User")
                .enabled(true)
                .roles(Collections.singleton("USER"))
                .build();

        loginRequest = LoginRequest.builder()
                .username("testuser")
                .password("password")
                .build();

        registerRequest = RegisterRequest.builder()
                .username("newuser")
                .email("new@example.com")
                .password("newpassword")
                .fullName("New User")
                .roles(Collections.singletonList("USER"))
                .build();
    }

    @Test
    void authenticate_shouldReturnAuthResponse_whenCredentialsAreValid() {
        Authentication authentication = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user));
        when(jwtService.generateToken(any(User.class))).thenReturn("mockedJwtToken");
        when(jwtService.getExpirationTime()).thenReturn(3600000L);

        AuthResponse response = authService.authenticate(loginRequest);

        assertNotNull(response);
        assertEquals("mockedJwtToken", response.getAccessToken());
        assertEquals("testuser", response.getUsername());
        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository, times(2)).findByUsername(anyString());
        verify(jwtService, times(1)).generateToken(any(User.class));
    }

    @Test
    void authenticate_shouldThrowAuthenticationException_whenInvalidCredentials() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        AuthenticationException exception = assertThrows(AuthenticationException.class, () -> {
            authService.authenticate(loginRequest);
        });

        assertEquals("Credenciais inválidas", exception.getMessage());
        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository, times(1)).findByUsername(anyString()); // Called once before authentication fails
        verify(jwtService, never()).generateToken(any(User.class));
    }

    @Test
    void authenticate_shouldThrowAuthenticationException_whenUserNotFoundAfterAuthentication() {
        Authentication authentication = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());

        AuthenticationException exception = assertThrows(AuthenticationException.class, () -> {
            authService.authenticate(loginRequest);
        });

        assertEquals("Usuário não encontrado", exception.getMessage());
        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository, times(2)).findByUsername(anyString()); // Called twice
        verify(jwtService, never()).generateToken(any(User.class));
    }

    @Test
    void register_shouldCreateNewUserAndReturnAuthResponse() {
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedNewPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User savedUser = invocation.getArgument(0);
            savedUser.setId(1L); // Simulate ID being set by JPA
            return savedUser;
        });

        Authentication authentication = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(jwtService.generateToken(any(User.class))).thenReturn("mockedJwtToken");
        when(jwtService.getExpirationTime()).thenReturn(3600000L);

        AuthResponse response = authService.register(registerRequest);

        assertNotNull(response);
        assertEquals("mockedJwtToken", response.getAccessToken());
        assertEquals(registerRequest.getUsername(), response.getUsername()); // Assert with request username
        verify(userRepository, times(1)).existsByUsername(anyString());
        verify(userRepository, times(1)).existsByEmail(anyString());
        verify(passwordEncoder, times(1)).encode(anyString());
        verify(userRepository, times(1)).save(any(User.class));
        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtService, times(1)).generateToken(any(User.class));
    }

    @Test
    void register_shouldThrowAuthenticationException_whenUsernameAlreadyExists() {
        when(userRepository.existsByUsername(anyString())).thenReturn(true);

        AuthenticationException exception = assertThrows(AuthenticationException.class, () -> {
            authService.register(registerRequest);
        });

        assertEquals("Nome de usuário já está em uso", exception.getMessage());
        verify(userRepository, times(1)).existsByUsername(anyString());
        verify(userRepository, never()).existsByEmail(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void register_shouldThrowAuthenticationException_whenEmailAlreadyExists() {
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        AuthenticationException exception = assertThrows(AuthenticationException.class, () -> {
            authService.register(registerRequest);
        });

        assertEquals("Email já está em uso", exception.getMessage());
        verify(userRepository, times(1)).existsByUsername(anyString());
        verify(userRepository, times(1)).existsByEmail(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void register_shouldAssignDefaultRole_whenRolesAreNotProvided() {
        registerRequest.setRoles(null); // Set roles to null to test default assignment

        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedNewPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User savedUser = invocation.getArgument(0);
            savedUser.setId(1L); // Simulate ID being set by JPA
            return savedUser;
        });

        Authentication authentication = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(jwtService.generateToken(any(User.class))).thenReturn("mockedJwtToken");
        when(jwtService.getExpirationTime()).thenReturn(3600000L);

        AuthResponse response = authService.register(registerRequest);

        assertNotNull(response);
        assertTrue(response.getRoles().contains("USER"));
        verify(userRepository, times(1)).save(any(User.class));
    }
}
