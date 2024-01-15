package com.alliance.diceruleengine.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.data.cassandra.repository.Query;
import org.springframework.stereotype.Repository;

import com.alliance.diceruleengine.constant.DataField.Status;
import com.alliance.diceruleengine.model.ReferralCode;

@Repository
public interface ReferralCodeRepository extends CassandraRepository<ReferralCode, UUID> {

    // @Query(("SELECT MAX(referralCodeId) FROM EVT_REFERRAL_CODE_LIST"))
    // Integer getMaxReferralCodeId();

    @Query(("SELECT * FROM EVT_REFERRAL_CODE_LIST WHERE codevalue = :referralCode AND status = :status ALLOW FILTERING"))
    ReferralCode getReferralCodeByValue(String referralCode, Status status);

    @Query("SELECT * FROM EVT_REFERRAL_CODE_LIST WHERE campaignid = :campaignId AND cifno = :cifNo AND status = 'ACTIVE' ALLOW FILTERING")
    ReferralCode getReferralCodeBasedOnCampaignIdAndCifNo(Integer campaignId, String cifNo);

    @Query("SELECT * FROM EVT_REFERRAL_CODE_LIST WHERE campaignid = :campaignId AND eformuuid = :eformUuid AND status = 'ACTIVE' ALLOW FILTERING")
    ReferralCode getReferralCodeBasedOnCampaignIdAndEformUuid(Integer campaignId, String eformUuid);

    @Query("SELECT * FROM EVT_REFERRAL_CODE_LIST WHERE cifno = :cifNo AND status = 'ACTIVE' LIMIT 1 ALLOW FILTERING")
    ReferralCode getReferralCodeBasedOnCifNo(String cifNo);

    @Query("SELECT * FROM EVT_REFERRAL_CODE_LIST WHERE eformuuid = :eformUuid AND status = 'ACTIVE' LIMIT 1 ALLOW FILTERING")
    ReferralCode getReferralCodeBasedOnEformUuid(String eformUuid);

    @Query("SELECT COUNT(*) FROM EVT_REFERRAL_CODE_LIST  WHERE codevalue=:codeValue AND status=:status ALLOW FILTERING")
    Integer countByCodeValueAndStatus(String codeValue, Status status);
}