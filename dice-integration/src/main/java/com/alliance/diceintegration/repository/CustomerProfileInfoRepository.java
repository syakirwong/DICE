package com.alliance.diceintegration.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.alliance.diceintegration.model.CustomerProfileInfo;

@Repository
public interface CustomerProfileInfoRepository extends JpaRepository<CustomerProfileInfo, Integer> {

    CustomerProfileInfo findByEformUuid(String id);

    List<CustomerProfileInfo> findByDeviceUuid(String id);

    @Query("SELECT l FROM CustomerProfileInfo l WHERE l.mobileNumber LIKE %:num")
    List<CustomerProfileInfo> findByMobileNumber(String num);

}
