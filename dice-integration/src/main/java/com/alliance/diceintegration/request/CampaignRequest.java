package com.alliance.diceintegration.request;

import com.alliance.diceintegration.constant.DataField.Status;
import lombok.Data;

import java.util.Date;
import java.util.Map;
import java.util.Set;

@Data
public class CampaignRequest extends BaseRequest{
    private Integer campaignId;
    private String campaignName;
    private Integer campaignPriority;
    private Status campaignStatus;
    private Date startDate;
    private Date endDate;
    private String description;
    private Set<Integer> diceActionTemplateId;
    private Map<String, String> campaignProperties;
}
