package com.kyoka.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Restaurant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long restaurantId;

    @OneToOne
    @JoinColumn(name = "owner_id")
    @JsonIgnore
    private User owner;

    @ManyToMany(mappedBy = "favoriteRestaurants")
    @JsonIgnore
    private List<User> favoritedByUsers = new ArrayList<>();

    private String name;
    private String description;
    private String cuisineType;
    private String restaurantAddress;
    private Double latitude;
    private Double longitude;

    @Embedded
    private ContactInformation contactInformation;

    private String openingHours;

    @JsonIgnore
    @OneToMany(mappedBy = "restaurant",cascade = CascadeType.ALL,orphanRemoval = true)
    private List<Review>reviews = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy="restaurant",cascade=CascadeType.ALL,orphanRemoval = true)
    private List<Order> orders = new ArrayList<>();

    private int numRating;

    @ElementCollection
    @Column(length = 1000)
    private List<String> images;

    private LocalDateTime registrationDate;

    private boolean open;

    @JsonIgnore
    @OneToMany(mappedBy = "restaurant",cascade = CascadeType.ALL)
    private List<Food> foods = new ArrayList<>();

    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL)
    private List<Event> events = new ArrayList<>();
}


