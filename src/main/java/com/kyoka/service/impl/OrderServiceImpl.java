package com.kyoka.service.impl;

import com.kyoka.util.AuthUtil;
import com.kyoka.dto.*;
import com.kyoka.exception.APIException;
import com.kyoka.exception.ResourceNotFoundException;
import com.kyoka.model.*;
import com.kyoka.repository.*;
import com.kyoka.service.OrderService;
import com.kyoka.service.PaymentService;
import com.stripe.exception.StripeException;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private RestaurantRepository restaurantRepository;

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private AuthUtil authUtil;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private PaymentService paymentService;

    @Override
    @Transactional
    public PaymentResponse createOrder(OrderDTO orderDTO) throws StripeException {
        User user = authUtil.loggedInUser();

        Restaurant restaurant = restaurantRepository.findById(orderDTO.getRestaurantId())
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant", "id", orderDTO.getRestaurantId()));

        if (!restaurant.isOpen()) {
            throw new APIException("Restaurant is currently closed");
        }

        Address address = addressRepository.findById(orderDTO.getAddressId())
                .orElseThrow(() -> new ResourceNotFoundException("Address", "id", orderDTO.getAddressId()));

        Order order = new Order();
        order.setUser(user);
        order.setRestaurant(restaurant);
        order.setDeliveryAddress(address);
        order.setOrderStatus("PENDING");
        order.setCreatedAt(new Date());

        System.out.println(orderDTO.getItems());

        List<OrderItem> orderItems = orderDTO.getItems().stream()
                .map(orderItemDTO -> modelMapper.map(orderItemDTO, OrderItem.class))
                .toList();

        order.setAmount(orderDTO.getAmount());
        order.setItems(orderItems);

        Order savedOrder = orderRepository.save(order);

        return paymentService.generatePaymentLink(savedOrder);
    }

    @Override
    public OrderDTO updateOrderStatus(Long orderId, String status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        if (!isValidOrderStatus(status)) {
            throw new APIException("Invalid order status: " + status);
        }

        order.setOrderStatus(status);
        Order updatedOrder = orderRepository.save(order);
        return modelMapper.map(updatedOrder, OrderDTO.class);
    }

    @Override
    public List<OrderDTO> getUserOrders() {
        Long userId = authUtil.loggedInUserId();
        List<Order> orders = orderRepository.findAllUserOrders(userId);
        return orders.stream()
                .map(order -> modelMapper.map(order, OrderDTO.class))
                .toList();
    }

    @Override
    public List<OrderDTO> getRestaurantOrders(Long restaurantId, String orderStatus) {
        List<Order> orders = orderRepository.findOrdersByRestaurantId(restaurantId);

        if (orderStatus != null && !orderStatus.isEmpty()) {
            orders = orders.stream()
                    .filter(order -> order.getOrderStatus().equals(orderStatus))
                    .toList();
        }

        return orders.stream()
                .map(order -> modelMapper.map(order, OrderDTO.class))
                .toList();
    }

    @Override
    @Transactional
    public void cancelOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        if (!order.getOrderStatus().equals("PENDING")) {
            throw new APIException("Cannot cancel order in " + order.getOrderStatus() + " status");
        }

        orderRepository.delete(order);
    }

    @Override
    public OrderItemDTO createOrderItem(OrderItemDTO orderItemDTO) {
        OrderItem orderItem = new OrderItem();
        orderItem.setQuantity(orderItemDTO.getQuantity());
        return modelMapper.map(orderItem, OrderItemDTO.class);
    }

    private boolean isValidOrderStatus(String status) {
        return status != null && (
                status.equals("PENDING") ||
                        status.equals("COMPLETED") ||
                        status.equals("CANCELLED")
        );
    }
}