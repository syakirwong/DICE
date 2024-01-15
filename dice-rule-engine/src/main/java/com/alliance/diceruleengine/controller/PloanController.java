package com.alliance.diceruleengine.controller;

import com.alliance.diceruleengine.constant.ApiResponse;
import com.alliance.diceruleengine.exception.ServiceException;
import com.alliance.diceruleengine.service.PloanService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Locale;
import java.util.Map;

@Slf4j
@Controller
public class PloanController {
    @Autowired
    MessageSource messageSource;

    @Autowired
    PloanService ploanService;

    @GetMapping("/ploan")
    public ResponseEntity<ApiResponse> getCustomerPloan(@RequestHeader HttpHeaders requestHeader,
            @RequestParam(name = "eformUUID", required = false) String eformUUID,
            @RequestParam(name = "getAll", required = false) Boolean getAll) {
        log.info("start - getCustomerPloan with eformUUID : {}", eformUUID);
        try {
            ApiResponse apiResponse = !getAll ? new ApiResponse(ApiResponse.HTTP_RESPONSE_OK, true,
                    messageSource.getMessage("common.request.success", null, Locale.getDefault()),
                    ploanService.getCustomerPloan(eformUUID))
                    : new ApiResponse(ApiResponse.HTTP_RESPONSE_OK, true,
                            messageSource.getMessage("common.request.success", null, Locale.getDefault()),
                            ploanService.getAllCustomerPloan());
            return ResponseEntity.ok().body(apiResponse);
        } catch (ServiceException serviceException) {
            ApiResponse apiResponse = new ApiResponse(serviceException.getErrorCode(), false,
                    serviceException.getMessage(), null);
            return ResponseEntity.status(apiResponse.getResponseCode()).body(apiResponse);
        }
    }

    @GetMapping("/ploan/{deviceID}")
    public ResponseEntity<ApiResponse> getCustomerPLoanByDevice(@PathVariable String deviceID){
        try {
            Map<String,Object> pLoanResponse = ploanService.getCustomerPLoanByDevice(deviceID);

            if (pLoanResponse.size()==0){
                ApiResponse apiResponse = new ApiResponse(403, true, "Customer Profile Not Found", null);
                log.info("getCustomerPLoanByDevice - Profile by DeviceID (" + deviceID + ") Not Found");
                return ResponseEntity.badRequest().body(apiResponse);
            }


            try {
                log.info("getCustomerPLoanByDevice - Profile by DeviceID (" + deviceID + ") :" + new ObjectMapper().writeValueAsString(pLoanResponse));
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
            ApiResponse apiResponse = new ApiResponse(ApiResponse.HTTP_RESPONSE_OK, true,
                    messageSource.getMessage("common.request.success", null, Locale.getDefault()), pLoanResponse);
            return ResponseEntity.ok(apiResponse);

        } catch (ServiceException e) {
            ApiResponse apiResponse = new ApiResponse(e.getErrorCode(), false, e.getMessage(), null);
            e.printStackTrace();
            return ResponseEntity.badRequest().body(apiResponse);
        }
    }
}
