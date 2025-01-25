package com.kyoka.service;

import java.util.List;

import com.kyoka.exception.ResourceNotFoundException;
import com.kyoka.model.User;

public interface UserService {
    public User findUserByEmail(String email) throws ResourceNotFoundException;

    public List<User> findAllUsers();

    public List<User> getPenddingRestaurantOwner();

    void updatePassword(User user, String newPassword);

    void sendPasswordResetEmail(User user);
}