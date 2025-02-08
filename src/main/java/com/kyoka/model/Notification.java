package com.kyoka.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long notificationId;

    @ManyToOne
    @JoinColumn(name = "recipient_id")
    private User customer;

    @ManyToOne
    private Restaurant restaurant;

    private String message;

    @Temporal(TemporalType.TIMESTAMP)
    private Date sentAt;

    private boolean readStatus;
}
