package com.kyoka.service.impl;

import com.kyoka.Util.AuthUtil;
import com.kyoka.dto.AddressDTO;
import com.kyoka.exception.ResourceNotFoundException;
import com.kyoka.model.Address;
import com.kyoka.model.User;
import com.kyoka.repository.AddressRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AddressServiceImplTest {

    @Mock
    private AddressRepository addressRepository;

    @Mock
    private AuthUtil authUtil;

    @Spy
    private ModelMapper modelMapper = new ModelMapper();

    @InjectMocks
    private AddressServiceImpl addressService;

    private User testUser;
    private Address testAddress;
    private AddressDTO testAddressDTO;

    @BeforeEach
    void setUp() {
        // Set up test User
        testUser = new User();
        testUser.setUserId(1L);
        testUser.setUserName("testUser");
        testUser.setEmail("test@example.com");
        testUser.setAddresses(new ArrayList<>());

        // Set up test Address
        testAddress = new Address();
        testAddress.setAddressId(1L);
        testAddress.setStreet("123 Test St");
        testAddress.setCity("Test City");
        testAddress.setCountry("Test Country");
        testAddress.setPostalCode("12345");
        testAddress.setUser(testUser);

        // Add the address to the user's addresses
        testUser.getAddresses().add(testAddress);

        // Set up test AddressDTO
        testAddressDTO = new AddressDTO();
        testAddressDTO.setStreet("123 Test St");
        testAddressDTO.setCity("Test City");
        testAddressDTO.setCountry("Test Country");
        testAddressDTO.setPostalCode("12345");
    }

    @Test
    void createAddress_ShouldCreateAndReturnAddress() {
        // Arrange
        when(authUtil.loggedInUser()).thenReturn(testUser);
        when(addressRepository.save(any(Address.class))).thenAnswer(invocation -> {
            Address savedAddress = invocation.getArgument(0);
            savedAddress.setAddressId(2L);
            return savedAddress;
        });

        // Act
        AddressDTO result = addressService.createAddress(testAddressDTO);

        // Assert
        assertNotNull(result);
        assertEquals(testAddressDTO.getStreet(), result.getStreet());
        assertEquals(testAddressDTO.getCity(), result.getCity());
        assertEquals(testAddressDTO.getCountry(), result.getCountry());
        assertEquals(testAddressDTO.getPostalCode(), result.getPostalCode());

        verify(authUtil, times(1)).loggedInUser();
        verify(addressRepository, times(1)).save(any(Address.class));
        // The user should now have 2 addresses (the existing one plus the new one)
        assertEquals(2, testUser.getAddresses().size());
    }

    @Test
    void getUserAddresses_ShouldReturnUserAddresses() {
        // Arrange
        when(authUtil.loggedInUser()).thenReturn(testUser);

        // Act
        List<AddressDTO> result = addressService.getUserAddresses();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        AddressDTO addressDTO = result.get(0);
        assertEquals(testAddress.getStreet(), addressDTO.getStreet());
        assertEquals(testAddress.getCity(), addressDTO.getCity());
        assertEquals(testAddress.getCountry(), addressDTO.getCountry());
        assertEquals(testAddress.getPostalCode(), addressDTO.getPostalCode());

        verify(authUtil, times(1)).loggedInUser();
    }

    @Test
    void getUserAddresses_ShouldReturnEmptyList_WhenUserHasNoAddresses() {
        // Arrange
        User userWithNoAddresses = new User();
        userWithNoAddresses.setUserId(2L);
        userWithNoAddresses.setUserName("userNoAddresses");
        userWithNoAddresses.setAddresses(new ArrayList<>());

        when(authUtil.loggedInUser()).thenReturn(userWithNoAddresses);

        // Act
        List<AddressDTO> result = addressService.getUserAddresses();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(authUtil, times(1)).loggedInUser();
    }

    @Test
    void updateAddress_ShouldUpdateAndReturnAddress() {
        // Arrange
        AddressDTO updateDTO = new AddressDTO();
        updateDTO.setStreet("456 Updated St");
        updateDTO.setCity("Updated City");
        updateDTO.setCountry("Updated Country");
        updateDTO.setPostalCode("54321");

        when(addressRepository.findById(anyLong())).thenReturn(Optional.of(testAddress));
        when(addressRepository.save(any(Address.class))).thenReturn(testAddress);

        // Act
        AddressDTO result = addressService.updateAddress(1L, updateDTO);

        // Assert
        assertNotNull(result);
        assertEquals(updateDTO.getStreet(), result.getStreet());
        assertEquals(updateDTO.getCity(), result.getCity());
        assertEquals(updateDTO.getCountry(), result.getCountry());
        assertEquals(updateDTO.getPostalCode(), result.getPostalCode());

        verify(addressRepository, times(1)).findById(1L);
        verify(addressRepository, times(1)).save(testAddress);
    }

    @Test
    void updateAddress_ShouldThrowException_WhenAddressNotFound() {
        // Arrange
        AddressDTO updateDTO = new AddressDTO();
        updateDTO.setStreet("456 Updated St");

        when(addressRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            addressService.updateAddress(999L, updateDTO);
        });

        verify(addressRepository, times(1)).findById(999L);
        verify(addressRepository, never()).save(any(Address.class));
    }

    @Test
    void deleteAddress_ShouldDeleteAndReturnSuccessMessage() {
        // Arrange
        when(addressRepository.findById(anyLong())).thenReturn(Optional.of(testAddress));
        doNothing().when(addressRepository).delete(any(Address.class));

        // Act
        String result = addressService.deleteAddress(1L);

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("deleted successfully"));

        verify(addressRepository, times(1)).findById(1L);
        verify(addressRepository, times(1)).delete(testAddress);
    }

    @Test
    void deleteAddress_ShouldThrowException_WhenAddressNotFound() {
        // Arrange
        when(addressRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            addressService.deleteAddress(999L);
        });

        verify(addressRepository, times(1)).findById(999L);
        verify(addressRepository, never()).delete(any(Address.class));
    }

    @Test
    void createAddress_ShouldHandleNullFields() {
        // Arrange
        AddressDTO incompleteDTO = new AddressDTO();
        incompleteDTO.setStreet("123 Test St");
        // Leave other fields null

        when(authUtil.loggedInUser()).thenReturn(testUser);
        when(addressRepository.save(any(Address.class))).thenAnswer(invocation -> {
            Address savedAddress = invocation.getArgument(0);
            savedAddress.setAddressId(2L);
            return savedAddress;
        });

        // Act
        AddressDTO result = addressService.createAddress(incompleteDTO);

        // Assert
        assertNotNull(result);
        assertEquals(incompleteDTO.getStreet(), result.getStreet());
        assertNull(result.getCity());
        assertNull(result.getCountry());
        assertNull(result.getPostalCode());

        verify(authUtil, times(1)).loggedInUser();
        verify(addressRepository, times(1)).save(any(Address.class));
    }

    @Test
    void updateAddress_ShouldUpdateOnlyProvidedFields() {
        // Arrange
        // Create a partial update DTO with only some fields
        AddressDTO partialUpdateDTO = new AddressDTO();
        partialUpdateDTO.setStreet("456 Updated St");
        // Leave other fields null, they shouldn't be updated

        when(addressRepository.findById(anyLong())).thenReturn(Optional.of(testAddress));
        when(addressRepository.save(any(Address.class))).thenReturn(testAddress);

        // Act
        AddressDTO result = addressService.updateAddress(1L, partialUpdateDTO);

        // Assert
        assertNotNull(result);
        assertEquals(partialUpdateDTO.getStreet(), result.getStreet());
        // These values should not have changed
        assertEquals(testAddress.getCity(), result.getCity());
        assertEquals(testAddress.getCountry(), result.getCountry());
        assertEquals(testAddress.getPostalCode(), result.getPostalCode());

        verify(addressRepository, times(1)).findById(1L);
        verify(addressRepository, times(1)).save(testAddress);
    }

    @Test
    void getUserAddresses_ShouldReturnMultipleAddresses_WhenUserHasMultiple() {
        // Arrange
        // Add a second address to the user
        Address secondAddress = new Address();
        secondAddress.setAddressId(2L);
        secondAddress.setStreet("789 Second St");
        secondAddress.setCity("Second City");
        secondAddress.setCountry("Second Country");
        secondAddress.setPostalCode("67890");
        secondAddress.setUser(testUser);

        testUser.getAddresses().add(secondAddress);

        when(authUtil.loggedInUser()).thenReturn(testUser);

        // Act
        List<AddressDTO> result = addressService.getUserAddresses();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());

        // Verify first address
        assertEquals(testAddress.getStreet(), result.get(0).getStreet());

        // Verify second address
        assertEquals(secondAddress.getStreet(), result.get(1).getStreet());
        assertEquals(secondAddress.getCity(), result.get(1).getCity());

        verify(authUtil, times(1)).loggedInUser();
    }
}