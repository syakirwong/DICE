package com.alliance.diceruleengine.utility;

import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alliance.diceruleengine.constant.DataField.Status;
import com.alliance.diceruleengine.repository.ReferralCodeRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class StringUtil {

    @Autowired
    private ReferralCodeRepository referralCodeRepository;
    
    public String generateRandomString(Integer length, boolean useLetters, boolean useNumbers) {
        Integer generateCodeAttempt = 10;
        String referralCode = null;
        for (int i = 0; i < generateCodeAttempt; i++) {
            // Integer digit = MathUtil
            //         .getHighestAvailableDigitForAlphaNumeric(referralCodeRepository.countByStatus(Status.ACTIVE));
            Integer digit = (i > 3) ? 7 : length; // Set digit to 7 if generateCodeAttempt > 3, otherwise default
            referralCode = RandomStringUtils.random(digit, useLetters, useNumbers);
           
            Integer countByCodeValueAndStatus = referralCodeRepository.countByCodeValueAndStatus(referralCode,
                    Status.ACTIVE) + referralCodeRepository.countByCodeValueAndStatus(referralCode, Status.DISABLE);
            if (countByCodeValueAndStatus == 0) {
                log.info("referralProcess - generateCodeAttempt = {} , referralCode = {}", i, referralCode);
                break;
            } else {
                log.info("referralProcess - code exist, retry generateCodeAttempt = {}", i);
                referralCode = null;
            }
        }

        return referralCode;
    }
}
