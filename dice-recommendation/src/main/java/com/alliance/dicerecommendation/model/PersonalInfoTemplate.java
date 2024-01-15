package com.alliance.dicerecommendation.model;

import org.springframework.data.cassandra.core.cql.Ordering;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.core.mapping.Table;

import lombok.Data;

@Data
@Table("EVT_PERSONAL_INFO_TEMPLATE")
public class PersonalInfoTemplate extends BaseInfo{
    @PrimaryKeyColumn(name = "PERSONAL_INFO_TEMPLATE_ID", ordinal = 0, type = PrimaryKeyType.PARTITIONED, ordering = Ordering.ASCENDING)
    private Integer personalInfoTemplateId;

    @Column(value = "PERSONAL_INFO_NAME")
    private String personalInfoTemplateName;

    @Column(value = "DESCRIPTION")
    private String description;
}
