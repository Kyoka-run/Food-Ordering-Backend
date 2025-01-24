package com.kyoka.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.kyoka.model.Cart;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {
    @Query("SELECT c FROM Cart c WHERE c.user.userId = ?1")
    Optional<Cart> findCartByUserId(Long userId);
}

