package com.kyoka.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
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

    private List<User> testUsers;

    @BeforeEach
    void setUp() {
        // Create roles
        Set<Role> customerRoles = new HashSet<>();
        Role customerRole = new Role();
        customerRole.setRoleId(1);
        customerRole.setRoleName(AppRole.ROLE_CUSTOMER);
        customerRoles.add(customerRole);

        Set<Role> ownerRoles = new HashSet<>();
        Role ownerRole = new Role();
        ownerRole.setRoleId(2);
        ownerRole.setRoleName(AppRole.ROLE_RESTAURANT_OWNER);
        ownerRoles.add(ownerRole);

        // Create test users
        User customer = new User();
        customer.setUserId(1L);
        customer.setUserName("customer");
        customer.setEmail("customer@example.com");
        customer.setRoles(customerRoles);

        User owner = new User();
        owner.setUserId(2L);
        owner.setUserName("owner");
        owner.setEmail("owner@example.com");
        owner.setRoles(ownerRoles);

        testUsers = Arrays.asList(customer, owner);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllCustomers_ShouldReturnUsersList() throws Exception {
        when(userService.findAllUsers()).thenReturn(testUsers);

        // Since we're testing model mapper in a complex scenario, we'll mock at a different level
        // We need to keep the controller's ModelMapper autowiring and instead mock the findAllUsers() method
        // to return a pre-mapped DTO list or have the actual model mapping happen.

        // This works because the real ModelMapper will map our test Users to UserDTOs

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