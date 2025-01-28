package com.kyoka.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderDTO {
    private Long orderId;
    private Long userId;
    private Long restaurantId;
    private String restaurantName;
    private List<OrderItemDTO> items;
    private Double totalAmount;
    private String orderStatus;
    private String paymentStatus;
    private Long addressId;
    private String paymentMethod;
}
