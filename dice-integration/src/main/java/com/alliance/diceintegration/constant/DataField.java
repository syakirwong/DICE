package com.alliance.diceintegration.constant;

public class DataField {
    public enum Status {
        DELETED,
        ACTIVE,
        DISABLE,
    }

    public enum EngagementStatus {
        NULL,
        COMPLETED_ENGAGED,
        COMPLETED_NO_ENGAGEMENT,
        PREENGAGEMENT;
    }

    public enum ActionStatus {
        TAPPED,
        REACHED,
        SUCCESS,
        FAILED,
        STOPED,
        SHOWED,
        PENDING
    }

    public enum RequestStatus {
        SUCCESS,
        EXCEPTION,
        SERVICE_EXCEPTION
    }
    public enum HttpMethod {
        GET,
        POST,
        PUT,
        DELETE,
        PATCH,
        HEAD,
        OPTIONS,
        TRACE
    }

}