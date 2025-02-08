package com.kyoka.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateOrderRequest {
    private Long restaurantId;
    private List<CreateOrderItemRequest> items;
    private Long addressId;
    private String paymentMethod;
}