package com.kyoka.service;

import java.util.List;

import com.kyoka.exception.ResourceNotFoundException;
import com.kyoka.model.User;

public interface UserService {
    User findUserByEmail(String email) throws ResourceNotFoundException;

    List<User> findAllUsers();

    void updatePassword(User user, String newPassword);

    void sendPasswordResetEmail(User user);
}