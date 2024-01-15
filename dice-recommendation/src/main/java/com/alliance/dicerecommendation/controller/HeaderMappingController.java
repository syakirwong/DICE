package com.alliance.dicerecommendation.controller;

import com.alliance.dicerecommendation.constant.ApiResponse;
import com.alliance.dicerecommendation.exception.ServiceException;
import com.alliance.dicerecommendation.model.HeaderMapping;
import com.alliance.dicerecommendation.request.DataMismatchHistoryRequest;
import com.alliance.dicerecommendation.request.HeaderMappingRequest;
import com.alliance.dicerecommendation.service.HeaderMappingService;
import java.util.Locale;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
public class HeaderMappingController {

    @Autowired
    private HeaderMappingService headerMappingService;

    @Autowired
    MessageSource messageSource;

    @PostMapping("/createHeaderMapping")
    public ResponseEntity<ApiResponse> createHeaderMapping(
            @RequestBody HeaderMappingRequest headerMappingRequest) {
        try {
            // log.info("start - createHeaderMapping - headerMappingRequest : {}", headerMappingRequest);
            ApiResponse apiResponse = new ApiResponse(ApiResponse.HTTP_RESPONSE_OK, true,
                    messageSource.getMessage("common.operation.success", null, Locale.getDefault()),
                    headerMappingService.createHeaderMapping(headerMappingRequest));

            return ResponseEntity.ok().body(apiResponse);

        } catch (ServiceException serviceException) {
            log.error("createHeaderMapping - ServiceException: {}", serviceException);
            ApiResponse apiResponse = new ApiResponse(serviceException.getErrorCode(),
                    false, serviceException.getMessage(), null);

            return ResponseEntity.status(apiResponse.getResponseCode()).body(apiResponse);
        } catch (Exception ex) {
            ApiResponse apiResponse = new ApiResponse(ApiResponse.HTTP_RESPONSE_INTERNAL_SERVER_ERROR, false,
                    ex.getLocalizedMessage(), null);

            return ResponseEntity.internalServerError().body(apiResponse);
        }
    }

    @GetMapping("/getHeaderMapping/{headerMappingId}")
    public ResponseEntity<HeaderMapping> getHeaderMapping(@PathVariable Integer headerMappingId) {
        log.info("start - getHeaderMapping with headerMappingId : {}", headerMappingId);
        Optional<HeaderMapping> optionalHeaderMapping = headerMappingService.getHeaderMapping(headerMappingId);
        if (optionalHeaderMapping.isPresent()) {
            return ResponseEntity.ok(optionalHeaderMapping.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/addDataMismatchHistoryLog")
    public ResponseEntity<ApiResponse> addDataMismatchHistoryLog(
            @RequestBody DataMismatchHistoryRequest dataMismatchHistoryRequest) {
        try {
            log.info("start - addDataMismatchHistoryLog");
            ApiResponse apiResponse = new ApiResponse(ApiResponse.HTTP_RESPONSE_OK, true,
                    messageSource.getMessage("common.operation.success", null, Locale.getDefault()),
                    headerMappingService.addDataMismatchHistoryLog(dataMismatchHistoryRequest));

            return ResponseEntity.ok().body(apiResponse);

        } catch (ServiceException serviceException) {
            log.error("createHeaderMapping - ServiceException: {}", serviceException);
            ApiResponse apiResponse = new ApiResponse(serviceException.getErrorCode(),
                    false, serviceException.getMessage(), null);

            return ResponseEntity.status(apiResponse.getResponseCode()).body(apiResponse);
        } catch (Exception ex) {
            ApiResponse apiResponse = new ApiResponse(ApiResponse.HTTP_RESPONSE_INTERNAL_SERVER_ERROR, false,
                    ex.getLocalizedMessage(), null);

            return ResponseEntity.internalServerError().body(apiResponse);
        }
    }


}
