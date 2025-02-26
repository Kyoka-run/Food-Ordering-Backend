package com.kyoka.service.impl;

import com.kyoka.model.User;
import com.kyoka.repository.PasswordResetTokenRepository;
import com.kyoka.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Mock
    private JavaMailSender javaMailSender;

    @InjectMocks
    private UserServiceImpl userService;

    private User testUser;
    private List<User> testUsers;
    private String testEmail;
    private String testNewPassword;
    private String testEncodedPassword;

    @BeforeEach
    void setUp() {
        testEmail = "test@example.com";
        testNewPassword = "newPassword123";
        testEncodedPassword = "encodedPassword123";

        // Set up test User
        testUser = new User();
        testUser.setUserId(1L);
        testUser.setUserName("testUser");
        testUser.setEmail(testEmail);
        testUser.setPassword("oldPassword123");
        testUser.setRoles(new HashSet<>());

        // Set up additional test users
        User user2 = new User();
        user2.setUserId(2L);
        user2.setUserName("user2");
        user2.setEmail("user2@example.com");

        User user3 = new User();
        user3.setUserId(3L);
        user3.setUserName("user3");
        user3.setEmail("user3@example.com");

        testUsers = Arrays.asList(testUser, user2, user3);
    }

    @Test
    void findUserByEmail_ShouldReturnUser_WhenUserExists() {
        // Arrange
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));

        // Act
        User result = userService.findUserByEmail(testEmail);

        // Assert
        assertNotNull(result);
        assertEquals(testUser.getUserId(), result.getUserId());
        assertEquals(testUser.getUserName(), result.getUserName());
        assertEquals(testUser.getEmail(), result.getEmail());

        verify(userRepository, times(1)).findByEmail(testEmail);
    }

    @Test
    void findUserByEmail_ShouldThrowException_WhenUserDoesNotExist() {
        // Arrange
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(UsernameNotFoundException.class, () -> {
            userService.findUserByEmail("nonexistent@example.com");
        });

        verify(userRepository, times(1)).findByEmail("nonexistent@example.com");
    }

    @Test
    void findAllUsers_ShouldReturnAllUsers() {
        // Arrange
        when(userRepository.findAll()).thenReturn(testUsers);

        // Act
        List<User> result = userService.findAllUsers();

        // Assert
        assertNotNull(result);
        assertEquals(testUsers.size(), result.size());
        assertEquals(testUsers.get(0).getUserId(), result.get(0).getUserId());
        assertEquals(testUsers.get(1).getUserId(), result.get(1).getUserId());
        assertEquals(testUsers.get(2).getUserId(), result.get(2).getUserId());

        verify(userRepository, times(1)).findAll();
    }

//    @Test
//    void updatePassword_ShouldEncodeAndUpdatePassword() {
//        // Arrange
//        when(passwordEncoder.encode(anyString())).thenReturn(testEncodedPassword);
//        when(userRepository.save(any(User.class))).thenReturn(testUser);
//
//        // Act
//        userService.updatePassword(testUser, testNewPassword);
//
//        // Assert
//        assertEquals(testEncodedPassword, testUser.getPassword());
//
//        verify(passwordEncoder, times(1)).encode(testNewPassword);
//        verify(userRepository, times(1)).save(testUser);
//    }

    @Test
    void findUserByEmail_ShouldThrowException_WhenEmailIsNull() {
        // Act & Assert
        assertThrows(UsernameNotFoundException.class, () -> {
            userService.findUserByEmail(null);
        });

        verify(userRepository, times(1)).findByEmail(null);
    }

    @Test
    void findAllUsers_ShouldReturnEmptyList_WhenNoUsers() {
        // Arrange
        when(userRepository.findAll()).thenReturn(Collections.emptyList());

        // Act
        List<User> result = userService.findAllUsers();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(userRepository, times(1)).findAll();
    }

//    @Test
//    void updatePassword_ShouldHandleEmptyPassword() {
//        // Arrange
//        String emptyPassword = "";
//        when(passwordEncoder.encode(anyString())).thenReturn(emptyPassword);
//        when(userRepository.save(any(User.class))).thenReturn(testUser);
//
//        // Act
//        userService.updatePassword(testUser, emptyPassword);
//
//        // Assert
//        assertEquals(emptyPassword, testUser.getPassword());
//
//        verify(passwordEncoder, times(1)).encode(emptyPassword);
//        verify(userRepository, times(1)).save(testUser);
//    }
}