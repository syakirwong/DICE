package com.alliance.diceanalytics.response;

import com.alliance.diceanalytics.model.ReferralCode;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;


@Data
public class MasterPromoCodeResponse {
    @JsonProperty("campaignid")
    private Integer campaignID;

    @JsonProperty("codevalue")
    private String codeValue;

    @JsonProperty("eformuuid")
    private String eformUUID;

    @JsonProperty("cifno")
    private String cifNo;

    public MasterPromoCodeResponse (ReferralCode referralCode){
        this.campaignID = referralCode.getCampaignId();
        this.codeValue = referralCode.getCodeValue();
        this.eformUUID = referralCode.getEformUuid();
        this.cifNo = referralCode.getCifNo();
    }

}
