package com.alliance.diceruleengine.controller;

import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import com.alliance.diceruleengine.constant.ApiResponse;
import com.alliance.diceruleengine.exception.ServiceException;
import com.alliance.diceruleengine.model.RuleTemplate;
import com.alliance.diceruleengine.request.CreateRuleTemplateRequest;
import com.alliance.diceruleengine.service.RuleTemplateService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
public class RuleTemplateController {
    @Autowired
    MessageSource messageSource;

    @Autowired
    RuleTemplateService ruleTemplateService;

    @GetMapping("/ruleTemplate")
    public ResponseEntity<ApiResponse> getRuleTemplate(@RequestHeader HttpHeaders requestHeader,
            @RequestParam Integer ruleTemplateId) {
        log.info("getRuleTemplate - Get customerSegmentationTemplate on getCustomerSegmentationTemplate : {}", ruleTemplateId);
        try {
            ApiResponse apiResponse = new ApiResponse(ApiResponse.HTTP_RESPONSE_OK, true,
                    messageSource.getMessage("common.request.success", null, Locale.getDefault()),
                    ruleTemplateService.getRuleTemplateById(ruleTemplateId));
            return ResponseEntity.ok().body(apiResponse);
        } catch (ServiceException serviceException) {
            ApiResponse apiResponse = new ApiResponse(serviceException.getErrorCode(), false,
                    serviceException.getMessage(), null);
            return ResponseEntity.status(apiResponse.getResponseCode()).body(apiResponse);
        }
    }

    @PostMapping("/createRuleTemplate")
    public ResponseEntity<RuleTemplate> creteRuleTemplate(
            @RequestBody CreateRuleTemplateRequest request) {
        RuleTemplate template = ruleTemplateService.createRuleTemplate(request.getRuleType(), request.getDescription(),
                request.getTableName(), request.getKey(), request.getValue());
        return ResponseEntity.ok(template);
    }
}
