package com.kyoka.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kyoka.util.AuthUtil;
import com.kyoka.dto.FoodDTO;
import com.kyoka.model.Category;
import com.kyoka.service.FoodService;
import com.kyoka.service.RestaurantService;
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
public class FoodControllerTest {

    @Mock
    private FoodService foodService;

    @Mock
    private RestaurantService restaurantService;

    @Mock
    private AuthUtil authUtil;

    @InjectMocks
    private FoodController foodController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(foodController).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void createFood_ShouldReturnCreatedFood() throws Exception {
        // Arrange
        FoodDTO requestDto = createFoodDTO(null, "New Food", "Description",
                10L, true, false, true);
        FoodDTO responseDto = createFoodDTO(1L, "New Food", "Description",
                10L, true, false, true);

        when(foodService.createFood(any(FoodDTO.class))).thenReturn(responseDto);

        // Act & Assert
        mockMvc.perform(post("/api/admin/food")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.foodId").value(1))
                .andExpect(jsonPath("$.name").value("New Food"))
                .andExpect(jsonPath("$.price").value(10))
                .andExpect(jsonPath("$.vegetarian").value(true))
                .andExpect(jsonPath("$.seasonal").value(false))
                .andExpect(jsonPath("$.available").value(true));

        verify(foodService, times(1)).createFood(any(FoodDTO.class));
    }

    @Test
    void deleteFood_ShouldReturnSuccessResponse() throws Exception {
        // Arrange
        Long foodId = 1L;
        String successMessage = "Food deleted successfully with foodId: " + foodId;

        when(foodService.deleteFood(foodId)).thenReturn(successMessage);

        // Act & Assert
        mockMvc.perform(delete("/api/admin/food/{id}", foodId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(successMessage))
                .andExpect(jsonPath("$.status").value(true));

        verify(foodService, times(1)).deleteFood(foodId);
    }

    @Test
    void searchFood_ShouldReturnMatchingFoods() throws Exception {
        // Arrange
        String searchKeyword = "pizza";
        List<FoodDTO> searchResults = Arrays.asList(
                createFoodDTO(1L, "Pizza Margherita", "Classic pizza", 15L, true, false, true),
                createFoodDTO(2L, "Pizza Pepperoni", "Spicy pizza", 18L, false, false, true)
        );

        when(foodService.searchFood(searchKeyword)).thenReturn(searchResults);

        // Act & Assert
        mockMvc.perform(get("/api/food/search")
                        .param("name", searchKeyword))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Pizza Margherita"))
                .andExpect(jsonPath("$[1].name").value("Pizza Pepperoni"));

        verify(foodService, times(1)).searchFood(searchKeyword);
    }

    @Test
    void getRestaurantFoods_ShouldReturnFilteredFoods() throws Exception {
        // Arrange
        Long restaurantId = 1L;
        List<FoodDTO> filteredFoods = Arrays.asList(
                createFoodDTO(1L, "Veg Salad", "Fresh salad", 8L, true, true, true),
                createFoodDTO(2L, "Fruit Bowl", "Seasonal fruits", 12L, true, true, true)
        );

        when(foodService.getRestaurantsFoods(
                eq(restaurantId), eq(true), eq(false), eq(true), eq("Salads")
        )).thenReturn(filteredFoods);

        // Act & Assert
        mockMvc.perform(get("/api/food/restaurant/{restaurantId}", restaurantId)
                        .param("vegetarian", "true")
                        .param("nonveg", "false")
                        .param("seasonal", "true")
                        .param("food_category", "Salads"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].vegetarian").value(true))
                .andExpect(jsonPath("$[0].seasonal").value(true));

        verify(foodService, times(1)).getRestaurantsFoods(
                eq(restaurantId), eq(true), eq(false), eq(true), eq("Salads"));
    }

    @Test
    void updateFoodAvailability_ShouldReturnUpdatedFood() throws Exception {
        // Arrange
        Long foodId = 1L;
        FoodDTO updatedFood = createFoodDTO(foodId, "Test Food", "Description",
                10L, true, false, false); // availability changed to false

        when(foodService.updateAvailabilityStatus(foodId)).thenReturn(updatedFood);

        // Act & Assert
        mockMvc.perform(put("/api/admin/food/{id}/availability", foodId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.foodId").value(foodId))
                .andExpect(jsonPath("$.available").value(false));

        verify(foodService, times(1)).updateAvailabilityStatus(foodId);
    }

    @Test
    void getFoodById_ShouldReturnFood() throws Exception {
        // Arrange
        Long foodId = 1L;
        FoodDTO foodDTO = createFoodDTO(foodId, "Test Food", "Description",
                10L, true, false, true);

        when(foodService.findFoodById(foodId)).thenReturn(foodDTO);

        // Act & Assert
        mockMvc.perform(get("/api/admin/food/{id}", foodId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.foodId").value(foodId))
                .andExpect(jsonPath("$.name").value("Test Food"));

        verify(foodService, times(1)).findFoodById(foodId);
    }

    @Test
    void updateFood_ShouldReturnUpdatedFood() throws Exception {
        // Arrange
        Long foodId = 1L;
        FoodDTO requestDto = createFoodDTO(foodId, "Updated Food", "Updated Description",
                15L, true, true, true);
        FoodDTO responseDto = createFoodDTO(foodId, "Updated Food", "Updated Description",
                15L, true, true, true);

        when(foodService.updateFood(eq(foodId), any(FoodDTO.class))).thenReturn(responseDto);

        // Act & Assert
        mockMvc.perform(put("/api/admin/food/{id}", foodId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.foodId").value(foodId))
                .andExpect(jsonPath("$.name").value("Updated Food"))
                .andExpect(jsonPath("$.price").value(15))
                .andExpect(jsonPath("$.seasonal").value(true));

        verify(foodService, times(1)).updateFood(eq(foodId), any(FoodDTO.class));
    }

    @Test
    void getRestaurantFoods_WithNoFilters_ShouldReturnAllFoods() throws Exception {
        // Arrange
        Long restaurantId = 1L;
        List<FoodDTO> allFoods = Arrays.asList(
                createFoodDTO(1L, "Food 1", "Description 1", 10L, true, false, true),
                createFoodDTO(2L, "Food 2", "Description 2", 15L, false, true, true)
        );

        when(foodService.getRestaurantsFoods(
                eq(restaurantId), eq(false), eq(false), eq(false), isNull()
        )).thenReturn(allFoods);

        // Act & Assert
        mockMvc.perform(get("/api/food/restaurant/{restaurantId}", restaurantId)
                        .param("vegetarian", "false")
                        .param("nonveg", "false")
                        .param("seasonal", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));

        verify(foodService, times(1)).getRestaurantsFoods(
                eq(restaurantId), eq(false), eq(false), eq(false), isNull());
    }

    private FoodDTO createFoodDTO(Long id, String name, String description,
                                  Long price, boolean vegetarian, boolean seasonal, boolean available) {
        FoodDTO dto = new FoodDTO();
        dto.setFoodId(id);
        dto.setName(name);
        dto.setDescription(description);
        dto.setPrice(price);
        dto.setVegetarian(vegetarian);
        dto.setSeasonal(seasonal);
        dto.setAvailable(available);

        Category category = new Category();
        category.setCategoryId(1L);
        category.setName("Test Category");
        dto.setCategory(category);

        dto.setRestaurantId(1L);
        dto.setRestaurantName("Test Restaurant");
        return dto;
    }
}