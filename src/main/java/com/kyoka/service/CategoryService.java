package com.kyoka.service;

import com.kyoka.dto.request.CategoryDTO;
import com.kyoka.model.User;

import java.util.List;

public interface CategoryService {
    CategoryDTO createCategory(CategoryDTO categoryDTO);

    List<CategoryDTO> findCategoryByRestaurantId(Long restaurantId);

    CategoryDTO findCategoryById(Long categoryId);

    CategoryDTO updateCategory(CategoryDTO categoryDTO);

    String deleteCategory(Long categoryId);
}