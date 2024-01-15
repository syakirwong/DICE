package com.alliance.dicesqlitecache.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table(name = "EVT_PLOAN_APPLICATION_VIEW")
public class PloanApplicationEntity {
    @Id
    @Column(name = "uuid")
    private String uuid;

    @Column(name = "nric_no")
    private String nricNo;

    @Column(name = "full_name")
    private String fullName;

    @Column(name = "promo_code")
    private String promoCode;

    @Column(name = "mobile_no")
    private String mobileNo;

    @Column(name = "device_uuid")
    private String deviceUuid;

    @Column(name = "device_platform")
    private String devicePlatform;

    @Column(name = "is_pdpa_consent")
    private Short isPdpaConsent;

    @Column(name = "is_nta")
    private Short isNta;
}
