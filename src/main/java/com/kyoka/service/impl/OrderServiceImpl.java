package com.kyoka.service.impl;

import com.kyoka.Util.AuthUtil;
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
    private FoodRepository foodRepository;

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
    public PaymentResponse createOrder(CreateOrderRequest request) throws StripeException {
        User user = authUtil.loggedInUser();

        Restaurant restaurant = restaurantRepository.findById(request.getRestaurantId())
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant", "id", request.getRestaurantId()));

        if (!restaurant.isOpen()) {
            throw new APIException("Restaurant is currently closed");
        }

        Address address = addressRepository.findById(request.getAddressId())
                .orElseThrow(() -> new ResourceNotFoundException("Address", "id", request.getAddressId()));

        Order order = new Order();
        order.setUser(user);
        order.setRestaurant(restaurant);
        order.setDeliveryAddress(address);
        order.setOrderStatus("PENDING");
        order.setCreatedAt(new Date());

        List<OrderItem> orderItems = new ArrayList<>();

        double totalAmount = 0.0;

        for (CreateOrderItemRequest itemRequest : request.getItems()) {
            Food food = foodRepository.findById(itemRequest.getFoodId())
                    .orElseThrow(() -> new ResourceNotFoundException("Food", "id", itemRequest.getFoodId()));

            if (!food.isAvailable()) {
                throw new APIException("Food item " + food.getName() + " is currently unavailable");
            }

            OrderItem orderItem = new OrderItem();
            orderItem.setFood(food);
            orderItem.setQuantity(itemRequest.getQuantity());
            orderItem.setIngredients(itemRequest.getIngredients());

            double itemTotalPrice = food.getPrice() * itemRequest.getQuantity();
            orderItem.setTotalPrice(itemTotalPrice);

            orderItems.add(orderItem);
            totalAmount += itemTotalPrice;
        }

        order.setItems(orderItems);
        order.setTotalAmount(totalAmount);
        order.setTotalItem(orderItems.size());

        Payment payment = new Payment();
        payment.setPaymentMethod(request.getPaymentMethod());
        payment.setPaymentStatus("PENDING");
        payment.setTotalAmount(totalAmount);
        payment.setCreatedAt(new Date());

        order.setPayment(payment);

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
    public OrderItemDTO createOrderIem(CreateOrderItemRequest request) {
        OrderItem orderItem = new OrderItem();
        orderItem.setQuantity(request.getQuantity());
        return modelMapper.map(orderItem, OrderItemDTO.class);
    }

    private boolean isValidOrderStatus(String status) {
        return status != null && (
                status.equals("PENDING") ||
                        status.equals("PREPARING") ||
                        status.equals("READY") ||
                        status.equals("OUT_FOR_DELIVERY") ||
                        status.equals("DELIVERED") ||
                        status.equals("CANCELLED")
        );
    }
}