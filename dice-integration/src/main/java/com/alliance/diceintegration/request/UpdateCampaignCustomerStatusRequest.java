package com.alliance.diceintegration.request;

import javax.annotation.Nullable;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import lombok.Data;

@Data
public class UpdateCampaignCustomerStatusRequest {
    @NotEmpty
	@NotNull
	private String cifNo;
	
	@NotEmpty
	@NotNull
	private String campaignId;

    Boolean isCampaignUpdated = false;

    Boolean isIgnore = false;

	@Nullable
	String idType;
	
	@Nullable
	String engagementMode;
}
