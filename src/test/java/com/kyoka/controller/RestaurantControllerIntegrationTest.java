package com.kyoka.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kyoka.util.AuthUtil;
import com.kyoka.dto.RestaurantDTO;
import com.kyoka.model.ContactInformation;
import com.kyoka.model.User;
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

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.ArrayList;
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
public class RestaurantControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RestaurantService restaurantService;

    @MockitoBean
    private AuthUtil authUtil;

    @Autowired
    private ObjectMapper objectMapper;

    private RestaurantDTO testRestaurantDTO;
    private User testUser;

    @BeforeEach
    void setUp() {
        // Set up test user
        testUser = new User();
        testUser.setUserId(1L);
        testUser.setUserName("testUser");
        testUser.setEmail("test@example.com");

        // Set up contact information
        ContactInformation contactInfo = new ContactInformation();
        contactInfo.setEmail("restaurant@example.com");
        contactInfo.setMobile("1234567890");

        // Set up test restaurant DTO
        testRestaurantDTO = new RestaurantDTO();
        testRestaurantDTO.setRestaurantId(1L);
        testRestaurantDTO.setName("Test Restaurant");
        testRestaurantDTO.setDescription("Test Description");
        testRestaurantDTO.setCuisineType("Test Cuisine");
        testRestaurantDTO.setAddress("123 Test St");
        testRestaurantDTO.setContactInformation(contactInfo);
        testRestaurantDTO.setOpeningHours("9:00-22:00");
        testRestaurantDTO.setOpen(true);
        testRestaurantDTO.setRegistrationDate(LocalDateTime.now());
        testRestaurantDTO.setImages(new ArrayList<>());
    }

    @Test
    @WithMockUser(roles = "RESTAURANT_OWNER")
    void createRestaurant_ShouldReturnCreatedRestaurant() throws Exception {
        when(restaurantService.createRestaurant(any(RestaurantDTO.class))).thenReturn(testRestaurantDTO);

        mockMvc.perform(post("/api/admin/restaurants")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testRestaurantDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Test Restaurant")))
                .andExpect(jsonPath("$.cuisineType", is("Test Cuisine")))
                .andExpect(jsonPath("$.open", is(true)));
    }

    @Test
    @WithMockUser(roles = "RESTAURANT_OWNER")
    void updateRestaurant_ShouldReturnUpdatedRestaurant() throws Exception {
        when(restaurantService.updateRestaurant(anyLong(), any(RestaurantDTO.class))).thenReturn(testRestaurantDTO);

        mockMvc.perform(put("/api/admin/restaurants/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testRestaurantDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Test Restaurant")));
    }

    @Test
    @WithMockUser(roles = "RESTAURANT_OWNER")
    void deleteRestaurant_ShouldReturnSuccessResponse() throws Exception {
        String successMessage = "Restaurant deleted successfully with restaurantId: 1";
        when(restaurantService.deleteRestaurant(anyLong())).thenReturn(successMessage);

        mockMvc.perform(delete("/api/admin/restaurants/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is(successMessage)))
                .andExpect(jsonPath("$.status", is(true)));
    }

    @Test
    @WithMockUser(roles = "RESTAURANT_OWNER")
    void updateRestaurantStatus_ShouldReturnUpdatedStatus() throws Exception {
        testRestaurantDTO.setOpen(false);
        when(restaurantService.updateRestaurantStatus(anyLong())).thenReturn(testRestaurantDTO);

        mockMvc.perform(put("/api/admin/restaurants/{id}/status", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.open", is(false)));
    }

    @Test
    @WithMockUser
    void getAllRestaurants_ShouldReturnListOfRestaurants() throws Exception {
        List<RestaurantDTO> restaurants = Arrays.asList(
                testRestaurantDTO,
                createRestaurantDTO(2L, "Restaurant 2")
        );

        when(restaurantService.getAllRestaurant()).thenReturn(restaurants);

        mockMvc.perform(get("/api/restaurants"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name", is("Test Restaurant")))
                .andExpect(jsonPath("$[1].name", is("Restaurant 2")));
    }

    @Test
    @WithMockUser
    void findRestaurantById_ShouldReturnRestaurant() throws Exception {
        when(restaurantService.findRestaurantById(anyLong())).thenReturn(testRestaurantDTO);

        mockMvc.perform(get("/api/restaurants/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.restaurantId", is(1)))
                .andExpect(jsonPath("$.name", is("Test Restaurant")));
    }

    @Test
    @WithMockUser
    void searchRestaurant_ShouldReturnMatchingRestaurants() throws Exception {
        List<RestaurantDTO> restaurants = Arrays.asList(testRestaurantDTO);
        when(restaurantService.searchRestaurant(any())).thenReturn(restaurants);

        mockMvc.perform(get("/api/restaurants/search")
                        .param("keyword", "Test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is("Test Restaurant")));
    }

    @Test
    @WithMockUser
    void addToFavorite_ShouldReturnSuccessMessage() throws Exception {
        String successMessage = "Restaurant added to favorites successfully";
        when(authUtil.loggedInUser()).thenReturn(testUser);
        when(restaurantService.addToFavorites(anyLong(), any(User.class))).thenReturn(successMessage);

        mockMvc.perform(put("/api/restaurants/{id}/add-favorites", 1L))
                .andExpect(status().isOk())
                .andExpect(content().string(successMessage));
    }

    private RestaurantDTO createRestaurantDTO(Long id, String name) {
        RestaurantDTO dto = new RestaurantDTO();
        dto.setRestaurantId(id);
        dto.setName(name);
        dto.setDescription("Description for " + name);
        dto.setCuisineType("Cuisine Type");
        dto.setAddress("Address");
        dto.setOpeningHours("9:00-22:00");
        dto.setOpen(true);
        dto.setRegistrationDate(LocalDateTime.now());
        dto.setImages(new ArrayList<>());
        return dto;
    }
}