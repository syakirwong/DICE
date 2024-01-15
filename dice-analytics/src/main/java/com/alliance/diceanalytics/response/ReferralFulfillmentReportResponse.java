package com.alliance.diceanalytics.response;

import com.alliance.diceanalytics.model.ReferralHistory;
import com.alliance.diceanalytics.utility.AESEncyptionUtil;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.text.SimpleDateFormat;

@Data
@NoArgsConstructor
public class ReferralFulfillmentReportResponse {

    private String referrerName;
    private String referrerNric;
    private String referrerMobileNo;
    private String referrerAccountNo;

    //TODO Referrer/DateTime of referral issues
    private String referrerDateTimeIssued;

    //TODO Referrer/DateTime of referral attemped
    private String referrerDateTimeAttempted;

    //Referrer/DateTime of referral success TODO CHECK
    private String referrerDateTimeSuccess;

    private String refereeName;
    private String refereeNric;
    private String refereeMobileNo;
    private String refereeAccountNo;
    private String referralCode;
    private String refereeApplicationStatus;


    public ReferralFulfillmentReportResponse(ReferralHistory referralHistory){
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");


        if (referralHistory.getReferrerNric()!=null)
            referrerNric = AESEncyptionUtil.decrypt(referralHistory.getReferrerNric());


        if (referralHistory.getReferrerMobileNo()!=null)
            referrerMobileNo = AESEncyptionUtil.decrypt(referralHistory.getReferrerMobileNo());

        if (referralHistory.getReferrerName()!=null)
            referrerName = AESEncyptionUtil.decrypt(referralHistory.getReferrerName());



        if (referralHistory.getReferreeNric()!=null)
            refereeNric =AESEncyptionUtil.decrypt(referralHistory.getReferreeNric());

        if (referralHistory.getReferreeMobileNo()!=null)
            refereeMobileNo = AESEncyptionUtil.decrypt(referralHistory.getReferreeMobileNo());

        if (referralHistory.getReferreeName()!=null)
            refereeName =  AESEncyptionUtil.decrypt(referralHistory.getReferreeName());


        if (referralHistory.getCreatedOn()!= null)
            referrerDateTimeIssued = dateFormat.format(referralHistory.getCreatedOn());

        refereeApplicationStatus = referralHistory.getStatus().toString();
        referralCode = referralHistory.getReferralCodeValue();

    }

    public void setReferrerMobileNo(String referrerMobileNo) {
        if (referrerMobileNo!=null){
            if (referrerMobileNo.charAt(0) == '+')
                this.referrerMobileNo = referrerMobileNo.substring(1);
            else
                this.referrerMobileNo = referrerMobileNo;
        }
    }
}
