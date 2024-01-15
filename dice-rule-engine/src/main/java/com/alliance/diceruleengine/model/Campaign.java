package com.alliance.diceruleengine.model;

import java.util.Date;
import java.util.Map;
import java.util.Set;

import javax.persistence.Entity;

import com.alliance.diceruleengine.constant.DataField.Status;

import lombok.Data;

@Data
@Entity
public class Campaign extends BaseInfo {
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
