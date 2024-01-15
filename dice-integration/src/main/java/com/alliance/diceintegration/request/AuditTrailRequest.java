package com.alliance.diceintegration.request;


import lombok.Data;

@Data
public class AuditTrailRequest {
    private String event;

    private String httpMethod;

    private String endPointUrl;

    private String statusCode;

    private String message;

    private String requestStatus;

    private String codeLocation;

    private Boolean isRetry;

    private Boolean isSentEmail;
}
