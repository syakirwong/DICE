package com.alliance.diceanalytics.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class BaseReportInfoRequest{
    private Date startDate;
    private Date endDate;
    private String engagementMode;
    private String eventType;
    private String [] campaignName;
    private Integer  campaignId;
    private Class<?> tableType;


    public BaseReportInfoRequest(Date startDate, Date endDate, String engagementMode, String eventType) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.engagementMode = engagementMode;
        this.eventType = eventType;
    }


}
