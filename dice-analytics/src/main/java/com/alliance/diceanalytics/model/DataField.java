package com.alliance.diceanalytics.model;

import lombok.Data;

@Data
public class DataField {
    public enum Status {
        DELETED,
        ACTIVE,
        DISABLE
    }

    public enum TriggerStatus {
        NEW,
        PENDING,
        COMPLETED,
        EXPIRED,
        DISABLE,
        FAILED,
        NON_TRIGGER
    }

    public enum ActionStatus {
        TAPPED,
        REACHED,
        SUCCESS,
        FAILED,
        SHOWED,
        STOPED,
        PENDING
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

    public enum RequestStatus {
        SUCCESS,
        EXCEPTION,
        SERVICE_EXCEPTION
	}

    public enum CodeLocation {
        InboundController,
        CallbackService
	}
}
