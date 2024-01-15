package com.alliance.diceintegration.repository;

import com.alliance.diceintegration.constant.DataField.Status;
import com.alliance.diceintegration.model.ReferralCode;

import java.util.List;
import java.util.UUID;

import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.data.cassandra.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ReferralCodeRepository extends CassandraRepository<ReferralCode, UUID> {

    // @Query(("SELECT MAX(referralCodeId) FROM EVT_REFERRAL_CODE_LIST"))
    // Integer getMaxReferralCodeId();

    // @Query("SELECT COUNT(*) FROM EVT_REFERRAL_CODE_LIST WHERE status=:status ALLOW FILTERING")
    // Integer countByStatus(Status status);

    @Query("SELECT COUNT(*) FROM EVT_REFERRAL_CODE_LIST WHERE codevalue=:codeValue AND status IN (:status) ALLOW FILTERING")
    Integer countByCodeValueAndStatusIn(String codeValue, List<Status> status);

    @Query("SELECT COUNT(*) FROM EVT_REFERRAL_CODE_LIST  WHERE codevalue=:codeValue AND status=:status ALLOW FILTERING")
    Integer countByCodeValueAndStatus(String codeValue, Status status);

}
