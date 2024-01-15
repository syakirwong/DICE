package com.alliance.diceintegration.model;

import java.util.Date;

import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

import lombok.Data;

@Data
@Table("EVT_REFERRAL_HISTORY_LOG")
public class ReferralHistory extends BaseInfo {
    @PrimaryKey
    private Integer referralHistoryLogId;

    private String referrerDeviceUuid;
    private String referrerNric;
    private String referrerAccountNo;
    private String referrerMobileNo;
    private String referrerName;
    private Date referrerAccountOpeningDate;

    private String referreeDeviceUuid;
    private String referreeNric;
    private String referreeAccountNo;
    private String referreeMobileNo;
    private String referreeName;
    private Date referreeAccountOpeningDate;
    private Date referreeInitiationDate;

    private String referralCodeValue;
    private Integer campaignId;
}

