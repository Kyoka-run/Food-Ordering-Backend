package com.kyoka.service.impl;

import com.kyoka.Util.AuthUtil;
import com.kyoka.dto.AddCartItemRequest;
import com.kyoka.dto.CartDTO;
import com.kyoka.dto.CartItemDTO;
import com.kyoka.exception.ResourceNotFoundException;
import com.kyoka.model.Cart;
import com.kyoka.model.CartItem;
import com.kyoka.model.Food;
import com.kyoka.model.User;
import com.kyoka.repository.CartItemRepository;
import com.kyoka.repository.CartRepository;
import com.kyoka.repository.FoodRepository;
import com.kyoka.service.CartService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CartServiceImpl implements CartService {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private FoodRepository foodRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private AuthUtil authUtil;

    @Override
    public CartItemDTO addItemToCart(AddCartItemRequest request) {
        User user = authUtil.loggedInUser();

        Food food = foodRepository.findById(request.getFoodId())
                .orElseThrow(() -> new ResourceNotFoundException("Food", "id", request.getFoodId()));

        Cart cart = cartRepository.findCartByUserId(user.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Cart", "user id", user.getUserId()));

        // Check if item already exists in cart
        Optional<CartItem> existingItem = cart.getItems().stream()
                .filter(item -> item.getFood().getFoodId().equals(food.getFoodId()))
                .findFirst();

        if (existingItem.isPresent()) {
            // Update quantity if item exists
            CartItem item = existingItem.get();
            int newQuantity = item.getQuantity() + request.getQuantity();
            return updateCartItemQuantity(item.getCartItemId(), newQuantity);
        }

        // Create new cart item if it doesn't exist
        CartItem newItem = new CartItem();
        newItem.setFood(food);
        newItem.setQuantity(request.getQuantity());
        newItem.setCart(cart);
        newItem.setIngredients(request.getIngredients());
        newItem.setTotalPrice(request.getQuantity() * food.getPrice());

        CartItem savedItem = cartItemRepository.save(newItem);
        cart.getItems().add(savedItem);
        cartRepository.save(cart);

        return modelMapper.map(savedItem, CartItemDTO.class);
    }

    @Override
    public CartItemDTO updateCartItemQuantity(Long cartItemId, int quantity) {
        CartItem item = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new ResourceNotFoundException("CartItem", "id", cartItemId));

        item.setQuantity(quantity);
        item.setTotalPrice(quantity * item.getFood().getPrice());
        CartItem updatedItem = cartItemRepository.save(item);

        return modelMapper.map(updatedItem, CartItemDTO.class);
    }

    @Override
    public CartDTO removeItemFromCart(Long cartItemId) {
        User user = authUtil.loggedInUser();

        Cart cart = cartRepository.findCartByUserId(user.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Cart", "user id", user.getUserId()));

        CartItem item = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new ResourceNotFoundException("CartItem", "id", cartItemId));

        cart.getItems().remove(item);
        cartItemRepository.delete(item);
        Cart updatedCart = cartRepository.save(cart);

        return modelMapper.map(updatedCart, CartDTO.class);
    }

    @Override
    public CartDTO findCartByUserId() {
        User user = authUtil.loggedInUser();
        Long userId = user.getUserId();
        Cart cart = cartRepository.findCartByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart", "user id", userId));
        return modelMapper.map(cart, CartDTO.class);
    }

    @Override
    public CartDTO clearCart() {
        Long userId = authUtil.loggedInUser().getUserId();
        Cart cart = cartRepository.findCartByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart", "user id", userId));

        cart.getItems().clear();
        Cart clearedCart = cartRepository.save(cart);

        return modelMapper.map(clearedCart, CartDTO.class);
    }
}