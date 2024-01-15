package com.alliance.dicerecommendation.repository;

import java.util.List;

import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.data.cassandra.repository.Query;
import org.springframework.stereotype.Repository;

import com.alliance.dicerecommendation.model.Recommendation;

@Repository
public interface RecommendationRepository extends CassandraRepository<Recommendation, Integer> {
    @Query(("SELECT * FROM EVT_RECOMMENDATION_LIST WHERE CIF_NO = :cifNo AND ENGAGEMENT_MODE CONTAINS :engagementMode AND IS_IGNORE = false AND IS_CAMPAIGN_UPDATED = false ALLOW FILTERING"))
    List<Recommendation> getByCifNoAndEngagementMode(String cifNo, String engagementMode);

    @Query(("SELECT * FROM EVT_RECOMMENDATION_LIST WHERE CIF_NO = :cifNo AND ENGAGEMENT_MODE CONTAINS :engagementMode AND CAMPAIGN_ID = :campaignId AND TRIGGER_STATUS = 'COMPLETED' LIMIT 1 ALLOW FILTERING"))
    Recommendation getByCifNoAndCampaignIdAndEngagementModeForCompleted(String cifNo, String engagementMode,
            Integer campaignId);

    @Query(("SELECT * FROM EVT_RECOMMENDATION_LIST WHERE CIF_NO = :cifNo AND ENGAGEMENT_MODE CONTAINS :engagementMode AND CAMPAIGN_ID = :campaignId AND TRIGGER_STATUS = 'NON_TRIGGER' LIMIT 1 ALLOW FILTERING"))
    Recommendation getByCifNoAndCampaignIdAndEngagementModeForNonTrigger(String cifNo, String engagementMode,
            Integer campaignId);

    @Query("SELECT COUNT(*) FROM EVT_RECOMMENDATION_LIST WHERE CIF_NO = :cifNo AND CAMPAIGN_ID = :campaignId ALLOW FILTERING")
    Integer countByCifNoAndCampaignId(String cifNo, Integer campaignId);

    @Query("SELECT * FROM EVT_RECOMMENDATION_LIST WHERE CIF_NO = :cifNo AND CAMPAIGN_ID = :campaignId ALLOW FILTERING")
    List<Recommendation> findByCifNoAndCampaignId(String cifNo, Integer campaignId);

    @Query("SELECT * FROM EVT_RECOMMENDATION_LIST WHERE PUSH_NOTIS_SEND_DATE_TIME < dateOf(NOW()) AND IS_TRIGGERED = false AND TRIGGER_STATUS = 'NEW' ALLOW FILTERING")
    List<Recommendation> getEngagementTriggerListFilterByCurrentDateTime();

    @Query("SELECT * FROM EVT_RECOMMENDATION_LIST WHERE CIF_NO = :cifNo AND CAMPAIGN_ID = :campaignId AND IS_CAMPAIGN_UPDATED = false AND IS_IGNORE = false LIMIT 1 ALLOW FILTERING")
    Recommendation getByCifNoAndCampaignId(String cifNo, Integer campaignId);

    @Query(("SELECT * FROM EVT_RECOMMENDATION_LIST WHERE CIF_NO = :nric AND ENGAGEMENT_MODE CONTAINS :engagementMode AND IS_CAMPAIGN_UPDATED = :isCampaignUpdated LIMIT 1 ALLOW FILTERING"))
    Recommendation getByNricAndEngagementModeAndIsUpdated(String nric, String engagementMode,
            Boolean isCampaignUpdated);

    @Query(("SELECT * FROM EVT_RECOMMENDATION_LIST WHERE CIF_NO = :nric AND ENGAGEMENT_MODE CONTAINS :engagementMode LIMIT 1 ALLOW FILTERING"))
    Recommendation getByNricAndEngagementMode(String nric, String engagementMode);

    @Query("SELECT * FROM EVT_RECOMMENDATION_LIST WHERE ENGAGEMENT_MODE CONTAINS :engagementMode AND CIF_NO = :nric AND CAMPAIGN_ID = :campaignId AND IS_CAMPAIGN_UPDATED = false AND IS_IGNORE = false LIMIT 1 ALLOW FILTERING")
    Recommendation getByNricAndCampaignIdAndEngagementMode(String nric, Integer campaignId, String engagementMode);
};
