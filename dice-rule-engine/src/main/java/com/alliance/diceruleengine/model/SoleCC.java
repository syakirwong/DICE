package com.alliance.diceruleengine.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;

@Data
@Entity
@Table(name = "DDM_SOLE_CC_VIEW")
public class SoleCC {
    @Id
    @Column(name = "CIFNO")
    private String cifNo;
    @Column(name = "USERID")
    private String userId;
    @Column(name = "ID_NO")
    private String idNo;
    @Column(name = "FULL_NAME")
    private String fullName;
    @Column(name = "MOBILE")
    private String mobile;
    @Column(name = "DEVICE_UUID")
    private String deviceUUID;
    @Column(name = "DEVICE_PLATFORM")
    private String devicePlatform;
}
