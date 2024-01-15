package com.alliance.diceruleengine.model;

import java.util.Set;

import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

import lombok.Data;

@Data
@Table("EVT_RULE_LIST")
public class Rule extends BaseInfo {
    @PrimaryKey
    private Integer ruleId;
    private String ruleName;
    private Set<Integer> ruleTemplateId;
}
