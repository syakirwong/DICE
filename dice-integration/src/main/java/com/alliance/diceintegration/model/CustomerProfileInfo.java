package com.alliance.diceintegration.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import org.hibernate.annotations.GenericGenerator;


import lombok.Data;

@Data
@Entity
public class CustomerProfileInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    private Integer customerProfileInfoId;

    @Column
    private String eformUuid;

    @Column
    private String alternateName;

    @Column
    private String mobileStatus;

    @Column
    private String applicationStageCode;

    @Column
    private String invitationCode;

    @Column
    private String applicationCreationTime;

    @Column
    private String applicationLastUpdate;

    @Column
    private String mobileIdNo;

    @Column
    private String devicePlatform;

    @Column
    private String deviceUuid;

    @Column
    private boolean payloadEtb;

    @Column
    private String payloadOccupationIncome;

    @Column
    private String payloadOccupationCCRISCode;

    @Column
    private String payloadOccupationAMLCode;

    @Column
    private String payloadBnmCounterPartyCode;

    @Column
    private String payloadIndustryCategory;

    @Column
    private String payloadSourceOfWealthCode;

    @Column
    private String payloadGrossAnnualIncome;

    @Column
    private String payloadEmployerName;

    @Column
    private String payloadEmploymentSectorCode;

    @Column
    private String payloadStandardIndustryClassCode;

    @Column
    private String payloadIndustryCode;

    @Column
    private String payloadMaritalStatusCode;

    @Column
    private String payloadOccupationCategoryCode;

    @Column
    private String payloadSourceOfFundCode;

    @Column
    private String payloadEmploymentTypeCode;

    @Column
    private String applicationRef;

    @Column
    private String identityInfoCountry;

    @Column
    private String identityInfoAddress3;

    @Column
    private String identityInfoAddress2;

    @Column
    private String identityInfoCity;

    @Column
    private String identityInfoCifNo;

    @Column
    private String identityInfoAddress1;

    @Column
    private String identityInfoFullname;// IDENTITY_INFO_FULLNAME

    @Column
    private String identityInfoGenderCode;

    @Column
    private String identityInfoNationality;

    @Column
    private String identityInfoIssuingCountry;

    @Column
    private String identityInfoDob;

    @Column
    private String identityInfoPostCode;

    @Column
    private String identityInfoStateCode;

    @Column
    private String identityInfoReligionCode;

    @Column
    private String identityInfoRaceCode;

    @Column
    private boolean primaryAccountHolder;

    @Column
    private String segmentCode;

    @Column
    private String relationship;

    @Column
    private String version;

    @Column
    private String idNo;// IDNO

    @Column
    private String contactDetails;

    @Column
    private String contactDetailsCountry;

    @Column
    private String contactDetailsAddress3;

    @Column
    private String contactDetailsAddress2;

    @Column
    private String contactDetailsIsContactAddressSameWithMYKAD;

    @Column
    private String contactDetailsCity;

    @Column
    private String contactDetailsAddress1;

    @Column
    private String contactDetailsPostCode;

    @Column
    private String contactDetailsStateCode;

    @Column
    private String mobileNumber;

    @Column
    private String contactDetailsEmail;

    @Column
    private String stageCode;

    @Column
    private String idTypeCode;

    @Column
    private boolean termsConditionsIsAcceptedTnc;

    @Column
    private boolean pdpaIsAgreePDPA;

    @Column
    private boolean fatcaIsUSPerson;

    @Column
    private String crsTaxResidenceStatus;

    // @Lob
    // @Column(columnDefinition = "BLOB")
    // private Blob digitalSignature;

    @Column
    private String digitalSignatureCurrentDate;

    @Column
    private String digitalSignatureCurrentTime;

    @Column
    private Date createdDate;

    @Column
    private Date modifiedDate;

    // public CustomerProfileInfo(String form_uuid, String device_uuid) {
    // this.form_uuid = form_uuid;
    // this.device_uuid = device_uuid;
    // }
    public CustomerProfileInfo() {
    }

    public CustomerProfileInfo(String eform_uuid) {
        this.eformUuid = eform_uuid;
    }
}

