package com.alliance.dicerecommendation.model;

import java.util.UUID;

import org.springframework.data.cassandra.core.cql.Ordering;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.CassandraType;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.core.mapping.Table;

import com.datastax.oss.driver.api.core.uuid.Uuids;

import lombok.Data;

@Data
@Table("EVT_DATA_MISMATCH_HISTORY_LOG")
public class DataMismatchHistoryLog extends BaseInfo {
    @PrimaryKeyColumn(name = "DATA_MISMATCH_HISTORY_LOG_ID", ordinal = 0, type = PrimaryKeyType.PARTITIONED, ordering = Ordering.ASCENDING)
    @CassandraType(type = CassandraType.Name.UUID)
    private UUID dataMismatchHistoryId = Uuids.timeBased();

    @Column(value = "CAMPAIGN_ID")
    private Integer campaignId;

    @Column(value = "UUID_TYPE")
    private String uuidType;

    @Column(value = "UUID")
    private String uuid;

    @Column(value = "UPLOADED_FILE_ID")
    private Integer uploadedFileId;

    @Column(value = "CAMPAIGN_SCHEDULE_ID")
    private String campaignScheduleId;

    @Column(value = "HEADER_MAPPING_ID")
    private Integer headerMappingId;

    @Column(value = "DATA_NAME")
    private String dataName;

    @Column(value = "ORIGINAL_DATA_VALUE")
    private String originalDataValue;

    @Column(value = "MATCHING_DATA_VALUE")
    private String matchingDataValue;

    @Column(value = "IS_SUCCESS_PROFILE_CHECK")
    private Boolean isSuccessProfileCheck;

    @Column(value = "DESCRIPTION")
    private String description;

    @Column(value = "REMARK")
    private String remark;
}


