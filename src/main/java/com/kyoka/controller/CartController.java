package com.kyoka.controller;

import com.kyoka.dto.request.AddCartItemRequest;
import com.kyoka.dto.request.CartDTO;
import com.kyoka.dto.request.CartItemDTO;
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
    public ResponseEntity<CartItemDTO> addItemToCart(@RequestBody AddCartItemRequest req) {
        CartItemDTO cartItemDTO = cartService.addItemToCart(req);
        return ResponseEntity.ok(cartItemDTO);
    }

    @PutMapping("/cart-item/update")
    public ResponseEntity<CartItemDTO> updateCartItemQuantity(
            @RequestParam Long cartItemId,
            @RequestParam int quantity) {
        CartItemDTO cartItemDTO = cartService.updateCartItemQuantity(cartItemId, quantity);
        return ResponseEntity.ok(cartItemDTO);
    }

    @DeleteMapping("/cart-item/{id}/remove")
    public ResponseEntity<CartDTO> removeItemFromCart(@PathVariable Long id) {
        CartDTO cart = cartService.removeItemFromCart(id);
        return ResponseEntity.ok(cart);
    }

    @GetMapping("/cart/total")
    public ResponseEntity<Double> calculateCartTotals(@RequestParam Long cartId) {
        Double total = cartService.calculateCartTotal(cartId);
        return ResponseEntity.ok(total);
    }

    @GetMapping("/cart/")
    public ResponseEntity<CartDTO> findUserCart(@RequestParam Long userId) {
        CartDTO cartDTO = cartService.findCartByUserId(userId);
        return ResponseEntity.ok(cartDTO);
    }

    @PutMapping("/cart/clear")
    public ResponseEntity<CartDTO> clearCart(@RequestParam Long userId) {
        CartDTO cartDTO = cartService.clearCart(userId);
        return ResponseEntity.ok(cartDTO);
    }
}