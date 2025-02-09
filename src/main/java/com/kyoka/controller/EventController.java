package com.kyoka.controller;

import com.kyoka.dto.EventDTO;
import com.kyoka.dto.APIResponse;
import com.kyoka.service.EventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class EventController {
    @Autowired
    private EventService eventService;

    @PostMapping("/admin/events")
    public ResponseEntity<EventDTO> createEvent(@RequestBody EventDTO eventDTO) {
        EventDTO createdEvent = eventService.createEvent(eventDTO);
        return ResponseEntity.ok(createdEvent);
    }

    @GetMapping("/events")
    public ResponseEntity<List<EventDTO>> getAllEvents() {
        List<EventDTO> events = eventService.getAllEvents();
        return ResponseEntity.ok(events);
    }

    @GetMapping("/events/restaurant/{restaurantId}")
    public ResponseEntity<List<EventDTO>> getRestaurantEvents(@PathVariable Long restaurantId) {
        List<EventDTO> events = eventService.getRestaurantEvents(restaurantId);
        return ResponseEntity.ok(events);
    }

    @GetMapping("/events/{id}")
    public ResponseEntity<EventDTO> getEventById(@PathVariable Long id) {
        EventDTO event = eventService.getEventById(id);
        return ResponseEntity.ok(event);
    }

    @PutMapping("/admin/events/{id}")
    public ResponseEntity<EventDTO> updateEvent(
            @PathVariable Long id,
            @RequestBody EventDTO eventDTO) {
        EventDTO updatedEvent = eventService.updateEvent(id, eventDTO);
        return ResponseEntity.ok(updatedEvent);
    }

    @DeleteMapping("/admin/events/{id}")
    public ResponseEntity<APIResponse> deleteEvent(@PathVariable Long id) {
        eventService.deleteEvent(id);
        APIResponse response = new APIResponse("Event deleted successfully", true);
        return ResponseEntity.ok(response);
    }
}