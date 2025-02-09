package com.kyoka.service;

import com.kyoka.dto.EventDTO;
import java.util.List;

public interface EventService {
    EventDTO createEvent(EventDTO eventDTO);

    List<EventDTO> getAllEvents();

    List<EventDTO> getRestaurantEvents(Long restaurantId);

    EventDTO getEventById(Long eventId);

    EventDTO updateEvent(Long eventId, EventDTO eventDTO);

    void deleteEvent(Long eventId);
}