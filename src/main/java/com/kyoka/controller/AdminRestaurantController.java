package com.kyoka.controller;

import com.kyoka.Util.AuthUtil;
import com.kyoka.dto.request.RestaurantDTO;
import com.kyoka.dto.response.APIResponse;
import com.kyoka.model.User;
import com.kyoka.service.RestaurantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/restaurants")
public class AdminRestaurantController {
    @Autowired
    private RestaurantService restaurantService;

    @Autowired
    private AuthUtil authUtil;

    @PostMapping
    public ResponseEntity<RestaurantDTO> createRestaurant(@RequestBody RestaurantDTO restaurantDTO) {
        User user = authUtil.loggedInUser();
        RestaurantDTO restaurant = restaurantService.createRestaurant(restaurantDTO, user);
        return ResponseEntity.ok(restaurant);
    }

    @PutMapping("/{id}")
    public ResponseEntity<RestaurantDTO> updateRestaurant(
            @PathVariable Long id,
            @RequestBody RestaurantDTO restaurantDTO) {
        RestaurantDTO restaurant = restaurantService.updateRestaurant(id, restaurantDTO);
        return ResponseEntity.ok(restaurant);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<APIResponse> deleteRestaurantById(@PathVariable("id") Long restaurantId) {
        String message = restaurantService.deleteRestaurant(restaurantId);
        APIResponse response = new APIResponse(message, true);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<RestaurantDTO> updateRestaurantStatus(@PathVariable Long id) {
        RestaurantDTO restaurant = restaurantService.updateRestaurantStatus(id);
        return ResponseEntity.ok(restaurant);
    }

    @GetMapping("/user")
    public ResponseEntity<RestaurantDTO> findRestaurantByUserId() {
        User user = authUtil.loggedInUser();
        RestaurantDTO restaurant = restaurantService.getRestaurantsByUserId(user.getUserId());
        return ResponseEntity.ok(restaurant);
    }
}
