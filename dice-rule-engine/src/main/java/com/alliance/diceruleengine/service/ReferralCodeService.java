package com.alliance.diceruleengine.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alliance.diceruleengine.constant.DataField.Status;
import com.alliance.diceruleengine.model.ReferralCode;
import com.alliance.diceruleengine.repository.ReferralCodeRepository;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ReferralCodeService {

    @Autowired
    ReferralCodeRepository referralCodeRepository;

    public ReferralCode getReferralCode(String referralCode, Status status) {

        if (referralCode != null && status != null) {
            ReferralCode code = referralCodeRepository.getReferralCodeByValue(referralCode, status);
            return code;
        } else {
            return null;
        }
        
    }

    public ReferralCode getExistingCodeBasedOnUuid(String uuidType, String uuid) {
        ReferralCode existingReferralCode = null;
        if (uuidType.equals("CIF_NO")) {
            log.info("getExistingCodeBasedOnUuid - CIF_NO: {}", uuid);
            existingReferralCode = referralCodeRepository.getReferralCodeBasedOnCifNo(
                    uuid);
        } else if (uuidType.equals("EFORM_UUID")) {
            log.info("getExistingCodeBasedOnUuid - EFORM_UUID: {}", uuid);
            existingReferralCode = referralCodeRepository.getReferralCodeBasedOnEformUuid(
                    uuid);
        }

        return existingReferralCode;
    }

    public ReferralCode getExistingCodeBasedOnCampaignIdAndCifNo(String campaignId, String cifNo) {
        Integer campaignid = Integer.parseInt(campaignId);
        ReferralCode existingReferralCode = referralCodeRepository.getReferralCodeBasedOnCampaignIdAndCifNo(campaignid,
                cifNo);
        return existingReferralCode;
    }

    public ReferralCode getExistingCodeBasedOnCampaignIdAndEformUuid(String campaignId, String eformUuid) {
        Integer campaignid = Integer.parseInt(campaignId);
        ReferralCode existingReferralCode = referralCodeRepository.getReferralCodeBasedOnCampaignIdAndEformUuid(
                campaignid,
                eformUuid);
        return existingReferralCode;
    }

    public ReferralCode createReferralCode(
            String codeValue,
            Integer campaignId,
            String cifNo) {

    

        ReferralCode referral = new ReferralCode();
        referral.setCampaignId(campaignId);
        referral.setCodeValue(codeValue);
        referral.setCifNo(cifNo);

        return referralCodeRepository.save(referral);
    }
}
