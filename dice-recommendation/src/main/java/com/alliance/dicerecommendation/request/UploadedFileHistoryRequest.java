package com.alliance.dicerecommendation.request;

import javax.annotation.Nullable;

import lombok.Data;

@Data
public class UploadedFileHistoryRequest {
    @Nullable
    private Integer uploadedFileId;

    @Nullable
    private String fileName;

    @Nullable
    private String fileFormat;

    @Nullable
    private String description;

    @Nullable
    private Integer totalRow;

    @Nullable
    private Integer totalColumn;

    @Nullable
    private Integer totalSheet;

    @Nullable
    private Boolean isReadHeader;

    @Nullable
    private String targetedCampaignList;

    @Nullable
    private Integer totalTargetedCampaign;

    @Nullable
    private Integer totalTriggerRequest;

    @Nullable
    private String createBy;
}
