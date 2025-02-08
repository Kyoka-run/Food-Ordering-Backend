package com.kyoka.controller;

import com.kyoka.dto.NotificationDTO;
import com.kyoka.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @GetMapping("/notifications")
    public ResponseEntity<List<NotificationDTO>> findUsersNotification() {
        List<NotificationDTO> notificationDTOs = notificationService.findUsersNotification();
        return new ResponseEntity<List<NotificationDTO>>(notificationDTOs, HttpStatus.ACCEPTED);
    }
}

