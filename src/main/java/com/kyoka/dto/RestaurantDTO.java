package com.kyoka.dto;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Data;

import java.util.List;

@Data
// This class can embed to other entity
@Embeddable
public class RestaurantDTO {
    private Long id;
    private String title;
    private String description;

    @Column(length = 1000)
    private List<String> images;
}

