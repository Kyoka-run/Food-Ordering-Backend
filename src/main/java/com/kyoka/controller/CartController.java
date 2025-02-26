package com.kyoka.controller;

import com.kyoka.dto.CartDTO;
import com.kyoka.dto.CartItemDTO;
import com.kyoka.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class CartController {

    @Autowired
    private CartService cartService;

    @PutMapping("/cart/add")
    public ResponseEntity<CartItemDTO> addItemToCart(@RequestBody CartItemDTO cartItemDTO) {
        CartItemDTO addedItem = cartService.addItemToCart(cartItemDTO);
        return ResponseEntity.ok(addedItem);
    }

    @PutMapping("/cart-item/update")
    public ResponseEntity<CartItemDTO> updateCartItemQuantity(@RequestBody CartItemDTO cartItemDTO) {
        CartItemDTO updatedCartItemDTO = cartService.updateCartItemQuantity(cartItemDTO.getCartItemId(), cartItemDTO.getQuantity());
        return ResponseEntity.ok(updatedCartItemDTO);
    }

    @DeleteMapping("/cart-item/{id}/remove")
    public ResponseEntity<CartDTO> removeItemFromCart(@PathVariable Long id) {
        CartDTO cart = cartService.removeItemFromCart(id);
        return ResponseEntity.ok(cart);
    }

    @GetMapping("/cart")
    public ResponseEntity<CartDTO> findUserCart() {
        CartDTO cartDTO = cartService.findCartByUserId();
        return ResponseEntity.ok(cartDTO);
    }

    @PutMapping("/cart/clear")
    public ResponseEntity<CartDTO> clearCart() {
        CartDTO cartDTO = cartService.clearCart();
        return ResponseEntity.ok(cartDTO);
    }
}