package com.alliance.diceanalytics.response;


import lombok.Data;

@Data
public class CrossSellSoloCCtoPLoanWeeklyResponse {
    private Integer noOfFloatingButtonDisplayed;
    private Integer noOfFloatingIconClicked;
    private Integer noOfApplyNowClicked;
    private Integer noOfAIPSubmission;
    private Integer noOfFullSubmission;

}
