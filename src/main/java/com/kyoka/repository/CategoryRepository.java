package com.kyoka.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.kyoka.model.Category;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    List<Category> findByRestaurantRestaurantId(Long RestaurantId);
}

