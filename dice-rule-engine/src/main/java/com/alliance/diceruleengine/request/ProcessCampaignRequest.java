package com.alliance.diceruleengine.request;

import java.util.Date;

import com.alliance.diceruleengine.model.CustomerProfile;

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

	private Boolean isProcess;

	private String tableView;

	private Integer campaignId;
	
    private CustomerProfile referrerProfile;

    private CustomerProfile refereeProfile;
}

