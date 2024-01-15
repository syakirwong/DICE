package com.alliance.diceintegration.model;

import lombok.Data;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

import com.alliance.diceintegration.constant.DataField.Status;

@Data
@Table("EVT_ENGAGEMENT_MODE_TEMPLATE")
public class EngagementModeTemplate extends BaseInfo {
    @PrimaryKey
    private Integer engagementModeTemplateId;
    private String engagementModeName;
    private Status templateStatus;

}
