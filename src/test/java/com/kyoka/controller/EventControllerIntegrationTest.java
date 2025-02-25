package com.kyoka.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.kyoka.dto.EventDTO;
import com.kyoka.service.EventService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class EventControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EventService eventService;

    @Autowired
    private ObjectMapper objectMapper;

    private EventDTO testEventDTO;
    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        // Configure ObjectMapper for LocalDateTime serialization
        objectMapper.registerModule(new JavaTimeModule());

        now = LocalDateTime.now();

        // Set up test event DTO
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
    @WithMockUser(roles = "RESTAURANT_OWNER")
    void createEvent_ShouldReturnCreatedEvent() throws Exception {
        when(eventService.createEvent(any(EventDTO.class))).thenReturn(testEventDTO);

        mockMvc.perform(post("/api/admin/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testEventDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.eventId", is(1)))
                .andExpect(jsonPath("$.name", is("Test Event")))
                .andExpect(jsonPath("$.description", is("Test Description")))
                .andExpect(jsonPath("$.location", is("Test Location")));
    }

    @Test
    @WithMockUser
    void getAllEvents_ShouldReturnEventsList() throws Exception {
        List<EventDTO> events = Arrays.asList(
                testEventDTO,
                createEventDTO(2L, "Second Event")
        );

        when(eventService.getAllEvents()).thenReturn(events);

        mockMvc.perform(get("/api/events"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name", is("Test Event")))
                .andExpect(jsonPath("$[1].name", is("Second Event")));
    }

    @Test
    @WithMockUser
    void getRestaurantEvents_ShouldReturnRestaurantEventsList() throws Exception {
        List<EventDTO> events = Arrays.asList(
                testEventDTO,
                createEventDTO(2L, "Second Event")
        );

        when(eventService.getRestaurantEvents(anyLong())).thenReturn(events);

        mockMvc.perform(get("/api/events/restaurant/{restaurantId}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name", is("Test Event")))
                .andExpect(jsonPath("$[1].name", is("Second Event")));
    }

    @Test
    @WithMockUser
    void getEventById_ShouldReturnEvent() throws Exception {
        when(eventService.getEventById(anyLong())).thenReturn(testEventDTO);

        mockMvc.perform(get("/api/events/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.eventId", is(1)))
                .andExpect(jsonPath("$.name", is("Test Event")));
    }

    @Test
    @WithMockUser(roles = "RESTAURANT_OWNER")
    void updateEvent_ShouldReturnUpdatedEvent() throws Exception {
        EventDTO updatedEventDTO = new EventDTO();
        updatedEventDTO.setEventId(1L);
        updatedEventDTO.setName("Updated Event");
        updatedEventDTO.setDescription("Updated Description");
        updatedEventDTO.setImage("updated-image.jpg");
        updatedEventDTO.setStartTime(now.plusDays(1));
        updatedEventDTO.setEndTime(now.plusDays(1).plusHours(3));
        updatedEventDTO.setLocation("Updated Location");
        updatedEventDTO.setRestaurantId(1L);

        when(eventService.updateEvent(anyLong(), any(EventDTO.class))).thenReturn(updatedEventDTO);

        mockMvc.perform(put("/api/admin/events/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedEventDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Updated Event")))
                .andExpect(jsonPath("$.description", is("Updated Description")))
                .andExpect(jsonPath("$.location", is("Updated Location")));
    }

    @Test
    @WithMockUser(roles = "RESTAURANT_OWNER")
    void deleteEvent_ShouldReturnSuccessResponse() throws Exception {
        doNothing().when(eventService).deleteEvent(anyLong());

        mockMvc.perform(delete("/api/admin/events/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("Event deleted successfully")))
                .andExpect(jsonPath("$.status", is(true)));
    }

    private EventDTO createEventDTO(Long id, String name) {
        EventDTO dto = new EventDTO();
        dto.setEventId(id);
        dto.setName(name);
        dto.setDescription("Description for " + name);
        dto.setImage("image-" + id + ".jpg");
        dto.setStartTime(now);
        dto.setEndTime(now.plusHours(2));
        dto.setLocation("Location for " + name);
        dto.setRestaurantId(1L);
        return dto;
    }
}