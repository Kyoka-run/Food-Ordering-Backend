package com.kyoka.service.impl;

import com.kyoka.dto.IngredientCategoryDTO;
import com.kyoka.dto.IngredientsItemDTO;
import com.kyoka.exception.ResourceNotFoundException;
import com.kyoka.model.IngredientCategory;
import com.kyoka.model.IngredientsItem;
import com.kyoka.model.Restaurant;
import com.kyoka.repository.IngredientsCategoryRepository;
import com.kyoka.repository.IngredientsItemRepository;
import com.kyoka.repository.RestaurantRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class IngredientsServiceImplTest {

    @Mock
    private IngredientsCategoryRepository ingredientsCategoryRepository;

    @Mock
    private IngredientsItemRepository ingredientsItemRepository;

    @Mock
    private RestaurantRepository restaurantRepository;

    @Spy
    private ModelMapper modelMapper = new ModelMapper();

    @InjectMocks
    private IngredientsServiceImpl ingredientsService;

    private Restaurant testRestaurant;
    private IngredientCategory testIngredientCategory;
    private IngredientsItem testIngredientsItem;
    private IngredientCategoryDTO testIngredientCategoryDTO;
    private IngredientsItemDTO testIngredientsItemDTO;

    @BeforeEach
    void setUp() {
        // Set up test Restaurant
        testRestaurant = new Restaurant();
        testRestaurant.setRestaurantId(1L);
        testRestaurant.setName("Test Restaurant");

        // Set up test IngredientCategory
        testIngredientCategory = new IngredientCategory();
        testIngredientCategory.setIngredientCategoryId(1L);
        testIngredientCategory.setName("Test Category");
        testIngredientCategory.setRestaurant(testRestaurant);

        // Set up test IngredientsItem
        testIngredientsItem = new IngredientsItem();
        testIngredientsItem.setIngredientsItemId(1L);
        testIngredientsItem.setName("Test Ingredient");
        testIngredientsItem.setIngredientCategory(testIngredientCategory);
        testIngredientsItem.setRestaurant(testRestaurant);
        testIngredientsItem.setInStock(true);

        // Set up test IngredientCategoryDTO
        testIngredientCategoryDTO = new IngredientCategoryDTO();
        testIngredientCategoryDTO.setIngredientCategoryId(1L);
        testIngredientCategoryDTO.setName("Test Category");
        testIngredientCategoryDTO.setRestaurantId(1L);

        // Set up test IngredientsItemDTO
        testIngredientsItemDTO = new IngredientsItemDTO();
        testIngredientsItemDTO.setIngredientsItemId(1L);
        testIngredientsItemDTO.setName("Test Ingredient");
        testIngredientsItemDTO.setIngredientCategoryId(1L);
        testIngredientsItemDTO.setIngredientCategoryName("Test Category");
        testIngredientsItemDTO.setRestaurantId(1L);
        testIngredientsItemDTO.setInStock(true);
    }

    @Test
    void createIngredientsCategory_ShouldCreateAndReturnCategory_WhenCategoryDoesNotExist() {
        // Arrange
        IngredientCategoryDTO newCategoryDTO = new IngredientCategoryDTO();
        newCategoryDTO.setName("New Category");
        newCategoryDTO.setRestaurantId(1L);

        when(ingredientsCategoryRepository.findByRestaurantIdAndNameIgnoreCase(anyLong(), anyString())).thenReturn(null);
        when(restaurantRepository.findById(anyLong())).thenReturn(Optional.of(testRestaurant));
        when(ingredientsCategoryRepository.save(any(IngredientCategory.class))).thenAnswer(invocation -> {
            IngredientCategory savedCategory = invocation.getArgument(0);
            savedCategory.setIngredientCategoryId(1L);
            return savedCategory;
        });

        // Act
        IngredientCategoryDTO result = ingredientsService.createIngredientsCategory(newCategoryDTO);

        // Assert
        assertNotNull(result);
        assertEquals(newCategoryDTO.getName(), result.getName());
        assertEquals(newCategoryDTO.getRestaurantId(), result.getRestaurantId());

        verify(ingredientsCategoryRepository, times(1)).findByRestaurantIdAndNameIgnoreCase(
                newCategoryDTO.getRestaurantId(), newCategoryDTO.getName());
        verify(restaurantRepository, times(1)).findById(newCategoryDTO.getRestaurantId());
        verify(ingredientsCategoryRepository, times(1)).save(any(IngredientCategory.class));
    }

    @Test
    void createIngredientsCategory_ShouldReturnExistingCategory_WhenCategoryExists() {
        // Arrange
        IngredientCategoryDTO existingCategoryDTO = new IngredientCategoryDTO();
        existingCategoryDTO.setName("Existing Category");
        existingCategoryDTO.setRestaurantId(1L);

        IngredientCategory existingCategory = new IngredientCategory();
        existingCategory.setIngredientCategoryId(1L);
        existingCategory.setName("Existing Category");
        existingCategory.setRestaurant(testRestaurant);

        when(ingredientsCategoryRepository.findByRestaurantIdAndNameIgnoreCase(anyLong(), anyString()))
                .thenReturn(existingCategory);

        // Act
        IngredientCategoryDTO result = ingredientsService.createIngredientsCategory(existingCategoryDTO);

        // Assert
        assertNotNull(result);
        assertEquals(existingCategory.getName(), result.getName());
        assertEquals(existingCategory.getRestaurant().getRestaurantId(), result.getRestaurantId());

        verify(ingredientsCategoryRepository, times(1)).findByRestaurantIdAndNameIgnoreCase(
                existingCategoryDTO.getRestaurantId(), existingCategoryDTO.getName());
        verify(restaurantRepository, never()).findById(anyLong());
        verify(ingredientsCategoryRepository, never()).save(any(IngredientCategory.class));
    }

    @Test
    void createIngredientsCategory_ShouldThrowException_WhenRestaurantNotFound() {
        // Arrange
        IngredientCategoryDTO newCategoryDTO = new IngredientCategoryDTO();
        newCategoryDTO.setName("New Category");
        newCategoryDTO.setRestaurantId(999L);

        when(ingredientsCategoryRepository.findByRestaurantIdAndNameIgnoreCase(anyLong(), anyString())).thenReturn(null);
        when(restaurantRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            ingredientsService.createIngredientsCategory(newCategoryDTO);
        });

        verify(ingredientsCategoryRepository, times(1)).findByRestaurantIdAndNameIgnoreCase(
                newCategoryDTO.getRestaurantId(), newCategoryDTO.getName());
        verify(restaurantRepository, times(1)).findById(newCategoryDTO.getRestaurantId());
        verify(ingredientsCategoryRepository, never()).save(any(IngredientCategory.class));
    }

    @Test
    void findIngredientsCategoryById_ShouldReturnCategory_WhenExists() {
        // Arrange
        when(ingredientsCategoryRepository.findById(anyLong())).thenReturn(Optional.of(testIngredientCategory));

        // Act
        IngredientCategoryDTO result = ingredientsService.findIngredientsCategoryById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(testIngredientCategory.getIngredientCategoryId(), result.getIngredientCategoryId());
        assertEquals(testIngredientCategory.getName(), result.getName());

        verify(ingredientsCategoryRepository, times(1)).findById(1L);
    }

    @Test
    void findIngredientsCategoryById_ShouldThrowException_WhenNotExists() {
        // Arrange
        when(ingredientsCategoryRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            ingredientsService.findIngredientsCategoryById(999L);
        });

        verify(ingredientsCategoryRepository, times(1)).findById(999L);
    }

    @Test
    void findIngredientsCategoryByRestaurantId_ShouldReturnCategories() {
        // Arrange
        List<IngredientCategory> categories = Arrays.asList(
                testIngredientCategory,
                createIngredientCategory(2L, "Category 2")
        );

        when(ingredientsCategoryRepository.findByRestaurantRestaurantId(anyLong())).thenReturn(categories);

        // Act
        List<IngredientCategoryDTO> result = ingredientsService.findIngredientsCategoryByRestaurantId(1L);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(testIngredientCategory.getName(), result.get(0).getName());
        assertEquals("Category 2", result.get(1).getName());

        verify(ingredientsCategoryRepository, times(1)).findByRestaurantRestaurantId(1L);
    }

    @Test
    void findRestaurantsIngredientItems_ShouldReturnItems() {
        // Arrange
        List<IngredientsItem> items = Arrays.asList(
                testIngredientsItem,
                createIngredientsItem(2L, "Ingredient 2")
        );

        when(ingredientsItemRepository.findByRestaurantRestaurantId(anyLong())).thenReturn(items);

        // Act
        List<IngredientsItemDTO> result = ingredientsService.findRestaurantsIngredientItems(1L);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(testIngredientsItem.getName(), result.get(0).getName());
        assertEquals("Ingredient 2", result.get(1).getName());

        verify(ingredientsItemRepository, times(1)).findByRestaurantRestaurantId(1L);
    }

    @Test
    void createIngredientsItem_ShouldCreateAndReturnItem_WhenItemDoesNotExist() {
        // Arrange
        IngredientsItemDTO newItemDTO = new IngredientsItemDTO();
        newItemDTO.setName("New Ingredient");
        newItemDTO.setIngredientCategoryId(1L);
        newItemDTO.setRestaurantId(1L);

        when(ingredientsCategoryRepository.findById(anyLong())).thenReturn(Optional.of(testIngredientCategory));
        when(ingredientsItemRepository.findByRestaurantIdAndNameIgnoreCase(anyLong(), anyString(), anyString()))
                .thenReturn(null);
        when(restaurantRepository.findById(anyLong())).thenReturn(Optional.of(testRestaurant));
        when(ingredientsItemRepository.save(any(IngredientsItem.class))).thenAnswer(invocation -> {
            IngredientsItem savedItem = invocation.getArgument(0);
            savedItem.setIngredientsItemId(1L);
            return savedItem;
        });
        when(ingredientsCategoryRepository.save(any(IngredientCategory.class))).thenReturn(testIngredientCategory);

        // Act
        IngredientsItemDTO result = ingredientsService.createIngredientsItem(newItemDTO);

        // Assert
        assertNotNull(result);
        assertEquals(newItemDTO.getName(), result.getName());
        assertEquals(newItemDTO.getIngredientCategoryId(), result.getIngredientCategoryId());
        assertEquals(newItemDTO.getRestaurantId(), result.getRestaurantId());
        assertTrue(result.isInStock()); // Default value should be true

        verify(ingredientsCategoryRepository, times(1)).findById(newItemDTO.getIngredientCategoryId());
        verify(ingredientsItemRepository, times(1)).findByRestaurantIdAndNameIgnoreCase(
                newItemDTO.getRestaurantId(), newItemDTO.getName(), testIngredientCategory.getName());
        verify(restaurantRepository, times(1)).findById(newItemDTO.getRestaurantId());
        verify(ingredientsItemRepository, times(1)).save(any(IngredientsItem.class));
        verify(ingredientsCategoryRepository, times(1)).save(testIngredientCategory);
    }

    @Test
    void createIngredientsItem_ShouldReturnExistingItem_WhenItemExists() {
        // Arrange
        IngredientsItemDTO existingItemDTO = new IngredientsItemDTO();
        existingItemDTO.setName("Existing Ingredient");
        existingItemDTO.setIngredientCategoryId(1L);
        existingItemDTO.setRestaurantId(1L);

        IngredientsItem existingItem = new IngredientsItem();
        existingItem.setIngredientsItemId(1L);
        existingItem.setName("Existing Ingredient");
        existingItem.setIngredientCategory(testIngredientCategory);
        existingItem.setRestaurant(testRestaurant);
        existingItem.setInStock(true);

        when(ingredientsCategoryRepository.findById(anyLong())).thenReturn(Optional.of(testIngredientCategory));
        when(ingredientsItemRepository.findByRestaurantIdAndNameIgnoreCase(anyLong(), anyString(), anyString()))
                .thenReturn(existingItem);

        // Act
        IngredientsItemDTO result = ingredientsService.createIngredientsItem(existingItemDTO);

        // Assert
        assertNotNull(result);
        assertEquals(existingItem.getName(), result.getName());
        assertEquals(existingItem.getIngredientCategory().getIngredientCategoryId(), result.getIngredientCategoryId());
        assertEquals(existingItem.getRestaurant().getRestaurantId(), result.getRestaurantId());

        verify(ingredientsCategoryRepository, times(1)).findById(existingItemDTO.getIngredientCategoryId());
        verify(ingredientsItemRepository, times(1)).findByRestaurantIdAndNameIgnoreCase(
                existingItemDTO.getRestaurantId(), existingItemDTO.getName(), testIngredientCategory.getName());
        verify(restaurantRepository, never()).findById(anyLong());
        verify(ingredientsItemRepository, never()).save(any(IngredientsItem.class));
    }

    @Test
    void createIngredientsItem_ShouldThrowException_WhenCategoryNotFound() {
        // Arrange
        IngredientsItemDTO newItemDTO = new IngredientsItemDTO();
        newItemDTO.setName("New Ingredient");
        newItemDTO.setIngredientCategoryId(999L);
        newItemDTO.setRestaurantId(1L);

        when(ingredientsCategoryRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            ingredientsService.createIngredientsItem(newItemDTO);
        });

        verify(ingredientsCategoryRepository, times(1)).findById(newItemDTO.getIngredientCategoryId());
        verify(ingredientsItemRepository, never()).findByRestaurantIdAndNameIgnoreCase(anyLong(), anyString(), anyString());
        verify(restaurantRepository, never()).findById(anyLong());
        verify(ingredientsItemRepository, never()).save(any(IngredientsItem.class));
    }

    @Test
    void createIngredientsItem_ShouldThrowException_WhenRestaurantNotFound() {
        // Arrange
        IngredientsItemDTO newItemDTO = new IngredientsItemDTO();
        newItemDTO.setName("New Ingredient");
        newItemDTO.setIngredientCategoryId(1L);
        newItemDTO.setRestaurantId(999L);

        when(ingredientsCategoryRepository.findById(anyLong())).thenReturn(Optional.of(testIngredientCategory));
        when(ingredientsItemRepository.findByRestaurantIdAndNameIgnoreCase(anyLong(), anyString(), anyString()))
                .thenReturn(null);
        when(restaurantRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            ingredientsService.createIngredientsItem(newItemDTO);
        });

        verify(ingredientsCategoryRepository, times(1)).findById(newItemDTO.getIngredientCategoryId());
        verify(ingredientsItemRepository, times(1)).findByRestaurantIdAndNameIgnoreCase(
                newItemDTO.getRestaurantId(), newItemDTO.getName(), testIngredientCategory.getName());
        verify(restaurantRepository, times(1)).findById(newItemDTO.getRestaurantId());
        verify(ingredientsItemRepository, never()).save(any(IngredientsItem.class));
    }

    @Test
    void updateStock_ShouldToggleAndReturnUpdatedStock() {
        // Arrange
        boolean initialStockStatus = testIngredientsItem.isInStock();

        when(ingredientsItemRepository.findById(anyLong())).thenReturn(Optional.of(testIngredientsItem));
        when(ingredientsItemRepository.save(any(IngredientsItem.class))).thenReturn(testIngredientsItem);

        // Act
        IngredientsItemDTO result = ingredientsService.updateStock(1L);

        // Assert
        assertNotNull(result);
        assertEquals(!initialStockStatus, result.isInStock());

        verify(ingredientsItemRepository, times(1)).findById(1L);
        verify(ingredientsItemRepository, times(1)).save(testIngredientsItem);
    }

    @Test
    void updateStock_ShouldThrowException_WhenItemNotFound() {
        // Arrange
        when(ingredientsItemRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            ingredientsService.updateStock(999L);
        });

        verify(ingredientsItemRepository, times(1)).findById(999L);
        verify(ingredientsItemRepository, never()).save(any(IngredientsItem.class));
    }

    @Test
    void updateIngredientsCategory_ShouldUpdateAndReturnCategory() {
        // Arrange
        IngredientCategoryDTO updateDTO = new IngredientCategoryDTO();
        updateDTO.setName("Updated Category");

        when(ingredientsCategoryRepository.findById(anyLong())).thenReturn(Optional.of(testIngredientCategory));
        when(ingredientsCategoryRepository.save(any(IngredientCategory.class))).thenReturn(testIngredientCategory);

        // Act
        IngredientCategoryDTO result = ingredientsService.updateIngredientsCategory(1L, updateDTO);

        // Assert
        assertNotNull(result);
        assertEquals(updateDTO.getName(), result.getName());

        verify(ingredientsCategoryRepository, times(1)).findById(1L);
        verify(ingredientsCategoryRepository, times(1)).save(testIngredientCategory);
    }

    @Test
    void updateIngredientsCategory_ShouldThrowException_WhenCategoryNotFound() {
        // Arrange
        IngredientCategoryDTO updateDTO = new IngredientCategoryDTO();
        updateDTO.setName("Updated Category");

        when(ingredientsCategoryRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            ingredientsService.updateIngredientsCategory(999L, updateDTO);
        });

        verify(ingredientsCategoryRepository, times(1)).findById(999L);
        verify(ingredientsCategoryRepository, never()).save(any(IngredientCategory.class));
    }

    @Test
    void updateIngredientsItem_ShouldUpdateAndReturnItem() {
        // Arrange
        IngredientsItemDTO updateDTO = new IngredientsItemDTO();
        updateDTO.setName("Updated Ingredient");
        updateDTO.setIngredientCategoryId(1L);

        when(ingredientsItemRepository.findById(anyLong())).thenReturn(Optional.of(testIngredientsItem));
        when(ingredientsCategoryRepository.findById(anyLong())).thenReturn(Optional.of(testIngredientCategory));
        when(ingredientsItemRepository.save(any(IngredientsItem.class))).thenReturn(testIngredientsItem);

        // Act
        IngredientsItemDTO result = ingredientsService.updateIngredientsItem(1L, updateDTO);

        // Assert
        assertNotNull(result);
        assertEquals(updateDTO.getName(), result.getName());

        verify(ingredientsItemRepository, times(1)).findById(1L);
        verify(ingredientsCategoryRepository, times(1)).findById(updateDTO.getIngredientCategoryId());
        verify(ingredientsItemRepository, times(1)).save(testIngredientsItem);
    }

    @Test
    void updateIngredientsItem_ShouldUpdateNameOnly_WhenCategoryIdNotProvided() {
        // Arrange
        IngredientsItemDTO updateDTO = new IngredientsItemDTO();
        updateDTO.setName("Updated Ingredient");
        // No category ID

        IngredientCategory originalCategory = testIngredientsItem.getIngredientCategory();

        when(ingredientsItemRepository.findById(anyLong())).thenReturn(Optional.of(testIngredientsItem));
        when(ingredientsItemRepository.save(any(IngredientsItem.class))).thenReturn(testIngredientsItem);

        // Act
        IngredientsItemDTO result = ingredientsService.updateIngredientsItem(1L, updateDTO);

        // Assert
        assertNotNull(result);
        assertEquals(updateDTO.getName(), result.getName());
        assertEquals(originalCategory, testIngredientsItem.getIngredientCategory());

        verify(ingredientsItemRepository, times(1)).findById(1L);
        verify(ingredientsCategoryRepository, never()).findById(anyLong());
        verify(ingredientsItemRepository, times(1)).save(testIngredientsItem);
    }

    @Test
    void updateIngredientsItem_ShouldThrowException_WhenItemNotFound() {
        // Arrange
        IngredientsItemDTO updateDTO = new IngredientsItemDTO();
        updateDTO.setName("Updated Ingredient");

        when(ingredientsItemRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            ingredientsService.updateIngredientsItem(999L, updateDTO);
        });

        verify(ingredientsItemRepository, times(1)).findById(999L);
        verify(ingredientsItemRepository, never()).save(any(IngredientsItem.class));
    }

    @Test
    void updateIngredientsItem_ShouldThrowException_WhenCategoryNotFound() {
        // Arrange
        IngredientsItemDTO updateDTO = new IngredientsItemDTO();
        updateDTO.setName("Updated Ingredient");
        updateDTO.setIngredientCategoryId(999L);

        when(ingredientsItemRepository.findById(anyLong())).thenReturn(Optional.of(testIngredientsItem));
        when(ingredientsCategoryRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            ingredientsService.updateIngredientsItem(1L, updateDTO);
        });

        verify(ingredientsItemRepository, times(1)).findById(1L);
        verify(ingredientsCategoryRepository, times(1)).findById(updateDTO.getIngredientCategoryId());
        verify(ingredientsItemRepository, never()).save(any(IngredientsItem.class));
    }

    private IngredientCategory createIngredientCategory(Long id, String name) {
        IngredientCategory category = new IngredientCategory();
        category.setIngredientCategoryId(id);
        category.setName(name);
        category.setRestaurant(testRestaurant);
        return category;
    }

    private IngredientsItem createIngredientsItem(Long id, String name) {
        IngredientsItem item = new IngredientsItem();
        item.setIngredientsItemId(id);
        item.setName(name);
        item.setIngredientCategory(testIngredientCategory);
        item.setRestaurant(testRestaurant);
        item.setInStock(true);
        return item;
    }
}