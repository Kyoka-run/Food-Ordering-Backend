package com.kyoka.service.impl;

import com.kyoka.util.AuthUtil;
import com.kyoka.dto.NotificationDTO;
import com.kyoka.model.Notification;
import com.kyoka.model.User;
import com.kyoka.repository.NotificationRepository;
import com.kyoka.service.NotificationService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationServiceImpl implements NotificationService {
    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private AuthUtil authUtil;

    @Autowired
    private ModelMapper modelMapper;

    @Override
    public List<NotificationDTO> findUsersNotification() {
        User user = authUtil.loggedInUser();
        List<Notification> notifications = notificationRepository.findByCustomerUserId(user.getUserId());
        return notifications.stream()
                .map(notification -> modelMapper.map(notification, NotificationDTO.class))
                .toList();
    }
}
