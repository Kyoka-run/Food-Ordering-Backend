package com.kyoka.service.impl;

import com.kyoka.util.AuthUtil;
import com.kyoka.dto.CategoryDTO;
import com.kyoka.exception.ResourceNotFoundException;
import com.kyoka.model.Category;
import com.kyoka.model.Restaurant;
import com.kyoka.model.User;
import com.kyoka.repository.CategoryRepository;
import com.kyoka.repository.RestaurantRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CategoryServiceImplTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private RestaurantRepository restaurantRepository;

    @Mock
    private AuthUtil authUtil;

    @Spy
    private ModelMapper modelMapper = new ModelMapper();

    @InjectMocks
    private CategoryServiceImpl categoryService;

    private User testUser;
    private Restaurant testRestaurant;
    private Category testCategory;
    private CategoryDTO testCategoryDTO;

    @BeforeEach
    void setUp() {
        // Set up test User
        testUser = new User();
        testUser.setUserId(1L);
        testUser.setUserName("testUser");
        testUser.setEmail("test@example.com");

        // Set up test Restaurant
        testRestaurant = new Restaurant();
        testRestaurant.setRestaurantId(1L);
        testRestaurant.setName("Test Restaurant");
        testRestaurant.setOwner(testUser);

        // Set up test Category
        testCategory = new Category();
        testCategory.setCategoryId(1L);
        testCategory.setName("Test Category");
        testCategory.setRestaurant(testRestaurant);

        // Set up test CategoryDTO
        testCategoryDTO = new CategoryDTO();
        testCategoryDTO.setCategoryId(1L);
        testCategoryDTO.setName("Test Category");
        testCategoryDTO.setRestaurantId(1L);
    }

    @Test
    void createCategory_ShouldCreateAndReturnCategory() {
        // Arrange
        when(authUtil.loggedInUser()).thenReturn(testUser);
        when(restaurantRepository.findByOwnerId(anyLong())).thenReturn(Optional.of(testRestaurant));
        when(categoryRepository.save(any(Category.class))).thenAnswer(invocation -> {
            Category savedCategory = invocation.getArgument(0);
            savedCategory.setCategoryId(1L);
            return savedCategory;
        });

        CategoryDTO newCategoryDTO = new CategoryDTO();
        newCategoryDTO.setName("New Category");

        // Act
        CategoryDTO result = categoryService.createCategory(newCategoryDTO);

        // Assert
        assertNotNull(result);
        assertEquals(newCategoryDTO.getName(), result.getName());
        assertEquals(testRestaurant.getRestaurantId(), result.getRestaurantId());

        verify(authUtil, times(1)).loggedInUser();
        verify(restaurantRepository, times(1)).findByOwnerId(testUser.getUserId());
        verify(categoryRepository, times(1)).save(any(Category.class));
    }

    @Test
    void createCategory_ShouldThrowException_WhenRestaurantNotFound() {
        // Arrange
        when(authUtil.loggedInUser()).thenReturn(testUser);
        when(restaurantRepository.findByOwnerId(anyLong())).thenReturn(Optional.empty());

        CategoryDTO newCategoryDTO = new CategoryDTO();
        newCategoryDTO.setName("New Category");

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            categoryService.createCategory(newCategoryDTO);
        });

        verify(authUtil, times(1)).loggedInUser();
        verify(restaurantRepository, times(1)).findByOwnerId(testUser.getUserId());
        verify(categoryRepository, never()).save(any(Category.class));
    }

    @Test
    void findCategoryByRestaurantId_ShouldReturnCategories() {
        // Arrange
        List<Category> categories = Arrays.asList(
                testCategory,
                createCategory(2L, "Category 2")
        );

        when(categoryRepository.findByRestaurantRestaurantId(anyLong())).thenReturn(categories);

        // Act
        List<CategoryDTO> result = categoryService.findCategoryByRestaurantId(1L);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(testCategory.getName(), result.get(0).getName());
        assertEquals("Category 2", result.get(1).getName());

        verify(categoryRepository, times(1)).findByRestaurantRestaurantId(1L);
    }

    @Test
    void findCategoryByRestaurantId_ShouldReturnEmptyList_WhenNoCategories() {
        // Arrange
        when(categoryRepository.findByRestaurantRestaurantId(anyLong())).thenReturn(new ArrayList<>());

        // Act
        List<CategoryDTO> result = categoryService.findCategoryByRestaurantId(1L);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(categoryRepository, times(1)).findByRestaurantRestaurantId(1L);
    }

    @Test
    void findCategoryById_ShouldReturnCategory_WhenExists() {
        // Arrange
        when(categoryRepository.findById(anyLong())).thenReturn(Optional.of(testCategory));

        // Act
        CategoryDTO result = categoryService.findCategoryById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(testCategory.getCategoryId(), result.getCategoryId());
        assertEquals(testCategory.getName(), result.getName());

        verify(categoryRepository, times(1)).findById(1L);
    }

    @Test
    void findCategoryById_ShouldThrowException_WhenNotExists() {
        // Arrange
        when(categoryRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            categoryService.findCategoryById(999L);
        });

        verify(categoryRepository, times(1)).findById(999L);
    }

    @Test
    void updateCategory_ShouldUpdateAndReturnCategory() {
        // Arrange
        CategoryDTO updateDTO = new CategoryDTO();
        updateDTO.setCategoryId(1L);
        updateDTO.setName("Updated Category");

        when(categoryRepository.findById(anyLong())).thenReturn(Optional.of(testCategory));
        when(categoryRepository.save(any(Category.class))).thenReturn(testCategory);

        // Act
        CategoryDTO result = categoryService.updateCategory(updateDTO);

        // Assert
        assertNotNull(result);
        assertEquals(updateDTO.getName(), result.getName());

        verify(categoryRepository, times(1)).findById(updateDTO.getCategoryId());
        verify(categoryRepository, times(1)).save(testCategory);
    }

    @Test
    void updateCategory_ShouldThrowException_WhenCategoryNotFound() {
        // Arrange
        CategoryDTO updateDTO = new CategoryDTO();
        updateDTO.setCategoryId(999L);
        updateDTO.setName("Updated Category");

        when(categoryRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            categoryService.updateCategory(updateDTO);
        });

        verify(categoryRepository, times(1)).findById(updateDTO.getCategoryId());
        verify(categoryRepository, never()).save(any(Category.class));
    }

    @Test
    void deleteCategory_ShouldDeleteAndReturnSuccessMessage() {
        // Arrange
        when(categoryRepository.findById(anyLong())).thenReturn(Optional.of(testCategory));
        doNothing().when(categoryRepository).delete(any(Category.class));

        // Act
        String result = categoryService.deleteCategory(1L);

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("deleted successfully"));

        verify(categoryRepository, times(1)).findById(1L);
        verify(categoryRepository, times(1)).delete(testCategory);
    }

    @Test
    void deleteCategory_ShouldThrowException_WhenCategoryNotFound() {
        // Arrange
        when(categoryRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            categoryService.deleteCategory(999L);
        });

        verify(categoryRepository, times(1)).findById(999L);
        verify(categoryRepository, never()).delete(any(Category.class));
    }

    @Test
    void createCategory_ShouldHandleNullFields() {
        // Arrange
        CategoryDTO incompleteDTO = new CategoryDTO();
        // Leave name null

        when(authUtil.loggedInUser()).thenReturn(testUser);
        when(restaurantRepository.findByOwnerId(anyLong())).thenReturn(Optional.of(testRestaurant));
        when(categoryRepository.save(any(Category.class))).thenAnswer(invocation -> {
            Category savedCategory = invocation.getArgument(0);
            savedCategory.setCategoryId(1L);
            return savedCategory;
        });

        // Act
        CategoryDTO result = categoryService.createCategory(incompleteDTO);

        // Assert
        assertNotNull(result);
        assertNull(result.getName());

        verify(authUtil, times(1)).loggedInUser();
        verify(restaurantRepository, times(1)).findByOwnerId(testUser.getUserId());
        verify(categoryRepository, times(1)).save(any(Category.class));
    }

    private Category createCategory(Long id, String name) {
        Category category = new Category();
        category.setCategoryId(id);
        category.setName(name);
        category.setRestaurant(testRestaurant);
        return category;
    }
}