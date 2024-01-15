package com.alliance.diceanalytics.response;

import lombok.Data;

@Data
public class CrossSellPLoanWeeklyReportResponse {
    private Integer noOfCustomerPromptedWithCrossSellPage;
    private Integer noOfApplyNowClicked;
    private Integer noOfSuccessfulSavePlusOpening;
}
