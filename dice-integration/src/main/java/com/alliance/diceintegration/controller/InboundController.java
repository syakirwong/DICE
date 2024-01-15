package com.alliance.diceintegration.controller;

import com.alliance.diceintegration.constant.ApiResponse;
import com.alliance.diceintegration.constant.ErrorDetails;
import com.alliance.diceintegration.exception.ServiceException;
import com.alliance.diceintegration.model.CustomerProfile;
import com.alliance.diceintegration.request.CampaignJourneyRequest;
import com.alliance.diceintegration.request.CustomerActionTrailRequest;
import com.alliance.diceintegration.request.ProfileInfoRequest;
import com.alliance.diceintegration.request.ProfileRequest;
import com.alliance.diceintegration.request.UpdateCampaignCustomerStatusRequest;
import com.alliance.diceintegration.response.CacheCustomerProfileSoleCCResponse;
import com.alliance.diceintegration.service.CacheService;
import com.alliance.diceintegration.service.CallbackService;
import com.alliance.diceintegration.service.InboundService;
import com.alliance.diceintegration.service.ProfileInfoService;
import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.ColumnDefinition;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.core.cql.Row;
import com.datastax.oss.driver.api.core.type.DataType;
import com.datastax.oss.driver.api.core.type.DataTypes;
import com.datastax.oss.driver.api.core.type.ListType;
import com.datastax.oss.driver.api.core.type.MapType;
import com.datastax.oss.driver.api.core.type.SetType;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@RestController
@Validated
public class InboundController {

	@Autowired
	private CallbackService callbackService;
	@Autowired
	private MessageSource messageSource;
	@Autowired
	private InboundService inboundService;

	@Autowired
	private ProfileInfoService profileInfoService;
	@Autowired
	private CacheService cacheService;


	@Value("${failure.handle.max.retries}")
	private Integer FAILURE_HANDLE_MAX_RETRIES;

	@Value("${failure.handle.retry.delay}")
	private Integer FAILURE_HANDLE_RETRY_DELAY;

	@Value("${validate.code.pattern.expression}")
	private String VALIDATE_CODE_PATTERN_EXPRESSION;

	@Value("${validate.code.is.on}")
	private Integer VALIDATE_CODE_IS_ON;

	private final CqlSession cqlSession;

	@Autowired
	public InboundController(CqlSession cqlSession) {
		this.cqlSession = cqlSession;
	}

	@PostMapping("/validateCode")
	public ResponseEntity<ApiResponse> validateCode(@RequestHeader HttpHeaders requestHeader,
			@RequestParam("code") String code) throws ServiceException {
		log.info("start - validateCode : {}", code);
		try {
			boolean matches;
			Pattern pattern = Pattern.compile(VALIDATE_CODE_PATTERN_EXPRESSION);
			if (VALIDATE_CODE_IS_ON == 1) {
				matches = pattern.matcher(code).matches();
			} else {
				log.info(
						"validateCode - validation for code is off : {} , change value of validate.code.is.on to 1 to turn on",
						VALIDATE_CODE_IS_ON);
				matches = true;
			}
			if (matches) {
				ApiResponse apiResponse = new ApiResponse(ApiResponse.HTTP_RESPONSE_OK, true,
						messageSource.getMessage("common.operation.success", null, Locale.getDefault()),
						inboundService.validateCode(code));
				log.info("end - validateCode");
				return ResponseEntity.ok().body(apiResponse);
			} else {
				List<ErrorDetails> errors = new ArrayList<>();
				ErrorDetails errorDetails = new ErrorDetails();
				errorDetails.setFieldName("code");
				errorDetails.setMessage(messageSource.getMessage("validate.code.pattern.invalid.message", null, null));
				errors.add(errorDetails);
				ApiResponse apiResponse = new ApiResponse(ApiResponse.HTTP_RESPONSE_NOT_ACCEPTABLE, false,
						"Field validation error.");
				apiResponse.setErrors(errors);
				log.info("validateCode - format invalid");
				log.info("end - validateCode");
				return ResponseEntity.badRequest().body(apiResponse);
			}
		} catch (ServiceException serviceException) {
			throw serviceException;
		}
	}

	@GetMapping("/getCampaignRecommendation")
	public ResponseEntity<Object> getCampaignRecommendation(HttpServletRequest request,
			@RequestParam("uuidType") String uuidType, @RequestParam("uuid") String uuid,
			@RequestParam("engagementMode") String engagementMode,
			@RequestParam(name = "campaignId", required = false) String campaignId) {
		try {
			log.info("start - getCampaignRecommendation, uuidType = {} | uuid = {} | engagementMode = {} | campaignId = {}",uuidType,uuid,engagementMode,campaignId);
			Object apiResponse = callbackService.getCampaignRecommendation(uuidType, uuid, engagementMode, campaignId);
			return ResponseEntity.ok().body(apiResponse);
		} catch (Exception ex) {
			log.error("getCampaignRecommendation controller - exception : {}", ex);
			throw ex;
		}
	}

	@GetMapping("/getMessageContent")
	public ResponseEntity<ApiResponse> getMessageContent(@RequestHeader HttpHeaders requestHeader,
			@RequestParam(name = "uuidType", required = false) String uuidType,
			@RequestParam(name = "uuid", required = false) String uuid,
			@RequestParam(name = "messageTemplateId", required = false) Integer messageTemplateId,
			@RequestParam(name = "engagementSentId", required = false) UUID engagementSentId) throws ServiceException {
		try {
			log.info(
					"start - getMessageContent - messageTemplateId = {}, engagementSentId = {}, uuidType = {}, uuid = {}",
					messageTemplateId, engagementSentId, uuidType, uuid);
			ApiResponse apiResponse = new ApiResponse(ApiResponse.HTTP_RESPONSE_OK, true,
					messageSource.getMessage("common.operation.success", null, Locale.getDefault()),
					inboundService.getMessageContent(messageTemplateId, engagementSentId, uuidType, uuid));
			return ResponseEntity.ok().body(apiResponse);
		} catch (ServiceException serviceException) {
			log.info("getMessageContent - serviceException : {}", serviceException.toString());
			throw  serviceException;
		} catch (Exception ex) {
			log.error("getMessageContent - Exception: {}", ex);
			throw ex;

		}
	}

	@PostMapping("/cust-journey")
	public ResponseEntity<ApiResponse> createCampaignJourney(HttpServletRequest request,
			@RequestHeader HttpHeaders requestHeader, @RequestBody CampaignJourneyRequest campaignJourneyRequest)
			throws JsonMappingException, JsonProcessingException, ServiceException, Exception {
		log.info("start - createCampaignJourney");
		try {
			log.info("start - createCampaignJourney : {}", campaignJourneyRequest);
			ApiResponse apiResponse = new ApiResponse(ApiResponse.HTTP_RESPONSE_OK, true,
					messageSource.getMessage("common.operation.success", null, Locale.getDefault()),
					inboundService.createCampaignJourney(campaignJourneyRequest));

			return ResponseEntity.ok().body(apiResponse);
		} catch (ServiceException serviceException) {
			log.info("createCampaignJourney - serviceException : {}", serviceException.toString());
			throw  serviceException;
		} catch (Exception ex) {
			log.error("createCampaignJourney - Exception: {}", ex);
			throw ex;

		}
	}

	@PostMapping("/cust-action-trail")
	public ResponseEntity<ApiResponse> createCustomerActionTrail(HttpServletRequest request,
			@RequestHeader HttpHeaders requestHeader,
			@RequestBody CustomerActionTrailRequest customerActionTrailRequest) throws ServiceException {
		// log.info("start - createCustomerActionTrail");

		try {

			ApiResponse apiResponse = new ApiResponse(ApiResponse.HTTP_RESPONSE_OK, true,
					messageSource.getMessage("common.operation.success", null, Locale.getDefault()),
					callbackService.createCustomerActionTrail(customerActionTrailRequest));

			return ResponseEntity.ok().body(apiResponse);
		} catch (ServiceException serviceException) {
			log.info("createCustomerActionTrail - ServiceException: {}", serviceException);
			throw  serviceException;
		} catch (Exception ex) {

			log.error("createCustomerActionTrail - Exception: {}", ex);
			throw ex;

		}

	}

	@PutMapping("/update-cust-campaign-status")
	public ResponseEntity<Object> updateCustCampaignStatus(HttpServletRequest request,
			@Valid @RequestBody UpdateCampaignCustomerStatusRequest data) {
		log.info("start - updateCustCampaignStatus");
		try {
			Object apiResponse = callbackService.updateCustCampaignStatus(data);

			return ResponseEntity.ok().body(apiResponse);
		} catch (Exception ex) {

			log.error("updateCustCampaignStatus - cifNo: {}. Exception: {}", data.getCifNo(), ex.toString());
			throw ex;
		}
	}

	@PostMapping("/executeQuery")
	public ResponseEntity<Object> executeCqlQuery(@RequestParam("query") String query) {
		try {
			log.info("start - executeQuery : {}", query);
			ResultSet resultSet = cqlSession.execute(query);
			List<Map<String, Object>> rows = new ArrayList<>();

			for (Row row : resultSet) {
				Map<String, Object> option = new HashMap<>();
				for (ColumnDefinition columnDefinition : row.getColumnDefinitions()) {
					String columnName = columnDefinition.getName().asInternal();
					DataType dataType = columnDefinition.getType();
					Object columnValue = getColumnValue(row, columnName, dataType);
					option.put(columnName, columnValue);
				}
				rows.add(option);
			}

			ApiResponse apiResponse = new ApiResponse(ApiResponse.HTTP_RESPONSE_OK, true,
					messageSource.getMessage("common.operation.success", null, Locale.getDefault()), rows);

			HttpHeaders headers = new HttpHeaders();
			headers.add("Access-Control-Allow-Origin", "https://jogeteap72-joget.dbosuat.corp.alliancebg.com.my");
			return ResponseEntity.ok().headers(headers).body(apiResponse);
		} catch (Exception ex) {
			// Handle any exceptions that occur during query execution
			// You can customize the error response based on your requirements
			log.info("exception : {}", ex);
			throw ex;
		}
	}


	@PostMapping("/getCustomerProfileByEformID")
	public ResponseEntity<Object> getCustomerProfileByEform(@RequestBody ProfileInfoRequest request) throws ServiceException {
		return ResponseEntity.ok(	profileInfoService.getProfileInfoDBOSDB(
			request.getSchema(),
			request.getTable(),
			request.getDetailID()
		));
	}

	@PostMapping("/getCustomerProfileByDeviceID")
	public ResponseEntity<Object> getCustomerProfileByDevice(@RequestBody ProfileInfoRequest request) throws ServiceException {
		return ResponseEntity.ok(	profileInfoService.getProfileInfoByDeviceDBOSDB(
				request.getSchema(),
				request.getTable(),
				request.getDetailID()
		));
	}


	private Object getColumnValue(Row row, String columnName, DataType dataType) {
		if (row.isNull(columnName)) {
			return null;
		}
	
		if (dataType.equals(DataTypes.INT)) {
			return row.getInt(columnName);
		} else if (dataType.equals(DataTypes.TEXT)) {
			return row.getString(columnName);
		} else if (dataType.equals(DataTypes.BOOLEAN)) {
			return row.getBoolean(columnName);
		} else if (dataType.equals(DataTypes.DECIMAL)) {
			return row.getBigDecimal(columnName);
		} else if (dataType.equals(DataTypes.DATE)) {
			return row.getLocalDate(columnName);
		} else if (dataType.equals(DataTypes.TIME)) {
			return row.getLocalTime(columnName);
		} else if (dataType.equals(DataTypes.TIMESTAMP)) {
			return row.getInstant(columnName);
		} else if (dataType.equals(DataTypes.UUID)) {
			return row.getUuid(columnName);
		} else if (dataType instanceof SetType) {
			// Handle Set data type (e.g., Set(TEXT))
			SetType setType = (SetType) dataType;
			DataType elementType = setType.getElementType();
	
			if (elementType.equals(DataTypes.TEXT)) {
				return row.getSet(columnName, String.class).stream().collect(Collectors.toList());
			} else if (elementType.equals(DataTypes.INT)) {
				return row.getSet(columnName, Integer.class).stream().collect(Collectors.toList());
			} else {
				// Handle other element data types as needed
				log.warn("the datatype is not being handled, element datatype: {}", elementType);
				return null;
			}
		} else if (dataType instanceof MapType) {
			// Handle Map data type (e.g., Map(TEXT, INT))
			MapType mapType = (MapType) dataType;
			DataType keyType = mapType.getKeyType();
			DataType valueType = mapType.getValueType();
	
			if (keyType.equals(DataTypes.TEXT) && valueType.equals(DataTypes.INT)) {
				return row.getMap(columnName, String.class, Integer.class);
			} else if (keyType.equals(DataTypes.TEXT) && valueType.equals(DataTypes.TEXT)) {
				return row.getMap(columnName, String.class, String.class);
			} else {
				// Handle other key-value data types as needed
				log.warn("the datatype is not being handled, key datatype: {}, value datatype: {}", keyType, valueType);
				return null;
			}
		} else if (dataType instanceof ListType) {
			// Handle List data type (e.g., List(TEXT))
			ListType listType = (ListType) dataType;
			DataType elementType = listType.getElementType();
		
			if (elementType.equals(DataTypes.TEXT)) {
				return row.getList(columnName, String.class);
			} else {
				// Handle other element data types as needed
				log.warn("the datatype is not being handled, element datatype: {}", elementType);
				return null;
			}
		} else {
			// Handle other data types as needed
			log.warn("the datatype is not being handled, datatype: {}", dataType);
			return null;
		}
	}

	@PostMapping(value = "/getProfileById", produces = "application/json")
	public ResponseEntity<ApiResponse> getProfileById(@Valid @RequestBody ProfileRequest profileRequest) {
		log.info("getProfileById: {}", profileRequest);

		try {
			CacheCustomerProfileSoleCCResponse result = cacheService.checkCustomerProfile(profileRequest);
			log.info("getProfileById - result : {}", result);
			log.info("getProfileById - test : {}", result.getCifNo());

			CustomerProfile customerProfile = new CustomerProfile();
			customerProfile.setMobile(result.getMobile());
			customerProfile.setFullName(result.getCustomerName()); 
		   
		
			customerProfile.setDeviceUuid(result.getMobileFirstPlatformId()); 
			customerProfile.setDevicePlatform(result.getDevicePlatform()); 
			customerProfile.setIdNo(result.getNewIcNo());

			log.info("getProfileById - test 2: {}", customerProfile);

			// if (result.getBody() != null) {
			// 	log.info("show data : {}", result.getBody().get("data"));

			// 	Map<String, String> dataMap = (Map<String, String>) result.getBody().get("data");

			// 	if (dataMap != null) {
			// 		String uuid = dataMap.get("uuid");
			// 		if (uuid != null) {
			// 			log.info("UUID: {}", uuid);
			// 			// Use uuid in your logic here
			// 		}
			// 	}

				ApiResponse apiResponse = new ApiResponse(ApiResponse.HTTP_RESPONSE_OK, true,
						messageSource.getMessage("common.operation.success", null, Locale.getDefault()), result);

				return ResponseEntity.ok().body(apiResponse);
			// } else {
			// 	log.error("No profile data found or error occurred while retrieving profile data");
			// 	ApiResponse apiResponse = new ApiResponse(ApiResponse.HTTP_RESPONSE_NOT_FOUND, false,
			// 			"No profile data found or error occurred while retrieving profile data");
			// 	return ResponseEntity.status(HttpStatus.NOT_FOUND).body(apiResponse);
			// }
		} catch (Exception ex) {
			log.error("customerProfile - ProfileRequest: {}. Exception: {}", profileRequest, ex);
			ApiResponse apiResponse = new ApiResponse(ApiResponse.HTTP_RESPONSE_INTERNAL_SERVER_ERROR, false,
					ex.getLocalizedMessage());
			return ResponseEntity.internalServerError().body(apiResponse);
		}
	}
	

}
