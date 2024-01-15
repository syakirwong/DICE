package com.alliance.dicerecommendation.model;

import java.util.UUID;

import javax.annotation.Nullable;

import org.springframework.data.cassandra.core.mapping.CassandraType;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

import com.datastax.oss.driver.api.core.uuid.Uuids;

import lombok.Data;

@Data
@Table("EVT_REFERRAL_CODE_LIST")
public class ReferralCode extends BaseInfo {

    @PrimaryKey
    @CassandraType(type = CassandraType.Name.UUID)
    private UUID referralCodeUUID = Uuids.timeBased();

    private String codeValue;

    private Integer campaignId;

    @Nullable
    private String cifNo;

    @Nullable
    private String eformUuid;

    @Nullable
    private String campaignJourneyId;
}
