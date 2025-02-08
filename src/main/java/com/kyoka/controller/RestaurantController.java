package com.kyoka.controller;

import com.kyoka.Util.AuthUtil;
import com.kyoka.dto.RestaurantDTO;
import com.kyoka.model.User;
import com.kyoka.service.RestaurantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.kyoka.dto.APIResponse;

import java.util.List;

@RestController
@RequestMapping("/api")
public class RestaurantController {
    @Autowired
    private RestaurantService restaurantService;

    @Autowired
    private AuthUtil authUtil;

    @PostMapping("/admin/restaurants")
    public ResponseEntity<RestaurantDTO> createRestaurant(@RequestBody RestaurantDTO restaurantDTO) {
        RestaurantDTO restaurant = restaurantService.createRestaurant(restaurantDTO);
        return ResponseEntity.ok(restaurant);
    }

    @PutMapping("/admin/restaurants/{id}")
    public ResponseEntity<RestaurantDTO> updateRestaurant(
            @PathVariable Long id,
            @RequestBody RestaurantDTO restaurantDTO) {
        RestaurantDTO restaurant = restaurantService.updateRestaurant(id, restaurantDTO);
        return ResponseEntity.ok(restaurant);
    }

    @DeleteMapping("/admin/restaurants/{id}")
    public ResponseEntity<APIResponse> deleteRestaurant(@PathVariable("id") Long restaurantId) {
        String message = restaurantService.deleteRestaurant(restaurantId);
        APIResponse response = new APIResponse(message, true);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/admin/restaurants/{id}/status")
    public ResponseEntity<RestaurantDTO> updateRestaurantStatus(@PathVariable Long id) {
        RestaurantDTO restaurant = restaurantService.updateRestaurantStatus(id);
        return ResponseEntity.ok(restaurant);
    }

    @GetMapping("/admin/restaurants/user")
    public ResponseEntity<RestaurantDTO> findRestaurantByUserId() {
        User user = authUtil.loggedInUser();
        RestaurantDTO restaurant = restaurantService.getRestaurantsByUserId(user.getUserId());
        return ResponseEntity.ok(restaurant);
    }

    @GetMapping("/restaurants/search")
    public ResponseEntity<List<RestaurantDTO>> findRestaurantByName(@RequestParam String keyword) {
        List<RestaurantDTO> restaurantDTO = restaurantService.searchRestaurant(keyword);
        return ResponseEntity.ok(restaurantDTO);
    }

    @GetMapping("/restaurants")
    public ResponseEntity<List<RestaurantDTO>> getAllRestaurants() {
        List<RestaurantDTO> restaurantDTOS = restaurantService.getAllRestaurant();
        return ResponseEntity.ok(restaurantDTOS);
    }

    @GetMapping("/restaurants/{id}")
    public ResponseEntity<RestaurantDTO> findRestaurantById(@PathVariable Long id) {
        RestaurantDTO restaurantDTO = restaurantService.findRestaurantById(id);
        return ResponseEntity.ok(restaurantDTO);
    }

    @PutMapping("/restaurants/{id}/add-favorites")
    public ResponseEntity<String> addToFavorite(@PathVariable Long id) {
        User user = authUtil.loggedInUser();
        String status = restaurantService.addToFavorites(id, user);
        return ResponseEntity.ok(status);
    }
}