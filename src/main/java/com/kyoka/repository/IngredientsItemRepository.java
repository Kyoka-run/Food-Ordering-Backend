package com.kyoka.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import com.kyoka.model.IngredientsItem;

public interface IngredientsItemRepository extends JpaRepository<IngredientsItem, Long> {
    List<IngredientsItem> findByRestaurantRestaurantId(Long restaurantId);

    @Query("SELECT e FROM IngredientsItem e "
            + "WHERE e.restaurant.restaurantId = :restaurantId "
            + "AND lower(e.name) = lower(:name)"
            + "AND e.category.name = :categoryName")
    IngredientsItem findByRestaurantIdAndNameIgnoreCase(Long restaurantId, String name, String categoryName);
}