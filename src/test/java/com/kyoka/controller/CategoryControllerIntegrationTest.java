package com.kyoka.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kyoka.util.AuthUtil;
import com.kyoka.dto.CategoryDTO;
import com.kyoka.model.Restaurant;
import com.kyoka.model.User;
import com.kyoka.service.CategoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class CategoryControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CategoryService categoryService;

    @MockitoBean
    private AuthUtil authUtil;

    @Autowired
    private ObjectMapper objectMapper;

    private CategoryDTO testCategoryDTO;
    private User testUser;
    private Restaurant testRestaurant;

    @BeforeEach
    void setUp() {
        // Set up test user
        testUser = new User();
        testUser.setUserId(1L);
        testUser.setUserName("testUser");
        testUser.setEmail("test@example.com");

        // Set up test restaurant
        testRestaurant = new Restaurant();
        testRestaurant.setRestaurantId(1L);
        testRestaurant.setName("Test Restaurant");
        testRestaurant.setOwner(testUser);

        // Set up test category DTO
        testCategoryDTO = new CategoryDTO();
        testCategoryDTO.setCategoryId(1L);
        testCategoryDTO.setName("Test Category");
        testCategoryDTO.setRestaurantId(1L);
    }

    @Test
    @WithMockUser(roles = "RESTAURANT_OWNER")
    void createCategory_ShouldReturnCreatedCategory() throws Exception {
        when(categoryService.createCategory(any(CategoryDTO.class))).thenReturn(testCategoryDTO);
        when(authUtil.loggedInUser()).thenReturn(testUser);

        mockMvc.perform(post("/api/admin/category")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testCategoryDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.categoryId", is(1)))
                .andExpect(jsonPath("$.name", is("Test Category")))
                .andExpect(jsonPath("$.restaurantId", is(1)));
    }

    @Test
    @WithMockUser
    void getRestaurantCategories_ShouldReturnCategoriesList() throws Exception {
        List<CategoryDTO> categories = Arrays.asList(
                testCategoryDTO,
                createCategoryDTO(2L, "Desserts")
        );

        when(categoryService.findCategoryByRestaurantId(anyLong())).thenReturn(categories);

        mockMvc.perform(get("/api/category/restaurant/{restaurantId}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name", is("Test Category")))
                .andExpect(jsonPath("$[1].name", is("Desserts")));
    }

    @Test
    @WithMockUser(roles = "RESTAURANT_OWNER")
    void getCategoryById_ShouldReturnCategory() throws Exception {
        when(categoryService.findCategoryById(anyLong())).thenReturn(testCategoryDTO);

        mockMvc.perform(get("/api/admin/category/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.categoryId", is(1)))
                .andExpect(jsonPath("$.name", is("Test Category")));
    }

    @Test
    @WithMockUser(roles = "RESTAURANT_OWNER")
    void updateCategory_ShouldReturnUpdatedCategory() throws Exception {
        testCategoryDTO.setName("Updated Category");

        when(categoryService.updateCategory(any(CategoryDTO.class))).thenReturn(testCategoryDTO);

        mockMvc.perform(put("/api/admin/category/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testCategoryDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Updated Category")));
    }

    @Test
    @WithMockUser(roles = "RESTAURANT_OWNER")
    void deleteCategory_ShouldReturnSuccessResponse() throws Exception {
        String successMessage = "Category deleted successfully with id: 1";
        when(categoryService.deleteCategory(anyLong())).thenReturn(successMessage);

        mockMvc.perform(delete("/api/admin/category/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is(successMessage)))
                .andExpect(jsonPath("$.status", is(true)));
    }

    private CategoryDTO createCategoryDTO(Long id, String name) {
        CategoryDTO dto = new CategoryDTO();
        dto.setCategoryId(id);
        dto.setName(name);
        dto.setRestaurantId(1L);
        return dto;
    }
}