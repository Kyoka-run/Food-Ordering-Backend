package com.kyoka.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CartItemDTO {
    private Long cartItemId;
    private Long foodId;
    private String foodImage;
    private String foodName;
    private int quantity;
    private Long totalPrice;
    private List<String> ingredients;
}

