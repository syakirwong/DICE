package com.alliance.diceanalytics.response;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;

@Data
public class CustomerProfileResponse {
    private String _type;

    @JsonAlias("new_ic_no")
    private String nric;

    @JsonAlias("cif_no")
    private String cifNo;

    @JsonAlias("gender")
    private String gender;

    @JsonAlias("mobile_first_platform_id")
    private String deviceUuid;

    @JsonAlias("nationality")
    private String nationality;

    @JsonAlias("user_id")
    private String userId;

    @JsonAlias("id_no")
    private String idNo;

    @JsonAlias("dob")
    private String dob;

    @JsonAlias("mobile")
    private String mobile;

    @JsonAlias("device_platform")
    private String devicePlatform;

    @JsonAlias("customer_name")
    private String fullName;

    @JsonAlias("package_id")
    private String packageType;

    @JsonAlias("email")
    private String email;


}

