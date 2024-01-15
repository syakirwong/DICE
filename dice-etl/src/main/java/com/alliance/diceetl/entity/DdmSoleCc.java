package com.alliance.diceetl.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Table(name = "EVT_DDM_SOLE_CC_VIEW")
@AllArgsConstructor
@NoArgsConstructor
public class DdmSoleCc {

    @Id
    @Column(name = "USERID")
    private String userId;

    @Column(name = "CIF")
    private String cif;

    @Column(name = "NEWICNO")
    private String newIcNo;

    @Column(name = "CUSTOMERNAME")
    private String customerName;

    @Column(name = "MOBILE")
    private String mobile;

    @Column(name = "PACKAGEID")
    private String packageId;  // PACKAGEID

    @Column(name = "DOB")
    private String dob; // DOB

    @Column(name = "GENDER")
    private String gender;

    @Column(name = "EMAIL")
    private String email;

    @Column(name = "NATIONALITY")
    private String nationality; // NATIONALITY

    @Column(name = "MOBILEFIRSTPLATFORMID")
    private String mobileFirstPlatformId;

    @Column(name = "DEVICEPLATFORM")
    private String devicePlatform; // DEVICEPLATFORM
}

