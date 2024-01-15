package com.alliance.dicerecommendation.repository;

import com.alliance.dicerecommendation.model.CampaignSchedule;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.data.cassandra.repository.Query;

import java.util.List;
import java.util.UUID;

public interface CampaignScheduleRepository extends CassandraRepository<CampaignSchedule, UUID> {

    @Query("SELECT * FROM evt_campaign_schedule WHERE schedule_status = 'PENDING' LIMIT 1 ALLOW FILTERING")
    CampaignSchedule findPendingFiles();

    @Query("SELECT * FROM evt_campaign_schedule WHERE campaign_schedule_id = :campaignScheduleUUID")
    CampaignSchedule getCampaignScheduleByUUID(UUID campaignScheduleUUID);

    @Query("SELECT * FROM evt_campaign_schedule WHERE campaign_id = :campaignId ALLOW FILTERING")
    List<CampaignSchedule> findCampaignSchedulesByCampaignId(Integer campaignId);
}
