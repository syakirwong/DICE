package com.alliance.dicesqlitecache.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table(name = "EVT_SOLE_CC_VIEW")
public class SoleCcEntity {
    @Id
    @Column(name = "user_id")
    private String userId;

    @Column(name = "cif_no")
    private String cifNo;

    @Column(name = "new_ic_no")
    private String newIcNo;

    @Column(name = "customer_name")
    private String customerName;

    @Column(name = "mobile")
    private String mobile;

    @Column(name = "package_id")
    private String packageId;

    @Column(name = "dob")
    private String dob;

    @Column(name = "gender")
    private String gender;

    @Column(name = "email")
    private String email;

    @Column(name = "nationality")
    private String nationality;

    @Column(name = "mobile_first_platform_id")
    private String mobileFirstPlatformId;

    @Column(name = "device_platform")
    private String devicePlatform;
}
