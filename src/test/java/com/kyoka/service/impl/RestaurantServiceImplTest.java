package com.kyoka.service.impl;

import com.kyoka.util.AuthUtil;
import com.kyoka.dto.RestaurantDTO;
import com.kyoka.exception.ResourceNotFoundException;
import com.kyoka.model.ContactInformation;
import com.kyoka.model.Restaurant;
import com.kyoka.model.User;
import com.kyoka.repository.AddressRepository;
import com.kyoka.repository.RestaurantRepository;
import com.kyoka.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RestaurantServiceImplTest {

    @Mock
    private RestaurantRepository restaurantRepository;

    @Mock
    private AddressRepository addressRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuthUtil authUtil;

    @Spy
    private ModelMapper modelMapper = new ModelMapper();

    @InjectMocks
    private RestaurantServiceImpl restaurantService;

    private User testUser;
    private Restaurant testRestaurant;
    private RestaurantDTO testRestaurantDTO;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setUserId(1L);
        testUser.setUserName("testUser");
        testUser.setEmail("test@example.com");
        testUser.setFavoriteRestaurants(new ArrayList<>());

        ContactInformation contactInfo = new ContactInformation();
        contactInfo.setEmail("restaurant@example.com");
        contactInfo.setMobile("1234567890");

        testRestaurant = new Restaurant();
        testRestaurant.setRestaurantId(1L);
        testRestaurant.setName("Test Restaurant");
        testRestaurant.setDescription("Test Description");
        testRestaurant.setCuisineType("Test Cuisine");
        testRestaurant.setRestaurantAddress("123 Test St");
        testRestaurant.setContactInformation(contactInfo);
        testRestaurant.setOpeningHours("9:00-22:00");
        testRestaurant.setOpen(true);
        testRestaurant.setRegistrationDate(LocalDateTime.now());
        testRestaurant.setOwner(testUser);
        testRestaurant.setImages(new ArrayList<>());

        testRestaurantDTO = new RestaurantDTO();
        testRestaurantDTO.setName("Test Restaurant");
        testRestaurantDTO.setDescription("Test Description");
        testRestaurantDTO.setCuisineType("Test Cuisine");
        testRestaurantDTO.setAddress("123 Test St");
        testRestaurantDTO.setContactInformation(contactInfo);
        testRestaurantDTO.setOpeningHours("9:00-22:00");
        testRestaurantDTO.setImages(new ArrayList<>());
    }

    @Test
    void createRestaurant_ShouldCreateAndReturnRestaurant() {
        // Arrange
        when(authUtil.loggedInUser()).thenReturn(testUser);
        when(restaurantRepository.save(any(Restaurant.class))).thenAnswer(invocation -> {
            Restaurant savedRestaurant = invocation.getArgument(0);
            savedRestaurant.setRestaurantId(1L);
            return savedRestaurant;
        });

        // Act
        RestaurantDTO result = restaurantService.createRestaurant(testRestaurantDTO);

        // Assert
        assertNotNull(result);
        assertEquals("Test Restaurant", result.getName());
        assertEquals("Test Cuisine", result.getCuisineType());
        assertTrue(result.isOpen());

        verify(authUtil, times(1)).loggedInUser();
        verify(restaurantRepository, times(1)).save(any(Restaurant.class));
    }

    @Test
    void findRestaurantById_ShouldReturnRestaurant_WhenExists() {
        // Arrange
        when(restaurantRepository.findById(anyLong())).thenReturn(Optional.of(testRestaurant));

        // Act
        RestaurantDTO result = restaurantService.findRestaurantById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(testRestaurant.getName(), result.getName());
        assertEquals(testRestaurant.getDescription(), result.getDescription());

        verify(restaurantRepository, times(1)).findById(1L);
    }

    @Test
    void findRestaurantById_ShouldThrowException_WhenNotExists() {
        // Arrange
        when(restaurantRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            restaurantService.findRestaurantById(999L);
        });

        verify(restaurantRepository, times(1)).findById(999L);
    }

    @Test
    void updateRestaurant_ShouldUpdateAndReturnRestaurant() {
        // Arrange
        RestaurantDTO updateDTO = new RestaurantDTO();
        updateDTO.setName("Updated Restaurant");
        updateDTO.setDescription("Updated Description");
        updateDTO.setCuisineType("Updated Cuisine");
        updateDTO.setAddress("456 Updated St");
        updateDTO.setOpeningHours("10:00-23:00");

        when(restaurantRepository.findById(anyLong())).thenReturn(Optional.of(testRestaurant));
        when(restaurantRepository.save(any(Restaurant.class))).thenReturn(testRestaurant);

        // Act
        RestaurantDTO result = restaurantService.updateRestaurant(1L, updateDTO);

        // Assert
        assertNotNull(result);
        assertEquals("Updated Restaurant", result.getName());
        assertEquals("Updated Description", result.getDescription());
        assertEquals("Updated Cuisine", result.getCuisineType());

        verify(restaurantRepository, times(1)).findById(1L);
        verify(restaurantRepository, times(1)).save(any(Restaurant.class));
    }

    @Test
    void deleteRestaurant_ShouldDeleteAndReturnSuccessMessage() {
        // Arrange
        when(restaurantRepository.findById(anyLong())).thenReturn(Optional.of(testRestaurant));
        doNothing().when(restaurantRepository).delete(any(Restaurant.class));

        // Act
        String result = restaurantService.deleteRestaurant(1L);

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("deleted successfully"));

        verify(restaurantRepository, times(1)).findById(1L);
        verify(restaurantRepository, times(1)).delete(testRestaurant);
    }

    @Test
    void getAllRestaurant_ShouldReturnAllRestaurants() {
        // Arrange
        List<Restaurant> restaurants = Arrays.asList(
                testRestaurant,
                createRestaurant(2L, "Restaurant 2"),
                createRestaurant(3L, "Restaurant 3")
        );

        when(restaurantRepository.findAll()).thenReturn(restaurants);

        // Act
        List<RestaurantDTO> result = restaurantService.getAllRestaurant();

        // Assert
        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals("Test Restaurant", result.get(0).getName());
        assertEquals("Restaurant 2", result.get(1).getName());
        assertEquals("Restaurant 3", result.get(2).getName());

        verify(restaurantRepository, times(1)).findAll();
    }

    @Test
    void searchRestaurant_ShouldReturnMatchingRestaurants() {
        // Arrange
        String keyword = "test";
        List<Restaurant> matchingRestaurants = Arrays.asList(
                testRestaurant,
                createRestaurant(2L, "Another Test Place")
        );

        when(restaurantRepository.findBySearchQuery(keyword)).thenReturn(matchingRestaurants);

        // Act
        List<RestaurantDTO> result = restaurantService.searchRestaurant(keyword);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Test Restaurant", result.get(0).getName());
        assertEquals("Another Test Place", result.get(1).getName());

        verify(restaurantRepository, times(1)).findBySearchQuery(keyword);
    }

    @Test
    void updateRestaurantStatus_ShouldToggleAndReturnUpdatedStatus() {
        // Arrange
        boolean initialStatus = testRestaurant.isOpen();
        when(restaurantRepository.findById(anyLong())).thenReturn(Optional.of(testRestaurant));
        when(restaurantRepository.save(any(Restaurant.class))).thenReturn(testRestaurant);

        // Act
        RestaurantDTO result = restaurantService.updateRestaurantStatus(1L);

        // Assert
        assertNotNull(result);
        assertEquals(!initialStatus, result.isOpen());

        verify(restaurantRepository, times(1)).findById(1L);
        verify(restaurantRepository, times(1)).save(testRestaurant);
    }

    @Test
    void addToFavorites_ShouldAddRestaurantToFavorites_WhenNotAlreadyFavorited() {
        // Arrange
        when(restaurantRepository.findById(anyLong())).thenReturn(Optional.of(testRestaurant));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        String result = restaurantService.addToFavorites(1L, testUser);

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("added to favorites"));

        verify(restaurantRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).save(testUser);
    }

    @Test
    void addToFavorites_ShouldRemoveRestaurantFromFavorites_WhenAlreadyFavorited() {
        // Arrange
        testUser.getFavoriteRestaurants().add(testRestaurant);

        when(restaurantRepository.findById(anyLong())).thenReturn(Optional.of(testRestaurant));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        String result = restaurantService.addToFavorites(1L, testUser);

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("removed from favorites"));

        verify(restaurantRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).save(testUser);
    }

    @Test
    void getRestaurantsByUserId_ShouldReturnRestaurant() {
        // Arrange
        when(restaurantRepository.findByOwnerId(anyLong())).thenReturn(Optional.of(testRestaurant));

        // Act
        RestaurantDTO result = restaurantService.getRestaurantsByUserId(1L);

        // Assert
        assertNotNull(result);
        assertEquals("Test Restaurant", result.getName());

        verify(restaurantRepository, times(1)).findByOwnerId(1L);
    }

    @Test
    void getRestaurantsByUserId_ShouldThrowException_WhenNoRestaurantFound() {
        // Arrange
        when(restaurantRepository.findByOwnerId(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            restaurantService.getRestaurantsByUserId(999L);
        });

        verify(restaurantRepository, times(1)).findByOwnerId(999L);
    }

    private Restaurant createRestaurant(Long id, String name) {
        Restaurant restaurant = new Restaurant();
        restaurant.setRestaurantId(id);
        restaurant.setName(name);
        restaurant.setDescription("Description for " + name);
        restaurant.setCuisineType("Cuisine Type");
        restaurant.setRestaurantAddress("Address");
        restaurant.setOpeningHours("9:00-22:00");
        restaurant.setOpen(true);
        restaurant.setRegistrationDate(LocalDateTime.now());
        restaurant.setOwner(testUser);
        restaurant.setImages(new ArrayList<>());
        return restaurant;
    }
}