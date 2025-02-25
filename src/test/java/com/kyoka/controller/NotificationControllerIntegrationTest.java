package com.kyoka.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.kyoka.dto.NotificationDTO;
import com.kyoka.model.Restaurant;
import com.kyoka.model.User;
import com.kyoka.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class NotificationControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private NotificationService notificationService;

    @Autowired
    private ObjectMapper objectMapper;

    private NotificationDTO testNotificationDTO;

    @BeforeEach
    void setUp() {
        // Configure ObjectMapper for Date serialization
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

        // Set up test user
        User testUser = new User();
        testUser.setUserId(1L);
        testUser.setUserName("testUser");
        testUser.setEmail("test@example.com");

        // Set up test restaurant
        Restaurant testRestaurant = new Restaurant();
        testRestaurant.setRestaurantId(1L);
        testRestaurant.setName("Test Restaurant");

        // Set up test notification DTO
        testNotificationDTO = new NotificationDTO();
        testNotificationDTO.setNotificationId(1L);
        testNotificationDTO.setCustomer(testUser);
        testNotificationDTO.setRestaurant(testRestaurant);
        testNotificationDTO.setMessage("Test notification message");
        testNotificationDTO.setSentAt(new Date());
        testNotificationDTO.setReadStatus(false);
    }

    @Test
    @WithMockUser
    void findUsersNotification_ShouldReturnNotificationsList() throws Exception {
        List<NotificationDTO> notifications = Arrays.asList(
                testNotificationDTO,
                createNotificationDTO(2L, "Second notification message", true)
        );

        when(notificationService.findUsersNotification()).thenReturn(notifications);

        mockMvc.perform(get("/api/notifications"))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].notificationId", is(1)))
                .andExpect(jsonPath("$[0].message", is("Test notification message")))
                .andExpect(jsonPath("$[0].readStatus", is(false)))
                .andExpect(jsonPath("$[1].notificationId", is(2)))
                .andExpect(jsonPath("$[1].message", is("Second notification message")))
                .andExpect(jsonPath("$[1].readStatus", is(true)));
    }

    @Test
    @WithMockUser
    void findUsersNotification_WhenNoNotifications_ShouldReturnEmptyList() throws Exception {
        when(notificationService.findUsersNotification()).thenReturn(List.of());

        mockMvc.perform(get("/api/notifications"))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    private NotificationDTO createNotificationDTO(Long id, String message, boolean readStatus) {
        User testUser = new User();
        testUser.setUserId(1L);
        testUser.setUserName("testUser");
        testUser.setEmail("test@example.com");

        Restaurant testRestaurant = new Restaurant();
        testRestaurant.setRestaurantId(1L);
        testRestaurant.setName("Test Restaurant");

        NotificationDTO dto = new NotificationDTO();
        dto.setNotificationId(id);
        dto.setCustomer(testUser);
        dto.setRestaurant(testRestaurant);
        dto.setMessage(message);
        dto.setSentAt(new Date());
        dto.setReadStatus(readStatus);
        return dto;
    }
}