package com.alliance.diceintegration.request;

import java.util.Date;

import com.alliance.diceintegration.model.CustomerProfile;

import lombok.Data;

@Data
public class ProcessCampaignRequest {
    private String campaignJourneyId;

	private String referenceId;

	private String statusCode;

	private Date createdOn;

	private String channel;

	private String transactionType;

	private String transactionDesc;

	private Boolean isProcess = false;

	private String tableView;

	private Integer campaignId;
	
    private CustomerProfile referrerProfile;

    private CustomerProfile refereeProfile;
}
