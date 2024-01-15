package com.alliance.dicerecommendation.model;

import org.springframework.data.cassandra.core.cql.Ordering;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.core.mapping.Table;

import lombok.Data;

@Data
@Table("EVT_UPLOADED_FILE_HISTORY_LOG")
public class UploadedFileHistory extends BaseInfo {
    @PrimaryKeyColumn(name = "UPLOADED_FILE_ID", ordinal = 0, type = PrimaryKeyType.PARTITIONED, ordering = Ordering.ASCENDING)
    private Integer uploadedFileId;

    @Column(value = "FILE_NAME")
    private String fileName;

    @Column(value = "FILE_FORMAT")
    private String fileFormat;

    @Column(value = "DESCRIPTION")
    private String description;

    @Column(value = "TOTAL_ROW")
    private Integer totalRow;

    @Column(value = "TOTAL_COLUMN")
    private Integer totalColumn;

    @Column(value = "TOTAL_SHEET")
    private Integer totalSheet;

    @Column(value = "IS_READ_HEADER")
    private Boolean isReadHeader;

    @Column(value = "TARGETED_CAMPAIGN_LIST")
    private String targetedCampaignList;

    @Column(value = "TOTAL_TARGETED_CAMPAIGN")
    private Integer totalTargetedCampaign;

    @Column(value = "TOTAL_TRIGGER_REQUEST")
    private Integer totalTriggerRequest;
}
