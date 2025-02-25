package com.kyoka.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kyoka.dto.IngredientCategoryDTO;
import com.kyoka.dto.IngredientsItemDTO;
import com.kyoka.service.IngredientsService;
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
public class IngredientsControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private IngredientsService ingredientsService;

    @Autowired
    private ObjectMapper objectMapper;

    private IngredientCategoryDTO testCategoryDTO;
    private IngredientsItemDTO testItemDTO;

    @BeforeEach
    void setUp() {
        // Set up test ingredient category DTO
        testCategoryDTO = new IngredientCategoryDTO();
        testCategoryDTO.setIngredientCategoryId(1L);
        testCategoryDTO.setName("Test Category");
        testCategoryDTO.setRestaurantId(1L);

        // Set up test ingredients item DTO
        testItemDTO = new IngredientsItemDTO();
        testItemDTO.setIngredientsItemId(1L);
        testItemDTO.setName("Test Ingredient");
        testItemDTO.setIngredientCategoryId(1L);
        testItemDTO.setIngredientCategoryName("Test Category");
        testItemDTO.setRestaurantId(1L);
        testItemDTO.setInStock(true);
    }

    @Test
    @WithMockUser(roles = "RESTAURANT_OWNER")
    void createIngredientCategory_ShouldReturnCreatedCategory() throws Exception {
        when(ingredientsService.createIngredientsCategory(any(IngredientCategoryDTO.class))).thenReturn(testCategoryDTO);

        mockMvc.perform(post("/api/admin/ingredients/category")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testCategoryDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ingredientCategoryId", is(1)))
                .andExpect(jsonPath("$.name", is("Test Category")))
                .andExpect(jsonPath("$.restaurantId", is(1)));
    }

    @Test
    @WithMockUser(roles = "RESTAURANT_OWNER")
    void createIngredient_ShouldReturnCreatedIngredient() throws Exception {
        when(ingredientsService.createIngredientsItem(any(IngredientsItemDTO.class))).thenReturn(testItemDTO);

        mockMvc.perform(post("/api/admin/ingredients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testItemDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ingredientsItemId", is(1)))
                .andExpect(jsonPath("$.name", is("Test Ingredient")))
                .andExpect(jsonPath("$.inStock", is(true)));
    }

    @Test
    @WithMockUser(roles = "RESTAURANT_OWNER")
    void updateStock_ShouldReturnUpdatedIngredient() throws Exception {
        // Create a copy of the test item with updated stock status
        IngredientsItemDTO updatedItemDTO = new IngredientsItemDTO();
        updatedItemDTO.setIngredientsItemId(1L);
        updatedItemDTO.setName("Test Ingredient");
        updatedItemDTO.setIngredientCategoryId(1L);
        updatedItemDTO.setIngredientCategoryName("Test Category");
        updatedItemDTO.setRestaurantId(1L);
        updatedItemDTO.setInStock(false); // Changed from true to false

        when(ingredientsService.updateStock(anyLong())).thenReturn(updatedItemDTO);

        mockMvc.perform(put("/api/admin/ingredients/{id}/stock", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ingredientsItemId", is(1)))
                .andExpect(jsonPath("$.inStock", is(false)));
    }

    @Test
    @WithMockUser(roles = "RESTAURANT_OWNER")
    void getRestaurantsIngredients_ShouldReturnIngredientsList() throws Exception {
        List<IngredientsItemDTO> items = Arrays.asList(
                testItemDTO,
                createIngredientsItemDTO(2L, "Second Ingredient")
        );

        when(ingredientsService.findRestaurantsIngredientItems(anyLong())).thenReturn(items);

        mockMvc.perform(get("/api/admin/ingredients/restaurant/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name", is("Test Ingredient")))
                .andExpect(jsonPath("$[1].name", is("Second Ingredient")));
    }

    @Test
    @WithMockUser(roles = "RESTAURANT_OWNER")
    void getRestaurantsIngredientCategories_ShouldReturnCategoriesList() throws Exception {
        List<IngredientCategoryDTO> categories = Arrays.asList(
                testCategoryDTO,
                createIngredientCategoryDTO(2L, "Second Category")
        );

        when(ingredientsService.findIngredientsCategoryByRestaurantId(anyLong())).thenReturn(categories);

        mockMvc.perform(get("/api/admin/ingredients/restaurant/{id}/category", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name", is("Test Category")))
                .andExpect(jsonPath("$[1].name", is("Second Category")));
    }

    @Test
    @WithMockUser(roles = "RESTAURANT_OWNER")
    void updateIngredientCategory_ShouldReturnUpdatedCategory() throws Exception {
        // Create an updated category DTO
        IngredientCategoryDTO updatedCategoryDTO = new IngredientCategoryDTO();
        updatedCategoryDTO.setIngredientCategoryId(1L);
        updatedCategoryDTO.setName("Updated Category");
        updatedCategoryDTO.setRestaurantId(1L);

        when(ingredientsService.updateIngredientsCategory(anyLong(), any(IngredientCategoryDTO.class)))
                .thenReturn(updatedCategoryDTO);

        mockMvc.perform(put("/api/admin/ingredients/category/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedCategoryDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Updated Category")));
    }

    @Test
    @WithMockUser(roles = "RESTAURANT_OWNER")
    void updateIngredient_ShouldReturnUpdatedIngredient() throws Exception {
        // Create an updated ingredient DTO
        IngredientsItemDTO updatedItemDTO = new IngredientsItemDTO();
        updatedItemDTO.setIngredientsItemId(1L);
        updatedItemDTO.setName("Updated Ingredient");
        updatedItemDTO.setIngredientCategoryId(2L); // Changed category ID
        updatedItemDTO.setRestaurantId(1L);

        when(ingredientsService.updateIngredientsItem(anyLong(), any(IngredientsItemDTO.class)))
                .thenReturn(updatedItemDTO);

        mockMvc.perform(put("/api/admin/ingredients/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedItemDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Updated Ingredient")))
                .andExpect(jsonPath("$.ingredientCategoryId", is(2)));
    }

    private IngredientsItemDTO createIngredientsItemDTO(Long id, String name) {
        IngredientsItemDTO dto = new IngredientsItemDTO();
        dto.setIngredientsItemId(id);
        dto.setName(name);
        dto.setIngredientCategoryId(1L);
        dto.setIngredientCategoryName("Test Category");
        dto.setRestaurantId(1L);
        dto.setInStock(true);
        return dto;
    }

    private IngredientCategoryDTO createIngredientCategoryDTO(Long id, String name) {
        IngredientCategoryDTO dto = new IngredientCategoryDTO();
        dto.setIngredientCategoryId(id);
        dto.setName(name);
        dto.setRestaurantId(1L);
        return dto;
    }
}