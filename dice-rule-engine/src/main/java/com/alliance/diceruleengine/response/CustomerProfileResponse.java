package com.alliance.diceruleengine.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class CustomerProfileResponse {
    @JsonProperty("user_id")
    private String userId;

    @JsonProperty("cif_no")
    private String cifNo;

    @JsonProperty("new_ic_no")
    private String newIcNo;

    @JsonProperty("customer_name")
    private String customerName;

    @JsonProperty("mobile")
    private String mobile;

    @JsonProperty("package_id")
    private String packageId;

    @JsonProperty("dob")
    private String dob;

    @JsonProperty("gender")
    private String gender;

    @JsonProperty("email")
    private String email;

    @JsonProperty("nationality")
    private String nationality;
    
    @JsonProperty("mobile_first_platform_id")
    private String mobileFirstPlatformId;

    @JsonProperty("device_platform")
    private String devicePlatform;
}
