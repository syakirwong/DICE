package com.alliance.diceanalytics.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CrossSellPLoanToSPMonthlyResponse {
    private String applicationMobileNumber;
    private String applicationNRIC;
    private String loanApplicationDateTime;
    private String ekycSavePlusStartDateTime;
    private String ekycSavePlusStatus;
}
