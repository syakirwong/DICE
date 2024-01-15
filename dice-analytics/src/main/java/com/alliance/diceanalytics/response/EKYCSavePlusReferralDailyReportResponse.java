package com.alliance.diceanalytics.response;

import java.util.List;

import lombok.Data;

@Data
public class EKYCSavePlusReferralDailyReportResponse {
    private String referrerCustomerName;
    private String referrerCustomerNRIC;
    private String referrerCustomerMobileNumber;
    private String referrerCustomerAccountNo;
    private String referrerDateTimeOfReferralIssues;
    private String referrerDateTimeOfReferralAttemped;
    private String referrerDateTimeOfReferralSuccess;
    private String refereeCustomerName;
    private String refereeCustomerNRIC;
    private String refereeCustomerMobileNumber;
    private String refereeCustomerAccountNo;
    private String refereeEKYCApplicationStatus;
}
