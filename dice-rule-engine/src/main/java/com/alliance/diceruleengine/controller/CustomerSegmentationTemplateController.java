package com.alliance.diceruleengine.controller;

import java.util.List;
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
import com.alliance.diceruleengine.model.CustomerSegmentationTemplate;
import com.alliance.diceruleengine.request.CreateCustomerSegmentationTemplateRequest;
import com.alliance.diceruleengine.service.CustomerSegmentationTemplateService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
public class CustomerSegmentationTemplateController {
    @Autowired
    MessageSource messageSource;

    @Autowired
    CustomerSegmentationTemplateService customerSegmentationService;

    @GetMapping("/customerSegmentationTemplateById")
    public ResponseEntity<ApiResponse> getCustomerSegmentationTemplate(@RequestHeader HttpHeaders requestHeader,
            @RequestParam Integer customerSegmentationTemplateId) {
        log.info("customerSegmentationTemplateById - Get customerSegmentationTemplate on getCustomerSegmentationTemplate : {}",
                customerSegmentationTemplateId);
        try {
            ApiResponse apiResponse = new ApiResponse(ApiResponse.HTTP_RESPONSE_OK, true,
                    messageSource.getMessage("common.request.success", null, Locale.getDefault()),
                    customerSegmentationService.getCustomerSegmentationTemplateById(customerSegmentationTemplateId));
            return ResponseEntity.ok().body(apiResponse);
        } catch (ServiceException serviceException) {
            ApiResponse apiResponse = new ApiResponse(serviceException.getErrorCode(), false,
                    serviceException.getMessage(), null);
            return ResponseEntity.status(apiResponse.getResponseCode()).body(apiResponse);
        }
    }

    @GetMapping("/customerSegmentationTemplate")
    public ResponseEntity<ApiResponse> getAllCustomerSegmentationTemplate() throws ServiceException {
        ApiResponse apiResponse = new ApiResponse(ApiResponse.HTTP_RESPONSE_OK, true,
                messageSource.getMessage("common.request.success", null, Locale.getDefault()),
                customerSegmentationService.getAllCustomerSegmentationTemplate());
        return ResponseEntity.ok().body(apiResponse);
    }

    @PostMapping("/createCustomerSegmentationTemplate")
    public ResponseEntity<CustomerSegmentationTemplate> createCustomerSegmentationTemplate(
            @RequestBody CreateCustomerSegmentationTemplateRequest request) {
        CustomerSegmentationTemplate template = customerSegmentationService.createCustomerSegmentationTemplate(
                request.getCustomerSegmentType(), request.getCustomerSegmentValue(),
                request.getCustomerSegmentBehaviour(), request.getDescription());
        return ResponseEntity.ok(template);
    }

}
