package com.alliance.diceanalytics.model;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.cassandra.core.query.CriteriaDefinition;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class QueryCondition {
    private String fieldName;
    private Object fieldValue;
    private CriteriaDefinition.Operators fieldComparator;
}
