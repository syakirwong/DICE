package com.alliance.diceanalytics.response;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CrossSellSoloCCtoPLoanMonthlyResponse {
    private String applicationMobileNumber;
    private String applicationNRIC;
    private String firstLoginDateTime;
    private String applicationStartDate;
    private String aipSubmissionDateTime;
    private String fullSubmissionDateTime;
}
