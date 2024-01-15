package com.alliance.diceruleengine.repository;

import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.data.cassandra.repository.Query;
import org.springframework.stereotype.Repository;

import com.alliance.diceruleengine.model.DiceActionTemplate;

@Repository
public interface DiceActionTemplateRepository extends CassandraRepository<DiceActionTemplate, Integer> {
    
    @Query(("SELECT MAX(diceActionTemplateId) FROM EVT_DICE_ACTION_TEMPLATE"))
    Integer getMaxDiceActionTemplateId();
}
