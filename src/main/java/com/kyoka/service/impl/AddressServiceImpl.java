package com.kyoka.service.impl;

import com.kyoka.util.AuthUtil;
import com.kyoka.dto.AddressDTO;
import com.kyoka.exception.ResourceNotFoundException;
import com.kyoka.model.Address;
import com.kyoka.model.User;
import com.kyoka.repository.AddressRepository;
import com.kyoka.service.AddressService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AddressServiceImpl implements AddressService {

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private AuthUtil authUtil;

    @Autowired
    private ModelMapper modelMapper;

    @Override
    public AddressDTO createAddress(AddressDTO addressDTO) {
        User user = authUtil.loggedInUser();
        Address address = modelMapper.map(addressDTO, Address.class);
        address.setUser(user);
        List<Address> addressesList = user.getAddresses();
        addressesList.add(address);
        user.setAddresses(addressesList);
        Address savedAddress = addressRepository.save(address);
        return modelMapper.map(savedAddress, AddressDTO.class);
    }

    @Override
    public List<AddressDTO> getUserAddresses() {
        User user = authUtil.loggedInUser();

        return user.getAddresses().stream()
                .map(address -> modelMapper.map(address, AddressDTO.class))
                .toList();
    }

    @Override
    @Transactional
    public AddressDTO updateAddress(Long addressId, AddressDTO addressDTO) {
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address", "id", addressId));

        address.setStreet(addressDTO.getStreet());
        address.setCity(addressDTO.getCity());
        address.setPostalCode(addressDTO.getPostalCode());
        address.setCountry(addressDTO.getCountry());

        Address updatedAddress = addressRepository.save(address);

        return modelMapper.map(updatedAddress, AddressDTO.class);
    }

    @Override
    public String deleteAddress(Long addressId) {
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address", "id", addressId));
        addressRepository.delete(address);
        return "Address deleted successfully with id: " + addressId;
    }
}
