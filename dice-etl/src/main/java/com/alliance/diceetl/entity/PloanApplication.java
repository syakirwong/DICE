package com.alliance.diceetl.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "EVT_PLOAN_APPLICATION_VIEW")
public class PloanApplication {

    @Id
    @Column(name = "UUID")
    private String uuid;

    @Column(name = "NRIC_NO")
    private String nricNo;

    @Column(name = "FULL_NAME")
    private String fullName;

    @Column(name = "PROMO_CODE")
    private String promoCode;

    @Column(name = "MOBILE_NO")
    private String mobileNo;

    @Column(name = "DEVICE_UUID")
    private String deviceUuid;

    @Column(name = "DEVICE_PLATFORM")
    private String devicePlatform;

    @Column(name = "IS_PDPA_CONSENT")
    private short isPdpaConsent;

    @Column(name = "IS_NTA")
    private short isNta;
}
