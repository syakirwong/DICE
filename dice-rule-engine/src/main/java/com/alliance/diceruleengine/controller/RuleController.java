package com.alliance.diceruleengine.controller;

import java.util.Locale;
import java.util.Optional;

import org.kie.api.runtime.KieContainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import com.alliance.diceruleengine.constant.ApiResponse;
import com.alliance.diceruleengine.exception.ServiceException;
import com.alliance.diceruleengine.model.Rule;
import com.alliance.diceruleengine.request.CreateRuleRequest;
import com.alliance.diceruleengine.request.ProcessCampaignRequest;
import com.alliance.diceruleengine.request.TestRuleProcessRequest;
import com.alliance.diceruleengine.response.ProcessCampaignResponse;
import com.alliance.diceruleengine.service.RuleService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
public class RuleController {
        @Autowired
        MessageSource messageSource;

        @Autowired
        RuleService ruleService;

        @GetMapping("/ruleList")
        public ResponseEntity<ApiResponse> getRuleList(@RequestHeader HttpHeaders requestHeader)
                        throws ServiceException {
                // log.info("Get all rule on getRuleList ");
                ApiResponse apiResponse = new ApiResponse(ApiResponse.HTTP_RESPONSE_OK, true,
                                messageSource.getMessage("common.request.success", null,
                                                Locale.getDefault()),
                                ruleService.getAllRule());
                return ResponseEntity.ok().body(apiResponse);
        }

        @PostMapping("/testProcessRule")
        public ResponseEntity<ApiResponse> processRule(@RequestBody TestRuleProcessRequest request)
                        throws NoSuchMessageException, ServiceException {
                try {
                        ApiResponse apiResponse = new ApiResponse(ApiResponse.HTTP_RESPONSE_OK, true,
                                        messageSource.getMessage("common.request.success", null,
                                                        Locale.getDefault()),
                                        ruleService.testProcessRule(request.getUuidType(), request.getUuid()));

                        return ResponseEntity.ok().body(apiResponse);
                } catch (ServiceException serviceException) {
                        log.error("processRule - ServiceException: {}", serviceException);
                        ApiResponse apiResponse = new ApiResponse(serviceException.getErrorCode(),
                                        false, serviceException.getMessage(), null);

                        return ResponseEntity.status(apiResponse.getResponseCode()).body(apiResponse);
                } catch (Exception ex) {
                        ApiResponse apiResponse = new ApiResponse(ApiResponse.HTTP_RESPONSE_INTERNAL_SERVER_ERROR,
                                        false,
                                        ex.getLocalizedMessage(), null);

                        return ResponseEntity.internalServerError().body(apiResponse);
                }

        }

        @GetMapping("/checkCampaign/{id}")
        public ResponseEntity<ApiResponse> checkCampaign(@PathVariable Integer id) {
                try {
                        ApiResponse apiResponse = new ApiResponse(ApiResponse.HTTP_RESPONSE_OK, true,
                                        messageSource.getMessage("common.request.success", null, Locale.getDefault()),
                                        ruleService.checkCampaign(id));
                        
                        return ResponseEntity.ok().body(apiResponse);
                } catch (ServiceException serviceException) {
                        log.info("checkCampaign controller - serviceException: {} | campaignId : {}", serviceException.toString(), id);
                        ApiResponse apiResponse = new ApiResponse(serviceException.getErrorCode(), true,
                                        serviceException.getMessage(), null);
                        return ResponseEntity.status(apiResponse.getResponseCode()).body(apiResponse);
                } catch (Exception ex) {
                        log.error("checkCampaign controller - Exception: {} : campaignId : {}", ex, id);
                        ApiResponse apiResponse = new ApiResponse(ApiResponse.HTTP_RESPONSE_INTERNAL_SERVER_ERROR,
                                        false,
                                        ex.getLocalizedMessage(), null);
                        return ResponseEntity.internalServerError().body(apiResponse);
                }
                // ApiResponse apiResponse = new ApiResponse(ApiResponse.HTTP_RESPONSE_OK, true,
                //                         messageSource.getMessage("common.request.success", null, Locale.getDefault()),
                //                         ruleService.checkCampaign(id));
                        
                //         return ResponseEntity.ok().body(apiResponse);
        }

        @PostMapping("/createRule")
        public ResponseEntity<Rule> createRule(
                        @RequestBody CreateRuleRequest request) {
                Rule rule = ruleService.createRule(request.getRuleName(), request.getRuleTemplateId());
                return ResponseEntity.ok(rule);
        }

        @PostMapping("/processCampaign")
        public ResponseEntity<ProcessCampaignResponse> processCampaign(
                        @RequestBody ProcessCampaignRequest processCampaignRequest) {
                try {
                        // log.info("start - processCampaign");
                        ProcessCampaignResponse apiResponse = ruleService.processCampaign(processCampaignRequest);
                        return ResponseEntity.ok().body(apiResponse);
                } catch (Exception ex) {

                        log.info("processCampaign - Exception - {}", ex.getMessage());

                        return null;
                }
        }
}
