package com.alliance.dicecampaign.model;

import java.util.Date;
import java.util.Map;
import java.util.Set;

import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

import com.alliance.dicecampaign.constant.DataField.Status;

import lombok.Data;

@Data
@Table("EVT_CAMPAIGN_LIST")
public class Campaign extends BaseInfo {
    @PrimaryKey
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
