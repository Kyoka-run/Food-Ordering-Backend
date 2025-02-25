package com.kyoka.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kyoka.util.AuthUtil;
import com.kyoka.dto.FoodDTO;
import com.kyoka.model.Category;
import com.kyoka.model.Restaurant;
import com.kyoka.model.User;
import com.kyoka.service.FoodService;
import com.kyoka.service.RestaurantService;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class FoodControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FoodService foodService;

    @MockitoBean
    private RestaurantService restaurantService;

    @MockitoBean
    private AuthUtil authUtil;

    @Autowired
    private ObjectMapper objectMapper;

    private FoodDTO testFoodDTO;
    private User testUser;
    private Restaurant testRestaurant;
    private Category testCategory;

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

        // Set up test category
        testCategory = new Category();
        testCategory.setCategoryId(1L);
        testCategory.setName("Test Category");
        testCategory.setRestaurant(testRestaurant);

        // Set up test food DTO
        testFoodDTO = new FoodDTO();
        testFoodDTO.setFoodId(1L);
        testFoodDTO.setName("Test Food");
        testFoodDTO.setDescription("Test Description");
        testFoodDTO.setPrice(10L);
        testFoodDTO.setCategory(testCategory);
        testFoodDTO.setImage("test-image.jpg");
        testFoodDTO.setRestaurantId(1L);
        testFoodDTO.setRestaurantName("Test Restaurant");
        testFoodDTO.setVegetarian(true);
        testFoodDTO.setSeasonal(false);
        testFoodDTO.setAvailable(true);
        testFoodDTO.setIngredients(new ArrayList<>());
    }

    @Test
    @WithMockUser(roles = "RESTAURANT_OWNER")
    void createFood_ShouldReturnCreatedFood() throws Exception {
        when(foodService.createFood(any(FoodDTO.class))).thenReturn(testFoodDTO);

        mockMvc.perform(post("/api/admin/food")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testFoodDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Test Food")))
                .andExpect(jsonPath("$.price", is(10)))
                .andExpect(jsonPath("$.vegetarian", is(true)))
                .andExpect(jsonPath("$.available", is(true)));
    }

    @Test
    @WithMockUser(roles = "RESTAURANT_OWNER")
    void deleteFood_ShouldReturnSuccessResponse() throws Exception {
        String successMessage = "Food deleted successfully with foodId: 1";
        when(foodService.deleteFood(anyLong())).thenReturn(successMessage);

        mockMvc.perform(delete("/api/admin/food/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is(successMessage)))
                .andExpect(jsonPath("$.status", is(true)));
    }

    @Test
    @WithMockUser
    void searchFood_ShouldReturnMatchingFoods() throws Exception {
        List<FoodDTO> foods = Arrays.asList(
                testFoodDTO,
                createFoodDTO(2L, "Pizza", false)
        );

        when(foodService.searchFood(anyString())).thenReturn(foods);

        mockMvc.perform(get("/api/food/search")
                        .param("name", "food"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name", is("Test Food")))
                .andExpect(jsonPath("$[1].name", is("Pizza")));
    }

    @Test
    @WithMockUser
    void getRestaurantFoods_ShouldReturnFilteredFoods() throws Exception {
        List<FoodDTO> foods = Arrays.asList(testFoodDTO);

        when(foodService.getRestaurantsFoods(anyLong(), anyBoolean(), anyBoolean(), anyBoolean(), anyString()))
                .thenReturn(foods);

        mockMvc.perform(get("/api/food/restaurant/{restaurantId}", 1L)
                        .param("vegetarian", "true")
                        .param("nonveg", "false")
                        .param("seasonal", "false")
                        .param("food_category", "Test Category"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is("Test Food")))
                .andExpect(jsonPath("$[0].vegetarian", is(true)));
    }

    @Test
    @WithMockUser(roles = "RESTAURANT_OWNER")
    void updateFoodAvailability_ShouldReturnUpdatedFood() throws Exception {
        testFoodDTO.setAvailable(false);
        when(foodService.updateAvailabilityStatus(anyLong())).thenReturn(testFoodDTO);

        mockMvc.perform(put("/api/admin/food/{id}/availability", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.available", is(false)));
    }

    @Test
    @WithMockUser(roles = "RESTAURANT_OWNER")
    void getFoodById_ShouldReturnFood() throws Exception {
        when(foodService.findFoodById(anyLong())).thenReturn(testFoodDTO);

        mockMvc.perform(get("/api/admin/food/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.foodId", is(1)))
                .andExpect(jsonPath("$.name", is("Test Food")));
    }

    @Test
    @WithMockUser(roles = "RESTAURANT_OWNER")
    void updateFood_ShouldReturnUpdatedFood() throws Exception {
        testFoodDTO.setName("Updated Food");
        testFoodDTO.setPrice(15L);

        when(foodService.updateFood(anyLong(), any(FoodDTO.class))).thenReturn(testFoodDTO);

        mockMvc.perform(put("/api/admin/food/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testFoodDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Updated Food")))
                .andExpect(jsonPath("$.price", is(15)));
    }

    private FoodDTO createFoodDTO(Long id, String name, boolean vegetarian) {
        FoodDTO dto = new FoodDTO();
        dto.setFoodId(id);
        dto.setName(name);
        dto.setDescription("Description for " + name);
        dto.setPrice(10L);
        dto.setCategory(testCategory);
        dto.setImage("image-" + id + ".jpg");
        dto.setRestaurantId(1L);
        dto.setRestaurantName("Test Restaurant");
        dto.setVegetarian(vegetarian);
        dto.setSeasonal(false);
        dto.setAvailable(true);
        dto.setIngredients(new ArrayList<>());
        return dto;
    }
}