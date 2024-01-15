package com.alliance.diceintegration.request;

import edu.umd.cs.findbugs.annotations.Nullable;
import lombok.Data;

@Data
public class CampaignJourneyRequest {
    @Nullable
    private String referenceId;
    @Nullable
    private String statusCode;
    @Nullable
    private String channel;
    @Nullable
    private String transactionType;
    @Nullable
    private String transactionDesc;
    @Nullable
    private String tableView;
    @Nullable
    private Boolean isProcess = false;
    @Nullable
    private String status;
    @Nullable
    private String campaignId;

}