package com.kyoka.service.impl;

import com.kyoka.Util.AuthUtil;
import com.kyoka.dto.CategoryDTO;
import com.kyoka.exception.ResourceNotFoundException;
import com.kyoka.model.Category;
import com.kyoka.model.Restaurant;
import com.kyoka.model.User;
import com.kyoka.repository.CategoryRepository;
import com.kyoka.repository.RestaurantRepository;
import com.kyoka.service.CategoryService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private RestaurantRepository restaurantRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private AuthUtil authUtil;

    @Override
    public CategoryDTO createCategory(CategoryDTO categoryDTO) {
        User user = authUtil.loggedInUser();
        Restaurant restaurant = restaurantRepository.findByOwnerId(user.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant", "owner id", user.getUserId()));

        Category category = new Category();
        category.setName(categoryDTO.getName());
        category.setRestaurant(restaurant);

        Category savedCategory = categoryRepository.save(category);
        return modelMapper.map(savedCategory, CategoryDTO.class);
    }

    @Override
    public List<CategoryDTO> findCategoryByRestaurantId(Long restaurantId) {
        List<Category> categories = categoryRepository.findByRestaurantRestaurantId(restaurantId);

        return categories.stream()
                .map(category -> modelMapper.map(category, CategoryDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public CategoryDTO findCategoryById(Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", categoryId));

        return modelMapper.map(category, CategoryDTO.class);
    }

    @Override
    public CategoryDTO updateCategory(CategoryDTO categoryDTO) {
        Long categoryId = categoryDTO.getCategoryId();
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", categoryId));

        category.setName(categoryDTO.getName());
        Category updatedCategory = categoryRepository.save(category);

        return modelMapper.map(updatedCategory, CategoryDTO.class);
    }

    @Override
    public String deleteCategory(Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", categoryId));

        categoryRepository.delete(category);
        return "Category deleted successfully with id: " + categoryId;
    }
}