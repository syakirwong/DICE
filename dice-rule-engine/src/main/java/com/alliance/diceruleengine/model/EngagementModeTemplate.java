package com.alliance.diceruleengine.model;

import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

import com.alliance.diceruleengine.constant.DataField.Status;

import lombok.Data;

@Data
@Table("EVT_ENGAGEMENT_MODE_TEMPLATE")
public class EngagementModeTemplate extends BaseInfo {
    @PrimaryKey
    private Integer engagementModeTemplateId;
    private Status templateStatus;
    private String engagementModeName;
}

