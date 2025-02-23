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

    @Mock
    private ModelMapper modelMapper;

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
        List<User> users = Arrays.asList(
                createUser(1L, "user1", "user1@example.com", "ROLE_CUSTOMER"),
                createUser(2L, "user2", "user2@example.com", "ROLE_CUSTOMER")
        );

        List<UserDTO> userDTOs = Arrays.asList(
                createUserDTO(1L, "user1", "user1@example.com", Arrays.asList("ROLE_CUSTOMER")),
                createUserDTO(2L, "user2", "user2@example.com", Arrays.asList("ROLE_CUSTOMER"))
        );

        when(userService.findAllUsers()).thenReturn(users);
        when(modelMapper.map(any(User.class), eq(UserDTO.class)))
                .thenReturn(userDTOs.get(0), userDTOs.get(1));

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
        verify(modelMapper, times(2)).map(any(User.class), eq(UserDTO.class));
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
        verify(modelMapper, never()).map(any(), any());
    }

    private User createUser(Long id, String username, String email, String roleName) {
        User user = new User();
        user.setUserId(id);
        user.setUserName(username);
        user.setEmail(email);

        Set<Role> roles = new HashSet<>();
        Role role = new Role();
        role.setRoleName(AppRole.valueOf(roleName));
        roles.add(role);
        user.setRoles(roles);

        return user;
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