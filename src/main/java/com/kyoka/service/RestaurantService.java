package com.kyoka.service;

import java.util.List;

import com.kyoka.dto.request.RestaurantDTO;
import com.kyoka.model.User;

public interface RestaurantService {

    public RestaurantDTO createRestaurant(RestaurantDTO restaurantDTO, User user);

    public RestaurantDTO updateRestaurantStatus(Long id);

    public String deleteRestaurant(Long restaurantId);

    public List<RestaurantDTO> getAllRestaurant();

    public List<RestaurantDTO> searchRestaurant(String keyword);

    RestaurantDTO updateRestaurant(Long restaurantId, RestaurantDTO restaurantDTO);

    public RestaurantDTO findRestaurantById(Long id);

    public RestaurantDTO getRestaurantsByUserId(Long userId);

    public String addToFavorites(Long restaurantId, User user);
}
