package com.alliance.dicenotification.controller;

import com.alliance.dicenotification.constant.ApiResponse;
import com.alliance.dicenotification.exception.ServiceException;
import com.alliance.dicenotification.request.EngagementTriggerPushNotiRequest;
import com.alliance.dicenotification.request.PandaiBroadcastRequest;
import com.alliance.dicenotification.request.ReferralPushNotisRequest;
import com.alliance.dicenotification.request.SendCommonEmailRequest;
import com.alliance.dicenotification.service.MessagingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.mail.MessagingException;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Locale;

@RestController
@Slf4j
public class NotificationController {

	@Autowired
	private MessagingService messagingService;

	@Autowired
	private JavaMailSender javaMailSender;

	@Value("${spring.mail.from}")
	private String EMAIL_FROM;

	@Autowired
	MessageSource messageSource;

	@PostMapping("/sendCommonEmail")
	public ResponseEntity<ApiResponse> sendCommonEmail(
			@RequestBody List<SendCommonEmailRequest> sendCommonEmailRequests)
			throws MessagingException, IOException {
		log.info("sendEmail - sendCommonEmailRequests: {}", sendCommonEmailRequests);
		try {
			Date emailStartDate = new Date();
			messagingService.sendCommonEmail(sendCommonEmailRequests);
			Date emailEndDate = new Date();
			log.info("Email Overall Progress Time - {}", emailEndDate.getTime() - emailStartDate.getTime());
			ApiResponse apiResponse = new ApiResponse(ApiResponse.HTTP_RESPONSE_OK, true,
					messageSource.getMessage("common.operation.success", null, Locale.getDefault()), null);

			return ResponseEntity.ok().body(apiResponse);
		} catch (Exception ex) {
			log.error("sendEmail - sendCommonEmailRequests: {} Exception: {}", sendCommonEmailRequests, ex);

			ApiResponse apiResponse = new ApiResponse(ApiResponse.HTTP_RESPONSE_INTERNAL_SERVER_ERROR, false,
					ex.getLocalizedMessage());
			return ResponseEntity.internalServerError().body(apiResponse);
		}
	}

	// Personal Info Update
	@PostMapping("/engagementTriggerPushNotis")
	public ResponseEntity<ApiResponse> engagementTriggerPushNotis(
			@RequestBody EngagementTriggerPushNotiRequest engagementTriggerPushNotiRequest) {
		log.info("engagementTriggerPushNotis - pushNotificationRequest: {}", engagementTriggerPushNotiRequest);
		try {
			Date pushStartDaate = new Date();
			messagingService.pushNotification(engagementTriggerPushNotiRequest);
			Date pushEndDate = new Date();
			log.info("engagementTriggerPushNotis - Overall Progress Time - {}",
					pushEndDate.getTime() - pushStartDaate.getTime());
			ApiResponse apiResponse = new ApiResponse(ApiResponse.HTTP_RESPONSE_OK, true,
					messageSource.getMessage("common.operation.success", null, Locale.getDefault()),
					null);

			return ResponseEntity.ok().body(apiResponse);
		} catch (Exception ex) {
			log.error("engagementTriggerPushNotis - pushNotificationRequest: {} Exception: {}",
					engagementTriggerPushNotiRequest, ex);

			ApiResponse apiResponse = new ApiResponse(ApiResponse.HTTP_RESPONSE_INTERNAL_SERVER_ERROR, false,
					ex.getLocalizedMessage());
			return ResponseEntity.internalServerError().body(apiResponse);
		}
	}

	@PutMapping("/engagementTriggerPushBellBox")
	public ResponseEntity<ApiResponse> engagementTriggerPushBellBox(
			@RequestBody EngagementTriggerPushNotiRequest engagementTriggerPushNotiRequest,
			@RequestParam("cifNo") String cifNo) {
		log.info("engagementTriggerPushBellBox - pushNotificationRequest: {}", engagementTriggerPushNotiRequest);
		try {
			Date pushStartDaate = new Date();
			messagingService.pushBellBox(engagementTriggerPushNotiRequest, cifNo);
			Date pushEndDate = new Date();
			log.info("engagementTriggerPushBellBox - Push Overall Progress Time : {}",
					pushEndDate.getTime() - pushStartDaate.getTime());
			ApiResponse apiResponse = new ApiResponse(ApiResponse.HTTP_RESPONSE_OK, true,
					messageSource.getMessage("common.operation.success", null, Locale.getDefault()),
					null);

			return ResponseEntity.ok().body(apiResponse);
		} catch (Exception ex) {
			log.error("engagementTriggerPushBellBox - pushNotificationRequest: {} Exception: {}",
					engagementTriggerPushNotiRequest, ex);

			ApiResponse apiResponse = new ApiResponse(ApiResponse.HTTP_RESPONSE_INTERNAL_SERVER_ERROR, false,
					ex.getLocalizedMessage());
			return ResponseEntity.internalServerError().body(apiResponse);
		}
	}

	@PostMapping("/pandaiBroadcast")
	public ResponseEntity<ApiResponse> pandaiBroadcast(@RequestBody PandaiBroadcastRequest request) {
		try {

			ApiResponse apiResponse = new ApiResponse(ApiResponse.HTTP_RESPONSE_OK, true,
					messageSource.getMessage("common.operation.success", null, Locale.getDefault()),
					messagingService.pandaiBroadcast(request));

			return ResponseEntity.ok().body(apiResponse);
		} catch (ServiceException ex) {
			// log.error("sendEmail - sendEmailRequest: {} ServiceException: {}",
			// sendEmailRequest, ex);

			ApiResponse apiResponse = new ApiResponse(ex.getErrorCode(), false,
					messageSource.getMessage(ex.getMessage(), ex.getMessageArgs(), Locale.getDefault()), null);
			return ResponseEntity.status(apiResponse.getResponseCode()).body(apiResponse);
		}
	}

	// chatbot referral
	@PostMapping("/referralPushNotis")
	public ResponseEntity<ApiResponse> referralPushNotis(
			@RequestBody ReferralPushNotisRequest pushNotificationRequest) {
		log.info("referralPushNotis - pushNotificationRequest: {}", pushNotificationRequest);
		try {
			Date pushStartDaate = new Date();
			messagingService.referralPushNotification(pushNotificationRequest);
			Date pushEndDate = new Date();
			log.info("Push Overall Progress Time - {}", pushEndDate.getTime() - pushStartDaate.getTime());
			ApiResponse apiResponse = new ApiResponse(ApiResponse.HTTP_RESPONSE_OK, true,
					messageSource.getMessage("common.operation.success", null, Locale.getDefault()),
					null);

			return ResponseEntity.ok().body(apiResponse);
		} catch (ServiceException ex) {
			log.error("referralPushNotis - pushNotificationRequest: {} ServiceException: {}", pushNotificationRequest,
					ex);

			ApiResponse apiResponse = new ApiResponse(ex.getErrorCode(), false,
					messageSource.getMessage(ex.getMessage(), ex.getMessageArgs(), Locale.getDefault()), null);
			return ResponseEntity.status(apiResponse.getResponseCode()).body(apiResponse);
		} catch (Exception ex) {
			log.error("referralPushNotis - pushNotificationRequest: {} Exception: {}", pushNotificationRequest, ex);

			ApiResponse apiResponse = new ApiResponse(ApiResponse.HTTP_RESPONSE_INTERNAL_SERVER_ERROR, false,
					ex.getLocalizedMessage());
			return ResponseEntity.internalServerError().body(apiResponse);
		}
	}
}