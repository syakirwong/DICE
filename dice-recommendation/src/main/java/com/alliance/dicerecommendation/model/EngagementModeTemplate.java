package com.alliance.dicerecommendation.model;

import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

import com.alliance.dicerecommendation.model.DataField.Status;

import lombok.Data;

@Data
@Table("EVT_ENGAGEMENT_MODE_TEMPLATE")
public class EngagementModeTemplate extends BaseInfo {
    @PrimaryKey
    private Integer engagementModeTemplateId;
    private String engagementModeName;
    private Status templateStatus;

}
