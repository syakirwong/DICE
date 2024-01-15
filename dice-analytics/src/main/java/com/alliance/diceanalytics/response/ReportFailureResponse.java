package com.alliance.diceanalytics.response;

import lombok.Data;

@Data
public class ReportFailureResponse {
    private String reportName;
    private String reportGenerationDate;
    private String frequency;
    private String message;

    public ReportFailureResponse(String reportName, String frequency, String message) {
        this.reportName = reportName;
        this.frequency = frequency;
        this.message = message;
    }
}
