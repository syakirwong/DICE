package com.alliance.dicecampaign.controller;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.alliance.dicecampaign.constant.ApiResponse;
import com.alliance.dicecampaign.constant.DataField.Status;
import com.alliance.dicecampaign.exception.ServiceException;
import com.alliance.dicecampaign.model.Campaign;
import com.alliance.dicecampaign.request.CreateCampaignRequest;
import com.alliance.dicecampaign.service.CampaignService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
public class CampaignController {

    @Autowired
    private CampaignService campaignService;

    @Autowired
    MessageSource messageSource;

    @PostMapping("/createCampaign")
    public ResponseEntity<ApiResponse> create(@Validated @RequestBody CreateCampaignRequest campaignRequest)
            throws ServiceException {
        log.info("start - createCampaign : {}", campaignRequest);
        ApiResponse apiResponse = new ApiResponse(ApiResponse.HTTP_RESPONSE_CREATED, true,
                messageSource.getMessage("create.campaign.success.add", null, Locale.getDefault()));
        campaignService.createCampaign(campaignRequest);

        return ResponseEntity.created(null).body(apiResponse);
    }

    @GetMapping("/campaigns/{id}")
    public ResponseEntity<Campaign> getCampaign(@PathVariable Integer id) {
        // log.info("start - getCampaign with id : {}", id);
        Optional<Campaign> optionalCampaign = campaignService.getCampaign(id);
        if (optionalCampaign.isPresent()) {
            return ResponseEntity.ok(optionalCampaign.get());
        } else {
            log.info("getCampaign - not found with id : {}", id);
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/getCampaignByName")
    public ResponseEntity<Campaign> getCampaignByName(
            @RequestParam(name = "campaignName", required = true) String campaignName) {
        // log.info("start - getCampaign with name : {}", campaignName);
        Optional<Campaign> optionalCampaign = campaignService.getCampaignByName(campaignName);
        if (optionalCampaign.isPresent()) {
            return ResponseEntity.ok(optionalCampaign.get());
        } else {
            log.info("getCampaign - not found with name : {}", campaignName);
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/getCampaignByPropertiesAndCampaignStatus")
    public ResponseEntity<List<Campaign>> getCampaignByPropertiesAndCampaignStatus(
            @RequestParam(name = "campaignPropertyKey", required = true) String campaignPropertyKey,
            @RequestParam(name = "campaignPropertyValue", required = true) String campaignPropertyValue,
            @RequestParam(name = "campaignStatus", required = true) Status campaignStatus) {
        try {
            List<Campaign> campaign = campaignService.getCampaignByPropertiesAndCampaignStatus(campaignPropertyKey,
                    campaignPropertyValue,
                    campaignStatus);

            return ResponseEntity.ok(campaign);
        } catch (Exception ex) {

            return ResponseEntity.notFound().build();
        }

    }

    @GetMapping("/campaigns")
    public List<Campaign> getAllCampaigns() {
        return campaignService.getAllCampaigns();
    }

}
