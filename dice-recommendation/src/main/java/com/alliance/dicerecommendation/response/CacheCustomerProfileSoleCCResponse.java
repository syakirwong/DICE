package com.alliance.dicerecommendation.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import javax.annotation.Nullable;
import lombok.Data;

@Data
public class CacheCustomerProfileSoleCCResponse {
    @Nullable
    @JsonProperty("user_id")
    private String userId;

    @Nullable
    @JsonProperty("cif_no")
    private String cifNo;

    @Nullable
    @JsonProperty("new_ic_no")
    private String newIcNo;

    @Nullable
    @JsonProperty("customer_name")
    private String customerName;

    @Nullable
    @JsonProperty("mobile")
    private String mobile;

    @Nullable
    @JsonProperty("package_id")
    private String packageId;

    @Nullable
    @JsonProperty("dob")
    private String dob;

    @Nullable
    @JsonProperty("gender")
    private String gender;

    @Nullable
    @JsonProperty("email")
    private String email;

    @Nullable
    @JsonProperty("nationality")
    private String nationality;

    @Nullable
    @JsonProperty("mobile_first_platform_id")
    private String mobileFirstPlatformId;

    @Nullable
    @JsonProperty("device_platform")
    private String devicePlatform;
}
