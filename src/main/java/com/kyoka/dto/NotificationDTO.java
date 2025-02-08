package com.kyoka.dto;

import com.kyoka.model.Restaurant;
import com.kyoka.model.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NotificationDTO {
    private Long notificationId;
    private User customer;
    private Restaurant restaurant;
    private String message;
    private Date sentAt;
    private boolean readStatus;
}
