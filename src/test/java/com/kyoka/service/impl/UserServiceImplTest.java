package com.kyoka.service.impl;

import com.kyoka.dto.UserDTO;
import com.kyoka.model.User;
import com.kyoka.repository.PasswordResetTokenRepository;
import com.kyoka.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
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
    private ModelMapper modelMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Mock
    private JavaMailSender javaMailSender;

    @InjectMocks
    private UserServiceImpl userService;

    private User testUser;
    private UserDTO testUserDTO;
    private List<User> testUsers;
    private List<UserDTO> testUserDTOs;
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

        // Set up test UserDTO
        testUserDTO = new UserDTO();
        testUserDTO.setUserId(1L);
        testUserDTO.setUsername("testUser");
        testUserDTO.setEmail(testEmail);
        testUserDTO.setRoles(List.of());

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

        // Set up corresponding DTOs
        UserDTO userDTO2 = new UserDTO();
        userDTO2.setUserId(2L);
        userDTO2.setUsername("user2");
        userDTO2.setEmail("user2@example.com");
        userDTO2.setRoles(List.of());

        UserDTO userDTO3 = new UserDTO();
        userDTO3.setUserId(3L);
        userDTO3.setUsername("user3");
        userDTO3.setEmail("user3@example.com");
        userDTO3.setRoles(List.of());

        testUserDTOs = Arrays.asList(testUserDTO, userDTO2, userDTO3);
    }

    @Test
    void findUserByEmail_ShouldReturnUserDTO_WhenUserExists() {
        // Arrange
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(modelMapper.map(testUser, UserDTO.class)).thenReturn(testUserDTO);

        // Act
        UserDTO result = userService.findUserByEmail(testEmail);

        // Assert
        assertNotNull(result);
        assertEquals(testUserDTO.getUserId(), result.getUserId());
        assertEquals(testUserDTO.getUsername(), result.getUsername());
        assertEquals(testUserDTO.getEmail(), result.getEmail());

        verify(userRepository, times(1)).findByEmail(testEmail);
        verify(modelMapper, times(1)).map(testUser, UserDTO.class);
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
        verify(modelMapper, never()).map(any(), any());
    }

    @Test
    void findAllUsers_ShouldReturnAllUserDTOs() {
        // Arrange
        when(userRepository.findAll()).thenReturn(testUsers);
        when(modelMapper.map(testUsers.get(0), UserDTO.class)).thenReturn(testUserDTOs.get(0));
        when(modelMapper.map(testUsers.get(1), UserDTO.class)).thenReturn(testUserDTOs.get(1));
        when(modelMapper.map(testUsers.get(2), UserDTO.class)).thenReturn(testUserDTOs.get(2));

        // Act
        List<UserDTO> result = userService.findAllUsers();

        // Assert
        assertNotNull(result);
        assertEquals(testUserDTOs.size(), result.size());
        assertEquals(testUserDTOs.get(0).getUserId(), result.get(0).getUserId());
        assertEquals(testUserDTOs.get(1).getUserId(), result.get(1).getUserId());
        assertEquals(testUserDTOs.get(2).getUserId(), result.get(2).getUserId());

        verify(userRepository, times(1)).findAll();
        verify(modelMapper, times(3)).map(any(User.class), eq(UserDTO.class));
    }

    @Test
    void findUserByEmail_ShouldThrowException_WhenEmailIsNull() {
        // Arrange
        when(userRepository.findByEmail(null)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(UsernameNotFoundException.class, () -> {
            userService.findUserByEmail(null);
        });

        verify(userRepository, times(1)).findByEmail(null);
        verify(modelMapper, never()).map(any(), any());
    }

    @Test
    void findAllUsers_ShouldReturnEmptyList_WhenNoUsers() {
        // Arrange
        when(userRepository.findAll()).thenReturn(Collections.emptyList());

        // Act
        List<UserDTO> result = userService.findAllUsers();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(userRepository, times(1)).findAll();
        verify(modelMapper, never()).map(any(), any());
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