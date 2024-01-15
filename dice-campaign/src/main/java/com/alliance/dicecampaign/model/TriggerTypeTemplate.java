package com.alliance.dicecampaign.model;

import org.springframework.data.cassandra.core.mapping.PrimaryKey;

import com.alliance.dicecampaign.constant.DataField.Status;

import lombok.Data;

@Data
public class TriggerTypeTemplate extends BaseInfo {
    @PrimaryKey
    private Integer triggerTypeTemplateId;
    private Status templateStatus;
    private String description;

}
