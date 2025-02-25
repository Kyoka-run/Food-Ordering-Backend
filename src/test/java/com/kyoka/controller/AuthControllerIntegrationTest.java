package com.kyoka.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kyoka.dto.UserDTO;
import com.kyoka.model.AppRole;
import com.kyoka.model.Cart;
import com.kyoka.model.Role;
import com.kyoka.model.User;
import com.kyoka.repository.CartRepository;
import com.kyoka.repository.RoleRepository;
import com.kyoka.repository.UserRepository;
import com.kyoka.security.UserDetailsImpl;
import com.kyoka.security.dto.LoginRequest;
import com.kyoka.security.dto.LoginResponse;
import com.kyoka.security.dto.MessageResponse;
import com.kyoka.security.dto.SignupRequest;
import com.kyoka.security.jwt.JwtUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.*;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthenticationManager authenticationManager;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private RoleRepository roleRepository;

    @MockitoBean
    private CartRepository cartRepository;

    @MockitoBean
    private JwtUtils jwtUtils;

    @Autowired
    private ObjectMapper objectMapper;

    private User testUser;
    private Role customerRole;
    private Role ownerRole;

    @BeforeEach
    void setUp() {
        // Set up test roles
        customerRole = new Role();
        customerRole.setRoleId(1);
        customerRole.setRoleName(AppRole.ROLE_CUSTOMER);

        ownerRole = new Role();
        ownerRole.setRoleId(2);
        ownerRole.setRoleName(AppRole.ROLE_RESTAURANT_OWNER);

        // Set up test user
        testUser = new User();
        testUser.setUserId(1L);
        testUser.setUserName("testUser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("$2a$10$8HFL8xdp0UDK1xY1v3fXb.UvHoK1xIuQbNNk8AzVOY5NqwMnHkIsW"); // encoded "password"

        Set<Role> roles = new HashSet<>();
        roles.add(customerRole);
        testUser.setRoles(roles);
    }

    @Test
    void signin_WithValidCredentials_ShouldReturnToken() throws Exception {
        // Arrange
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("testUser");
        loginRequest.setPassword("password");

        Authentication authentication = mock(Authentication.class);
        UserDetailsImpl userDetails = new UserDetailsImpl(
                1L, "testUser", "test@example.com", "password",
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
                .andExpect(jsonPath("$.jwtToken", is("test.jwt.token")))
                .andExpect(jsonPath("$.username", is("testUser")))
                .andExpect(jsonPath("$.roles", hasSize(1)))
                .andExpect(jsonPath("$.roles[0]", is("ROLE_CUSTOMER")));
    }

    @Test
    void signin_WithInvalidCredentials_ShouldReturnUnauthorized() throws Exception {
        // Arrange
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("testUser");
        loginRequest.setPassword("wrongpassword");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new org.springframework.security.authentication.BadCredentialsException("Bad credentials"));

        // Act & Assert
        mockMvc.perform(post("/api/auth/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", is("Bad credentials")));
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

        when(userRepository.existsByUserName(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(roleRepository.findByRoleName(AppRole.ROLE_CUSTOMER)).thenReturn(Optional.of(customerRole));
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(cartRepository.save(any(Cart.class))).thenReturn(new Cart());

        // Act & Assert
        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("User registered successfully")));
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
                .andExpect(jsonPath("$.message", is("Error: Username is already taken!")));
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
                .andExpect(jsonPath("$.message", is("Error: Email is already in use!")));
    }

    @Test
    void signout_ShouldReturnSuccessMessage() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/auth/signout"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("You've been logged out successfully")));
    }

    @Test
    void signup_WithMultipleRoles_ShouldCreateUserWithAllRoles() throws Exception {
        // Arrange
        SignupRequest signupRequest = new SignupRequest();
        signupRequest.setUsername("multiuser");
        signupRequest.setEmail("multi@example.com");
        signupRequest.setPassword("password");
        Set<String> roles = new HashSet<>();
        roles.add("customer");
        roles.add("owner");
        signupRequest.setRole(roles);

        when(userRepository.existsByUserName(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(roleRepository.findByRoleName(AppRole.ROLE_CUSTOMER)).thenReturn(Optional.of(customerRole));
        when(roleRepository.findByRoleName(AppRole.ROLE_RESTAURANT_OWNER)).thenReturn(Optional.of(ownerRole));
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(cartRepository.save(any(Cart.class))).thenReturn(new Cart());

        // Act & Assert
        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("User registered successfully")));
    }
}