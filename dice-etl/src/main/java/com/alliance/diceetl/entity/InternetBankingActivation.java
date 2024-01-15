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
@Table(name = "EVT_INTERNET_BANKING_ACTIVATION_VIEW")
@AllArgsConstructor
@NoArgsConstructor
public class InternetBankingActivation {

    @Id
    @Column(name = "UUID")
    private String uuid;

    @Column(name = "ID_NO")
    private String idNo;

    @Column(name = "FULL_NAME")
    private String fullName;

    @Column(name = "PROMO_CODE")
    private String promoCode;

    @Column(name = "MOBILE")
    private String mobile;

    @Column(name = "DEVICE_UUID")
    private String deviceUuid;

    @Column(name = "DEVICE_PLATFORM")
    private String devicePlatform;

    @Column(name = "COMPLETED_ON")
    private Timestamp completedOn;

    @Column(name = "STATUS_CODE")
    private String statusCode;

    @Column(name = "PDPA_FLAG")
    private String pdpaFlag;
}
