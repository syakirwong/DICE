package com.alliance.diceruleengine.request;

import java.util.Set;

import lombok.Data;

@Data
public class CreateRuleRequest {
    private String ruleName;
    private Set<Integer> ruleTemplateId;
}
