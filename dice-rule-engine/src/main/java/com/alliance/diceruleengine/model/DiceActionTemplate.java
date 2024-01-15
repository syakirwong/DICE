package com.alliance.diceruleengine.model;

import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

import lombok.Data;

@Data
@Table("EVT_DICE_ACTION_TEMPLATE")
public class DiceActionTemplate extends BaseInfo{
    @PrimaryKey
    private Integer diceActionTemplateId;
    private String diceActionName;
    private String description;
}
