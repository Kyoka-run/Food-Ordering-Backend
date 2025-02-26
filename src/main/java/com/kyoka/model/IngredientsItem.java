package com.kyoka.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class IngredientsItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long ingredientsItemId;

    private String name;

    @ManyToOne
    private IngredientCategory ingredientCategory;

    @JsonIgnore
    @ManyToOne
    private Restaurant restaurant;

    private boolean inStock = true;
}
