package com.alliance.diceintegration.service;

import com.alliance.diceintegration.constant.ApiResponse;
import com.alliance.diceintegration.constant.DataField.Status;
import com.alliance.diceintegration.constant.MustacheItem;
import com.alliance.diceintegration.exception.ServiceException;
import com.alliance.diceintegration.model.*;
import com.alliance.diceintegration.repository.*;
import com.alliance.diceintegration.request.CampaignJourneyRequest;
import com.alliance.diceintegration.request.ProcessCampaignRequest;
import com.alliance.diceintegration.request.ProfileRequest;
import com.alliance.diceintegration.request.ReferralPushNotisRequest;
import com.alliance.diceintegration.response.ButtonResponse;
import com.alliance.diceintegration.response.CacheCustomerProfileSoleCCResponse;
import com.alliance.diceintegration.response.CampaignCheckResponse;
import com.alliance.diceintegration.response.MessageContentResponse;
import com.alliance.diceintegration.response.PandaiBroadcastResponse;
import com.alliance.diceintegration.response.ProcessCampaignResponse;
import com.alliance.diceintegration.utility.AESEncyptionUtil;
import com.alliance.diceintegration.utility.DateUtil;
import com.alliance.diceintegration.utility.MathUtil;
import com.alliance.diceintegration.utility.MustacheUtil;
import com.alliance.diceintegration.utility.StringUtil;
import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.StringReader;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.*;

@Service
@Slf4j
public class InboundService {
    @Autowired
    private MessageTemplateRepository messageTemplateRepository;
    @Autowired
    private ButtonTemplateRepository buttonTemplateRepository;
    @Autowired
    private CampaignJourneyRepository campaignJourneyRepository;
    @Autowired
    private ButtonListRepository buttonListRepository;
    @Autowired
    private MessageSource messageSource;
    @Autowired
    private CallbackService callbackService;
    @Autowired
    private ProfileInfoService profileInfoService;
    @Autowired
    private MessagingService messagingService;
    @Autowired
    private CacheService cacheService;
    @Autowired
    private ReferralCodeRepository referralCodeRepository;
    @Autowired
    private RewardTemplateRepository rewardTemplateRepository;
    @Autowired
    private EngagementHistoryRepository engagementHistoryRepository;
    @Autowired
    private ReferralHistoryRepository referralHistoryRepository;

    @Value("${dice.encryption.secret.key}")
    private String SECRET_KEY;

    @Value("${dice.encryption.salt}")
    private String SALT;

    @Value("${customer.profile.schema}")
    private String CUSTOMER_PROFILE_SCHEMA;

    @Value("${pandaiBroadcast.messageType}")
    private String PANDAI_BROADCAST_MESSAGE_TYPE;

    @Value("${pandaiBroadcast.flowName}")
    private String PANDAI_BROADCAST_FLOW_NAME;

    @Value("${pandaiBroadcast.language}")
    private String PANDAI_BROADCAST_LANGUAGE;

    @Value("${notification.type}")
    private String NOTIFICATION_TYPE;

    @Value("${communication.channel.inapp}")
    private String COMMUNICATION_CHANNLE_INAPP;

    public MessageContentResponse getMessageContent(Integer messageTemplateId, UUID engagementSentId,
            String uuidType, String uuid)
            throws NoSuchMessageException, ServiceException {
        if (messageTemplateId == null && engagementSentId == null) {
            throw new ServiceException(ApiResponse.HTTP_RESPONSE_BAD_REQUEST,
                    messageSource.getMessage("messageTemplate.both.request.exist", null, null));
        } else {
            if (messageTemplateId != null) {

                MessageTemplate messageTemplate = messageTemplateRepository.getByMessageTemplateId(messageTemplateId);

                if (messageTemplate == null) {
                    log.error("getMessageContent - Message Template Not Found. messageTemplateId : {}",
                            messageTemplateId);
                    throw new ServiceException(ApiResponse.HTTP_RESPONSE_BAD_REQUEST,
                            messageSource.getMessage("messageTemplate.notFound", null, null));
                }

                List<ButtonResponse> list = new ArrayList<ButtonResponse>();
                MessageContentResponse response = new MessageContentResponse();
                if (messageTemplate.getButtonIds() != null && !messageTemplate.getButtonIds().isEmpty()) {
                    messageTemplate.getButtonIds().forEach(i -> {
                        ButtonResponse btnResponse = new ButtonResponse();

                        ButtonList buttonList = buttonListRepository.getByButtonTemplateId(Integer.parseInt(i));
                        if (buttonList != null && buttonList.getButtonTemplateId() != null) {
                            ButtonTemplate buttonTemplate = buttonTemplateRepository
                                    .getByButtonTemplateId(buttonList.getButtonTemplateId());
                            if (buttonTemplate != null) {
                                btnResponse.setBtnName(buttonTemplate.getButtonTemplateName());
                                btnResponse.setBtnType(buttonTemplate.getButtonType());
                            }
                            // btnResponse.setContent(buttonTemplate.getContent());
                            try {
                                btnResponse.setContent(fillContentByMustacle(
                                        buttonList.getButtonInAppContentTemplateId() != null
                                                ? buttonList.getButtonInAppContentTemplateId()
                                                : null,
                                        uuidType, uuid));

                            } catch (ServiceException ex) {
                                log.error("fillContentByMustacle - ServiceException :: {}", ex.toString());
                            } catch (Exception e) {
                                log.error("fillContentByMustacle - Exception: {}", e);
                            }

                            list.add(btnResponse);
                        }
                    });

                }
                response.setButtons(list);
                response.setContent(messageTemplate.getContent());
                if (messageTemplate.getTitle() != null && !messageTemplate.getTitle().isEmpty()) {
                    response.setTitle(messageTemplate.getTitle());
                }

                return response;

            } else if (engagementSentId != null) {
                EngagementHistory engagementSent = engagementHistoryRepository.findByEngagementSentId(engagementSentId);
                if (engagementSent != null) {
                    List<ButtonResponse> list = new ArrayList<ButtonResponse>();
                    MessageContentResponse response = new MessageContentResponse();
                    MessageTemplate messageTemplate = messageTemplateRepository
                            .getByMessageTemplateId(engagementSent.getMessageTemplateId());

                    if (messageTemplate.getButtonIds() != null && !messageTemplate.getButtonIds().isEmpty()) {
                        messageTemplate.getButtonIds().forEach(i -> {
                            ButtonResponse btnResponse = new ButtonResponse();

                            ButtonList buttonList = buttonListRepository.getByButtonTemplateId(Integer.parseInt(i));
                            if (buttonList != null && buttonList.getButtonTemplateId() != null) {
                                ButtonTemplate buttonTemplate = buttonTemplateRepository
                                        .getByButtonTemplateId(buttonList.getButtonTemplateId());
                                if (buttonTemplate != null) {
                                    btnResponse.setBtnName(buttonTemplate.getButtonTemplateName());
                                    btnResponse.setBtnType(buttonTemplate.getButtonType());
                                }
                                // btnResponse.setContent(buttonTemplate.getContent());
                                try {
                                    btnResponse.setContent(fillContentByMustacle(
                                            buttonList.getButtonInAppContentTemplateId() != null
                                                    ? buttonList.getButtonInAppContentTemplateId()
                                                    : null,
                                            uuidType, uuid));

                                } catch (ServiceException ex) {
                                    log.error("fillContentByMustacle - ServiceException :: {}", ex.toString());
                                } catch (Exception e) {
                                    log.error("fillContentByMustacle - Exception: {}", e);
                                }

                                list.add(btnResponse);
                            }
                        });

                    }
                    response.setButtons(list);
                    response.setContent(engagementSent.getMessageContent());
                    if (messageTemplate.getTitle() != null && !messageTemplate.getTitle().isEmpty()) {
                        response.setTitle(messageTemplate.getTitle());
                    }

                    return response;
                } else {
                    log.error("getMessageContent - Message Template Not Found. engagementSentId: {}", engagementSentId);
                    throw new ServiceException(ApiResponse.HTTP_RESPONSE_BAD_REQUEST,
                            messageSource.getMessage("messageTemplate.notFound", null, null));
                }
            } else {
                log.error("getMessageContent - Message Template Not Found. messageTemplateId: {}",
                        messageTemplateId);
                throw new ServiceException(ApiResponse.HTTP_RESPONSE_BAD_REQUEST,
                        messageSource.getMessage("messageTemplate.notFound", null, null));

            }
        }
    }

    private String fillContentByMustacle(Integer messageTemplateId, String uuidType, String uuid)
            throws Exception {
        MessageTemplate messageTemplate = new MessageTemplate();
        if (messageTemplateId != null) {
            messageTemplate = messageTemplateRepository.getByMessageTemplateId(messageTemplateId);
        } else {
            messageTemplate = null;
            log.info("fillContentByMustacle - no button inApp content");
        }

        MustacheItem items = new MustacheItem();
        if (messageTemplate != null) {
            ReferralCode code = callbackService.getReferralCodeByUuid(uuidType, uuid);

            if (code != null) {

                if (code.getCampaignId() != null) {
                    CampaignCheckResponse campaignCheck = callbackService
                            .checkCampaign(code.getCampaignId());

                    log.info("fillContentByMustacle - campaignCheck : {}", campaignCheck);
                    if (!campaignCheck.getIsExpired() || !campaignCheck.getIsDisable()) {
                        items.setReferralCode(code.getCodeValue());
                        log.info("fillContentByMustacle - setReferralCode : {}", items.getReferralCode());
                    } else {
                        items.setReferralCode("");
                    }

                }
                items.setReward("30");
            }

            MustacheFactory mf = new DefaultMustacheFactory();
            Mustache m = mf.compile(new StringReader(messageTemplate.getContent()), "");

            log.info(items.toString());
            log.info(messageTemplate.getContent().toString());

            StringWriter writer = new StringWriter();

            try {
                m.execute(writer, items).flush();
            } catch (Exception e) {
                e.printStackTrace();
            }

            log.info("fillContentByMustacle - done");
            return writer.toString();
        }
        log.info("fillContentByMustacle - done");
        return null;
    }

    public ProcessCampaignResponse createCampaignJourney(CampaignJourneyRequest campaignJourneyRequest)
            throws ServiceException, Exception {

        CampaignJourney campaignJourney = new CampaignJourney();
        campaignJourney.setChannel(campaignJourneyRequest.getChannel());
        campaignJourney.setReferenceId(campaignJourneyRequest.getReferenceId());
        campaignJourney.setStatusCode(campaignJourneyRequest.getStatusCode());
        campaignJourney.setTableView(campaignJourneyRequest.getTableView());
        campaignJourney.setTransactionDesc(campaignJourneyRequest.getTransactionDesc());
        campaignJourney.setTransactionType(campaignJourneyRequest.getTransactionType());
        if (campaignJourneyRequest.getCampaignId() != null && !campaignJourneyRequest.getCampaignId().isEmpty()) {
            campaignJourney.setCampaignId(campaignJourneyRequest.getCampaignId());
        }

        campaignJourneyRepository.save(campaignJourney);

        CustomerProfile refereeProfile = getCustomerProfile(campaignJourneyRequest.getTableView(),
                campaignJourneyRequest.getReferenceId());
        ProcessCampaignRequest request = new ProcessCampaignRequest();

        if (refereeProfile != null) {
            CustomerProfile referrerProfile = new CustomerProfile();
            ReferralCode existingCode = new ReferralCode();
            if (refereeProfile.getPromoCode() != null && !refereeProfile.getPromoCode().isEmpty()) {

                existingCode = callbackService.getReferralCodeByCodeValue(refereeProfile.getPromoCode(),
                        Status.ACTIVE);

                if (existingCode != null) {

                    if (existingCode.getEformUuid() != null && !existingCode.getEformUuid().isEmpty()) {

                        CampaignJourney journey = getCampaignJourney(existingCode.getCampaignJourneyId());
                        log.info("CampaignJourneyId: {}", existingCode.getCampaignJourneyId());

                        if (journey != null) {
                            referrerProfile = getCustomerProfile(campaignJourneyRequest.getTableView(),
                                    existingCode.getEformUuid());
                        }

                    } else if (existingCode.getCifNo() != null && !existingCode.getCifNo().isEmpty()) {
                        ProfileRequest profileRequest = new ProfileRequest();
                        profileRequest.setIdType("cif_no");
                        profileRequest.setIdValue(existingCode.getCifNo());
                        profileRequest.setTableName("EVT_SOLE_CC_VIEW");
                        CacheCustomerProfileSoleCCResponse cacheCustomerProfileSoleCCResponse = cacheService.checkCustomerProfile(profileRequest);
                        referrerProfile.setMobile(cacheCustomerProfileSoleCCResponse.getMobile());
                        referrerProfile.setFullName(cacheCustomerProfileSoleCCResponse.getCustomerName()); 
                       
                    
                        referrerProfile.setDeviceUuid(cacheCustomerProfileSoleCCResponse.getMobileFirstPlatformId()); 
                        referrerProfile.setDevicePlatform(cacheCustomerProfileSoleCCResponse.getDevicePlatform()); 
                        referrerProfile.setIdNo(cacheCustomerProfileSoleCCResponse.getNewIcNo());
                      
                        // referrerProfile = callbackService.getCustomerProfileViaCif(existingCode.getCifNo());
                        request.setCampaignId(existingCode.getCampaignId());

                    } else {
                        log.error("createCampaignJourney - ReferrerProfile is null. uuid: {}",
                                campaignJourneyRequest.getReferenceId());
                        throw new ServiceException(ApiResponse.HTTP_RESPONSE_BAD_REQUEST, messageSource.getMessage(
                                "customer.profile.notFound", new Object[] { campaignJourneyRequest.getReferenceId() },
                                null));
                    }
                } else {

                }

            }

            request.setRefereeProfile(refereeProfile);
            request.setReferrerProfile(referrerProfile);
            request.setCampaignJourneyId(campaignJourney.getId().toString());
            request.setChannel(campaignJourneyRequest.getChannel());
            request.setReferenceId(campaignJourneyRequest.getReferenceId());
            request.setStatusCode(campaignJourneyRequest.getStatusCode());
            request.setTableView(campaignJourneyRequest.getTableView());
            request.setTransactionDesc(campaignJourneyRequest.getTransactionDesc());
            request.setTransactionType(campaignJourneyRequest.getTransactionType());
            request.setIsProcess(campaignJourneyRequest.getIsProcess());
            request.setCreatedOn(new Date());
            ProcessCampaignResponse processCampaignResponse = callbackService.processCampaign(request);
            log.info("processCampaignRequest: {}", request);
            log.info("processCampaignResponse: {}", processCampaignResponse);

            if (processCampaignResponse.getCampaignId() == null) {
                if (request.getCampaignId() == null) {
                    log.info(
                            "createCampaignJourney - the promo code doesn't matach any campaign, result in campaign is null");
                    throw new ServiceException(ApiResponse.HTTP_RESPONSE_OK, messageSource.getMessage(
                            "campaign.not.match", null, null));
                } else {
                    log.info("createCampaignJourney - Campaign is expired/disabled.");
                    throw new ServiceException(ApiResponse.HTTP_RESPONSE_OK, messageSource.getMessage(
                            "campaign.expired.disabled", null, null));
                }
            } else {
                if (processCampaignResponse.getIsValid()) {
                    referralProcess(request, processCampaignResponse, existingCode);
                }

                return processCampaignResponse;
            }

        } else {
            log.error("createCampaignJourney - CustomerProfile is null. referenceId: {}",
                    campaignJourneyRequest.getReferenceId());
            throw new ServiceException(ApiResponse.HTTP_RESPONSE_BAD_REQUEST, messageSource.getMessage(
                    "customer.profile.notFound", new Object[] { campaignJourneyRequest.getReferenceId() }, null));
        }
    }

    public void updateCampaignJourney(String uuid, CampaignJourneyRequest campaignJourneyRequest)
            throws ServiceException {
        CampaignJourney campaignJourney = campaignJourneyRepository.getById(UUID.fromString(uuid));

        if (campaignJourney != null) {
            campaignJourney.setChannel(campaignJourneyRequest.getChannel() != null ? campaignJourneyRequest.getChannel()
                    : campaignJourney.getChannel());
            campaignJourney.setReferenceId(
                    campaignJourneyRequest.getReferenceId() != null ? campaignJourneyRequest.getReferenceId()
                            : campaignJourney.getReferenceId());
            campaignJourney.setStatusCode(
                    campaignJourneyRequest.getStatusCode() != null ? campaignJourneyRequest.getStatusCode()
                            : campaignJourney.getStatusCode());
            campaignJourney
                    .setTableView(campaignJourneyRequest.getTableView() != null ? campaignJourneyRequest.getTableView()
                            : campaignJourney.getTableView());
            campaignJourney.setTransactionDesc(
                    campaignJourneyRequest.getTransactionDesc() != null ? campaignJourneyRequest.getTransactionDesc()
                            : campaignJourney.getTransactionDesc());
            campaignJourney.setTransactionType(
                    campaignJourneyRequest.getTransactionType() != null ? campaignJourneyRequest.getTransactionType()
                            : campaignJourney.getTransactionType());
            campaignJourney
                    .setIsProcess(campaignJourneyRequest.getIsProcess() != false ? campaignJourneyRequest.getIsProcess()
                            : campaignJourney.getIsProcess());
            campaignJourney.setStatus(campaignJourneyRequest.getStatus() != null ? campaignJourneyRequest.getStatus()
                    : campaignJourney.getStatus());
            campaignJourneyRepository.save(campaignJourney);
        }
    }

    public CustomerProfile getCustomerProfile(String tableView, String referenceId) throws ServiceException {
        List<CustomerProfile> customerProfileInfos = profileInfoService.getProfileInfoDBOSDB(CUSTOMER_PROFILE_SCHEMA,
                tableView, referenceId);
        if (customerProfileInfos.size() == 1) {
            return customerProfileInfos.get(0);
        }
        return null;
    }

    public CampaignJourney getCampaignJourney(String uuid) throws ServiceException {
        CampaignJourney campaignJourney = campaignJourneyRepository.getById(UUID.fromString(uuid));
        if (campaignJourney == null) {
            log.info("getCampaignJourney - CampaignJourney not found. id: {}", uuid);
            throw new ServiceException(ApiResponse.HTTP_RESPONSE_BAD_REQUEST,
                    messageSource.getMessage("common.operation.success", null, Locale.ENGLISH));

        }
        return campaignJourney;
    }

    public Map<String, Boolean> validateCode(String code) throws ServiceException {
        try {
            Boolean valid = referralCodeRepository.countByCodeValueAndStatus(code, Status.ACTIVE) > 0;
            Map<String, Boolean> map = new HashMap<String, Boolean>();

            if (valid) {
                map.put("valid", true);
            } else {
                // map.put("valid", false);
                map.put("valid", true);
            }
            return map;
        } catch (Exception ex) {
            log.error("validateReferralCode - Exception : {}", ex);
            throw new ServiceException(ApiResponse.HTTP_RESPONSE_INTERNAL_SERVER_ERROR, ex.toString());
        }
    }

    private void referralProcess(ProcessCampaignRequest processCampaignRequest,
            ProcessCampaignResponse processCampaignResponse, ReferralCode existingCode)
            throws ServiceException, Exception {

        Date now = new Date();
        String referralCode = null;
        Integer generateCodeAttempt = 10;
        Date leadInitiationDate = null;
        Campaign campaign = callbackService.getCampaignById(processCampaignResponse.getCampaignId());
        log.info("referralProcess - campaign: {}", campaign);

        if(!(processCampaignRequest.getTransactionType().equalsIgnoreCase("ABMBVCC-A"))){
        for (int i = 0; i < generateCodeAttempt; i++) {
                // Integer digit = MathUtil
                //         .getHighestAvailableDigitForAlphaNumeric(referralCodeRepository.countByStatus(Status.ACTIVE));
                Integer digit = (i > 3) ? 7 : 6; // Set digit to 7 if generateCodeAttempt > 3, otherwise 6
                referralCode = StringUtil.generateRandomString(digit, true, true);
                Integer countByCodeValueAndStatus = referralCodeRepository.countByCodeValueAndStatus(referralCode,
                        Status.ACTIVE) + referralCodeRepository.countByCodeValueAndStatus(referralCode, Status.DISABLE);
                if (countByCodeValueAndStatus == 0) {
                    log.info("referralProcess - generateCodeAttempt = {} , referralCode = {}", i, referralCode);
                    break;
                } else {
                    log.info("referralProcess - code exist, retry generateCodeAttempt = {}", i);
                    referralCode = null;
                }
            }
        }
        else {
            referralCode = null;
            log.info("referralProcess - TransactionType : {} | referralCode : {}", processCampaignRequest.getTransactionType(), referralCode);
        }

        if (referralCode != null) {
            try {
                // scenario : new saveplus account
                ReferralCode referralCodeToStore = new ReferralCode();
                log.info("referralProcess - referralCode is {}", referralCode);
                referralCodeToStore.setCampaignId(campaign.getCampaignId());
                referralCodeToStore.setCodeValue(referralCode);
                referralCodeToStore.setCampaignJourneyId(processCampaignRequest.getCampaignJourneyId());
                referralCodeToStore.setEformUuid(processCampaignRequest.getRefereeProfile().getUuid());
                log.info("referralProcess - referralCodeToStore : {}", referralCodeToStore);
                referralCodeRepository.save(referralCodeToStore);

                String platformChannel = "save_plus";
                String inviteCode;
                String inviteId;
                Boolean isMatchCode;
                if (existingCode != null) {
                    // check is the code belong to correct campaign related to be pass to pand.ai
                    if (existingCode.getCampaignId() == processCampaignResponse.getCampaignId()) {
                        isMatchCode = true;
                    } else {
                        isMatchCode = false;
                    }
                } else {
                    isMatchCode = false;
                }

                log.info("referralProcess - isMatchCode : {}", isMatchCode);

                if (isMatchCode == true && processCampaignRequest.getReferrerProfile().getUuid() != null
                        && !processCampaignRequest.getReferrerProfile().getUuid().isEmpty()) {
                    inviteCode = processCampaignRequest.getRefereeProfile().getPromoCode();
                    inviteId = processCampaignRequest.getReferrerProfile().getMobile();
                    log.info("referralProcess - referralCodeUsed : {} | referralCodeOwnerMobileNumber : {}", inviteCode,
                            inviteId);
                } else {
                    inviteCode = null;
                    inviteId = null;
                    log.info(
                            "referralProcess - Referrer Profile is empty or no valid whatsapp campaign promo code being use by customer : uuid = {}",
                            processCampaignRequest.getRefereeProfile().getUuid());
                }
                PandaiBroadcastResponse broadcastResponse = new PandaiBroadcastResponse();
                
                if (processCampaignRequest.getRefereeProfile().getPdpaFlag().equalsIgnoreCase("true")) {
                    broadcastResponse = messagingService.pandaiBroadcast(
                            processCampaignRequest.getRefereeProfile().getFullName(),
                            processCampaignRequest.getRefereeProfile().getMobile(), referralCode, null,
                            PANDAI_BROADCAST_MESSAGE_TYPE, processCampaignResponse.getPandaiBroadcastFlowName(),
                            PANDAI_BROADCAST_LANGUAGE, inviteCode, inviteId, platformChannel);

                    log.info("broadcastResponse -> {}", broadcastResponse);
                    if (broadcastResponse != null) {
                        for (int i = 0; i < broadcastResponse.getSuccessful_users().size(); i++) {
                            if (broadcastResponse.getSuccessful_users().get(i).getUser_id()
                                    .equals(processCampaignRequest.getRefereeProfile().getMobile())) {
                                leadInitiationDate = DateUtil.convertToDate("yyyy-MM-dd'T'HH:mm:ss.SSSXXX",
                                        broadcastResponse.getSuccessful_users().get(i).getCreated_at());
                            }
                        }
                    }

                    log.debug("ReferralProcess - refereePdpaFlag is {} , broadcastResponse: {}",
                            processCampaignRequest.getRefereeProfile().getPdpaFlag(), broadcastResponse);
                } else {
                    log.debug("ReferralProcess - refereePdpaFlag is {} , broadcastResponse : {}",
                            processCampaignRequest.getRefereeProfile().getPdpaFlag(), broadcastResponse);
                }

            } catch (Exception ex) {
                log.error("ReferralProcess - Exception: {} | {}", ex.getMessage(), ex);
                throw new ServiceException(ApiResponse.HTTP_RESPONSE_INTERNAL_SERVER_ERROR, ex.toString());

            }
        } else if (referralCode == null && processCampaignRequest.getTransactionType().equalsIgnoreCase("ABMBVCC-A") && !(processCampaignResponse.getPandaiBroadcastFlowNameReferral().equals(null))){
             try {
                // scenario : vcc referral campaign
                String platformChannel = "vcc";
                String inviteCode;
                String inviteId;
                Boolean isMatchCode;
                if (existingCode != null) {
                    // check is the code belong to correct campaign related to be pass to pand.ai
                    if (existingCode.getCampaignId() == processCampaignResponse.getCampaignId()) {
                        isMatchCode = true;
                    } else {
                        isMatchCode = false;
                    }
                } else {
                    isMatchCode = false;
                }

                log.info("referralProcess - isMatchCode : {}", isMatchCode);

                if (isMatchCode == true && processCampaignRequest.getReferrerProfile().getUuid() != null
                        && !processCampaignRequest.getReferrerProfile().getUuid().isEmpty()) {
                    inviteCode = processCampaignRequest.getRefereeProfile().getPromoCode();
                    inviteId = processCampaignRequest.getReferrerProfile().getMobile();
                    log.info("referralProcess - referralCodeUsed : {} | referralCodeOwnerMobileNumber : {}", inviteCode,
                            inviteId);
                } else {
                    inviteCode = null;
                    inviteId = null;
                    log.info(
                            "referralProcess - Referrer Profile is empty or no valid whatsapp campaign promo code being use by customer : uuid = {}",
                            processCampaignRequest.getRefereeProfile().getUuid());
                }
                PandaiBroadcastResponse broadcastResponse = new PandaiBroadcastResponse();
                if (processCampaignRequest.getRefereeProfile().getPdpaFlag().equalsIgnoreCase("true")) {
                    broadcastResponse = messagingService.pandaiBroadcast(
                            processCampaignRequest.getRefereeProfile().getFullName(),
                            processCampaignRequest.getRefereeProfile().getMobile(), null, null,
                            PANDAI_BROADCAST_MESSAGE_TYPE, processCampaignResponse.getPandaiBroadcastFlowNameReferral(),
                            PANDAI_BROADCAST_LANGUAGE, inviteCode, inviteId, platformChannel);

                    log.info("broadcastResponse -> {}", broadcastResponse);
                    if (broadcastResponse != null) {
                        for (int i = 0; i < broadcastResponse.getSuccessful_users().size(); i++) {
                            if (broadcastResponse.getSuccessful_users().get(i).getUser_id()
                                    .equals(processCampaignRequest.getRefereeProfile().getMobile())) {
                                leadInitiationDate = DateUtil.convertToDate("yyyy-MM-dd'T'HH:mm:ss.SSSXXX",
                                        broadcastResponse.getSuccessful_users().get(i).getCreated_at());
                            }
                        }
                    }

                    log.debug("ReferralProcess - refereePdpaFlag is {} , broadcastResponse: {}",
                            processCampaignRequest.getRefereeProfile().getPdpaFlag(), broadcastResponse);
                } else {
                    log.debug("ReferralProcess - refereePdpaFlag is {} , broadcastResponse : {}",
                            processCampaignRequest.getRefereeProfile().getPdpaFlag(), broadcastResponse);
                }

            } catch (Exception ex) {
                log.error("ReferralProcess - Exception: {} | {}", ex.getMessage(), ex);
                throw new ServiceException(ApiResponse.HTTP_RESPONSE_INTERNAL_SERVER_ERROR, ex.toString());

            }

        }

        // scenario :referrer promo code use
        if (processCampaignRequest.getReferrerProfile() != null
                && processCampaignRequest.getReferrerProfile().getDevicePlatform() != null
                && processCampaignRequest.getReferrerProfile().getDeviceUuid() != null) {

            log.info("referrerProfile is {}", processCampaignRequest.getReferrerProfile());
            Campaign respectiveCampaign = callbackService.getCampaignById(existingCode.getCampaignId());


            MessageTemplate messageTemplate = messageTemplateRepository.getByMessageTemplateId(
                    Integer.parseInt(respectiveCampaign.getCampaignProperties().get("inAppPushMessageTemplateId")));
            MessageTemplate shareContentMessageTemplate = messageTemplateRepository.getByMessageTemplateId(
                    Integer.parseInt(respectiveCampaign.getCampaignProperties().get("pushNotificationTemplateId")));

            RewardTemplate reward = rewardTemplateRepository.getByCampaignId(respectiveCampaign.getCampaignId());

            log.info("messageTemplate --> {}", messageTemplate);
            log.info("shareContentMessageTemplate --> {}", shareContentMessageTemplate);
            log.info("reward --> {}", reward);

            String content;
            String shareContent;

            MustacheItem mustacheItem = new MustacheItem();
            BigDecimal rewardAmount = reward.getRewardAmount();
            DecimalFormat decimalFormat = new DecimalFormat("#,##0.##");
            decimalFormat.setDecimalSeparatorAlwaysShown(false);
            String formattedNumber = decimalFormat.format(rewardAmount);
            mustacheItem.setReward(formattedNumber + " " + reward.getRewardType());

            String sendPush = campaign.getCampaignProperties().get("sendPushNotification");

            log.info("sendPush --> {}", sendPush);

            if (sendPush != null && sendPush.equalsIgnoreCase("true")) {
                try {
                    String refereeName = processCampaignRequest.getRefereeProfile().getFullName();
                    mustacheItem.setRefereeName(
                            refereeName.substring(0, refereeName.length() - 3).replaceAll(".", "*")
                                    + refereeName.substring(refereeName.length() - 3, refereeName.length()));
                    mustacheItem.setTitle(messageTemplate.getTitle());
                    content = MustacheUtil.fillContentByMustache(messageTemplate.getContent(), mustacheItem);
                    shareContent = MustacheUtil.fillContentByMustache(shareContentMessageTemplate.getContent(),
                            mustacheItem);

                    EngagementHistory engagementSentHistory = new EngagementHistory();
                    engagementSentHistory
                            .setApplicationSessionID(processCampaignRequest.getReferrerProfile().getUuid());
                    engagementSentHistory.setCommunicationChannel(COMMUNICATION_CHANNLE_INAPP);
                    engagementSentHistory.setLanguage(PANDAI_BROADCAST_LANGUAGE);
                    engagementSentHistory.setMessageContent(content);
                    engagementSentHistory.setMessageRecipient(null);
                    engagementSentHistory.setMessageTemplateId(messageTemplate.getMessageTemplateId());
                    engagementSentHistory.setSentDateTime(new Date());
                    engagementSentHistory.setCampaignId(campaign.getCampaignId());
                    engagementHistoryRepository.save(engagementSentHistory);

                    ReferralPushNotisRequest request = new ReferralPushNotisRequest();
                    request.setApplicationSessionId(processCampaignRequest.getReferrerProfile().getUuid());
                    request.setContentId(
                            engagementSentHistory != null ? engagementSentHistory.getEngagementHistoryLogId() : null);
                    request.setDeviceId(processCampaignRequest.getReferrerProfile().getDeviceUuid());
                    request.setDevicePlatform(processCampaignRequest.getReferrerProfile().getDevicePlatform());
                    request.setTitle(messageTemplate.getTitle());
                    request.setMessage(shareContent);
                    request.setNotificationType(NOTIFICATION_TYPE);
                    log.info("ReferralProcess - pushContent: {}", shareContent);

                    messagingService.pushNotification(request);
                } catch (ServiceException ex) {
                    log.error("ReferralProcess - ServiceException: {}", ex);

                }
            }

            ReferralHistory referralHistory = new ReferralHistory();
            Integer referralHistoryLogId = (referralHistoryRepository.getMaxReferralHistoryLogId() != null)
                    ? referralHistoryRepository.getMaxReferralHistoryLogId() + 1
                    : 1;
            referralHistory.setReferralHistoryLogId(referralHistoryLogId);
            referralHistory.setCreatedOn(now);
            referralHistory.setUpdatedOn(now);
            referralHistory.setCampaignId(existingCode.getCampaignId());

            referralHistory.setReferrerDeviceUuid(AESEncyptionUtil
                    .encrypt(processCampaignRequest.getReferrerProfile().getDeviceUuid().toString(), SECRET_KEY, SALT));
            referralHistory.setReferrerAccountNo(processCampaignRequest.getReferrerProfile().getUuid() == null ? "null"
                    : AESEncyptionUtil
                            .encrypt(processCampaignRequest.getReferrerProfile().getUuid().toString(), SECRET_KEY,
                                    SALT));
            referralHistory.setReferrerAccountOpeningDate(DateUtil.convertToDate("dd/MM/yyyy hh:mm a",
                    processCampaignRequest.getReferrerProfile().getCompletedOn()));
            referralHistory.setReferrerMobileNo(AESEncyptionUtil
                    .encrypt(processCampaignRequest.getReferrerProfile().getMobile().toString(), SECRET_KEY, SALT));
            referralHistory.setReferrerName(AESEncyptionUtil
                    .encrypt(processCampaignRequest.getReferrerProfile().getFullName().toString(), SECRET_KEY, SALT));
            referralHistory.setReferrerNric(AESEncyptionUtil
                    .encrypt(processCampaignRequest.getReferrerProfile().getIdNo().toString(), SECRET_KEY, SALT));

            referralHistory.setReferreeDeviceUuid(AESEncyptionUtil
                    .encrypt(processCampaignRequest.getRefereeProfile().getDeviceUuid().toString(), SECRET_KEY, SALT));
            referralHistory.setReferreeAccountNo(AESEncyptionUtil
                    .encrypt(processCampaignRequest.getRefereeProfile().getUuid().toString(), SECRET_KEY, SALT));
            referralHistory.setReferreeAccountOpeningDate(DateUtil.convertToDate("dd/MM/yyyy hh:mm a",
                    processCampaignRequest.getRefereeProfile().getCompletedOn()));
            if (leadInitiationDate != null) {
                referralHistory.setReferreeInitiationDate(leadInitiationDate);
            } else {
                log.warn("ReferralProcess - leadInitiationDate is null and set to current datetime");
                referralHistory.setReferreeInitiationDate(getCurrentDate());
            }
            referralHistory.setReferreeMobileNo(AESEncyptionUtil
                    .encrypt(processCampaignRequest.getRefereeProfile().getMobile().toString(), SECRET_KEY, SALT));
            referralHistory.setReferreeName(AESEncyptionUtil
                    .encrypt(processCampaignRequest.getRefereeProfile().getFullName().toString(), SECRET_KEY, SALT));
            referralHistory.setReferreeNric(AESEncyptionUtil
                    .encrypt(processCampaignRequest.getRefereeProfile().getIdNo().toString(), SECRET_KEY, SALT));
            referralHistory.setReferralCodeValue(processCampaignRequest.getRefereeProfile().getPromoCode());

            referralHistoryRepository.save(referralHistory);
        }
        CampaignJourneyRequest request = new CampaignJourneyRequest();
        request.setIsProcess(true);
        updateCampaignJourney(processCampaignRequest.getCampaignJourneyId(), request);
        log.info("referralProcess - Completed.");
    }

    @Async
    public Date getCurrentDate() throws Exception {
        Date currentDate = new Date();
        long currentTimeInMillis = currentDate.getTime();
        long eightHoursInMillis = 8 * 60 * 60 * 1000; // 8 hours in milliseconds
        long malaysiaTimeInMillis = currentTimeInMillis + eightHoursInMillis;
        Date malaysiaTime = new Date(malaysiaTimeInMillis); // to store as malaysia time in cassandra since it will make
                                                            // the date be UTC
        return malaysiaTime;
    }

}
