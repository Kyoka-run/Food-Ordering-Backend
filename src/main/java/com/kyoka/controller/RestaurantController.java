package com.kyoka.controller;

import com.kyoka.Util.AuthUtil;
import com.kyoka.dto.request.RestaurantDTO;
import com.kyoka.model.User;
import com.kyoka.service.RestaurantService;
import com.kyoka.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/restaurants")
public class RestaurantController {
    @Autowired
    private RestaurantService restaurantService;

    @Autowired
    private UserService userService;
    @Autowired
    private AuthUtil authUtil;

    @GetMapping("/search")
    public ResponseEntity<List<RestaurantDTO>> findRestaurantByName(@RequestParam String keyword) {
        List<RestaurantDTO> restaurantDTO = restaurantService.searchRestaurant(keyword);
        return ResponseEntity.ok(restaurantDTO);
    }

    @GetMapping()
    public ResponseEntity<List<RestaurantDTO>> getAllRestaurants() {
        List<RestaurantDTO> restaurantDTOS = restaurantService.getAllRestaurant();
        return ResponseEntity.ok(restaurantDTOS);
    }

    @GetMapping("/{id}")
    public ResponseEntity<RestaurantDTO> findRestaurantById(@PathVariable Long id) {
        RestaurantDTO restaurantDTO = restaurantService.findRestaurantById(id);
        return ResponseEntity.ok(restaurantDTO);
    }

    @PutMapping("/{id}/add-favorites")
    public ResponseEntity<String> addToFavorite(@PathVariable Long id) {
        User user = authUtil.loggedInUser();
        String status = restaurantService.addToFavorites(id, user);
        return ResponseEntity.ok(status);
    }
}

