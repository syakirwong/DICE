package com.alliance.diceintegration.repository;

import com.alliance.diceintegration.model.EngagementModeTemplate;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EngagementModeRepository extends CassandraRepository<EngagementModeTemplate,Integer> {


    
}
