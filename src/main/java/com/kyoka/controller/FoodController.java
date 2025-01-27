package com.kyoka.controller;

import com.kyoka.Util.AuthUtil;
import com.kyoka.dto.request.FoodDTO;
import com.kyoka.dto.request.RestaurantDTO;
import com.kyoka.dto.response.APIResponse;
import com.kyoka.service.FoodService;
import com.kyoka.service.RestaurantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class FoodController {
    @Autowired
    private FoodService foodService;

    @Autowired
    private RestaurantService restaurantService;

    @Autowired
    private AuthUtil authUtil;

    @PostMapping("/admin/food")
    public ResponseEntity<FoodDTO> createFood(@RequestBody FoodDTO foodDTO) {
        FoodDTO createdFood = foodService.createFood(foodDTO);
        return ResponseEntity.ok(createdFood);
    }

    @DeleteMapping("/admin/food/{id}")
    public ResponseEntity<APIResponse> deleteFood(@PathVariable("id") Long foodId) {
        String message = foodService.deleteFood(foodId);
        APIResponse response = new APIResponse(message, true);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/food/search")
    public ResponseEntity<List<FoodDTO>> searchFood(@RequestParam String name) {
        List<FoodDTO> foods = foodService.searchFood(name);
        return ResponseEntity.ok(foods);
    }

    @GetMapping("/food/restaurant/{restaurantId}")
    public ResponseEntity<List<FoodDTO>> getRestaurantFoods(
            @PathVariable Long restaurantId,
            @RequestParam boolean vegetarian,
            @RequestParam boolean nonveg,
            @RequestParam boolean seasonal,
            @RequestParam(required = false) String food_category) {
        List<FoodDTO> foods = foodService.getRestaurantsFoods(
                restaurantId, vegetarian, nonveg, seasonal, food_category);
        return ResponseEntity.ok(foods);
    }

    @PutMapping("/admin/food/{id}/availability")
    public ResponseEntity<FoodDTO> updateFoodAvailability(@PathVariable Long id) {
        FoodDTO food = foodService.updateAvailibilityStatus(id);
        return ResponseEntity.ok(food);
    }

    @GetMapping("/admin/food/{id}")
    public ResponseEntity<FoodDTO> getFoodById(@PathVariable Long id) {
        FoodDTO food = foodService.findFoodById(id);
        return ResponseEntity.ok(food);
    }
}