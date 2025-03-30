package com.kyoka.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kyoka.dto.UserDTO;
import com.kyoka.model.AppRole;
import com.kyoka.model.Role;
import com.kyoka.model.User;
import com.kyoka.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.*;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class SuperAdminControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    private List<UserDTO> testUserDTOs;

    @BeforeEach
    void setUp() {
        // Create test user DTOs
        UserDTO customer = new UserDTO();
        customer.setUserId(1L);
        customer.setUsername("customer");
        customer.setEmail("customer@example.com");
        customer.setRoles(Arrays.asList("ROLE_CUSTOMER"));

        UserDTO owner = new UserDTO();
        owner.setUserId(2L);
        owner.setUsername("owner");
        owner.setEmail("owner@example.com");
        owner.setRoles(Arrays.asList("ROLE_RESTAURANT_OWNER"));

        testUserDTOs = Arrays.asList(customer, owner);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllCustomers_ShouldReturnUsersList() throws Exception {
        when(userService.findAllUsers()).thenReturn(testUserDTOs);

        mockMvc.perform(get("/api/customers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].username", is("customer")))
                .andExpect(jsonPath("$[1].username", is("owner")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllCustomers_WhenNoUsers_ShouldReturnEmptyList() throws Exception {
        when(userService.findAllUsers()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/customers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    // Test for access control - regular users shouldn't be able to access admin endpoints
    @Test
    @WithMockUser(roles = "CUSTOMER")
    void getAllCustomers_WithRegularUser_ShouldReturnForbidden() throws Exception {
        mockMvc.perform(get("/api/customers"))
                .andExpect(status().isForbidden());
    }
}