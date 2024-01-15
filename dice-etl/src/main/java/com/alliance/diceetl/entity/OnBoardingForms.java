package com.alliance.diceetl.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@Entity
@Table(name = "EVT_ONBOARDING_FORMS_VIEW")
@AllArgsConstructor
@NoArgsConstructor
public class OnBoardingForms {

    @Id
    @Column(name = "UUID")
    private String uuid;

    @Column(name = "ID_NO")
    private String idNo;

    @Column(name = "MOBILE_NO")
    private String mobileNo;

    @Column(name = "CUSTOMER_NAME")
    private String customerName;

    @Column(name = "SUBMITTED_ON")
    private Timestamp submittedOn;

    @Column(name = "PROMO_CODE")
    private String promoCode;

    @Column(name = "DEVICE_UUID")
    private String deviceUuid;

    @Column(name = "DEVICE_PLATFORM")
    private String devicePlatform;
}
