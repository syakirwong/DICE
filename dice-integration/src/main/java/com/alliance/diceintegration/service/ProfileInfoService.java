package com.alliance.diceintegration.service;

import com.alliance.diceintegration.constant.ApiResponse;
import com.alliance.diceintegration.constant.DataBaseConnection;
import com.alliance.diceintegration.exception.ServiceException;
import com.alliance.diceintegration.model.CustomerProfile;
import com.alliance.diceintegration.model.CustomerProfileInfo;
import com.alliance.diceintegration.model.FilterCustomerSegmentation;
import com.alliance.diceintegration.repository.CustomerProfileInfoRepository;
import com.alliance.diceintegration.repository.ProfileInfoRepository;
import com.alliance.diceintegration.response.CustomerProfileResponse;
import com.alliance.diceintegration.response.EformUuidResponse;
import com.alliance.diceintegration.utility.SystemParam;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.sql.*;
import java.util.Date;
import java.util.*;

@Slf4j
@Service
@Transactional
public class ProfileInfoService {
    @Autowired
    private MessageSource messageSource;

    @Autowired(required = false)
    private CustomerProfileInfoRepository customerProfileInfoRepository;

    @Autowired
    private ProfileInfoRepository profileInfoRepository;

    @Value("${filter.customersegment.endpointUrl}")
    private String CUSTOMER_SEGMENT_ENDPOINT_URL;

    @Value("${vcc.table.view}")
    private String VCC_TABLE_VIEW;

    @Value("${vcc.db.schema}")
    private String VCC_DB_SCHEMA;

    public CustomerProfileInfo getProfileInfoDBOSDB(String eform_uuid, boolean retry) throws ServiceException {
        log.info("//---getProfileInfoDBOSDB---//");
        // String url = SystemParam.getInstance().getDBOSConnectionString();
        // String user = SystemParam.getInstance().getDBOSUsername();
        // String password = SystemParam.getInstance().getDBOSPassword();

        Connection connection = DataBaseConnection.getInstance().getConnection();
        CustomerProfileInfo customerProfileInfoDBOSDB = new CustomerProfileInfo();
        try {
            if (connection == null) {
                DataBaseConnection.getInstance();
                connection = DataBaseConnection.getInstance().getConnection();
            }
            if (connection.isClosed()) {
                log.info("Connection is Closed");
                connection.close();
                DataBaseConnection.connection = null;
                DataBaseConnection.setConnectionNull();
                DataBaseConnection.getInstance();
                connection = DataBaseConnection.getInstance().getConnection();
            }

            if (connection != null) {
                try {
                    log.info(messageSource.getMessage("common.request.success", null, null));

                    String sql = "SELECT FORMS.UUID AS FORMUUID,MOBILE.DEVICE_UUID,MOBILE.DEVICE_PLATFORM,FORMS.ALTERNATE_NAME,FORMS.FULL_NAME,MOBILE.STATUS_CODE AS "
                            +
                            "MOBILE_STATUS,MOBILE.MOBILE AS MOBILE_NUMBER,APP.STAGE_CODE AS APPLICATION_STAGE_CODE,MOBILE.ID_NO AS ID_NO,FORMS.PROMO_CODE AS INVITATION_CODE,"
                            +
                            "APP.CREATED_ON AS APPLICATION_CREATION_TIME,APP.LAST_MODIFIED_ON AS APPLICATION_LAST_UPDATE, FORMS.PAYLOAD AS USERINFO FROM EFORM.EFORMS FORMS "
                            +
                            "INNER JOIN EFORM.USERMOBILEINFO MOBILE ON MOBILE.FORM_UUID = FORMS.UUID INNER JOIN EFORM.APPLICATIONS APP ON MOBILE.APP_UUID = APP.UUID WHERE "
                            +
                            "FORMS.UUID='"
                            + eform_uuid + "' ORDER BY FORMS.CREATED_ON";

                    System.out.println(sql);
                    Statement selectStmt = connection.createStatement();
                    boolean isSucess = selectStmt.execute(sql);
                    log.info("getProfileInfoDBOSDB - isSucess : {}", isSucess);

                    System.out.println(isSucess);
                    ResultSet selectResults = selectStmt.executeQuery(sql);

                    while (selectResults.next()) {
                        String formUUID = selectResults.getString("FORMUUID");
                        String device_UUID = selectResults.getString("DEVICE_UUID");
                        String device_Plaftform = selectResults.getString("DEVICE_PLATFORM");
                        String alternate_Name = selectResults.getString("ALTERNATE_NAME");
                        String mobile_Status = selectResults.getString("MOBILE_STATUS");
                        String application_StageCode = selectResults.getString("APPLICATION_STAGE_CODE");
                        String invitation_Code = selectResults.getString("INVITATION_CODE");
                        String application_CreationTime = selectResults.getString("APPLICATION_CREATION_TIME");
                        String application_LastUpdate = selectResults.getString("APPLICATION_LAST_UPDATE");
                        String mobile_number = selectResults.getString("MOBILE_NUMBER");
                        String mobileIdNo = selectResults.getString("ID_NO");

                        String userinfo = selectResults.getString("USERINFO");

                        log.info(
                                "getProfileInfoDBOSDB - sql result after execute = formUUID: {}, device_UUID: {}, device_Plaftform: {}, alternate_Name: {}, mobile_Status: {}, application_StageCode: {}, invitation_Code: {}, application_CreationTime: {}, application_LastUpdate: {}, mobile_number: {}, mobileIdNo: {}, userinfo: {}",
                                formUUID, device_UUID, device_Plaftform, alternate_Name, mobile_Status,
                                application_StageCode, invitation_Code, application_CreationTime,
                                application_LastUpdate, mobile_number, mobileIdNo, userinfo);

                        CustomerProfileInfo customerProfileInfo = customerProfileInfoRepository
                                .findByEformUuid(
                                        formUUID);
                        if (customerProfileInfo != null) {
                            customerProfileInfoDBOSDB = customerProfileInfoRepository
                                    .findByEformUuid(
                                            formUUID);

                            customerProfileInfoDBOSDB.setModifiedDate(new Date());

                        } else {
                            customerProfileInfoDBOSDB.setCreatedDate(new Date());
                        }

                        customerProfileInfoDBOSDB.setEformUuid(formUUID);
                        customerProfileInfoDBOSDB.setDeviceUuid(device_UUID);
                        customerProfileInfoDBOSDB.setDevicePlatform(device_Plaftform);
                        customerProfileInfoDBOSDB.setAlternateName(alternate_Name);
                        customerProfileInfoDBOSDB.setMobileStatus(mobile_Status);
                        customerProfileInfoDBOSDB.setApplicationStageCode(application_StageCode);
                        customerProfileInfoDBOSDB.setInvitationCode(invitation_Code);
                        customerProfileInfoDBOSDB.setApplicationCreationTime(application_CreationTime);
                        customerProfileInfoDBOSDB.setApplicationLastUpdate(application_LastUpdate);
                        customerProfileInfoDBOSDB.setMobileNumber(mobile_number);
                        customerProfileInfoDBOSDB.setMobileIdNo(mobileIdNo);

                        try {

                            System.out.println("mobile_number" + mobile_number);
                            System.out.println("userinfo" + userinfo);
                            JSONObject json = new JSONObject(userinfo);
                            Iterator<?> jsonObjKeyItr = json.keys();
                            JSONObject jsonOccupation = new JSONObject();
                            JSONObject jsonIdentity = new JSONObject();
                            JSONObject jsonContactDetails = new JSONObject();
                            JSONObject jsonTerms = new JSONObject();
                            JSONObject jsonPdpa = new JSONObject();
                            JSONObject jsonFatca = new JSONObject();
                            JSONObject jsonCrs = new JSONObject();
                            JSONObject jsonDigitalSignature = new JSONObject();

                            while (jsonObjKeyItr.hasNext()) {// Loop through keys to get json element and add into
                                                             // additional data list
                                Object jsonObjKey = jsonObjKeyItr.next();
                                Object value = json.get(jsonObjKey.toString());

                                if (jsonObjKey.equals("etb")) {
                                    customerProfileInfoDBOSDB.setPayloadEtb((boolean) value);
                                } else if (jsonObjKey.equals("occupationIncome")) {
                                    jsonOccupation = new JSONObject(value.toString());

                                } else if (jsonObjKey.equals("applicationRef")) {

                                    customerProfileInfoDBOSDB.setApplicationRef(value.toString());

                                } else if (jsonObjKey.equals("identityInfo")) {
                                    jsonIdentity = new JSONObject(value.toString());

                                } else if (jsonObjKey.equals("primaryAccountHolder")) {
                                    customerProfileInfoDBOSDB.setPrimaryAccountHolder((boolean) value);

                                } else if (jsonObjKey.equals("segmentCode")) {
                                    customerProfileInfoDBOSDB.setSegmentCode(value.toString());

                                } else if (jsonObjKey.equals("relationship")) {
                                    customerProfileInfoDBOSDB.setRelationship(value.toString());
                                } else if (jsonObjKey.equals("version")) {
                                    customerProfileInfoDBOSDB.setVersion(value.toString());

                                } else if (jsonObjKey.equals("idNo")) {
                                    customerProfileInfoDBOSDB.setIdNo(value.toString());

                                } else if (jsonObjKey.equals("contactDetails")) {
                                    jsonContactDetails = new JSONObject(value.toString());

                                } else if (jsonObjKey.equals("stageCode")) {
                                    customerProfileInfoDBOSDB.setSegmentCode(value.toString());
                                }

                                else if (jsonObjKey.equals("idTypeCode")) {
                                    customerProfileInfoDBOSDB.setIdTypeCode(value.toString());

                                } else if (jsonObjKey.equals("termsConditions")) {
                                    jsonTerms = new JSONObject(value.toString());

                                } else if (jsonObjKey.equals("pdpa")) {
                                    jsonPdpa = new JSONObject(value.toString());

                                } else if (jsonObjKey.equals("fatca")) {
                                    jsonFatca = new JSONObject(value.toString());

                                } else if (jsonObjKey.equals("crs")) {
                                    jsonCrs = new JSONObject(value.toString());

                                } else if (jsonObjKey.equals("digitalSignature")) {
                                    jsonDigitalSignature = new JSONObject(value.toString());

                                }
                            }

                            // System.out.println(jsonOccupation.toString());
                            Iterator<?> jsonObjKeyItrOcc = jsonOccupation.keys();

                            while (jsonObjKeyItrOcc.hasNext()) {

                                Object jsonObjKey = jsonObjKeyItrOcc.next();
                                Object value = jsonOccupation.get(jsonObjKey.toString());

                                if (jsonObjKey.equals("occupationCCRISCode")) {
                                    customerProfileInfoDBOSDB.setPayloadOccupationCCRISCode(value.toString());
                                } else if (jsonObjKey.equals("occupationAMLCode")) {
                                    customerProfileInfoDBOSDB.setPayloadOccupationAMLCode(value.toString());
                                } else if (jsonObjKey.equals("bnmCounterPartyCode")) {
                                    customerProfileInfoDBOSDB.setPayloadBnmCounterPartyCode(value.toString());
                                } else if (jsonObjKey.equals("industryCategory")) {
                                    customerProfileInfoDBOSDB.setPayloadBnmCounterPartyCode(value.toString());
                                } else if (jsonObjKey.equals("sourceOfWealthCode")) {
                                    customerProfileInfoDBOSDB.setPayloadSourceOfWealthCode(value.toString());
                                } else if (jsonObjKey.equals("grossAnnualIncome")) {
                                    customerProfileInfoDBOSDB.setPayloadGrossAnnualIncome(value.toString());
                                } else if (jsonObjKey.equals("employmentSectorCode")) {
                                    customerProfileInfoDBOSDB.setPayloadEmploymentSectorCode(value.toString());
                                } else if (jsonObjKey.equals("standardIndustryClassCode")) {
                                    customerProfileInfoDBOSDB
                                            .setPayloadStandardIndustryClassCode(value.toString());
                                } else if (jsonObjKey.equals("industryCode")) {
                                    customerProfileInfoDBOSDB.setPayloadIndustryCode(value.toString());
                                } else if (jsonObjKey.equals("maritalStatusCode")) {

                                    customerProfileInfoDBOSDB.setPayloadMaritalStatusCode(value.toString());
                                } else if (jsonObjKey.equals("occupationCategoryCode")) {
                                    customerProfileInfoDBOSDB.setPayloadOccupationCategoryCode(value.toString());

                                } else if (jsonObjKey.equals("sourceOfFundCode")) {
                                    customerProfileInfoDBOSDB.setPayloadSourceOfWealthCode(value.toString());

                                } else if (jsonObjKey.equals("employmentTypeCode")) {
                                    customerProfileInfoDBOSDB.setPayloadEmploymentTypeCode(value.toString());
                                }
                            }
                            // System.out.println(jsonIdentity.toString());
                            Iterator<?> jsonObjKeyItrIdentity = jsonIdentity.keys();

                            while (jsonObjKeyItrIdentity.hasNext()) {

                                Object jsonObjKey = jsonObjKeyItrIdentity.next();
                                Object value = jsonIdentity.get(jsonObjKey.toString());

                                if (jsonObjKey.equals("country")) {
                                    customerProfileInfoDBOSDB.setIdentityInfoCountry(value.toString());
                                } else if (jsonObjKey.equals("address3")) {
                                    customerProfileInfoDBOSDB.setIdentityInfoAddress3(value.toString());
                                } else if (jsonObjKey.equals("address2")) {
                                    customerProfileInfoDBOSDB.setIdentityInfoAddress2(value.toString());
                                } else if (jsonObjKey.equals("city")) {
                                    customerProfileInfoDBOSDB.setIdentityInfoCity(value.toString());
                                } else if (jsonObjKey.equals("cifNo")) {
                                    customerProfileInfoDBOSDB.setIdentityInfoCifNo(value.toString());
                                } else if (jsonObjKey.equals("address1")) {
                                    customerProfileInfoDBOSDB.setIdentityInfoAddress1(value.toString());
                                } else if (jsonObjKey.equals("fullName")) {
                                    customerProfileInfoDBOSDB.setIdentityInfoFullname(value.toString());
                                } else if (jsonObjKey.equals("genderCode")) {
                                    customerProfileInfoDBOSDB.setIdentityInfoGenderCode(value.toString());
                                } else if (jsonObjKey.equals("nationality")) {
                                    customerProfileInfoDBOSDB.setIdentityInfoNationality(value.toString());
                                } else if (jsonObjKey.equals("issuingCountry")) {
                                    customerProfileInfoDBOSDB.setIdentityInfoIssuingCountry(value.toString());
                                } else if (jsonObjKey.equals("dob")) {
                                    customerProfileInfoDBOSDB.setIdentityInfoDob(value.toString());
                                } else if (jsonObjKey.equals("postCode")) {
                                    customerProfileInfoDBOSDB.setIdentityInfoPostCode(value.toString());
                                } else if (jsonObjKey.equals("stateCode")) {
                                    customerProfileInfoDBOSDB.setIdentityInfoStateCode(value.toString());
                                } else if (jsonObjKey.equals("religionCode")) {
                                    customerProfileInfoDBOSDB.setIdentityInfoReligionCode(value.toString());
                                } else if (jsonObjKey.equals("raceCode")) {
                                    customerProfileInfoDBOSDB.setIdentityInfoRaceCode(value.toString());
                                }
                            }
                            // System.out.println(jsonContactDetails.toString());
                            Iterator<?> jsonObjKeyItrContactDetails = jsonContactDetails.keys();

                            while (jsonObjKeyItrContactDetails.hasNext()) {

                                Object jsonObjKey = jsonObjKeyItrContactDetails.next();
                                Object value = jsonContactDetails.get(jsonObjKey.toString());

                                if (jsonObjKey.equals("country")) {
                                    customerProfileInfoDBOSDB.setContactDetailsCountry(value.toString());
                                } else if (jsonObjKey.equals("address3")) {
                                    customerProfileInfoDBOSDB.setContactDetailsAddress3(value.toString());
                                } else if (jsonObjKey.equals("address2")) {
                                    customerProfileInfoDBOSDB.setContactDetailsAddress2(value.toString());
                                } else if (jsonObjKey.equals("isContactAddressSameWithMYKAD")) {
                                    customerProfileInfoDBOSDB
                                            .setContactDetailsIsContactAddressSameWithMYKAD(value.toString());
                                } else if (jsonObjKey.equals("city")) {
                                    customerProfileInfoDBOSDB.setContactDetailsCity(value.toString());
                                } else if (jsonObjKey.equals("address1")) {
                                    customerProfileInfoDBOSDB.setContactDetailsAddress1(value.toString());
                                } else if (jsonObjKey.equals("postCode")) {
                                    customerProfileInfoDBOSDB.setContactDetailsPostCode(value.toString());
                                } else if (jsonObjKey.equals("stateCode")) {
                                    customerProfileInfoDBOSDB.setContactDetailsStateCode(value.toString());
                                } else if (jsonObjKey.equals("mobileNo")) {
                                    if (value.toString() != null && !value.toString().isEmpty()) {
                                        customerProfileInfoDBOSDB.setMobileNumber(value.toString());
                                    }
                                } else if (jsonObjKey.equals("email")) {
                                    customerProfileInfoDBOSDB.setContactDetailsEmail(value.toString());
                                }
                            }

                            Iterator<?> jsonObjKeyItrTerms = jsonTerms.keys();

                            while (jsonObjKeyItrTerms.hasNext()) {

                                Object jsonObjKey = jsonObjKeyItrTerms.next();
                                Object value = jsonTerms.get(jsonObjKey.toString());

                                if (jsonObjKey.equals("isAcceptedTnc")) {
                                    customerProfileInfoDBOSDB.setTermsConditionsIsAcceptedTnc((boolean) value);
                                }
                            }

                            Iterator<?> jsonObjKeyItrPdpa = jsonPdpa.keys();

                            while (jsonObjKeyItrPdpa.hasNext()) {

                                Object jsonObjKey = jsonObjKeyItrPdpa.next();
                                Object value = jsonPdpa.get(jsonObjKey.toString());

                                if (jsonObjKey.equals("pdpa")) {
                                    customerProfileInfoDBOSDB.setPdpaIsAgreePDPA((boolean) value);
                                }
                            }

                            Iterator<?> jsonObjKeyItrFatca = jsonFatca.keys();

                            while (jsonObjKeyItrFatca.hasNext()) {

                                Object jsonObjKey = jsonObjKeyItrFatca.next();
                                Object value = jsonFatca.get(jsonObjKey.toString());

                                if (jsonObjKey.equals("fatca")) {
                                    customerProfileInfoDBOSDB.setFatcaIsUSPerson((boolean) value);
                                }
                            }

                            Iterator<?> jsonObjKeyItrCrs = jsonCrs.keys();

                            while (jsonObjKeyItrCrs.hasNext()) {

                                Object jsonObjKey = jsonObjKeyItrCrs.next();
                                Object value = jsonCrs.get(jsonObjKey.toString());

                                if (jsonObjKey.equals("fatca")) {
                                    customerProfileInfoDBOSDB.setCrsTaxResidenceStatus(value.toString());
                                }
                            }

                            Iterator<?> jsonObjKeyItrjsonDigitalSignature = jsonDigitalSignature.keys();

                            while (jsonObjKeyItrjsonDigitalSignature.hasNext()) {

                                Object jsonObjKey = jsonObjKeyItrjsonDigitalSignature.next();
                                Object value = jsonDigitalSignature.get(jsonObjKey.toString());

                                if (jsonObjKey.equals("digitalSignature")) {

                                    // java.sql.Blob blobData = com.ibm.db2.jcc.t2zos.DB2LobFactory
                                    // .createBlob(value.toString().getBytes());

                                    // customerProfileInfoDBOSDB.setDigitalSignature(blobData);

                                } else if (jsonObjKey.equals("currentDate")) {

                                    customerProfileInfoDBOSDB.setDigitalSignatureCurrentDate(value.toString());

                                } else if (jsonObjKey.equals("currentTime")) {
                                    customerProfileInfoDBOSDB.setDigitalSignatureCurrentTime(value.toString());
                                }
                            }

                            log.info("Filter Customer Profile Info");
                            FilterCustomerSegmentation filterProfileInfo = sendFilterCustomerSegmentation(
                                    customerProfileInfoDBOSDB);
                            customerProfileInfoDBOSDB.setEformUuid(filterProfileInfo.getEformUuid());
                            customerProfileInfoDBOSDB.setDevicePlatform(filterProfileInfo.getDevicePlatform());
                            customerProfileInfoDBOSDB.setDeviceUuid(filterProfileInfo.getDeviceUuid());
                            customerProfileInfoDBOSDB
                                    .setIdentityInfoFullname(filterProfileInfo.getIdentityInfoFullname());
                            customerProfileInfoDBOSDB
                                    .setIdentityInfoGenderCode(filterProfileInfo.getIdentityInfoGenderCode());
                            customerProfileInfoDBOSDB.setIdNo(filterProfileInfo.getIdNo());
                            customerProfileInfoDBOSDB.setIdTypeCode(filterProfileInfo.getIdTypeCode());

                            log.info("Filter Customer Profile Info - " + filterProfileInfo.toString());

                            customerProfileInfoRepository.saveAndFlush(customerProfileInfoDBOSDB);

                            log.info("Customer Profile Info  - " + customerProfileInfoDBOSDB.toString());

                            return customerProfileInfoDBOSDB;

                        } catch (JSONException jsonException) {
                            // System.out.println(jsonException.toString());
                            log.error(jsonException.toString());
                        }
                    }

                } catch (SQLException e) {
                    // System.out.println(e.toString());
                    log.error("SQLException1 : " + e.toString());
                    try {
                        if (connection.isClosed()) {
                            log.error("Connection is Closed2");
                        } else {
                            connection.close();
                        }
                        DataBaseConnection.connection = null;
                        DataBaseConnection.setConnectionNull();
                        DataBaseConnection.getInstance();
                        connection = DataBaseConnection.getInstance().getConnection();
                        if (retry == false) {
                            return getProfileInfoDBOSDB(eform_uuid, true);
                        }
                    } catch (SQLException ex) {
                        log.error("SQL Exception2 : " + ex.toString());
                    }
                }

            }
        } catch (Exception ex) {
            log.error("Error integration :" + ex.getMessage());
        }

        return customerProfileInfoDBOSDB;

    }

    public FilterCustomerSegmentation sendFilterCustomerSegmentation(CustomerProfileInfo customerProfileInfo)
            throws ServiceException {
        try {

            SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
            factory.setConnectTimeout(15000);
            factory.setReadTimeout(15000);

            HttpHeaders headers = new HttpHeaders();
            // headers.set("Content-Type", "application/json");
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, String> bodyPair = new HashMap<String, String>();
            bodyPair.put("contactDetailsEmail",
                    customerProfileInfo.getContactDetailsEmail());
            bodyPair.put("mobileNumber",
                    customerProfileInfo.getMobileNumber());
            bodyPair.put("devicePlatform", customerProfileInfo.getDevicePlatform());
            bodyPair.put("deviceUuid", customerProfileInfo.getDeviceUuid());
            bodyPair.put("eformUuid", customerProfileInfo.getEformUuid());
            bodyPair.put("idNo", customerProfileInfo.getIdNo());
            bodyPair.put("idTypeCode", customerProfileInfo.getIdTypeCode());
            bodyPair.put("identityInfoFullname",
                    customerProfileInfo.getIdentityInfoFullname());
            bodyPair.put("identityInfoGenderCode",
                    customerProfileInfo.getIdentityInfoFullname());

            HttpEntity<Map<String, String>> entity = new HttpEntity<>(bodyPair, headers);

            RestTemplate restTemplate = new RestTemplate(factory);
            ResponseEntity<Map> response = restTemplate.postForEntity(CUSTOMER_SEGMENT_ENDPOINT_URL, entity,
                    Map.class);

            if (response.getStatusCode().value() != 200) {
                log.error("call back  - failed : {}", response);
                throw new ServiceException(ApiResponse.HTTP_RESPONSE_BAD_REQUEST, response.toString());
            } else {
                FilterCustomerSegmentation jsonFilterCustomerSegmentation = new FilterCustomerSegmentation();

                ObjectMapper oMapper = new ObjectMapper();
                Map jsonObjKey = oMapper.convertValue(
                        response.getBody().get("result"),
                        Map.class);

                // log.info("response.getBody().get('result')" +
                // response.getBody().get("result"));
                // log.info(" log.info(jsonObjKey.toString());" + jsonObjKey.toString());

                if (jsonObjKey.containsKey("contactDetailsEmail")) {
                    jsonFilterCustomerSegmentation
                            .setContactDetailsEmail(jsonObjKey.get("contactDetailsEmail") == null ? ""
                                    : jsonObjKey.get("contactDetailsEmail").toString());
                }
                if (jsonObjKey.containsKey("mobileNumber")) {
                    jsonFilterCustomerSegmentation
                            .setMobileNumber(jsonObjKey.get("mobileNumber") == null ? ""
                                    : jsonObjKey.get("mobileNumber").toString());
                }
                if (jsonObjKey.containsKey("devicePlatform")) {
                    jsonFilterCustomerSegmentation
                            .setDevicePlatform(jsonObjKey.get("devicePlatform") == null ? ""
                                    : jsonObjKey.get("devicePlatform").toString());
                }
                if (jsonObjKey.containsKey("deviceUuid")) {
                    jsonFilterCustomerSegmentation
                            .setDeviceUuid(jsonObjKey.get("deviceUuid") == null ? ""
                                    : jsonObjKey.get("deviceUuid").toString());
                }
                if (jsonObjKey.containsKey("eformUuid")) {
                    jsonFilterCustomerSegmentation
                            .setEformUuid(
                                    jsonObjKey.get("eformUuid") == null ? "" : jsonObjKey.get("eformUuid").toString());
                }
                if (jsonObjKey.containsKey("idNo")) {
                    jsonFilterCustomerSegmentation
                            .setIdNo(jsonObjKey.get("idNo") == null ? "" : jsonObjKey.get("idNo").toString());
                }
                if (jsonObjKey.containsKey("idTypeCode")) {
                    jsonFilterCustomerSegmentation
                            .setIdTypeCode(jsonObjKey.get("idTypeCode") == null ? ""
                                    : jsonObjKey.get("idTypeCode").toString());
                }
                if (jsonObjKey.containsKey("identityInfoFullname")) {
                    jsonFilterCustomerSegmentation
                            .setIdentityInfoFullname(jsonObjKey.get("identityInfoFullname") == null ? ""
                                    : jsonObjKey.get("identityInfoFullname").toString());
                }
                if (jsonObjKey.containsKey("identityInfoGenderCode")) {
                    jsonFilterCustomerSegmentation
                            .setIdentityInfoGenderCode(jsonObjKey.get("identityInfoGenderCode") == null ? ""
                                    : jsonObjKey.get("identityInfoGenderCode").toString());
                }

                log.info("call back - status code {}, body {}", response.getStatusCode(), response.getBody());

                return jsonFilterCustomerSegmentation;
            }
        } catch (Exception ex) {
            log.error("The Error occur when FilterCustomer service Info :: {}", ex.toString());
            throw new ServiceException(ApiResponse.HTTP_RESPONSE_INTERNAL_SERVER_ERROR,
                    ex.toString());
        }
    }

    public CustomerProfileInfo getProfileInfoDBOSDBTest(String eform_uuid) throws ServiceException {

        log.info("//---------getProfileInfoDBOSDBTest-----//");

        CustomerProfileInfo customerProfileInfoDBOSDB = customerProfileInfoRepository
                .findByEformUuid("97e00410-4b12-4180-b461-628f69a773bd") == null ? new CustomerProfileInfo()
                        : customerProfileInfoRepository.findByEformUuid("97e00410-4b12-4180-b461-628f69a773bd");

        String cb = "{'etb':false,'occupationIncome':{'occupationCCRISCode':'9330','occupationAMLCode':'G001','bnmCounterPartyCode':'78','industryCategory':'Q8','sourceOfWealthCode':'F16','otherSourceOfFund':'Savings ','grossAnnualIncome':1800,'employerName':'Kanagamalar ','employmentSectorCode':'Q1000','standardIndustryClassCode':'M003','industryCode':'86101','maritalStatusCode':'W','occupationCategoryCode':'OI','sourceOfFundCode':'F20','employmentTypeCode':'113'},'applicationRef':'3efaa608-0fba-498e-86c8-4c36e6e4fe7d','identityInfo':{'country':'MY','address3':'SEKSYEN 25','address2':'TAMAN ALAM INDAH','city':'SHAH ALAM','cifNo':'','address1':'17 JALAN TELOK JAMBU 1B','fullName':'KANAGAMALAR A/P KANAGASABAI','genderCode':'F','nationality':'MY','issuingCountry':'MY','dob':'1979-05-27','postCode':'42450','stateCode':'B','religionCode':'','raceCode':'IND'},'primaryAccountHolder':true,'segmentCode':'MSIANPR','relationship':'null','version':'v1','idNo':'790527105060','contactDetails':{'country':'MY','address3':'SEKSYEN 25','isContactAddressSameWithMYKAD':true,'address2':'TAMAN ALAM INDAH','city':'SHAH ALAM','address1':'17 JALAN TELOK JAMBU 1B','postCode':'42450','stateCode':'B','mobileNo':'+60102629845','email':'zenithashwan@gmail.com'},'stageCode':'S','idTypeCode':'IN','termsConditions':{'isAcceptedTnc':true},'pdpa':{'isAgreePDPA':true},'crs':{'taxResidenceStatus':'Malaysia Tax Resident'},'fatca':{'isUSPerson':false},'digitalSignature':{'digitalSignature':'data:image/tiff;base64,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','currentDate':'2022-09-22','currentTime':'12:52:43'}}'";

        log.info(messageSource.getMessage("common.request.success", null, null));

        try {

            JSONObject json = new JSONObject(cb);

            Iterator<?> jsonObjKeyItr = json.keys();
            JSONObject jsonOccupation = new JSONObject();
            JSONObject jsonIdentity = new JSONObject();
            JSONObject jsonContactDetails = new JSONObject();
            JSONObject jsonTerms = new JSONObject();
            JSONObject jsonPdpa = new JSONObject();
            JSONObject jsonFatca = new JSONObject();
            JSONObject jsonCrs = new JSONObject();
            JSONObject jsonDigitalSignature = new JSONObject();

            while (jsonObjKeyItr.hasNext()) {// Loop through keys to get json element and add into
                                             // additional data list
                Object jsonObjKey = jsonObjKeyItr.next();
                Object value = json.get(jsonObjKey.toString());

                if (jsonObjKey.equals("etb")) {
                    customerProfileInfoDBOSDB.setPayloadEtb((boolean) value);
                } else if (jsonObjKey.equals("occupationIncome")) {
                    jsonOccupation = new JSONObject(value.toString());

                } else if (jsonObjKey.equals("applicationRef")) {

                    customerProfileInfoDBOSDB.setApplicationRef(value.toString());

                } else if (jsonObjKey.equals("identityInfo")) {
                    jsonIdentity = new JSONObject(value.toString());

                } else if (jsonObjKey.equals("primaryAccountHolder")) {
                    customerProfileInfoDBOSDB.setPrimaryAccountHolder((boolean) value);

                } else if (jsonObjKey.equals("segmentCode")) {
                    customerProfileInfoDBOSDB.setSegmentCode(value.toString());

                } else if (jsonObjKey.equals("relationship")) {
                    customerProfileInfoDBOSDB.setRelationship(value.toString());
                } else if (jsonObjKey.equals("version")) {
                    customerProfileInfoDBOSDB.setVersion(value.toString());

                } else if (jsonObjKey.equals("idNo")) {
                    customerProfileInfoDBOSDB.setVersion(value.toString());

                } else if (jsonObjKey.equals("contactDetails")) {
                    jsonContactDetails = new JSONObject(value.toString());

                } else if (jsonObjKey.equals("stageCode")) {
                    customerProfileInfoDBOSDB.setSegmentCode(value.toString());
                }

                else if (jsonObjKey.equals("idTypeCode")) {
                    customerProfileInfoDBOSDB.setIdTypeCode(value.toString());

                } else if (jsonObjKey.equals("termsConditions")) {
                    jsonTerms = new JSONObject(value.toString());

                } else if (jsonObjKey.equals("pdpa")) {
                    jsonPdpa = new JSONObject(value.toString());

                } else if (jsonObjKey.equals("fatca")) {
                    jsonFatca = new JSONObject(value.toString());

                } else if (jsonObjKey.equals("crs")) {
                    jsonCrs = new JSONObject(value.toString());

                } else if (jsonObjKey.equals("digitalSignature")) {
                    jsonDigitalSignature = new JSONObject(value.toString());

                }
            }

            System.out.println(jsonOccupation.toString());
            Iterator<?> jsonObjKeyItrOcc = jsonOccupation.keys();

            while (jsonObjKeyItrOcc.hasNext()) {

                Object jsonObjKey = jsonObjKeyItrOcc.next();
                Object value = jsonOccupation.get(jsonObjKey.toString());

                if (jsonObjKey.equals("occupationCCRISCode")) {
                    customerProfileInfoDBOSDB.setPayloadOccupationCCRISCode(value.toString());
                } else if (jsonObjKey.equals("occupationAMLCode")) {
                    customerProfileInfoDBOSDB.setPayloadOccupationAMLCode(value.toString());
                } else if (jsonObjKey.equals("bnmCounterPartyCode")) {
                    customerProfileInfoDBOSDB.setPayloadBnmCounterPartyCode(value.toString());
                } else if (jsonObjKey.equals("industryCategory")) {
                    customerProfileInfoDBOSDB.setPayloadBnmCounterPartyCode(value.toString());
                } else if (jsonObjKey.equals("sourceOfWealthCode")) {
                    customerProfileInfoDBOSDB.setPayloadSourceOfWealthCode(value.toString());
                } else if (jsonObjKey.equals("grossAnnualIncome")) {
                    customerProfileInfoDBOSDB.setPayloadGrossAnnualIncome(value.toString());
                } else if (jsonObjKey.equals("employmentSectorCode")) {
                    customerProfileInfoDBOSDB.setPayloadEmploymentSectorCode(value.toString());
                } else if (jsonObjKey.equals("standardIndustryClassCode")) {
                    customerProfileInfoDBOSDB
                            .setPayloadStandardIndustryClassCode(value.toString());
                } else if (jsonObjKey.equals("industryCode")) {
                    customerProfileInfoDBOSDB.setPayloadIndustryCode(value.toString());
                } else if (jsonObjKey.equals("maritalStatusCode")) {

                    customerProfileInfoDBOSDB.setPayloadMaritalStatusCode(value.toString());
                } else if (jsonObjKey.equals("occupationCategoryCode")) {
                    customerProfileInfoDBOSDB.setPayloadOccupationCategoryCode(value.toString());

                } else if (jsonObjKey.equals("sourceOfFundCode")) {
                    customerProfileInfoDBOSDB.setPayloadSourceOfWealthCode(value.toString());

                } else if (jsonObjKey.equals("employmentTypeCode")) {
                    customerProfileInfoDBOSDB.setPayloadEmploymentTypeCode(value.toString());
                }
            }
            System.out.println(jsonIdentity.toString());
            Iterator<?> jsonObjKeyItrIdentity = jsonIdentity.keys();

            while (jsonObjKeyItrIdentity.hasNext()) {

                Object jsonObjKey = jsonObjKeyItrIdentity.next();
                Object value = jsonIdentity.get(jsonObjKey.toString());

                if (jsonObjKey.equals("country")) {
                    customerProfileInfoDBOSDB.setIdentityInfoCountry(value.toString());
                } else if (jsonObjKey.equals("address3")) {
                    customerProfileInfoDBOSDB.setIdentityInfoAddress3(value.toString());
                } else if (jsonObjKey.equals("address2")) {
                    customerProfileInfoDBOSDB.setIdentityInfoAddress2(value.toString());
                } else if (jsonObjKey.equals("city")) {
                    customerProfileInfoDBOSDB.setIdentityInfoCity(value.toString());
                } else if (jsonObjKey.equals("cifNo")) {
                    customerProfileInfoDBOSDB.setIdentityInfoCifNo(value.toString());
                } else if (jsonObjKey.equals("address1")) {
                    customerProfileInfoDBOSDB.setIdentityInfoAddress1(value.toString());
                } else if (jsonObjKey.equals("fullName")) {
                    customerProfileInfoDBOSDB.setIdentityInfoFullname(value.toString());
                } else if (jsonObjKey.equals("genderCode")) {
                    customerProfileInfoDBOSDB.setIdentityInfoGenderCode(value.toString());
                } else if (jsonObjKey.equals("nationality")) {
                    customerProfileInfoDBOSDB.setIdentityInfoNationality(value.toString());
                } else if (jsonObjKey.equals("issuingCountry")) {
                    customerProfileInfoDBOSDB.setIdentityInfoIssuingCountry(value.toString());
                } else if (jsonObjKey.equals("dob")) {
                    customerProfileInfoDBOSDB.setIdentityInfoDob(value.toString());
                } else if (jsonObjKey.equals("postCode")) {
                    customerProfileInfoDBOSDB.setIdentityInfoPostCode(value.toString());
                } else if (jsonObjKey.equals("stateCode")) {
                    customerProfileInfoDBOSDB.setIdentityInfoStateCode(value.toString());
                } else if (jsonObjKey.equals("religionCode")) {
                    customerProfileInfoDBOSDB.setIdentityInfoReligionCode(value.toString());
                } else if (jsonObjKey.equals("raceCode")) {
                    customerProfileInfoDBOSDB.setIdentityInfoRaceCode(value.toString());
                }
            }
            System.out.println(jsonContactDetails.toString());
            Iterator<?> jsonObjKeyItrContactDetails = jsonContactDetails.keys();

            while (jsonObjKeyItrContactDetails.hasNext()) {

                Object jsonObjKey = jsonObjKeyItrContactDetails.next();
                Object value = jsonContactDetails.get(jsonObjKey.toString());

                if (jsonObjKey.equals("country")) {
                    customerProfileInfoDBOSDB.setContactDetailsCountry(value.toString());
                } else if (jsonObjKey.equals("address3")) {
                    customerProfileInfoDBOSDB.setContactDetailsAddress3(value.toString());
                } else if (jsonObjKey.equals("address2")) {
                    customerProfileInfoDBOSDB.setContactDetailsAddress2(value.toString());
                } else if (jsonObjKey.equals("isContactAddressSameWithMYKAD")) {
                    customerProfileInfoDBOSDB
                            .setContactDetailsIsContactAddressSameWithMYKAD(value.toString());
                } else if (jsonObjKey.equals("city")) {
                    customerProfileInfoDBOSDB.setContactDetailsCity(value.toString());
                } else if (jsonObjKey.equals("address1")) {
                    customerProfileInfoDBOSDB.setContactDetailsAddress1(value.toString());
                } else if (jsonObjKey.equals("postCode")) {
                    customerProfileInfoDBOSDB.setContactDetailsPostCode(value.toString());
                } else if (jsonObjKey.equals("stateCode")) {
                    customerProfileInfoDBOSDB.setContactDetailsStateCode(value.toString());
                } else if (jsonObjKey.equals("email")) {
                    customerProfileInfoDBOSDB.setContactDetailsEmail(value.toString());
                }
            }

            Iterator<?> jsonObjKeyItrTerms = jsonTerms.keys();

            while (jsonObjKeyItrTerms.hasNext()) {

                Object jsonObjKey = jsonObjKeyItrTerms.next();
                Object value = jsonTerms.get(jsonObjKey.toString());

                if (jsonObjKey.equals("isAcceptedTnc")) {
                    customerProfileInfoDBOSDB.setTermsConditionsIsAcceptedTnc((boolean) value);
                }
            }

            Iterator<?> jsonObjKeyItrPdpa = jsonPdpa.keys();

            while (jsonObjKeyItrPdpa.hasNext()) {

                Object jsonObjKey = jsonObjKeyItrPdpa.next();
                Object value = jsonPdpa.get(jsonObjKey.toString());

                if (jsonObjKey.equals("pdpa")) {
                    customerProfileInfoDBOSDB.setPdpaIsAgreePDPA((boolean) value);
                }
            }

            Iterator<?> jsonObjKeyItrFatca = jsonFatca.keys();

            while (jsonObjKeyItrFatca.hasNext()) {

                Object jsonObjKey = jsonObjKeyItrFatca.next();
                Object value = jsonFatca.get(jsonObjKey.toString());

                if (jsonObjKey.equals("fatca")) {
                    customerProfileInfoDBOSDB.setFatcaIsUSPerson((boolean) value);
                }
            }

            Iterator<?> jsonObjKeyItrCrs = jsonCrs.keys();

            while (jsonObjKeyItrCrs.hasNext()) {

                Object jsonObjKey = jsonObjKeyItrCrs.next();
                Object value = jsonCrs.get(jsonObjKey.toString());

                if (jsonObjKey.equals("fatca")) {
                    customerProfileInfoDBOSDB.setCrsTaxResidenceStatus(value.toString());
                }
            }

            Iterator<?> jsonObjKeyItrjsonDigitalSignature = jsonDigitalSignature.keys();

            while (jsonObjKeyItrjsonDigitalSignature.hasNext()) {

                Object jsonObjKey = jsonObjKeyItrjsonDigitalSignature.next();
                Object value = jsonDigitalSignature.get(jsonObjKey.toString());

                if (jsonObjKey.equals("digitalSignature")) {

                    // SerialBlob blob = new SerialBlob(value.toString().toCharArray());
                    // Blob docInBlob = new SerialBlob(value.toString().getBytes());
                    // java.sql.Blob blobData = com.ibm.db2.jcc.t2zos.DB2LobFactory
                    // .createBlob(value.toString().getBytes());

                    // customerProfileInfoDBOSDB.setDigitalSignature(blobData);

                } else if (jsonObjKey.equals("currentDate")) {

                    customerProfileInfoDBOSDB.setDigitalSignatureCurrentDate(value.toString());

                } else if (jsonObjKey.equals("currentTime")) {
                    customerProfileInfoDBOSDB.setDigitalSignatureCurrentTime(value.toString());
                }
            }

            log.info("Filter Customer Profile Info");
            customerProfileInfoDBOSDB.setEformUuid("97e00410-4b12-4180-b461-628f69a773bd");
            customerProfileInfoDBOSDB.setDeviceUuid("2CE59C6E-B67B-4927-9A3F-707E5121E9C4");
            customerProfileInfoDBOSDB.setDevicePlatform("iOS");
            FilterCustomerSegmentation filterProfileInfo = sendFilterCustomerSegmentation(
                    customerProfileInfoDBOSDB);

            customerProfileInfoDBOSDB.setEformUuid(filterProfileInfo.getEformUuid());
            customerProfileInfoDBOSDB.setDevicePlatform(filterProfileInfo.getDevicePlatform());
            customerProfileInfoDBOSDB.setDeviceUuid(filterProfileInfo.getDeviceUuid());
            customerProfileInfoDBOSDB.setIdentityInfoFullname(filterProfileInfo.getIdentityInfoFullname());
            customerProfileInfoDBOSDB
                    .setIdentityInfoGenderCode(filterProfileInfo.getIdentityInfoGenderCode());
            customerProfileInfoDBOSDB.setIdNo(filterProfileInfo.getIdNo());
            customerProfileInfoDBOSDB
                    .setMobileNumber(filterProfileInfo.getMobileNumber());
            customerProfileInfoDBOSDB.setIdTypeCode(filterProfileInfo.getIdTypeCode());

            log.info("Filter Customer Profile Info - " + filterProfileInfo.toString());

            customerProfileInfoRepository.saveAndFlush(customerProfileInfoDBOSDB);

            // log.info("Customer Profile Info Total Storage - " +
            // customerProfileInfoRepository.count());
            log.info("Customer Profile Info - " + customerProfileInfoDBOSDB.toString());

            return customerProfileInfoDBOSDB;

        } catch (JSONException jsonException) {
            log.error(jsonException.toString());
        }
        return customerProfileInfoDBOSDB;

    }

    public List<String> getProfileInfoIB2BDB() throws ServiceException, SQLException {

        String url = SystemParam.getInstance().getIB2GConnectionString();
        String user = SystemParam.getInstance().getIB2GUsername();
        String password = SystemParam.getInstance().getIB2GPassword();
        List<String> list = new ArrayList<>();

        Connection connection = null;
        try {
            // Load class into memory
            Class.forName(SystemParam.getInstance().getDB2className());
            // Establish connection
            connection = DriverManager.getConnection(url, user, password);
        } catch (ClassNotFoundException e) {
            log.error("JDBC Class Not Found Error :: " + e.getMessage());
        } catch (SQLException e) {
            log.error("JDBC Connection Issue  :: " + e.getMessage());
        } finally {

            if (connection != null) {
                try {
                    // System.out.println("Connected successfully.");
                    log.info(messageSource.getMessage("common.request.success", null, null));

                    String sql = "SELECT * FROM \"lead\"";
                    Statement selectStmt = connection.createStatement();
                    ResultSet selectResults = selectStmt.executeQuery(sql);
                    // ResultSetMetaData metaData = selectResults.getMetaData();
                    // int columns = metaData.getColumnCount();
                    while (selectResults.next()) {
                        Long resultLEADID = selectResults.getLong(1);
                        // String resultEMAIL = selectResults.getString("EMAIL");

                        System.out.println(" " + resultLEADID.toString());
                        list.add(" " + resultLEADID.toString());
                    }

                } catch (SQLException e) {
                    System.out.println(e.toString());
                }
                try {
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }

        return list;
    }

    public EformUuidResponse getEformUUIDWithDeviceID(String deviceID) throws ServiceException {
        try {
            EformUuidResponse eformUUID = new EformUuidResponse();

            List<CustomerProfileInfo> customerInfo = customerProfileInfoRepository.findByDeviceUuid(deviceID) != null
                    ? customerProfileInfoRepository.findByDeviceUuid(deviceID)
                    : new ArrayList<CustomerProfileInfo>();

            if (customerInfo.size() > 0) {
                log.info("customer info in getEformUUIDWithDeviceID services : " + customerInfo.get(0).toString());
                eformUUID.setEformUuid(customerInfo.get(customerInfo.size() - 1).getEformUuid());
            }

            log.info("return eform in getEformUUIDWithDeviceID services : " + eformUUID.toString());
            return eformUUID;

        } catch (Exception ex) {

            log.error("The Error set the Adjust Info :: {}", ex.toString());
            throw new ServiceException(ApiResponse.HTTP_RESPONSE_INTERNAL_SERVER_ERROR,
                    "adjust.info.error.storeAdjustInfo");

        }

    }

    public EformUuidResponse getEformUUIDWithMobileNum(String MobileNum) throws ServiceException {

        try {
            EformUuidResponse eformUUID = new EformUuidResponse();

            List<CustomerProfileInfo> customerInfo = customerProfileInfoRepository
                    .findByMobileNumber(MobileNum) != null
                            ? customerProfileInfoRepository.findByMobileNumber(MobileNum)
                            : new ArrayList<CustomerProfileInfo>();

            if (customerInfo.size() > 0) {
                log.info("customer info in getEformUUIDWithMobileNum services : " + customerInfo.get(0).toString());
                eformUUID.setEformUuid(customerInfo.get(0).getEformUuid());
            }
            log.info("return eform in getEformUUIDWithMobileNum services : " + eformUUID.toString());
            return eformUUID;
        } catch (Exception ex) {

            log.error("The Error set the Adjust Info :: {}", ex.toString());
            throw new ServiceException(ApiResponse.HTTP_RESPONSE_INTERNAL_SERVER_ERROR,
                    "adjust.info.error.storeAdjustInfo");
        }

    }

    public CustomerProfileResponse getCustomerProfileInfo(String eformUuid) throws ServiceException {
        CustomerProfileInfo customerProfileInfo = new CustomerProfileInfo();
        CustomerProfileResponse customerResponse = new CustomerProfileResponse();

        customerProfileInfo = getProfileInfoDBOSDB(eformUuid, false);

        customerResponse.setDevicePlatform(customerProfileInfo.getDevicePlatform());
        customerResponse.setDeviceUuid(customerProfileInfo.getDeviceUuid());
        customerResponse.setTo(customerProfileInfo.getMobileNumber());

        log.info("customerProfileInfo.getIdentityInfoFullname() : " + customerProfileInfo.getIdentityInfoFullname());
        log.info("customerProfileInfo.getAlternateName() : " + customerProfileInfo.getAlternateName());

        customerResponse.setFullName(customerProfileInfo.getIdentityInfoFullname() != null
                && customerProfileInfo.getIdentityInfoFullname() != ""
                        ? customerProfileInfo.getIdentityInfoFullname()
                        : customerProfileInfo.getAlternateName());
        customerResponse.setIdNo(customerProfileInfo.getMobileIdNo());

        customerResponse.setEmail(customerProfileInfo.getContactDetailsEmail());
        customerResponse.setMobileStatus(customerProfileInfo.getMobileStatus());

        customerResponse.setApplicationDateTime(customerProfileInfo.getApplicationCreationTime());
        customerResponse.setApplicationLastUpdate(customerProfileInfo.getApplicationLastUpdate());
        customerResponse.setApplicationStageCode(customerProfileInfo.getApplicationStageCode());
        customerResponse.setInvitationCode(customerProfileInfo.getInvitationCode());

        return customerResponse;
    }

    public List<CustomerProfile> getProfileInfoDBOSDB(String schema, String table, String eform_uuid)
            throws ServiceException {
        log.info("//---getProfileInfoDBOSDB for referral---//");

        List<CustomerProfile> list = new ArrayList<CustomerProfile>();
        List<Object[]> selectResults = new ArrayList<>();

        try {
            log.info(messageSource.getMessage("common.request.success", null, null));
            if (table.equalsIgnoreCase(VCC_TABLE_VIEW)) {
                log.info("getProfileInfoDBOSDB - findByUuidViaVccDb for request : {} | {}", table, eform_uuid);
                selectResults = profileInfoRepository.findByUuidViaVccDb(VCC_DB_SCHEMA, table, eform_uuid);
            } else {
                log.info("getProfileInfoDBOSDB - findByUuid for request : {} | {}", table, eform_uuid);
                selectResults = profileInfoRepository.findByUuid(schema, table, eform_uuid);
            }

            log.info("getProfileInfoDBOSDB for referral - done execute query : {}", selectResults);

            if (table.equalsIgnoreCase(VCC_TABLE_VIEW)) {
                for (Object[] row : selectResults) {
                    CustomerProfile customerProfileInfo = new CustomerProfile();
                    customerProfileInfo.setUuid((String) row[0]); // UUID
                    String mobile = (String) row[1]; // MOBILE_NO
                    if (mobile.startsWith("+")) {
                        mobile = mobile.substring(1);
                    }
                    customerProfileInfo.setMobile(mobile);
                    customerProfileInfo.setFullName((String) row[2]); // CUSTOMER_NAME
                    customerProfileInfo.setCompletedOn((Date) row[3]); // SUBMITTED_ON
                    customerProfileInfo.setPromoCode((String) row[4]); // PROMO_CODE
                    customerProfileInfo.setDeviceUuid((String) row[5]); // DEVICE_UUID
                    customerProfileInfo.setDevicePlatform((String) row[6]); // DEVICE_PLATFORM
                    customerProfileInfo.setIdNo(null);
                    customerProfileInfo.setStatusCode("OK");
                    customerProfileInfo.setPdpaFlag("true");
                    list.add(customerProfileInfo);
                }
            } else {
                for (Object[] row : selectResults) {
                    CustomerProfile customerProfileInfo = new CustomerProfile();
                    customerProfileInfo.setUuid((String) row[0]); // UUID
                    customerProfileInfo.setIdNo((String) row[1]); // ID_NO
                    customerProfileInfo.setFullName((String) row[2]); // FULL_NAME
                    customerProfileInfo.setPromoCode((String) row[3]); // PROMO_CODE
                    String mobile = (String) row[4]; // MOBILE
                    if (mobile.startsWith("+")) {
                        mobile = mobile.substring(1);
                    }
                    customerProfileInfo.setMobile(mobile);
                    customerProfileInfo.setDeviceUuid((String) row[5]); // DEVICE_UUID
                    customerProfileInfo.setDevicePlatform((String) row[6]); // DEVICE_PLATFORM
                    customerProfileInfo.setCompletedOn((Date) row[7]); // COMPLETED_ON
                    customerProfileInfo.setStatusCode((String) row[8]); // STATUS_CODE
                    customerProfileInfo.setPdpaFlag((String) row[9]); // PDPA_FLAG

                    list.add(customerProfileInfo);
                }
            }

        } catch (Exception ex) {
            log.error("getProfileInfoDBOSDB - Exception : " + ex.toString());
        }
        return list;

    }

    public List<CustomerProfile> getProfileInfoByDeviceDBOSDB(String schema, String table, String deviceID)
            throws ServiceException {
        log.info("//---getProfileInfoDBOSDB for referral---//");

        List<CustomerProfile> list = new ArrayList<CustomerProfile>();
        List<Object[]> selectResults = new ArrayList<>();

        try {
            log.info(messageSource.getMessage("common.request.success", null, null));
            if (table.equalsIgnoreCase(VCC_TABLE_VIEW)) {
                log.info("getProfileInfoByDeviceDBOSDB - findByDeviceIDViaVccDb for request : {} | {}", table, deviceID);
                selectResults = profileInfoRepository.findByDeviceIDViaVccDb(VCC_DB_SCHEMA, table, deviceID);
            } else {
                log.info("getProfileInfoByDeviceDBOSDB - profileInfoRepository for request : {} | {}", table, deviceID);
                selectResults = profileInfoRepository.findByDeviceID(schema, table, deviceID);
            }

            log.info("getProfileInfoDBOSDB for referral - done execute query : {}", selectResults);
            if (table.equalsIgnoreCase(VCC_TABLE_VIEW)) {
                for (Object[] row : selectResults) {
                    CustomerProfile customerProfileInfo = new CustomerProfile();
                    customerProfileInfo.setUuid((String) row[0]); // UUID
                    String mobile = (String) row[1]; // MOBILE_NO
                    if (mobile.startsWith("+")) {
                        mobile = mobile.substring(1);
                    }
                    customerProfileInfo.setMobile(mobile);
                    customerProfileInfo.setFullName((String) row[2]); // CUSTOMER_NAME
                    customerProfileInfo.setPromoCode((String) row[3]); // PROMO_CODE
                    customerProfileInfo.setDeviceUuid((String) row[4]); // DEVICE_UUID
                    customerProfileInfo.setDevicePlatform((String) row[5]); // DEVICE_PLATFORM
                    customerProfileInfo.setCompletedOn((Date) row[6]); // SUBMITTED_ON
                    customerProfileInfo.setIdNo(null);
                    customerProfileInfo.setStatusCode("OK");
                    customerProfileInfo.setPdpaFlag("true");
                    list.add(customerProfileInfo);
                }
            } else {
                for (Object[] row : selectResults) {
                    CustomerProfile customerProfileInfo = new CustomerProfile();
                    customerProfileInfo.setUuid((String) row[0]); // UUID
                    customerProfileInfo.setIdNo((String) row[1]); // ID_NO
                    customerProfileInfo.setFullName((String) row[2]); // FULL_NAME
                    customerProfileInfo.setPromoCode((String) row[3]); // PROMO_CODE
                    String mobile = (String) row[4]; // MOBILE
                    if (mobile.startsWith("+")) {
                        mobile = mobile.substring(1);
                    }
                    customerProfileInfo.setMobile(mobile);
                    customerProfileInfo.setDeviceUuid((String) row[5]); // DEVICE_UUID
                    customerProfileInfo.setDevicePlatform((String) row[6]); // DEVICE_PLATFORM
                    customerProfileInfo.setCompletedOn((Date) row[7]); // COMPLETED_ON
                    customerProfileInfo.setStatusCode((String) row[8]); // STATUS_CODE
                    customerProfileInfo.setPdpaFlag((String) row[9]); // PDPA_FLAG

                    list.add(customerProfileInfo);
                }
            }

        } catch (Exception ex) {
            log.error("getProfileInfoDBOSDB - Exception : " + ex.toString());
        }
        return list;

    }


}
