package com.kyoka.service;

import com.kyoka.dto.CreateOrderItemRequest;
import com.kyoka.dto.CreateOrderRequest;
import com.kyoka.dto.OrderDTO;
import com.kyoka.dto.OrderItemDTO;

import java.util.List;

public interface OrderService {
    OrderDTO createOrder(CreateOrderRequest request);

    OrderDTO updateOrderStatus(Long orderId, String status);

    List<OrderDTO> getUserOrders(Long userId);

    List<OrderDTO> getRestaurantOrders(Long restaurantId, String orderStatus);

    void cancelOrder(Long orderId);

    OrderItemDTO createOrderIem (CreateOrderItemRequest orderItemDTO);
}
