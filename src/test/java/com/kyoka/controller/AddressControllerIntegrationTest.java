package com.kyoka.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kyoka.util.AuthUtil;
import com.kyoka.dto.AddressDTO;
import com.kyoka.model.User;
import com.kyoka.service.AddressService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class AddressControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AddressService addressService;

    @MockitoBean
    private AuthUtil authUtil;

    @Autowired
    private ObjectMapper objectMapper;

    private User testUser;
    private AddressDTO testAddressDTO;

    @BeforeEach
    void setUp() {
        // Set up test user
        testUser = new User();
        testUser.setUserId(1L);
        testUser.setUserName("testUser");
        testUser.setEmail("test@example.com");

        // Set up test address DTO
        testAddressDTO = new AddressDTO();
        testAddressDTO.setStreet("123 Test St");
        testAddressDTO.setCity("Test City");
        testAddressDTO.setCountry("Test Country");
        testAddressDTO.setPostalCode("12345");
    }

    @Test
    @WithMockUser
    void createAddress_ShouldReturnCreatedAddress() throws Exception {
        when(addressService.createAddress(any(AddressDTO.class))).thenReturn(testAddressDTO);

        mockMvc.perform(post("/api/address")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testAddressDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.street", is("123 Test St")))
                .andExpect(jsonPath("$.city", is("Test City")))
                .andExpect(jsonPath("$.country", is("Test Country")))
                .andExpect(jsonPath("$.postalCode", is("12345")));
    }

    @Test
    @WithMockUser
    void getUserAddresses_ShouldReturnAddressesList() throws Exception {
        List<AddressDTO> addresses = Arrays.asList(
                testAddressDTO,
                createAddressDTO("456 Second St", "Second City")
        );

        when(addressService.getUserAddresses()).thenReturn(addresses);

        mockMvc.perform(get("/api/address"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].street", is("123 Test St")))
                .andExpect(jsonPath("$[1].street", is("456 Second St")));
    }

    @Test
    @WithMockUser
    void updateAddress_ShouldReturnUpdatedAddress() throws Exception {
        AddressDTO updatedAddressDTO = new AddressDTO();
        updatedAddressDTO.setStreet("456 Updated St");
        updatedAddressDTO.setCity("Updated City");
        updatedAddressDTO.setCountry("Updated Country");
        updatedAddressDTO.setPostalCode("54321");

        when(addressService.updateAddress(anyLong(), any(AddressDTO.class))).thenReturn(updatedAddressDTO);

        mockMvc.perform(put("/api/address/{addressId}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedAddressDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.street", is("456 Updated St")))
                .andExpect(jsonPath("$.city", is("Updated City")))
                .andExpect(jsonPath("$.country", is("Updated Country")))
                .andExpect(jsonPath("$.postalCode", is("54321")));
    }

    @Test
    @WithMockUser
    void deleteAddress_ShouldReturnSuccessMessage() throws Exception {
        String successMessage = "Address deleted successfully with id: 1";
        when(addressService.deleteAddress(anyLong())).thenReturn(successMessage);

        mockMvc.perform(delete("/api/address/{addressId}", 1L))
                .andExpect(status().isOk())
                .andExpect(content().string(successMessage));
    }

    private AddressDTO createAddressDTO(String street, String city) {
        AddressDTO dto = new AddressDTO();
        dto.setStreet(street);
        dto.setCity(city);
        dto.setCountry("Test Country");
        dto.setPostalCode("12345");
        return dto;
    }
}