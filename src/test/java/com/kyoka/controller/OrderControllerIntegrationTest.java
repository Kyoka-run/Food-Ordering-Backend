package com.kyoka.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kyoka.util.AuthUtil;
import com.kyoka.dto.*;
import com.kyoka.model.User;
import com.kyoka.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class OrderControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OrderService orderService;

    @MockitoBean
    private AuthUtil authUtil;

    @Autowired
    private ObjectMapper objectMapper;

    private User testUser;
    private OrderDTO testOrderDTO;
    private CreateOrderRequest testCreateOrderRequest;
    private PaymentResponse testPaymentResponse;

    @BeforeEach
    void setUp() {
        // Set up test user
        testUser = new User();
        testUser.setUserId(1L);
        testUser.setUserName("testUser");
        testUser.setEmail("test@example.com");

        // Set up test order items
        List<OrderItemDTO> orderItems = new ArrayList<>();
        OrderItemDTO orderItemDTO = new OrderItemDTO();
        orderItemDTO.setOrderItemId(1L);
        orderItemDTO.setFoodId(1L);
        orderItemDTO.setFoodName("Test Food");
        orderItemDTO.setQuantity(2);
        orderItemDTO.setTotalPrice(20.0);
        orderItems.add(orderItemDTO);

        // Set up test order DTO
        testOrderDTO = new OrderDTO();
        testOrderDTO.setOrderId(1L);
        testOrderDTO.setUserId(1L);
        testOrderDTO.setRestaurantId(1L);
        testOrderDTO.setRestaurantName("Test Restaurant");
        testOrderDTO.setItems(orderItems);
        testOrderDTO.setTotalAmount(20.0);
        testOrderDTO.setOrderStatus("PENDING");
        testOrderDTO.setPaymentStatus("PENDING");
        testOrderDTO.setAddressId(1L);
        testOrderDTO.setPaymentMethod("CARD");

        // Set up test create order request
        testCreateOrderRequest = new CreateOrderRequest();
        testCreateOrderRequest.setRestaurantId(1L);
        testCreateOrderRequest.setAmount(20.0);
        testCreateOrderRequest.setAddressId(1L);
        testCreateOrderRequest.setPaymentMethod("CARD");

        // Set up test payment response
        testPaymentResponse = new PaymentResponse();
        testPaymentResponse.setPayment_url("https://test-payment-url.com");
    }

    @Test
    @WithMockUser
    void createOrder_ShouldReturnPaymentResponse() throws Exception {
        when(orderService.createOrder(any(CreateOrderRequest.class))).thenReturn(testPaymentResponse);

        mockMvc.perform(post("/api/order")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testCreateOrderRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.payment_url", is("https://test-payment-url.com")));
    }

    @Test
    @WithMockUser
    void getAllUserOrders_ShouldReturnOrdersList() throws Exception {
        List<OrderDTO> orders = Arrays.asList(
                testOrderDTO,
                createOrderDTO(2L, "DELIVERED")
        );

        when(orderService.getUserOrders()).thenReturn(orders);

        mockMvc.perform(get("/api/order/user"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].orderStatus", is("PENDING")))
                .andExpect(jsonPath("$[1].orderStatus", is("DELIVERED")));
    }

    @Test
    @WithMockUser(roles = "RESTAURANT_OWNER")
    void deleteOrder_ShouldReturnSuccessResponse() throws Exception {
        doNothing().when(orderService).cancelOrder(anyLong());

        mockMvc.perform(delete("/api/admin/order/{orderId}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("Order deleted with id 1")))
                .andExpect(jsonPath("$.status", is(true)));
    }

    @Test
    @WithMockUser(roles = "RESTAURANT_OWNER")
    void getAllRestaurantOrders_ShouldReturnOrdersList() throws Exception {
        List<OrderDTO> orders = Arrays.asList(
                testOrderDTO,
                createOrderDTO(2L, "PREPARING")
        );

        when(orderService.getRestaurantOrders(anyLong(), anyString())).thenReturn(orders);

        mockMvc.perform(get("/api/admin/order/restaurant/{restaurantId}", 1L)
                        .param("order_status", "PENDING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].orderStatus", is("PENDING")))
                .andExpect(jsonPath("$[1].orderStatus", is("PREPARING")));
    }

    @Test
    @WithMockUser(roles = "RESTAURANT_OWNER")
    void updateOrders_ShouldReturnUpdatedOrder() throws Exception {
        testOrderDTO.setOrderStatus("PREPARING");

        when(orderService.updateOrderStatus(anyLong(), anyString())).thenReturn(testOrderDTO);

        mockMvc.perform(put("/api/admin/orders/{orderId}/{orderStatus}", 1L, "PREPARING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId", is(1)))
                .andExpect(jsonPath("$.orderStatus", is("PREPARING")));
    }

    @Test
    @WithMockUser(roles = "RESTAURANT_OWNER")
    void deleteOrder_WithInvalidId_ShouldReturnNotFound() throws Exception {
        mockMvc.perform(delete("/api/admin/order/{orderId}", ""))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "RESTAURANT_OWNER")
    void getAllRestaurantOrders_WithoutStatus_ShouldReturnAllOrders() throws Exception {
        List<OrderDTO> orders = Arrays.asList(
                testOrderDTO,
                createOrderDTO(2L, "PREPARING"),
                createOrderDTO(3L, "DELIVERED")
        );

        when(orderService.getRestaurantOrders(anyLong(), isNull())).thenReturn(orders);

        mockMvc.perform(get("/api/admin/order/restaurant/{restaurantId}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)));
    }

    private OrderDTO createOrderDTO(Long id, String status) {
        OrderDTO dto = new OrderDTO();
        dto.setOrderId(id);
        dto.setUserId(1L);
        dto.setRestaurantId(1L);
        dto.setRestaurantName("Test Restaurant");
        dto.setItems(new ArrayList<>());
        dto.setTotalAmount(30.0);
        dto.setOrderStatus(status);
        dto.setPaymentStatus("PENDING");
        dto.setAddressId(1L);
        dto.setPaymentMethod("CARD");
        return dto;
    }
}