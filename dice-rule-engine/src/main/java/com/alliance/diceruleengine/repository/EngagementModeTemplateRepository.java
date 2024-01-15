package com.alliance.diceruleengine.repository;

import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.data.cassandra.repository.Query;
import org.springframework.stereotype.Repository;

import com.alliance.diceruleengine.model.EngagementModeTemplate;

@Repository
public interface EngagementModeTemplateRepository extends CassandraRepository<EngagementModeTemplate, Integer> {

    @Query(("SELECT MAX(engagementModeTemplateId) FROM EVT_ENGAGEMENT_MODE_TEMPLATE"))
    Integer getMaxEngagementModeTemplateId();
}
