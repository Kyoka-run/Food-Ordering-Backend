package com.kyoka.service;

import java.util.List;

import com.kyoka.dto.UserDTO;
import com.kyoka.exception.ResourceNotFoundException;

public interface UserService {
    UserDTO findUserByEmail(String email);

    List<UserDTO> findAllUsers();

//    void updatePassword(User user, String newPassword);

//    void sendPasswordResetEmail(User user);
}