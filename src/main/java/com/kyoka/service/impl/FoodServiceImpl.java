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
import com.kyoka.service.FoodService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class FoodServiceImpl implements FoodService {
    @Autowired
    private FoodRepository foodRepository;

    @Autowired
    private RestaurantRepository restaurantRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    ModelMapper modelMapper;

    @Override
    public FoodDTO createFood(FoodDTO foodDTO) {
        Restaurant restaurant = restaurantRepository.findById(foodDTO.getRestaurantId())
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant", "id", foodDTO.getRestaurantId()));

        Category category = categoryRepository.findById(foodDTO.getCategory().getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", foodDTO.getCategory().getCategoryId()));

        if (!category.getRestaurant().getRestaurantId().equals(restaurant.getRestaurantId())) {
            throw new APIException("Category does not exist in this restaurant");
        }

        Food food = new Food();
        food.setFoodCategory(category);
        food.setCreationDate(new Date());
        food.setDescription(foodDTO.getDescription());
        food.setImages(foodDTO.getImages());
        food.setName(foodDTO.getName());
        food.setPrice(foodDTO.getPrice());
        food.setSeasonal(foodDTO.isSeasonal());
        food.setVegetarian(foodDTO.isVegetarian());
        food.setIngredients(foodDTO.getIngredients());
        food.setRestaurant(restaurant);

        Food savedFood = foodRepository.save(food);
        return modelMapper.map(savedFood, FoodDTO.class);
    }

    @Override
    public String deleteFood(Long foodId) {
        Food food = foodRepository.findById(foodId)
                .orElseThrow(() -> new ResourceNotFoundException("Food", "id", foodId));
        foodRepository.delete(food);
        return "Food deleted successfully with foodId: " + foodId;
    }

    @Override
    public List<FoodDTO> getRestaurantsFoods(Long restaurantId, boolean isVegetarian, boolean isNonveg, boolean isSeasonal, String foodCategory) {
        List<Food> foods = foodRepository.findByRestaurantRestaurantId(restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant", "id", restaurantId));

        if (isVegetarian) {
            foods = foods.stream().filter(Food::isVegetarian).toList();
        }

        if (isNonveg) {
            foods = foods.stream().filter(food -> !food.isVegetarian()).toList();
        }

        // Food::isVegetarian == food -> food.isVegetarian()
        if (isSeasonal) {
            foods = foods.stream().filter(Food::isSeasonal).toList();
        }

        if (foodCategory != null && !foodCategory.isEmpty()) {
            foods = foods.stream()
                    .filter(food -> food.getFoodCategory().getName().equalsIgnoreCase(foodCategory))
                    .toList();
        }

        return foods.stream()
                .map(food -> modelMapper.map(food, FoodDTO.class))
                .toList();
    }

    @Override
    public List<FoodDTO> searchFood(String keyword) {
        List<Food> foods = keyword == null || keyword.isEmpty()
                ? foodRepository.findAll()
                : foodRepository.searchByNameOrCategory(keyword);

        return foods.stream()
                .map(food -> modelMapper.map(food, FoodDTO.class))
                .toList();
    }

    @Override
    public FoodDTO findFoodById(Long foodId) {
        Food food = foodRepository.findById(foodId)
                .orElseThrow(() -> new ResourceNotFoundException("Food", "id", foodId));
        return modelMapper.map(food, FoodDTO.class);
    }

    @Override
    public FoodDTO updateAvailibilityStatus(Long foodId) {
        Food food = foodRepository.findById(foodId)
                .orElseThrow(() -> new ResourceNotFoundException("Food", "id", foodId));

        food.setAvailable(!food.isAvailable());
        Food updatedFood = foodRepository.save(food);
        return modelMapper.map(updatedFood, FoodDTO.class);
    }
}
