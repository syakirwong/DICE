package com.alliance.dicerecommendation.model;

import java.util.List;

import org.springframework.data.cassandra.core.cql.Ordering;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.core.mapping.Table;

import lombok.Data;

@Data
@Table("EVT_HEADER_MAPPING")
public class HeaderMapping extends BaseInfo {
    @PrimaryKeyColumn(name = "HEADER_MAPPING_ID", ordinal = 0, type = PrimaryKeyType.PARTITIONED, ordering = Ordering.ASCENDING)
    private Integer headerMappingId;

    @Column(value = "HEADER_NAME")
    private String headerName;

    @Column(value = "DESCRIPTION")
    private String description;

    @Column(value = "REMARK")
    private String remark;

    @Column(value = "HEADER_TYPE")
    private String headerType;

    @Column(value = "HEADER_NAME_MAPPING")
    private List<String> headerNameMapping;
}

