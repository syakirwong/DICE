package com.alliance.diceruleengine.repository;

import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.data.cassandra.repository.Query;
import org.springframework.stereotype.Repository;

import com.alliance.diceruleengine.model.Rule;

@Repository
public interface RuleRepository extends CassandraRepository<Rule, Integer> {
    @Query(("SELECT MAX(ruleId) FROM EVT_RULE_LIST"))
    Integer getMaxRuleId();
}
