package com.kyoka.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class IngredientCategoryDTO {
    private Long ingredientCategoryId;
    private String name;
    private Long restaurantId;
}
