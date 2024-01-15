package com.alliance.diceintegration.exception;


import com.alliance.diceintegration.constant.ApiResponse;
import com.alliance.diceintegration.constant.DataField;
import com.alliance.diceintegration.request.AuditTrailRequest;
import com.alliance.diceintegration.service.AuditTrailService;
import lombok.extern.slf4j.Slf4j;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

@Slf4j
@ControllerAdvice
public class AuditExceptionHandler {

    @Autowired
    AuditTrailService auditTrailService;

    @ExceptionHandler(value = {ServiceException.class})
    public ResponseEntity<ApiResponse> serviceException(ServiceException ex, HandlerMethod handlerMethod, HttpServletRequest request) {

        AuditTrailRequest auditTrail = new AuditTrailRequest();
        ApiResponse apiResponse = new ApiResponse(ex.getErrorCode(), false, ex.getMessage(), null);

        auditTrail.setHttpMethod(request.getMethod());
        auditTrail.setStatusCode(String.valueOf(ex.getErrorCode()));
        auditTrail.setEndPointUrl(request.getRequestURL().toString());
        auditTrail.setMessage(ex.getMessage());
        auditTrail.setEvent(handlerMethod.getMethod().getName());
        auditTrail.setRequestStatus(DataField.RequestStatus.SERVICE_EXCEPTION.toString());
        auditTrail.setCodeLocation(handlerMethod.getMethod().getDeclaringClass().toString());
        auditTrail.setIsRetry(false);
        auditTrail.setIsSentEmail(false);

        try {
            log.info("Audit Log: " +new ObjectMapper().writeValueAsString(auditTrail));
            auditTrailService.saveAuditTrail(auditTrail);
        } catch (IOException e) {
            e.printStackTrace();
        }


        return ResponseEntity.status(ex.getErrorCode()).body(apiResponse);
    }

    @ExceptionHandler({MissingServletRequestParameterException.class,NumberFormatException.class, MethodArgumentTypeMismatchException.class})
    public ResponseEntity<ApiResponse> handleMissingInvalidParams(Exception ex) {
        ApiResponse apiResponse = new ApiResponse(ApiResponse.HTTP_RESPONSE_BAD_REQUEST, false, ex.getMessage(), null);
        return ResponseEntity.badRequest().body(apiResponse);
    }

    @ExceptionHandler(value = {Exception.class})
    public ResponseEntity<ApiResponse> internalErrorException(Exception ex,HandlerMethod handlerMethod,  HttpServletRequest request) {

        AuditTrailRequest auditTrail = new AuditTrailRequest();
        ApiResponse apiResponse = new ApiResponse(ApiResponse.HTTP_RESPONSE_INTERNAL_SERVER_ERROR, false, ex.getMessage(), null);
        
        auditTrail.setHttpMethod(request.getMethod());
        auditTrail.setStatusCode(String.valueOf(ApiResponse.HTTP_RESPONSE_INTERNAL_SERVER_ERROR));
        auditTrail.setEndPointUrl(request.getRequestURL().toString());
        auditTrail.setEvent(handlerMethod.getMethod().getName());
        auditTrail.setMessage(ex.getMessage());
        auditTrail.setRequestStatus(DataField.RequestStatus.EXCEPTION.toString());
        auditTrail.setCodeLocation(handlerMethod.getMethod().getDeclaringClass().toString());
        auditTrail.setIsRetry(false);
        auditTrail.setIsSentEmail(false);

        try {
            log.info("Audit Log: " +new ObjectMapper().writeValueAsString(auditTrail));
            auditTrailService.saveAuditTrail(auditTrail);
        } catch (IOException e) {
            e.printStackTrace();
        }


        return ResponseEntity.internalServerError().body(apiResponse);
    }



}
