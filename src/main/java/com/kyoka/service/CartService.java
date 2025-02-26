package com.kyoka.service;

import com.kyoka.dto.CartDTO;
import com.kyoka.dto.CartItemDTO;

public interface CartService {
    CartItemDTO addItemToCart(CartItemDTO cartItemDTO);

    CartItemDTO updateCartItemQuantity(Long cartItemId, int quantity);

    CartDTO removeItemFromCart(Long cartItemId);

    CartDTO findCartByUserId();

    CartDTO clearCart();
}
