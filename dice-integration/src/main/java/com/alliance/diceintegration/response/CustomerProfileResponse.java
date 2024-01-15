package com.alliance.diceintegration.response;

import lombok.Data;

@Data
public class CustomerProfileResponse {

    private String to;

    private String devicePlatform;

    private String deviceUuid;

    private String fullName;

    private String idNo;

    private String email;

    private String applicationStageCode;

    private String applicationDateTime;

    private String mobileStatus;

    private String invitationCode;

    private String applicationLastUpdate;
}

