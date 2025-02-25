package com.kyoka.service.impl;

import com.kyoka.dto.FoodDTO;
import com.kyoka.exception.APIException;
import com.kyoka.exception.ResourceNotFoundException;
import com.kyoka.model.Category;
import com.kyoka.model.Food;
import com.kyoka.model.Restaurant;
import com.kyoka.repository.CategoryRepository;
import com.kyoka.repository.FoodRepository;
import com.kyoka.repository.RestaurantRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class FoodServiceImplTest {

    @Mock
    private FoodRepository foodRepository;

    @Mock
    private RestaurantRepository restaurantRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Spy
    private ModelMapper modelMapper = new ModelMapper();

    @InjectMocks
    private FoodServiceImpl foodService;

    private Restaurant testRestaurant;
    private Category testCategory;
    private Food testFood;
    private FoodDTO testFoodDTO;

    @BeforeEach
    void setUp() {
        // Set up test Restaurant
        testRestaurant = new Restaurant();
        testRestaurant.setRestaurantId(1L);
        testRestaurant.setName("Test Restaurant");
        testRestaurant.setFoods(new ArrayList<>());

        // Set up test Category
        testCategory = new Category();
        testCategory.setCategoryId(1L);
        testCategory.setName("Test Category");
        testCategory.setRestaurant(testRestaurant);

        // Set up test Food
        testFood = new Food();
        testFood.setFoodId(1L);
        testFood.setName("Test Food");
        testFood.setDescription("Test Description");
        testFood.setPrice(10L);
        testFood.setFoodCategory(testCategory);
        testFood.setImage("test-image.jpg");
        testFood.setAvailable(true);
        testFood.setRestaurant(testRestaurant);
        testFood.setVegetarian(true);
        testFood.setSeasonal(false);
        testFood.setCreationDate(new Date());
        testFood.setIngredients(new ArrayList<>());

        // Set up test FoodDTO
        testFoodDTO = new FoodDTO();
        testFoodDTO.setName("Test Food");
        testFoodDTO.setDescription("Test Description");
        testFoodDTO.setPrice(10L);
        testFoodDTO.setCategory(testCategory);
        testFoodDTO.setImage("test-image.jpg");
        testFoodDTO.setRestaurantId(1L);
        testFoodDTO.setVegetarian(true);
        testFoodDTO.setSeasonal(false);
    }

    @Test
    void createFood_ShouldCreateAndReturnFood_WhenValidData() {
        // Arrange
        when(restaurantRepository.findById(anyLong())).thenReturn(Optional.of(testRestaurant));
        when(categoryRepository.findById(anyLong())).thenReturn(Optional.of(testCategory));
        when(foodRepository.save(any(Food.class))).thenAnswer(invocation -> {
            Food savedFood = invocation.getArgument(0);
            savedFood.setFoodId(1L);
            return savedFood;
        });

        // Act
        FoodDTO result = foodService.createFood(testFoodDTO);

        // Assert
        assertNotNull(result);
        assertEquals("Test Food", result.getName());
        assertEquals(10L, result.getPrice());
        assertEquals(testCategory.getCategoryId(), result.getCategory().getCategoryId());

        verify(restaurantRepository, times(1)).findById(testFoodDTO.getRestaurantId());
        verify(categoryRepository, times(1)).findById(testFoodDTO.getCategory().getCategoryId());
        verify(foodRepository, times(1)).save(any(Food.class));
    }

    @Test
    void createFood_ShouldThrowException_WhenRestaurantNotFound() {
        // Arrange
        when(restaurantRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            foodService.createFood(testFoodDTO);
        });

        verify(restaurantRepository, times(1)).findById(testFoodDTO.getRestaurantId());
        verify(categoryRepository, never()).findById(anyLong());
        verify(foodRepository, never()).save(any(Food.class));
    }

    @Test
    void createFood_ShouldThrowException_WhenCategoryNotFound() {
        // Arrange
        when(restaurantRepository.findById(anyLong())).thenReturn(Optional.of(testRestaurant));
        when(categoryRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            foodService.createFood(testFoodDTO);
        });

        verify(restaurantRepository, times(1)).findById(testFoodDTO.getRestaurantId());
        verify(categoryRepository, times(1)).findById(testFoodDTO.getCategory().getCategoryId());
        verify(foodRepository, never()).save(any(Food.class));
    }

    @Test
    void createFood_ShouldThrowException_WhenCategoryNotInSameRestaurant() {
        // Arrange
        Restaurant differentRestaurant = new Restaurant();
        differentRestaurant.setRestaurantId(2L);

        Category categoryFromDifferentRestaurant = new Category();
        categoryFromDifferentRestaurant.setCategoryId(1L);
        categoryFromDifferentRestaurant.setRestaurant(differentRestaurant);

        when(restaurantRepository.findById(anyLong())).thenReturn(Optional.of(testRestaurant));
        when(categoryRepository.findById(anyLong())).thenReturn(Optional.of(categoryFromDifferentRestaurant));

        // Act & Assert
        assertThrows(APIException.class, () -> {
            foodService.createFood(testFoodDTO);
        });

        verify(restaurantRepository, times(1)).findById(testFoodDTO.getRestaurantId());
        verify(categoryRepository, times(1)).findById(testFoodDTO.getCategory().getCategoryId());
        verify(foodRepository, never()).save(any(Food.class));
    }

    @Test
    void deleteFood_ShouldDeleteAndReturnSuccessMessage() {
        // Arrange
        when(foodRepository.findById(anyLong())).thenReturn(Optional.of(testFood));
        doNothing().when(foodRepository).delete(any(Food.class));

        // Act
        String result = foodService.deleteFood(1L);

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("deleted successfully"));

        verify(foodRepository, times(1)).findById(1L);
        verify(foodRepository, times(1)).delete(testFood);
    }

    @Test
    void deleteFood_ShouldThrowException_WhenFoodNotFound() {
        // Arrange
        when(foodRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            foodService.deleteFood(999L);
        });

        verify(foodRepository, times(1)).findById(999L);
        verify(foodRepository, never()).delete(any(Food.class));
    }

    @Test
    void getRestaurantsFoods_ShouldReturnAllFoods_WhenNoFilters() {
        // Arrange
        Long restaurantId = 1L;
        List<Food> foods = Arrays.asList(
                testFood,
                createFood(2L, "Food 2", false, false),
                createFood(3L, "Food 3", true, true)
        );

        when(foodRepository.findByRestaurantRestaurantId(restaurantId))
                .thenReturn(Optional.of(foods));

        // Act
        List<FoodDTO> result = foodService.getRestaurantsFoods(restaurantId, false, false, false, null);

        // Assert
        assertNotNull(result);
        assertEquals(3, result.size());

        verify(foodRepository, times(1)).findByRestaurantRestaurantId(restaurantId);
    }

    @Test
    void getRestaurantsFoods_ShouldFilterVegetarianFoods_WhenVegetarianFlagIsTrue() {
        // Arrange
        Long restaurantId = 1L;
        List<Food> foods = Arrays.asList(
                testFood, // vegetarian
                createFood(2L, "Food 2", false, false), // non-vegetarian
                createFood(3L, "Food 3", true, true) // vegetarian
        );

        when(foodRepository.findByRestaurantRestaurantId(restaurantId))
                .thenReturn(Optional.of(foods));

        // Act
        List<FoodDTO> result = foodService.getRestaurantsFoods(restaurantId, true, false, false, null);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(FoodDTO::isVegetarian));

        verify(foodRepository, times(1)).findByRestaurantRestaurantId(restaurantId);
    }

    @Test
    void getRestaurantsFoods_ShouldFilterNonVegetarianFoods_WhenNonVegFlagIsTrue() {
        // Arrange
        Long restaurantId = 1L;
        List<Food> foods = Arrays.asList(
                testFood, // vegetarian
                createFood(2L, "Food 2", false, false), // non-vegetarian
                createFood(3L, "Food 3", true, true) // vegetarian
        );

        when(foodRepository.findByRestaurantRestaurantId(restaurantId))
                .thenReturn(Optional.of(foods));

        // Act
        List<FoodDTO> result = foodService.getRestaurantsFoods(restaurantId, false, true, false, null);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertFalse(result.get(0).isVegetarian());

        verify(foodRepository, times(1)).findByRestaurantRestaurantId(restaurantId);
    }

    @Test
    void getRestaurantsFoods_ShouldFilterSeasonalFoods_WhenSeasonalFlagIsTrue() {
        // Arrange
        Long restaurantId = 1L;
        List<Food> foods = Arrays.asList(
                testFood, // not seasonal
                createFood(2L, "Food 2", false, false), // not seasonal
                createFood(3L, "Food 3", true, true) // seasonal
        );

        when(foodRepository.findByRestaurantRestaurantId(restaurantId))
                .thenReturn(Optional.of(foods));

        // Act
        List<FoodDTO> result = foodService.getRestaurantsFoods(restaurantId, false, false, true, null);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.get(0).isSeasonal());

        verify(foodRepository, times(1)).findByRestaurantRestaurantId(restaurantId);
    }

    @Test
    void getRestaurantsFoods_ShouldFilterByCategory_WhenCategoryNameProvided() {
        // Arrange
        Long restaurantId = 1L;

        Category dessertCategory = new Category();
        dessertCategory.setCategoryId(2L);
        dessertCategory.setName("Desserts");
        dessertCategory.setRestaurant(testRestaurant);

        Food dessertFood = createFood(3L, "Dessert", true, false);
        dessertFood.setFoodCategory(dessertCategory);

        List<Food> foods = Arrays.asList(
                testFood, // category: "Test Category"
                createFood(2L, "Food 2", false, false), // category: "Test Category"
                dessertFood // category: "Desserts"
        );

        when(foodRepository.findByRestaurantRestaurantId(restaurantId))
                .thenReturn(Optional.of(foods));

        // Act
        List<FoodDTO> result = foodService.getRestaurantsFoods(restaurantId, false, false, false, "Desserts");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Dessert", result.get(0).getName());

        verify(foodRepository, times(1)).findByRestaurantRestaurantId(restaurantId);
    }

    @Test
    void getRestaurantsFoods_ShouldThrowException_WhenRestaurantNotFound() {
        // Arrange
        Long restaurantId = 999L;
        when(foodRepository.findByRestaurantRestaurantId(restaurantId))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            foodService.getRestaurantsFoods(restaurantId, false, false, false, null);
        });

        verify(foodRepository, times(1)).findByRestaurantRestaurantId(restaurantId);
    }

    @Test
    void searchFood_ShouldReturnMatchingFoods_WhenKeywordProvided() {
        // Arrange
        String keyword = "pizza";
        List<Food> matchingFoods = Arrays.asList(
                createFood(1L, "Pizza Margherita", true, false),
                createFood(2L, "Pizza Pepperoni", false, false)
        );

        when(foodRepository.searchByNameOrCategory(keyword)).thenReturn(matchingFoods);

        // Act
        List<FoodDTO> result = foodService.searchFood(keyword);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(food -> food.getName().contains("Pizza")));

        verify(foodRepository, times(1)).searchByNameOrCategory(keyword);
    }

    @Test
    void searchFood_ShouldReturnAllFoods_WhenKeywordIsNull() {
        // Arrange
        List<Food> allFoods = Arrays.asList(
                testFood,
                createFood(2L, "Food 2", false, false),
                createFood(3L, "Food 3", true, true)
        );

        when(foodRepository.findAll()).thenReturn(allFoods);

        // Act
        List<FoodDTO> result = foodService.searchFood(null);

        // Assert
        assertNotNull(result);
        assertEquals(3, result.size());

        verify(foodRepository, times(1)).findAll();
        verify(foodRepository, never()).searchByNameOrCategory(anyString());
    }

    @Test
    void searchFood_ShouldReturnAllFoods_WhenKeywordIsEmpty() {
        // Arrange
        List<Food> allFoods = Arrays.asList(
                testFood,
                createFood(2L, "Food 2", false, false),
                createFood(3L, "Food 3", true, true)
        );

        when(foodRepository.findAll()).thenReturn(allFoods);

        // Act
        List<FoodDTO> result = foodService.searchFood("");

        // Assert
        assertNotNull(result);
        assertEquals(3, result.size());

        verify(foodRepository, times(1)).findAll();
        verify(foodRepository, never()).searchByNameOrCategory(anyString());
    }

    @Test
    void findFoodById_ShouldReturnFood_WhenExists() {
        // Arrange
        when(foodRepository.findById(anyLong())).thenReturn(Optional.of(testFood));

        // Act
        FoodDTO result = foodService.findFoodById(1L);

        // Assert
        assertNotNull(result);
        assertEquals("Test Food", result.getName());
        assertEquals(10L, result.getPrice());

        verify(foodRepository, times(1)).findById(1L);
    }

    @Test
    void findFoodById_ShouldThrowException_WhenNotExists() {
        // Arrange
        when(foodRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            foodService.findFoodById(999L);
        });

        verify(foodRepository, times(1)).findById(999L);
    }

    @Test
    void updateAvailabilityStatus_ShouldToggleAndReturnUpdatedStatus() {
        // Arrange
        boolean initialStatus = testFood.isAvailable();
        when(foodRepository.findById(anyLong())).thenReturn(Optional.of(testFood));
        when(foodRepository.save(any(Food.class))).thenReturn(testFood);

        // Act
        FoodDTO result = foodService.updateAvailabilityStatus(1L);

        // Assert
        assertNotNull(result);
        assertEquals(!initialStatus, result.isAvailable());

        verify(foodRepository, times(1)).findById(1L);
        verify(foodRepository, times(1)).save(testFood);
    }

    @Test
    void updateFood_ShouldUpdateAndReturnFood() {
        // Arrange
        FoodDTO updateDTO = new FoodDTO();
        updateDTO.setName("Updated Food");
        updateDTO.setDescription("Updated Description");
        updateDTO.setPrice(15L);
        updateDTO.setVegetarian(false);
        updateDTO.setSeasonal(true);

        when(foodRepository.findById(anyLong())).thenReturn(Optional.of(testFood));
        when(foodRepository.save(any(Food.class))).thenReturn(testFood);

        // Act
        FoodDTO result = foodService.updateFood(1L, updateDTO);

        // Assert
        assertNotNull(result);
        assertEquals("Updated Food", result.getName());
        assertEquals("Updated Description", result.getDescription());
        assertEquals(15L, result.getPrice());
        assertFalse(result.isVegetarian());
        assertTrue(result.isSeasonal());

        verify(foodRepository, times(1)).findById(1L);
        verify(foodRepository, times(1)).save(testFood);
    }

    @Test
    void updateFood_ShouldUpdateCategoryIfProvided() {
        // Arrange
        Category newCategory = new Category();
        newCategory.setCategoryId(2L);
        newCategory.setName("New Category");
        newCategory.setRestaurant(testRestaurant);

        FoodDTO updateDTO = new FoodDTO();
        updateDTO.setCategory(newCategory);

        when(foodRepository.findById(anyLong())).thenReturn(Optional.of(testFood));
        when(categoryRepository.findById(anyLong())).thenReturn(Optional.of(newCategory));
        when(foodRepository.save(any(Food.class))).thenReturn(testFood);

        // Act
        FoodDTO result = foodService.updateFood(1L, updateDTO);

        // Assert
        assertNotNull(result);

        verify(foodRepository, times(1)).findById(1L);
        verify(categoryRepository, times(1)).findById(newCategory.getCategoryId());
        verify(foodRepository, times(1)).save(testFood);
    }

    @Test
    void updateFood_ShouldThrowException_WhenCategoryNotInSameRestaurant() {
        // Arrange
        Restaurant differentRestaurant = new Restaurant();
        differentRestaurant.setRestaurantId(2L);

        Category categoryFromDifferentRestaurant = new Category();
        categoryFromDifferentRestaurant.setCategoryId(2L);
        categoryFromDifferentRestaurant.setRestaurant(differentRestaurant);

        FoodDTO updateDTO = new FoodDTO();
        updateDTO.setCategory(categoryFromDifferentRestaurant);

        when(foodRepository.findById(anyLong())).thenReturn(Optional.of(testFood));
        when(categoryRepository.findById(anyLong())).thenReturn(Optional.of(categoryFromDifferentRestaurant));

        // Act & Assert
        assertThrows(APIException.class, () -> {
            foodService.updateFood(1L, updateDTO);
        });

        verify(foodRepository, times(1)).findById(1L);
        verify(categoryRepository, times(1)).findById(categoryFromDifferentRestaurant.getCategoryId());
        verify(foodRepository, never()).save(any(Food.class));
    }

    @Test
    void updateFood_ShouldThrowException_WhenFoodNotFound() {
        // Arrange
        FoodDTO updateDTO = new FoodDTO();
        updateDTO.setName("Updated Food");

        when(foodRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            foodService.updateFood(999L, updateDTO);
        });

        verify(foodRepository, times(1)).findById(999L);
        verify(foodRepository, never()).save(any(Food.class));
    }

    @Test
    void updateFood_ShouldNotUpdateCategory_WhenCategoryNotProvided() {
        // Arrange
        Category originalCategory = testFood.getFoodCategory();
        FoodDTO updateDTO = new FoodDTO();
        updateDTO.setName("Updated Food");

        when(foodRepository.findById(anyLong())).thenReturn(Optional.of(testFood));
        when(foodRepository.save(any(Food.class))).thenReturn(testFood);

        // Act
        FoodDTO result = foodService.updateFood(1L, updateDTO);

        // Assert
        assertNotNull(result);
        assertEquals("Updated Food", result.getName());
        assertEquals(originalCategory, testFood.getFoodCategory());

        verify(foodRepository, times(1)).findById(1L);
        verify(categoryRepository, never()).findById(anyLong());
        verify(foodRepository, times(1)).save(testFood);
    }

    @Test
    void updateFood_ShouldThrowException_WhenCategoryNotFound() {
        // Arrange
        Category newCategory = new Category();
        newCategory.setCategoryId(2L);

        FoodDTO updateDTO = new FoodDTO();
        updateDTO.setCategory(newCategory);

        when(foodRepository.findById(anyLong())).thenReturn(Optional.of(testFood));
        when(categoryRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            foodService.updateFood(1L, updateDTO);
        });

        verify(foodRepository, times(1)).findById(1L);
        verify(categoryRepository, times(1)).findById(newCategory.getCategoryId());
        verify(foodRepository, never()).save(any(Food.class));
    }

    @Test
    void searchFood_ShouldHandleEmptyResultList() {
        // Arrange
        String keyword = "nonexistent";
        when(foodRepository.searchByNameOrCategory(keyword)).thenReturn(Collections.emptyList());

        // Act
        List<FoodDTO> result = foodService.searchFood(keyword);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(foodRepository, times(1)).searchByNameOrCategory(keyword);
    }

    @Test
    void getRestaurantsFoods_ShouldApplyMultipleFilters() {
        // Arrange
        Long restaurantId = 1L;

        Food vegetarianSeasonalFood = createFood(3L, "Veg Seasonal", true, true);
        Food nonVegSeasonalFood = createFood(4L, "NonVeg Seasonal", false, true);

        List<Food> foods = Arrays.asList(
                testFood,                    // vegetarian, not seasonal
                createFood(2L, "Food 2", false, false),  // non-vegetarian, not seasonal
                vegetarianSeasonalFood,      // vegetarian, seasonal
                nonVegSeasonalFood           // non-vegetarian, seasonal
        );

        when(foodRepository.findByRestaurantRestaurantId(restaurantId))
                .thenReturn(Optional.of(foods));

        // Act - filter for vegetarian and seasonal foods
        List<FoodDTO> result = foodService.getRestaurantsFoods(restaurantId, true, false, true, null);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.get(0).isVegetarian());
        assertTrue(result.get(0).isSeasonal());
        assertEquals("Veg Seasonal", result.get(0).getName());

        verify(foodRepository, times(1)).findByRestaurantRestaurantId(restaurantId);
    }

    @Test
    void updateAvailabilityStatus_ShouldThrowException_WhenFoodNotFound() {
        // Arrange
        when(foodRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            foodService.updateAvailabilityStatus(999L);
        });

        verify(foodRepository, times(1)).findById(999L);
        verify(foodRepository, never()).save(any(Food.class));
    }

    private Food createFood(Long id, String name, boolean isVegetarian, boolean isSeasonal) {
        Food food = new Food();
        food.setFoodId(id);
        food.setName(name);
        food.setDescription("Description for " + name);
        food.setPrice(10L);
        food.setFoodCategory(testCategory);
        food.setImage("image-" + id + ".jpg");
        food.setAvailable(true);
        food.setRestaurant(testRestaurant);
        food.setVegetarian(isVegetarian);
        food.setSeasonal(isSeasonal);
        food.setCreationDate(new Date());
        food.setIngredients(new ArrayList<>());
        return food;
    }
}
