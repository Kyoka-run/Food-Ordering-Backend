package com.kyoka.dto;

import com.kyoka.model.Address;
import com.kyoka.model.Restaurant;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDTO {
    private Long userId;
    private String username;
    private List<String> roles;
    private List<Restaurant> favorites;
    private String email;
    private List<Address> addresses;
}

