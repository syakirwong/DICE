package com.alliance.diceanalytics.repository;

import com.alliance.diceanalytics.model.UploadedFileHistory;
import org.springframework.data.cassandra.repository.AllowFiltering;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.data.cassandra.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Date;

@Repository
public interface UploadedFileHistoryRepository extends CassandraRepository<UploadedFileHistory, Integer> {
    @Query(("SELECT MAX(UPLOADED_FILE_ID) FROM EVT_UPLOADED_FILE_HISTORY_LOG"))
    Integer getMaxUploadedFileId();

    @AllowFiltering
    @Query("select SUM(total_row) from EVT_UPLOADED_FILE_HISTORY_LOG WHERE created_on >= :startDate AND created_on <= :endDate ALLOW FILTERING")
    Integer getSumOfTotalRowByDate(Date startDate, Date endDate);
}
