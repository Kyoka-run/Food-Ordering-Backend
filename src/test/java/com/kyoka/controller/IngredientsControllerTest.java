package com.kyoka.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kyoka.dto.IngredientCategoryDTO;
import com.kyoka.dto.IngredientsItemDTO;
import com.kyoka.service.IngredientsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class IngredientsControllerTest {

    @Mock
    private IngredientsService ingredientsService;

    @InjectMocks
    private IngredientsController ingredientsController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(ingredientsController).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void createIngredientCategory_ShouldReturnCreatedCategory() throws Exception {
        // Arrange
        IngredientCategoryDTO requestDto = new IngredientCategoryDTO();
        requestDto.setName("Vegetables");
        requestDto.setRestaurantId(1L);

        IngredientCategoryDTO responseDto = new IngredientCategoryDTO();
        responseDto.setIngredientCategoryId(1L);
        responseDto.setName("Vegetables");
        responseDto.setRestaurantId(1L);

        when(ingredientsService.createIngredientsCategory(any(IngredientCategoryDTO.class)))
                .thenReturn(responseDto);

        // Act & Assert
        mockMvc.perform(post("/api/admin/ingredients/category")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ingredientCategoryId").value(1))
                .andExpect(jsonPath("$.name").value("Vegetables"))
                .andExpect(jsonPath("$.restaurantId").value(1));

        verify(ingredientsService, times(1)).createIngredientsCategory(any(IngredientCategoryDTO.class));
    }

    @Test
    void createIngredient_ShouldReturnCreatedIngredient() throws Exception {
        // Arrange
        IngredientsItemDTO requestDto = new IngredientsItemDTO();
        requestDto.setName("Tomato");
        requestDto.setIngredientCategoryId(1L);
        requestDto.setRestaurantId(1L);

        IngredientsItemDTO responseDto = new IngredientsItemDTO();
        responseDto.setIngredientsItemId(1L);
        responseDto.setName("Tomato");
        responseDto.setIngredientCategoryId(1L);
        responseDto.setRestaurantId(1L);
        responseDto.setInStock(true);

        when(ingredientsService.createIngredientsItem(any(IngredientsItemDTO.class)))
                .thenReturn(responseDto);

        // Act & Assert
        mockMvc.perform(post("/api/admin/ingredients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ingredientsItemId").value(1))
                .andExpect(jsonPath("$.name").value("Tomato"))
                .andExpect(jsonPath("$.inStock").value(true));

        verify(ingredientsService, times(1)).createIngredientsItem(any(IngredientsItemDTO.class));
    }

    @Test
    void updateStock_ShouldReturnUpdatedIngredient() throws Exception {
        // Arrange
        Long ingredientId = 1L;
        IngredientsItemDTO responseDto = new IngredientsItemDTO();
        responseDto.setIngredientsItemId(ingredientId);
        responseDto.setName("Tomato");
        responseDto.setInStock(false); // Stock status changed

        when(ingredientsService.updateStock(ingredientId)).thenReturn(responseDto);

        // Act & Assert
        mockMvc.perform(put("/api/admin/ingredients/{id}/stock", ingredientId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ingredientsItemId").value(ingredientId))
                .andExpect(jsonPath("$.inStock").value(false));

        verify(ingredientsService, times(1)).updateStock(ingredientId);
    }

    @Test
    void getRestaurantsIngredients_ShouldReturnIngredientsList() throws Exception {
        // Arrange
        Long restaurantId = 1L;
        List<IngredientsItemDTO> ingredients = Arrays.asList(
                createIngredientsItemDTO(1L, "Tomato", 1L, "Vegetables", true),
                createIngredientsItemDTO(2L, "Chicken", 2L, "Meat", true)
        );

        when(ingredientsService.findRestaurantsIngredientItems(restaurantId))
                .thenReturn(ingredients);

        // Act & Assert
        mockMvc.perform(get("/api/admin/ingredients/restaurant/{id}", restaurantId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Tomato"))
                .andExpect(jsonPath("$[1].name").value("Chicken"));

        verify(ingredientsService, times(1)).findRestaurantsIngredientItems(restaurantId);
    }

    @Test
    void getRestaurantsIngredientCategories_ShouldReturnCategoriesList() throws Exception {
        // Arrange
        Long restaurantId = 1L;
        List<IngredientCategoryDTO> categories = Arrays.asList(
                createIngredientCategoryDTO(1L, "Vegetables", restaurantId),
                createIngredientCategoryDTO(2L, "Meat", restaurantId)
        );

        when(ingredientsService.findIngredientsCategoryByRestaurantId(restaurantId))
                .thenReturn(categories);

        // Act & Assert
        mockMvc.perform(get("/api/admin/ingredients/restaurant/{id}/category", restaurantId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Vegetables"))
                .andExpect(jsonPath("$[1].name").value("Meat"));

        verify(ingredientsService, times(1)).findIngredientsCategoryByRestaurantId(restaurantId);
    }

    @Test
    void updateIngredientCategory_ShouldReturnUpdatedCategory() throws Exception {
        // Arrange
        Long categoryId = 1L;
        IngredientCategoryDTO requestDto = new IngredientCategoryDTO();
        requestDto.setName("Updated Category");

        IngredientCategoryDTO responseDto = new IngredientCategoryDTO();
        responseDto.setIngredientCategoryId(categoryId);
        responseDto.setName("Updated Category");
        responseDto.setRestaurantId(1L);

        when(ingredientsService.updateIngredientsCategory(eq(categoryId), any(IngredientCategoryDTO.class)))
                .thenReturn(responseDto);

        // Act & Assert
        mockMvc.perform(put("/api/admin/ingredients/category/{id}", categoryId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ingredientCategoryId").value(categoryId))
                .andExpect(jsonPath("$.name").value("Updated Category"));

        verify(ingredientsService, times(1)).updateIngredientsCategory(eq(categoryId), any(IngredientCategoryDTO.class));
    }

    @Test
    void updateIngredient_ShouldReturnUpdatedIngredient() throws Exception {
        // Arrange
        Long ingredientId = 1L;
        IngredientsItemDTO requestDto = new IngredientsItemDTO();
        requestDto.setName("Updated Ingredient");
        requestDto.setIngredientCategoryId(2L);

        IngredientsItemDTO responseDto = new IngredientsItemDTO();
        responseDto.setIngredientsItemId(ingredientId);
        responseDto.setName("Updated Ingredient");
        responseDto.setIngredientCategoryId(2L);
        responseDto.setInStock(true);

        when(ingredientsService.updateIngredientsItem(eq(ingredientId), any(IngredientsItemDTO.class)))
                .thenReturn(responseDto);

        // Act & Assert
        mockMvc.perform(put("/api/admin/ingredients/{id}", ingredientId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ingredientsItemId").value(ingredientId))
                .andExpect(jsonPath("$.name").value("Updated Ingredient"))
                .andExpect(jsonPath("$.ingredientCategoryId").value(2));

        verify(ingredientsService, times(1)).updateIngredientsItem(eq(ingredientId), any(IngredientsItemDTO.class));
    }

    private IngredientsItemDTO createIngredientsItemDTO(Long id, String name,
                                                        Long categoryId, String categoryName, boolean inStock) {
        IngredientsItemDTO dto = new IngredientsItemDTO();
        dto.setIngredientsItemId(id);
        dto.setName(name);
        dto.setIngredientCategoryId(categoryId);
        dto.setIngredientCategoryName(categoryName);
        dto.setInStock(inStock);
        dto.setRestaurantId(1L);
        return dto;
    }

    private IngredientCategoryDTO createIngredientCategoryDTO(
            Long id, String name, Long restaurantId) {
        IngredientCategoryDTO dto = new IngredientCategoryDTO();
        dto.setIngredientCategoryId(id);
        dto.setName(name);
        dto.setRestaurantId(restaurantId);
        return dto;
    }
}