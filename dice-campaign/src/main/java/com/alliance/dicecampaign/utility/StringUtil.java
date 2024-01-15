package com.alliance.dicecampaign.utility;

import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class StringUtil {
    public String generateRandomString(Integer length, boolean useLetters, boolean useNumbers) {
        String randomString = RandomStringUtils.random(length, useLetters, useNumbers);
    
        return randomString;
    }
}
