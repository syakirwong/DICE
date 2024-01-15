package com.alliance.diceanalytics.response;


import lombok.Data;

@Data
public class HenryPerformanceWeeklyResponse {
    private String  engagementMode;
    private Integer noOfInAppPromoRedirection =0;
    private Integer noOfYesPlsContactMe =0;
    private Integer noOfNoImStillConsidering =0;
    private Integer noOfNoAvailableFunds =0;
    private Integer noOfPromoNotAttractive =0;
    private Integer noOfBetterOffers =0;
    private Integer noOfMinimumThresholdTooHigh =0;
    private Integer noOfClicksOthers =0;
    private Integer noOfTargetedCustomer = 500;

}
