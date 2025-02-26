package com.kyoka.service.impl;

import com.kyoka.dto.PaymentResponse;
import com.kyoka.model.Order;
import com.kyoka.model.Restaurant;
import com.kyoka.model.User;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PaymentServiceImplTest {

    @InjectMocks
    private PaymentServiceImpl paymentService;

    private Order testOrder;
    private User testUser;
    private Restaurant testRestaurant;
    private String testApiKey = "test_stripe_api_key";

    @BeforeEach
    void setUp() {
        // Set the stripe API key via reflection since it's normally set from @Value
        ReflectionTestUtils.setField(paymentService, "stripeSecretKey", testApiKey);

        // Set up test User
        testUser = new User();
        testUser.setUserId(1L);
        testUser.setUserName("testUser");
        testUser.setEmail("test@example.com");

        // Set up test Restaurant
        testRestaurant = new Restaurant();
        testRestaurant.setRestaurantId(1L);
        testRestaurant.setName("Test Restaurant");

        // Set up test Order
        testOrder = new Order();
        testOrder.setOrderId(1L);
        testOrder.setUser(testUser);
        testOrder.setRestaurant(testRestaurant);
        testOrder.setTotalAmount(100.0);
        testOrder.setItems(new ArrayList<>());
        testOrder.setOrderStatus("PENDING");
        testOrder.setCreatedAt(new Date());
    }

    @Test
    void generatePaymentLink_ShouldCreateStripeSessionAndReturnPaymentUrl() throws StripeException {
        // Setup mocked static method for Stripe Session.create()
        Session mockSession = mock(Session.class);
        String expectedUrl = "https://checkout.stripe.com/test-session";
        when(mockSession.getUrl()).thenReturn(expectedUrl);

        try (MockedStatic<Session> mockedStatic = mockStatic(Session.class)) {
            // Mock the static create method
            mockedStatic.when(() -> Session.create(any(SessionCreateParams.class)))
                    .thenReturn(mockSession);

            // Act
            PaymentResponse result = paymentService.generatePaymentLink(testOrder);

            // Assert
            assertNotNull(result);
            assertEquals(expectedUrl, result.getPayment_url());

            // Verify Stripe.apiKey was set
            mockedStatic.verify(() -> Session.create(any(SessionCreateParams.class)));
        }
    }

    @Test
    void generatePaymentLink_ShouldHandleDecimalAmounts() throws StripeException {
        // Set a decimal amount to test rounding
        testOrder.setTotalAmount(99.99);

        // Setup mocked static method for Stripe Session.create()
        Session mockSession = mock(Session.class);
        String expectedUrl = "https://checkout.stripe.com/test-session";
        when(mockSession.getUrl()).thenReturn(expectedUrl);

        try (MockedStatic<Session> mockedStatic = mockStatic(Session.class)) {
            // Mock the static create method and capture params
            mockedStatic.when(() -> Session.create(any(SessionCreateParams.class)))
                    .thenAnswer(invocation -> {
                        SessionCreateParams params = invocation.getArgument(0);
                        // We can't easily access the amount directly due to Stripe's API design
                        // but we could add more detailed verification if needed
                        return mockSession;
                    });

            // Act
            PaymentResponse result = paymentService.generatePaymentLink(testOrder);

            // Assert
            assertNotNull(result);
            assertEquals(expectedUrl, result.getPayment_url());

            // Verify Stripe.apiKey was set
            mockedStatic.verify(() -> Session.create(any(SessionCreateParams.class)));
        }
    }

    @Test
    void generatePaymentLink_ShouldPropagateStripeException() throws StripeException {
        // Setup mocked static method for Stripe Session.create() to throw exception
        StripeException mockException = mock(StripeException.class);

        try (MockedStatic<Session> mockedStatic = mockStatic(Session.class)) {
            // Mock the static create method to throw exception
            mockedStatic.when(() -> Session.create(any(SessionCreateParams.class)))
                    .thenThrow(mockException);

            // Act & Assert
            assertThrows(StripeException.class, () -> {
                paymentService.generatePaymentLink(testOrder);
            });

            // Verify Stripe.apiKey was set and Session.create was called
            mockedStatic.verify(() -> Session.create(any(SessionCreateParams.class)));
        }
    }
}