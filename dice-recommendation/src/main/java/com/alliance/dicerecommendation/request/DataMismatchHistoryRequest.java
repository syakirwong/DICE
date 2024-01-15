package com.alliance.dicerecommendation.request;

import javax.annotation.Nullable;

import lombok.Data;

@Data
public class DataMismatchHistoryRequest {
    private Integer campaignId;
    private String uuidType;
    private String uuid;
    private Integer uploadedFileId;
    private String campaignScheduleId;
    private Integer headerMappingId;
    private String dataName;
    private String originalDataValue;
    private String matchingDataValue;
    private Boolean isSuccessProfileCheck;
    @Nullable
    private String description;
    @Nullable
    private String remark;
}

