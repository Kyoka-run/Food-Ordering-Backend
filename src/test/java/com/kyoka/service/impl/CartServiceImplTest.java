package com.kyoka.service.impl;

import com.kyoka.util.AuthUtil;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CartServiceImplTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private FoodRepository foodRepository;

    @Mock
    private AuthUtil authUtil;

    @Spy
    private ModelMapper modelMapper = new ModelMapper();

    @InjectMocks
    private CartServiceImpl cartService;

    private User testUser;
    private Cart testCart;
    private Food testFood;
    private CartItem testCartItem;
    private AddCartItemRequest testAddCartItemRequest;

    @BeforeEach
    void setUp() {
        // Set up test User
        testUser = new User();
        testUser.setUserId(1L);
        testUser.setUserName("testUser");
        testUser.setEmail("test@example.com");

        // Set up test Food
        testFood = new Food();
        testFood.setFoodId(1L);
        testFood.setName("Test Food");
        testFood.setPrice(10L);
        testFood.setAvailable(true);

        // Set up test CartItem
        testCartItem = new CartItem();
        testCartItem.setCartItemId(1L);
        testCartItem.setFood(testFood);
        testCartItem.setQuantity(2);
        testCartItem.setTotalPrice(20L);
        testCartItem.setIngredients(Arrays.asList("ingredient1", "ingredient2"));

        // Set up test Cart
        testCart = new Cart();
        testCart.setCartId(1L);
        testCart.setUser(testUser);
        testCart.setItems(new ArrayList<>(Arrays.asList(testCartItem)));
        testCartItem.setCart(testCart);

        // Set up test AddCartItemRequest
        testAddCartItemRequest = new AddCartItemRequest();
        testAddCartItemRequest.setFoodId(1L);
        testAddCartItemRequest.setQuantity(1);
        testAddCartItemRequest.setIngredients(Arrays.asList("ingredient1", "ingredient2"));
    }

    @Test
    void addItemToCart_ShouldAddNewItem_WhenItemDoesNotExist() {
        // Arrange
        // Create a cart without the item we're going to add
        Cart cartWithoutItem = new Cart();
        cartWithoutItem.setCartId(1L);
        cartWithoutItem.setUser(testUser);
        cartWithoutItem.setItems(new ArrayList<>());

        // Set up a food item with different ID than what's in the cart
        Food differentFood = new Food();
        differentFood.setFoodId(2L);
        differentFood.setName("Different Food");
        differentFood.setPrice(15L);

        AddCartItemRequest differentRequest = new AddCartItemRequest();
        differentRequest.setFoodId(2L);
        differentRequest.setQuantity(1);
        differentRequest.setIngredients(Arrays.asList("ingredient1", "ingredient2"));

        when(authUtil.loggedInUser()).thenReturn(testUser);
        when(foodRepository.findById(eq(2L))).thenReturn(Optional.of(differentFood));
        when(cartRepository.findCartByUserId(anyLong())).thenReturn(Optional.of(cartWithoutItem));
        when(cartItemRepository.save(any(CartItem.class))).thenAnswer(invocation -> {
            CartItem savedItem = invocation.getArgument(0);
            savedItem.setCartItemId(2L);
            return savedItem;
        });
        when(cartRepository.save(any(Cart.class))).thenReturn(cartWithoutItem);

        // Act
        CartItemDTO result = cartService.addItemToCart(differentRequest);

        // Assert
        assertNotNull(result);
        assertEquals(2L, result.getCartItemId());
        assertEquals(differentFood.getFoodId(), result.getFoodId());
        assertEquals(differentRequest.getQuantity(), result.getQuantity());
        assertEquals(differentFood.getPrice() * differentRequest.getQuantity(), result.getTotalPrice());

        verify(authUtil, times(1)).loggedInUser();
        verify(foodRepository, times(1)).findById(differentRequest.getFoodId());
        verify(cartRepository, times(1)).findCartByUserId(testUser.getUserId());
        verify(cartItemRepository, times(1)).save(any(CartItem.class));
        verify(cartRepository, times(1)).save(cartWithoutItem);
    }

    @Test
    void addItemToCart_ShouldUpdateExistingItem_WhenItemExists() {
        // Arrange
        // For the existing cart item with foodId=1
        CartServiceImpl spyCartService = spy(cartService);

        // Create updated CartItemDTO to be returned by the mocked method
        CartItemDTO updatedCartItemDTO = new CartItemDTO();
        updatedCartItemDTO.setCartItemId(1L);
        updatedCartItemDTO.setFoodId(testFood.getFoodId());
        updatedCartItemDTO.setFoodName(testFood.getName());
        updatedCartItemDTO.setQuantity(testCartItem.getQuantity() + testAddCartItemRequest.getQuantity());
        updatedCartItemDTO.setTotalPrice((testCartItem.getQuantity() + testAddCartItemRequest.getQuantity()) * testFood.getPrice());

        when(authUtil.loggedInUser()).thenReturn(testUser);
        when(foodRepository.findById(eq(1L))).thenReturn(Optional.of(testFood));
        when(cartRepository.findCartByUserId(anyLong())).thenReturn(Optional.of(testCart));
        doReturn(updatedCartItemDTO).when(spyCartService).updateCartItemQuantity(
                eq(testCartItem.getCartItemId()),
                eq(testCartItem.getQuantity() + testAddCartItemRequest.getQuantity())
        );

        // Act
        CartItemDTO result = spyCartService.addItemToCart(testAddCartItemRequest);

        // Assert
        assertNotNull(result);
        assertEquals(testCartItem.getCartItemId(), result.getCartItemId());
        assertEquals(testCartItem.getQuantity() + testAddCartItemRequest.getQuantity(), result.getQuantity());

        verify(authUtil, times(1)).loggedInUser();
        verify(foodRepository, times(1)).findById(testAddCartItemRequest.getFoodId());
        verify(cartRepository, times(1)).findCartByUserId(testUser.getUserId());
        verify(spyCartService, times(1)).updateCartItemQuantity(
                testCartItem.getCartItemId(),
                testCartItem.getQuantity() + testAddCartItemRequest.getQuantity()
        );
    }

    @Test
    void addItemToCart_ShouldThrowException_WhenFoodNotFound() {
        // Arrange
        when(authUtil.loggedInUser()).thenReturn(testUser);
        when(foodRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            cartService.addItemToCart(testAddCartItemRequest);
        });

        verify(authUtil, times(1)).loggedInUser();
        verify(foodRepository, times(1)).findById(testAddCartItemRequest.getFoodId());
        verify(cartRepository, never()).findCartByUserId(anyLong());
        verify(cartItemRepository, never()).save(any(CartItem.class));
    }

    @Test
    void addItemToCart_ShouldThrowException_WhenCartNotFound() {
        // Arrange
        when(authUtil.loggedInUser()).thenReturn(testUser);
        when(foodRepository.findById(anyLong())).thenReturn(Optional.of(testFood));
        when(cartRepository.findCartByUserId(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            cartService.addItemToCart(testAddCartItemRequest);
        });

        verify(authUtil, times(1)).loggedInUser();
        verify(foodRepository, times(1)).findById(testAddCartItemRequest.getFoodId());
        verify(cartRepository, times(1)).findCartByUserId(testUser.getUserId());
        verify(cartItemRepository, never()).save(any(CartItem.class));
    }

    @Test
    void updateCartItemQuantity_ShouldUpdateAndReturnItem() {
        // Arrange
        int newQuantity = 5;
        long expectedTotalPrice = newQuantity * testFood.getPrice();

        when(cartItemRepository.findById(anyLong())).thenReturn(Optional.of(testCartItem));
        when(cartItemRepository.save(any(CartItem.class))).thenReturn(testCartItem);

        // Act
        CartItemDTO result = cartService.updateCartItemQuantity(testCartItem.getCartItemId(), newQuantity);

        // Assert
        assertNotNull(result);
        assertEquals(testCartItem.getCartItemId(), result.getCartItemId());
        assertEquals(newQuantity, result.getQuantity());
        assertEquals(expectedTotalPrice, result.getTotalPrice());

        verify(cartItemRepository, times(1)).findById(testCartItem.getCartItemId());
        verify(cartItemRepository, times(1)).save(testCartItem);
    }

    @Test
    void updateCartItemQuantity_ShouldThrowException_WhenItemNotFound() {
        // Arrange
        when(cartItemRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            cartService.updateCartItemQuantity(999L, 5);
        });

        verify(cartItemRepository, times(1)).findById(999L);
        verify(cartItemRepository, never()).save(any(CartItem.class));
    }

    @Test
    void removeItemFromCart_ShouldRemoveItemAndReturnUpdatedCart() {
        // Arrange
        when(authUtil.loggedInUser()).thenReturn(testUser);
        when(cartRepository.findCartByUserId(anyLong())).thenReturn(Optional.of(testCart));
        when(cartItemRepository.findById(anyLong())).thenReturn(Optional.of(testCartItem));
        doNothing().when(cartItemRepository).delete(any(CartItem.class));
        when(cartRepository.save(any(Cart.class))).thenReturn(testCart);

        // Act
        CartDTO result = cartService.removeItemFromCart(testCartItem.getCartItemId());

        // Assert
        assertNotNull(result);
        assertEquals(testCart.getCartId(), result.getCartId());
        // The item should be removed from the cart
        assertEquals(0, testCart.getItems().size());

        verify(authUtil, times(1)).loggedInUser();
        verify(cartRepository, times(1)).findCartByUserId(testUser.getUserId());
        verify(cartItemRepository, times(1)).findById(testCartItem.getCartItemId());
        verify(cartItemRepository, times(1)).delete(testCartItem);
        verify(cartRepository, times(1)).save(testCart);
    }

    @Test
    void removeItemFromCart_ShouldThrowException_WhenCartNotFound() {
        // Arrange
        when(authUtil.loggedInUser()).thenReturn(testUser);
        when(cartRepository.findCartByUserId(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            cartService.removeItemFromCart(testCartItem.getCartItemId());
        });

        verify(authUtil, times(1)).loggedInUser();
        verify(cartRepository, times(1)).findCartByUserId(testUser.getUserId());
        verify(cartItemRepository, never()).findById(anyLong());
        verify(cartItemRepository, never()).delete(any(CartItem.class));
        verify(cartRepository, never()).save(any(Cart.class));
    }

    @Test
    void removeItemFromCart_ShouldThrowException_WhenItemNotFound() {
        // Arrange
        when(authUtil.loggedInUser()).thenReturn(testUser);
        when(cartRepository.findCartByUserId(anyLong())).thenReturn(Optional.of(testCart));
        when(cartItemRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            cartService.removeItemFromCart(999L);
        });

        verify(authUtil, times(1)).loggedInUser();
        verify(cartRepository, times(1)).findCartByUserId(testUser.getUserId());
        verify(cartItemRepository, times(1)).findById(999L);
        verify(cartItemRepository, never()).delete(any(CartItem.class));
        verify(cartRepository, never()).save(any(Cart.class));
    }

    @Test
    void findCartByUserId_ShouldReturnCart() {
        // Arrange
        when(authUtil.loggedInUser()).thenReturn(testUser);
        when(cartRepository.findCartByUserId(anyLong())).thenReturn(Optional.of(testCart));

        // Act
        CartDTO result = cartService.findCartByUserId();

        // Assert
        assertNotNull(result);
        assertEquals(testCart.getCartId(), result.getCartId());
        assertEquals(1, result.getItems().size());

        verify(authUtil, times(1)).loggedInUser();
        verify(cartRepository, times(1)).findCartByUserId(testUser.getUserId());
    }

    @Test
    void findCartByUserId_ShouldThrowException_WhenCartNotFound() {
        // Arrange
        when(authUtil.loggedInUser()).thenReturn(testUser);
        when(cartRepository.findCartByUserId(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            cartService.findCartByUserId();
        });

        verify(authUtil, times(1)).loggedInUser();
        verify(cartRepository, times(1)).findCartByUserId(testUser.getUserId());
    }

    @Test
    void clearCart_ShouldClearAllItemsAndReturnEmptyCart() {
        // Arrange
        when(authUtil.loggedInUser()).thenReturn(testUser);
        when(cartRepository.findCartByUserId(anyLong())).thenReturn(Optional.of(testCart));
        when(cartRepository.save(any(Cart.class))).thenAnswer(invocation -> {
            Cart savedCart = invocation.getArgument(0);
            savedCart.getItems().clear();
            return savedCart;
        });

        // Act
        CartDTO result = cartService.clearCart();

        // Assert
        assertNotNull(result);
        assertEquals(testCart.getCartId(), result.getCartId());
        assertTrue(result.getItems().isEmpty());

        verify(authUtil, times(1)).loggedInUser();
        verify(cartRepository, times(1)).findCartByUserId(testUser.getUserId());
        verify(cartRepository, times(1)).save(testCart);
    }

    @Test
    void clearCart_ShouldThrowException_WhenCartNotFound() {
        // Arrange
        when(authUtil.loggedInUser()).thenReturn(testUser);
        when(cartRepository.findCartByUserId(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            cartService.clearCart();
        });

        verify(authUtil, times(1)).loggedInUser();
        verify(cartRepository, times(1)).findCartByUserId(testUser.getUserId());
        verify(cartRepository, never()).save(any(Cart.class));
    }
}
