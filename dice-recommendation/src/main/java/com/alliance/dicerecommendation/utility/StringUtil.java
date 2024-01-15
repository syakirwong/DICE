package com.alliance.dicerecommendation.utility;

import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Component;

@Component
public class StringUtil {

    public static String generateRandomString(Integer length, boolean useLetters, boolean useNumbers) {
        String randomString = RandomStringUtils.random(length, useLetters, useNumbers);
    
        return randomString;
    }
}
