package com.alliance.dicesqlitecache.model;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "EVT_INTERNET_BANKING_ACTIVATION_VIEW")
@Data
public class InternetBankingActivationEntity {
    @Id
    @Column(name = "uuid")
    private String uuid;

    @Column(name = "id_no")
    private String idNo;

    @Column(name = "full_name")
    private String fullName;

    @Column(name = "promo_code")
    private String promoCode;

    @Column(name = "mobile")
    private String mobile;

    @Column(name = "device_uuid")
    private String deviceUuid;

    @Column(name = "device_platform")
    private String devicePlatform;

    @Column(name = "completed_on")
    private Date completedOn;

    @Column(name = "status_code")
    private String statusCode;

    @Column(name = "pdpa_flag")
    private String pdpaFlag;
}
