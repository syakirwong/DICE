package com.alliance.dicerecommendation.model;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;

import org.springframework.data.cassandra.core.mapping.CassandraType;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.core.mapping.Table;
import org.springframework.data.cassandra.core.cql.Ordering;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;

import com.alliance.dicerecommendation.model.DataField.TriggerStatus;
import com.datastax.oss.driver.api.core.uuid.Uuids;

import lombok.Data;

@Data
@Table(value = "EVT_RECOMMENDATION_LIST")
public class Recommendation extends BaseInfo {

    @PrimaryKeyColumn(name = "RECOMMENDATION_ID", ordinal = 0, type = PrimaryKeyType.PARTITIONED, ordering = Ordering.ASCENDING)
    @CassandraType(type = CassandraType.Name.UUID)
    private UUID recommendationId = Uuids.timeBased();

    @Column(value = "CAMPAIGN_ID")
    private Integer campaignID;

    @Column(value = "CAMPAIGN_PRIORITY")
    private Integer campaignPriority;

    @Column(value = "CAMPAIGN_NAME")
    private String campaignName;

    @Column(value = "CAMPAIGN_DESCRIPTION")
    private String campaignDescription;

    @Column(value = "CAMPAIGN_START_DATE")
    private Date campaignStartDate;

    @Column(value = "CAMPAIGN_END_DATE")
    private Date campaignEndDate;

    @Column(value = "REFERRAL_CODE")
    private String referralCode;

    @Column(value = "PERMANENT_LINK_TYPE")
    private String permanentLinkType;

    @Column(value = "PERMANENT_LINK_TEXT")
    private String permanentLinkText;

    @Column(value = "ENGAGEMENT_MODE")
    @CassandraType(type = CassandraType.Name.SET, typeArguments = { CassandraType.Name.TEXT })
    private Set<String> engagementMode;

    @Column(value = "REWARD_AMOUNT")
    private BigDecimal rewardAmount;

    @Column(value = "REWARD_TYPE")
    private String rewardType;

    @Column(value = "TRIGGER_TYPE_ID")
    private Integer triggerTypeId;

    @Column(value = "MESSAGE_TEMPLATE_ID")
    private Map<String, Integer> messageTemplateId;

    @Column(value = "CUSTOMER_SEGMENTATION_ID")
    private Set<Integer> customerSegmentationId;

    @Column(value = "CIF_NO")
    private String cifNo;

    @Column(value = "DEVICE_ID")
    private String deviceId;

    @Column(value = "DEVICE_PLATFORM")
    private String devicePlatform;

    @Column(value = "IS_IGNORE")
    private Boolean isIgnore;

    @Column(value = "IS_TRIGGERED")
    private Boolean isTriggered;

    @Column(value = "IS_CAMPAIGN_UPDATED")
    private Boolean isCampaignUpdated;

    @Column(value = "PUSH_NOTIS_SEND_DATE_TIME")
    private Date pushNotificationSendDateTime;

    @Column(value = "DICE_ACTION_TEMPLATE_ID")
    private Set<Integer> diceActionTemplateId;

    @Column(value = "UPDATE_SEQUENCE")
    private List<String> updateSequence;

    @Column(value = "HIGH_RISK")
    private List<String> highRisk;

    @Column(value = "TRIGGER_STATUS")
    @Enumerated(EnumType.STRING)    
    private TriggerStatus triggerStatus = TriggerStatus.NON_TRIGGER;

    @Column(value = "FLOAT_ICON_IMAGE")
    private String floatIconImage;

}
