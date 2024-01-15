package com.alliance.dicerecommendation.response;

import javax.annotation.Nullable;

import lombok.Data;

@Data
public class CustomerProfileResponse {
    @Nullable
    private String _type;
    @Nullable
    private String cifNo;
    @Nullable
    private String userId;
    @Nullable
    private String idNo;
    @Nullable
    private String fullName;
    @Nullable
    private String mobileNo;
    @Nullable
    private String deviceUuid;
    @Nullable
    private String devicePlatform;
    @Nullable
    private String packageType;
    @Nullable
    private String dob;
    @Nullable
    private String gender;
}

