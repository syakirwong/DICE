package com.alliance.diceintegration.response;

import lombok.Data;

@Data
public class ReferralCodeResponse {
    private Integer referralCodeId;
    private String codeValue;
    private String cifNo;
    private Integer campaignId;
    private String eformUuid;
    private String campaignJourneyId;
}
