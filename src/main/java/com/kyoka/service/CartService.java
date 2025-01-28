package com.kyoka.service;

import com.kyoka.dto.request.AddCartItemRequest;
import com.kyoka.dto.request.CartDTO;
import com.kyoka.dto.request.CartItemDTO;

public interface CartService {
    CartItemDTO addItemToCart(AddCartItemRequest request);

    CartItemDTO updateCartItemQuantity(Long cartItemId, int quantity);

    CartDTO removeItemFromCart(Long cartItemId);

    Double calculateCartTotal(Long cartId);

    CartDTO findCartByUserId(Long userId);

    CartDTO clearCart(Long userId);
}
