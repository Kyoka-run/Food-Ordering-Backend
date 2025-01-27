package com.kyoka.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import com.kyoka.model.IngredientCategory;

public interface IngredientsCategoryRepository extends JpaRepository<IngredientCategory, Long>{

    List<IngredientCategory> findByRestaurantRestaurantId(Long restaurantId);

    @Query("SELECT e FROM IngredientCategory e "
            + "WHERE e.restaurant.restaurantId = :restaurantId "
            + "AND lower(e.name) = lower(:name)")
    IngredientCategory findByRestaurantIdAndNameIgnoreCase(Long restaurantId, String name);
}