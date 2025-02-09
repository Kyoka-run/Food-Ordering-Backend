package com.kyoka.repository;

import com.kyoka.model.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long> {
    List<Event> findEventsByRestaurantRestaurantId(Long restaurantId);
}
