package com.alliance.diceintegration.model;

import java.io.Serializable;

import lombok.Data;

@Data
public class FilterCustomerSegmentation implements Serializable {

    private String eformUuid;

    private String devicePlatform;

    private String deviceUuid;

    private String identityInfoFullname;

    private String identityInfoGenderCode;

    private String idNo;

    private String mobileNumber;

    private String contactDetailsEmail;

    private String idTypeCode;

}

