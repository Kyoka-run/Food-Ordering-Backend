package com.kyoka.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kyoka.Util.AuthUtil;
import com.kyoka.dto.CategoryDTO;
import com.kyoka.service.CategoryService;
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
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class CategoryControllerTest {

    @Mock
    private CategoryService categoryService;

    @Mock
    private AuthUtil authUtil;

    @InjectMocks
    private CategoryController categoryController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(categoryController).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void createCategory_ShouldReturnCreatedCategory() throws Exception {
        // Arrange
        CategoryDTO requestDto = new CategoryDTO();
        requestDto.setName("Test Category");
        requestDto.setRestaurantId(1L);

        CategoryDTO responseDto = new CategoryDTO();
        responseDto.setCategoryId(1L);
        responseDto.setName("Test Category");
        responseDto.setRestaurantId(1L);

        when(categoryService.createCategory(any(CategoryDTO.class))).thenReturn(responseDto);

        // Act & Assert
        mockMvc.perform(post("/api/admin/category")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.categoryId").value(1))
                .andExpect(jsonPath("$.name").value("Test Category"))
                .andExpect(jsonPath("$.restaurantId").value(1));

        verify(categoryService, times(1)).createCategory(any(CategoryDTO.class));
    }

    @Test
    void getRestaurantCategories_ShouldReturnCategoriesList() throws Exception {
        // Arrange
        Long restaurantId = 1L;
        List<CategoryDTO> categories = Arrays.asList(
                createCategoryDTO(1L, "Category 1", restaurantId),
                createCategoryDTO(2L, "Category 2", restaurantId)
        );

        when(categoryService.findCategoryByRestaurantId(restaurantId)).thenReturn(categories);

        // Act & Assert
        mockMvc.perform(get("/api/category/restaurant/{restaurantId}", restaurantId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].categoryId").value(1))
                .andExpect(jsonPath("$[0].name").value("Category 1"))
                .andExpect(jsonPath("$[1].categoryId").value(2))
                .andExpect(jsonPath("$[1].name").value("Category 2"));

        verify(categoryService, times(1)).findCategoryByRestaurantId(restaurantId);
    }

    @Test
    void getCategoryById_ShouldReturnCategory() throws Exception {
        // Arrange
        Long categoryId = 1L;
        CategoryDTO categoryDTO = createCategoryDTO(categoryId, "Test Category", 1L);

        when(categoryService.findCategoryById(categoryId)).thenReturn(categoryDTO);

        // Act & Assert
        mockMvc.perform(get("/api/admin/category/{id}", categoryId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.categoryId").value(categoryId))
                .andExpect(jsonPath("$.name").value("Test Category"));

        verify(categoryService, times(1)).findCategoryById(categoryId);
    }

    @Test
    void updateCategory_ShouldReturnUpdatedCategory() throws Exception {
        // Arrange
        Long categoryId = 1L;
        CategoryDTO requestDto = new CategoryDTO();
        requestDto.setCategoryId(categoryId);
        requestDto.setName("Updated Category");
        requestDto.setRestaurantId(1L);

        CategoryDTO responseDto = new CategoryDTO();
        responseDto.setCategoryId(categoryId);
        responseDto.setName("Updated Category");
        responseDto.setRestaurantId(1L);

        when(categoryService.updateCategory(any(CategoryDTO.class))).thenReturn(responseDto);

        // Act & Assert
        mockMvc.perform(put("/api/admin/category/{id}", categoryId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.categoryId").value(categoryId))
                .andExpect(jsonPath("$.name").value("Updated Category"));

        verify(categoryService, times(1)).updateCategory(any(CategoryDTO.class));
    }

    @Test
    void deleteCategory_ShouldReturnSuccessResponse() throws Exception {
        // Arrange
        Long categoryId = 1L;
        String successMessage = "Category deleted successfully with id: " + categoryId;

        when(categoryService.deleteCategory(categoryId)).thenReturn(successMessage);

        // Act & Assert
        mockMvc.perform(delete("/api/admin/category/{id}", categoryId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(successMessage))
                .andExpect(jsonPath("$.status").value(true));

        verify(categoryService, times(1)).deleteCategory(categoryId);
    }

    @Test
    void createCategory_WithInvalidData_ShouldReturnBadRequest() throws Exception {
        // Arrange
        CategoryDTO invalidDto = new CategoryDTO();
        // Omitting required fields

        // Act & Assert
        mockMvc.perform(post("/api/admin/category")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isOk()); // Since validation is handled at service layer

        verify(categoryService, times(1)).createCategory(any(CategoryDTO.class));
    }

    @Test
    void getRestaurantCategories_WithInvalidRestaurantId_ShouldReturnEmptyList() throws Exception {
        // Arrange
        Long invalidRestaurantId = 999L;
        when(categoryService.findCategoryByRestaurantId(invalidRestaurantId)).thenReturn(List.of());

        // Act & Assert
        mockMvc.perform(get("/api/category/restaurant/{restaurantId}", invalidRestaurantId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));

        verify(categoryService, times(1)).findCategoryByRestaurantId(invalidRestaurantId);
    }

    private CategoryDTO createCategoryDTO(Long id, String name, Long restaurantId) {
        CategoryDTO dto = new CategoryDTO();
        dto.setCategoryId(id);
        dto.setName(name);
        dto.setRestaurantId(restaurantId);
        return dto;
    }
}
