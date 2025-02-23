package com.kyoka.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.kyoka.dto.NotificationDTO;
import com.kyoka.model.Restaurant;
import com.kyoka.model.User;
import com.kyoka.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class NotificationControllerTest {

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private NotificationController notificationController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(notificationController).build();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule()); // For Date serialization
    }

    @Test
    void findUsersNotification_ShouldReturnNotificationsList() throws Exception {
        // Arrange
        List<NotificationDTO> notifications = Arrays.asList(
                createNotificationDTO(1L, "Order Confirmed", true),
                createNotificationDTO(2L, "New Offer Available", false)
        );

        when(notificationService.findUsersNotification()).thenReturn(notifications);

        // Act & Assert
        mockMvc.perform(get("/api/notifications"))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].notificationId").value(1))
                .andExpect(jsonPath("$[0].message").value("Order Confirmed"))
                .andExpect(jsonPath("$[0].readStatus").value(true))
                .andExpect(jsonPath("$[1].notificationId").value(2))
                .andExpect(jsonPath("$[1].message").value("New Offer Available"))
                .andExpect(jsonPath("$[1].readStatus").value(false));

        verify(notificationService, times(1)).findUsersNotification();
    }

    @Test
    void findUsersNotification_WhenNoNotifications_ShouldReturnEmptyList() throws Exception {
        // Arrange
        when(notificationService.findUsersNotification()).thenReturn(List.of());

        // Act & Assert
        mockMvc.perform(get("/api/notifications"))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));

        verify(notificationService, times(1)).findUsersNotification();
    }

    @Test
    void findUsersNotification_ShouldIncludeCustomerAndRestaurantInfo() throws Exception {
        // Arrange
        NotificationDTO notification = createNotificationDTO(1L, "Test Notification", true);
        notification.getCustomer().setUserName("testuser");
        notification.getRestaurant().setName("Test Restaurant");

        when(notificationService.findUsersNotification()).thenReturn(List.of(notification));

        // Act & Assert
        mockMvc.perform(get("/api/notifications"))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$[0].customer.userName").value("testuser"))
                .andExpect(jsonPath("$[0].restaurant.name").value("Test Restaurant"));

        verify(notificationService, times(1)).findUsersNotification();
    }

    private NotificationDTO createNotificationDTO(Long id, String message, boolean readStatus) {
        NotificationDTO dto = new NotificationDTO();
        dto.setNotificationId(id);
        dto.setMessage(message);
        dto.setReadStatus(readStatus);
        dto.setSentAt(new Date());

        // Set customer
        User customer = new User();
        customer.setUserId(1L);
        customer.setUserName("testuser");
        customer.setEmail("test@example.com");
        dto.setCustomer(customer);

        // Set restaurant
        Restaurant restaurant = new Restaurant();
        restaurant.setRestaurantId(1L);
        restaurant.setName("Test Restaurant");
        dto.setRestaurant(restaurant);

        return dto;
    }
}