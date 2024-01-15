package com.alliance.dicerecommendation.repository;

import java.util.UUID;

import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.data.cassandra.repository.Query;
import org.springframework.stereotype.Repository;

import com.alliance.dicerecommendation.model.EngagementHistory;

@Repository
public interface EngagementHistoryRepository extends CassandraRepository<EngagementHistory, String> {

    @Query(value = "SELECT * from EVT_ENGAGEMENT_HISTORY_LOG WHERE ENGAGEMENT_HISTORY_LOG_ID=:uuid")
    public EngagementHistory findByEngagementSentId(UUID uuid);
}
