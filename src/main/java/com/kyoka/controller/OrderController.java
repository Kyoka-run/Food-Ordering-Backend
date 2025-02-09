package com.kyoka.controller;

import com.kyoka.dto.CreateOrderRequest;
import com.kyoka.dto.OrderDTO;
import com.kyoka.dto.APIResponse;
import com.kyoka.dto.PaymentResponse;
import com.kyoka.service.OrderService;
import com.stripe.exception.StripeException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @PostMapping("/order")
    public ResponseEntity<PaymentResponse> createOrder(@RequestBody CreateOrderRequest request) throws StripeException {
        PaymentResponse paymentResponse = orderService.createOrder(request);
        return ResponseEntity.ok(paymentResponse);
    }

    @GetMapping("/order/user")
    public ResponseEntity<List<OrderDTO>> getAllUserOrders() {
        List<OrderDTO> orders = orderService.getUserOrders();
        return ResponseEntity.ok(orders);
    }

    @DeleteMapping("/admin/order/{orderId}")
    public ResponseEntity<APIResponse> deleteOrder(@PathVariable Long orderId) {
        if(orderId == null) {
            return new ResponseEntity<>(new APIResponse("Invalid order id", false), HttpStatus.BAD_REQUEST);
        }
        orderService.cancelOrder(orderId);
        return ResponseEntity.ok(new APIResponse("Order deleted with id " + orderId, true));
    }

    @GetMapping("/admin/order/restaurant/{restaurantId}")
    public ResponseEntity<List<OrderDTO>> getAllRestaurantOrders(
            @PathVariable Long restaurantId,
            @RequestParam(required = false) String order_status) {
        List<OrderDTO> orders = orderService.getRestaurantOrders(restaurantId, order_status);

        return ResponseEntity.ok(orders);
    }

    @PutMapping("/admin/orders/{orderId}/{orderStatus}")
    public ResponseEntity<OrderDTO> updateOrders(
            @PathVariable Long orderId,
            @PathVariable String orderStatus) {
        OrderDTO updatedOrder = orderService.updateOrderStatus(orderId, orderStatus);
        return ResponseEntity.ok(updatedOrder);
    }
}