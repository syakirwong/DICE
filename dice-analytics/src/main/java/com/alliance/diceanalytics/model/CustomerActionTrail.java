package com.alliance.diceanalytics.model;

import com.alliance.diceanalytics.model.DataField.ActionStatus;
import com.datastax.oss.driver.api.core.uuid.Uuids;
import lombok.Data;
import org.springframework.data.cassandra.core.cql.Ordering;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.CassandraType;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.core.mapping.Table;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Data
@Table(value = "EVT_CUSTOMER_ACTION_TRAIL_LOG")
public class CustomerActionTrail extends BaseInfo {

    @PrimaryKeyColumn(name = "ID", ordinal = 0, type = PrimaryKeyType.PARTITIONED, ordering = Ordering.ASCENDING)
    @CassandraType(type = CassandraType.Name.UUID)
    private UUID id = Uuids.timeBased();

    @Column(value = "SESSION_ID")
    private String sessionId;

    @Column(value = "CAMPAIGN_ID")
    private Integer campaignId;

    @Column(value = "EVENT")
    private String event;

    @Column(value = "ACTION")
    private String action;

    @Column(value = "ACTION_PAGE")
    private String actionPage;

    @Column(value = "CHANNEL")
    private String channel;

    @Column(value = "ENGAGEMENT_MODE")
    @CassandraType(type = CassandraType.Name.SET, typeArguments = {CassandraType.Name.TEXT})
    private Set<String> engagementMode;

    @Column(value = "CIF_NO")
    private String cifNo;

    @Column(value = "DEVICE_ID")
    private String deviceId;

    @Column(value = "DEVICE_PLATFORM")
    private String devicePlatform;

    @Column(value = "IS_LOGIN")
    private Boolean isLogin;

    @Column(value = "ACTION_STATUS")
    @Enumerated(EnumType.STRING)
    private ActionStatus actionStatus;

    @Override
    public boolean equals(Object o) {
        if (o instanceof CustomerActionTrail){
            CustomerActionTrail trail = (CustomerActionTrail) o;
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
