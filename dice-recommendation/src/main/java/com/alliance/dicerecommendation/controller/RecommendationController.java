package com.alliance.dicerecommendation.controller;

import java.io.IOException;
import java.text.ParseException;
import java.util.Base64;
import java.util.Locale;
import java.util.Map;

import javax.validation.Valid;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.alliance.dicerecommendation.constant.ApiResponse;
import com.alliance.dicerecommendation.exception.ServiceException;
import com.alliance.dicerecommendation.request.CreateRecommendationRequest;
import com.alliance.dicerecommendation.request.DeleteRecommendationRequest;
import com.alliance.dicerecommendation.request.ProfileRequest;
import com.alliance.dicerecommendation.request.UpdateCampaignCustomerStatusRequest;
import com.alliance.dicerecommendation.response.CacheCustomerProfileSoleCCResponse;
import com.alliance.dicerecommendation.response.CampaignDetailResponse;
import com.alliance.dicerecommendation.response.CustomerProfileResponse;
import com.alliance.dicerecommendation.service.RecommendationService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
public class RecommendationController {
	@Autowired
	private RecommendationService recommendationService;

	@Autowired
	MessageSource messageSource;

	@GetMapping("/getCampaignRecommendation")
	public ResponseEntity<ApiResponse> getCampaignRecommendation(@RequestParam("uuidType") String uuidType,
			@RequestParam("uuid") String uuid, @RequestParam("engagementMode") String engagementMode,
			@RequestParam(name = "campaignId", required = false) String campaignId) {
		try {
			log.info(
					"start - getCampaignRecommendation, uuidType = {} | uuid = {} | engagementMode = {} | campaignId = {}",
					uuidType, uuid, engagementMode, campaignId);

			// Execute only when it is pre float, which is pre-login then getRecommendation
			if (engagementMode.equalsIgnoreCase("PRE_FLOAT")
					&& !(engagementMode.equalsIgnoreCase("EKYC_CC_P1") || engagementMode.equalsIgnoreCase("EKYC_CC_P2")
							|| engagementMode.equalsIgnoreCase("EKYC_PL_P1")
							|| engagementMode.equalsIgnoreCase("EKYC_PL_P2"))) {
				recommendationService.processRecommendation(uuidType, uuid);
			}

			ApiResponse apiResponse = new ApiResponse(ApiResponse.HTTP_RESPONSE_OK, true,
					messageSource.getMessage("common.operation.success", null, Locale.getDefault()),
					recommendationService.getCampaignRecommendation(uuidType, uuid, engagementMode, campaignId));
			return ResponseEntity.ok().body(apiResponse);

		} catch (

		ServiceException serviceException) {
			log.info("getCampaignRecommendation - uuidType: {} uuid: {}. serviceException: {}", uuidType, uuid,
					serviceException.toString());
			ApiResponse apiResponse = new ApiResponse(serviceException.getErrorCode(), true,
					serviceException.getMessage(), null);
			return ResponseEntity.status(apiResponse.getResponseCode()).body(apiResponse);
		} catch (Exception ex) {
			log.error("getCampaignRecommendation - uuidType: {} uuid: {}. Exception: {}", uuidType, uuid, ex);
			ApiResponse apiResponse = new ApiResponse(ApiResponse.HTTP_RESPONSE_INTERNAL_SERVER_ERROR, false,
					ex.getLocalizedMessage(), null);
			return ResponseEntity.internalServerError().body(apiResponse);

		}
	}

	@PostMapping("/createRecommendation")
	public ResponseEntity<ApiResponse> createRecommendation(
			@Validated @RequestBody CreateRecommendationRequest createRecommendationRequest)
			throws ServiceException {
		try {
			log.info("start - createRecommendation - fromController : {}", createRecommendationRequest);
			ApiResponse apiResponse = new ApiResponse(ApiResponse.HTTP_RESPONSE_CREATED, true,
					messageSource.getMessage("create.recommendation.success.add", null, Locale.getDefault()));
			recommendationService.createRecommendation(createRecommendationRequest);

			return ResponseEntity.created(null).body(apiResponse);
		} catch (Exception ex) {
			log.error("createRecommendation - Exception: {}", ex);
			ApiResponse apiResponse = new ApiResponse(ApiResponse.HTTP_RESPONSE_INTERNAL_SERVER_ERROR, false,
					ex.getLocalizedMessage(), null);
			return ResponseEntity.internalServerError().body(apiResponse);

		}
	}

	@DeleteMapping("/deleteRecommendation")
	public ResponseEntity<ApiResponse> deleteRecommendation(
			@Validated @RequestBody DeleteRecommendationRequest deleteRecommendationRequest)
			throws ServiceException {
		try {
			log.info("start - deleteRecommendation - fromController : {}", deleteRecommendationRequest);
			ApiResponse apiResponse = new ApiResponse(ApiResponse.HTTP_RESPONSE_ACCEPTED, true,
					messageSource.getMessage("delete.recommendation.success", null, Locale.getDefault()));
			recommendationService.deleteRecommendation(deleteRecommendationRequest);

			return ResponseEntity.created(null).body(apiResponse);
		} catch (Exception ex) {
			log.error("deleteRecommendation - Exception: {}", ex);
			ApiResponse apiResponse = new ApiResponse(ApiResponse.HTTP_RESPONSE_INTERNAL_SERVER_ERROR, false,
					ex.getLocalizedMessage(), null);
			return ResponseEntity.internalServerError().body(apiResponse);

		}
	}

	@PostMapping("/createTargetedCustomer")
	public ResponseEntity<ApiResponse> createTargetedCustomer(
			@Valid @RequestParam("targetedCampaign") String targetedCampaign, @RequestParam("fileName") String fileName,
			@RequestParam("createdBy") String createdBy, @RequestParam("isReadHeader") Boolean isReadHeader,
			@RequestBody String targetedCustomerFile)
			throws Exception, IOException, NumberFormatException, ParseException {
		log.info("start - createTargetedCustomer : {},{}", targetedCampaign, targetedCustomerFile);
		ObjectMapper objectMapper = new ObjectMapper();
		JsonNode jsonNode = objectMapper.readTree(targetedCustomerFile);
		String based64Byte = jsonNode.get("targetedCustomerFile").asText();
		byte[] decodedBytes = Base64.getDecoder().decode(based64Byte);
		try {
			ApiResponse apiResponse = new ApiResponse(ApiResponse.HTTP_RESPONSE_OK, true,
					messageSource.getMessage("create.target.customer.success.add", null, Locale.getDefault()));
			recommendationService.addTargetedCustomer(targetedCampaign, decodedBytes, fileName, createdBy,
					isReadHeader);

			return ResponseEntity.ok().body(apiResponse);
		} catch (ServiceException ex) {
			log.info("createTargetedCustomer - ServiceException: {}", ex.toString());
			ApiResponse apiResponse = new ApiResponse(ex.getErrorCode(), false,
					messageSource.getMessage(ex.getMessage(), ex.getMessageArgs(), Locale.getDefault()), null);
			return ResponseEntity.status(apiResponse.getResponseCode()).body(apiResponse);
		} catch (Exception ex) {
			log.error("createTargetedCustomer - Exception: {}", ex);
			ApiResponse apiResponse = new ApiResponse(ApiResponse.HTTP_RESPONSE_INTERNAL_SERVER_ERROR, false,
					ex.getLocalizedMessage(), null);
			return ResponseEntity.internalServerError().body(apiResponse);

		}
	}

	@PutMapping("/update-cust-campaign-status")
	public ResponseEntity<ApiResponse> updateCustCampaignStatus(
			@Valid @RequestBody UpdateCampaignCustomerStatusRequest data) {
		try {
			log.info("start - updateCustCampaignStatus : {}", data);
			recommendationService.updateCustCampaignStatus(data);
			ApiResponse apiResponse = new ApiResponse(ApiResponse.HTTP_RESPONSE_OK, true,
					messageSource.getMessage("campaign.customer.info.success.update", null, Locale.getDefault()), null);

			return ResponseEntity.ok().body(apiResponse);
		} catch (ServiceException ex) {
			log.info("updateCustCampaignStatus - cifNo: {}. ServiceException: {}", data.getCifNo(), ex.toString());
			ApiResponse apiResponse = new ApiResponse(ex.getErrorCode(), false,
					messageSource.getMessage(ex.getMessage(), ex.getMessageArgs(), Locale.getDefault()), null);
			return ResponseEntity.status(apiResponse.getResponseCode()).body(apiResponse);
		} catch (Exception ex) {
			log.error("updateCustCampaignStatus - cifNo: {}. Exception: {}", data.getCifNo(), ex);
			ApiResponse apiResponse = new ApiResponse(ApiResponse.HTTP_RESPONSE_INTERNAL_SERVER_ERROR, false,
					ex.getLocalizedMessage());
			return ResponseEntity.internalServerError().body(apiResponse);
		}
	}

	@PostMapping(value = "/getProfileById", produces = "application/json")
	public ResponseEntity<ApiResponse> getProfileById(@Valid @RequestBody ProfileRequest profileRequest) {
		log.info("getProfileById: {}", profileRequest);

		try {
			CacheCustomerProfileSoleCCResponse result = recommendationService.checkCustomerProfile(profileRequest);
			log.info("getProfileById - result : {}", result);

			Boolean isCustomer = recommendationService.isExistingVccCustomer(profileRequest);
			log.info("getProfileById - isCustomer : {}", isCustomer);

			// if (result.getBody() != null) {
			// log.info("show data : {}", result.getBody().get("data"));

			// Map<String, String> dataMap = (Map<String, String>)
			// result.getBody().get("data");

			// if (dataMap != null) {
			// String uuid = dataMap.get("uuid");
			// if (uuid != null) {
			// log.info("UUID: {}", uuid);
			// // Use uuid in your logic here
			// }
			// }

			ApiResponse apiResponse = new ApiResponse(ApiResponse.HTTP_RESPONSE_OK, true,
					messageSource.getMessage("common.operation.success", null, Locale.getDefault()), result);

			return ResponseEntity.ok().body(apiResponse);
			// } else {
			// log.error("No profile data found or error occurred while retrieving profile
			// data");
			// ApiResponse apiResponse = new
			// ApiResponse(ApiResponse.HTTP_RESPONSE_NOT_FOUND, false,
			// "No profile data found or error occurred while retrieving profile data");
			// return ResponseEntity.status(HttpStatus.NOT_FOUND).body(apiResponse);
			// }
		} catch (Exception ex) {
			log.error("getProfileById - ProfileRequest: {}. Exception: {}", profileRequest, ex);
			ApiResponse apiResponse = new ApiResponse(ApiResponse.HTTP_RESPONSE_INTERNAL_SERVER_ERROR, false,
					ex.getLocalizedMessage());
			return ResponseEntity.internalServerError().body(apiResponse);
		}
	}

}
