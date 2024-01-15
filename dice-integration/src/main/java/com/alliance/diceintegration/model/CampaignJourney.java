package com.alliance.diceintegration.model;

import java.util.Date;
import java.util.UUID;

import org.springframework.data.cassandra.core.cql.Ordering;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.core.mapping.Table;

import com.fasterxml.jackson.annotation.JsonFormat;

import edu.umd.cs.findbugs.annotations.Nullable;
import lombok.Data;

import com.datastax.oss.driver.api.core.uuid.Uuids;

@Data
@Table("EVT_CAMPAIGN_JOURNEY_LIST")
public class CampaignJourney {
    @PrimaryKeyColumn(name = "ID", ordinal = 0, type = PrimaryKeyType.PARTITIONED, ordering = Ordering.ASCENDING)
    private UUID id = Uuids.timeBased();

    @Column(value = "REFERENCE_ID")
    private String referenceId;

    @Column(value = "STATUS_CODE")
    private String statusCode;

    @Column(value = "CREATED_ON")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private Date createdOn = new Date();

    @Column(value = "CHANNEL")
    private String channel;

    @Column(value = "TRANSACTION_TYPE")
    private String transactionType;

    @Column(value = "TRANSACTION_DESC")
    private String transactionDesc;

    @Column(value = "IS_PROCESS")
    private Boolean isProcess = false;

    @Column(value = "TABLEVIEW")
    private String tableView;

    @Column(value = "STATUS")
    @Nullable
    private String status;

    @Column(value = "CAMPAIGN_ID")
    @Nullable
    private String campaignId;
}
