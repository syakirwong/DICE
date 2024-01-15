package com.alliance.dicecontentmanagement.constant;

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

    public enum DashboardType {
        ENGAGEMENT,
        PREENGAGEMENT,
        NOT_ENGAGEMENT,
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

}
