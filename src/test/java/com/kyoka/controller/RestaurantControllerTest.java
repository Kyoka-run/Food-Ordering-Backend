package com.kyoka.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.kyoka.util.AuthUtil;
import com.kyoka.dto.RestaurantDTO;
import com.kyoka.model.ContactInformation;
import com.kyoka.model.User;
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

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class RestaurantControllerTest {

    @Mock
    private RestaurantService restaurantService;

    @Mock
    private AuthUtil authUtil;

    @InjectMocks
    private RestaurantController restaurantController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(restaurantController).build();
        objectMapper = new ObjectMapper();
        // add JavaTimeModule to support Java 8 Date Time Serialization
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    }

    @Test
    void createRestaurant_ShouldReturnCreatedRestaurant() throws Exception {
        // Arrange
        RestaurantDTO requestDto = createRestaurantDTO(null);
        RestaurantDTO responseDto = createRestaurantDTO(1L);

        when(restaurantService.createRestaurant(any(RestaurantDTO.class))).thenReturn(responseDto);

        // Act & Assert
        mockMvc.perform(post("/api/admin/restaurants")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.restaurantId").value(1))
                .andExpect(jsonPath("$.name").value("Test Restaurant"))
                .andExpect(jsonPath("$.cuisineType").value("Test Cuisine"));

        verify(restaurantService, times(1)).createRestaurant(any(RestaurantDTO.class));
    }

    @Test
    void updateRestaurant_ShouldReturnUpdatedRestaurant() throws Exception {
        // Arrange
        Long restaurantId = 1L;
        RestaurantDTO requestDto = createRestaurantDTO(restaurantId);
        requestDto.setName("Updated Restaurant");

        when(restaurantService.updateRestaurant(eq(restaurantId), any(RestaurantDTO.class)))
                .thenReturn(requestDto);

        // Act & Assert
        mockMvc.perform(put("/api/admin/restaurants/{id}", restaurantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.restaurantId").value(restaurantId))
                .andExpect(jsonPath("$.name").value("Updated Restaurant"));

        verify(restaurantService, times(1)).updateRestaurant(eq(restaurantId), any(RestaurantDTO.class));
    }

    @Test
    void deleteRestaurant_ShouldReturnSuccessResponse() throws Exception {
        // Arrange
        Long restaurantId = 1L;
        String successMessage = "Restaurant deleted successfully with restaurantId: " + restaurantId;

        when(restaurantService.deleteRestaurant(restaurantId)).thenReturn(successMessage);

        // Act & Assert
        mockMvc.perform(delete("/api/admin/restaurants/{id}", restaurantId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(successMessage))
                .andExpect(jsonPath("$.status").value(true));

        verify(restaurantService, times(1)).deleteRestaurant(restaurantId);
    }

    @Test
    void updateRestaurantStatus_ShouldReturnUpdatedStatus() throws Exception {
        // Arrange
        Long restaurantId = 1L;
        RestaurantDTO updatedRestaurant = createRestaurantDTO(restaurantId);
        updatedRestaurant.setOpen(false);

        when(restaurantService.updateRestaurantStatus(restaurantId)).thenReturn(updatedRestaurant);

        // Act & Assert
        mockMvc.perform(put("/api/admin/restaurants/{id}/status", restaurantId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.restaurantId").value(restaurantId))
                .andExpect(jsonPath("$.open").value(false));

        verify(restaurantService, times(1)).updateRestaurantStatus(restaurantId);
    }

    @Test
    void findRestaurantByUserId_ShouldReturnRestaurant() throws Exception {
        // Arrange
        User mockUser = new User();
        mockUser.setUserId(1L);
        RestaurantDTO restaurantDTO = createRestaurantDTO(1L);

        when(authUtil.loggedInUser()).thenReturn(mockUser);
        when(restaurantService.getRestaurantsByUserId(mockUser.getUserId())).thenReturn(restaurantDTO);

        // Act & Assert
        mockMvc.perform(get("/api/admin/restaurants/user"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.restaurantId").value(1));

        verify(restaurantService, times(1)).getRestaurantsByUserId(mockUser.getUserId());
    }

    @Test
    void findRestaurantByName_ShouldReturnMatchingRestaurants() throws Exception {
        // Arrange
        String keyword = "test";
        List<RestaurantDTO> restaurants = Arrays.asList(
                createRestaurantDTO(1L),
                createRestaurantDTO(2L)
        );

        when(restaurantService.searchRestaurant(keyword)).thenReturn(restaurants);

        // Act & Assert
        mockMvc.perform(get("/api/restaurants/search")
                        .param("keyword", keyword))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));

        verify(restaurantService, times(1)).searchRestaurant(keyword);
    }

    @Test
    void getAllRestaurants_ShouldReturnAllRestaurants() throws Exception {
        // Arrange
        List<RestaurantDTO> restaurants = Arrays.asList(
                createRestaurantDTO(1L),
                createRestaurantDTO(2L),
                createRestaurantDTO(3L)
        );

        when(restaurantService.getAllRestaurant()).thenReturn(restaurants);

        // Act & Assert
        mockMvc.perform(get("/api/restaurants"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(3));

        verify(restaurantService, times(1)).getAllRestaurant();
    }

    @Test
    void findRestaurantById_ShouldReturnRestaurant() throws Exception {
        // Arrange
        Long restaurantId = 1L;
        RestaurantDTO restaurantDTO = createRestaurantDTO(restaurantId);

        when(restaurantService.findRestaurantById(restaurantId)).thenReturn(restaurantDTO);

        // Act & Assert
        mockMvc.perform(get("/api/restaurants/{id}", restaurantId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.restaurantId").value(restaurantId));

        verify(restaurantService, times(1)).findRestaurantById(restaurantId);
    }

    @Test
    void addToFavorite_ShouldReturnSuccessMessage() throws Exception {
        // Arrange
        Long restaurantId = 1L;
        User mockUser = new User();
        String successMessage = "Restaurant added to favorites successfully";

        when(authUtil.loggedInUser()).thenReturn(mockUser);
        when(restaurantService.addToFavorites(restaurantId, mockUser)).thenReturn(successMessage);

        // Act & Assert
        mockMvc.perform(put("/api/restaurants/{id}/add-favorites", restaurantId))
                .andExpect(status().isOk())
                .andExpect(content().string(successMessage));

        verify(restaurantService, times(1)).addToFavorites(restaurantId, mockUser);
    }

    private RestaurantDTO createRestaurantDTO(Long id) {
        RestaurantDTO dto = new RestaurantDTO();
        dto.setRestaurantId(id);
        dto.setName("Test Restaurant");
        dto.setDescription("Test Description");
        dto.setCuisineType("Test Cuisine");
        dto.setAddress("Test Address");

        ContactInformation contactInfo = new ContactInformation();
        contactInfo.setEmail("test@example.com");
        contactInfo.setMobile("1234567890");
        dto.setContactInformation(contactInfo);

        dto.setOpeningHours("9:00-22:00");
        dto.setRegistrationDate(LocalDateTime.now());
        dto.setOpen(true);
        return dto;
    }
}