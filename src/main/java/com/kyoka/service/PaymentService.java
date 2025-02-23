package com.kyoka.service;

import com.kyoka.dto.PaymentResponse;
import com.kyoka.model.Order;
import com.stripe.exception.StripeException;

public interface PaymentService {
    public PaymentResponse generatePaymentLink(Order order) throws StripeException;
}

