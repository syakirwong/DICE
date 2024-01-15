package com.alliance.diceanalytics.response;

import com.alliance.diceanalytics.model.ReferralHistory;
import com.alliance.diceanalytics.utility.AESEncyptionUtil;
import lombok.Data;

import java.text.SimpleDateFormat;

@Data
public class WhatsappReferralReportResponse {
    private Integer id;
    private Integer campaignId;

    private String referrerName;
    private String referrerNric;
    private String referrerMobileNo;
    private String referrerAccountNo;
    private String referrerAccountOpeningDate;

    private String refereeName;
    private String refereeNric;
    private String refereeMobileNo;
    private String refereeAccountNo;
    private String refereeInitiationDate;
    private String refereeAccountOpeningDate;


    private String referralCodeValue;

    public WhatsappReferralReportResponse(ReferralHistory referralHistory){
        SimpleDateFormat dateTimeFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

        id = referralHistory.getReferralHistoryLogId();
        campaignId = referralHistory.getCampaignId();


        //referrer
        if (referralHistory.getReferrerNric()!=null)
            referrerNric = AESEncyptionUtil.decrypt(referralHistory.getReferrerNric());


        if (referralHistory.getReferrerMobileNo()!=null){
            referrerMobileNo = AESEncyptionUtil.decrypt(referralHistory.getReferrerMobileNo());
            if (referrerMobileNo.charAt(0) == '6')
                referrerMobileNo=referrerMobileNo.substring(1);
        }

        if (referralHistory.getReferrerName()!=null)
            referrerName = AESEncyptionUtil.decrypt(referralHistory.getReferrerName());

        if (referralHistory.getReferrerAccountOpeningDate()!= null)
            referrerAccountOpeningDate = dateTimeFormat.format(referralHistory.getReferrerAccountOpeningDate());


        //referree
        if (referralHistory.getReferreeNric()!=null)
            refereeNric =AESEncyptionUtil.decrypt(referralHistory.getReferreeNric());


        if (referralHistory.getReferreeMobileNo()!=null){
            refereeMobileNo =AESEncyptionUtil.decrypt(referralHistory.getReferreeMobileNo());
            if (refereeMobileNo.charAt(0) == '6')
                refereeMobileNo=refereeMobileNo.substring(1);
        }

        if (referralHistory.getReferreeName()!=null)
            refereeName = AESEncyptionUtil.decrypt(referralHistory.getReferreeName());




        if (referralHistory.getReferreeAccountOpeningDate()!=null)
            refereeAccountOpeningDate = dateTimeFormat.format(referralHistory.getReferreeAccountOpeningDate());

        if (referralHistory.getReferreeInitiationDate()!=null)
            refereeInitiationDate =dateTimeFormat.format(referralHistory.getReferreeInitiationDate());

        referralCodeValue=referralHistory.getReferralCodeValue();

    }
}
