package com.alliance.diceanalytics.controller;

import com.alliance.diceanalytics.constant.ApiResponse;
import com.alliance.diceanalytics.constant.SFTPProfile;
import com.alliance.diceanalytics.exception.ServiceException;
import com.alliance.diceanalytics.model.AuditTrail;
import com.alliance.diceanalytics.model.FileForUpload;
import com.alliance.diceanalytics.request.CustomerActionTrailRequest;
import com.alliance.diceanalytics.request.SendCommonEmailRequest;
import com.alliance.diceanalytics.request.UploadedFileHistoryRequest;
import com.alliance.diceanalytics.service.*;
import com.alliance.diceanalytics.utility.MailUtil;
import com.alliance.diceanalytics.utility.QueryUtil;
import com.alliance.diceanalytics.utility.SFTPUtil;
import com.alliance.diceanalytics.utility.SystemParam;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Slf4j
@RestController
public class AnalyticsController {
    @Autowired
    private AnalyticsService analyticsService;

	@Autowired
	private AuditTrailService auditTrailService;

	@Autowired
	private QueryUtil queryUtil;

	@Autowired
	private MessagingService messagingService;

    @Autowired
	private MessageSource messageSource;

	@Autowired
	private MailUtil mailUtil;

	@PostMapping("/cust-action-trail")
	public ResponseEntity<ApiResponse> createCustomerActionTrail(@RequestHeader HttpHeaders requestHeader, @RequestBody CustomerActionTrailRequest customerActionTrailRequest) throws ServiceException {
		try {
			analyticsService.createCustomerActionTrail(customerActionTrailRequest);
			ApiResponse apiResponse = new ApiResponse(ApiResponse.HTTP_RESPONSE_OK, true,
					messageSource.getMessage("common.operation.success", null, Locale.getDefault()), null);
			return ResponseEntity.ok().body(apiResponse);
		} catch (ServiceException serviceException) {
			log.error("createCustomerActionTrail - ServiceException: {}", serviceException);
			throw serviceException;
		} catch (Exception ex) {
			log.error("createCustomerActionTrail - Exception: {}", ex);
			ApiResponse apiResponse = new ApiResponse(ApiResponse.HTTP_RESPONSE_INTERNAL_SERVER_ERROR, false,
					ex.getLocalizedMessage());
			throw ex;
		}
	}

	@PostMapping("/addUploadedFileHistoryLog")
	public ResponseEntity<ApiResponse> addUploadedFileHistoryLog(@RequestHeader HttpHeaders requestHeader, @RequestBody UploadedFileHistoryRequest uploadedFileHistoryRequest) {
        try {
			log.info("start - addUploadedFileHistoryLog : {}",uploadedFileHistoryRequest);
			analyticsService.addUploadedFileHistoryLog(uploadedFileHistoryRequest);
			ApiResponse apiResponse = new ApiResponse(ApiResponse.HTTP_RESPONSE_OK, true,
					messageSource.getMessage("common.operation.success", null, Locale.getDefault()), null);
			return ResponseEntity.ok().body(apiResponse);
		} catch (ServiceException serviceException) {
			log.error("addUploadedFileHistoryLog - ServiceException: {}", serviceException);
			ApiResponse apiResponse = new ApiResponse(serviceException.getErrorCode(),
					false, serviceException.getMessage(), null);
			return ResponseEntity.status(apiResponse.getResponseCode()).body(apiResponse);
		} catch (Exception ex) {
			log.error("addUploadedFileHistoryLog - Exception: {}", ex);
			ApiResponse apiResponse = new ApiResponse(ApiResponse.HTTP_RESPONSE_INTERNAL_SERVER_ERROR, false,
					ex.getLocalizedMessage());
			return ResponseEntity.internalServerError().body(apiResponse);
		}
    }


	@PostMapping("/create-audit-trail")
	public ResponseEntity<ApiResponse> createAuditTrail(@RequestBody AuditTrail auditTrail) {
		try {
			auditTrailService.saveAuditTrail(auditTrail);
			ApiResponse apiResponse = new ApiResponse(ApiResponse.HTTP_RESPONSE_OK, true,
					messageSource.getMessage("common.operation.success", null, Locale.getDefault()), null);

			return ResponseEntity.ok().body(apiResponse);
		} catch (Exception ex) {
			log.error("createAuditTrail - Exception: {}", ex);
			ApiResponse apiResponse = new ApiResponse(ApiResponse.HTTP_RESPONSE_INTERNAL_SERVER_ERROR, false,
					ex.getLocalizedMessage());
			return ResponseEntity.internalServerError().body(apiResponse);
		}

	}


	@PostMapping( value="/testDownloadReportWithDate")
	public ResponseEntity<byte[]> downloadReport(@RequestParam String duration,
												 @RequestParam Integer report,
												 @RequestParam (required = false) String startDate,
												 @RequestParam (required = false) String endDate,
												 @RequestParam Boolean sendOut) throws Exception {

			long startTime = System.currentTimeMillis();

			duration = duration.toLowerCase();



			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
			Date inputStartDate = null;
			Date inputEndDate = null;

			if (startDate!=null && endDate!=null){
				try {
					inputStartDate= format.parse(startDate);
					inputEndDate = format.parse(endDate);
				} catch (ParseException e) {
					log.info("Invalid Date found, using default Date");
				}
			}


		FileForUpload file = analyticsService.generateReport(0, duration,report,inputStartDate,inputEndDate);
			if (file.getFileName().isEmpty())
				return ResponseEntity.noContent().build();


		if (sendOut){
					List<SendCommonEmailRequest> emailRequestList = new ArrayList<>();
					emailRequestList.add(mailUtil.createEmailRequest(file.getFileData(), file.getFileName(), duration));
					messagingService.sendEmail(emailRequestList);
					SFTPUtil util  = new SFTPUtil();
					util.transferFile(new ByteArrayInputStream(file.getFileData()), file.getFileName());
		}

		HttpHeaders responseHeaders = new HttpHeaders();
		responseHeaders.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getFileName() + "\"");

		log.info("Time to Generate (s): {} ",  (System.currentTimeMillis()- startTime)/1000.00);

		return  ResponseEntity.ok()
					.headers(responseHeaders)
					.body(file.getFileData());
	}

	// @GetMapping("/MFTDownloadAndSaveFile")
	// public ResponseEntity<ApiResponse> MFTDownloadAndSaveFile(@RequestParam String fileName, @RequestParam String localFilePath) throws Exception{
	// 	try {
	// 		log.info("start - MFTDownloadAndSaveFile - fileName : {} | localFilePath = {}", fileName, localFilePath);
	// 		SFTPProfile sftpProfile = new SFTPProfile();
	// 		sftpProfile = SystemParam.getInstance().getMftPullSFTPProfile();
	// 		SFTPUtil sftpUtil = new SFTPUtil(sftpProfile);
			
	// 		// String localTmpDirectory = "tmp"; // Path to your local tmp directory

	// 		// File downloadedFile = sftpUtil.downloadAndSaveFile(fileName, localFilePath);
	// 		// if(downloadedFile!=null){
	// 		// 	log.info("MFTDownloadAndSaveFile - File downloaded and saved to: " + downloadedFile.getAbsolutePath());
	// 		// }
	// 		// else{
	// 		// 	log.info("MFTDownloadAndSaveFile - unable to save file");
	// 		// }

			
	// 		ApiResponse apiResponse = new ApiResponse(ApiResponse.HTTP_RESPONSE_OK, true,
	// 				messageSource.getMessage("common.operation.success", null, Locale.getDefault()), sftpUtil.getMFTFile(fileName));
	// 		return ResponseEntity.ok().body(apiResponse);
	// 	} catch (Exception ex) {
	// 		log.error("testMFTReadFile - Exception: {}", ex);
	// 		ApiResponse apiResponse = new ApiResponse(ApiResponse.HTTP_RESPONSE_INTERNAL_SERVER_ERROR, false,
	// 				ex.getLocalizedMessage());
	// 		throw ex;
	// 	}
	// }

	// @PostMapping("/test-mft-list-file")
	// public ResponseEntity<ApiResponse> testMFTListFile(@RequestParam String filePath, @RequestParam String user){
	// 	try {
	// 		log.info("start - testMFTListFile : {} , {}", filePath, user);
	// 		SFTPProfile sftpProfile = new SFTPProfile();
	// 		sftpProfile = SystemParam.getInstance().getMftSFTPProfile();
	// 		sftpProfile.setUser(user);
	// 		SFTPUtil sftpUtil = new SFTPUtil(sftpProfile);
	// 		List<String> filesInDirectory = sftpUtil.listFiles(filePath);
	// 		for (String fileName : filesInDirectory) {
	// 			log.info("File: {}", fileName);
	// 		}
			
	// 		ApiResponse apiResponse = new ApiResponse(ApiResponse.HTTP_RESPONSE_OK, true,
	// 				messageSource.getMessage("common.operation.success", null, Locale.getDefault()), null);
	// 		return ResponseEntity.ok().body(apiResponse);
	// 	} catch (Exception ex) {
	// 		log.error("testMFTReadFile - Exception: {}", ex);
	// 		ApiResponse apiResponse = new ApiResponse(ApiResponse.HTTP_RESPONSE_INTERNAL_SERVER_ERROR, false,
	// 				ex.getLocalizedMessage());
	// 		throw ex;
	// 	}
	// }

	// @PostMapping("/test-download-mft-file")
	// public ResponseEntity<ApiResponse> testDownloadMftFile(@RequestParam String fileName) throws Exception{
	// 	try {
	// 		log.info("start - testDownloadMftFile - fileName : {} , {}", fileName);
	// 		SFTPProfile sftpProfile = new SFTPProfile();
	// 		sftpProfile = SystemParam.getInstance().getMftPullSFTPProfile();
	// 		SFTPUtil sftpUtil = new SFTPUtil(sftpProfile);
	// 		sftpUtil.downloadMFTFile(fileName);

	// 		ApiResponse apiResponse = new ApiResponse(ApiResponse.HTTP_RESPONSE_OK, true,
	// 				messageSource.getMessage("common.operation.success", null, Locale.getDefault()), null);
	// 		return ResponseEntity.ok().body(apiResponse);
	// 	} catch (Exception ex) {
	// 		log.error("testMFTReadFile - Exception: {}", ex);
	// 		ApiResponse apiResponse = new ApiResponse(ApiResponse.HTTP_RESPONSE_INTERNAL_SERVER_ERROR, false,
	// 				ex.getLocalizedMessage());
	// 		throw ex;
	// 	}
	// }



}


