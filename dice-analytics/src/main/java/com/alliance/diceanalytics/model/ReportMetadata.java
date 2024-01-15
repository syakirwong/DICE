package com.alliance.diceanalytics.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Date;

@Data
@AllArgsConstructor
public class ReportMetadata {
    private String reportName;

    @JsonFormat(pattern="dd/MM/yyy HH:mm:ss")
    private Date reportStartDate;

    @JsonFormat(pattern="dd/MM/yyy HH:mm:ss")
    private Date reportEndDate;

    @JsonFormat(pattern="dd/MM/yyy HH:mm:ss")
    private Date reportRunDate;
}
