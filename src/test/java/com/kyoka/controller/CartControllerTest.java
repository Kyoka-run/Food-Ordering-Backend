package com.kyoka.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kyoka.dto.CartDTO;
import com.kyoka.dto.CartItemDTO;
import com.kyoka.service.CartService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class CartControllerTest {

    @Mock
    private CartService cartService;

    @InjectMocks
    private CartController cartController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(cartController).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void addItemToCart_ShouldReturnAddedCartItem() throws Exception {
        // Arrange
        CartItemDTO requestDto = new CartItemDTO();
        requestDto.setFoodId(1L);
        requestDto.setQuantity(2);
        requestDto.setIngredients(Arrays.asList("ingredient1", "ingredient2"));

        CartItemDTO responseDto = new CartItemDTO();
        responseDto.setCartItemId(1L);
        responseDto.setFoodId(1L);
        responseDto.setFoodName("Test Food");
        responseDto.setQuantity(2);
        responseDto.setTotalPrice(20L);
        responseDto.setIngredients(Arrays.asList("ingredient1", "ingredient2"));

        when(cartService.addItemToCart(any(CartItemDTO.class))).thenReturn(responseDto);

        // Act & Assert
        mockMvc.perform(put("/api/cart/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cartItemId").value(1))
                .andExpect(jsonPath("$.foodId").value(1))
                .andExpect(jsonPath("$.quantity").value(2))
                .andExpect(jsonPath("$.totalPrice").value(20))
                .andExpect(jsonPath("$.ingredients[0]").value("ingredient1"))
                .andExpect(jsonPath("$.ingredients[1]").value("ingredient2"));

        verify(cartService, times(1)).addItemToCart(any(CartItemDTO.class));
    }

    @Test
    void updateCartItemQuantity_ShouldReturnUpdatedCartItem() throws Exception {
        // Arrange
        CartItemDTO requestDto = new CartItemDTO();
        requestDto.setCartItemId(1L);
        requestDto.setQuantity(3);

        CartItemDTO responseDto = new CartItemDTO();
        responseDto.setCartItemId(1L);
        responseDto.setFoodId(1L);
        responseDto.setQuantity(3);
        responseDto.setTotalPrice(30L);

        when(cartService.updateCartItemQuantity(eq(1L), eq(3))).thenReturn(responseDto);

        // Act & Assert
        mockMvc.perform(put("/api/cart-item/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cartItemId").value(1))
                .andExpect(jsonPath("$.quantity").value(3))
                .andExpect(jsonPath("$.totalPrice").value(30));

        verify(cartService, times(1)).updateCartItemQuantity(eq(1L), eq(3));
    }

    @Test
    void removeItemFromCart_ShouldReturnUpdatedCart() throws Exception {
        // Arrange
        Long cartItemId = 1L;
        CartDTO responseDto = new CartDTO();
        responseDto.setCartId(1L);
        responseDto.setUserId(1L);
        responseDto.setItems(Arrays.asList(new CartItemDTO()));
        responseDto.setTotal(50.0);

        when(cartService.removeItemFromCart(cartItemId)).thenReturn(responseDto);

        // Act & Assert
        mockMvc.perform(delete("/api/cart-item/{id}/remove", cartItemId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cartId").value(1))
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.total").value(50.0))
                .andExpect(jsonPath("$.items").isArray());

        verify(cartService, times(1)).removeItemFromCart(cartItemId);
    }

    @Test
    void findUserCart_ShouldReturnCart() throws Exception {
        // Arrange
        CartDTO responseDto = new CartDTO();
        responseDto.setCartId(1L);
        responseDto.setUserId(1L);
        responseDto.setItems(Arrays.asList(
                createCartItemDTO(1L, "Food 1", 2, 20L),
                createCartItemDTO(2L, "Food 2", 1, 15L)
        ));
        responseDto.setTotal(55.0);

        when(cartService.findCartByUserId()).thenReturn(responseDto);

        // Act & Assert
        mockMvc.perform(get("/api/cart"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cartId").value(1))
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.total").value(55.0))
                .andExpect(jsonPath("$.items.length()").value(2))
                .andExpect(jsonPath("$.items[0].foodName").value("Food 1"))
                .andExpect(jsonPath("$.items[1].foodName").value("Food 2"));

        verify(cartService, times(1)).findCartByUserId();
    }

    @Test
    void clearCart_ShouldReturnEmptyCart() throws Exception {
        // Arrange
        CartDTO responseDto = new CartDTO();
        responseDto.setCartId(1L);
        responseDto.setUserId(1L);
        responseDto.setItems(List.of());
        responseDto.setTotal(0.0);

        when(cartService.clearCart()).thenReturn(responseDto);

        // Act & Assert
        mockMvc.perform(put("/api/cart/clear"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cartId").value(1))
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.total").value(0.0))
                .andExpect(jsonPath("$.items").isEmpty());

        verify(cartService, times(1)).clearCart();
    }

    private CartItemDTO createCartItemDTO(Long foodId, String foodName, int quantity, Long totalPrice) {
        CartItemDTO dto = new CartItemDTO();
        dto.setFoodId(foodId);
        dto.setFoodName(foodName);
        dto.setQuantity(quantity);
        dto.setTotalPrice(totalPrice);
        return dto;
    }
}