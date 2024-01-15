package com.alliance.dicerecommendation.response;

import lombok.Data;

@Data
public class CheckCampaignResponse {
    private Integer campaignId;
    private Boolean isValid = true;
    private Boolean isExpired = false;
    private Boolean isDisable = false;
}
