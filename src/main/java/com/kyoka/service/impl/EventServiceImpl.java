package com.kyoka.service.impl;

import com.kyoka.dto.EventDTO;
import com.kyoka.exception.ResourceNotFoundException;
import com.kyoka.model.Event;
import com.kyoka.model.Restaurant;
import com.kyoka.repository.EventRepository;
import com.kyoka.repository.RestaurantRepository;
import com.kyoka.service.EventService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class EventServiceImpl implements EventService {
    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private RestaurantRepository restaurantRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Override
    public EventDTO createEvent(EventDTO eventDTO) {
        Restaurant restaurant = restaurantRepository.findById(eventDTO.getRestaurantId())
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant", "id", eventDTO.getRestaurantId()));

        Event event = modelMapper.map(eventDTO, Event.class);
        event.setRestaurant(restaurant);
        Event savedEvent = eventRepository.save(event);

        return modelMapper.map(savedEvent, EventDTO.class);
    }

    @Override
    public List<EventDTO> getAllEvents() {
        List<Event> events = eventRepository.findAll();
        return events.stream()
                .map(event -> modelMapper.map(event, EventDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public List<EventDTO> getRestaurantEvents(Long restaurantId) {
        List<Event> events = eventRepository.findEventsByRestaurantRestaurantId(restaurantId);
        return events.stream()
                .map(event -> modelMapper.map(event, EventDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public EventDTO getEventById(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event", "id", eventId));
        return modelMapper.map(event, EventDTO.class);
    }

    @Override
    public EventDTO updateEvent(Long eventId, EventDTO eventDTO) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event", "id", eventId));

        event.setName(eventDTO.getName());
        event.setDescription(eventDTO.getDescription());
        event.setImage(eventDTO.getImage());
        event.setStartTime(eventDTO.getStartTime());
        event.setEndTime(eventDTO.getEndTime());
        event.setLocation(eventDTO.getLocation());

        Event updatedEvent = eventRepository.save(event);
        return modelMapper.map(updatedEvent, EventDTO.class);
    }

    @Override
    public void deleteEvent(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event", "id", eventId));
        eventRepository.delete(event);
    }
}