package com.kyoka.service.impl;

import com.kyoka.dto.EventDTO;
import com.kyoka.exception.ResourceNotFoundException;
import com.kyoka.model.Event;
import com.kyoka.model.Restaurant;
import com.kyoka.repository.EventRepository;
import com.kyoka.repository.RestaurantRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EventServiceImplTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private RestaurantRepository restaurantRepository;

    @Spy
    private ModelMapper modelMapper = new ModelMapper();

    @InjectMocks
    private EventServiceImpl eventService;

    private Restaurant testRestaurant;
    private Event testEvent;
    private EventDTO testEventDTO;
    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        now = LocalDateTime.now();

        // Set up test Restaurant
        testRestaurant = new Restaurant();
        testRestaurant.setRestaurantId(1L);
        testRestaurant.setName("Test Restaurant");

        // Set up test Event
        testEvent = new Event();
        testEvent.setEventId(1L);
        testEvent.setName("Test Event");
        testEvent.setDescription("Test Description");
        testEvent.setImage("test-image.jpg");
        testEvent.setStartTime(now);
        testEvent.setEndTime(now.plusHours(2));
        testEvent.setLocation("Test Location");
        testEvent.setRestaurant(testRestaurant);

        // Set up test EventDTO
        testEventDTO = new EventDTO();
        testEventDTO.setEventId(1L);
        testEventDTO.setName("Test Event");
        testEventDTO.setDescription("Test Description");
        testEventDTO.setImage("test-image.jpg");
        testEventDTO.setStartTime(now);
        testEventDTO.setEndTime(now.plusHours(2));
        testEventDTO.setLocation("Test Location");
        testEventDTO.setRestaurantId(1L);
    }

    @Test
    void createEvent_ShouldCreateAndReturnEvent() {
        // Arrange
        EventDTO newEventDTO = new EventDTO();
        newEventDTO.setName("New Event");
        newEventDTO.setDescription("New Description");
        newEventDTO.setStartTime(now);
        newEventDTO.setEndTime(now.plusHours(2));
        newEventDTO.setLocation("New Location");
        newEventDTO.setRestaurantId(1L);

        when(restaurantRepository.findById(anyLong())).thenReturn(Optional.of(testRestaurant));
        when(eventRepository.save(any(Event.class))).thenAnswer(invocation -> {
            Event savedEvent = invocation.getArgument(0);
            savedEvent.setEventId(1L);
            return savedEvent;
        });

        // Act
        EventDTO result = eventService.createEvent(newEventDTO);

        // Assert
        assertNotNull(result);
        assertEquals(newEventDTO.getName(), result.getName());
        assertEquals(newEventDTO.getDescription(), result.getDescription());
        assertEquals(newEventDTO.getStartTime(), result.getStartTime());
        assertEquals(newEventDTO.getEndTime(), result.getEndTime());
        assertEquals(newEventDTO.getLocation(), result.getLocation());
        assertEquals(newEventDTO.getRestaurantId(), result.getRestaurantId());

        verify(restaurantRepository, times(1)).findById(1L);
        verify(eventRepository, times(1)).save(any(Event.class));
    }

    @Test
    void createEvent_ShouldThrowException_WhenRestaurantNotFound() {
        // Arrange
        EventDTO newEventDTO = new EventDTO();
        newEventDTO.setName("New Event");
        newEventDTO.setRestaurantId(999L);

        when(restaurantRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            eventService.createEvent(newEventDTO);
        });

        verify(restaurantRepository, times(1)).findById(999L);
        verify(eventRepository, never()).save(any(Event.class));
    }

    @Test
    void getAllEvents_ShouldReturnAllEvents() {
        // Arrange
        List<Event> events = Arrays.asList(
                testEvent,
                createEvent(2L, "Event 2")
        );

        when(eventRepository.findAll()).thenReturn(events);

        // Act
        List<EventDTO> result = eventService.getAllEvents();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(testEvent.getName(), result.get(0).getName());
        assertEquals("Event 2", result.get(1).getName());

        verify(eventRepository, times(1)).findAll();
    }

    @Test
    void getRestaurantEvents_ShouldReturnRestaurantEvents() {
        // Arrange
        List<Event> events = Arrays.asList(
                testEvent,
                createEvent(2L, "Event 2")
        );

        when(eventRepository.findEventsByRestaurantRestaurantId(anyLong())).thenReturn(events);

        // Act
        List<EventDTO> result = eventService.getRestaurantEvents(1L);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(testEvent.getName(), result.get(0).getName());
        assertEquals("Event 2", result.get(1).getName());

        verify(eventRepository, times(1)).findEventsByRestaurantRestaurantId(1L);
    }

    @Test
    void getEventById_ShouldReturnEvent_WhenExists() {
        // Arrange
        when(eventRepository.findById(anyLong())).thenReturn(Optional.of(testEvent));

        // Act
        EventDTO result = eventService.getEventById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(testEvent.getEventId(), result.getEventId());
        assertEquals(testEvent.getName(), result.getName());
        assertEquals(testEvent.getDescription(), result.getDescription());

        verify(eventRepository, times(1)).findById(1L);
    }

    @Test
    void getEventById_ShouldThrowException_WhenNotExists() {
        // Arrange
        when(eventRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            eventService.getEventById(999L);
        });

        verify(eventRepository, times(1)).findById(999L);
    }

    @Test
    void updateEvent_ShouldUpdateAndReturnEvent() {
        // Arrange
        EventDTO updateDTO = new EventDTO();
        updateDTO.setName("Updated Event");
        updateDTO.setDescription("Updated Description");
        updateDTO.setImage("updated-image.jpg");
        updateDTO.setStartTime(now.plusDays(1));
        updateDTO.setEndTime(now.plusDays(1).plusHours(3));
        updateDTO.setLocation("Updated Location");

        when(eventRepository.findById(anyLong())).thenReturn(Optional.of(testEvent));
        when(eventRepository.save(any(Event.class))).thenReturn(testEvent);

        // Act
        EventDTO result = eventService.updateEvent(1L, updateDTO);

        // Assert
        assertNotNull(result);
        assertEquals(updateDTO.getName(), result.getName());
        assertEquals(updateDTO.getDescription(), result.getDescription());
        assertEquals(updateDTO.getImage(), result.getImage());
        assertEquals(updateDTO.getStartTime(), result.getStartTime());
        assertEquals(updateDTO.getEndTime(), result.getEndTime());
        assertEquals(updateDTO.getLocation(), result.getLocation());

        verify(eventRepository, times(1)).findById(1L);
        verify(eventRepository, times(1)).save(testEvent);
    }

    @Test
    void updateEvent_ShouldThrowException_WhenEventNotFound() {
        // Arrange
        EventDTO updateDTO = new EventDTO();
        updateDTO.setName("Updated Event");

        when(eventRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            eventService.updateEvent(999L, updateDTO);
        });

        verify(eventRepository, times(1)).findById(999L);
        verify(eventRepository, never()).save(any(Event.class));
    }

    @Test
    void deleteEvent_ShouldDeleteEvent() {
        // Arrange
        when(eventRepository.findById(anyLong())).thenReturn(Optional.of(testEvent));
        doNothing().when(eventRepository).delete(any(Event.class));

        // Act
        eventService.deleteEvent(1L);

        // Assert
        verify(eventRepository, times(1)).findById(1L);
        verify(eventRepository, times(1)).delete(testEvent);
    }

    @Test
    void deleteEvent_ShouldThrowException_WhenEventNotFound() {
        // Arrange
        when(eventRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            eventService.deleteEvent(999L);
        });

        verify(eventRepository, times(1)).findById(999L);
        verify(eventRepository, never()).delete(any(Event.class));
    }

    @Test
    void getAllEvents_ShouldReturnEmptyList_WhenNoEvents() {
        // Arrange
        when(eventRepository.findAll()).thenReturn(List.of());

        // Act
        List<EventDTO> result = eventService.getAllEvents();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(eventRepository, times(1)).findAll();
    }

    @Test
    void getRestaurantEvents_ShouldReturnEmptyList_WhenNoEvents() {
        // Arrange
        when(eventRepository.findEventsByRestaurantRestaurantId(anyLong())).thenReturn(List.of());

        // Act
        List<EventDTO> result = eventService.getRestaurantEvents(1L);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(eventRepository, times(1)).findEventsByRestaurantRestaurantId(1L);
    }

    private Event createEvent(Long id, String name) {
        Event event = new Event();
        event.setEventId(id);
        event.setName(name);
        event.setDescription("Description for " + name);
        event.setImage("image-" + id + ".jpg");
        event.setStartTime(now);
        event.setEndTime(now.plusHours(2));
        event.setLocation("Location for " + name);
        event.setRestaurant(testRestaurant);
        return event;
    }
}
