package com.kyoka.service;

import com.kyoka.dto.FoodDTO;

import java.util.List;

public interface FoodService {
    FoodDTO createFood(FoodDTO foodDTO);

    String deleteFood(Long foodId);

    List<FoodDTO> getRestaurantsFoods(Long restaurantId, boolean isVegetarian, boolean isNonveg, boolean isSeasonal,String foodCategory);

    List<FoodDTO> searchFood(String keyword);

    FoodDTO findFoodById(Long foodId);

    FoodDTO updateAvailibilityStatus(Long foodId);
}
