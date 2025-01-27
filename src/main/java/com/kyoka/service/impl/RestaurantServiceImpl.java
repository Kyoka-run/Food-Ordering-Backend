package com.kyoka.service.impl;

import com.kyoka.Util.AuthUtil;
import com.kyoka.dto.request.RestaurantDTO;
import com.kyoka.exception.ResourceNotFoundException;
import com.kyoka.model.Address;
import com.kyoka.model.Restaurant;
import com.kyoka.model.User;
import com.kyoka.repository.AddressRepository;
import com.kyoka.repository.RestaurantRepository;
import com.kyoka.repository.UserRepository;
import com.kyoka.service.RestaurantService;
import com.kyoka.service.UserService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RestaurantServiceImpl implements RestaurantService {
    @Autowired
    private RestaurantRepository restaurantRepository;

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    AuthUtil authUtil;

    @Override
    public RestaurantDTO createRestaurant(RestaurantDTO restaurantDTO) {
        User user = authUtil.loggedInUser();
        Address address = new Address();
        address.setCity(restaurantDTO.getAddress().getCity());
        address.setCountry(restaurantDTO.getAddress().getCountry());
        address.setFullName(restaurantDTO.getAddress().getFullName());
        address.setPostalCode(restaurantDTO.getAddress().getPostalCode());
        address.setState(restaurantDTO.getAddress().getState());
        address.setStreetAddress(restaurantDTO.getAddress().getStreetAddress());
        Address savedAddress = addressRepository.save(address);

        Restaurant restaurant = new Restaurant();

        restaurant.setAddress(savedAddress);
        restaurant.setContactInformation(restaurantDTO.getContactInformation());
        restaurant.setCuisineType(restaurantDTO.getCuisineType());
        restaurant.setDescription(restaurantDTO.getDescription());
        restaurant.setImages(restaurantDTO.getImages());
        restaurant.setName(restaurantDTO.getName());
        restaurant.setOpeningHours(restaurantDTO.getOpeningHours());
        restaurant.setRegistrationDate(restaurantDTO.getRegistrationDate());
        restaurant.setOwner(user);

        Restaurant savedRestaurant = restaurantRepository.save(restaurant);
        return modelMapper.map(savedRestaurant, RestaurantDTO.class);
    }

    @Override
    public RestaurantDTO updateRestaurant(Long restaurantId, RestaurantDTO restaurantDTO) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant", "id", restaurantId));

        restaurant.setCuisineType(restaurantDTO.getCuisineType());
        restaurant.setDescription(restaurantDTO.getDescription());

        Restaurant updatedRestaurant = restaurantRepository.save(restaurant);
        return modelMapper.map(updatedRestaurant, RestaurantDTO.class);
    }

    @Override
    public RestaurantDTO findRestaurantById(Long restaurantId) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant", "id", restaurantId));
        return modelMapper.map(restaurant, RestaurantDTO.class);
    }

    @Override
    public String deleteRestaurant(Long restaurantId) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant", "id", restaurantId));

        restaurantRepository.delete(restaurant);

        return "Restaurant deleted successfully with restaurantId: " + restaurantId;
    }

    @Override
    public List<RestaurantDTO> getAllRestaurant() {
        List<Restaurant> restaurants = restaurantRepository.findAll();
        return restaurants.stream()
                .map(restaurant -> modelMapper.map(restaurant, RestaurantDTO.class))
                .toList();
    }

    @Override
    public RestaurantDTO getRestaurantsByUserId(Long userId) {
        Restaurant restaurant = restaurantRepository.findByOwnerId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant", "id", userId));
        return modelMapper.map(restaurant, RestaurantDTO.class);
    }

    @Override
    public List<RestaurantDTO> searchRestaurant(String keyword) {
        List<Restaurant> restaurants = restaurantRepository.findBySearchQuery(keyword);
        return restaurants.stream()
                .map(restaurant -> modelMapper.map(restaurant, RestaurantDTO.class))
                .toList();
    }

    @Override
    public String addToFavorites(Long restaurantId, User user) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant", "id", restaurantId));

        List<Restaurant> userFavorites = user.getFavoriteRestaurants();
        boolean isAlreadyFavorited = userFavorites.contains(restaurant);

        if (isAlreadyFavorited) {
            userFavorites.remove(restaurant);
            userRepository.save(user);
            return "Restaurant removed from favorites successfully";
        } else {
            userFavorites.add(restaurant);
            userRepository.save(user);
            return "Restaurant added to favorites successfully";
        }
    }

    @Override
    public RestaurantDTO updateRestaurantStatus(Long restaurantId) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant", "id", restaurantId));
        restaurant.setOpen(!restaurant.isOpen());
        return modelMapper.map(restaurant, RestaurantDTO.class);
    }
}

