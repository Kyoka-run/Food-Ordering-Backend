package com.kyoka.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.kyoka.model.Order;

public interface OrderRepository extends JpaRepository<Order,Long> {
    @Query("SELECT o FROM Order o WHERE o.user.userId = :userId")
    List<Order> findAllUserOrders(Long userId);

    @Query("SELECT o FROM Order o WHERE o.restaurant.restaurantId = :restaurantId")
    List<Order> findOrdersByRestaurantId(Long restaurantId);
}