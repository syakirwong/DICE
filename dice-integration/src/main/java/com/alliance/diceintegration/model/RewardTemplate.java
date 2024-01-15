package com.alliance.diceintegration.model;

import java.math.BigDecimal;

import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

import lombok.Data;

@Data
@Table("EVT_REWARD_TEMPLATE")
public class RewardTemplate extends BaseInfo {
    @PrimaryKey
    private Integer rewardTemplateId;
    private Integer campaignId;
    private String rewardType;
    private BigDecimal rewardAmount;
    private Integer maxReferralCount;
}

