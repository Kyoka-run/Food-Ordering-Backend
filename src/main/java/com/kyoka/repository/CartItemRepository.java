package com.kyoka.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.kyoka.model.CartItem;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
}
