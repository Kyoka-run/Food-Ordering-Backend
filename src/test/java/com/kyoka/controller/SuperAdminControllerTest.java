package com.kyoka.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kyoka.dto.UserDTO;
import com.kyoka.model.AppRole;
import com.kyoka.model.Role;
import com.kyoka.model.User;
import com.kyoka.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class SuperAdminControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private SuperAdminController superAdminController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(superAdminController).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void getAllCustomers_ShouldReturnListOfUsers() throws Exception {
        // Arrange
        List<UserDTO> userDTOs = Arrays.asList(
                createUserDTO(1L, "user1", "user1@example.com", Arrays.asList("ROLE_CUSTOMER")),
                createUserDTO(2L, "user2", "user2@example.com", Arrays.asList("ROLE_CUSTOMER"))
        );

        when(userService.findAllUsers()).thenReturn(userDTOs);

        // Act & Assert
        mockMvc.perform(get("/api/customers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].userId").value(1))
                .andExpect(jsonPath("$[0].username").value("user1"))
                .andExpect(jsonPath("$[0].email").value("user1@example.com"))
                .andExpect(jsonPath("$[1].userId").value(2))
                .andExpect(jsonPath("$[1].username").value("user2"))
                .andExpect(jsonPath("$[1].email").value("user2@example.com"));

        verify(userService, times(1)).findAllUsers();
    }

    @Test
    void getAllCustomers_WhenNoUsers_ShouldReturnEmptyList() throws Exception {
        // Arrange
        when(userService.findAllUsers()).thenReturn(List.of());

        // Act & Assert
        mockMvc.perform(get("/api/customers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));

        verify(userService, times(1)).findAllUsers();
    }

    private UserDTO createUserDTO(Long id, String username, String email, List<String> roles) {
        UserDTO dto = new UserDTO();
        dto.setUserId(id);
        dto.setUsername(username);
        dto.setEmail(email);
        dto.setRoles(roles);
        return dto;
    }
}