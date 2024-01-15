package com.alliance.dicerecommendation.response;

import javax.annotation.Nullable;

import lombok.Data;

@Data
public class RecommendationResponse {
    @Nullable
    private String engagementMode;
    @Nullable
    private String updateSequence;
    @Nullable
    private String highRisk;
    @Nullable
    private String campaignId;
    @Nullable
    private String inAppMessageTemplateId;
    @Nullable
    private Boolean isCampaignUpdated = false;
    @Nullable
    private Boolean isIgnore = false;
    @Nullable
    private String floatingBtnImg;
    @Nullable
    private String messageDisplayMethod;
    @Nullable
    private Boolean isBase64Content=false;
    @Nullable
    private PermenantLinkResponse permanentLink;
    @Nullable
    private PloanAssetsResponse ploanAssest;
}
