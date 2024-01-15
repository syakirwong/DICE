package com.alliance.diceintegration.response;

import lombok.Data;

@Data
public class CampaignCheckResponse {
    private Integer campaignId;
    private Boolean isValid;
    private Boolean isExpired;
    private Boolean isDisable;
}
