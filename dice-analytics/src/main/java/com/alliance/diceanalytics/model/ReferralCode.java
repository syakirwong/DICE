package com.alliance.diceanalytics.model;

import lombok.Data;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

import java.util.Objects;

@Data
@Table("EVT_REFERRAL_CODE_LIST")
public class ReferralCode extends BaseInfo {
    @PrimaryKey
    private Integer referralCodeId;
    private String codeValue;
    private Integer campaignId;
    private String cifNo;
    private String eformUuid;
    private String campaignJourneyId;

    @Override
    public boolean equals(Object o) {
        if (o instanceof ReferralCode){
            ReferralCode trail = (ReferralCode) o;
            return cifNo.equals(trail.cifNo);
        }
        else
            return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(cifNo);
    }
}
