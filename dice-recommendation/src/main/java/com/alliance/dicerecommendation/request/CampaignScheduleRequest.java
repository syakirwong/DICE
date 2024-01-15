package com.alliance.dicerecommendation.request;

import java.util.Date;

import com.alliance.dicerecommendation.model.DataField.ScheduleStatus;

import lombok.Data;

@Data
public class CampaignScheduleRequest {
    private String filePath;
    private String fileName;
    private Integer campaignId;
    private Date engagementStartDateTime;
    private ScheduleStatus scheduleStatus = ScheduleStatus.PENDING;
    private String remark;
    private String filePassword;
    private Date processDateTime;

    // copy method
    public CampaignScheduleRequest copy() {
        CampaignScheduleRequest copy = new CampaignScheduleRequest();
        copy.setFilePath(this.filePath);
        copy.setFileName(this.fileName);
        copy.setCampaignId(this.campaignId);
        copy.setEngagementStartDateTime(this.engagementStartDateTime);
        copy.setScheduleStatus(this.scheduleStatus);
        copy.setRemark(this.remark);
        copy.setFilePassword(this.filePassword);
        copy.setProcessDateTime(this.processDateTime);
        return copy;
    }
}
