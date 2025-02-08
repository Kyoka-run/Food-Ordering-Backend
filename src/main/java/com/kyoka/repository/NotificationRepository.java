package com.kyoka.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.kyoka.model.Notification;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByCustomerUserId(Long userId);

    List<Notification> findByRestaurantRestaurantId(Long restaurantId);
}
