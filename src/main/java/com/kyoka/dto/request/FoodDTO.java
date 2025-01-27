package com.kyoka.dto.request;

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
    private String name;
    private String description;
    private Long price;

    private Category category;
    private List<String> images;

    private Long restaurantId;

    private boolean vegetarian;
    private boolean seasonal;

    private List<IngredientsItem> ingredients;
}

