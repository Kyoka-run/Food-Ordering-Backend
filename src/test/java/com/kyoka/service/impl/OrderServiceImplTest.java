package com.kyoka.service.impl;

import com.kyoka.util.AuthUtil;
import com.kyoka.dto.*;
import com.kyoka.exception.APIException;
import com.kyoka.exception.ResourceNotFoundException;
import com.kyoka.model.*;
import com.kyoka.repository.*;
import com.kyoka.service.PaymentService;
import com.stripe.exception.StripeException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OrderServiceImplTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private RestaurantRepository restaurantRepository;

    @Mock
    private FoodRepository foodRepository;

    @Mock
    private AddressRepository addressRepository;

    @Mock
    private AuthUtil authUtil;

    @Mock
    private PaymentService paymentService;

    @Spy
    private ModelMapper modelMapper = new ModelMapper();

    @InjectMocks
    private OrderServiceImpl orderService;

    private User testUser;
    private Restaurant testRestaurant;
    private Order testOrder;
    private Address testAddress;
    private Food testFood;
    private OrderItem testOrderItem;
    private CreateOrderRequest testOrderRequest;

    @BeforeEach
    void setUp() {
        // Set up test User
        testUser = new User();
        testUser.setUserId(1L);
        testUser.setUserName("testUser");
        testUser.setEmail("test@example.com");

        // Set up test Restaurant
        testRestaurant = new Restaurant();
        testRestaurant.setRestaurantId(1L);
        testRestaurant.setName("Test Restaurant");
        testRestaurant.setOpen(true);

        // Set up test Address
        testAddress = new Address();
        testAddress.setAddressId(1L);
        testAddress.setStreet("123 Test St");
        testAddress.setCity("Test City");
        testAddress.setCountry("Test Country");
        testAddress.setPostalCode("12345");
        testAddress.setUser(testUser);

        // Set up test Food
        testFood = new Food();
        testFood.setFoodId(1L);
        testFood.setName("Test Food");
        testFood.setPrice(10L);
        testFood.setRestaurant(testRestaurant);

        // Set up test OrderItem
        testOrderItem = new OrderItem();
        testOrderItem.setOrderItemId(1L);
        testOrderItem.setFood(testFood);
        testOrderItem.setQuantity(2);
        testOrderItem.setTotalPrice(20.0);

        // Set up test Order
        testOrder = new Order();
        testOrder.setOrderId(1L);
        testOrder.setUser(testUser);
        testOrder.setRestaurant(testRestaurant);
        testOrder.setDeliveryAddress(testAddress);
        testOrder.setOrderStatus("PENDING");
        testOrder.setCreatedAt(new Date());
        testOrder.setTotalAmount(20.0);
        testOrder.setItems(Collections.singletonList(testOrderItem));

        // Set up test CreateOrderRequest
        testOrderRequest = new CreateOrderRequest();
        testOrderRequest.setRestaurantId(1L);
        testOrderRequest.setAddressId(1L);
        testOrderRequest.setAmount(20.0);
        testOrderRequest.setPaymentMethod("CARD");
    }

    @Test
    void createOrder_ShouldCreateOrderAndReturnPaymentLink() throws StripeException {
        // Arrange
        PaymentResponse expectedResponse = new PaymentResponse();
        expectedResponse.setPayment_url("https://test-payment-url.com");

        when(authUtil.loggedInUser()).thenReturn(testUser);
        when(restaurantRepository.findById(anyLong())).thenReturn(Optional.of(testRestaurant));
        when(addressRepository.findById(anyLong())).thenReturn(Optional.of(testAddress));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);
        when(paymentService.generatePaymentLink(any(Order.class))).thenReturn(expectedResponse);

        // Act
        PaymentResponse result = orderService.createOrder(testOrderRequest);

        // Assert
        assertNotNull(result);
        assertEquals(expectedResponse.getPayment_url(), result.getPayment_url());

        verify(authUtil, times(1)).loggedInUser();
        verify(restaurantRepository, times(1)).findById(testOrderRequest.getRestaurantId());
        verify(addressRepository, times(1)).findById(testOrderRequest.getAddressId());
        verify(orderRepository, times(1)).save(any(Order.class));
        verify(paymentService, times(1)).generatePaymentLink(any(Order.class));
    }

    @Test
    void createOrder_ShouldThrowException_WhenRestaurantNotFound() {
        // Arrange
        when(authUtil.loggedInUser()).thenReturn(testUser);
        when(restaurantRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            orderService.createOrder(testOrderRequest);
        });

        verify(authUtil, times(1)).loggedInUser();
        verify(restaurantRepository, times(1)).findById(testOrderRequest.getRestaurantId());
        verify(addressRepository, never()).findById(anyLong());
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void createOrder_ShouldThrowException_WhenRestaurantClosed() {
        // Arrange
        Restaurant closedRestaurant = new Restaurant();
        closedRestaurant.setRestaurantId(1L);
        closedRestaurant.setName("Closed Restaurant");
        closedRestaurant.setOpen(false);

        when(authUtil.loggedInUser()).thenReturn(testUser);
        when(restaurantRepository.findById(anyLong())).thenReturn(Optional.of(closedRestaurant));

        // Act & Assert
        assertThrows(APIException.class, () -> {
            orderService.createOrder(testOrderRequest);
        });

        verify(authUtil, times(1)).loggedInUser();
        verify(restaurantRepository, times(1)).findById(testOrderRequest.getRestaurantId());
        verify(addressRepository, never()).findById(anyLong());
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void createOrder_ShouldThrowException_WhenAddressNotFound() {
        // Arrange
        when(authUtil.loggedInUser()).thenReturn(testUser);
        when(restaurantRepository.findById(anyLong())).thenReturn(Optional.of(testRestaurant));
        when(addressRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            orderService.createOrder(testOrderRequest);
        });

        verify(authUtil, times(1)).loggedInUser();
        verify(restaurantRepository, times(1)).findById(testOrderRequest.getRestaurantId());
        verify(addressRepository, times(1)).findById(testOrderRequest.getAddressId());
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void updateOrderStatus_ShouldUpdateAndReturnOrder_WhenValidStatus() {
        // Arrange
        String newStatus = "PREPARING";
        Order updatedOrder = new Order();
        updatedOrder.setOrderId(1L);
        updatedOrder.setOrderStatus(newStatus);
        updatedOrder.setUser(testUser);
        updatedOrder.setRestaurant(testRestaurant);

        when(orderRepository.findById(anyLong())).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(updatedOrder);

        // Act
        OrderDTO result = orderService.updateOrderStatus(1L, newStatus);

        // Assert
        assertNotNull(result);
        assertEquals(newStatus, result.getOrderStatus());

        verify(orderRepository, times(1)).findById(1L);
        verify(orderRepository, times(1)).save(testOrder);
    }

    @Test
    void updateOrderStatus_ShouldThrowException_WhenOrderNotFound() {
        // Arrange
        when(orderRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            orderService.updateOrderStatus(999L, "PREPARING");
        });

        verify(orderRepository, times(1)).findById(999L);
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void updateOrderStatus_ShouldThrowException_WhenInvalidStatus() {
        // Arrange
        String invalidStatus = "INVALID_STATUS";

        when(orderRepository.findById(anyLong())).thenReturn(Optional.of(testOrder));

        // Act & Assert
        assertThrows(APIException.class, () -> {
            orderService.updateOrderStatus(1L, invalidStatus);
        });

        verify(orderRepository, times(1)).findById(1L);
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void getUserOrders_ShouldReturnUserOrders() {
        // Arrange
        List<Order> userOrders = Arrays.asList(
                testOrder,
                createOrder(2L, "DELIVERED")
        );

        when(authUtil.loggedInUserId()).thenReturn(1L);
        when(orderRepository.findAllUserOrders(anyLong())).thenReturn(userOrders);

        // Act
        List<OrderDTO> result = orderService.getUserOrders();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("PENDING", result.get(0).getOrderStatus());
        assertEquals("DELIVERED", result.get(1).getOrderStatus());

        verify(authUtil, times(1)).loggedInUserId();
        verify(orderRepository, times(1)).findAllUserOrders(1L);
    }

    @Test
    void getRestaurantOrders_ShouldReturnAllRestaurantOrders_WhenNoStatusFilter() {
        // Arrange
        List<Order> restaurantOrders = Arrays.asList(
                testOrder,
                createOrder(2L, "PREPARING"),
                createOrder(3L, "DELIVERED")
        );

        when(orderRepository.findOrdersByRestaurantId(anyLong())).thenReturn(restaurantOrders);

        // Act
        List<OrderDTO> result = orderService.getRestaurantOrders(1L, null);

        // Assert
        assertNotNull(result);
        assertEquals(3, result.size());

        verify(orderRepository, times(1)).findOrdersByRestaurantId(1L);
    }

    @Test
    void getRestaurantOrders_ShouldFilterByStatus_WhenStatusProvided() {
        // Arrange
        String statusFilter = "PENDING";
        List<Order> allRestaurantOrders = Arrays.asList(
                testOrder, // PENDING
                createOrder(2L, "PREPARING"),
                createOrder(3L, "DELIVERED")
        );

        when(orderRepository.findOrdersByRestaurantId(anyLong())).thenReturn(allRestaurantOrders);

        // Act
        List<OrderDTO> result = orderService.getRestaurantOrders(1L, statusFilter);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(statusFilter, result.get(0).getOrderStatus());

        verify(orderRepository, times(1)).findOrdersByRestaurantId(1L);
    }

    @Test
    void cancelOrder_ShouldCancelOrder_WhenOrderIsPending() {
        // Arrange
        when(orderRepository.findById(anyLong())).thenReturn(Optional.of(testOrder));
        doNothing().when(orderRepository).delete(any(Order.class));

        // Act
        orderService.cancelOrder(1L);

        // Assert
        verify(orderRepository, times(1)).findById(1L);
        verify(orderRepository, times(1)).delete(testOrder);
    }

    @Test
    void cancelOrder_ShouldThrowException_WhenOrderNotFound() {
        // Arrange
        when(orderRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            orderService.cancelOrder(999L);
        });

        verify(orderRepository, times(1)).findById(999L);
        verify(orderRepository, never()).delete(any(Order.class));
    }

    @Test
    void cancelOrder_ShouldThrowException_WhenOrderNotInPendingStatus() {
        // Arrange
        Order nonPendingOrder = new Order();
        nonPendingOrder.setOrderId(1L);
        nonPendingOrder.setOrderStatus("PREPARING");

        when(orderRepository.findById(anyLong())).thenReturn(Optional.of(nonPendingOrder));

        // Act & Assert
        assertThrows(APIException.class, () -> {
            orderService.cancelOrder(1L);
        });

        verify(orderRepository, times(1)).findById(1L);
        verify(orderRepository, never()).delete(any(Order.class));
    }

    @Test
    void createOrderItem_ShouldCreateAndReturnOrderItem() {
        // Arrange
        CreateOrderItemRequest request = new CreateOrderItemRequest();
        request.setFoodId(1L);
        request.setQuantity(2);
        request.setIngredients(Arrays.asList("ingredient1", "ingredient2"));

        // Act
        OrderItemDTO result = orderService.createOrderIem(request);

        // Assert
        assertNotNull(result);
        assertEquals(request.getQuantity(), result.getQuantity());
    }

    @Test
    void isValidOrderStatus_ShouldReturnTrue_ForValidStatuses() {
        // This tests a private method using reflection
        try {
            java.lang.reflect.Method method = OrderServiceImpl.class.getDeclaredMethod(
                    "isValidOrderStatus", String.class);
            method.setAccessible(true);

            // Test all valid statuses
            assertTrue((Boolean) method.invoke(orderService, "PENDING"));
            assertTrue((Boolean) method.invoke(orderService, "PREPARING"));
            assertTrue((Boolean) method.invoke(orderService, "READY"));
            assertTrue((Boolean) method.invoke(orderService, "OUT_FOR_DELIVERY"));
            assertTrue((Boolean) method.invoke(orderService, "DELIVERED"));
            assertTrue((Boolean) method.invoke(orderService, "CANCELLED"));
        } catch (Exception e) {
            fail("Exception thrown while testing private method: " + e.getMessage());
        }
    }

    private Order createOrder(Long id, String status) {
        Order order = new Order();
        order.setOrderId(id);
        order.setUser(testUser);
        order.setRestaurant(testRestaurant);
        order.setDeliveryAddress(testAddress);
        order.setOrderStatus(status);
        order.setCreatedAt(new Date());
        order.setTotalAmount(30.0);
        order.setItems(Collections.singletonList(testOrderItem));
        return order;
    }
}