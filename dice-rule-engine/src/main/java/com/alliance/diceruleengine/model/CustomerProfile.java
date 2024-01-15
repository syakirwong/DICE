package com.alliance.diceruleengine.model;

import lombok.Data;

@Data
public class CustomerProfile {
    private String uuid;
    private String idNo;
    private String fullName;
    private String promoCode;
    private String mobile;
    private String deviceUuid;
    private String devicePlatform;
    private String completedOn;
    private String statusCode;
    private String pdpaFlag;
}