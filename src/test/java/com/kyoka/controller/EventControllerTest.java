package com.kyoka.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.kyoka.dto.EventDTO;
import com.kyoka.service.EventService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class EventControllerTest {

    @Mock
    private EventService eventService;

    @InjectMocks
    private EventController eventController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(eventController).build();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule()); // For LocalDateTime serialization
    }

    @Test
    void createEvent_ShouldReturnCreatedEvent() throws Exception {
        // Arrange
        EventDTO requestDto = createEventDTO(null, "New Year Event", "Description",
                LocalDateTime.now(), LocalDateTime.now().plusDays(1), 1L);

        EventDTO responseDto = createEventDTO(1L, "New Year Event", "Description",
                LocalDateTime.now(), LocalDateTime.now().plusDays(1), 1L);

        when(eventService.createEvent(any(EventDTO.class))).thenReturn(responseDto);

        // Act & Assert
        mockMvc.perform(post("/api/admin/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.eventId").value(1))
                .andExpect(jsonPath("$.name").value("New Year Event"))
                .andExpect(jsonPath("$.description").value("Description"));

        verify(eventService, times(1)).createEvent(any(EventDTO.class));
    }

    @Test
    void getAllEvents_ShouldReturnEventsList() throws Exception {
        // Arrange
        List<EventDTO> events = Arrays.asList(
                createEventDTO(1L, "Event 1", "Description 1",
                        LocalDateTime.now(), LocalDateTime.now().plusDays(1), 1L),
                createEventDTO(2L, "Event 2", "Description 2",
                        LocalDateTime.now(), LocalDateTime.now().plusDays(2), 2L)
        );

        when(eventService.getAllEvents()).thenReturn(events);

        // Act & Assert
        mockMvc.perform(get("/api/events"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].eventId").value(1))
                .andExpect(jsonPath("$[0].name").value("Event 1"))
                .andExpect(jsonPath("$[1].eventId").value(2))
                .andExpect(jsonPath("$[1].name").value("Event 2"));

        verify(eventService, times(1)).getAllEvents();
    }

    @Test
    void getRestaurantEvents_ShouldReturnRestaurantEventsList() throws Exception {
        // Arrange
        Long restaurantId = 1L;
        List<EventDTO> events = Arrays.asList(
                createEventDTO(1L, "Restaurant Event 1", "Description 1",
                        LocalDateTime.now(), LocalDateTime.now().plusDays(1), restaurantId),
                createEventDTO(2L, "Restaurant Event 2", "Description 2",
                        LocalDateTime.now(), LocalDateTime.now().plusDays(2), restaurantId)
        );

        when(eventService.getRestaurantEvents(restaurantId)).thenReturn(events);

        // Act & Assert
        mockMvc.perform(get("/api/events/restaurant/{restaurantId}", restaurantId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].restaurantId").value(restaurantId))
                .andExpect(jsonPath("$[1].restaurantId").value(restaurantId));

        verify(eventService, times(1)).getRestaurantEvents(restaurantId);
    }

    @Test
    void getEventById_ShouldReturnEvent() throws Exception {
        // Arrange
        Long eventId = 1L;
        EventDTO eventDTO = createEventDTO(eventId, "Test Event", "Description",
                LocalDateTime.now(), LocalDateTime.now().plusDays(1), 1L);

        when(eventService.getEventById(eventId)).thenReturn(eventDTO);

        // Act & Assert
        mockMvc.perform(get("/api/events/{id}", eventId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.eventId").value(eventId))
                .andExpect(jsonPath("$.name").value("Test Event"))
                .andExpect(jsonPath("$.description").value("Description"));

        verify(eventService, times(1)).getEventById(eventId);
    }

    @Test
    void updateEvent_ShouldReturnUpdatedEvent() throws Exception {
        // Arrange
        Long eventId = 1L;
        EventDTO requestDto = createEventDTO(eventId, "Updated Event", "Updated Description",
                LocalDateTime.now(), LocalDateTime.now().plusDays(1), 1L);

        EventDTO responseDto = createEventDTO(eventId, "Updated Event", "Updated Description",
                LocalDateTime.now(), LocalDateTime.now().plusDays(1), 1L);

        when(eventService.updateEvent(eq(eventId), any(EventDTO.class))).thenReturn(responseDto);

        // Act & Assert
        mockMvc.perform(put("/api/admin/events/{id}", eventId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.eventId").value(eventId))
                .andExpect(jsonPath("$.name").value("Updated Event"))
                .andExpect(jsonPath("$.description").value("Updated Description"));

        verify(eventService, times(1)).updateEvent(eq(eventId), any(EventDTO.class));
    }

    @Test
    void deleteEvent_ShouldReturnSuccessResponse() throws Exception {
        // Arrange
        Long eventId = 1L;
        doNothing().when(eventService).deleteEvent(eventId);

        // Act & Assert
        mockMvc.perform(delete("/api/admin/events/{id}", eventId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Event deleted successfully"))
                .andExpect(jsonPath("$.status").value(true));

        verify(eventService, times(1)).deleteEvent(eventId);
    }

    @Test
    void createEvent_WithInvalidDates_ShouldStillProcess() throws Exception {
        // Arrange
        EventDTO requestDto = createEventDTO(null, "Invalid Event", "Description",
                LocalDateTime.now().plusDays(1), LocalDateTime.now(), 1L); // End date before start date

        when(eventService.createEvent(any(EventDTO.class))).thenReturn(requestDto);

        // Act & Assert
        mockMvc.perform(post("/api/admin/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk()); // Validation should be handled at service layer

        verify(eventService, times(1)).createEvent(any(EventDTO.class));
    }

    @Test
    void getRestaurantEvents_WithInvalidRestaurantId_ShouldReturnEmptyList() throws Exception {
        // Arrange
        Long invalidRestaurantId = 999L;
        when(eventService.getRestaurantEvents(invalidRestaurantId)).thenReturn(List.of());

        // Act & Assert
        mockMvc.perform(get("/api/events/restaurant/{restaurantId}", invalidRestaurantId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));

        verify(eventService, times(1)).getRestaurantEvents(invalidRestaurantId);
    }

    private EventDTO createEventDTO(Long id, String name, String description,
                                    LocalDateTime startTime, LocalDateTime endTime, Long restaurantId) {
        EventDTO dto = new EventDTO();
        dto.setEventId(id);
        dto.setName(name);
        dto.setDescription(description);
        dto.setStartTime(startTime);
        dto.setEndTime(endTime);
        dto.setRestaurantId(restaurantId);
        dto.setLocation("Test Location");
        dto.setImage("test-image.jpg");
        return dto;
    }
}