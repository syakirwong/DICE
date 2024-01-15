package com.alliance.diceanalytics.repository;

import com.alliance.diceanalytics.model.AuditTrail;
import org.springframework.data.cassandra.repository.AllowFiltering;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.data.cassandra.repository.Query;

import java.util.Date;
import java.util.UUID;

public interface AuditTrailRepository extends CassandraRepository<AuditTrail, UUID> {


    @AllowFiltering
    @Query("SELECT COUNT(*) FROM EVT_AUDIT_TRAIL_LOG WHERE CREATED_ON >= :startDate AND CREATED_ON <= :endDate AND event=:event AND REQUEST_STATUS='EXCEPTION' ALLOW FILTERING")
    public Integer countErrorByEventAndDuration(String event, Date startDate, Date endDate);


    @AllowFiltering
    @Query("SELECT COUNT(*) FROM EVT_AUDIT_TRAIL_LOG WHERE CREATED_ON >= :startDate AND CREATED_ON <= :endDate AND event=:event AND REQUEST_STATUS='EXCEPTION' AND IS_SENT_EMAIL = true ALLOW FILTERING")
    public Integer countEmailSentByEventAndDuration(String event, Date startDate, Date endDate);


    @AllowFiltering
    @Query("UPDATE EVT_AUDIT_TRAIL_LOG SET IS_SENT_EMAIL = true WHERE audit_trail_log_id= :id")
    public void updateEmailStatusByUUID(UUID id);


}
