package com.alliance.dicecampaign.model;

import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

import com.alliance.dicecampaign.constant.DataField.Status;

import lombok.Data;

@Data
@Table("EVT_CAMPAIGN_HISTORY_LOG")
public class CampaignHistoryLog extends BaseInfo {
    @PrimaryKey
    private Integer campaignHistoryLogId;
    private Status campaignStatusBefore;
    private Status campaigStatusAfter;
    private String description;
    private Integer campaignId;

}
