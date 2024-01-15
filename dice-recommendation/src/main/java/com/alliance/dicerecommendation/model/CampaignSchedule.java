package com.alliance.dicerecommendation.model;

import java.util.Date;
import java.util.UUID;

import org.springframework.data.cassandra.core.cql.Ordering;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.CassandraType;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.core.mapping.Table;

import com.alliance.dicerecommendation.model.DataField.ScheduleStatus;
import com.datastax.oss.driver.api.core.uuid.Uuids;

import lombok.Data;

@Data
@Table("EVT_CAMPAIGN_SCHEDULE")
public class CampaignSchedule extends BaseInfo {
    @PrimaryKeyColumn(name = "CAMPAIGN_SCHEDULE_ID", ordinal = 0, type = PrimaryKeyType.PARTITIONED, ordering = Ordering.ASCENDING)
    @CassandraType(type = CassandraType.Name.UUID)
    private UUID campaignScheduleId = Uuids.timeBased();

    @Column(value = "FILE_PATH")
    private String filePath;

    @Column(value = "FILE_NAME")
    private String fileName;

    @Column(value = "CAMPAIGN_ID")
    private Integer campaignId;

    @Column(value = "ENGAGEMENT_START_DATETIME")
    private Date engagementStartDateTime;

    @Column(value = "SCHEDULE_STATUS")
    private ScheduleStatus scheduleStatus;

    @Column(value = "REMARK")
    private String remark;

    @Column(value = "FILE_PASSWORD")
    private String filePassword;

    @Column(value = "PROCESS_DATETIME")
    private Date processDateTime;

    @Column(value = "PROCESSED_INDEX")
    private Integer processedIndex = 0;
}
