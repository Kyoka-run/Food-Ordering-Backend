package com.kyoka.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EventDTO {
    private Long eventId;
    private String name;
    private String description;
    private String image;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String location;
    private Long restaurantId;
}
