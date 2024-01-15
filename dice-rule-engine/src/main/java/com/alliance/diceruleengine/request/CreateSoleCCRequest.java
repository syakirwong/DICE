package com.alliance.diceruleengine.request;

import lombok.Data;

@Data
public class CreateSoleCCRequest {
    private String cifNo;
    private String userId;
    private String idNo;
    private String fullName;
    private String mobile;
    private String deviceUUID;
    private String devicePlatform;
}
