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
import com.alliance.diceruleengine.model.SoleCC;
import com.alliance.diceruleengine.request.CreateSoleCCRequest;
import com.alliance.diceruleengine.service.SoleCCService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
public class SoleCCController {
    @Autowired
    MessageSource messageSource;

    @Autowired
    SoleCCService soleCCService;

    @GetMapping("/soleCC")
    public ResponseEntity<ApiResponse> getCustomerSoleCC(@RequestHeader HttpHeaders requestHeader,
            @RequestParam(name = "cifNo", required = false) String cifNo,
            @RequestParam(name = "getAll", required = false) Boolean getAll) {
        log.info("getCustomerSoleCC - Get soleCC for cifNo : {}", cifNo);
        try {

            ApiResponse apiResponse = !getAll ? new ApiResponse(ApiResponse.HTTP_RESPONSE_OK, true,
                    messageSource.getMessage("common.request.success", null, Locale.getDefault()),
                    soleCCService.getCustomerSoleCC(cifNo))
                    : new ApiResponse(ApiResponse.HTTP_RESPONSE_OK, true,
                            messageSource.getMessage("common.request.success", null, Locale.getDefault()),
                            soleCCService.getAllCustomerSoleCC());

            return ResponseEntity.ok().body(apiResponse);
        } catch (ServiceException serviceException) {
            ApiResponse apiResponse = new ApiResponse(serviceException.getErrorCode(), false,
                    serviceException.getMessage(), null);
            return ResponseEntity.status(apiResponse.getResponseCode()).body(apiResponse);
        }
    }

    // @PostMapping("/createSoleCC")
    // public ResponseEntity<SoleCC> createSoleCC(@RequestBody CreateSoleCCRequest
    // request) {
    // SoleCC soleCC = soleCCService.createSoleCC(request.getCifNo(),
    // request.getUserId(), request.getIdNo(),
    // request.getFullName(), request.getMobile(), request.getDeviceUUID(),
    // request.getDevicePlatform());
    // return ResponseEntity.ok(soleCC);
    // }
}
