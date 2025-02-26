package com.kyoka.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kyoka.util.AuthUtil;
import com.kyoka.dto.CartDTO;
import com.kyoka.dto.CartItemDTO;
import com.kyoka.model.User;
import com.kyoka.service.CartService;
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

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class CartControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CartService cartService;

    @MockitoBean
    private AuthUtil authUtil;

    @Autowired
    private ObjectMapper objectMapper;

    private User testUser;
    private CartDTO testCartDTO;
    private CartItemDTO testCartItemDTO;

    @BeforeEach
    void setUp() {
        // Set up test user
        testUser = new User();
        testUser.setUserId(1L);
        testUser.setUserName("testUser");
        testUser.setEmail("test@example.com");

        // Set up test cart item DTO
        testCartItemDTO = new CartItemDTO();
        testCartItemDTO.setCartItemId(1L);
        testCartItemDTO.setFoodId(1L);
        testCartItemDTO.setFoodName("Test Food");
        testCartItemDTO.setFoodImage("test-image.jpg");
        testCartItemDTO.setFoodRestaurantId(1L);
        testCartItemDTO.setQuantity(2);
        testCartItemDTO.setTotalPrice(20L);
        testCartItemDTO.setIngredients(Arrays.asList("ingredient1", "ingredient2"));

        // Set up test cart DTO
        testCartDTO = new CartDTO();
        testCartDTO.setCartId(1L);
        testCartDTO.setUserId(1L);
        testCartDTO.setItems(Arrays.asList(testCartItemDTO));
        testCartDTO.setTotal(20.0);
    }

    @Test
    @WithMockUser
    void addItemToCart_ShouldReturnAddedCartItem() throws Exception {
        // Create a request cart item DTO
        CartItemDTO requestDto = new CartItemDTO();
        requestDto.setFoodId(1L);
        requestDto.setQuantity(2);
        requestDto.setIngredients(Arrays.asList("ingredient1", "ingredient2"));

        when(cartService.addItemToCart(any(CartItemDTO.class))).thenReturn(testCartItemDTO);

        mockMvc.perform(put("/api/cart/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cartItemId", is(1)))
                .andExpect(jsonPath("$.foodId", is(1)))
                .andExpect(jsonPath("$.quantity", is(2)))
                .andExpect(jsonPath("$.totalPrice", is(20)));
    }

    @Test
    @WithMockUser
    void updateCartItemQuantity_ShouldReturnUpdatedCartItem() throws Exception {
        // Create a cart item with updated quantity
        CartItemDTO updatedCartItemDTO = new CartItemDTO();
        updatedCartItemDTO.setCartItemId(1L);
        updatedCartItemDTO.setFoodId(1L);
        updatedCartItemDTO.setQuantity(3);
        updatedCartItemDTO.setTotalPrice(30L);

        when(cartService.updateCartItemQuantity(anyLong(), anyInt())).thenReturn(updatedCartItemDTO);

        mockMvc.perform(put("/api/cart-item/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedCartItemDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cartItemId", is(1)))
                .andExpect(jsonPath("$.quantity", is(3)))
                .andExpect(jsonPath("$.totalPrice", is(30)));
    }

    @Test
    @WithMockUser
    void removeItemFromCart_ShouldReturnUpdatedCart() throws Exception {
        // Create a cart with no items (after removal)
        CartDTO emptyCartDTO = new CartDTO();
        emptyCartDTO.setCartId(1L);
        emptyCartDTO.setUserId(1L);
        emptyCartDTO.setItems(new ArrayList<>());
        emptyCartDTO.setTotal(0.0);

        when(cartService.removeItemFromCart(anyLong())).thenReturn(emptyCartDTO);

        mockMvc.perform(delete("/api/cart-item/{id}/remove", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cartId", is(1)))
                .andExpect(jsonPath("$.items", hasSize(0)))
                .andExpect(jsonPath("$.total", is(0.0)));
    }

    @Test
    @WithMockUser
    void findUserCart_ShouldReturnCart() throws Exception {
        when(cartService.findCartByUserId()).thenReturn(testCartDTO);

        mockMvc.perform(get("/api/cart"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cartId", is(1)))
                .andExpect(jsonPath("$.userId", is(1)))
                .andExpect(jsonPath("$.items", hasSize(1)))
                .andExpect(jsonPath("$.items[0].foodName", is("Test Food")))
                .andExpect(jsonPath("$.total", is(20.0)));
    }

    @Test
    @WithMockUser
    void clearCart_ShouldReturnEmptyCart() throws Exception {
        // Create an empty cart (after clearing)
        CartDTO emptyCartDTO = new CartDTO();
        emptyCartDTO.setCartId(1L);
        emptyCartDTO.setUserId(1L);
        emptyCartDTO.setItems(new ArrayList<>());
        emptyCartDTO.setTotal(0.0);

        when(cartService.clearCart()).thenReturn(emptyCartDTO);

        mockMvc.perform(put("/api/cart/clear"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cartId", is(1)))
                .andExpect(jsonPath("$.items", hasSize(0)))
                .andExpect(jsonPath("$.total", is(0.0)));
    }
}