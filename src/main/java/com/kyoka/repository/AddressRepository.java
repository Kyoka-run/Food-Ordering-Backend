package com.kyoka.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.kyoka.model.Address;

public interface AddressRepository extends JpaRepository<Address, Long> {

}
