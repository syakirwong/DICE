package com.alliance.dicerecommendation.request;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.alliance.dicerecommendation.model.DataField.TriggerStatus;

import edu.umd.cs.findbugs.annotations.Nullable;
import lombok.Data;

@Data
public class CreateRecommendationRequest {
    @Nullable
    private Integer campaignID;

    @Nullable
    private Integer campaignPriority;

    @Nullable
    private String campaignName;

    @Nullable
    private String campaignDescription;

    @Nullable
    private Date campaignStartDate;

    @Nullable
    private Date campaignEndDate;

    @Nullable
    private String referralCode;

    @Nullable
    private String permanentLinkType;

    @Nullable
    private String permanentBackgroundStyle;

    @Nullable
    private String permanentIconImg;

    @Nullable
    private String permanentTitleText;

    @Nullable
    private String permanentTitleStyle;

    @Nullable
    private String permanentLinkDescriptionText;

    @Nullable
    private String permanentLinkDescriptionStyle;

    @Nullable
    private Set<String> engagementModeId;

    @Nullable
    private BigDecimal rewardAmount;

    @Nullable
    private String rewardType;

    @Nullable
    private Integer triggerTypeId;

    @Nullable
    private Map<String, Integer> messageTemplateId;

    @Nullable
    private Set<Integer> customerSegmentationId;

    @Nullable
    private String cifNo;

    @Nullable
    private String deviceId;

    @Nullable
    private String devicePlatform;

    @Nullable
    private Boolean isIgnore;

    @Nullable
    private Boolean isTriggered;

    @Nullable
    private Boolean isCampaignUpdated;

    @Nullable
    private Date pushNotificationSendDateTime;

    @Nullable
    private Set<Integer> diceActionTemplateId;

    @Nullable
    private List<String> updateSequence;

    @Nullable
    private List<String> highRisk;

    @Nullable
    private TriggerStatus triggerStatus;

    @Nullable
    private String floatIconImage;
}
