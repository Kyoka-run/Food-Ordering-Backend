package com.kyoka.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kyoka.model.AppRole;
import com.kyoka.model.Role;
import com.kyoka.model.User;
import com.kyoka.repository.CartRepository;
import com.kyoka.repository.RoleRepository;
import com.kyoka.repository.UserRepository;
import com.kyoka.security.UserDetailsImpl;
import com.kyoka.security.dto.LoginRequest;
import com.kyoka.security.dto.SignupRequest;
import com.kyoka.security.jwt.JwtUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class AuthControllerTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private CartRepository cartRepository;

    @Mock
    private PasswordEncoder encoder;

    @Mock
    private JwtUtils jwtUtils;

    @InjectMocks
    private AuthController authController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(authController).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void signin_WithValidCredentials_ShouldReturnToken() throws Exception {
        // Arrange
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("password");

        Authentication authentication = mock(Authentication.class);
        UserDetailsImpl userDetails = new UserDetailsImpl(
                1L, "testuser", "test@example.com", "password",
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_CUSTOMER"))
        );

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(jwtUtils.generateTokenFromUsername(any(UserDetailsImpl.class))).thenReturn("test.jwt.token");

        // Act & Assert
        mockMvc.perform(post("/api/auth/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jwtToken").value("test.jwt.token"))
                .andExpect(jsonPath("$.username").value("testuser"));

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    void signin_WithInvalidCredentials_ShouldReturnUnauthorized() throws Exception {
        // Arrange
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("wrongpassword");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new org.springframework.security.authentication.BadCredentialsException("Bad credentials"));

        // Act & Assert
        mockMvc.perform(post("/api/auth/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Bad credentials"));
    }

    @Test
    void signup_WithValidRequest_ShouldCreateUser() throws Exception {
        // Arrange
        SignupRequest signupRequest = new SignupRequest();
        signupRequest.setUsername("newuser");
        signupRequest.setEmail("newuser@example.com");
        signupRequest.setPassword("password");
        Set<String> roles = new HashSet<>();
        roles.add("customer");
        signupRequest.setRole(roles);

        Role customerRole = new Role(AppRole.ROLE_CUSTOMER);
        when(userRepository.existsByUserName(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(roleRepository.findByRoleName(any(AppRole.class))).thenReturn(Optional.of(customerRole));
        when(encoder.encode(anyString())).thenReturn("encodedPassword");

        // Act & Assert
        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("User registered successfully"));

        verify(userRepository).save(any(User.class));
        verify(cartRepository).save(any());
    }

    @Test
    void signup_WithExistingUsername_ShouldReturnError() throws Exception {
        // Arrange
        SignupRequest signupRequest = new SignupRequest();
        signupRequest.setUsername("existinguser");
        signupRequest.setEmail("new@example.com");
        signupRequest.setPassword("password");

        when(userRepository.existsByUserName("existinguser")).thenReturn(true);

        // Act & Assert
        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Error: Username is already taken!"));

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void signup_WithExistingEmail_ShouldReturnError() throws Exception {
        // Arrange
        SignupRequest signupRequest = new SignupRequest();
        signupRequest.setUsername("newuser");
        signupRequest.setEmail("existing@example.com");
        signupRequest.setPassword("password");

        when(userRepository.existsByUserName(anyString())).thenReturn(false);
        when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

        // Act & Assert
        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Error: Email is already in use!"));

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void currentUserProfile_ShouldReturnUserDetails() throws Exception {
        // Arrange
        Long userId = 1L;
        User user = new User();
        user.setUserId(userId);
        user.setUserName("testuser");
        user.setEmail("test@example.com");
        Set<Role> roles = new HashSet<>();
        roles.add(new Role(AppRole.ROLE_CUSTOMER));
        user.setRoles(roles);

        Authentication authentication = mock(Authentication.class);
        UserDetailsImpl userDetails = new UserDetailsImpl(
                userId, "testuser", "test@example.com", "password",
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_CUSTOMER"))
        );

        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // Act & Assert
        mockMvc.perform(get("/api/auth/profile")
                        .principal(authentication))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.email").value("test@example.com"));
    }

    @Test
    void signout_ShouldReturnSuccessMessage() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/auth/signout"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("You've been logged out successfully"));
    }
}