package com.kyoka.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class IngredientsItemDTO {
    private Long ingredientsItemId;
    private String name;
    private Long ingredientCategoryId;
    private String ingredientCategoryName;
    private Long restaurantId;
    private boolean inStock;
}