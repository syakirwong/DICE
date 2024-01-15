package com.alliance.dicerecommendation.repository;

import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.stereotype.Repository;

import com.alliance.dicerecommendation.model.RecommendationHistoryLog;

@Repository
public interface RecommendationHistoryLogRepository extends CassandraRepository<RecommendationHistoryLog, Integer> {

}
