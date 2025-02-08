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
import com.kyoka.service.IngredientsService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class IngredientsServiceImpl implements IngredientsService {

    @Autowired
    private IngredientsCategoryRepository ingredientsCategoryRepository;

    @Autowired
    private IngredientsItemRepository ingredientsItemRepository;

    @Autowired
    private RestaurantRepository restaurantRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Override
    public IngredientCategoryDTO createIngredientsCategory(IngredientCategoryDTO categoryDTO) {
        // Check if category already exists
        IngredientCategory existingCategory = ingredientsCategoryRepository
                .findByRestaurantIdAndNameIgnoreCase(categoryDTO.getRestaurantId(), categoryDTO.getName());

        if (existingCategory != null) {
            return modelMapper.map(existingCategory, IngredientCategoryDTO.class);
        }

        // Find restaurant
        Restaurant restaurant = restaurantRepository.findById(categoryDTO.getRestaurantId())
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant", "id", categoryDTO.getRestaurantId()));

        // Create new category
        IngredientCategory category = new IngredientCategory();
        category.setName(categoryDTO.getName());
        category.setRestaurant(restaurant);

        IngredientCategory savedCategory = ingredientsCategoryRepository.save(category);
        return modelMapper.map(savedCategory, IngredientCategoryDTO.class);
    }

    @Override
    public IngredientCategoryDTO findIngredientsCategoryById(Long ingredientCategoryId) {
        IngredientCategory category = ingredientsCategoryRepository.findById(ingredientCategoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Ingredient Category", "id", ingredientCategoryId));
        return modelMapper.map(category, IngredientCategoryDTO.class);
    }

    @Override
    public List<IngredientCategoryDTO> findIngredientsCategoryByRestaurantId(Long restaurantId) {
        List<IngredientCategory> categories = ingredientsCategoryRepository.findByRestaurantRestaurantId(restaurantId);
        return categories.stream()
                .map(category -> modelMapper.map(category, IngredientCategoryDTO.class))
                .toList();
    }

    @Override
    public List<IngredientsItemDTO> findRestaurantsIngredientItems(Long restaurantId) {
        List<IngredientsItem> items = ingredientsItemRepository.findByRestaurantRestaurantId(restaurantId);
        return items.stream()
                .map(item -> modelMapper.map(item, IngredientsItemDTO.class))
                .toList();
    }

    @Override
    public IngredientsItemDTO createIngredientsItem(IngredientsItemDTO itemDTO) {
        // Find category
        IngredientCategory category = ingredientsCategoryRepository.findById(itemDTO.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Ingredient Category", "id", itemDTO.getCategoryId()));

        // Check if item already exists
        IngredientsItem existingItem = ingredientsItemRepository
                .findByRestaurantIdAndNameIgnoreCase(itemDTO.getRestaurantId(), itemDTO.getName(), category.getName());

        if (existingItem != null) {
            return modelMapper.map(existingItem, IngredientsItemDTO.class);
        }

        // Find restaurant
        Restaurant restaurant = restaurantRepository.findById(itemDTO.getRestaurantId())
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant", "id", itemDTO.getRestaurantId()));

        // Create new item
        IngredientsItem item = new IngredientsItem();
        item.setName(itemDTO.getName());
        item.setRestaurant(restaurant);
        item.setCategory(category);
        item.setInStoke(true);

        IngredientsItem savedItem = ingredientsItemRepository.save(item);

        // Update category's ingredients list
        category.getIngredients().add(savedItem);
        ingredientsCategoryRepository.save(category);

        return modelMapper.map(savedItem, IngredientsItemDTO.class);
    }

    @Override
    public IngredientsItemDTO updateStock(Long ingredientItemId) {
        IngredientsItem item = ingredientsItemRepository.findById(ingredientItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Ingredient Item", "id", ingredientItemId));

        item.setInStoke(!item.isInStoke());
        IngredientsItem updatedItem = ingredientsItemRepository.save(item);

        return modelMapper.map(updatedItem, IngredientsItemDTO.class);
    }
}