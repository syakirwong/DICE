package com.alliance.dicerecommendation.controller;

import com.alliance.dicerecommendation.constant.ApiResponse;
import com.alliance.dicerecommendation.model.CampaignSchedule;
import com.alliance.dicerecommendation.model.DataField.ScheduleStatus;
import com.alliance.dicerecommendation.repository.CampaignScheduleRepository;
import com.alliance.dicerecommendation.request.CampaignScheduleRequest;
import com.alliance.dicerecommendation.response.CampaignScheduleResponse;
import com.alliance.dicerecommendation.service.CampaignScheduleService;
import com.alliance.dicerecommendation.service.FileService;
import com.alliance.dicerecommendation.utility.AESEncyptionUtil;
import com.alliance.dicerecommendation.utility.SystemParam;

import java.io.File;
import java.util.Locale;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
public class CampaignScheduleController {

    @Autowired
    private CampaignScheduleService campaignScheduleService;

    @Autowired
    private FileService fileService;

    @Autowired
    MessageSource messageSource;

    @PostMapping("/createCampaignSchedule")
    public ResponseEntity<ApiResponse> createCampaignSchedule(
            @RequestBody CampaignScheduleRequest campaignScheduleRequest) {
            try {
                CampaignScheduleRequest logCampaignScheduleRequest = campaignScheduleRequest.copy();
            
            if (logCampaignScheduleRequest.getFilePassword() != null) {
                logCampaignScheduleRequest.setFilePassword("hidden");
            } else {
                log.warn("createCampaignSchedule - file password is null");
            }
            
            log.info("start - createCampaignSchedule controler - campaignScheduleRequest : {}", logCampaignScheduleRequest);
            CampaignSchedule campaignSchedule = campaignScheduleService.createCampaignSchedule(campaignScheduleRequest);
            CampaignScheduleResponse response = new CampaignScheduleResponse();
            response.setCampaignId(campaignSchedule.getCampaignId());
            response.setFileName(campaignSchedule.getFileName());
            response.setFilePath(campaignSchedule.getFilePath());
            response.setRemark(campaignSchedule.getRemark());
            response.setScheduleStatus(campaignSchedule.getScheduleStatus());
            response.setEngagementStartDateTime(campaignSchedule.getEngagementStartDateTime());
            response.setProcessDateTime(campaignSchedule.getProcessDateTime());
            response.setDiceEncryptedFilePassword(campaignSchedule.getFilePassword());
            ApiResponse apiResponse = new ApiResponse(ApiResponse.HTTP_RESPONSE_OK, true,
                    messageSource.getMessage("common.operation.success", null, Locale.getDefault()),
                    response);

            return ResponseEntity.ok().body(apiResponse);

        } catch (Exception ex) {
            log.error("createCampaignSchedule controler - Exception : {}", ex);
            ApiResponse apiResponse = new ApiResponse(ApiResponse.HTTP_RESPONSE_INTERNAL_SERVER_ERROR, false,
                    ex.getLocalizedMessage(), null);
            return ResponseEntity.internalServerError().body(apiResponse);

        }
    }

    @PutMapping("/updateCampaignSchedule/{campaignScheduleUUID}")
    public ResponseEntity<ApiResponse> updateCampaignSchedule(
            @PathVariable UUID campaignScheduleUUID,
            @RequestBody CampaignScheduleRequest campaignScheduleRequest) {
        try {
            CampaignSchedule existingSchedule = campaignScheduleService.getCampaignScheduleByUUID(campaignScheduleUUID);

            if (existingSchedule == null) {
                ApiResponse notFoundResponse = new ApiResponse(ApiResponse.HTTP_RESPONSE_NOT_FOUND, false,
                        "Campaign schedule not found", null);
                return ResponseEntity.status(ApiResponse.HTTP_RESPONSE_NOT_FOUND).body(notFoundResponse);
            }

            CampaignSchedule updatedSchedule = campaignScheduleService.updateCampaignSchedule(existingSchedule,
                    campaignScheduleRequest);

            CampaignScheduleResponse response = new CampaignScheduleResponse();

            response.setCampaignId(updatedSchedule.getCampaignId());
            response.setFileName(updatedSchedule.getFileName());
            response.setFilePath(updatedSchedule.getFilePath());
            response.setRemark(updatedSchedule.getRemark());
            response.setScheduleStatus(updatedSchedule.getScheduleStatus());
            response.setEngagementStartDateTime(updatedSchedule.getEngagementStartDateTime());
            response.setProcessDateTime(updatedSchedule.getProcessDateTime());
            response.setDiceEncryptedFilePassword(updatedSchedule.getFilePassword());

            ApiResponse apiResponse = new ApiResponse(ApiResponse.HTTP_RESPONSE_OK, true,
                    messageSource.getMessage("common.operation.success", null, Locale.getDefault()),
                    response);

            return ResponseEntity.ok().body(apiResponse);

        } catch (Exception ex) {
            ApiResponse apiResponse = new ApiResponse(ApiResponse.HTTP_RESPONSE_INTERNAL_SERVER_ERROR, false,
                    ex.getLocalizedMessage(), null);
            return ResponseEntity.internalServerError().body(apiResponse);
        }
    }

    @PutMapping("/updateScheduleStatus/{campaignScheduleUUID}")
    public ResponseEntity<ApiResponse> updateScheduleStatus(
            @PathVariable UUID campaignScheduleUUID,
            @RequestParam ScheduleStatus newScheduleStatus) {
        try {
            CampaignSchedule existingSchedule = campaignScheduleService.getCampaignScheduleByUUID(campaignScheduleUUID);

            if (existingSchedule == null) {
                ApiResponse notFoundResponse = new ApiResponse(ApiResponse.HTTP_RESPONSE_NOT_FOUND, false,
                        "Campaign schedule not found", null);
                return ResponseEntity.status(ApiResponse.HTTP_RESPONSE_NOT_FOUND).body(notFoundResponse);
            }

            existingSchedule.setScheduleStatus(newScheduleStatus);

            CampaignSchedule updatedSchedule = campaignScheduleService.updateCampaignScheduleStatus(existingSchedule);

            CampaignScheduleResponse response = new CampaignScheduleResponse();
            response.setCampaignId(updatedSchedule.getCampaignId());
            response.setFileName(updatedSchedule.getFileName());
            response.setFilePath(updatedSchedule.getFilePath());
            response.setRemark(updatedSchedule.getRemark());
            response.setScheduleStatus(updatedSchedule.getScheduleStatus());
            response.setEngagementStartDateTime(updatedSchedule.getEngagementStartDateTime());
            response.setProcessDateTime(updatedSchedule.getProcessDateTime());
            response.setDiceEncryptedFilePassword(updatedSchedule.getFilePassword());

            ApiResponse apiResponse = new ApiResponse(ApiResponse.HTTP_RESPONSE_OK, true,
                    messageSource.getMessage("common.operation.success", null, Locale.getDefault()),
                    response);

            return ResponseEntity.ok().body(apiResponse);

        } catch (Exception ex) {
            ApiResponse apiResponse = new ApiResponse(ApiResponse.HTTP_RESPONSE_INTERNAL_SERVER_ERROR, false,
                    ex.getLocalizedMessage(), null);
            return ResponseEntity.internalServerError().body(apiResponse);
        }
    }

    @PostMapping("/testDownloadMFTFile")
	public ResponseEntity<ApiResponse> testDownloadMFTFile(@RequestParam String fileName) throws Exception{
		try {
			log.info("start - testDownloadMFTFile - fileName : {}", fileName);
			
			ApiResponse apiResponse = new ApiResponse(ApiResponse.HTTP_RESPONSE_OK, true,
					messageSource.getMessage("common.operation.success", null, Locale.getDefault()), campaignScheduleService.downloadAndSaveMFTFile(fileName));
			return ResponseEntity.ok().body(apiResponse);
		} catch (Exception ex) {
			log.error("testDownloadMFTFile - Exception: {}", ex);
			ApiResponse apiResponse = new ApiResponse(ApiResponse.HTTP_RESPONSE_INTERNAL_SERVER_ERROR, false,
					ex.getLocalizedMessage());
			throw ex;
		}
	}

}
