package com.kyoka.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kyoka.dto.*;
import com.kyoka.service.OrderService;
import com.stripe.exception.StripeException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class OrderControllerTest {

    @Mock
    private OrderService orderService;

    @InjectMocks
    private OrderController orderController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(orderController).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void createOrder_ShouldReturnPaymentResponse() throws Exception {
        // Arrange
        CreateOrderRequest request = new CreateOrderRequest();
        request.setRestaurantId(1L);
        request.setAmount(100.0);
        request.setAddressId(1L);
        request.setPaymentMethod("CARD");

        PaymentResponse paymentResponse = new PaymentResponse();
        paymentResponse.setPayment_url("https://stripe.com/payment/test");

        when(orderService.createOrder(any(CreateOrderRequest.class))).thenReturn(paymentResponse);

        // Act & Assert
        mockMvc.perform(post("/api/order")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.payment_url").value("https://stripe.com/payment/test"));

        verify(orderService, times(1)).createOrder(any(CreateOrderRequest.class));
    }

    @Test
    void getAllUserOrders_ShouldReturnOrdersList() throws Exception {
        // Arrange
        List<OrderDTO> orders = Arrays.asList(
                createOrderDTO(1L, "PENDING", 100.0),
                createOrderDTO(2L, "DELIVERED", 150.0)
        );

        when(orderService.getUserOrders()).thenReturn(orders);

        // Act & Assert
        mockMvc.perform(get("/api/order/user"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].orderId").value(1))
                .andExpect(jsonPath("$[0].orderStatus").value("PENDING"))
                .andExpect(jsonPath("$[1].orderId").value(2))
                .andExpect(jsonPath("$[1].orderStatus").value("DELIVERED"));

        verify(orderService, times(1)).getUserOrders();
    }

    @Test
    void deleteOrder_WithValidId_ShouldReturnSuccess() throws Exception {
        // Arrange
        Long orderId = 1L;
        doNothing().when(orderService).cancelOrder(orderId);

        // Act & Assert
        mockMvc.perform(delete("/api/admin/order/{orderId}", orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Order deleted with id " + orderId))
                .andExpect(jsonPath("$.status").value(true));

        verify(orderService, times(1)).cancelOrder(orderId);
    }

    @Test
    void getAllRestaurantOrders_ShouldReturnOrdersList() throws Exception {
        // Arrange
        Long restaurantId = 1L;
        String orderStatus = "PENDING";
        List<OrderDTO> orders = Arrays.asList(
                createOrderDTO(1L, orderStatus, 100.0),
                createOrderDTO(2L, orderStatus, 150.0)
        );

        when(orderService.getRestaurantOrders(eq(restaurantId), eq(orderStatus))).thenReturn(orders);

        // Act & Assert
        mockMvc.perform(get("/api/admin/order/restaurant/{restaurantId}", restaurantId)
                        .param("order_status", orderStatus))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].orderStatus").value(orderStatus));

        verify(orderService, times(1)).getRestaurantOrders(restaurantId, orderStatus);
    }

    @Test
    void updateOrders_ShouldReturnUpdatedOrder() throws Exception {
        // Arrange
        Long orderId = 1L;
        String newStatus = "PREPARING";
        OrderDTO updatedOrder = createOrderDTO(orderId, newStatus, 100.0);

        when(orderService.updateOrderStatus(orderId, newStatus)).thenReturn(updatedOrder);

        // Act & Assert
        mockMvc.perform(put("/api/admin/orders/{orderId}/{orderStatus}", orderId, newStatus))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value(orderId))
                .andExpect(jsonPath("$.orderStatus").value(newStatus));

        verify(orderService, times(1)).updateOrderStatus(orderId, newStatus);
    }

    @Test
    void getAllRestaurantOrders_WithoutStatus_ShouldReturnAllOrders() throws Exception {
        // Arrange
        Long restaurantId = 1L;
        List<OrderDTO> orders = Arrays.asList(
                createOrderDTO(1L, "PENDING", 100.0),
                createOrderDTO(2L, "DELIVERED", 150.0)
        );

        when(orderService.getRestaurantOrders(eq(restaurantId), isNull())).thenReturn(orders);

        // Act & Assert
        mockMvc.perform(get("/api/admin/order/restaurant/{restaurantId}", restaurantId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));

        verify(orderService, times(1)).getRestaurantOrders(restaurantId, null);
    }

    private OrderDTO createOrderDTO(Long orderId, String status, Double amount) {
        OrderDTO dto = new OrderDTO();
        dto.setOrderId(orderId);
        dto.setUserId(1L);
        dto.setRestaurantId(1L);
        dto.setRestaurantName("Test Restaurant");
        dto.setOrderStatus(status);
        dto.setTotalAmount(amount);
        dto.setPaymentStatus("PENDING");
        dto.setAddressId(1L);
        dto.setPaymentMethod("CARD");
        dto.setItems(Arrays.asList(createOrderItemDTO(1L), createOrderItemDTO(2L)));
        return dto;
    }

    private OrderItemDTO createOrderItemDTO(Long itemId) {
        OrderItemDTO dto = new OrderItemDTO();
        dto.setOrderItemId(itemId);
        dto.setFoodId(itemId);
        dto.setFoodName("Test Food " + itemId);
        dto.setQuantity(1);
        dto.setTotalPrice(50.0);
        return dto;
    }
}