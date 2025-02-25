package com.kyoka.service.impl;

import com.kyoka.Util.AuthUtil;
import com.kyoka.dto.NotificationDTO;
import com.kyoka.model.Notification;
import com.kyoka.model.Restaurant;
import com.kyoka.model.User;
import com.kyoka.repository.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class NotificationServiceImplTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private AuthUtil authUtil;

    @Spy
    private ModelMapper modelMapper = new ModelMapper();

    @InjectMocks
    private NotificationServiceImpl notificationService;

    private User testUser;
    private Restaurant testRestaurant;
    private Notification testNotification;

    @BeforeEach
    void setUp() {
        // Set up test User
        testUser = new User();
        testUser.setUserId(1L);
        testUser.setUserName("testUser");
        testUser.setEmail("test@example.com");

        // Set up test Restaurant
        testRestaurant = new Restaurant();
        testRestaurant.setRestaurantId(1L);
        testRestaurant.setName("Test Restaurant");

        // Set up test Notification
        testNotification = new Notification();
        testNotification.setNotificationId(1L);
        testNotification.setCustomer(testUser);
        testNotification.setRestaurant(testRestaurant);
        testNotification.setMessage("Test notification message");
        testNotification.setSentAt(new Date());
        testNotification.setReadStatus(false);
    }

    @Test
    void findUsersNotification_ShouldReturnUserNotifications() {
        // Arrange
        List<Notification> notifications = Arrays.asList(
                testNotification,
                createNotification(2L, "Second notification message")
        );

        when(authUtil.loggedInUser()).thenReturn(testUser);
        when(notificationRepository.findByCustomerUserId(anyLong())).thenReturn(notifications);

        // Act
        List<NotificationDTO> result = notificationService.findUsersNotification();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());

        // Check first notification
        NotificationDTO firstNotification = result.get(0);
        assertEquals(testNotification.getNotificationId(), firstNotification.getNotificationId());
        assertEquals(testNotification.getMessage(), firstNotification.getMessage());
        assertEquals(testNotification.isReadStatus(), firstNotification.isReadStatus());
        assertEquals(testNotification.getCustomer().getUserId(), firstNotification.getCustomer().getUserId());
        assertEquals(testNotification.getRestaurant().getRestaurantId(), firstNotification.getRestaurant().getRestaurantId());

        // Check second notification
        assertEquals("Second notification message", result.get(1).getMessage());

        verify(authUtil, times(1)).loggedInUser();
        verify(notificationRepository, times(1)).findByCustomerUserId(testUser.getUserId());
    }

    @Test
    void findUsersNotification_ShouldReturnEmptyList_WhenUserHasNoNotifications() {
        // Arrange
        when(authUtil.loggedInUser()).thenReturn(testUser);
        when(notificationRepository.findByCustomerUserId(anyLong())).thenReturn(List.of());

        // Act
        List<NotificationDTO> result = notificationService.findUsersNotification();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(authUtil, times(1)).loggedInUser();
        verify(notificationRepository, times(1)).findByCustomerUserId(testUser.getUserId());
    }

    @Test
    void findUsersNotification_ShouldHandleReadAndUnreadNotifications() {
        // Arrange
        Notification readNotification = createNotification(2L, "Read notification");
        readNotification.setReadStatus(true);

        Notification unreadNotification = createNotification(3L, "Unread notification");
        unreadNotification.setReadStatus(false);

        List<Notification> notifications = Arrays.asList(readNotification, unreadNotification);

        when(authUtil.loggedInUser()).thenReturn(testUser);
        when(notificationRepository.findByCustomerUserId(anyLong())).thenReturn(notifications);

        // Act
        List<NotificationDTO> result = notificationService.findUsersNotification();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());

        // Find read notification
        NotificationDTO readNotificationDTO = result.stream()
                .filter(NotificationDTO::isReadStatus)
                .findFirst()
                .orElse(null);

        assertNotNull(readNotificationDTO);
        assertEquals("Read notification", readNotificationDTO.getMessage());
        assertTrue(readNotificationDTO.isReadStatus());

        // Find unread notification
        NotificationDTO unreadNotificationDTO = result.stream()
                .filter(n -> !n.isReadStatus())
                .findFirst()
                .orElse(null);

        assertNotNull(unreadNotificationDTO);
        assertEquals("Unread notification", unreadNotificationDTO.getMessage());
        assertFalse(unreadNotificationDTO.isReadStatus());

        verify(authUtil, times(1)).loggedInUser();
        verify(notificationRepository, times(1)).findByCustomerUserId(testUser.getUserId());
    }

    @Test
    void findUsersNotification_ShouldPreserveDateInformation() {
        // Arrange
        // Create a notification with a specific date
        Date specificDate = new Date(1640995200000L); // 2022-01-01
        Notification datedNotification = createNotification(2L, "Dated notification");
        datedNotification.setSentAt(specificDate);

        List<Notification> notifications = List.of(datedNotification);

        when(authUtil.loggedInUser()).thenReturn(testUser);
        when(notificationRepository.findByCustomerUserId(anyLong())).thenReturn(notifications);

        // Act
        List<NotificationDTO> result = notificationService.findUsersNotification();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());

        NotificationDTO notificationDTO = result.get(0);
        assertEquals(specificDate, notificationDTO.getSentAt());

        verify(authUtil, times(1)).loggedInUser();
        verify(notificationRepository, times(1)).findByCustomerUserId(testUser.getUserId());
    }

    @Test
    void findUsersNotification_ShouldPreserveUserAndRestaurantDetails() {
        // Arrange
        // Create users and restaurants with specific details
        User specificUser = new User();
        specificUser.setUserId(2L);
        specificUser.setUserName("specificUser");
        specificUser.setEmail("specific@example.com");

        Restaurant specificRestaurant = new Restaurant();
        specificRestaurant.setRestaurantId(2L);
        specificRestaurant.setName("Specific Restaurant");

        Notification specificNotification = new Notification();
        specificNotification.setNotificationId(2L);
        specificNotification.setCustomer(specificUser);
        specificNotification.setRestaurant(specificRestaurant);
        specificNotification.setMessage("Specific notification");
        specificNotification.setSentAt(new Date());
        specificNotification.setReadStatus(false);

        List<Notification> notifications = List.of(specificNotification);

        when(authUtil.loggedInUser()).thenReturn(specificUser);
        when(notificationRepository.findByCustomerUserId(anyLong())).thenReturn(notifications);

        // Act
        List<NotificationDTO> result = notificationService.findUsersNotification();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());

        NotificationDTO notificationDTO = result.get(0);
        assertEquals(specificUser.getUserId(), notificationDTO.getCustomer().getUserId());
        assertEquals(specificUser.getUserName(), notificationDTO.getCustomer().getUserName());
        assertEquals(specificUser.getEmail(), notificationDTO.getCustomer().getEmail());

        assertEquals(specificRestaurant.getRestaurantId(), notificationDTO.getRestaurant().getRestaurantId());
        assertEquals(specificRestaurant.getName(), notificationDTO.getRestaurant().getName());

        verify(authUtil, times(1)).loggedInUser();
        verify(notificationRepository, times(1)).findByCustomerUserId(specificUser.getUserId());
    }

    private Notification createNotification(Long id, String message) {
        Notification notification = new Notification();
        notification.setNotificationId(id);
        notification.setCustomer(testUser);
        notification.setRestaurant(testRestaurant);
        notification.setMessage(message);
        notification.setSentAt(new Date());
        notification.setReadStatus(false);
        return notification;
    }
}