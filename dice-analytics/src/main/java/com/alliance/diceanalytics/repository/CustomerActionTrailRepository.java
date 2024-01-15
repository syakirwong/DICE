package com.alliance.diceanalytics.repository;

import com.alliance.diceanalytics.model.CustomerActionTrail;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.data.cassandra.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.UUID;

@Repository
public interface CustomerActionTrailRepository extends CassandraRepository<CustomerActionTrail, UUID> {
    CustomerActionTrail findByCifNo(String cifNo);

    @Query("SELECT * FROM evt_customer_action_trail_log WHERE session_id = ?0 AND action = ?1 ALLOW FILTERING")
    CustomerActionTrail findAllBySessionIdAndAction(String sessionId, String action);

    @Query("SELECT COUNT(*) FROM evt_customer_action_trail_log WHERE engagement_mode CONTAINS :engagementMode AND action = :action AND is_login = true AND action_status = 'TAPPED' AND event = 'PERSONAL_INFO_UPDATE' AND created_on >= :startDate AND created_on < :endDate ALLOW FILTERING")
    Integer countByEngagementModeAndIsLoginAndActionStatusAndEvent(Date startDate, Date endDate, String engagementMode, String action);

    @Query("SELECT MAX(created_on) FROM evt_customer_action_trail_log WHERE action = 'PLOAN_FULL_SUBMIT' and action_status ='SUCCESS' and cif_no=:cifNo ALLOW FILTERING")
    CustomerActionTrail getMaxFullSubmissionDateByCif(String cifNo);

    @Query("SELECT MAX(created_on) AS created_on FROM evt_customer_action_trail_log WHERE action = 'PLOAN_AIP_SUBMIT'  and action_status ='REACHED' and is_login=true and device_id=:deviceID ALLOW FILTERING")
    CustomerActionTrail getMaxAIPSubmissionDateByDevice(String deviceID);


}
