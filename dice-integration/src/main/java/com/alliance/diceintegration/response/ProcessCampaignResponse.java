package com.alliance.diceintegration.response;

import java.util.Date;

import javax.annotation.Nullable;

import com.alliance.diceintegration.constant.DataField.Status;

import lombok.Data;

@Data
public class ProcessCampaignResponse {
    private Integer campaignId;
    private Date startDate;
    private Date endDate;
    private Status campaignStatus;
    private String transactionType;
    private Boolean isValid;
    @Nullable
    private String pandaiBroadcastFlowName;
    @Nullable
    private String pandaiBroadcastFlowNameReferral;
}
