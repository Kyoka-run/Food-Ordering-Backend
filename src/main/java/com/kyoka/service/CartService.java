package com.kyoka.service;

import com.kyoka.dto.AddCartItemRequest;
import com.kyoka.dto.CartDTO;
import com.kyoka.dto.CartItemDTO;

public interface CartService {
    CartItemDTO addItemToCart(AddCartItemRequest request);

    CartItemDTO updateCartItemQuantity(Long cartItemId, int quantity);

    CartDTO removeItemFromCart(Long cartItemId);

    CartDTO findCartByUserId();

    CartDTO clearCart();
}
