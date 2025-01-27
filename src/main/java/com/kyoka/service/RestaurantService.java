package com.kyoka.service;

import java.util.List;

import com.kyoka.dto.request.RestaurantDTO;
import com.kyoka.model.User;

public interface RestaurantService {

    RestaurantDTO createRestaurant(RestaurantDTO restaurantDTO);

    RestaurantDTO updateRestaurantStatus(Long id);

    String deleteRestaurant(Long restaurantId);

    List<RestaurantDTO> getAllRestaurant();

    List<RestaurantDTO> searchRestaurant(String keyword);

    RestaurantDTO updateRestaurant(Long restaurantId, RestaurantDTO restaurantDTO);

    RestaurantDTO findRestaurantById(Long id);

    RestaurantDTO getRestaurantsByUserId(Long userId);

    String addToFavorites(Long restaurantId, User user);
}
