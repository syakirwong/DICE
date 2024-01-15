package com.alliance.diceruleengine.model;

import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

import com.alliance.diceruleengine.constant.DataField.RuleType;

import lombok.Data;

@Data
@Table("EVT_RULE_TEMPLATE")
public class RuleTemplate extends BaseInfo {
    @PrimaryKey
    private Integer ruleTemplateId;
    private RuleType ruleType;
    private String description;
    private String tableName;
    private String key;
    private String value;

}
