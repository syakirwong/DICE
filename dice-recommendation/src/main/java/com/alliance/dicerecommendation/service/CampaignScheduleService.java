package com.alliance.dicerecommendation.service;

import com.alliance.dicerecommendation.constant.ApiResponse;
import com.alliance.dicerecommendation.constant.SFTPProfile;
import com.alliance.dicerecommendation.exception.ServiceException;
import com.alliance.dicerecommendation.model.CampaignSchedule;
import com.alliance.dicerecommendation.model.CustomerProfile;
import com.alliance.dicerecommendation.model.DataField;
import com.alliance.dicerecommendation.model.DataField.ScheduleStatus;
import com.alliance.dicerecommendation.repository.CampaignScheduleRepository;
import com.alliance.dicerecommendation.request.CampaignScheduleRequest;
import com.alliance.dicerecommendation.request.TriggerEngagementPushNotiRequest;
import com.alliance.dicerecommendation.utility.AESEncyptionUtil;
import com.alliance.dicerecommendation.utility.SFTPUtil;
import com.alliance.dicerecommendation.utility.SystemParam;
import com.jcraft.jsch.SftpException;

import freemarker.template.TemplateException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class CampaignScheduleService {

    @Autowired
    private CampaignScheduleRepository campaignScheduleRepository;

    @Autowired
    private TriggerEngagementService triggerEngagementService;

    @Autowired
    private FileService fileService;

    @Autowired
    private MessagingService messagingService;

    @Value("${dice.encryption.secret.key}")
    private String SECRET_KEY;

    @Value("${dice.encryption.salt}")
    private String SALT;

    @Value("${freemarker.email.template.name.campaign.schedule.alert}")
    private String EMAIL_CAMPAIGN_SCHEDULE_ALERT_TEMPLATE_NAME;

    @Value("${download.mft.file.endpointURL}")
    private String DOWNLOAD_MFT_FILE_ENDPOINT_URL;

    @Autowired
    private MessageSource messageSource;

    @Scheduled(fixedRate = 10000) // Run every 30 seconds
    public void fileProcessingCronJob() throws TemplateException, IOException {
        log.debug("start - fileProcessingCronJob : Starting cron job... with debug mode");
        CampaignSchedule campaignSchedule = campaignScheduleRepository.findPendingFiles();

        if (campaignSchedule != null) {
            log.info("start - fileProcessingCronJob : Starting cron job... campaignSchedule : {}", campaignSchedule);
            campaignSchedule.setScheduleStatus(ScheduleStatus.PROCESSING);
            campaignScheduleRepository.save(campaignSchedule);

            try {
                // General rule validation
                DataField.CampaignStatus campaignStatus = triggerEngagementService
                        .checkCampaign(campaignSchedule.getCampaignId());

                switch (campaignStatus) {
                    case VALID:
                        // TODO continue with flow to get file and process it
                        CampaignSchedule tempCampaignSchedule = processCampaignScheduleFile(campaignSchedule);
                        if (!(tempCampaignSchedule.getScheduleStatus().equals(ScheduleStatus.FAILED)) &&
                        !(tempCampaignSchedule.getScheduleStatus().equals(ScheduleStatus.INVALID))) {
                            campaignSchedule.setScheduleStatus(DataField.ScheduleStatus.COMPLETED);
                            campaignSchedule.setRemark("File processing is completed");
                        }
                        else{
                            campaignSchedule.setScheduleStatus(tempCampaignSchedule.getScheduleStatus());
                            campaignSchedule.setRemark(tempCampaignSchedule.getRemark());
                        }
                        break;
                    case DISABLE:
                        campaignSchedule.setScheduleStatus(ScheduleStatus.DISABLE);
                        campaignSchedule.setRemark("Campaign has been disabled");
                        break;
                    case EXPIRED:
                        campaignSchedule.setScheduleStatus(ScheduleStatus.EXPIRED);
                        campaignSchedule.setRemark("Campaign is expired");
                        break;
                    case INVALID:
                        campaignSchedule.setScheduleStatus(ScheduleStatus.INVALID);
                        campaignSchedule.setRemark("Invalid campaign");
                        break;

                }

                if (campaignSchedule.getScheduleStatus() == ScheduleStatus.DISABLE
                        || campaignSchedule.getScheduleStatus() == ScheduleStatus.EXPIRED
                        || campaignSchedule.getScheduleStatus() == ScheduleStatus.INVALID) {
                }

            } catch (Exception ex) {
                campaignSchedule.setScheduleStatus(ScheduleStatus.FAILED);
                campaignSchedule.setRemark("Unexpected error while processing campaign schedule");
                log.error("fileProcessingCronJob - Exception for campaignSchedule : {} | ex : {}", campaignSchedule, ex);
            }

            campaignScheduleRepository.save(campaignSchedule);

            // send alert email if status not completed
            if(campaignSchedule.getScheduleStatus() != ScheduleStatus.COMPLETED) {
                log.info("fileProcessingCronJob - sending alert email for case campaignScheduleId : {} | scheduleStatus : {}", campaignSchedule.getCampaignScheduleId(), campaignSchedule.getScheduleStatus());
                
                ZoneId zoneId = ZoneId.of("Asia/Kuala_Lumpur");
                Date dateNow = Date.from(LocalDateTime.now().atZone(zoneId).toInstant());
                String formattedDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(dateNow);

                Map<String, Object> mailInfo = new HashMap<String, Object>();
                mailInfo.put("emailTemplate", EMAIL_CAMPAIGN_SCHEDULE_ALERT_TEMPLATE_NAME);
                mailInfo.put("title", messageSource.getMessage("spring.mail.subject.campaign.schedule.alert", new String[]{formattedDate}, Locale.ENGLISH));
                mailInfo.put("campaignScheduleId", campaignSchedule.getCampaignScheduleId());
                mailInfo.put("filePath", campaignSchedule.getFilePath());
                mailInfo.put("fileName", campaignSchedule.getFileName());
                mailInfo.put("campaignId", campaignSchedule.getCampaignId());
                mailInfo.put("scheduleStatus", campaignSchedule.getScheduleStatus());
                mailInfo.put("remark", campaignSchedule.getRemark());
                mailInfo.put("processedIndex", campaignSchedule.getProcessedIndex());

                messagingService.sendEmail(Arrays.asList(messagingService.createCampaignScheduleAlertEmailRequest(formattedDate, mailInfo)));
            }

            log.info("end - fileProcessingCronJob : End cron job... campaignSchedule : {}", campaignSchedule);
        } else {
            // No files to process
            log.debug("fileProcessingCronJob - No campaign schedule to process with debug mode");
        }

        log.debug("end - fileProcessingCronJob : End cron job... with debug mode");

    }

    public CampaignSchedule createCampaignSchedule(CampaignScheduleRequest campaignScheduleRequest)
            throws ServiceException {
        try {
            CampaignSchedule campaignSchedule = new CampaignSchedule();
            campaignSchedule.setCampaignId(campaignScheduleRequest.getCampaignId());
            campaignSchedule.setFileName(campaignScheduleRequest.getFileName());
            campaignSchedule.setFilePath(campaignScheduleRequest.getFilePath());
            campaignSchedule.setFilePassword(AESEncyptionUtil.encrypt(campaignScheduleRequest.getFilePassword(), SECRET_KEY, SALT));
            campaignSchedule.setRemark(campaignScheduleRequest.getRemark());
            campaignSchedule.setEngagementStartDateTime(campaignScheduleRequest.getEngagementStartDateTime());
            campaignSchedule.setProcessDateTime(campaignScheduleRequest.getProcessDateTime());
            campaignSchedule.setScheduleStatus(campaignScheduleRequest.getScheduleStatus());
            log.info("end - createCampaignSchedule service - campaignSchedule Creation: {}", campaignSchedule);
            return campaignScheduleRepository.save(campaignSchedule);
        } catch (Exception ex) {
            log.error("createCampaignSchedule - campaignScheduleRequest : {} - Exception: {}", campaignScheduleRequest, ex.getMessage());
            throw new ServiceException(ApiResponse.HTTP_RESPONSE_INTERNAL_SERVER_ERROR, ex.toString());
        }

    }

    public CampaignSchedule getCampaignScheduleByUUID(UUID campaignScheduleId) {
        return campaignScheduleRepository.getCampaignScheduleByUUID(campaignScheduleId);
    }

    public CampaignSchedule updateCampaignSchedule(CampaignSchedule existingSchedule,
            CampaignScheduleRequest campaignScheduleRequest) {
        existingSchedule.setFileName(campaignScheduleRequest.getFileName());
        existingSchedule.setFilePath(campaignScheduleRequest.getFilePath());
        existingSchedule
                .setFilePassword(AESEncyptionUtil.encrypt(campaignScheduleRequest.getFilePassword(), SECRET_KEY, SALT));
        existingSchedule.setRemark(campaignScheduleRequest.getRemark());
        existingSchedule.setProcessDateTime(campaignScheduleRequest.getProcessDateTime());
        existingSchedule.setEngagementStartDateTime(campaignScheduleRequest.getEngagementStartDateTime());
        existingSchedule.setScheduleStatus(campaignScheduleRequest.getScheduleStatus());

        return campaignScheduleRepository.save(existingSchedule);
    }

    public CampaignSchedule updateCampaignScheduleStatus(CampaignSchedule existingSchedule) {
        return campaignScheduleRepository.save(existingSchedule);
    }

    public CampaignSchedule processCampaignScheduleFile(CampaignSchedule campaignSchedule) throws SftpException {
        log.info("Start - processCampaignScheduleFile : {}", campaignSchedule);
        Boolean isDownloaded = downloadAndSaveMFTFile(campaignSchedule.getFileName());
        if(isDownloaded.equals(Boolean.TRUE)){
            log.info("processCampaignScheduleFile - File downloaded and saved to: tmp/" + campaignSchedule.getFileName());
        }
        else{
            log.info("processCampaignScheduleFile - unable to save file");
        }
        String diceDecryptedFilePassword = AESEncyptionUtil.decrypt(campaignSchedule.getFilePassword(), SECRET_KEY, SALT);
        if (campaignSchedule.getFileName().endsWith(".7z")) {
            try {
                log.info("processCampaignScheduleFile - start read file name : {}", campaignSchedule.getFileName());
                fileService.read7zFile(campaignSchedule, diceDecryptedFilePassword);

                return campaignSchedule;
            } catch (Exception ex) {
                log.error("processCampaignScheduleFile - Exception for {} : {}",campaignSchedule, ex);
                campaignSchedule.setScheduleStatus(ScheduleStatus.FAILED);
                campaignSchedule.setRemark("Unexpected Error");

                return campaignSchedule;
            }
        } else {
            campaignSchedule.setScheduleStatus(ScheduleStatus.FAILED);
            campaignSchedule.setRemark("File name does not have 7zip extension");
            log.warn("processCampaignScheduleFile - File name does not have 7zip extension : {}",campaignSchedule);

            return campaignSchedule;
        }
    }

    public Boolean downloadAndSaveMFTFile(String fileName) throws SftpException {
			SFTPProfile sftpProfile = new SFTPProfile();
			sftpProfile = SystemParam.getInstance().getMftPullSFTPProfile();
			SFTPUtil sftpUtil = new SFTPUtil(sftpProfile);
			Boolean isDownloaded = sftpUtil.downloadMFTFile(fileName);
            return isDownloaded;
	}
}
