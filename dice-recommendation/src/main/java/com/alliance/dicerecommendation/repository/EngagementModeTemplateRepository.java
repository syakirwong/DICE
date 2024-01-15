package com.alliance.dicerecommendation.repository;

import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.data.cassandra.repository.Query;
import org.springframework.stereotype.Repository;

import com.alliance.dicerecommendation.model.EngagementModeTemplate;

@Repository
public interface EngagementModeTemplateRepository extends CassandraRepository<EngagementModeTemplate, Integer> {
    @Query("SELECT engagementModeName FROM EVT_ENGAGEMENT_MODE_TEMPLATE WHERE engagementModeTemplateId = :engagementModeId ALLOW FILTERING")
    String getEngagementModeNameById(Integer engagementModeId);

    @Query(("SELECT MAX(engagementModeTemplateId) FROM EVT_ENGAGEMENT_MODE_TEMPLATE"))
    Integer getMaxEngagementModeTemplateId();
}
