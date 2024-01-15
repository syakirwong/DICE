package com.alliance.dicerecommendation.model;

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

    public enum CampaignStatus {
        VALID,
        EXPIRED,
        DISABLE,
        INVALID
    }

    public enum ScheduleStatus {
        PENDING,
        PROCESSING,
        COMPLETED,
        EXPIRED,
        DISABLE,
        FAILED,
        INVALID,
    }
}
