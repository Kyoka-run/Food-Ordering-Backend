package com.kyoka.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kyoka.dto.AddressDTO;
import com.kyoka.service.AddressService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class AddressControllerTest {

    @Mock
    private AddressService addressService;

    @InjectMocks
    private AddressController addressController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(addressController).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void createAddress_ShouldReturnCreatedAddress() throws Exception {
        // Arrange
        AddressDTO requestDto = new AddressDTO();
        requestDto.setStreet("123 Test St");
        requestDto.setCity("Test City");
        requestDto.setCountry("Test Country");
        requestDto.setPostalCode("12345");

        AddressDTO responseDto = new AddressDTO();
        responseDto.setStreet("123 Test St");
        responseDto.setCity("Test City");
        responseDto.setCountry("Test Country");
        responseDto.setPostalCode("12345");

        when(addressService.createAddress(any(AddressDTO.class))).thenReturn(responseDto);

        // Act & Assert
        mockMvc.perform(post("/api/address")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.street").value("123 Test St"))
                .andExpect(jsonPath("$.city").value("Test City"))
                .andExpect(jsonPath("$.country").value("Test Country"))
                .andExpect(jsonPath("$.postalCode").value("12345"));

        verify(addressService, times(1)).createAddress(any(AddressDTO.class));
    }

    @Test
    void getUserAddresses_ShouldReturnListOfAddresses() throws Exception {
        // Arrange
        List<AddressDTO> addresses = Arrays.asList(
                new AddressDTO("123 Test St", "Test City", "Test Country", "12345"),
                new AddressDTO("456 Sample St", "Sample City", "Sample Country", "67890")
        );

        when(addressService.getUserAddresses()).thenReturn(addresses);

        // Act & Assert
        mockMvc.perform(get("/api/address"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].street").value("123 Test St"))
                .andExpect(jsonPath("$[1].street").value("456 Sample St"))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));

        verify(addressService, times(1)).getUserAddresses();
    }

    @Test
    void updateAddress_ShouldReturnUpdatedAddress() throws Exception {
        // Arrange
        Long addressId = 1L;
        AddressDTO requestDto = new AddressDTO();
        requestDto.setStreet("Updated St");
        requestDto.setCity("Updated City");
        requestDto.setCountry("Updated Country");
        requestDto.setPostalCode("54321");

        AddressDTO responseDto = new AddressDTO();
        responseDto.setStreet("Updated St");
        responseDto.setCity("Updated City");
        responseDto.setCountry("Updated Country");
        responseDto.setPostalCode("54321");

        when(addressService.updateAddress(eq(addressId), any(AddressDTO.class))).thenReturn(responseDto);

        // Act & Assert
        mockMvc.perform(put("/api/address/{addressId}", addressId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.street").value("Updated St"))
                .andExpect(jsonPath("$.city").value("Updated City"))
                .andExpect(jsonPath("$.country").value("Updated Country"))
                .andExpect(jsonPath("$.postalCode").value("54321"));

        verify(addressService, times(1)).updateAddress(eq(addressId), any(AddressDTO.class));
    }

    @Test
    void deleteAddress_ShouldReturnSuccessMessage() throws Exception {
        // Arrange
        Long addressId = 1L;
        String successMessage = "Address deleted successfully with id: " + addressId;

        when(addressService.deleteAddress(addressId)).thenReturn(successMessage);

        // Act & Assert
        mockMvc.perform(delete("/api/address/{addressId}", addressId))
                .andExpect(status().isOk())
                .andExpect(content().string(successMessage));

        verify(addressService, times(1)).deleteAddress(addressId);
    }
}