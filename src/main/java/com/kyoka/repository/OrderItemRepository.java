package com.kyoka.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.kyoka.model.OrderItem;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

}

