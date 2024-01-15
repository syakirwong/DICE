package com.alliance.dicecampaign.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.data.cassandra.repository.Query;

import com.alliance.dicecampaign.constant.DataField.Status;
import com.alliance.dicecampaign.model.Campaign;

public interface CampaignRepository extends CassandraRepository<Campaign, Integer> {

    @Query(("SELECT MAX(campaignId) FROM EVT_CAMPAIGN_LIST"))
    Integer getMaxCampaignId();

    @Query("SELECT * FROM EVT_CAMPAIGN_LIST WHERE campaignName = :campaignName ALLOW FILTERING")
    Optional<Campaign> findByName(String campaignName);

    @Query("SELECT * FROM EVT_CAMPAIGN_LIST WHERE campaignProperties CONTAINS KEY 'customerSegmentationId' AND campaignproperties['customerSegmentationId'] = :customerSegmentationTemplateId ALLOW FILTERING")
    Campaign getCampaignByCustomerSegmentationTemplate(String customerSegmentationTemplateId);

    @Query("SELECT * FROM EVT_CAMPAIGN_LIST WHERE campaignproperties CONTAINS KEY :campaignPropertyKey AND campaignproperties[:campaignPropertyKey] = :campaignPropertyValue AND campaignstatus = :campaignStatus ALLOW FILTERING")
    List<Campaign> getCampaignByPropertiesAndCampaignStatus(String campaignPropertyKey, String campaignPropertyValue, Status campaignStatus);

    @Query("SELECT * FROM EVT_CAMPAIGN_LIST ALLOW FILTERING")
    List<Campaign> getCampaignList();


};
