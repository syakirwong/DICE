package com.alliance.dicerecommendation.response;

import java.util.Date;
import java.util.Map;
import javax.annotation.Nullable;

import lombok.Data;
@Data
public class CampaignDetailResponse {
    @Nullable
    private Integer campaignId;
    @Nullable
    private Integer campaignPriority;
    @Nullable
    private String campaignName;
    @Nullable
    private String description;
    @Nullable
    private Date startDate;
    @Nullable
    private Date endDate;
    @Nullable
    private Map<String, String> campaignProperties;
}
