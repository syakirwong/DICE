package com.alliance.diceanalytics.model;

import com.alliance.diceanalytics.model.DataField.HttpMethod;
import com.alliance.diceanalytics.model.DataField.RequestStatus;
import com.datastax.oss.driver.api.core.uuid.Uuids;
import lombok.Data;
import org.springframework.data.cassandra.core.cql.Ordering;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.CassandraType;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.core.mapping.Table;

import java.util.UUID;

@Data
@Table("EVT_AUDIT_TRAIL_LOG")
public class AuditTrail extends BaseInfo {
    @PrimaryKeyColumn(name = "AUDIT_TRAIL_LOG_ID", ordinal = 0, type = PrimaryKeyType.PARTITIONED, ordering = Ordering.ASCENDING)
    @CassandraType(type = CassandraType.Name.UUID)
    private UUID auditTrailLogId = Uuids.timeBased();

    @Column(value = "EVENT")
    private String event;

    @Column(value = "HTTP_METHOD")
    private HttpMethod httpMethod;

    @Column(value = "END_POINT_URL")
    private String endPointUrl;

    @Column(value = "STATUS_CODE")
    private String statusCode;

    @Column(value = "MESSAGE")
    private String message;

    @Column(value = "REQUEST_STATUS")
    private RequestStatus requestStatus;

    @Column(value = "CODE_LOCATION")
    private String codeLocation;

    @Column(value = "IS_RETRY")
    private Boolean isRetry;

    @Column(value = "IS_SENT_EMAIL")
    private Boolean isSentEmail;
}
