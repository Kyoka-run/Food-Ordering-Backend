package com.kyoka.service;

import com.kyoka.dto.*;
import com.stripe.exception.StripeException;

import java.util.List;

public interface OrderService {
    PaymentResponse createOrder(CreateOrderRequest request) throws StripeException;

    OrderDTO updateOrderStatus(Long orderId, String status);

    List<OrderDTO> getUserOrders();

    List<OrderDTO> getRestaurantOrders(Long restaurantId, String orderStatus);

    void cancelOrder(Long orderId);

    OrderItemDTO createOrderIem (CreateOrderItemRequest orderItemDTO);
}
