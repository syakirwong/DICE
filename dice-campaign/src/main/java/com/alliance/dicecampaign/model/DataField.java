package com.alliance.dicecampaign.model;

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
}
