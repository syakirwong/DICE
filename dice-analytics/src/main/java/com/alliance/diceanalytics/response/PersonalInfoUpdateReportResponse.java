package com.alliance.diceanalytics.response;

import lombok.Data;

@Data
public class PersonalInfoUpdateReportResponse {
    // 1 : Total number of targeted customers uploaded
    private Integer totalTargetCustomerUploaded;

    // A : Total number of customers targeted for push notification
    private Integer totalTargetCustomerPushNoti;

    // C : Total number of customers targeted for floating button (post-login)
    private Integer totalTargetCustomerFloatingButtonPostLogin;

    // D : Total number of customers targeted for logout
    private Integer totalTargetCustomerLogout;

    // 2 : How many successfully submitted the personal information update requests
    private Integer personalInfoUpdateRequestSubmitted;

    // A : How many customers being targeted for push notification
    private Integer noOfTargetedCustomerPushNoti2A;

    // B : How many customers tapped on bell inbox notification (post-login)
    private Integer noOfCustomerBellNotification;

    // C : How many customers being targeted for floating button (post-login)
    private Integer noOfTargetedCustomerFloatingBtn2C;

    // D : How many customers being targeted for logout
    private Integer noOfTargetedCustomerLogout;

    // 3 : How many failed to submit the personal information update requests
    private Integer noOfCustomerFailedSubmit;

    // A : How many customers being targeted for push notification
    private Integer noOfTargetedCustomerPushNoti3A;

    // ii : How many targeted customers did not tap on the push notification
    private Integer noOfTargetedCustomerNotTappedPushNoti;

    // C : How many customers being targeted for floating button (post-login)
    private Integer noOfTargetedCustomerFloatingBtn3C;

    // ii) How many targeted customers did not tap on floating button (post-login)
    private Integer noOfTargetedCustomerNotTappedFloatingBtn;

    // D : How many customers being targeted for logout
    private Integer noOfCustomerLogout3D;

    // ii : How many targeted customers did not tap on logout
    private Integer noOfTargetedCustomerNotLogout;

    // Total number of customers tapped on bell inbox notification (post-login)
    private Integer totalNumCustTapBellPostLogin;
    // i : How many targeted customers tapped on the push notification
    private Integer totalNumCustTapPush;
    // i : How many targeted customers tapped on floating button (post-login)
    private Integer totalNumCustTapFloat;
    // i : How many targeted customers tapped on logout
    private Integer totalNumCustTapLogout;
}
