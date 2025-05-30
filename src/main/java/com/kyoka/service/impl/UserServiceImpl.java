package com.kyoka.service.impl;

import java.util.*;

import com.kyoka.dto.UserDTO;
import com.kyoka.model.PasswordResetToken;
import com.kyoka.model.User;
import com.kyoka.repository.PasswordResetTokenRepository;
import com.kyoka.repository.UserRepository;
import com.kyoka.service.UserService;
import org.modelmapper.ModelMapper;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {
    private UserRepository userRepository;
    private ModelMapper modelMapper;
//    private PasswordEncoder passwordEncoder;
//    private PasswordResetTokenRepository passwordResetTokenRepository;
//    private JavaMailSender javaMailSender;

//    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder, PasswordResetTokenRepository passwordResetTokenRepository, JavaMailSender javaMailSender) {
//        this.userRepository = userRepository;
//        this.passwordEncoder = passwordEncoder;
//        this.passwordResetTokenRepository = passwordResetTokenRepository;
//        this.javaMailSender = javaMailSender;
//    }


    public UserServiceImpl(UserRepository userRepository, ModelMapper modelMapper) {
        this.userRepository = userRepository;
        this.modelMapper = modelMapper;
    }

    @Override
    public List<UserDTO> findAllUsers() {
        List<User> users = userRepository.findAll();
        return users.stream()
                .map(user -> modelMapper.map(user, UserDTO.class))
                .toList();
    }

//    @Override
//    public void updatePassword(User user, String newPassword) {
//        user.setPassword(passwordEncoder.encode(newPassword));
//        userRepository.save(user);
//    }

//    @Override
//    public void sendPasswordResetEmail(User user) {
//        // Generate a random token (you might want to use a library for this)
//        String resetToken = generateRandomToken();
//
//        // Calculate expiry date
//        Date expiryDate = calculateExpiryDate();
//
//        // Save the token in the database
//        PasswordResetToken passwordResetToken = new PasswordResetToken(resetToken,user,expiryDate);
//        passwordResetTokenRepository.save(passwordResetToken);
//
//        // Send an email containing the reset link
//        sendEmail(user.getEmail(), "Password Reset", "Click the following link to reset your password: http://localhost:3000/account/reset-password?token=" + resetToken);
//    }

//    private void sendEmail(String to, String subject, String message) {
//        SimpleMailMessage mailMessage = new SimpleMailMessage();
//
//        mailMessage.setTo(to);
//        mailMessage.setSubject(subject);
//        mailMessage.setText(message);
//
//        javaMailSender.send(mailMessage);
//    }
//
//    private String generateRandomToken() {
//        return UUID.randomUUID().toString();
//    }
//
//    private Date calculateExpiryDate() {
//        Calendar cal = Calendar.getInstance();
//        cal.setTime(new Date());
//        cal.add(Calendar.MINUTE, 10);
//        return cal.getTime();
//    }

    @Override
    public UserDTO findUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User Not Found with email: " + email));

        return modelMapper.map(user, UserDTO.class);
    }
}
