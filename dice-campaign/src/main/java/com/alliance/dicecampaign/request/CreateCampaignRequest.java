package com.alliance.dicecampaign.request;

import java.util.Date;
import java.util.Map;
import java.util.Set;

import lombok.Data;

@Data
public class CreateCampaignRequest extends BaseRequest{
    private String campaignName;
    private Integer campaignPriority;
    private String campaignStatus;
    private Date startDate;
    private Date endDate;
    private String description;
    private Set<Integer> diceActionTemplateId;
    private Map<String, String> campaignProperties;
}
