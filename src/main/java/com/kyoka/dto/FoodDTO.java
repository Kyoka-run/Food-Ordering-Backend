package com.kyoka.dto;

import com.kyoka.model.Category;
import com.kyoka.model.IngredientsItem;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FoodDTO {
    private Long foodId;
    private String name;
    private String description;
    private Long price;
    private Category category;
    private String image;
    private Long restaurantId;
    private boolean vegetarian;
    private boolean seasonal;
    private boolean available;
    private List<IngredientsItem> ingredients;
}

