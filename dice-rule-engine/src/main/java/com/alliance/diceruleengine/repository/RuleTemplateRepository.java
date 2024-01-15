package com.alliance.diceruleengine.repository;

import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.data.cassandra.repository.Query;
import org.springframework.stereotype.Repository;

import com.alliance.diceruleengine.model.RuleTemplate;

@Repository
public interface RuleTemplateRepository extends CassandraRepository<RuleTemplate, Integer> {
    public RuleTemplate getRuleTemplateByRuleTemplateId(Integer ruleTemplateId);

    @Query(("SELECT MAX(ruleTemplateId) FROM EVT_RULE_TEMPLATE"))
    Integer getMaxRuleTemplateId();
}
