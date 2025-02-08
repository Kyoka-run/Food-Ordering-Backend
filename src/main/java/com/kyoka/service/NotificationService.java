package com.kyoka.service;

import com.kyoka.dto.NotificationDTO;

import java.util.List;

public interface NotificationService {
    List<NotificationDTO> findUsersNotification();
}
