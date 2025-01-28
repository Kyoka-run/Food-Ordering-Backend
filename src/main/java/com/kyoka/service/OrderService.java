package com.kyoka.service;

import com.kyoka.dto.request.CreateOrderItemRequest;
import com.kyoka.dto.request.CreateOrderRequest;
import com.kyoka.dto.request.OrderDTO;
import com.kyoka.dto.request.OrderItemDTO;

import java.util.List;

public interface OrderService {
    OrderDTO createOrder(CreateOrderRequest request);

    OrderDTO updateOrderStatus(Long orderId, String status);

    List<OrderDTO> getUserOrders(Long userId);

    List<OrderDTO> getRestaurantOrders(Long restaurantId, String orderStatus);

    void cancelOrder(Long orderId);

    OrderItemDTO createOrderIem (CreateOrderItemRequest orderItemDTO);
}
