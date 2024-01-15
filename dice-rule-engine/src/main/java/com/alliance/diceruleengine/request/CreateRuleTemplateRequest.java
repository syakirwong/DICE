package com.alliance.diceruleengine.request;

import com.alliance.diceruleengine.constant.DataField.RuleType;

import lombok.Data;

@Data
public class CreateRuleTemplateRequest {
    private RuleType ruleType;
    private String description;
    private String tableName;
    private String key;
    private String value;
}
