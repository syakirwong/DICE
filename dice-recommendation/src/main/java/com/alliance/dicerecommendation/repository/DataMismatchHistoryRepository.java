package com.alliance.dicerecommendation.repository;

import com.alliance.dicerecommendation.model.DataMismatchHistoryLog;

import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.data.cassandra.repository.Query;

import java.util.UUID;

public interface DataMismatchHistoryRepository extends CassandraRepository<DataMismatchHistoryLog, UUID> {

    @Query("SELECT * FROM evt_data_mismatch_history_log WHERE data_mismatch_history_log_id = :dataMismatchHistoryUUID")
    DataMismatchHistoryLog getDataMismatchHistoryByUUID(UUID dataMismatchHistoryUUID);
}
