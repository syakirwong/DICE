package com.alliance.diceanalytics.model;

import com.datastax.oss.driver.api.core.uuid.Uuids;
import lombok.Data;
import org.springframework.data.cassandra.core.cql.Ordering;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.CassandraType;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.core.mapping.Table;

import java.util.Date;
import java.util.UUID;

@Data
@Table("EVT_ENGAGEMENT_HISTORY_LOG")
public class EngagementHistory extends BaseInfo {

    @PrimaryKeyColumn(name = "ENGAGEMENT_HISTORY_LOG_ID", ordinal = 0, type = PrimaryKeyType.PARTITIONED, ordering = Ordering.ASCENDING)
    @CassandraType(type = CassandraType.Name.UUID)
    private UUID engagementHistoryLogId = Uuids.timeBased();
    private Integer messageTemplateId;
    private String messageRecipient;
    private String communicationChannel;
    private String language;
    private String title;
    private String messageContent;
    private Date sentDateTime;
    private String latestStage;
    private String triggerEventGeneralRuleName;
    private String applicationSessionID;
    private Integer triggerEventGeneralRuleId;
    private Integer campaignId;
}
