package com.alliance.dicecampaign.service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.alliance.dicecampaign.constant.ApiResponse;
import com.alliance.dicecampaign.constant.DataField.Status;
import com.alliance.dicecampaign.model.Campaign;
import com.alliance.dicecampaign.repository.CampaignRepository;
import com.alliance.dicecampaign.request.CreateCampaignRequest;
import com.alliance.dicecampaign.request.CreateRecommendationRequest;
import com.alliance.dicecampaign.utility.StringUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class CampaignService {

    @Autowired
    private CampaignRepository campaignRepository;

    @Autowired
    private StringUtil stringUtil;

    @Value("${create.campaign.recommendation.endpointUrl}")
    private String CREATE_CAMPAIGN_RECOMMENDATION_ENDPOINT_URL;

    public Campaign createCampaign(CreateCampaignRequest campaignRequest) {
        log.info("createCampaign - CampaignRequest: {}", campaignRequest);

        Integer campaignId = (campaignRepository.getMaxCampaignId() != null) ? campaignRepository.getMaxCampaignId() + 1
                : 1;

        Campaign campaign = new Campaign();
        campaign.setCampaignId(campaignId);
        campaign.setCampaignName(campaignRequest.getCampaignName());
        campaign.setCampaignPriority(campaignRequest.getCampaignPriority()); //
        campaign.setDiceActionTemplateId(campaignRequest.getDiceActionTemplateId());
        campaign.setStartDate(campaignRequest.getStartDate());
        campaign.setEndDate(campaignRequest.getEndDate());
        campaign.setDescription(campaignRequest.getDescription());
        campaign.setCampaignProperties(campaignRequest.getCampaignProperties());
        campaign.setCampaignStatus(Status.ACTIVE);
        campaign.setCreatedOn(new Date());

        return campaignRepository.save(campaign);

    }

    public Optional<Campaign> getCampaign(Integer campaignId) {
        return campaignRepository.findById(campaignId);
    }

    public Optional<Campaign> getCampaignByName(String campaignName) {
        return campaignRepository.findByName(campaignName);
    }

    public List<Campaign> getCampaignByPropertiesAndCampaignStatus(String campaignPropertyKey, String campaignPropertyValue,
            Status campaignStatus) {
        // log.info("getCampaignByPropertiesAndCampaignStatus - campaignPropertyKey : {} | campaignPropertyValue : {} | campaignStatus : {}", campaignPropertyKey, campaignPropertyValue, campaignStatus);
        log.info("getCampaignByPropertiesAndCampaignStatus - campaignPropertyKey : {} | campaignPropertyValue : {} | campaignStatus : {} | result : {}", 
        campaignPropertyKey, campaignPropertyValue, campaignStatus, campaignRepository.getCampaignByPropertiesAndCampaignStatus(campaignPropertyKey,
                campaignPropertyValue, campaignStatus));
        return campaignRepository.getCampaignByPropertiesAndCampaignStatus(campaignPropertyKey, campaignPropertyValue,
                campaignStatus);
    }

    public List<Campaign> getAllCampaigns() {
        return campaignRepository.findAll();
    }


}