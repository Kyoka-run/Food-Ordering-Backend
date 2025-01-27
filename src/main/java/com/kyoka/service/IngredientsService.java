package com.kyoka.service;

import com.kyoka.dto.request.IngredientCategoryDTO;
import com.kyoka.dto.request.IngredientsItemDTO;
import java.util.List;

public interface IngredientsService {

    IngredientCategoryDTO createIngredientsCategory(IngredientCategoryDTO categoryDTO);

    IngredientCategoryDTO findIngredientsCategoryById(Long id);

    List<IngredientCategoryDTO> findIngredientsCategoryByRestaurantId(Long restaurantId);

    List<IngredientsItemDTO> findRestaurantsIngredientItems(Long restaurantId);

    IngredientsItemDTO createIngredientsItem(IngredientsItemDTO itemDTO);

    IngredientsItemDTO updateStock(Long id);
}
