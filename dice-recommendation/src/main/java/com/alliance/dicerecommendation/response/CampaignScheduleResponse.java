package com.alliance.dicerecommendation.response;

import java.util.Date;

import com.alliance.dicerecommendation.model.DataField.ScheduleStatus;

import lombok.Data;

@Data
public class CampaignScheduleResponse {
    private String filePath;
    private String fileName;
    private Integer campaignId;
    private Date engagementStartDateTime;
    private ScheduleStatus scheduleStatus;
    private String remark;
    private Date processDateTime;
    private String diceEncryptedFilePassword;
}
