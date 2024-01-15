package com.alliance.diceanalytics.response;

import lombok.Data;

@Data
public class CampaignReportResponse extends BaseResponse {
    private Integer totalTargetedCustomer;
    private Integer totalNotificationSent;
    private Integer totalPushNotificationTap;
    private Integer totalBellBoxNotificationTapPostLogin;
    private Integer totalFloatingButtonTapPostLogin;
    private Integer totalGeneralLogoutTap;
    private Integer totalTargetLogoutTap;
    private Integer totalBellBoxNotificationTapPreLogin;
    private Integer totalFloatingButtonPreLogin;
    private Integer totalInAppMessageReached;
    private Integer totalUpdateNowTap;
    private Integer totalDontShowMeAgainTap;
    private Integer totalNextTap;
    private Integer totalNoChangeTap;
    private Integer totalSuccessLogin;
    private Integer totalFailLogin;
    private Integer totalWithFacialProfilInteger;
    private Integer totalWithOutFacialProfilInteger;
    private Integer totalEnrollFacialProfile;
    private Integer totalSuccessFacialBiometric;
    private Integer totalFailFacialBiometric;
    private Integer totalSuccessUpdateSubmit;
    private Integer totalFailUpdateSubmit;
}

