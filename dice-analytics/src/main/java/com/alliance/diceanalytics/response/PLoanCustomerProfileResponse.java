package com.alliance.diceanalytics.response;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class PLoanCustomerProfileResponse {

    @JsonProperty("UUID")
    @JsonAlias("uuid")
    private String uuid;

    @JsonProperty("NRIC_NO")
    @JsonAlias("nric_no")
    private String idNo;

    @JsonProperty("FULL_NAME")
    @JsonAlias("full_name")
    private String fullName;

    @JsonProperty("PROMO_CODE")
    @JsonAlias("promo_code")
    private String promoCode;

    @JsonProperty("MOBILE_NO")
    @JsonAlias("mobile_no")
    private String mobileNo;

    @JsonProperty("DEVICE_UUID")
    @JsonAlias("device_uuid")
    private String deviceUUID;

    @JsonProperty("DEVICE_PLATFORM")
    @JsonAlias("device_platform")
    private String devicePlatform;

    @JsonProperty("IS_PDPA_CONSENT")
    @JsonAlias("is_pdpa_consent")
    private String isPDPAConsent;

    @JsonProperty("IS_NTA")
    @JsonAlias("is_nta")
    private String isNTA;
}
