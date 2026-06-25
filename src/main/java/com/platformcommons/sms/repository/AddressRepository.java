package com.platformcommons.sms.repository;

import com.platformcommons.sms.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AddressRepository extends JpaRepository<Address, Long> {
}