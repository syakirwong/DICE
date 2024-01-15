package com.alliance.dicesqlitecache.model;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table(name = "EVT_VCC_ONBOARDING_FORMS_VIEW")
public class VccOnboardingFormsEntity {
    @Id
    @Column(name = "uuid")
    private String uuid;

    @Column(name = "id_no")
    private String idNo;

    @Column(name = "mobile_no")
    private String mobileNo;

    @Column(name = "customer_name")
    private String customerName;

    @Column(name = "submitted_on")
    private Date submittedOn;

    @Column(name = "promo_code")
    private String promoCode;

    @Column(name = "device_uuid")
    private String deviceUuid;

    @Column(name = "device_platform")
    private String devicePlatform;
}
